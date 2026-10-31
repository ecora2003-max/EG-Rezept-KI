# On-device-KI – überprüfter optionaler Integrationspfad (31.10.2026)

## Abgrenzung

**Der vorliegende Kotlin-Kern ruft kein Sprachmodell auf, stellt aber mittlerweile eine getestete lokale Modell-Schnittstelle bereit.** Alle Kernfunktionen laufen offline und kostenlos. KI-Modellkandidaten können über `RecipeCandidateGenerator`/`ProvidedCandidates` eingebracht werden, passieren aber ausnahmslos denselben deterministischen Validator und sind höchstens `REVIEW_REQUIRED`. Keine implizite Netzwerkübertragung personenbezogener Gesundheits-/Vorratsdaten.

## Nachgewiesene Android-Möglichkeit, keine pauschale Gerätegarantie

Google dokumentiert für ML Kit GenAI *Direct model access* (Gemini Nano via AICore) die Gradle-Abhängigkeit `com.google.mlkit:genai-prompt:1.0.0-beta4` und Android `minSdk 26`. Zur Laufzeit muss `Generation.getClient().checkStatus()` die Zustände `AVAILABLE`, `DOWNLOADABLE`, `UNAVAILABLE` (ggf. `DOWNLOADING`) unterscheiden. `DOWNLOADABLE` erfordert ein verfügbares/downladbares Modell, `UNAVAILABLE` fällt ohne KI auf `OfflineGenerator` zurück.

Google nennt außerdem ein begrenztes Modellkontextfenster, App-Quoten, Fehler bei nicht unterstützten Geräten, AICore-Initialisierung und Einschränkungen bei Geräten mit entsperrtem Bootloader. Die API ist Beta und kann inkompatibel geändert werden. Kein Modell-/Netzwerkstatus wurde auf tatsächlicher Hardware geprüft. Gerätelisten ändern sich; beim Start *immer* den Runtime-Status prüfen.

Offizielle Primärquellen:
- https://developers.google.com/ml-kit/genai/prompt/android/get-started
- https://developers.google.com/ml-kit/genai/prompt/android
- https://developers.google.com/ml-kit/genai

## Implementierter sicherer Modellport

`src/main/kotlin/de/eg/recipe/OnDeviceRecipeGateway.kt` kapselt `LocalModel.availability()` und `LocalModel.suggest(request)` für eine **rein lokale** Hostimplementierung. Bei fehlender Modellinstanz, Status `DOWNLOADABLE`, `DOWNLOADING`, `UNAVAILABLE`, `UNKNOWN`, Status-/Inferenzfehler, leerem oder fehlerhaftem Output und mehr als 32 Kandidaten erfolgt ein lokaler Offline-Fallback. Vorgeschaltete Datenminimierung verhindert, dass deaktivierte, nicht geteilte oder unqualifizierte Bestands-/Einkaufs-/Angebotssnapshots dem Modell übergeben werden. Modellkandidaten sind stets `UNTRUSTED_PROVIDED`/`REVIEW_REQUIRED`, nicht allergenfrei freigegeben. **13 synthetische Tests** bestanden in [CI Run 41221248578](https://github.com/ecora2003-max/EG-Rezept-KI/actions/runs/41221248578). Ein echter Android-AICore/ML-Kit-Adapter oder realer Geräte-/Privacytest ist weiterhin offen.

## Vorgesehene Trennung

1. Android-Host prüft Geräte-/Modellverfügbarkeit und Datenschutzfreigabe.
2. Nur bei verfügbarer On-device-Ausführung eine begrenzte, strukturierte Kandidatenliste anfordern. Modelle sind nicht als vertrauenswürdige Ernährungsdatenquelle zu betrachten; Nährwert- und Allergendaten weiterhin explizit als `RecipeFacts` übergeben.
3. Eingabeadapter prüft Größe, JSON-Struktur, Einheiten und zulässige Felder und mappt in `Candidate`.
4. `RecipeEngine.propose(request, facts, ProvidedCandidates(candidates))` erzwingt Validator + `REVIEW_REQUIRED`.
5. Bei nicht verfügbarer/inaktiver KI, Quoten-/Modellfehler oder User-Opt-out: **OfflineGenerator**.
6. Codex muss Android-Abhängigkeiten, Modellbereitstellung, Geräte-Tests, Nutzerzustimmung und OOM-/Latenzgrenzen separat prüfen. Kein Server, API-Key oder Zahlmodell wird vorausgesetzt.

**Noch offen:** konkreter Android-ML-Kit-Hostadapter und Geräte-E2E-Tests; deswegen keine Ankündigung als fertiges On-device-Feature.
