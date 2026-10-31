# Integrations-Ports – Rezept-KI
Fachliche Beispielsignaturen, nicht zwingende Methodennamen:
- `generateCandidates(request, contextSnapshots, generatorStrategy) -> RecipeCandidate[]`
- `validateRecipe(candidate, ingredientFacts, restrictions, availableEquipment) -> ValidationReport`
- `estimateNutrition(validatedRecipe, knownIngredientNutrition) -> NutritionEstimateWithGaps`
- `toDraftContract(validatedRecipe, userFlags) -> RecipeDraft`

Alle Ports müssen über synthetische Offline-Snapshots testbar sein. `considerStock`, `considerShopping`, `considerOffers` sind **unabhängig** und vollständig zu testen. `recipe.draft` folgt `EG-Architecture/contracts/v1`; neue Felder nur als dokumentierte Extension oder Architekturänderungsantrag. Codex bindet die Ausgabe nach ausdrücklicher Prüfung an vorhandene versionierte Rezept-, Planung- und Küchenoperationen. Die KI erhält nie automatisch Room/Sheets-Zugriff, darf keine eigenständigen Bestands- oder Verzehrbuchungen veranlassen und muss bei nicht verfügbarem Modell sicher degradieren.
