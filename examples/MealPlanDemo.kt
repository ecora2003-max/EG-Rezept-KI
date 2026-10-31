package de.eg.recipe

import java.time.LocalDate

/** 56 synthetic drafts from one plan; never accesses user files or books stock. */
fun main() {
    val date=LocalDate.parse("2026-11-01")
    val request=RecipeEngine.Request(
        requestId="SYNTHETIC-PLAN",createdAt="2026-10-31T17:00:00+01:00",
        shoppingDate=date,portions=2,mealType="MAIN",equipment=setOf("HOB","POT"))
    val plan=MealPlanDraftPlanner.propose(
        MealPlanDraftPlanner.Request("SYNTHETIC-56-DAY-PLAN",date,56,
            listOf(MealPlanDraftPlanner.Slot("dinner","MAIN",2)),request),
        RecipeEngine.RecipeFacts(emptyMap()))
    check(plan.occurrences.size==56 && plan.unfilled.isEmpty())
    check(plan.view(0,7).planId==plan.view(0,56).planId)
    println(JsonOutput.encode(plan.occurrences.map{it.draft}))
}
