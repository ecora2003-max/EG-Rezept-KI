# EG-Rezept-KI — unabhängige, offlinefähige Rezeptgenerierung

**Modulstatus:** `PARTIAL_READY` (ausführbarer Offline-Fachkern geprüft; freie neuronale KI-Generierung und Android-Geräteintegration noch nicht nachgewiesen). Dieses Repository liefert ausschließlich **Rezeptentwürfe** für die Android-App Ernährungsgedächtnis. Die gemeinsame semantische Vertragsquelle ist [EG-Architecture](https://github.com/ecora2003-max/EG-Architecture) `1.0.0-draft`. Die produktiven Rezept-, Lager- und Verzehrdatenbanken werden weder ersetzt noch beschrieben.

## Leistungsumfang

- `RecipeEngine`: austauschbarer `RecipeCandidateGenerator`, deterministischer `validate`-Port, exakter `estimateNutrition`-Rechner auf Basis belegter pro-100g/ml-Nährwerte, `recipe.draft`-JSON-Serialisierung.
- `CombinatorialGenerator`: **27 unterschiedliche** zulässige Grundkombinationen aus Getreide, bereits gegarten Hülsenfrüchten und Gemüse, mit individuell ausgewiesenen Einheiten, Roh-/Gargewichten und Zubereitungsschritten. Keine Behauptung über individuelle Verträglichkeit.
- `HybridGenerator`: zwei feste Hauptgerichte plus standardmäßig zwölf kombinatorisch erzeugte Hauptgerichte; `GoalAwareRecipePlanner` sortiert diese nach **ausschließlich bestätigten** Nährstoffzielen und dokumentiert unbekannte Zielerfüllung.
- `RecipeEngine.OfflineGenerator`: drei feste Basisgerichte, darunter Frühstück. `ProvidedCandidates`: gesonderter Input-Port für **unvertrauenswürdige** Modellvorschläge, die immer durch denselben Validator laufen.
- Drei **unabhängige** Optionen `considerStock`, `considerShopping`, `considerOffers`. Ausschließlich explizit übergebene, entsprechend qualifizierte und datierte Snapshots werden gelesen. Einkaufsliste ist kein Bestand; abgelaufenes Angebot ist kein gültiger Preis.
- Bestätigte Allergene, Ernährungseinschränkungen und explizit gesperrte Lebensmittel werden geprüft; unbekannte Sicherheit bleibt `REVIEW_REQUIRED`. Menge, Nährstoff, Haltbarkeit und Lagerplatz werden niemals erfunden.
- Meal Prep rechnet Rohzutaten nur auf die **gesamte geplante Portionenzahl**, mit eingefrorenen Portionen als Planung; `rawIngredientsCharged=false`, `preparedPortionsCreated=false`, `consumptionRecorded=false`. Keine Buchung erfolgt.

## Optionale 1-/7-/56-Tageplanung

Ein separater `MealPlanDraftPlanner` erzeugt aus synthetischen Tages-Slots und bestätigten Eingaben standardisierte, einzeln validierte `recipe.draft`-Nachrichten. Dieselbe Plan-ID verbindet Tages-, Wochen- und 56-Tage-Ansichten; Zutatenmengen werden je eindeutigem Slot aggregiert, nicht durch zusätzliche Planungsübersichten doppelt gezählt. Unbekannte Mengen bleiben unbekannt, Lagerbuchung/Kochen/Verzehr erfolgen nicht. Nicht unterstützte Gefrierzuweisungen werden explizit abgewiesen. [API, Grenzen und Tests](docs/MEAL_PLAN.md).

## Ohne Server, API-Schlüssel oder Bezahldienst testen

Der Kern benötigt lediglich JDK 17+, Kotlin-Compiler 1.9+, Python 3. Die ursprüngliche Kern-Quelldatei und ihr 47-Test-Treiber werden in diesem Repository aus einem **SHA-256-validierten komprimierten Quellarchiv** wiederhergestellt; die weiteren Generatoren, Tests und Beispiele liegen direkt im Repo als lesbare `.kt`-Dateien. Zur einmaligen Wiederherstellung wird das freie Python-Paket `brotli==1.1.0` benötigt, **nicht** für die Kotlin-Runtime.

```bash
python3 -m pip install brotli==1.1.0
bash scripts/test.sh
```

Der Runner stellt die beiden Quellen aus `transfer/bundle.part1.b64` und `bundle.part2.b64` wieder her, prüft ihre Prüfsumme, kompiliert sie mit Kotlin, führt **129 Regressionstests** aus und kontrolliert beide synthetischen Demo-Vertragsnachrichten auf grundlegende Gültigkeit. Vollständige Draft-2020-12-Validierung mit **2 gültigen und 16 bewusst fehlerhaften Nachrichten sowie 56 Plan-Rezepten** Nachrichten läuft zusätzlich im [GitHub-Actions-Workflow](.github/workflows/offline-tests.yml) gegen den gepinnten Architekturvertrag.

**Verifizierter Build:** [Run 41228527601](https://github.com/ecora2003-max/EG-Rezept-KI/actions/runs/41228527601), Commit `edd4340e27f89f1b45a9d0f30993789661607f5c`, **183/183 Tests erfolgreich**, vollständige JSON-Schema-Prüfungen bestanden.

## Strikter, begrenzter JSON-Import

Mit `BoundedDraftJson.decode(text)` steht ein offlinefähiger Kotlin-Decoder bereit, der direkte `recipe.draft`-Roundtrips ermöglicht, genaue Integer/Decimal-Typen erhält und doppelte JSON-Keys, ungültige Escapes sowie Größen-/Tiefenüberschreitungen ablehnt. `RecipeDraftImport.read` und `RecipeDraftRevalidation.revalidate` interpretieren fremde Sicherheits-/Nährwertbehauptungen ausschließlich als **untrusted**, prüfen mit aktuellen Host-Fakten neu und erzeugen keine Buchungen. **Die formale vollständige Schema-Prüfung bleibt zusätzlich erforderlich**. [API/Limitierungen](docs/IMPORT.md).

Der Kotlin-Kern lässt sich mit `bash scripts/build_library.sh` in `build/eg-rezept-ki-core.jar` als Bibliotheks-JAR **ohne enthaltene Kotlin-Laufzeit** bauen; die GitHub-CI kompiliert tatsächlich alle öffentlichen Quellen und prüft die enthaltenen Klassen. Android-Gerätetests bleiben ausstehend.

## Sicheres Wiedereinlesen aus dem Android-Host

`RecipeDraftImport.read(parsedMap)` liest das vertragliche `recipe.draft` als typisierte, **immer unbestätigte** Datenstruktur zurück. `RecipeDraftRevalidation.revalidate(parsedMap, request, facts, confirmedStates)` prüft diese fremden Vorschläge unabhängig mit dem deterministischen Mengen-/Allergen-/Restriktionsvalidator; ein behaupteter `STRUCTURALLY_VALID`-Status oder fremde Nährwerte werden niemals als Vertrauensbeleg übernommen. Fehlerhafte Flags, Mengen, Zustände und Buchungsbehauptungen werden abgewiesen. [Integration und synthetische Regressionen](docs/IMPORT.md).

## Android und Einschränkungen

Codex übernimmt die reinen Kotlin-Quelldateien in das bestehende Android-/JVM-Source-Set, liefert optional Snapshots aus vorhandenen Room-Schnittstellen **read-only** und lässt alle Entwürfe vor einer expliziten Nutzerentscheidung prüfen. Keine neue Datenbank, kein Server und keine Änderung der Haupt-App sind Teil dieses Repositories. Empfohlenes Entry Point: `GoalAwareRecipePlanner.propose(request, facts)`; siehe [Integration](docs/INTEGRATION.md) und [SmartDemo](examples/SmartDemo.kt).

**Nicht behauptet:** frei formulierende LLM-/On-Device-Rezept-KI, Verfügbarkeit von Gemini Nano auf einem konkreten Android-Gerät, reale Room-/Compose-Integration, lückenlose Produkt- oder Allergendaten, Lebensmittelsicherheit bei Kühlung/Kreuzkontakt, verbindliche Kosten oder Ernährungsberatung. Das generative Offline-Verfahren ist regel- und kombinatorikbasiert. [On-device-KI-Prüfung](docs/ON_DEVICE_AI.md) · [Testbericht](docs/TEST_REPORT.md) · [aktueller Status](docs/STATUS.md).

[Vertragsauswirkung der Architekturaktualisierung (v1.1-Shopping-Draft)](docs/ARCHITECTURE_IMPACT_20261031.md): Der Rezept-Core verbleibt bei `1.0.0-draft`; kein stiller v1.1-Wechsel.
