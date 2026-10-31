package de.eg.recipe
import java.time.LocalDate

/** 1/7/56 day read-only plan proposals. */
object MealPlanDraftPlanner {
  data class Slot(val id:String,val mealType:String,val portions:Int,val choiceIndex:Int?=null)
  data class Request(val planId:String,val start:LocalDate,val horizonDays:Int,val slots:List<Slot>,val recipeRequest:RecipeEngine.Request,val maximumCombinations:Int=12)
  data class Occurrence(val id:String,val date:LocalDate,val slot:Slot,val draft:Map<String,Any?>)
  data class IngredientKey(val foodTypeId:String,val unit:String,val basis:String,val preparationState:String)
  data class Demand(val key:IngredientKey,val amount:Long?,val sources:Int,val missingFrom:List<String>)
  data class View(val planId:String,val firstDay:Int,val days:Int,val occurrences:List<Occurrence>,val demand:List<Demand>)
}
