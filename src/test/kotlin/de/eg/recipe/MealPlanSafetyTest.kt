package de.eg.recipe

import java.time.LocalDate

object MealPlanSafetyTest {
    private val start=LocalDate.parse("2026-11-01")
    private val facts=RecipeEngine.RecipeFacts(emptyMap())
    private val base=RecipeEngine.Request(
        "BASE","2026-10-31T17:00:00+01:00",start,2,
        mealType="MAIN",equipment=setOf("HOB","POT"))
    private fun req(days:Int=1,baseRequest:RecipeEngine.Request=base,
        slots:List<MealPlanDraftPlanner.Slot> = listOf(MealPlanDraftPlanner.Slot("dinner","MAIN",2))) =
        MealPlanDraftPlanner.Request("synthetic-plan-42",start,days,slots,baseRequest)
    private fun invalid(fn:()->Unit) {
        var failed=false
        try { fn() } catch (_:IllegalArgumentException) { failed=true }
        check(failed)
    }
    @JvmStatic fun main(args:Array<String>){
        var passed=0
        fun test(name:String,fn:()->Unit){fn();passed++;println("PASS "+name)}
        test("confirmed forbidden food excluded") {
            val r=base.copy(restrictions=listOf(RecipeEngine.Restriction("FOOD:dry-bulgur",true)))
            val p=MealPlanDraftPlanner.propose(req(baseRequest=r),facts)
            val lines=(p.occurrences.first().draft["payload"] as Map<*,*>)["ingredients"] as List<*>
            check(lines.none{(it as Map<*,*>)["foodTypeId"]=="dry-bulgur"})
        }
        test("blocked breakfast yields unfilled, not invented meal") {
            val r=base.copy(restrictions=listOf("oats","apple","water").map{
                RecipeEngine.Restriction("FOOD:$it",true)})
            val p=MealPlanDraftPlanner.propose(req(baseRequest=r,slots=listOf(
                MealPlanDraftPlanner.Slot("breakfast","BREAKFAST",1))),facts)
            check(p.occurrences.isEmpty() && p.unfilled.size==1)
        }
        test("unavailable equipment leaves slot unfilled") {
            val p=MealPlanDraftPlanner.propose(req(7,base.copy(equipment=emptySet())),facts)
            check(p.occurrences.isEmpty() && p.unfilled.size==7)
        }
        test("all eight disabled flags isolate snapshots") {
            val snapshot=RecipeEngine.Snapshot(true,RecipeEngine.Completeness.COMPLETE,
                "2026-10-31T17:00:00+01:00",emptyList(),"synthetic","SOURCED")
            for(bits in 0..7) {
                val flags=RecipeEngine.Flags(bits and 1!=0,bits and 2!=0,bits and 4!=0)
                val original=base.copy(flags=flags,stock=snapshot,shopping=snapshot)
                val changed=original.copy(
                    stock=if(flags.considerStock)snapshot else snapshot.copy(source="changed"),
                    shopping=if(flags.considerShopping)snapshot else snapshot.copy(source="changed"))
                val a=MealPlanDraftPlanner.propose(req(baseRequest=original),facts)
                val b=MealPlanDraftPlanner.propose(req(baseRequest=changed),facts)
                check(JsonOutput.encode(a.occurrences.map{it.draft})==JsonOutput.encode(b.occurrences.map{it.draft}))
            }
        }
        test("invalid horizon and duplicate daily slot rejected") {
            for(d in listOf(0,-1,2,14,57)) invalid{MealPlanDraftPlanner.propose(req(d),facts)}
            invalid{MealPlanDraftPlanner.propose(req(slots=listOf(
                MealPlanDraftPlanner.Slot("x","MAIN",1),MealPlanDraftPlanner.Slot("x","MAIN",2))),facts)}
        }
        test("unsupported freezing request never silently ignored") {
            invalid{MealPlanDraftPlanner.propose(req(baseRequest=base.copy(freezePortions=1)),facts)}
        }
        test("invalid view windows rejected") {
            val p=MealPlanDraftPlanner.propose(req(7),facts)
            invalid{p.view(-1,1)};invalid{p.view(0,8)};invalid{p.view(7,1)}
        }
        println("SUMMARY meal_plan_safety passed=$passed failed=0")
    }
}
