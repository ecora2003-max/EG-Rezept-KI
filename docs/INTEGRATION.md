# Integrationsanleitung – Rezept-KI / 1.0.0-draft

## Version und Systemgrenze

Gelesene Architektur: `ecora2003-max/EG-Architecture@d22862739bd0e3ef8451ce029e54f7c33ee53e40` (`1.0.0-draft`), insbesondere `contracts/v1/contract.schema.json`. Ausgangsmodul bleibt **Entwurfslieferant**. Das private Hauptrepo und alle anderen Module bleiben unberührt. Kotlin-Paket: `de.eg.recipe`. Eine formale vollständige Schema-Gegenprüfung und Codex-Integration fehlen noch; keine Produktionsfreigabe.

## Build und Android

1. `python3 -m pip install brotli==1.1.0 && python3 scripts/restore_sources.py` stellt zwei Klartext-Kotlin-Dateien aus geprüfter Quelle wieder her.
2. Nach bestandenen Offline-Tests `src/main/kotlin/de/eg/recipe/RecipeEngine.kt` als **pure Kotlin-Quelldatei** in einen Android-/JVM-Library-Modul-Source-Set aufnehmen oder als Modul übernehmen. Keine Kotlin-spezifische Datenbank bzw. kein DI-Framework erforderlich.
3. Java 8+-Funktionen `java.time.LocalDate/OffsetDateTime`, `java.math.BigDecimal` und exakte Langzahl-Arithmetik werden verwendet. Empfohlen Android `minSdk 26`; darunter coreLibraryDesugaring und konkrete Geräte-/Regressionstests als Vorbedingung.
4. Codex liest Snapshots **read-only** über eigene `FoodStore`-/Planungs-Adapter, mappt sie explizit auf `RecipeEngine.Request`, `Snapshot`, `OfferSnapshot` und `RecipeFacts`. Ohne ausdrückliche Flags **keine** Snapshotübergabe.
5. `RecipeEngine.propose(request, facts, RecipeEngine.OfflineGenerator())` gibt eine geordnete Liste `messages` und `rejected` mit Begründungen zurück. `JsonOutput.encode(message)` erzeugt `recipe.draft`-JSON. Die Haupt-App muss Vertragsversion, eigenes Schema und `xRecipeKi.verification` prüfen, **vor** einer möglichen Nutzerübernahme.
6. **Kein automatischer Schreibpfad:** Entwurf anzeigen/prüfen; App-Nutzer entscheidet später separat über Rezeptanlage, Kochen und Verzehr via vorhandenen transaktionalen Operationen. Keine direkten Repository-, Room-, Sheet- oder Verbrauchs-Methoden existieren im Engine-Code.
7. Optional: `ProvidedCandidates(listOf(...))` liefert KI-Kandidaten. **Immer** `propose()` verwenden, nie direkt Roh-KI als Rezept akzeptieren. Untrusted Kandidaten werden mindestens `REVIEW_REQUIRED`. Parser/JSON-zu-Kandidaten-Adapter wäre separat von Codex zu implementieren.

## Kotlin-API

- `Request`: `requestId`, `createdAt` ISO-Zeit mit Offset, `shoppingDate`, `portions`, `flags`, optionale `mealType`, `maxMinutes`, `equipment`, `restrictions`, `targets`, `preferredFoodTypes`, `budgetCents/budgetConfirmed`, `freezePortions`, `freezerCapacityPortions`, `stock`, `shopping`, `offers`.
- `Snapshot(shared, coverage, asOf, entries, source, quality)`; nur `shared=true`, Zeit und qualifizierte Herkunft gelten als verfügbar. `OfferSnapshot` mit Preis `priceCents`, Datumsintervall und Herkunft. Die Engine **liest** diese Strukturen nur.
- `NutrientFact`: `foodTypeId`, `unit`, `basis`, `state` (RAW/COOKED), `per100` als `Map<String, BigDecimal?>`, `allergens`, `allergenEvidence`, `dietTags`, `source`, `dataQuality`. Keine Konversion zwischen g/ml/Stück, trocken/gegart oder NET/DRAINED ohne belegten Adapter.
- Restriktionen `ALLERGEN:<CODE>`, `DIET:<CODE>`, `FOOD:<foodTypeId>` gelten nur bei `confirmed=true`; unbekannter bestätigter Code wird blockiert. Targets gelten nur bei `confirmed=true`. Keine impliziten persönlichen Zielwerte.
- Ausgabe `Result.messages` (nur nicht blockierte Entwürfe; Warnungen können eine manuelle Prüfung verlangen), `Result.rejected` (Blockgründe). `DRAFT` entspricht niemals einem `CONFIRMED`-Status.

## Fehlermodelle

- Ungültige Request-IDs, Zeitstempel, Portionen, Zielgrenzen oder Budgets: `IllegalArgumentException` / Parsefehler.
- Widersprüchliche Mengen, falsche Geräte, bestätigter Allergie-/Food-/Diet-Konflikt: Kandidat verworfen, `BLOCKED` mit Codes.
- Fehlende Nährwerte, ungesicherte Allergendaten, nicht belegte Geräte/Freezer/Budget: `REVIEW_REQUIRED`; Nährstoff-/Mengenwerte bleiben `null`.
- Fehlendes ausgewähltes Snapshot: explizite `*_SNAPSHOT_UNAVAILABLE`-Warnung; nicht als leer bekannt.
- Einkaufsliste ist Zukunftsbedarf und **erhöht nicht** `stockAvailable`. Abgelaufene oder unbelegte Angebote erhalten keinen Angebotsvorteil.
- Long-Summenüberlauf in Beständen: Wert unbekannt, keine negative/phantom Menge.
- Modell/API/Netzwerk nicht vorhanden: `OfflineGenerator` als garantierter Fallback.

## Quellen und Vertragslücken

Keine eingebauten realen Nährwert- oder Allergendaten; der Adapter liefert passende bestätigte Fakten. Die einzige freie Erweiterung im `recipe.draft` ist `xRecipeKi`. Strikte JSON-Schema-2020-12-Validierung ist noch nachzuholen; Datenmodell ist nicht an Room gekuppelt. Kein eigener Plan-, Rezept-, Vorrats- oder Verzehrspeicher.
