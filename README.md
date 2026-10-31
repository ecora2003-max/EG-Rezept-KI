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

## Ohne Server, API-Schlüssel oder Bezahldienst testen

Der Kern benötigt lediglich JDK 17+, Kotlin-Compiler 1.9+, Python 3. Die ursprüngliche Kern-Quelldatei und ihr 47-Test-Treiber werden in diesem Repository aus einem **SHA-256-validierten komprimierten Quellarchiv** wiederhergestellt; die weiteren Generatoren, Tests und Beispiele liegen direkt im Repo als lesbare `.kt`-Dateien. Zur einmaligen Wiederherstellung wird das freie Python-Paket `brotli==1.1.0` benötigt, **nicht** für die Kotlin-Runtime.

```bash
python3 -m pip install brotli==1.1.0
bash scripts/test.sh
```

Der Runner stellt die beiden Quellen aus `transfer/bundle.part1.b64` und `bundle.part2.b64` wieder her, prüft ihre Prüfsumme, kompiliert sie mit Kotlin, führt **65 Regressionstests** aus und kontrolliert beide synthetischen Demo-Vertragsnachrichten auf grundlegende Gültigkeit. Vollständige Draft-2020-12-Validierung mit **2 gültigen und 16 bewusst fehlerhaften** Nachrichten läuft zusätzlich im [GitHub-Actions-Workflow](.github/workflows/offline-tests.yml) gegen den gepinnten Architekturvertrag.

**Verifizierter Build:** [Run 41220695779](https://github.com/ecora2003-max/EG-Rezept-KI/actions/runs/41220695779), Commit `c66d7be437a47af052cd3af878ff06fc3ca0b250`, **65/65 Tests erfolgreich**, vollständige JSON-Schema-Prüfungen bestanden.

## Android und Einschränkungen

Codex übernimmt die reinen Kotlin-Quelldateien in das bestehende Android-/JVM-Source-Set, liefert optional Snapshots aus vorhandenen Room-Schnittstellen **read-only** und lässt alle Entwürfe vor einer expliziten Nutzerentscheidung prüfen. Keine neue Datenbank, kein Server und keine Änderung der Haupt-App sind Teil dieses Repositories. Empfohlenes Entry Point: `GoalAwareRecipePlanner.propose(request, facts)`; siehe [Integration](docs/INTEGRATION.md) und [SmartDemo](examples/SmartDemo.kt).

**Nicht behauptet:** frei formulierende LLM-/On-Device-Rezept-KI, Verfügbarkeit von Gemini Nano auf einem konkreten Android-Gerät, reale Room-/Compose-Integration, lückenlose Produkt- oder Allergendaten, Lebensmittelsicherheit bei Kühlung/Kreuzkontakt, verbindliche Kosten oder Ernährungsberatung. Das generative Offline-Verfahren ist regel- und kombinatorikbasiert. [On-device-KI-Prüfung](docs/ON_DEVICE_AI.md) · [Testbericht](docs/TEST_REPORT.md) · [aktueller Status](docs/STATUS.md).
