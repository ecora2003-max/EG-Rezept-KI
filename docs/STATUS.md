# Modulstatus – EG-Rezept-KI

**Datum:** 31.10.2026, Berlin. **Status: PARTIAL_READY** (nachgewiesen ausführbarer, integrierbarer Offline-Kern; *noch keine* überprüfte freie LLM-/On-Device-KI-Generierung). `INTEGRATED` kann nur Codex nach echtem Android-App-Test setzen. Die fertig getestete Offline-Komponente ist ein **eigenständig nutzbarer Kandidaten-Generator**; es wird keine generative KI-Hardwareverfügbarkeit behauptet.

## Neuester nachgewiesener Zustand

- Einzige Schreibablage `ecora2003-max/EG-Rezept-KI`. Keine fremden Repository-/Produktivdatenänderungen.
- Kanonische Architektur `EG-Architecture@d22862739bd0e3ef8451ce029e54f7c33ee53e40`; Vertrag `1.0.0-draft`, weiterhin Vertragsentwurf.
- **CI-Beleg:** [GitHub Actions Run 41221248578](https://github.com/ecora2003-max/EG-Rezept-KI/actions/runs/41221248578), Commit `0b429348d409e0fce41c8728b74c1c246fa806e1`, **completed/success**, Kotlin 1.9.25/JDK17, Python 3.11. **78/78 echte Offline-JVM-Funktionstests**, 0 Fehler, 0 Skip; **2/2 JSON-Schema-2020-12-Positivfälle und 16/16 bewusst defekte Fälle** bestanden.
- Befehle: `python3 -m pip install brotli==1.1.0 && bash scripts/test.sh`; CI zusätzlich `python3 -m pip install jsonschema==4.26.0` und `python3 scripts/full_contract_check.py build/recipe-draft.json build/contract.schema.json` sowie dasselbe für `build/smart-draft.json`.
- Original-Kotlin-Kern und 47 Tests liegen unverändert SHA-256-validiert als Quellarchiv `transfer/bundle.part1.b64`/`bundle.part2.b64` (Prüfsumme `a5c53a77dfdf74d0cfc6497db5f415cf2abb0a32e7a3a0363ca2dc9f59581838`). `scripts/restore_sources.py` extrahiert genau zwei Klartext-Kotlin-Dateien; CI prüft diese tatsächlich.
- Weitere Kotlin-Quellen direkt im Repo: `CombinatorialGenerator.kt` (**27 verschiedene synthetische Gerichte**), `GoalAwareRecipePlanner.kt` (standardmäßig 14 Hauptgerichte; Zielsortierung nur nach bestätigten Nährstoffbereichen) sowie 31 ergänzende Tests und 2 Demos. `OnDeviceRecipeGateway.kt` kapselt einen optionalen lokalen Modellport mit Geräte-/Statusprüfung durch den Host, 13 Fake-Modelltests, Privacy-Filter für Snapshots und deterministischem Fallback; **keine ML-Kit-Implementierung oder Hardwarefunktion behauptet**.
- Main-App `ecora2003-max/Ernaerungsgedaechtnis`: Lesen via GitHub-Verbindung gab 404; Integration nur als klare Adapteranforderung nach gelesener Architektur dokumentiert, kein ungerechtfertigter App-Test behauptet.

## Bestehende Funktionsabnahme (offline verifiziert)

1. Rezeptentwürfe mit Rohzutatenmengen, Mengenbasis, Zubereitungsschritten, Geräten, Portionen, Mahlzeiten und Meal-Prep-Metadaten.
2. Austauschbare Generatoren und separater deterministischer Validator/Nährwertrechner, nur dokumentierte Fakten (fehlend ≠ 0), begrenzte Nutzung von `BigDecimal`.
3. Drei unabhängige Flags in allen acht Kombinationen, ausgeschaltete Kontexte ohne Einfluss; angeforderte unbekannte Snapshots explizit gemeldet.
4. Bestätigte/unklare Allergene und Ernährungsverbote, Roh-/Garkonflikte, falsche Einheiten/Mengen, bekannt/fehlendes Vitamin D/B12, Zeit/Geräte, begrenzte Gefrierkapazität, MHD und Angebotsdatum.
5. Modellgateway: verfügbare und nicht verfügbare Modellzustände, Fehlerfallback, maximale 32 Modellkandidaten, Validierung untrusted-Ergebnisse und kein Zugriff auf deaktivierte/ungültige Snapshots; 13 Tests rein synthetisch.
6. Einkaufsliste ≠ gekauft; Vorschlag ≠ Kochen ≠ Verzehr. Keine Schreiboperation für App-/Room-/Sheet-Bestände.
7. Vollständige, gepinnte Schema-2020-12-Validierung für beide Beispielrezepte mit negativer Mutationstestmatrix. Details [TEST_REPORT](TEST_REPORT.md).

## Noch offen / Gate gegen unberechtigte Vollfreigabe

- Der lokale Modellport ist als getestete `OnDeviceRecipeGateway.LocalModel`-Schnittstelle implementiert. **Tatsächliche LLM-basierte KI-Generierung** auf einem konkret unterstützten Android-Gerät samt Runtime-Status, Datenschutzfreigabe, syntaktischem Input-Adapter und Revalidierung; nicht als kostenlos verfügbar auf allen Geräten voraussetzen. Ein rein kombinatorischer Generator ist **kein LLM**.
- Die Haupt-App konnte in dieser Sitzung nicht über die GitHub-Verbindung eingesehen werden (404), daher kein echtes Room-/Compose-/Geräte-End-to-End und keine Migration verifiziert. Das obliegt Codex.
- Der vom Architekturteam freizugebende stabile Vertrag `1.0.0` existiert noch nicht; bisherige Nachrichten entsprechen ausschließlich der Draft-Version.
- Individuelle Lager-/Kühlkettensicherheit, allergene Kreuzkontamination, Haltbarkeit, konkrete Rezeptkosten und reale Nährwert-Quellenrechte können ohne zusätzliche Belege nicht abschließend garantiert werden.
- Die erste Quellcode-Datei benötigt aktuell einmalige SHA-geprüfte Entpackung. Für reguläres Android-Publishing kann die entpackte `.kt`-Datei durch Codex in das Bibliotheksmodul aufgenommen werden.

## Nächste Sitzung / Wiederaufnahme

1. Neueste `docs/STATUS.md`, `AGENTS.md`, `docs/ACCEPTANCE.md` und aktuelle Architektur-SHA lesen. Vorherigen Erfolg nicht als Erfolg einer Änderung an neuen Daten/Verträgen ausgeben.
2. `OnDeviceRecipeGateway.LocalModel` ist implementiert und mit 13 Fake-Fällen getestet. Ein **konkreter ML-Kit/AICore-Hostadapter** mit Geräte-/Status- und Datenschutzprüfung ist die nächste optionale Erweiterung, nur mit nachgewiesener Android-Unterstützung. Alternativ weitere Offline-Rezeptgrammatiken mit negativen Sicherheits- und Mengenfällen ergänzen.
3. Test nach jeder Codeänderung: `bash scripts/test.sh`, CI-Schema-Prüfungen; echten Run/Commit und Fehler/Skip hier dokumentieren.
4. Erst bei nachgewiesenen **allen** noch zutreffenden Abnahmekriterien den Status auf `READY_FOR_INTEGRATION` setzen; weder ohne Vertrags-/Gerätebelege `INTEGRATED` behaupten noch fremde Repositories ändern.

**Benachrichtigung nur bei einer notwendigen Entscheidung, unlösbaren Blockade oder vollständiger Abnahme.** Keine Zwischenmeldungen.
