# Sichere Android-Lesestrecke für recipe.draft (1.0.0-draft)

## Zweck

Die Haupt-App muss einen Rezeptentwurf **wieder einlesen** können, ohne selbst erklärte KI-Sicherheit oder bereits erfolgtes Kochen zu übernehmen. Der hier implementierte Port arbeitet offline, rein lesend und ohne Android-/JSON-Bibliotheksabhängigkeit:

1. `JsonOutput.encode(contractMap)` erzeugt versioniertes JSON aus einem geprüften Kotlin-Vorschlag.
2. `BoundedDraftJson.decode(json)` ist jetzt ein **optionaler, reiner Kotlin-JSON-Decoder** mit strikten Limits (262.144 Zeichen, 32 Schachtelungsebenen, 10.000 JSON-Werte, 32.768 Zeichen je String). Er erhält ganzzahlige Mengen als `Int`, `Long` oder `BigInteger` und Dezimalzahlen exakt als `BigDecimal`; Dubletten-Keys, unpaarige Unicode-Surrogate, ungültige Escapes, nicht endliche bzw. exzessive Zahlen, unerwartete nachgestellte Daten und fehlende Trennzeichen werden abgewiesen. Das ersetzt **keine vollständige JSON-Schema-2020-12-Prüfung**, die der Host vor Übernahme gegen `contracts/v1/contract.schema.json` durchführen muss.
3. `RecipeDraftImport.read(parsedMap)` ist eine deterministische **zusätzliche semantische Eingabeprüfung**, keine vollständige JSON-Schema-Engine. Erfordert korrekte `contractVersion`, `messageType`, `meta`, `DRAFT`, drei boolesche Kontextflags, passende Einheit/Mengenbasis, Zutaten, Schritte, Quellenstatus und Not-Booked-Invarianten. Fehlende Menge bleibt `null`; `0` wird nicht als fehlend interpretiert.
4. `RecipeDraftRevalidation.revalidate(parsedMap, currentRequest, confirmedFacts, confirmedStates)` führt zusätzlich **die vorhandene deterministische Allergen-/Verbots-/Nährstoff-/Mengenprüfung** des `RecipeEngine` aus. Ohne eindeutig bekannte `foodTypeId` wird abgelehnt. Widersprüchliche Opt-ins oder Portionen werden nicht still korrigiert. Bestätigte Roh-/Gargewichte stammen ausschließlich aus gesondertem Hostbeleg (`confirmedStates`), nicht aus `quantity.notes` oder Modellaussagen.
5. Ergebnis `RecipeEngine.Result.messages` bleibt bei unblocked Imports mindestens `REVIEW_REQUIRED` mit `UNTRUSTED_PROVIDED`; bekannte bestätigte Allergenkonflikte erscheinen in `Result.rejected`. **Auch ein `suppliedVerificationClaim=STRUCTURALLY_VALID` der Eingangsdatei ist nie ein Sicherheitsnachweis.**
6. Erst explizite Nutzerentscheidung und die bestehenden FoodStore-/Kitchen-/Nutrition-Transaktionen der Haupt-App dürfen später einen Eintrag anlegen oder Verbrauch erfassen. Diese Bibliothek besitzt keine Schreibports.

## Android-Beispiel (synthetische Eingabe, kein produktives Profil)

```kotlin
// Alternativ zu einem Host-Parser: begrenzter, exakter Kotlin-Decoder.
val parsed: Map<String, Any?> = BoundedDraftJson.decode(jsonFromHost)
// Zusätzlich vollständige Vertragsprüfung via Host-Schema-Validator.
val imported = RecipeDraftImport.read(parsed)
check(imported.requiresIndependentValidation)

val request: RecipeEngine.Request = currentExplicitRequest
val facts: RecipeEngine.RecipeFacts = currentVerifiedIngredientFacts
val rechecked = RecipeDraftRevalidation.revalidate(
    parsed,
    request,
    facts,
    confirmedStates = emptyMap() // ohne nachgewiesenen Roh-/Gareinheitenstatus
)
// Nur Warnungen/Fehler und neue DRAFT-Daten anzeigen; keine Datenbankänderung.
```

Die App muss die **neu validierte** Entwurfsnachricht verwenden, nicht ungeprüft die ursprüngliche `nutrition`, `sourceStatus`, `confirmed` oder `xRecipeKi.verification`. Der Importer führt nicht automatisch Roh-/Gareinheitenumrechnung, Produktidentitätsklärung, Beschaffung oder Kühlkettenprüfung durch. Angebots-/Vorratssnapshots werden ausschließlich anhand der aktuellen bestätigten Flags im neuen Request verarbeitet.

## Bekannte Grenzen

- Der neue `BoundedDraftJson` ist ein **vollständiger, bewusst begrenzter JSON-Textdecoder**, aber **kein vollständiger JSON-Schema-Validator**. Die oben genannten Limits dürfen vom Host nur innerhalb der festgelegten erlaubten Maxima angepasst werden. Eine gültige JSON-Struktur beweist weder gültige Recipe-Draft-Fachsemantik noch Datenechtheit.
- Die Importstruktur enthält unbekannte optionale Namespaced-Extensions nur als nicht interpretierte Inputdaten. Pflichtfelder und Flags werden typstreng geprüft.
- Für importierte Zutaten ohne explizit hostbestätigten Roh-/Garzustand bleibt `State.UNKNOWN`, dadurch keine irreführend vollständige Nährwertberechnung.
- Unbestätigte oder fremde Mengennachweise werden nicht in bestätigte neue Mengen umgewandelt. Ein `quantity.confirmed` der Quelle ist nur eine *Behauptung*, kein Nachweis.
- Der mitgelieferte Geräte-/Zeitkontext ist bei importierten, fremden Vorschlägen **unbekannt** (`minutes=null`, `equipment=emptySet()`); der Revalidator erzwingt `REVIEW_REQUIRED` und Codex muss zusätzliche Anforderungen im Host separat prüfen.
- Kein echter Room-/Compose-/Android-Geräte-End-to-End-Test; das bleibt Aufgabe Codex.

## Verifikation

`bash scripts/test.sh` kompiliert die Quellen aus `src/main/kotlin/de/eg/recipe/RecipeDraftImport.kt` und `RecipeDraftRevalidation.kt`, führt `RecipeDraftImportTest` (**14 Fälle**) und `RecipeDraftRevalidationTest` (**7 Fälle**) sowie `BoundedDraftJsonTest` (**24 Fälle** inklusive JSON-Text-Roundtrip, 8 Flags, Duplikat-Keys, Surrogat-/Zahlen- und Ressourcenfehler) offline mit synthetischen Fixtures und `java -ea` aus. Vollständige positive/negative JSON-Schema-Prüfung bleibt Teil des GitHub-CI-Workflows. Keine echten Gesundheits-, Einkaufs- oder Vorratsdaten.
