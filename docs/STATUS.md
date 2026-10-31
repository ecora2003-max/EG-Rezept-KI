# Entwicklungsstatus – EG-Rezept-KI

**Stand:** 31.10.2026. **Status: PARTIAL_READY**, ausdrücklich **nicht** `READY_FOR_INTEGRATION` oder `INTEGRATED`.

## Source of truth und Fortschritt

- Gemeinsame Architektur gelesen: `EG-Architecture@d22862739bd0e3ef8451ce029e54f7c33ee53e40`, Vertrag `1.0.0-draft`.
- Eigenrepo bei Arbeitsbeginn ohne Implementierung; Kotlin-Generator, Validator, Nährwertrechner und JSON-Ausgabe wurden implementiert. Die **identischen** Kotlin-Quellen und Regressionstests liegen checksummengeschützt in `transfer/bundle.part1.b64` und `bundle.part2.b64`. `scripts/restore_sources.py` rekonstruiert sie als Klartext und prüft SHA-256.
- Lokal ausgeführt: `kotlinc src/main/kotlin/de/eg/recipe/RecipeEngine.kt src/test/kotlin/de/eg/recipe/RecipeEngineTest.kt -include-runtime -d build/tests.jar && java -ea -jar build/tests.jar`, **47 passed, 0 failed**, Kotlin 1.9.0 / JVM 21, nur synthetische Fixtures.
- Zweite Reproduktionsprüfung: Quellcode aus Archiv gelöscht, mit `python scripts/restore_sources.py` rekonstruiert, Dateiprüfsummen unverändert, erneut **47 passed / 0 failed**.
- Demo: `kotlinc ... examples/Demo.kt -include-runtime -d build/demo.jar && java -jar build/demo.jar`: Ausgabe syntaktisch gültiges JSON (`python -m json.tool`).
- GitHub CI `.github/workflows/offline-tests.yml` angelegt; **CI-Ergebnis in diesem Status noch nicht verifiziert**.
- Haupt-App: GitHub-Leseversuch mit `Ernaerungsgedaechtnis` ergab 404 in dieser Verbindung; Architekturdokumente als Integrationskontext benutzt. Keine Fremd-Repositories verändert.

## Nachgewiesene Tests

47 Tests decken alle acht Flagkombinationen samt Nichtbeeinflussung bei deaktiviertem Flag, Snapshots fehlend/unsicher, bestätigte und unbekannte Allergene, unbekannte und widersprüchliche Ernährungseinschränkungen, Food-Verbote, unterschiedliche Roh-/Garmengen, 2/4-Portionen, Null-/Negativmengen, Long-Überlauf, Vit-D/B12-Lücken, veraltete Angebote, Best-before-Prüfung, geplanten Einkauf ≠ Bestand, Budgetlücke, Meal-Prep-Freezerlücke, unbestätigte Targets, externe untrusted Generatoren, Offline-Purity und keinerlei Produktions-/Verzehrbuchung ab.

## Offene Gates (nicht kaschieren)

1. **Vollständige JSON-Schema-2020-12-Validierung** aller echten `recipe.draft`-Beispielnachrichten direkt gegen die aktuell gepinnte Architekturdatei, inklusive negativer Fälle. Die eingebaute Python-Prüfung ist nur eine **strukturelle** Kontrolle.
2. **Verifizierter GitHub-CI-Lauf** sowie Code- und Archivzuordnung auf GitHub (SHA-256).
3. Main-App-Integration durch Codex, Datentransformation aus tatsächlichem lokalen App-Modell und echte Android-Gerätetests.
4. Offline-Template-Auswahl bisher drei Beispielgerichte; kein freies Sprachmodell. On-Device-LLM nur technische Option und mit Laufzeit-Verfügbarkeitsprüfung, nicht als nutzbare Implementierung ausgegeben.
5. Sicherheitsgrenzen: Allergenkreuzkontakte, unbestätigte Nährwertquellen, Kühlkette/Haltbarkeit und echte Rezeptkosten nicht autonom abschließend nachweisbar.

## Nächste konkrete Aktionen

- GitHub-Actions-Ergebnis der CI prüfen; bei fehlender Tooling-/Netzwerkverfügbarkeit CI korrigieren und echte bestandene Runs dokumentieren.
- Vollschema-Validator gegen gepinnten `contracts/v1/contract.schema.json` hinzufügen und positive/negative Demo-Payloads laufen lassen.
- Optionalen AI-Adapter nur bei realer Geräteunterstützung und stets unverändertem Validator. Zusätzliche Templates mit belegter Zutaten-/Zubereitungslogik.
- Nach allen Modulgates Testbericht mit Commit-SHA, Befehlen, Zahlen und Fehlern aktualisieren. Nur nach nachgewiesener Erfüllung `READY_FOR_INTEGRATION`.

Keine personenbezogenen Daten, echten Bons, Secrets oder produktiven Buchungen verwendet.
