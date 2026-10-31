package de.eg.recipe

/** Roll up demand from distinct recipe occurrence IDs, never from views of views. */
object MealPlanDemand {
    private data class Sum(var value:Long?,var sources:Int,val missing:MutableList<String>)
    fun aggregate(entries:List<MealPlanDraftPlanner.Occurrence>):List<MealPlanDraftPlanner.Demand>{
        val out=linkedMapOf<MealPlanDraftPlanner.IngredientKey,Sum>()
        for (entry in entries.distinctBy { it.id }) {
            val payload=entry.draft["payload"] as? Map<*,*> ?: continue
            val lines=payload["ingredients"] as? List<*> ?: continue
            for (line in lines) {
                val ingredient=line as? Map<*,*> ?: continue
                val q=ingredient["quantity"] as? Map<*,*> ?: continue
                val id=ingredient["foodTypeId"] as? String ?: continue
                val unit=q["unit"] as? String ?: continue
                val basis=q["basis"] as? String ?: continue
                val notes=q["notes"] as? String ?: ""
                val state=when {
                    notes.startsWith("RAW ")->"RAW"
                    notes.startsWith("COOKED ")->"COOKED"
                    else->"UNKNOWN"
                }
                val key=MealPlanDraftPlanner.IngredientKey(id,unit,basis,state)
                val amount=(q["amount"] as? Long)?.takeIf{it>0L}
                val sum=out.getOrPut(key){Sum(0,0,mutableListOf())}
                sum.sources++
                if (amount==null) {
                    sum.value=null
                    sum.missing+=entry.id
                } else if (sum.value!=null) {
                    sum.value=try{Math.addExact(sum.value!!,amount)}
                        catch (_:ArithmeticException){sum.missing+=entry.id;null}
                } else sum.missing+=entry.id
            }
        }
        return out.map{(k,s)->MealPlanDraftPlanner.Demand(k,s.value,s.sources,s.missing.toList())}
            .sortedWith(compareBy({it.key.foodTypeId},{it.key.unit},{it.key.basis},{it.key.preparationState}))
    }
}
