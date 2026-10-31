package de.eg.recipe

/**
 * Android-host boundary for an optional *local* candidate generator.
 * No ML Kit/AICore classes are linked into the portable Kotlin module.
 * The host must establish consent and local-only execution before supplying a bridge.
 * These booleans are host assertions, NOT a hardware or privacy attestation.
 */
object AndroidLocalModelAdapter {
    data class Authorization(
        val userOptedIn: Boolean,
        val requestDataApproved: Boolean,
        val localOnlyExecutionVerified: Boolean
    )

    /** Implement only with a locally executing Android SDK/model; never a cloud client. */
    interface AndroidBridge {
        fun checkStatus(): OnDeviceRecipeGateway.Availability
        fun generateLocally(request: RecipeEngine.Request): List<RecipeEngine.Candidate>
    }

    fun propose(
        request: RecipeEngine.Request,
        facts: RecipeEngine.RecipeFacts,
        authorization: Authorization,
        bridge: AndroidBridge?,
        offline: RecipeEngine.RecipeCandidateGenerator = HybridGenerator()
    ): OnDeviceRecipeGateway.Output {
        fun offlineOnly(reason: String): OnDeviceRecipeGateway.Output =
            OnDeviceRecipeGateway.propose(request, facts, null, offline)
                .copy(fallbackReason = reason)

        // Fail closed: the host must explicitly establish all three conditions.
        if (!authorization.userOptedIn) return offlineOnly("USER_OPT_OUT")
        if (!authorization.requestDataApproved) return offlineOnly("MODEL_INPUT_NOT_APPROVED")
        if (!authorization.localOnlyExecutionVerified) return offlineOnly("LOCAL_ONLY_NOT_VERIFIED")
        if (bridge == null) return offlineOnly("NO_MODEL_ADAPTER")

        val model = object : OnDeviceRecipeGateway.LocalModel {
            override fun availability() = bridge.checkStatus()
            override fun suggest(request: RecipeEngine.Request) = bridge.generateLocally(request)
        }
        // The gateway independently filters all three context flags, checks status,
        // validates every untrusted candidate, and falls back deterministically.
        return OnDeviceRecipeGateway.propose(request, facts, model, offline)
    }
}
