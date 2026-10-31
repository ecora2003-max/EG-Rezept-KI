package de.eg.recipe
import java.time.LocalDate

object MealPlanDemandTest {
    private val day = LocalDate.parse("2026-11-01")
    private val slot = MealPlanDraftPlanner.Slot("d", "MAIN", 1)
    private fun occurrence(id: String, n: Any?, unit: String = "g", basis: String = "NET", notes: String = "RAW weight"):
        MealPlanDraftPlanner.Occurrence {
        val ingredient = mapOf("foodTypeId" to "rice", "quantity" to
            mapOf("amount" to n, "unit" to unit, "basis" to basis, "notes" to notes))
        return MealPlanDraftPlanner.Occurrence(id, day, slot,
            mapOf("payload" to mapOf("ingredients" to listOf(ingredient))))
    }
    private fun invalid(block: () -> Unit) {
        var rejected = false
        try { block() } catch (_: IllegalArgumentException) { rejected = true }
        check(rejected)
    }
    @JvmStatic fun main(args: Array<String>) {
        var passed = 0
        fun test(name: String, block: () -> Unit) { block(); passed++; println("PASS $name") }
        fun sum(vararg values: MealPlanDraftPlanner.Occurrence) = MealPlanDemand.aggregate(values.toList()).single()
        test("distinct occurrence quantities add") {
            check(sum(occurrence("a", 100L), occurrence("b", 200L)).amount == 300L)
        }
        test("identical repeated occurrence deduplicates") {
            val once = occurrence("a", 100L)
            check(sum(once, once).amount == 100L)
        }
        test("missing amount keeps total unknown") {
            val result = sum(occurrence("a", 100L), occurrence("b", null))
            check(result.amount == null && result.missingFrom == listOf("b"))
        }
        test("overflow keeps total unknown") {
            check(sum(occurrence("a", Long.MAX_VALUE), occurrence("b", 1L)).amount == null)
        }
        test("conflicting duplicate ID is rejected") {
            invalid { sum(occurrence("same", 100L), occurrence("same", 200L)) }
        }
        test("same ID with different date is rejected") {
            val a = occurrence("same", 100L)
            invalid { sum(a, a.copy(date = day.plusDays(1))) }
        }
        test("missing payload is rejected") {
            invalid { MealPlanDemand.aggregate(listOf(occurrence("a", 100L).copy(draft = emptyMap()))) }
        }
        test("missing ingredients are rejected") {
            invalid { MealPlanDemand.aggregate(listOf(occurrence("a", 100L).copy(draft = mapOf("payload" to emptyMap<String, Any?>())))) }
        }
        test("missing quantity basis is rejected") {
            val draft = mapOf("payload" to mapOf("ingredients" to listOf(mapOf("foodTypeId" to "rice"))))
            invalid { MealPlanDemand.aggregate(listOf(occurrence("a", 100L).copy(draft = draft))) }
        }
        test("negative and zero quantities are rejected") {
            invalid { sum(occurrence("a", -1L)) }
            invalid { sum(occurrence("a", 0L)) }
        }
        test("unsupported amount types are rejected") {
            invalid { sum(occurrence("a", 1.5)) }
            invalid { sum(occurrence("a", "100")) }
        }
        test("integer JSON amount is accepted without float") {
            check(sum(occurrence("a", 100)).amount == 100L)
        }
        test("incompatible units are not combined") {
            val totals = MealPlanDemand.aggregate(listOf(occurrence("a", 100L), occurrence("b", 100L, "ml")))
            check(totals.size == 2 && totals.map { it.amount } == listOf(100L, 100L))
        }
        test("raw and cooked are not combined") {
            val totals = MealPlanDemand.aggregate(listOf(occurrence("a", 100L), occurrence("b", 100L, notes = "COOKED weight")))
            check(totals.size == 2)
        }
        test("invalid unit and basis are rejected") {
            invalid { sum(occurrence("a", 100L, "kg")) }
            invalid { sum(occurrence("a", 100L, basis = "DENSITY_GUESSED")) }
        }
        test("empty occurrence ID is rejected") {
            invalid { sum(occurrence("", 100L)) }
        }
        println("SUMMARY meal_plan_demand passed=$passed failed=0")
    }
}
