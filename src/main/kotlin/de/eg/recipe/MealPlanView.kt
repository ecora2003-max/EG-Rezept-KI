package de.eg.recipe

/** A projection of the same plan identity; never add view demand to plan demand. */
fun MealPlanDraftPlanner.Plan.view(
    firstDay:Int, days:Int
):MealPlanDraftPlanner.View {
    require(firstDay>=0 && days>0 && firstDay.toLong()+days<=horizonDays.toLong())
    val from=start.plusDays(firstDay.toLong())
    val until=from.plusDays(days.toLong())
    val subset=occurrences.filter { !it.date.isBefore(from) && it.date.isBefore(until) }
    return MealPlanDraftPlanner.View(planId,firstDay,days,subset,MealPlanDemand.aggregate(subset))
}
