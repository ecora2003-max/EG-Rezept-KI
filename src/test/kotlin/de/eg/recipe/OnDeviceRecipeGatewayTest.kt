package de.eg.recipe

import java.time.LocalDate

/** Offline fake-model tests only. Do not claim testing a real model or Android hardware. */
object OnDeviceRecipeGatewayTest {
    private val date = LocalDate.parse("2026-11-04")
    private val noFacts = RecipeEngine.RecipeFacts(emptyMap())
    private fun request(flags: RecipeEngine.Flags = RecipeEngine.Flags()) = RecipeEngine.Request(
        requestId="GATEWAY", createdAt="2026-10-31T17:00:00+01:00", shoppingDate=date,
        portions=2, flags=flags, mealType="MAIN", equipment=setOf("HOB", "POT"))

    private class Fake(
        val status: OnDeviceRecipeGateway.Availability = OnDeviceRecipeGateway.Availability.AVAILABLE,
        val throwOnStatus: Boolean = false,
        val throwOnSuggest: Boolean = false,
        val supply: (RecipeEngine.Request) -> List<RecipeEngine.Candidate> = { r ->
            RecipeEngine.OfflineGenerator().generate(r).take(1)
        }
    ) : OnDeviceRecipeGateway.LocalModel {
        var received: RecipeEngine.Request? = null
        var attempts = 0
        override fun availability(): OnDeviceRecipeGateway.Availability {
            if (throwOnStatus) error("status failure")
            return status
        }
        override fun suggest(request: RecipeEngine.Request): List<RecipeEngine.Candidate> {
            attempts++
            received = request
            if (throwOnSuggest) error("model failure")
            return supply(request)
        }
    }
    @JvmStatic fun main(args: Array<String>) {
        val gate = OnDeviceRecipeGateway
        val offline=gate.propose(request(),noFacts,null)
        check(offline.provider == "OFFLINE_FALLBACK" && offline.fallbackReason == "NO_MODEL_ADAPTER")
        check(offline.recipes.messages.size == 14)

        for (state in listOf(OnDeviceRecipeGateway.Availability.DOWNLOADABLE,
            OnDeviceRecipeGateway.Availability.DOWNLOADING,
            OnDeviceRecipeGateway.Availability.UNAVAILABLE,
            OnDeviceRecipeGateway.Availability.UNKNOWN)) {
            val model=Fake(state)
            val response=gate.propose(request(),noFacts,model)
            check(response.provider == "OFFLINE_FALLBACK" && model.attempts == 0)
        }
        check(gate.propose(request(),noFacts,Fake(throwOnStatus = true)).fallbackReason == "MODEL_STATUS_ERROR")
        check(gate.propose(request(),noFacts,Fake(throwOnSuggest = true)).fallbackReason == "MODEL_INFERENCE_ERROR")
        check(gate.propose(request(),noFacts,Fake(supply={ emptyList() })).fallbackReason == "MODEL_EMPTY_OUTPUT")
        check(gate.propose(request(),noFacts,Fake(supply={ r -> List(33) { RecipeEngine.OfflineGenerator().generate(r).first() } })).fallbackReason == "MODEL_OUTPUT_OVER_LIMIT")

        val passed=gate.propose(request(),noFacts,Fake())
        check(passed.provider == "LOCAL_UNTRUSTED_MODEL")
        check(passed.recipes.messages.size == 1)
        check(passed.recipes.messages.all { m ->
            val payload=m["payload"] as Map<*,*>
            val x=payload["xRecipeKi"] as Map<*,*>
            x["generator"]=="UNTRUSTED_PROVIDED" && x["verification"]=="REVIEW_REQUIRED"
        })
        val blocked=gate.propose(request().copy(restrictions=listOf(RecipeEngine.Restriction("FOOD:dry-rice",true))),
            noFacts,Fake(supply={ r -> RecipeEngine.OfflineGenerator().generate(r).filter { it.template.id == "rice-beans" } }))
        check(blocked.provider == "OFFLINE_FALLBACK" && blocked.fallbackReason == "MODEL_CANDIDATES_BLOCKED")

        val snapshot = RecipeEngine.Snapshot(true,RecipeEngine.Completeness.COMPLETE,
            "2026-10-31T17:00:00+01:00",emptyList(),"synthetic","CONFIRMED")
        val offer=RecipeEngine.OfferSnapshot(true,"2026-10-31T17:00:00+01:00",emptyList())
        for (bits in 0..7) {
            val flags=RecipeEngine.Flags(bits and 1!=0,bits and 2!=0,bits and 4!=0)
            val model=Fake()
            val req=request(flags).copy(stock=snapshot,shopping=snapshot,offers=offer)
            gate.propose(req,noFacts,model)
            val received=model.received!!
            check((received.stock != null)==flags.considerStock)
            check((received.shopping != null)==flags.considerShopping)
            check((received.offers != null)==flags.considerOffers)
        }
        println("SUMMARY on_device_gateway passed=11 failed=0")
    }
}
