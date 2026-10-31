package de.eg.recipe

/**
 * Host-only runtime gate for an optional LOCAL model. This module deliberately does
 * not depend on ML Kit, AICore, network access or any particular Android device.
 * The Android app must implement [LocalModel] only after checking runtime support.
 * All model candidates are untrusted and flow through the deterministic validator.
 */
object OnDeviceRecipeGateway {
    enum class Availability { AVAILABLE, DOWNLOADABLE, DOWNLOADING, UNAVAILABLE, UNKNOWN }

    interface LocalModel {
        fun availability(): Availability
        /** Must not access cloud APIs; this contract exposes a LOCAL model only. */
        fun suggest(request: RecipeEngine.Request): List<RecipeEngine.Candidate>
    }

    data class Output(
        val recipes: RecipeEngine.Result,
        val provider: String,
        val availability: Availability,
        val fallbackReason: String?
    )

    fun propose(
        request: RecipeEngine.Request,
        facts: RecipeEngine.RecipeFacts,
        model: LocalModel?,
        offline: RecipeEngine.RecipeCandidateGenerator = HybridGenerator()
    ): Output {
        // Preserve independence: disabled inputs must never even reach model code.
        // A future-dated context snapshot is not an observed fact at request time.
        // Compare instants (not local wall-clock strings) to respect UTC offsets.
        val requestInstant = runCatching {
            java.time.OffsetDateTime.parse(request.createdAt).toInstant()
        }.getOrNull()
        fun dated(value: String?): Boolean = value != null && requestInstant != null &&
            runCatching {
                !java.time.OffsetDateTime.parse(value).toInstant().isAfter(requestInstant)
            }.getOrDefault(false)
        fun approved(snapshot: RecipeEngine.Snapshot?): Boolean =
            snapshot != null && snapshot.shared && dated(snapshot.asOf) &&
                !snapshot.source.isNullOrBlank() && snapshot.quality in setOf("CONFIRMED", "SOURCED")
        fun approved(snapshot: RecipeEngine.OfferSnapshot?): Boolean =
            snapshot != null && snapshot.shared && dated(snapshot.asOf)
        val sanitized = request.copy(
            stock = request.stock?.takeIf { request.flags.considerStock && approved(it) },
            shopping = request.shopping?.takeIf { request.flags.considerShopping && approved(it) },
            offers = request.offers?.takeIf { request.flags.considerOffers && approved(it) }
        )
        fun fallback(state: Availability, why: String): Output = Output(
            RecipeEngine.propose(sanitized, facts, offline), "OFFLINE_FALLBACK", state, why
        )
        if (model == null) return fallback(Availability.UNAVAILABLE, "NO_MODEL_ADAPTER")
        val state = try { model.availability() } catch (_: Exception) {
            return fallback(Availability.UNKNOWN, "MODEL_STATUS_ERROR")
        }
        if (state != Availability.AVAILABLE) return fallback(state, "MODEL_NOT_AVAILABLE")
        val candidates = try { model.suggest(sanitized) } catch (_: Exception) {
            return fallback(state, "MODEL_INFERENCE_ERROR")
        }
        if (candidates.isEmpty()) return fallback(state, "MODEL_EMPTY_OUTPUT")
        if (candidates.size > 32) return fallback(state, "MODEL_OUTPUT_OVER_LIMIT")
        val result = try { RecipeEngine.propose(sanitized, facts, RecipeEngine.ProvidedCandidates(candidates)) }
                     catch (_: Exception) { return fallback(state, "MODEL_OUTPUT_INVALID") }
        // A failed model is not an excuse to auto-accept a blocked proposal.
        if (result.messages.isEmpty()) return fallback(state, "MODEL_CANDIDATES_BLOCKED")
        return Output(result, "LOCAL_UNTRUSTED_MODEL", state, null)
    }
}
