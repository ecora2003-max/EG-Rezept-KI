# Offline-Fachkern und Vertragsprüfung: gemessener Testbericht

**Datum:** 31.10.2026. **Vertrag:** `EG-Architecture@d22862739bd0e3ef8451ce029e54f7c33ee53e40` (`1.0.0-draft`). **Verifizierter GitHub-Codecommit:** `c66d7be437a47af052cd3af878ff06fc3ca0b250`.

## Reproduzierbarkeit

GitHub Actions [Run 41220695779](https://github.com/ecora2003-max/EG-Rezept-KI/actions/runs/41220695779) ist **completed/success**, einschließlich aller Ausführungsschritte und kompletter JSON-Schema-2020-12-Validierung. Runner: Ubuntu, Temurin JDK 17, Kotlin 1.9.25, Python 3.11, freie `brotli==1.1.0` und `jsonschema==4.26.0`, ausschließlich synthetische Daten.

```bash
python3 -m pip install brotli==1.1.0
bash scripts/test.sh
# GitHub CI zusätzlich:
python3 -m pip install jsonschema==4.26.0
# Pinned: EG-Architecture commit d228627...
python3 scripts/full_contract_check.py build/recipe-draft.json build/contract.schema.json
python3 scripts/full_contract_check.py build/smart-draft.json build/contract.schema.json
```

Der Runner prüft zunächst per SHA-256 das Archiv `transfer/bundle.part1.b64` + `bundle.part2.b64` (SHA-256 `a5c53a77dfdf74d0cfc6497db5f415cf2abb0a32e7a3a0363ca2dc9f59581838`), entpackt exakt zwei Kotlin-Quellpfade und kompiliert die Kotlin-Klassen. Die erzeugten Plaintext-Dateien sind keine binäre Laufzeitkomponente.

## Tatsächlich ausgeführte Resultate

| Prüfung | Bestanden | Fehlgeschlagen | Übersprungen |
| --- | ---: | ---: | ---: |
| `RecipeEngineTest` – Validierung, Nährwertberechnung, Kontextflags, Meal Prep, No-Side-Effect | 47 | 0 | 0 |
| `CombinatorialGeneratorTest` – 27 Gerichte, Mengen und Unabhängigkeit | 10 | 0 | 0 |
| `GoalAwareRecipePlannerTest` – bestätigte Ziele, Rangfolge, fehlende Daten | 8 | 0 | 0 |
| JSON-Schema Draft 2020-12 – zwei positive Nachrichten | 2 | 0 | 0 |
| JSON-Schema Draft 2020-12 – je acht absichtlich ungültige Nachrichten abgewiesen | 16 | 0 | 0 |

**65/65 Kotlin-Testfälle und 18/18 vollständige Schema-Prüffälle bestanden**. Zusätzlich zwei einfache Vertragsstrukturprüfungen `scripts/contract_check.py`. CI-Log: `SUMMARY passed=47 failed=0`, `SUMMARY combinatorial_generator passed=10 failed=0`, `SUMMARY goal_aware_planner passed=8 failed=0`, zweimal `PASS full draft 2020-12 recipe.draft message` und zweimal `PASS 8 malformed messages rejected`. Diese Resultate belegen **keine** getestete Android-App.

## Grenz- und Invariantenfälle

- Die drei Flags laufen in allen **acht** Kombinationen, nicht gewählter Snapshot darf keinerlei Einfluss haben; ausgewählt aber fehlend → `null` und Warnung statt Nullbestand.
- Bestätigte Allergie, eingeschränkte Ernährung und verbotener Lebensmitteltyp werden geprüft und gegebenenfalls blockiert. Unbekannter Allergenstatus erzeugt zwingend Review. Externe/untrusted Kandidaten erhalten keine automatische Verträglichkeitsfreigabe.
- Unterschiedliche Mengenbasis und Roh-/Gargewicht, ungültige oder widersprüchliche Mengen, zwei/vier Portionen, Überlauf, fehlende Gramm-/Milliliterdaten.
- Fehlendes Vitamin D/B12 lässt genau diese Summen unbekannt, während vorhandene Makrowerte erhalten bleiben; `BigDecimal` statt unkontrollierter Float-Arithmetik.
- Keine unbestätigten Nährstoffziele; bestätigte Ziele nur dann `IN_RANGE`, wenn zugehörige pro-Portion-Mengen vollständig belegt sind.
- Einkaufsbedarf ist kein bestätigter Kauf/Bestand; Angebote nur mit gültigem Zeitfenster und gültiger Herkunft; MHD ist keine automatische Verderbsbehauptung.
- Meal-Prep-Einfriermengen, unbekannte/zu kleine Gefrierkapazität, keine fiktive Kühlkettenfreigabe, Rohzutaten nicht doppelt auf Portionen gebucht.
- Aus `propose` folgen **keine** realen Einkäufe, Vorratsbewegungen, Herstellungen, Rezepteinträge oder Verzehrereignisse.

## Grenzen

Die Engine verwendet keine persönlichen Ernährungsdaten. Quellen- und Lizenzfragen für reale Produkte/Nährwerte sind Sache der lesenden Host-Adapter. **Es wurde kein auf realer Android-Hardware laufendes LLM, keine tatsächliche Room-Anbindung, keine automatische Allergensicherheitsgarantie, kein vollständiges Produktpreisverzeichnis und kein KI-Freitextgenerator nachgewiesen.** Die freie deterministische Offline-Generierung mit 27 variablen Kombinationen und nachvollziehbarer Prüfung ist nutzbar; echte KI-Sprachgenerierung bleibt optionales/offenes Integrationsziel.
