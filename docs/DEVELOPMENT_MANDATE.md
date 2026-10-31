# Entwicklungsmandat – austauschbare Rezept-KI
**Rolle:** Chat 5, Rezept-KI / eigenständiger Zulieferer

## 1. Produktergebnis
Ein **modularer Rezeptvorschlagsdienst** mit unabhängiger Generator-Schnittstelle und einem **deterministischen Sicherheits-/Nährwert-/Einheiten-Validator**. Er soll Vorschläge für Einzelmahlzeiten, Meal Prep und optional koordinierten Tages-/Wochen-/56-Tage-Kontext erzeugen, aber alle Vorschläge als **Entwürfe** markieren. Fertige, prüfbare Datenpakete werden an die Android-App übergeben; dort entscheidet der Nutzer über Übernahme oder tatsächliches Kochen.

## 2. Bereits vorhandene Haupt-App-Funktionen (nur lesen)
Laut Dokumentation vom 30.10.2026: Room-Schema 7/35 Tabellen, versionierte Artikel und Rezepte, Nährwertberechnung auf Basis von Zutaten, Chargen, Kochen/Portionen, geplante Zubereitung und späterer Verzehr mit ausdrücklicher Ist-Zuordnung, Berechnung von Restbedarf und Kosten. **Kein** paralleles Bestands- oder Nährwertbuchungssystem bauen. Codex adaptiert auf bestehende `Nutrition`, `Planning`, `FoodStore`, `Kitchen` etc.; Entwicklung erfolgt nur im eigenen Repo.

## 3. Drei unabhängige Nutzeroptionen
- `considerStock` („Vorräte berücksichtigen“): liest nur ausdrücklich bereitgestellten, datierten und als freigegeben markierten Vorratssnapshot; keine Lageränderung.
- `considerShopping` („Einkaufsliste berücksichtigen“): berücksichtigt offenen Bedarf und geplante Einkäufe, aber **geplant ≠ gekauft**. Keine implizite Bestellung.
- `considerOffers` („Angebote berücksichtigen“): verwendet nur für geplanten Einkaufstag gültige, nachvollziehbar bepreiste Angebote, ggf. Preisunsicherheit; keine automatische Günstigkeitsbehauptung.

Optionen sind unabhängig: **alle 8 booleschen Kombinationen** müssen getestet werden. Wenn ein Flag `false` ist, darf dessen Kontext die Ausgabe **nicht beeinflussen** (mit Fixture-Test). Wenn ein Flag `true` ist, die zugehörigen Daten aber fehlen, explizit als nicht verfügbar kennzeichnen, nicht `0` annehmen.

## 4. Fachliche Constraints
- Unverträglichkeiten, Allergien, bestätigte Ernährungsweisen und Lebensmittelverbote strikt beachten – bei unbekanntem Status keine Sicherheit behaupten. Bei Rezepten mit Allergeninformationen aus unsicheren Quellen Prüfhinweis.
- Nährstoffzielbereiche nur nutzen, wenn als **bestätigt/aktiv** übergeben. Im produktiven Sheet waren 150–170g Protein und 3000 kcal **unbestätigte Vorschläge**, keine automatisch verbindlichen Ziele.
- Einheiten in g/ml/Portion, Rohgewicht, gegartes Gewicht, Gesamtfüllmenge, Abtropfgewicht, Stück nur mit belegter Umrechnung. Mengen dürfen fehlen, dann Rezept als unvollständig markieren. Nährwerte nur aus ausreichenden Zutatendaten berechnen; fehlende Mikro-/Makrodaten → betroffene Summe unbekannt, nie als Null.
- Portionen, Zubereitungsdauer, Reihenfolge, verfügbare Geräte (wenn bekannt), Zutatenstatus, Budgetrahmen, Lagerorte, tatsächliche Kühl-/Gefrierkapazität und Lebensmittelsicherheit getrennt prüfen. Kein MHD als automatisches Verfallsverbot; kein Haltbarkeitsversprechen ohne Quelle/Bestätigung.
- Meal Prep: geplantes Kochen verbraucht noch nichts; Zutaten beim **tatsächlichen** Herstellen abbuchen, fertige Portionen erzeugen, Verzehr später. Rezeptzutaten nicht mehrfach als Bedarf erzeugen, wenn mehrere Portionen geplant sind.
- Beim Plan Tag/7 Tage/56 Tage dieselbe Planidentität verwenden; Wochen-/Langfristziele dürfen nicht zusätzlich Mengen erzeugen.
- Keine erfundenen Vorräte, persönlichen Vorlieben, Trainingstage, Preise oder Kalorienverbräuche.

## 5. Technische Architektur der Zulieferlösung
Drei getrennte Ports:
1. `RecipeCandidateGenerator`: austauschbare KI-/Regel-/Template-Strategie. Eine kostenlose, offline testbare deterministische Referenzvariante **muss** existieren. Optionale On-Device-LLM-Nutzung nur als Zusatz mit nachgewiesenen Plattform-/Modellanforderungen und Fallback; keine pauschale Zusage, dass ein konkretes Android-Gerät Gemini Nano hat. Keine heimliche Datenübertragung.
2. `RecipeValidator`: pure, streng testbare Prüfung von Zutaten, Portionen, Einheiten, Status, Allergenen, Sicherheitshinweisen und Plausibilität. Mutmaßlich fehlende Zutaten als fehlend kennzeichnen; keine erfundene Korrektur.
3. `RecipeNutritionEstimator`: deterministischer Rechenweg aus *quellenbelegten* pro-100g/pro-100ml-Nährwerten plus Rohmengen/Portionsausbeute; Unsicherheiten und nicht bezifferbare Werte separat. Kein doppeltes Kalorienzählen bei gekochten Restportionen.

Der Generator darf intern Kotlin/andere portierbare Sprache nutzen; liefere exact Android-Integration/Portierungsaufwand. Kein dauerhafter Server oder kostenpflichtiger KI-Provider als Pflicht. Fehlende Modellverfügbarkeit → klarer Offline-Fallback.

## 6. Benötigte Inputs
`RecipeRequest`: Mahlzeit-/Zeit-/Portionswunsch, bestätigte Einschränkungen, optionale Nährstoffzielbereiche, verfügbare Geräte und Zeiten, Benutzervorlieben (nur explizit übergeben), die drei Flags, optional `StockSnapshot`, `ShoppingSnapshot`, `OfferSnapshot`, samt Datum, Quelle und Datenqualität. Kontext **niemals** selbst aus privatem Repository oder Google Sheet abrufen.

## 7. Outputs
`recipe.draft` laut `EG-Architecture/contracts/v1`: Entwurfs-ID, Name, Portionen, Zutaten mit Mengenbasis, Schritte, Flags, Nährwerte nur wenn hinreichend sicher, Warnungen und Quellen/Erklärungen. Weitere optionale Vorschläge mit Ranking, substitutionsrelevanten Merkmalen, offenen Einkaufsmengen, vorbereitenden Meal-Prep-Schritten und nachgewiesener Quellenabdeckung. Kein `confirmed`-Status ohne Nutzer-/App-Prüfung.

## 8. Sicherheits- und Testschwerpunkte
- Keine allergene/verbotene Zutat trotz bestätigter Einschränkung als „sicher“ anbieten.
- Unbekannte Nährwerte und Zutatenmengen dürfen keine vollständige Makro-/Mikrobilanz erzeugen.
- Drei Flags unabhängig; ausgeschalteter Kontext hat keinen Einfluss.
- Kein generiertes Rezept verändert echte Vorräte oder geplante Einkäufe automatisch.
- Unit-/Rundungs-/Portionsberechnung reproduzierbar.
- Offline/kein LLM verfügbar: hilfreicher Fallback oder ehrliche Nichtverfügbarkeit; nie stille API-Kosten.
- Optionaler KI-Einsatz: Ausgaben immer durch denselben deterministischen Validator, keine „Prompt ist korrekt“-Garantie.

## 9. Abgabe
Ausführbarer Generator+Validator+Rechner, Mock-Snapshots und Synthetikrezepte, maschinenlesbare Vertragsbeispiele, reproduzierbare automatische Tests, klarer Android-Adapterpfad, Liste der fehlenden oder unbestätigten Sicherheitsprüfungen, `docs/STATUS.md`, Testbericht und README. Freigabe nur nach `docs/ACCEPTANCE.md`.
