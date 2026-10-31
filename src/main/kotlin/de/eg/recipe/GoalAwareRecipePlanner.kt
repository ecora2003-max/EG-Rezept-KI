package de.eg.recipe

/** Selects only confirmed goals for ranking. No unsupported biological/clinical assumptions. */
object GoalAwareRecipePlanner {
    fun propose(
        request: RecipeEngine.Request,
        facts: RecipeEngine.RecipeFacts,
        maximumCombinations: Int = 12
    ): RecipeEngine.Result {
        // Compile the deterministic composition grammar into the trusted offline template port.
        // Do NOT use this conversion for user/model-provided, untrusted candidates.
        val templates = HybridGenerator(maximumCombinations).generate(request).map { it.template }
        val raw = RecipeEngine.propose(request, facts, RecipeEngine.OfflineGenerator(templates))
        if (request.targets.none { it.confirmed }) return raw
        fun evaluation(message: Map<String, Any?>): List<Map<*, *>> {
            val payload = message["payload"] as? Map<*, *> ?: return emptyList()
            val extension = payload["xRecipeKi"] as? Map<*, *> ?: return emptyList()
            return (extension["targetsEvaluation"] as? List<*>)?.filterIsInstance<Map<*, *>>() ?: emptyList()
        }
        val sorted = raw.messages.sortedWith(
            compareByDescending<Map<String, Any?>> { message -> evaluation(message).count { it["status"] == "IN_RANGE" } }
                .thenBy { message -> evaluation(message).count { it["status"] == "BELOW" || it["status"] == "ABOVE" } }
                .thenBy { message -> evaluation(message).count { it["status"] == "UNKNOWN" } }
        )
        return raw.copy(messages = sorted)
    }
}
