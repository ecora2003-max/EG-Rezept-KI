# Verbindlicher Auftrag – Chat 5 / Rezept-KI und Ernährungsplanung

**Ziel:** eigenständiges, flexibel in Android integrierbares Produkt zur nachvollziehbaren Rezeptgenerierung und Rezeptprüfung. Du entwickelst nicht die komplette App.

## Zugriffsgrenzen
- **Einziges Schreibziel:** `ecora2003-max/EG-Rezept-KI`.
- Ausschließlich **lesen**: `ecora2003-max/EG-Architecture`, andere Fachmodule und privates Hauptrepo `ecora2003-max/Ernaerungsgedaechnis` („Ernährungsplan“). Nie Fremddateien, Fremdbranches, Issues, PRs oder Workflows verändern. Produktives Google Sheet nicht verändern.
- Nur synthetische Testprofile und Vorräte verwenden, keine realen Gesundheitsdaten, persönlichen Bons oder API-Secrets in öffentlichen Repos.
- Lies vor Arbeiten die neueste Architektur (`docs/MASTER_ARCHITECTURE.md`, `docs/DOMAIN_MODEL.md`, `docs/INTEGRATION_AND_GOVERNANCE.md`, `contracts/v1/`) und die drei Dokumente `docs/DEVELOPMENT_MANDATE.md`, `docs/ACCEPTANCE.md`, `docs/INTEGRATION.md`.

## Arbeitsweise
Fachkern und KI-Strategie klar trennen: generatorische Vorschläge können probabilistisch sein; **Einheiten, Nährwertberechnung, Mengen-/Allergenprüfungen und Datenlücken deterministisch validieren**. Die drei Optionen Vorräte, Einkaufsliste und Angebote sind **unabhängig**; ohne gewählte Option keine Nutzung dieser Daten. Kein kostenpflichtiger/extern gehosteter KI-Dienst als Pflicht.

**Jede Änderung testen:** reine Validierung, Rezeptentwurf, optionale Kontexte, Offline-Modus, widersprüchliche Mengen, Allergie- und Sicherheitsfälle sowie Regressionsfälle. Testergebnis mit Commit, Befehl, Datum und offenen Punkten dokumentieren. Nicht nur Prompt-Demos oder scheinbare Tests liefern.

## Abschluss
`READY_FOR_INTEGRATION` nur, wenn ausführbarer Generierungs-/Validierungsweg, Vertrag, Testfixtures und die Pflichtfälle in `docs/ACCEPTANCE.md` belegt sind; andernfalls `PARTIAL_READY`/ `PROTOTYPE` mit genauen Grenzen. `INTEGRATED` nur Codex. Keine realen Käufe, Bestände, Rezeptfreigaben oder Verzehrereignisse auslösen. Bei Vertragsänderung im eigenen Repo einen Antrag formulieren, Architektur-Repo nur lesen.
