package de.eg.recipe

import java.time.LocalDate

object MealPlanHorizonTest {
    private val start=LocalDate.parse("2026-11-01")
    private val facts=RecipeEngine.RecipeFacts(emptyMap())
    private val base=RecipeEngine.Request(
        "BASE","2026-10-31T17:00:00+01:00",start,2,
        mealType="MAIN",equipment=setOf("HOB","POT"))
    private fun req(days:Int,slots:List<MealPlanDraftPlanner.Slot> =
        listOf(MealPlanDraftPlanner.Slot("dinner","MAIN",2))) =
        MealPlanDraftPlanner.Request("synthetic-plan-42",start,days,slots,base)
    @JvmStatic fun main(args:Array<String>){
        var passed=0
        fun test(name:String, fn:()->Unit){fn();passed++;println("PASS "+name)}
        test("single day draft contract") {
            val p=MealPlanDraftPlanner.propose(req(1),facts)
            check(p.occurrences.size==1 && p.unfilled.isEmpty())
            check(p.occurrences.single().draft["messageType"]=="recipe.draft")
            check(p.view(0,1).demand.isNotEmpty())
        }
        test("unique seven day occurrence IDs") {
            val p=MealPlanDraftPlanner.propose(req(7),facts)
            check(p.occurrences.size==7 && p.occurrences.map{it.id}.toSet().size==7)
        }
        test("56-day stable identity and projections") {
            val p=MealPlanDraftPlanner.propose(req(56),facts)
            check(p.occurrences.size==56)
            check(p.view(0,1).planId==p.view(0,7).planId)
            check(p.view(0,7).planId==p.view(0,56).planId)
        }
        test("1/7/56 demand not double counted") {
            val p=MealPlanDraftPlanner.propose(req(56,listOf(MealPlanDraftPlanner.Slot("dinner","MAIN",2,0))),facts)
            fun amount(days:Int)=p.view(0,days).demand.single{it.key.foodTypeId=="dry-bulgur"}.amount
            check(amount(1)==150L && amount(7)==1050L && amount(56)==8400L)
        }
        test("two distinct meal slots charge need twice") {
            val slots=listOf(MealPlanDraftPlanner.Slot("lunch","MAIN",2,0),
                MealPlanDraftPlanner.Slot("dinner","MAIN",2,0))
            val p=MealPlanDraftPlanner.propose(req(1,slots),facts)
            check(p.view(0,1).demand.single{it.key.foodTypeId=="dry-bulgur"}.amount==300L)
        }
        test("unknown micronutrients remain unknown") {
            val p=MealPlanDraftPlanner.propose(req(1),facts)
            val payload=p.occurrences.single().draft["payload"] as Map<*,*>
            val total=(payload["nutrition"] as Map<*,*>)["total"] as Map<*,*>
            check(total["vitaminDUg"]==null && total["vitaminB12Ug"]==null)
        }
        test("read-only, no booked production or consumption") {
            val p=MealPlanDraftPlanner.propose(req(1),facts)
            val x=(p.occurrences.first().draft["payload"] as Map<*,*>)["xRecipeKi"] as Map<*,*>
            val prep=x["mealPrep"] as Map<*,*>
            check(prep["rawIngredientsCharged"]==false)
            check(prep["preparedPortionsCreated"]==false)
            check(prep["consumptionRecorded"]==false)
        }
        println("SUMMARY meal_plan_horizon passed=$passed failed=0")
    }
}
