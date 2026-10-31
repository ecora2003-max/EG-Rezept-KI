package de.eg.recipe

/**
 * Pure Kotlin read-only projection into the existing Android app's 1/1000 base units.
 * Host owns product-version identity and all writes. No dependency on private app classes.
 */
object AppQuantityBridge {
    const val HOST_MAX_MILLI: Long = 1_000_000_000_000L
    enum class HostUnit { GRAM, MILLILITRE, PORTION }
    enum class Gap { UNRESOLVED_PRODUCT_VERSION, UNKNOWN_AMOUNT, UNSUPPORTED_PIECE_SIZE,
        INCOMPATIBLE_UNIT_BASIS, UNCONFIRMED_IDENTITY, INVALID_QUANTITY, AMOUNT_OVERFLOW,
        UNKNOWN_PREPARATION_STATE }
    data class SourceLine(
        val foodTypeId: String,
        val productVersionId: String?,
        val identityConfirmed: Boolean,
        val quantity: RecipeEngine.Quantity,
        /** Only explicitly host-confirmed conversion; unit is milli-grams per one piece. */
        val confirmedPieceMassMilliGrams: Long? = null
    )
    data class Projection(
        val productVersionId: String?,
        val unit: HostUnit?,
        val amountMilli: Long?,
        val issues: Set<Gap>
    ) {
        /** Structurally mappable, NOT a food safety, availability or persistence approval. */
        val structurallyMappable get() = issues.isEmpty() && productVersionId != null &&
            unit != null && amountMilli != null
    }

    fun project(line: SourceLine): Projection {
        require(line.foodTypeId.isNotBlank()) { "FoodType must be known for identity confirmation" }
        val q = line.quantity
        val gaps = linkedSetOf<Gap>()
        if (line.productVersionId.isNullOrBlank()) gaps += Gap.UNRESOLVED_PRODUCT_VERSION
        if (!line.identityConfirmed) gaps += Gap.UNCONFIRMED_IDENTITY
        if (q.state == RecipeEngine.State.UNKNOWN) gaps += Gap.UNKNOWN_PREPARATION_STATE
        if (q.amount == null) gaps += Gap.UNKNOWN_AMOUNT
        if (q.amount != null && q.amount <= 0L) gaps += Gap.INVALID_QUANTITY
        val unit = when (q.unit) {
            RecipeEngine.UnitCode.G -> HostUnit.GRAM
            RecipeEngine.UnitCode.ML -> HostUnit.MILLILITRE
            RecipeEngine.UnitCode.PORTION -> HostUnit.PORTION
            RecipeEngine.UnitCode.PIECE -> HostUnit.GRAM
        }
        val validBasis = when (q.unit) {
            RecipeEngine.UnitCode.G, RecipeEngine.UnitCode.ML ->
                q.basis in setOf(RecipeEngine.Basis.NET, RecipeEngine.Basis.DRAINED)
            RecipeEngine.UnitCode.PORTION -> q.basis == RecipeEngine.Basis.PER_PORTION
            RecipeEngine.UnitCode.PIECE -> q.basis == RecipeEngine.Basis.PIECE
        }
        if (!validBasis) gaps += Gap.INCOMPATIBLE_UNIT_BASIS
        val milli = when {
            q.amount == null || q.amount <= 0 || !validBasis -> null
            q.unit == RecipeEngine.UnitCode.PIECE &&
                (line.confirmedPieceMassMilliGrams == null || line.confirmedPieceMassMilliGrams <= 0) -> {
                gaps += Gap.UNSUPPORTED_PIECE_SIZE; null
            }
            else -> try {
                val factor = if (q.unit == RecipeEngine.UnitCode.PIECE)
                    line.confirmedPieceMassMilliGrams!! else 1000L
                Math.multiplyExact(q.amount, factor).takeIf { it in 1..HOST_MAX_MILLI }
                    ?: run { gaps += Gap.AMOUNT_OVERFLOW; null }
            } catch (_: ArithmeticException) { gaps += Gap.AMOUNT_OVERFLOW; null }
        }
        return Projection(line.productVersionId?.takeIf { it.isNotBlank() }, unit, milli, gaps)
    }
}
