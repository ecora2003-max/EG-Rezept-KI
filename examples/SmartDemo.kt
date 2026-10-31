package de.eg.recipe

import java.time.LocalDate

/** Completely synthetic end-to-end example using the goal-aware hybrid recipe generator. */
fun main() {
    val request = RecipeEngine.Request(
        requestId = "DEMO-SMART-01",
        createdAt = "2026-10-31T17:00:00+01:00",
        shoppingDate = LocalDate.parse("2026-11-04"),
        portions = 4,
        flags = RecipeEngine.Flags(true, true, false),
        mealType = "MAIN",
        equipment = setOf("HOB", "POT"),
        freezePortions = 2,
        freezerCapacityPortions = null
    )
    val drafts = GoalAwareRecipePlanner.propose(request, RecipeEngine.RecipeFacts(emptyMap()))
    check(drafts.messages.size == 14)
    println(JsonOutput.encode(drafts.messages.first()))
}
