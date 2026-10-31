package de.eg.recipe
import java.time.LocalDate

object MealPlanDemandTest {
    private val day=LocalDate.parse("2026-11-01")
    private val slot=MealPlanDraftPlanner.Slot("d","MAIN",1)
    private fun occurrence(id:String, n:Long?):MealPlanDraftPlanner.Occurrence {
        val ingredient=mapOf("foodTypeId" to "rice", "quantity" to
            mapOf("amount" to n,"unit" to "g","basis" to "NET","notes" to "RAW weight"))
        return MealPlanDraftPlanner.Occurrence(id,day,slot,
            mapOf("payload" to mapOf("ingredients" to listOf(ingredient))))
    }
    @JvmStatic fun main(args:Array<String>){
        fun sum(vararg values:MealPlanDraftPlanner.Occurrence)=MealPlanDemand.aggregate(values.toList()).single()
        check(sum(occurrence("a",100),occurrence("b",200)).amount==300L)
        val once=occurrence("a",100)
        check(sum(once,once).amount==100L)
        check(sum(occurrence("a",100),occurrence("b",null)).amount==null)
        check(sum(occurrence("a",Long.MAX_VALUE),occurrence("b",1)).amount==null)
        println("SUMMARY meal_plan_demand passed=4 failed=0")
    }
}
