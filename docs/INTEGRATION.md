# Technischer Integrationsvertrag — EG-Rezept-KI

**Unterstützte öffentliche Architekturversion:** `1.0.0-draft`, gelesen aus `EG-Architecture@25541fc877751f81848ac160cb0d9577cbcf83c8`, insbesondere `contracts/v1/contract.schema.json`. Der Haupt-App-Adapter darf Schema, Felder und Validierung nicht still ändern. Kein Zugriff auf fremde Schreibrepositories oder produktive Nutzerdaten.

## Tatsächlich gelesene Host-Einheiten und Produktversionen

Die Android-Haupt-App wurde bei `Ernaerungsgedaechnis@6ba419ed2875c1988f45afee65e2708e7895cacf` **nur lesend** überprüft. Ihre `BaseUnit`-Werte sind `GRAM`, `MILLILITRE`, `PORTION`; Mengen werden in Tausendsteln mit `Amounts.MAX=1_000_000_000_000L` gespeichert. `AppQuantityBridge.project` projiziert bestätigte g/ml/Portionsmengen exakter und begrenzter Integerarithmetik nach `amountMilli`, bewahrt FoodType/Basis/Rohstatus und verweigert unbelegte Stückumrechnung. Die **konkrete** `ProductVersionEntity.id` muss vom Host separat freigegeben werden. Ohne Nutzerbestätigung weder `FoodStore.saveRecipe` noch `FoodStore.savePlan` noch `FoodStore.produce` aufrufen. [Mapping und abgewiesene Fälle](APP_DTO_MAPPING.md).

## Kotlin-Bibliotheksbuild und JSON-Wiedereinlesen

Der verifizierte Build `bash scripts/build_library.sh` erzeugt `build/eg-rezept-ki-core.jar` aus allen 12 reinen Kotlin-Moduldateien ohne Fat-JAR und ohne Android-SDK-Abhängigkeit. Für die Android-App die bestehenden Kotlin-Runtime/Desugaring-Anforderungen im Hostprojekt beachten; **kein** echter AAR-/Geräte-Build wurde durchgeführt. GitHub Actions [41227672665](https://github.com/ecora2003-max/EG-Rezept-KI/actions/runs/41227672665) hat diesen Bibliotheksbuild erfolgreich ausgeführt.

`BoundedDraftJson.decode(json)` bietet einen typgenauen, gegen übergroße Texte, Tiefe, Menge von JSON-Werten und Strings begrenzten Decoder. Danach ausschließlich `RecipeDraftImport.read` und `RecipeDraftRevalidation.revalidate` mit aktuellen Host-Einwilligungen und belegten Zutatenfakten verwenden. **Nicht** aus gültigem JSON auf eine vollständige Schema-Konformität schließen; die formale Prüfung unter `contracts/v1` bleibt zusätzlich verpflichtend. Keine fremden `nutrition`, `confirmed`, `sourceStatus` oder `verification`-Behauptungen vertrauen. [Details](IMPORT.md).

**Neuere Architektur geprüft:** `EG-Architecture@d7aa7dda1f0a34b52102b35785195e7ade26ceb4`. Die kanonische v1-Schema-Blob-SHA `bedde4677342817fe6b883962c8dfbbd60100072` blieb unverändert. Der neue separate v1.1-Shopping-Entwurf ist für die Rezept-KI **nicht** still aktiviert. [Vertragsfolgen](ARCHITECTURE_IMPACT_20261031.md).

## Build und Integration

```bash
python3 -m pip install brotli==1.1.0
python3 scripts/restore_sources.py
bash scripts/test.sh
```

Nach dieser einmaligen Source-Extraktion in einen sauberen Checkout als **pure Kotlin**-Quelldateien (kein Android SDK, kein Python-Bedarf zur Laufzeit) in ein Kotlin/JVM- oder Android-Bibliotheks-Source-Set aufnehmen:

- `src/main/kotlin/de/eg/recipe/RecipeEngine.kt`
- `src/main/kotlin/de/eg/recipe/CombinatorialGenerator.kt`
- `src/main/kotlin/de/eg/recipe/GoalAwareRecipePlanner.kt`
- `src/main/kotlin/de/eg/recipe/OnDeviceRecipeGateway.kt`
- `src/main/kotlin/de/eg/recipe/RecipeDraftImport.kt`
- `src/main/kotlin/de/eg/recipe/RecipeDraftRevalidation.kt`
- `src/main/kotlin/de/eg/recipe/BoundedDraftJson.kt`
- `src/main/kotlin/de/eg/recipe/AppQuantityBridge.kt`
- `src/main/kotlin/de/eg/recipe/AndroidLocalModelAdapter.kt` (optional, mit Gate)

For optional read-only 1/7/56-day planning and demand projections, additionally include:

- `src/main/kotlin/de/eg/recipe/MealPlanDraftPlanner.kt`
- `src/main/kotlin/de/eg/recipe/MealPlanBuild.kt`
- `src/main/kotlin/de/eg/recipe/MealPlanView.kt`
- `src/main/kotlin/de/eg/recipe/MealPlanDemand.kt`

Benötigt `java.math.BigDecimal`, `java.time.LocalDate/OffsetDateTime`; `minSdk 26` empfohlen, ansonsten `coreLibraryDesugaring` samt Android-Gerätetests. Keine verpflichtende Server-/Cloud- oder On-Device-AI-Bibliothek. Namen und Datentypen sind vom tatsächlichen Room-Schema entkoppelt.

## Beispiel: rein lesender Android-Adapter

```kotlin
// Im App-Adapter, nach expliziter Nutzeroption und erteilter Datenfreigabe:
val request = RecipeEngine.Request(
    requestId = "SYNTHETIC-REQUEST-01",
    createdAt = "2026-10-31T17:00:00+01:00",
    shoppingDate = java.time.LocalDate.parse("2026-11-04"),
    portions = 4,
    mealType = "MAIN",
    equipment = setOf("HOB", "POT"),
    flags = RecipeEngine.Flags(
        considerStock = true,
        considerShopping = false,
        considerOffers = true
    ),
    stock = null, // Nur explizit freigegebenes, datiertes StockSnapshot!
    shopping = null,
    offers = null
)
val facts = RecipeEngine.RecipeFacts(emptyMap()) // Reale Nährwerte nur aus belegten Daten.
val result = GoalAwareRecipePlanner.propose(request, facts)
val draftJson = result.messages.map(JsonOutput::encode)
// NICHT speichern, kochen, Vorrat abbuchen oder Verzehr buchen.
// Erst Schema und Reviews prüfen, Entwurf zeigen, Nutzer separat bestätigen lassen.
```

`Result.messages` enthält ausschließlich nicht-blockierte **`recipe.draft`-Entwürfe**, Warnungen und `xRecipeKi.verification` (z. B. `REVIEW_REQUIRED`); `Result.rejected` beschreibt blockierte Kandidaten. Unbekannte Allergene bedeuten **nie „allergenfrei“**.

## Nachweisbarer Rücklese- und Revalidierungsport

Die Host-Anwendung dekodiert JSON typgetreu (ganzzahlige Mengen **nicht** in Float umwandeln) und prüft den vollständigen `1.0.0-draft`-Vertrag. Danach übernimmt `RecipeDraftImport.read(parsedMap)` die streng typisierte, **stets unbestätigte** Darstellung des Entwurfs. `RecipeDraftRevalidation.revalidate(parsedMap, currentRequest, facts, confirmedStates)` prüft anschließend mit dem bestehenden deterministischen Validator erneut Allergene, Verbote, Portionen, Mengenbasis und bezeugte Nährwerte. Fehlende Lebensmittelidentitäten werden vor der Prüfung zurückgewiesen; Quellbehauptungen zu Roh-/Gargewicht, Nährwerten und `xRecipeKi.verification` werden nicht als Belege übernommen. Neue `REVIEW_REQUIRED`-Nachrichten bleiben ohne FoodStore-/Kitchen-Seiteneffekte. Die Read-/Revalidation-Portdateien sind `src/main/kotlin/de/eg/recipe/RecipeDraftImport.kt` und `RecipeDraftRevalidation.kt`; vollständige Dokumentation [IMPORT.md](IMPORT.md). Keine Android-App-/Geräteintegration behauptet.

## Eingangs- und Ausgangs-Ports

- `OnDeviceRecipeGateway.propose(request, facts, model, offline)` kapselt **optionale lokale** Modelle. Vor Aufruf prüft der Host tatsächliche Android-Statusverfügbarkeit (kein Gerät angenommen); der Gateway entfernt inaktive und nicht freigegebene Stock-/Shopping-/Offer-Snapshots **vor der Modellübergabe**, begrenzt Modellergebnisse auf 32 Kandidaten, revalidiert alles über `ProvidedCandidates`, verwirft blockierte Vorschläge und fällt bei nicht verfügbarem/fehlerhaftem Modell deterministisch offline zurück. `LOCAL_UNTRUSTED_MODEL` ist nie eine Sicherheitsfreigabe. 13 Tests mit Fake-Modell sind nachgewiesen, **kein echter ML-Kit/AICore-Test**.
- `RecipeEngine.RecipeCandidateGenerator.generate(request)`: deterministische Offline-Templates oder anderer Kandidatenlieferant.
- `CombinatorialGenerator(maxCandidates=27)`: 27 Getreide×Hülsenfrucht×Gemüse-Gerichte mit klaren Mengen/Zubereitungsschritten, filterbar nach `mealType` und nach Vorlieben sortierbar.
- `HybridGenerator(maximumNewRecipes=12)`: standardmäßig 14 Hauptgerichte (2 Basis + 12 Kombinationen); keine Doppelberechnung über die Quelldatenbank.
- `GoalAwareRecipePlanner.propose(request,facts,maximumCombinations=12)`: bewertet und sortiert die validierten Hybrid-Gerichte nach **bestätigten** Zielbereichen. Nicht bestätigte Ziele werden nie ausgewertet.
- `RecipeEngine.validate(candidate,request,facts)`: pure deterministische `BLOCKED`, `REVIEW_REQUIRED`, `STRUCTURALLY_VALID`-Prüfung. Nutzer-/ML-Ausgaben immer erst prüfen.
- `RecipeEngine.estimateNutrition(candidate,facts)`: `BigDecimal`-Summen pro Rezept und Portion, nur kompatible `g/ml`, Roh-/Gargewichte, `NET/DRAINED`, mit Quellennachweis; fehlende Summen bleiben `null`, Teilbeträge und Lücken separat.
- `RecipeEngine.propose(request,facts,RecipeEngine.ProvidedCandidates(listOf(candidate)))`: KI-Integrationsport, markiert alle fremden Kandidaten mindestens `REVIEW_REQUIRED`. Nicht ungeprüft an App weitergeben.
- `JsonOutput.encode(message)`: lesbares, versioniertes JSON unter Architekturtyp `recipe.draft`; optionale Zusatzfelder ausschließlich in `payload.xRecipeKi`.
- `MealPlanDraftPlanner.propose(request, facts)` und `Plan.view(firstDay, days)`: optionale, rein lesende Planung mit identischer Plan-ID und deterministischer Bedarfsprojektion. `MealPlanDemand.aggregate` akzeptiert nur strukturell auswertbare Entwürfe, verwirft widersprüchliche Duplikat-IDs per `IllegalArgumentException` und kennzeichnet unbekannte Mengen als `null`. **Fehler müssen im Host sichtbar sein**, nicht als leerer Bedarf interpretiert werden. Siehe [MEAL_PLAN.md](MEAL_PLAN.md).

### Inputs

`Request`: `requestId`, Zeitstempel mit Offset, `shoppingDate`, `portions`, `mealType`, `maxMinutes`, `equipment`, `restrictions: List<Restriction(code,confirmed)>`, `targets: List<Target(...,confirmed)>`, `preferredFoodTypes`, optionales bestätigtes Budget, `freezePortions/freezerCapacityPortions`, unabhängige `Flags`, `stock`, `shopping`, `offers`. `InventoryEntry` unterscheidet Food-Type, Menge, Einheit, Basis, Roh/Gargewicht und MHD. `NutrientFact` enthält `foodTypeId`, pro-100g/ml `Map<String,BigDecimal?>`, Einheit, Mengenbasis, Rohzustand, Allergenbelege, Quellen- und Qualitätsstatus.

Verbotene/geschützte Informationen werden ausschließlich über `confirmed=true` für `ALLERGEN:...`, `DIET:...`, `FOOD:...` wirksam. Nicht unterstützte bestätigte Einschränkungen blockieren statt sie zu ignorieren. `StockSnapshot`, `ShoppingSnapshot`, `OfferSnapshot` werden nur bei jeweils aktiviertem Flag gelesen; `false` → kein Einfluss, mit CI-Test über alle 8 Kombinationen. Insbesondere Kaufplanung ≠ bestätigter Bestand.

### Fehler- und Sicherheitsverhalten

- Fehlerhafte Requests (negative Budgets, nicht positive Portionen/Zeitgrenzen, inkonsistente Zielbereiche) führen zu `IllegalArgumentException` oder ISO-Parserfehler.
- Blockierende Kandidaten: bestätigter Allergen-/Food-/Diet-Konflikt, widersprüchliche Mengen/Einheiten, fehlende Geräte, überschrittenes Zeitlimit, nicht ausreichend bestätigter Gefrierplatz.
- Überprüfungsbedürftige Kandidaten: unbekannte Allergene, Zutatenmengen, Nährwerte, Zeitbelege, MHD, freezer capacity, Kühlkette und vollständige Kosten; `review` darf nicht als sichere Freigabe ausgelegt werden.
- Angeforderte aber fehlende/unbestätigte Snapshots: `*_SNAPSHOT_UNAVAILABLE`; kein impliziter Lagerstand `0`.
- Event-Daten bleiben `rawIngredientsCharged=false`, `preparedPortionsCreated=false`, `consumptionRecorded=false`. Der einzige schreibende Prozess liegt im bestehenden, explizit nutzerbestätigten App-`FoodStore`-Pfad.

## Validierung, Releases und offene Gate

Ein reproduzierbarer Kotlin-/Python-Workflow prüft **169 synthetische Offline-Funktionstests** plus zwei gültige und sechzehn bewusst fehlerhafte JSON-Messages sowie 56 Plan-Rezepte im vollständigen Draft-2020-12-Validator gegen die gepinnte Architekturdatei. [GitHub CI Run 41227672665](https://github.com/ecora2003-max/EG-Rezept-KI/actions/runs/41227672665) erfolgreich. Änderungen am Architekturvertrag erfordern erneute Anpassungs- und Negativtests; das Schema ist als `1.0.0-draft` nicht final freigegeben.

**Noch nicht umgesetzt:** echte Android-Host-/Room-Integration, Hardware-Tests, on-device LLM. Diese Grenzen nicht als bestandene Tests ausgeben. Keine eigene Rezept-, Einkaufs-, Vorrats-, Charge- oder Verzehrdatenbank.

## Optionaler Android-Hostadapter mit explizitem Opt-in (neuer, **nicht auf Gerät geprüfter** Pfad)

Zusätzlich zu den oben genannten Quelldateien bei optionalem lokalen Modellbetrieb `src/main/kotlin/de/eg/recipe/AndroidLocalModelAdapter.kt` in das Android-Source-Set aufnehmen. Der Host implementiert `AndroidLocalModelAdapter.AndroidBridge` mit `checkStatus()` und `generateLocally(request)` **nur**, wenn die tatsächliche SDK-/Geräteprüfung ausschließlich lokale Ausführung belegt. Kein cloudbasierter Bridge-Adapter. Die drei Authorization-Bools sind Host-Aussagen, keine Geräte-Attestation:

```kotlin
val result = AndroidLocalModelAdapter.propose(
    request = request, facts = facts,
    authorization = AndroidLocalModelAdapter.Authorization(
        userOptedIn = userExplicitlyEnabledLocalModel,
        requestDataApproved = userApprovedThisRequestForLocalInference,
        localOnlyExecutionVerified = hostVerifiedLocalOnlyRuntime
    ),
    bridge = localAndroidBridgeOrNull
)
// result.recipes enthält ausschließlich unbestätigte recipe.draft-Vorschläge.
// Kein Kochen, kein Einkauf, keine Bestands- oder Verzehrbuchung.
```

Wenn irgendeine Freigabe fehlt, wird die Bridge **nicht einmal nach Status gefragt**; der deterministische Offline-Generator liefert Vorschläge. Bei `DOWNLOADABLE`/`DOWNLOADING` wird kein Modelldownload ausgelöst. Alle 8 Kontextflag-Kombinationen werden zusätzlich im Gateway gefiltert; eine echte Privacy-/Netzwerkprüfung bleibt Host-Aufgabe. **Nur synthetische JVM-Fake-Bridge getestet**, kein ML Kit/AICore und keine Android-Instrumentation. Nachweis: [CI 41226586359](https://github.com/ecora2003-max/EG-Rezept-KI/actions/runs/41226586359), 16 neue Porttests als Teil von 145 Kotlin-Tests; 74 Schema-Fälle.
