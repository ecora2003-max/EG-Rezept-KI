# Reproduzierbarer Testbericht – 31.10.2026

## Code und Umgebung

- Arbeitsvertrag: `EG-Architecture@d22862739bd0e3ef8451ce029e54f7c33ee53e40`, `1.0.0-draft`.
- Module-Sourcen sind verlustfrei in `transfer/bundle.part1.b64` und `bundle.part2.b64` archiviert, Archiv SHA-256 `a5c53a77dfdf74d0cfc6497db5f415cf2abb0a32e7a3a0363ca2dc9f59581838`; Entpackung via `scripts/restore_sources.py`.
- Lokale Umgebung: Kotlin CLI 1.9.0, OpenJDK 21; kein Netzwerk und keine Android-Hardware.
- GitHub Actions: OpenJDK 17, Kotlin CLI 1.9.25, Python 3.11; echter erfolgreicher Workflow-Run [41219880851](https://github.com/ecora2003-max/EG-Rezept-KI/actions/runs/41219880851) auf Commit `2c06f2be6bd24c653bf173c7be32747ca5e21864`.
- Typ der Daten: ausschließlich synthetische Fixtures, keine echten Nutzer-, Einkaufs- oder Gesundheitsdaten.

## Befehle und Messung

```bash
python3 -m pip install brotli==1.1.0
bash scripts/test.sh
```

Der Test-Runner stellt den source code aus dem SHA-256-Archiv wieder her, kompiliert Kotlin-Kern und Testtreiber, führt `java -ea -jar build/tests.jar` aus, führt ein Standalone-Demo aus und validiert den Basisvertrag. Sowohl lokal als auch in Run 41219880851: **47/47 positiv, 0 fehlgeschlagen, 0 übersprungen.** Demo JSON strukturell geprüft. Der GitHub-Run enthält alle Einzelzeilen und `SUMMARY passed=47 failed=0` als Beleg.

## Prüfmengen

- **Flagmatrix:** alle acht Kombinationen `considerStock/Shopping/Offers`; bei `false` dürfen stark geänderte Snapshots das Ergebnis nicht beeinflussen.
- **Allergene/Ernährung:** bestätigter Allergen- und Diet-Konflikt blockiert; unbekannt = Review; nicht bestätigtes Verbot gilt nicht als bestätigt; bestätigtes `FOOD:<foodTypeId>` blockiert; unbekanntes Restriktionsformat blockiert.
- **Mengen/Nährwerte:** 2/4-Portionen, trocken/gegart nicht vertauschbar, Null/Negativ, falsche Mengenbasis, fehlende Mengen, nicht belegte/negative Nährstoffe, selektiv fehlendes Vitamin D/B12, partial vs vollständige Summen, Long-Überlauf.
- **Planung:** unbekannte Vorräte, nicht freigegebene Snapshots, Einkaufslistenposition ≠ verfügbarer Bestand, zeitgebundene Angebote, MHD nur Warnung, fehlendes Budget, unbekannter/zu kleiner Gefrierplatz, tatsächliche Herstellung und Verzehr **nicht** gebucht.
- **Robustheit:** falsches Gerät, Zeitvorgaben, unbestätigte vs bestätigte Ziele, untrusted-Kandidat weiterhin validiert, JSON-Escaping, deterministische Wiederholung und Fehlerpfade.

## Getrennte Schema-Prüfung

- `scripts/contract_check.py`: **nur** dependency-freie Struktur-/Invariantenprüfung, keine vollständige Schema-Konformitätsaussage.
- `scripts/full_contract_check.py`: echte `jsonschema.Draft202012Validator` mit `FormatChecker` gegen gepinnte Architekturdatei und acht mutierte negative Nachrichten. Per CI eingeführt auf Commit `70071637b36011b9bfad53db850d0f72c087900d`; **Lauf zu dieser Dokumentation noch nicht abgeschlossen/verifiziert**.
- Keine Android- oder App-Gesamtintegration; Quellen-/Medizingarantien nicht beansprucht.

## Annahmegrenzen

Die 47 Tests belegen den Offline-Fachkern, **nicht** die Sicherheit einer individuellen Mahlzeit ohne verlässliche Zutatenfakten. Eine erfolgreiche JSON-Schema-Prüfung ersetzt keine End-to-End-Tests mit Room und realen App-Adaptern. On-device-LLM ist noch nicht implementiert; ohne Modell garantiert der Offline-Kern dennoch eine reproduzierbare Ausgabe.
