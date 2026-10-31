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

## Host-Autorisierung vor optionaler Android-Inferenz (31.10.2026)

Der neue **pure Kotlin**-Port `AndroidLocalModelAdapter.kt` nimmt eine vom Android-Host implementierte `AndroidBridge` und eine `Authorization` entgegen. Vor jedem Status- oder Inferenzaufruf müssen **alle drei** Host-Freigaben wahr sein: `userOptedIn` (ausdrückliches Opt-in), `requestDataApproved` (Freigabe der tatsächlich an das lokale Modell gegebenen Anfrage) und `localOnlyExecutionVerified` (Host hat ausschließlich lokale Ausführung nachgewiesen). Sonst folgt deterministisch `OFFLINE_FALLBACK` mit `USER_OPT_OUT`, `MODEL_INPUT_NOT_APPROVED` oder `LOCAL_ONLY_NOT_VERIFIED`; die Bridge wird dann **nicht** aufgerufen. Die Flags und Snapshot-Freigaben werden zusätzlich vom bestehenden Gateway gefiltert.

**Diese drei Werte sind nur vom Host gesetzte Aussagen, keine technisch nachgewiesene Geräte-/Netzwerk-Attestation.** Der Android-Integrator muss den tatsächlichen SDK-Aufruf, die Modellverfügbarkeit, Datentransfers, die Nutzereinwilligung und die Laufzeitberechtigungen gesondert prüfen. Ein Bridge-Objekt, das eine Cloud-API aufruft, verletzt den Vertrag und ist nicht zulässig. Es gibt weiterhin **keine echte Android-, ML-Kit- oder Hardwareprüfung**; der neue Test benutzt ausschließlich eine synthetische Fake-Bridge. Kein automatischer Modelldownload, keine Cloud-Pflicht, keine Buchung oder Freigabe eines Rezeptentwurfs.
