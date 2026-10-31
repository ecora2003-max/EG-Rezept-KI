# EG-Rezept-KI — offlinefähiger Kotlin-Rezeptentwurfs-Kern

**Stand: PARTIAL_READY, nicht in der Haupt-App integriert.** Arbeitsrepo für Chat 5. Ausschließlich [EG-Architecture](https://github.com/ecora2003-max/EG-Architecture) definiert die gemeinsamen Fachverträge; hier wird **nur** die Zulieferkomponente entwickelt.

## Funktion

- **Reiner Kotlin/JVM-Fachkern** ohne Android-SDK, Server, API-Schlüssel, Netzwerkzugriffe, Datenbank oder persistente Seiteneffekte.
- Austauschbare `RecipeCandidateGenerator`-Schnittstelle: deterministische Offline-Templates oder untrusted externe/modelbasierte Kandidaten.
- Deterministischer `validate`-Port: Portionen, Zutatenmengen, Einheiten, Roh-/Gargewicht, Allergene, ausdrücklich bestätigte Ernährungsbeschränkungen, Zeit, Geräte, Meal-Prep- und Sicherheitslücken. Blockierende Konflikte werden verworfen; unbewiesene Sicherheit erhält `REVIEW_REQUIRED`.
- `estimateNutrition`: pro Zutat bekannte quellenbelegte Werte je 100 g/ml, `BigDecimal`-Summen und Portionierung; unbekannte Nährstoffe bleiben `null`, bekannte Teilsummen getrennt.
- Drei *unabhängige* Flags `considerStock`, `considerShopping`, `considerOffers`; nur freigegebene datierte Snapshots, niemals Room/Google-Sheets-Zugriff. Einkauf geplant ≠ Vorrat.
- `recipe.draft` gemäß `1.0.0-draft` mit `xRecipeKi`-Extension; keine Buchung von Kochen, Einkaufen oder Verzehr.

## Lokale Reproduktion

Das Modul wird aus technischen Übertragungsgründen im Repository als **SHA-256-geprüftes, quelloffenes Kotlin-Archiv** in `transfer/bundle.part*.b64` transportiert. `scripts/restore_sources.py` stellt die zwei lesbaren Dateien unter `src/main/kotlin/...` bzw. `src/test/kotlin/...` wieder her. Die Archivierung enthält kein Binärprogramm und keinen geheimen Quellcode.

Voraussetzung: Java/JDK 17+, `kotlinc` 1.9+, Python 3 und einmalig das kostenlose Python-Paket `brotli` (nur für das Entpacken).

```bash
python3 -m pip install brotli==1.1.0
bash scripts/test.sh
```

Das Skript entpackt prüfsummengeschützt, kompiliert Generator+Tests, startet **47 Offline-Regressionstests** mit aktivierten JVM-`assert`s, führt ein Demo-Rezept aus, serialisiert JSON und überprüft dessen Pflichtstruktur. Bei jedem Fehler bricht es mit Nichtnullstatus ab. Keine Live-API oder personenbezogene Daten erforderlich.

Manuell:

```bash
python3 scripts/restore_sources.py
kotlinc src/main/kotlin/de/eg/recipe/RecipeEngine.kt examples/Demo.kt -include-runtime -d build/demo.jar
java -jar build/demo.jar
```

## Reifegrad und Grenzen

Automatisierte CI: `.github/workflows/offline-tests.yml`. **Volle JSON-Schema-Draft-2020-12-Validierung gegen die Architekturdatei und echte Android-App-/Gerätetests sind gesonderte Gates**, nicht durch die Basisstrukturprüfung ersetzt. Ohne bestätigte Produkt-/Allergendaten ist eine Verträglichkeitszusage unzulässig. Offline-Templates sind begrenzt (aktuell drei), keine freie generative KI; ein On-Device-Modell ist optional und derzeit **nicht implementiert**.

Vollständige Schnittstelle: [Integration](docs/INTEGRATION.md). Gemessener Fortschritt: [Status](docs/STATUS.md). Testfälle: [Testbericht](docs/TEST_REPORT.md). Optionale KI: [Geräte- und Datenschutzprüfung](docs/ON_DEVICE_AI.md).
