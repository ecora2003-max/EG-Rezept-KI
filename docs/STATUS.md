# EG-Rezept-KI STATUS – 31.10.2026

**PARTIAL_READY / OFFLINE_VERIFIED**, nicht INTEGRATED. Kotlin-Fachkern mit Offline-Rezeptentwürfen, 27 Kombinationsvarianten, bestätigten Ernährungszielen, validiertem Fallback für optionale lokale Modellkandidaten, 1/7/56-Tage-Planung und sicherer No-Side-Effects-Trennung. Kein nachgewiesenes echtes Android-On-Device-LLM.

**Gepinnte Architektur:** EG-Architecture Commit `d22862739bd0e3ef8451ce029e54f7c33ee53e40`, Vertragsversion `1.0.0-draft`.

**Echter verifizierter Test:** Modul-Code-Commit `444bb1bba4037ecb42c26c8d0e22608d340a749a`; [GitHub Actions Run 41222719551](https://github.com/ecora2003-max/EG-Rezept-KI/actions/runs/41222719551) completed/success, JDK17/Kotlin1.9.25, Python3.11; **96/96 JVM-Tests, 0 Fehler, 0 Skip** (47 Engine + 10 Generator + 8 Zielplanung + 13 Fake-Modell + 7 Planhorizonte + 7 Sicherheit + 4 Bedarf). Vollständige Draft-2020-12-Schema-Prüfung: **2 gültige Beispielnachrichten + 16 erwartungsgemäß abgewiesene Negativfälle + 56 gültige Plan-Rezepte**, 0 Fehler/Skip. Alle Fixtures synthetisch.

**Reproduktion:** `python3 -m pip install brotli==1.1.0 && bash scripts/test.sh`; CI zudem `python3 -m pip install jsonschema==4.26.0`, danach `python3 scripts/full_contract_check.py build/recipe-draft.json build/contract.schema.json`, analog `build/smart-draft.json`, und `python3 scripts/plan_contract_check.py build/meal-plan-drafts.json build/contract.schema.json`. Vertrag wird in CI vom obigen Architektur-Commit gelesen. Original-Kernarchiv SHA-256: `a5c53a77dfdf74d0cfc6497db5f415cf2abb0a32e7a3a0363ca2dc9f59581838`; `scripts/restore_sources.py` rekonstruiert zwei Kotlin-Quellen.

**Neu:** `MealPlanDraftPlanner`, `MealPlanDemand`, `Plan.view`, [docs/MEAL_PLAN.md](MEAL_PLAN.md). Eine Plan-ID für Tag/Woche/56 Tage; keine Addition über Projektionsansichten und keine Bestands-/Produktions-/Verzehrbuchung; unbekannte Mengen und Überlauf bleiben null. Nicht unterstützte Gefrierzuweisung wird abgewiesen.

**Offen:** echtes Android-LLM, Hardware-/Privacy-/Room-/Compose-Integration (Haupt-App via GitHub derzeit 404), stabiler Vertragsrelease, reale Allergene/Quellen/Lager-/Kühlkettenbelege. Diese sind nicht als bestanden zu deklarieren. Nächste Schritte: echten Android-Hostadapter und End-to-End-Tests nach Zugang/Plattformbeleg, dann alle Acceptance-Gates erneut prüfen. Nur eigenes GitHub-Repository beschreiben; sämtliche Fremdrepos und Produktivdaten ausschließlich lesen.


## Fortsetzung 31.10.2026 – fehlertolerante Bedarfsprojektion ohne stille Unterzählung

- **Code-/Test-Commit:** `db481b3b3779b1e6d4c36d7e3d40909f8f699c36` (auf Ausgangsstand `8bcec4561d565672aedc5eaa7eb0cedc689718cc`), Dokumentationsfortsetzung `0465179d85342c7ea041ebbb13b53a94c7e0c9bc`.
- **Änderung:** `MealPlanDemand.aggregate` dedupliziert nur vollständig identische Vorkommen; unterschiedliche Daten bei gleicher Occurrence-ID werden als Fehler zurückgewiesen. Fehlende Draft-Struktur, Mengenbasis, unbekannte Einheit und unzulässige Zahltypen werden nicht länger still übersprungen. Fehlender *Mengenwert* bei bekannter Einheit/Basis bleibt `null`; Überlauf ebenfalls `null`. Ein `Int`-Wert wird exakt nach `Long` konvertiert.
- **Lokal tatsächlich ausgeführt:** `kotlinc Stub.kt MealPlanDemand.kt MealPlanDemandTest.kt -include-runtime -d tests.jar && java -ea -cp tests.jar de.eg.recipe.MealPlanDemandTest`, Kotlin 1.9.0, **16/16 bestanden, 0 Fehler, 0 Skip**. Der lokale Stub ersetzt nur für diesen isolierten Unit-Test die Plan-Datentypen; er ist **kein** Nachweis des vollständigen Moduls.
- **Vollständige CI-Prüfung:** [Run 41223911853](https://github.com/ecora2003-max/EG-Rezept-KI/actions/runs/41223911853), Commit `db481b3`; beim Erstellen dieses Nachtrags noch in Ausführung. Das ältere erfolgreiche Ergebnis 96 Kotlin + 74 Schema bleibt der letzte vollständig belegte Gesamtstand, bis die neue CI abgeschlossen ist.
- **Weiterhin offen:** reale Android-/Room-/Compose-Integration und Hardware-/On-Device-LLM-Nachweis, vollständige reale Nährwert-/Allergen-/Kühlkettenbelege, finaler Architekturvertrag. **Status bleibt PARTIAL_READY / OFFLINE_VERIFIED**, nicht INTEGRATED.
- **Nächste Schritte:** neue CI einschließlich 56 Plan-Schema-Beispielen prüfen; bei Fehlern Code korrigieren und erneut testen; danach vollständige Zahlen und Commit im Status nachtragen. Bei Integration den Host Fehler der Bedarfsaggregation anzeigen lassen statt einen leeren Bedarf anzunehmen.
