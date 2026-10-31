# Neuester Testnachweis – 31.10.2026, 18:08 UTC

**Code-/Test-Commit** `0f4b3cc34f42be63a1b47a6afbb739dc9df9ecf1` · [GitHub Actions Run 41227672665](https://github.com/ecora2003-max/EG-Rezept-KI/actions/runs/41227672665) · Job `124615113444` · **completed/success**, alle Schritte erfolgreich. Ubuntu / JDK17 / Kotlin1.9.25 / Python3.11, synthetische Testdaten.

| Prüfgruppe | Pass | Fail | Skip |
| --- | ---: | ---: | ---: |
| Rezeptkern / Validator / Mengen / Nutrition | 47 | 0 | 0 |
| Kombinationsgenerator | 10 | 0 | 0 |
| bestätigte Zielplanung | 8 | 0 | 0 |
| On-Device-Fake-Gateway | 13 | 0 | 0 |
| 1/7/56-Tage-Horizont | 7 | 0 | 0 |
| Planungssicherheit / 8 Flags | 7 | 0 | 0 |
| Bedarfsaggregation inkl. fehlender Mengen / Overflow | 16 | 0 | 0 |
| Rezeptmap-Import / Eingangsvalidierung | 14 | 0 | 0 |
| deterministische Revalidierung importierter Entwürfe | 7 | 0 | 0 |
| Android-Hostport mit Privacy-/Offline-Fake | 16 | 0 | 0 |
| **neu: begrenzter typgenauer JSON-Decoder** | **24** | **0** | **0** |
| **Kotlin-Summe** | **169** | **0** | **0** |
| vollständige Draft-2020-12-Kontrakte (2 positive, 16 negative, 56 Plandrafts) | 74 | 0 | 0 |
| Buildprüfung aller öffentlichen Kotlin-Klassen im eigenständigen JAR | 1 | 0 | 0 |

**Reproduzierbare Befehle:** `python3 -m pip install brotli==1.1.0 && bash scripts/test.sh`; darin neu `kotlinc src/main/kotlin/de/eg/recipe/RecipeEngine.kt src/main/kotlin/de/eg/recipe/RecipeDraftImport.kt src/main/kotlin/de/eg/recipe/RecipeDraftRevalidation.kt src/main/kotlin/de/eg/recipe/BoundedDraftJson.kt src/test/kotlin/de/eg/recipe/BoundedDraftJsonTest.kt -include-runtime -d build/bounded-draft-json-tests.jar`, `java -ea -cp build/bounded-draft-json-tests.jar de.eg.recipe.BoundedDraftJsonTest` und `bash scripts/build_library.sh`. CI: `python3 -m pip install jsonschema==4.26.0`, danach je `python3 scripts/full_contract_check.py build/recipe-draft.json build/contract.schema.json` / `build/smart-draft.json` und `python3 scripts/plan_contract_check.py build/meal-plan-drafts.json build/contract.schema.json`. Unveränderte v1-Schema-Blob-SHA `bedde4677342817fe6b883962c8dfbbd60100072` vom gepinnten Architekturcommit `25541fc877751f81848ac160cb0d9577cbcf83c8` (neuester Architektur-HEAD `d7aa7dda1f0a34b52102b35785195e7ade26ceb4`, v1 unverändert). JAR-Check `PASS compiled Kotlin-only release JAR with all required public ports`.

**Neue funktionale Grenze:** `BoundedDraftJson` ist ein echter JSON-Decoder mit strikten Eingabe-/Tiefen-/Knoten-/String- und Zahlenlimits, aber kein JSON-Schema-Validator. `RecipeDraftImport` und `RecipeDraftRevalidation` verwenden weiterhin Host-Fakten und keine untrusted Claims. Ein Android-Gerät, Room-/Compose-App, AICore/ML-Kit-Inferenz oder echte Allergie-/Nährwert-/Quellenbelege wurden **nicht** getestet. Der v1.1-Shopping-Draft-Vertrag wurde bewusst nicht aktiviert.

---

## Historischer, beim vorigen Meilenstein verifizierter Teststand

# Offline-Fachkern und Vertragsprüfung: gemessener Testbericht

**Datum:** 31.10.2026. **Vertrag:** `EG-Architecture@25541fc877751f81848ac160cb0d9577cbcf83c8` (`1.0.0-draft`). **Verifizierter GitHub-Codecommit:** `e50a6c3a6eedf1417818285a895786f290616625`.

## Reproduzierbarkeit

GitHub Actions [Run 41224635664](https://github.com/ecora2003-max/EG-Rezept-KI/actions/runs/41224635664) ist **completed/success**, einschließlich aller Ausführungsschritte und kompletter JSON-Schema-2020-12-Validierung. Runner: Ubuntu, Temurin JDK 17, Kotlin 1.9.25, Python 3.11, freie `brotli==1.1.0` und `jsonschema==4.26.0`, ausschließlich synthetische Daten.

```bash
python3 -m pip install brotli==1.1.0
bash scripts/test.sh
# GitHub CI zusätzlich:
python3 -m pip install jsonschema==4.26.0
# Pinned: EG-Architecture commit 25541fc...
python3 scripts/full_contract_check.py build/recipe-draft.json build/contract.schema.json
python3 scripts/full_contract_check.py build/smart-draft.json build/contract.schema.json
python3 scripts/plan_contract_check.py build/meal-plan-drafts.json build/contract.schema.json
```

Der Runner prüft zunächst per SHA-256 das Archiv `transfer/bundle.part1.b64` + `bundle.part2.b64` (SHA-256 `a5c53a77dfdf74d0cfc6497db5f415cf2abb0a32e7a3a0363ca2dc9f59581838`), entpackt exakt zwei Kotlin-Quellpfade und kompiliert die Kotlin-Klassen. Die erzeugten Plaintext-Dateien sind keine binäre Laufzeitkomponente.

## Tatsächlich ausgeführte Resultate

| Prüfung | Bestanden | Fehlgeschlagen | Übersprungen |
| --- | ---: | ---: | ---: |
| `RecipeEngineTest` – Validierung, Nährwertberechnung, Kontextflags, Meal Prep, No-Side-Effect | 47 | 0 | 0 |
| `CombinatorialGeneratorTest` – 27 Gerichte, Mengen und Unabhängigkeit | 10 | 0 | 0 |
| `GoalAwareRecipePlannerTest` – bestätigte Ziele, Rangfolge, fehlende Daten | 8 | 0 | 0 |
| `OnDeviceRecipeGatewayTest` – Statusprüfung, kein Snapshot-Leak, Fehlerfallback, Fake-Modell | 13 | 0 | 0 |
| `MealPlanHorizonTest` – 1/7/56 Tage, Planidentität, No-Side-Effects | 7 | 0 | 0 |
| `MealPlanSafetyTest` – Einschränkungen, acht Flags, unbekannte Lagerkapazität | 7 | 0 | 0 |
| `MealPlanDemandTest` – Dubletten-Konflikte, Struktur, Einheiten, Überlauf, Nullmengen | 16 | 0 | 0 |
| `RecipeDraftImportTest` – strikte importierte Mengen, Flags, Status, Metadaten | 14 | 0 | 0 |
| `RecipeDraftRevalidationTest` – unabhängiger Allergen-/Faktencheck nach Einlesen | 7 | 0 | 0 |
| JSON-Schema Draft 2020-12 – zwei positive Nachrichten | 2 | 0 | 0 |
| JSON-Schema Draft 2020-12 – je acht absichtlich ungültige Nachrichten abgewiesen | 16 | 0 | 0 |
| JSON-Schema Draft 2020-12 – 56 synthetische Plan-Rezepte | 56 | 0 | 0 |

**129/129 Kotlin-Testfälle und 74/74 vollständige Schema-Prüffälle bestanden**. Zusätzlich zwei einfache Vertragsstrukturprüfungen `scripts/contract_check.py`. CI-Log: `SUMMARY passed=47 failed=0`, `SUMMARY combinatorial_generator passed=10 failed=0`, `SUMMARY goal_aware_planner passed=8 failed=0`, `SUMMARY on_device_gateway passed=13 failed=0`, zweimal `PASS full draft 2020-12 recipe.draft message` und zweimal `PASS 8 malformed messages rejected`, `SUMMARY meal_plan_horizon passed=7 failed=0`, `SUMMARY meal_plan_safety passed=7 failed=0`, `SUMMARY meal_plan_demand passed=16 failed=0`, `SUMMARY recipe_draft_import passed=14 failed=0`, `SUMMARY recipe_draft_revalidation passed=7 failed=0`, `PASS 56 plan drafts satisfy pinned draft-2020-12 schema`. Diese Resultate belegen **keine** getestete Android-App.

## Neuer Import-Sicherheitsnachweis

Der Host-map-Reader weist falsche Versionen, fremde Nachrichtentypen, bestätigte/gebuchte Status, unvollständige oder typfehlerhafte Flagkombinationen, nichtganzzahlige/negative/überlaufende Mengen, inkompatible Einheiten, fehlende Schritte sowie manipulierte Buchungsbehauptungen zurück. Selbst ein extern gesetztes `STRUCTURALLY_VALID`-Merkmal bleibt **untrusted**. Die zweite Validierungsstufe führt bekannte Allergene/Verbote mit unabhängigen Host-Fakten neu durch; unbekannte Roh-/Gareinheiten ergeben keine scheinbar vollständigen Nährstoffbilanzen. Vollständige JSON-Parsing- und Android-Geräteintegration stehen weiterhin aus. [Importvertrag](IMPORT.md).

## Grenz- und Invariantenfälle

- Die drei Flags laufen in allen **acht** Kombinationen, nicht gewählter Snapshot darf keinerlei Einfluss haben; ausgewählt aber fehlend → `null` und Warnung statt Nullbestand.
- Bestätigte Allergie, eingeschränkte Ernährung und verbotener Lebensmitteltyp werden geprüft und gegebenenfalls blockiert. Unbekannter Allergenstatus erzeugt zwingend Review. Externe/untrusted Kandidaten erhalten keine automatische Verträglichkeitsfreigabe.
- Unterschiedliche Mengenbasis und Roh-/Gargewicht, ungültige oder widersprüchliche Mengen, zwei/vier Portionen, Überlauf, fehlende Gramm-/Milliliterdaten.
- Fehlendes Vitamin D/B12 lässt genau diese Summen unbekannt, während vorhandene Makrowerte erhalten bleiben; `BigDecimal` statt unkontrollierter Float-Arithmetik.
- Keine unbestätigten Nährstoffziele; bestätigte Ziele nur dann `IN_RANGE`, wenn zugehörige pro-Portion-Mengen vollständig belegt sind.
- Einkaufsbedarf ist kein bestätigter Kauf/Bestand; Angebote nur mit gültigem Zeitfenster und gültiger Herkunft; MHD ist keine automatische Verderbsbehauptung.
- Meal-Prep-Einfriermengen, unbekannte/zu kleine Gefrierkapazität, keine fiktive Kühlkettenfreigabe, Rohzutaten nicht doppelt auf Portionen gebucht.
- Aus `propose` folgen **keine** realen Einkäufe, Vorratsbewegungen, Herstellungen, Rezepteinträge oder Verzehrereignisse.
- Plan-Bedarf verwirft widersprüchliche Vorkommen mit identischer ID, fehlerhafte Mengenbasis/Einheit und unzulässige Zahltypen **explizit**; fehlende Mengenwerte mit bekannter Basis bleiben unbekannt. Identische Wiederholungen zählen nur einmal, damit keine stille Unterzählung oder doppelte Rohwarenbuchung entsteht.

## Zusätzlich getesteter Modellgateway

Ein *Fake* simuliert lokalen Modellstatus `AVAILABLE`, `DOWNLOADABLE`, `DOWNLOADING`, `UNAVAILABLE` und `UNKNOWN`, Status-/Generierungsfehler, leere und übergroße Modellausgaben, blockierte Allergen-/Food-Konflikte und das Nichtweitergeben der deaktivierten sowie unfreigegebenen Snapshots. Modellresultate werden **niemals** ungeprüft als sicher freigegeben; der reine Offline-Fallback benötigt keine Modellinstanz.

## Grenzen

Die Engine verwendet keine persönlichen Ernährungsdaten. Quellen- und Lizenzfragen für reale Produkte/Nährwerte sind Sache der lesenden Host-Adapter. **Es wurde kein auf realer Android-Hardware laufendes LLM, keine tatsächliche Room-Anbindung, keine automatische Allergensicherheitsgarantie, kein vollständiges Produktpreisverzeichnis und kein KI-Freitextgenerator nachgewiesen.** Die freie deterministische Offline-Generierung mit 27 variablen Kombinationen und nachvollziehbarer Prüfung ist nutzbar; echte KI-Sprachgenerierung bleibt optionales/offenes Integrationsziel.

## 31.10.2026 – AndroidLocalModelAdapter: Freigabe-Gates und Offline-Fallback

Getesteter Code-SHA: `d7b696771a62b580c72a1addc3b138148f3fcbe0`; [CI Run 41226586359](https://github.com/ecora2003-max/EG-Rezept-KI/actions/runs/41226586359), Job `124611903506`, success. `bash scripts/test.sh` (Kotlin 1.9.25/JDK 17): **145 pass / 0 fail / 0 skip**, einschließlich 16 neuen Fake-Bridge-Prüfungen (Opt-out, fehlende Eingabefreigabe, fehlender Lokalnachweis, fehlende Bridge, Erfolg, Downloadstatus, Inferenzfehler, acht unabhängige Flags, nicht geteilte Snapshots). Vollständige Schema-Prüfung über `python scripts/full_contract_check.py build/recipe-draft.json build/contract.schema.json`, analog `build/smart-draft.json`, und `python scripts/plan_contract_check.py build/meal-plan-drafts.json build/contract.schema.json`: **74 pass / 0 fail / 0 skip**. Der gepinnte `1.0.0-draft`-Schema-Blob `bedde4677342817fe6b883962c8dfbbd60100072` ist auch in `EG-Architecture@404f8c68a80ff77a1c01b02ae6709508b50c3743` unverändert. Keine reale Hardware-, Datenschutz- oder App-Integration nachgewiesen.
