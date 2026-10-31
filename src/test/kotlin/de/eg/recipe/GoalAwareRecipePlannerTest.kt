package de.eg.recipe

import java.math.BigDecimal
import java.time.LocalDate
import de.eg.recipe.RecipeEngine.Request
import de.eg.recipe.RecipeEngine.Flags
import de.eg.recipe.RecipeEngine.RecipeFacts
import de.eg.recipe.RecipeEngine.NutrientFact
import de.eg.recipe.RecipeEngine.UnitCode
import de.eg.recipe.RecipeEngine.Basis
import de.eg.recipe.RecipeEngine.State
import de.eg.recipe.RecipeEngine.Evidence
import de.eg.recipe.RecipeEngine.Target

object GoalAwareRecipePlannerTest {
    private fun req(targets: List<Target> = emptyList()) = Request(
        requestId="GOAL", createdAt="2026-10-31T17:00:00+01:00",
        shoppingDate=LocalDate.parse("2026-11-04"),portions=2,flags=Flags(),
        mealType="MAIN", equipment=setOf("HOB","POT"),targets=targets)
    private fun fact(id: String, protein: Int): NutrientFact = NutrientFact(id,UnitCode.G,Basis.NET,State.RAW,
        mapOf("proteinG" to BigDecimal(protein)),emptySet(),Evidence.COMPLETE,
        setOf("VEGAN"),"synthetic-macro", "SOURCED")
    @JvmStatic fun main(args: Array<String>) {
        val baseline=GoalAwareRecipePlanner.propose(req(),RecipeFacts(emptyMap()))
        check(baseline.messages.size==14)
        check(baseline.messages.all { (it["payload"] as Map<*,*> )["status"]=="DRAFT" })
        val unconfirmed=GoalAwareRecipePlanner.propose(req(listOf(Target("proteinG",BigDecimal("5"),null,false))),RecipeFacts(emptyMap()))
        check(JsonOutput.encode(unconfirmed.messages)==JsonOutput.encode(baseline.messages))
        val confirmed=GoalAwareRecipePlanner.propose(req(listOf(Target("proteinG",BigDecimal("5"),null,true))),RecipeFacts(emptyMap()))
        check(confirmed.messages.size==14)
        check(JsonOutput.encode(confirmed.messages).contains("TARGET_UNKNOWN:proteinG"))
        check(confirmed.messages.all { message ->
            val x=((message["payload"] as Map<*,*>)["xRecipeKi"] as Map<*,*>)
            x["generator"]=="OFFLINE_TEMPLATE" && x["verification"]!="BLOCKED"
        })
        val many=GoalAwareRecipePlanner.propose(req().copy(preferredFoodTypes=setOf("dry-quinoa","tomato")),RecipeFacts(emptyMap()))
        check(many.messages.size==14)
        check(JsonOutput.encode(many.messages).contains("dry-quinoa"))
        val nutrients = mutableMapOf<String,NutrientFact>()
        for ((id,protein) in listOf("dry-rice" to 1,"dry-quinoa" to 10,"dry-bulgur" to 5,
            "cooked-beans" to 10,"cooked-chickpeas" to 15,"cooked-lentils" to 20,
            "tomato" to 1,"carrot" to 1,"bell-pepper" to 1)) {
            val cooked=id.startsWith("cooked-")
            nutrients[id]=fact(id,protein).copy(basis=if(cooked) Basis.DRAINED else Basis.NET,
                state=if(cooked) State.COOKED else State.RAW)
        }
        val withGoals=GoalAwareRecipePlanner.propose(
            req(listOf(Target("proteinG",BigDecimal("30"),BigDecimal("32"),true))),RecipeFacts(nutrients))
        val firstExt=((withGoals.messages.first()["payload"] as Map<*,*>)["xRecipeKi"] as Map<*,*>)
        val firstChecks=firstExt["targetsEvaluation"] as List<*>
        check((firstChecks.first() as Map<*,*>) ["status"]=="IN_RANGE")
        check(withGoals.messages.size==14)
        println("SUMMARY goal_aware_planner passed=8 failed=0")
    }
}
