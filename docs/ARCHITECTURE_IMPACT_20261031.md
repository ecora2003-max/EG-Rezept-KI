# Vertragsfolgen der Architekturaktualisierung vom 31.10.2026

**Nur lesend geprüft:** `EG-Architecture@f1984378d81f4d49ca93e5da195ecbc4d7260f80`, `docs/STATUS.md`, `contracts/v1`, `contracts/v1.1`, `docs/INTEGRATION_AND_GOVERNANCE.md` und `docs/CODEX_INTEGRATION_PLAN.md`.

## Rezept-KI bleibt vorerst bei `1.0.0-draft`

- `contracts/v1/contract.schema.json` ist weiterhin der Blob `bedde4677342817fe6b883962c8dfbbd60100072`, unverändert gegenüber dem bisherigen gepinnten Stand. Rezeptnachrichten sind weiterhin `recipe.draft`, `status=DRAFT`, ohne Kauf-, Koch-, Verzehr- oder Lagerbuchungssemantik.
- `contracts/v1.1/contract.schema.json` ist ein **noch nicht freigegebener** separater Einkaufsvertragsentwurf (Blob `dca777d219307a90bf3d4dff6189fbf12476c508`); seine neuen Typen `shopping.need.projection`, `shopping.purchase.suggestion`, `shopping.offer.interest` und `shopping.past.comparison` sind **keine** direkt akzeptierten `recipe.draft`-Eingaben. Der Rezeptmodul-Core verwendet keinen v1.1-Serializer und kein stilles Upgrade.
- Die v1.1-Nachschärfungen `unknownNeeds[]`, `packageLines[]` und `savingsBasis=MERCHANDISE_ONLY` betreffen nur Einkaufsprozesse. Der Rezeptdienst darf aus einer v1.1-Bedarfsprojektion keine gekauften Vorräte, aus `FEASIBLE_NOT_PURCHASED` keinen Einkauf und aus einem historischen Preis keine aktuellen Angebote ableiten.
- Eine spätere **Codex-Hostadaption** darf einen ausdrücklich freigegebenen, datierten Einkaufsprojektion-Snapshot in `RecipeEngine.Request.shopping` übersetzen. Dazu müssen die drei Nutzerflags unabhängig sein, Mengenbasis/Quellrecht/Abdeckung belegt sein und unklare Mengen unbekannt bleiben. Es gibt aktuell **keinen** verifizierten v1.1-zu-Rezept-Snapshotadapter in diesem Repo; kein stilles Einlesen des neuen Typs.
- Der bestehende v1-Rezept-Core ist durch die v1.1-Änderung nicht fachlich inkompatibel geworden, weil `recipe.draft` in v1 strukturell unverändert ist. Das ist **kein** Beweis einer bereits erfolgreich integrierten v1.1-App. Die Architekturversion bleibt `draft`.

## CI / offener Prüfauftrag

Bestehender GitHub-CI-Workflow validiert **v1** mit Draft-2020-12 gegen die gepinnte, SHA-feste v1-Vertragskopie. Bei expliziter Freigabe eines v1.1-Rezept-Adapters sind zusätzliche Schema-/Semantik-/Quellenrechts- und Snapshot-Testfälle erforderlich, insbesondere fremder Shopping-Preis-/Bedarfsdatenstatus und 8 unabhängige Flags. Haupt-App und alle anderen Modulrepos sind ausschließlich lesbar; Schreibziel bleibt `EG-Rezept-KI`.
