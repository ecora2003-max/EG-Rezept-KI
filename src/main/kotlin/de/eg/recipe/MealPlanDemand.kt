package de.eg.recipe

/** Roll up demand from distinct recipe occurrence IDs, never from views of views.
 * Malformed drafts are rejected rather than silently undercounted.
 */
object MealPlanDemand {
    private data class Sum(var value: Long?, var sources: Int, val missing: MutableList<String>)

    fun aggregate(entries: List<MealPlanDraftPlanner.Occurrence>): List<MealPlanDraftPlanner.Demand> {
        val unique = linkedMapOf<String, MealPlanDraftPlanner.Occurrence>()
        for (entry in entries) {
            require(entry.id.isNotBlank()) { "Occurrence ID is missing" }
            val previous = unique.putIfAbsent(entry.id, entry)
            require(previous == null || previous == entry) {
                "Conflicting occurrences share ID ${entry.id}"
            }
        }
        val out = linkedMapOf<MealPlanDraftPlanner.IngredientKey, Sum>()
        for (entry in unique.values) {
            val payload = entry.draft["payload"] as? Map<*, *>
                ?: throw IllegalArgumentException("Missing recipe payload for ${entry.id}")
            val lines = payload["ingredients"] as? List<*>
                ?: throw IllegalArgumentException("Missing ingredients for ${entry.id}")
            for ((index, line) in lines.withIndex()) {
                val ingredient = line as? Map<*, *>
                    ?: throw IllegalArgumentException("Invalid ingredient $index in ${entry.id}")
                val q = ingredient["quantity"] as? Map<*, *>
                    ?: throw IllegalArgumentException("Missing quantity basis for ${entry.id} ingredient $index")
                val id = (ingredient["foodTypeId"] as? String)?.takeIf { it.isNotBlank() }
                    ?: throw IllegalArgumentException("Missing food type for ${entry.id} ingredient $index")
                val unit = q["unit"] as? String
                require(unit in setOf("g", "ml", "piece", "portion")) {
                    "Unsupported unit for ${entry.id} ingredient $index"
                }
                val basis = q["basis"] as? String
                require(basis in setOf("NET", "DRAINED", "GROSS", "PIECE", "PER_PORTION", "UNKNOWN")) {
                    "Unsupported basis for ${entry.id} ingredient $index"
                }
                val notes = q["notes"] as? String ?: ""
                val state = when {
                    notes.startsWith("RAW ") -> "RAW"
                    notes.startsWith("COOKED ") -> "COOKED"
                    else -> "UNKNOWN"
                }
                val key = MealPlanDraftPlanner.IngredientKey(id, unit!!, basis!!, state)
                val rawAmount = q["amount"]
                val amount = when (rawAmount) {
                    null -> null
                    is Long -> rawAmount
                    is Int -> rawAmount.toLong()
                    else -> throw IllegalArgumentException("Invalid amount type for ${entry.id} ingredient $index")
                }
                require(amount == null || amount > 0L) {
                    "Nonpositive ingredient amount for ${entry.id} ingredient $index"
                }
                val sum = out.getOrPut(key) { Sum(0, 0, mutableListOf()) }
                sum.sources++
                if (amount == null) {
                    sum.value = null
                    sum.missing += entry.id
                } else if (sum.value != null) {
                    sum.value = try { Math.addExact(sum.value!!, amount) }
                    catch (_: ArithmeticException) { sum.missing += entry.id; null }
                } else sum.missing += entry.id
            }
        }
        return out.map { (key, sum) ->
            MealPlanDraftPlanner.Demand(key, sum.value, sum.sources, sum.missing.toList())
        }.sortedWith(compareBy({ it.key.foodTypeId }, { it.key.unit }, { it.key.basis }, { it.key.preparationState }))
    }
}
