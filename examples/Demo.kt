package de.eg.recipe

import java.time.LocalDate

/** Synthetic example. No user data, DB, network, inventory or consumption events. */
fun main() {
    val request = RecipeEngine.Request(
        requestId = "DEMO-RECIPE-01",
        createdAt = "2026-10-31T17:00:00+01:00",
        shoppingDate = LocalDate.parse("2026-11-04"),
        portions = 2,
        flags = RecipeEngine.Flags(false, false, false),
        mealType = "BREAKFAST",
        equipment = emptySet()
    )
    val output = RecipeEngine.propose(request, RecipeEngine.RecipeFacts(emptyMap()))
    check(output.messages.size == 1)
    println(JsonOutput.encode(output.messages.single()))
}
