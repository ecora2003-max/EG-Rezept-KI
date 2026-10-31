package de.eg.recipe

/** Pure plan builder. Each day/slot is one unique unconfirmed recipe draft. */
fun MealPlanDraftPlanner.propose(
    request: MealPlanDraftPlanner.Request, facts: RecipeEngine.RecipeFacts
): MealPlanDraftPlanner.Plan {
    require(request.planId.isNotBlank() && request.planId.length <= 128)
    require(request.horizonDays in listOf(1, 7, 56))
    require(request.maximumCombinations in 1..27)
    require(request.recipeRequest.freezePortions == 0 && request.recipeRequest.freezerCapacityPortions == null) {
        "Per-slot freezer allocations not supported; never silently discard freezer request"
    }
    require(request.slots.isNotEmpty() && request.slots.size <= 8)
    require(request.slots.all {
        it.id.matches(Regex("[A-Za-z0-9_-]{1,36}")) && it.mealType.isNotBlank() &&
            it.portions > 0 && (it.choiceIndex == null || it.choiceIndex >= 0)
    })
    require(request.slots.map { it.id }.toSet().size == request.slots.size)
    val filled = mutableListOf<MealPlanDraftPlanner.Occurrence>()
    val missing = mutableListOf<String>()
    for (day in 0 until request.horizonDays) {
        val date = request.start.plusDays(day.toLong())
        for ((index, slot) in request.slots.withIndex()) {
            val id = "${request.planId}/${date}/${slot.id}"
            val slotRequest = request.recipeRequest.copy(
                requestId=id, shoppingDate=date, mealType=slot.mealType, portions=slot.portions)
            val options = GoalAwareRecipePlanner.propose(
                slotRequest, facts, request.maximumCombinations).messages
            if (options.isEmpty()) missing += id
            else filled += MealPlanDraftPlanner.Occurrence(
                id,date,slot,options[(slot.choiceIndex ?: (day+index)) % options.size])
        }
    }
    return MealPlanDraftPlanner.Plan(request.planId,request.start,request.horizonDays,
        filled,missing,listOf("PLANNING_ONLY_NOT_PURCHASED_COOKED_OR_CONSUMED") +
            if (missing.isEmpty()) emptyList() else listOf("UNFILLED_SLOTS_REQUIRE_REVIEW"))
}
