# EG-Rezept-KI – Kotlin-Fachkern-/Vertragsreferenz

**Stand 18.11.2026:** Dieses Repo enthält den eigenständigen geprüften Rezept-Fachkern, Offlinegenerator, Mengen-/Allergen-/Planungsvalidierung und Referenztests. Die aktuelle Android-Rezeptoberfläche mit **Gemini Nano 4 Fast, Nano 3, Gemini Cloud** wird bereits in [Ernaerungsgedaechnis](https://github.com/ecora2003-max/Ernaerungsgedaechnis) integriert und dort weiterentwickelt.

- Kein eigenständiger Dauerauftrag für „Chat 5“ oder weitere Abnahme- und Architektur-Chat-Schleifen. Bestehende `docs/STATUS.md` und ältere Integrationstexte sind Nachschlagewerke zum Stand 31.10.2026.
- Unverändert erhalten: Kotlin-Fachcode, v1 `1.0.0-draft`-Vertragssemantik, Offline-/Import-/Revalidierungstests, keine stillschweigende v1.1-Aktivierung.
- Unverzichtbare Grenzen: generative Ausgaben sind **Entwürfe**, keine belegten Produkt-/Nährwert-/Allergen-/Portionsfakten; konkrete Produktversion/Mengen-/Zustandsprüfungen und echte separate Nutzerbestätigungen für Rezept, Herstellung und Verzehr.
- Die drei optionalen Kontextquellen Vorrat, Einkaufsliste, Angebote bleiben unabhängig. Kein Cloudaufruf ohne ausdrückliche aktuelle Kosten-/Datenfreigabe, kein ungefragter Fallback und keine privaten Daten/API-Secrets im Repo.
- Nur bei einem konkreten Nutzerauftrag hier Code und Tests ändern. Reale Nano-/Cloud-Geräteabnahme gehört in den Haupt-App-PR, nicht in einen weiteren Modul-Statusprozess.

Kein Schreiben in andere Repositories ohne neuen ausdrücklichen Auftrag.
