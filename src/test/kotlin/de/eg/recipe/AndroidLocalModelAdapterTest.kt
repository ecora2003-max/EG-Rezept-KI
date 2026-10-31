package de.eg.recipe

import java.time.LocalDate

/** Fake Android bridge only: no device, ML Kit, AICore or real model is exercised. */
object AndroidLocalModelAdapterTest {
    private val facts = RecipeEngine.RecipeFacts(emptyMap())
    private fun request(flags: RecipeEngine.Flags = RecipeEngine.Flags()) = RecipeEngine.Request(
        requestId = "SYNTHETIC-ANDROID-PORT", createdAt = "2026-10-31T17:00:00+01:00",
        shoppingDate = LocalDate.parse("2026-11-04"), portions = 2,
        flags = flags, mealType = "MAIN", equipment = setOf("HOB", "POT")
    )
    private class Fake(
        val state: OnDeviceRecipeGateway.Availability = OnDeviceRecipeGateway.Availability.AVAILABLE,
        val failInference: Boolean = false
    ) : AndroidLocalModelAdapter.AndroidBridge {
        var statusCalls = 0
        var inferCalls = 0
        var lastRequest: RecipeEngine.Request? = null
        override fun checkStatus(): OnDeviceRecipeGateway.Availability {
            statusCalls++
            return state
        }
        override fun generateLocally(request: RecipeEngine.Request): List<RecipeEngine.Candidate> {
            inferCalls++
            lastRequest = request
            if (failInference) error("synthetic inference failure")
            return RecipeEngine.OfflineGenerator().generate(request).take(1)
        }
    }

    @JvmStatic fun main(args: Array<String>) {
        var passed = 0
        fun verify(value: Boolean) { check(value); passed++ }
        val approved = AndroidLocalModelAdapter.Authorization(true, true, true)
        val port = AndroidLocalModelAdapter
        for ((auth, expected) in listOf(
            approved.copy(userOptedIn = false) to "USER_OPT_OUT",
            approved.copy(requestDataApproved = false) to "MODEL_INPUT_NOT_APPROVED",
            approved.copy(localOnlyExecutionVerified = false) to "LOCAL_ONLY_NOT_VERIFIED"
        )) {
            val fake = Fake()
            val result = port.propose(request(), facts, auth, fake)
            verify(result.provider == "OFFLINE_FALLBACK" && result.fallbackReason == expected &&
                fake.statusCalls == 0 && fake.inferCalls == 0)
        }
        verify(port.propose(request(), facts, approved, null).fallbackReason == "NO_MODEL_ADAPTER")
        val ready = Fake()
        val result = port.propose(request(), facts, approved, ready)
        verify(result.provider == "LOCAL_UNTRUSTED_MODEL" && result.recipes.messages.size == 1 &&
            ready.inferCalls == 1)
        val unavailable = Fake(OnDeviceRecipeGateway.Availability.DOWNLOADABLE)
        verify(port.propose(request(), facts, approved, unavailable).provider == "OFFLINE_FALLBACK" &&
            unavailable.inferCalls == 0)
        verify(port.propose(request(), facts, approved, Fake(failInference = true)).fallbackReason ==
            "MODEL_INFERENCE_ERROR")

        val stock = RecipeEngine.Snapshot(true, RecipeEngine.Completeness.COMPLETE,
            "2026-10-31T17:00:00+01:00", emptyList(), "synthetic", "CONFIRMED")
        val offer = RecipeEngine.OfferSnapshot(true, "2026-10-31T17:00:00+01:00", emptyList())
        for (bits in 0..7) {
            val flags = RecipeEngine.Flags(bits and 1 != 0, bits and 2 != 0, bits and 4 != 0)
            val fake = Fake()
            port.propose(request(flags).copy(stock = stock, shopping = stock, offers = offer),
                facts, approved, fake)
            val seen = fake.lastRequest!!
            verify((seen.stock != null) == flags.considerStock &&
                (seen.shopping != null) == flags.considerShopping &&
                (seen.offers != null) == flags.considerOffers)
        }
        val unshared = Fake()
        port.propose(request(RecipeEngine.Flags(true, true, true)).copy(
            stock = stock.copy(shared = false), shopping = stock.copy(shared = false),
            offers = offer.copy(shared = false)), facts, approved, unshared)
        verify(unshared.lastRequest!!.stock == null && unshared.lastRequest!!.shopping == null &&
            unshared.lastRequest!!.offers == null)
        println("SUMMARY android_local_model_adapter passed=$passed failed=0 skipped=0")
    }
}
