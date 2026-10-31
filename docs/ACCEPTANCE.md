# Verbindliche Tests und Definition of Done – Rezept-KI

**Immer testen** nach Generierungs-/Prompt-/Validator-/Nährwert-/Flag-/Einheitenänderungen. Pro Meilenstein `docs/STATUS.md`: Datum, Commit-SHA, exakter Befehl, Testzahlen, reale vs. synthetische Testfälle, bekannte Grenzen.

## Mindest-Testmatrix
| Szenario | Erwartete Wirkung |
| --- | --- |
| 3 unabhängige Flags | **alle 8 Kombinationen**; bei false kein Datenleck/Einfluss des jeweiligen Snapshots |
| Flag true, aber kein Snapshot | keine Annahme eines leeren Vorrats/Preisbestands; Warnung über fehlenden Kontext |
| bestätigte Allergie gegen Rezeptzutat | Vorschlag nicht als sicher freigeben; Validator erkennt Konflikt |
| Allergiestatus unbekannt | keine stillen Freigaben/„allergenfrei“-Aussage |
| Rezept mit 2 statt 4 Portionen | Zutaten/Nährwerte konsistent skaliert; keine Rohwaren-Doppelzählung |
| 200 g trockener Reis vs. 200 g gekochter Reis | keine gleichartige Rohmengen-/Nährwertbehandlung ohne Konversion |
| fehlende Zutatenmenge | Nährwert-/Einkaufsbedarf unvollständig statt erfundenem Wert |
| fehlendes Vitamin D / B12 | nur betreffende Nährstoffsumme unbekannt, bekannte Teilwerte erhalten |
| bestätigtes Budget fehlt | kein erfülltes Budget versprechen |
| Meal Prep braucht unbekannten Gefrierplatz | keine automatische sichere Lagerfreigabe |
| MHD vor geplantem Kochdatum | Prüfung nötig, nicht pauschal Verderb |
| Einkaufsliste enthält Artikel, aber kein Kauf | nicht als vorhandener Bestand rechnen |
| kommende Angebote | Preis nur für tatsächlich gültigen geplanten Kaufzeitraum |
| App offline / KI-Modell nicht verfügbar | deterministischer Fallback oder klarer Fehler ohne API-Pflicht |
| Rezeptvorschlag | **keine** tatsächliche Produktions-/Lager-/Verzehrbuchung |
| Vertrag `recipe.draft` | gültige JSON-Beispiele; fehlende Daten korrekt repräsentiert |
| deterministische Nährwertberechnung | exakte Mengenbasis/Portionen mit integer/decimal, keine unkontrollierte Float-Scheingenauigkeit |

## Ziel erreicht `READY_FOR_INTEGRATION`
- Mindestens ein **ausführbarer, ohne kostenpflichtige API nutzbarer** Kandidatengenerator (auch regelbasiert), ein **deterministischer Validator** und dokumentierte Berechnung der verfügbaren Nährwerte.
- Die 8 Flagkombinationen und alle genannten Grenz-/Sicherheitsfälle sind mit reproduzierbaren automatisierten Tests bestanden.
- Das Rezept kann aus synthetischen Inputs korrekt erzeugt, geprüft, serialisiert und mit konkretem Android-Adapterpfad wieder eingelesen werden.
- Fehlende Daten/Allergene/Sicherheitskontexte blockieren unzulässige Gewissheiten; reale Bestandserfassung und reale Verzehrbuchung bleiben unberührt.
- Vorhandene KI-Optionen nach Plattform/Verfügbarkeit und Datenschutz ehrlich dokumentiert; eine nicht nachgewiesene On-Device-Funktion ist kein erledigtes Feature.
- Vertrag, Testlogs und noch offene Punkte vollständig, Fehler nicht unterdrückt.
- Fehlt noch die eigentliche KI-Generierung, aber Validator/Fallback sind fertig, Status **`PARTIAL_READY`** und konkrete funktionale Grenze nennen. **`INTEGRATED` nur Codex.**

## Nicht bestanden
Nur ein Prompt-Text ohne ausführbaren Code; behauptete Nährstoffgenauigkeit aus fehlenden Produktdaten; Rezeptvorschlag als bereits gekocht; Kontextsicherung nur über Textanweisung ohne deterministische Tests; zwingender privater Cloud-Schlüssel oder laufende Bezahlkosten.
