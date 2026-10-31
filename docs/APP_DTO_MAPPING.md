# Read-only compatibility mapping to the existing Android app (31.10.2026)

**Inspected but not modified:** `ecora2003-max/Ernaerungsgedaechnis@6ba419ed2875c1988f45afee65e2708e7895cacf`, notably `data/local/Values.kt`, `PlanningModels.kt`, `PlanningOperations.kt`, `PlanningReader.kt`, `NutritionReader.kt`, `Entities.kt`, `FoodStore.kt`, `docs/PLANNING.md`, `docs/DEVELOPMENT.md`. The repository is **private, read-only** for this module; do not copy host code, credentials, user data, or DB files to this public module repository.

## Verified shape of the adapter boundary

- The host's `BaseUnit` has `GRAM`, `MILLILITRE`, `PORTION`; it stores **all amounts as thousandths** of their declared unit. `Amounts.MAX` is `1_000_000_000_000L`. This is not the same representation as the recipe contract's integer `g`, `ml`, `portion`.
- `AppQuantityBridge.project` converts known whole `g`/`ml`/`portion` to exact `amountMilli=amount*1000` with checked integer multiplication and the host's upper bound. A `piece` quantity requires an independently **confirmed milli-gram-per-piece** value. Missing quantities remain `null`; incompatible `GROSS`, `UNKNOWN`, mismatched basis and overflow are blocked, never guessed or rounded.
- Host `ProductVersionEntity.id` is a **concrete version identifier**. `RecipeDraft.ingredients[].foodTypeId` is an abstract food type and **must never be copied as a productVersionId**. The host must resolve the correct concrete version and pass `identityConfirmed=true` independently. The bridge reports `UNRESOLVED_PRODUCT_VERSION` or `UNCONFIRMED_IDENTITY` rather than inventing a mapping.
- The existing host `RecipeInput(name, yieldPortions, ingredients, steps, ...)` uses **milli-portions** for `yieldPortions`, and each `RecipeItemInput(versionId, amount, note)` refers to an exact **product version** (not abstract FoodType). Its real `KitchenOperations.saveRecipe` rejects duplicate product-version ingredients, unknown versions, invalid counts and blank titles. Codex must resolve, check and, where necessary, explicitly merge multiple ingredients mapped to one actual product version **without hiding different RAW/COOKED or NET/DRAINED bases**, only after user confirmation. The reference `AppQuantityBridge` intentionally does not call this writer.
- Actual cooking requires a separate `FoodStore.produce(key, ProductionInput, confirmed)` including real allocated batches, confirmed storage and dates. An AI suggestion or recipe-version save does **not** satisfy that transaction.
- Host `PlanInput` uses `PlanEntryInput`, `PlanKind.PREPARATION` and `recipeVersionId` for cooking plans. **A `recipe.draft` is not a host recipe version**: Codex must show the review and obtain explicit confirmation before using its app-specific recipe-version write boundary. The module has no `FoodStore` imports and no call to `savePlan` or `saveRecipe`.
- Host `PlanningReader.report` already computes coherent 1/7/56-day demand, takes competing active plans into account and uses an existing allocation ledger. The module's plan summaries are previews **not independent demand events**; do not add their amounts to host demand or write duplicate bookings.
- The host's `NutritionReader.recipe(id)` uses stored versioned ingredients and can propagate missing values. Its existing `ProductVersionEntity` nutrient fields are `Double?` plus `Source/Quality`, whereas this module calculates only explicitly backed `BigDecimal` estimates. No implicit precision guarantee or automatic coercion between them; host must preserve unknowns and source quality on import.
- Confirming a draft must not implicitly call `FoodStore` production, stock or consumption methods. Host `EntryStatus.CONFIRMED` in **plan records** does not mean food was cooked or eaten.

## Sample pure Kotlin mapping (no Room/Android dependency)

```kotlin
val checked = AppQuantityBridge.project(
    AppQuantityBridge.SourceLine(
        foodTypeId = "synthetic-rice",
        productVersionId = explicitlyResolvedHostVersionId,
        identityConfirmed = hostConfirmedTheSpecificVersion,
        quantity = RecipeEngine.Quantity(
            amount = 200,
            unit = RecipeEngine.UnitCode.G,
            basis = RecipeEngine.Basis.NET,
            state = RecipeEngine.State.RAW
        )
    )
)
check(checked.amountMilli == 200_000L)
// checked.structurallyMappable checks the amount and ID structure only.
// It is NOT permission to write a host plan or a food-safety clearance.
```

## Unresolved app integration gates

An independent test of `AppQuantityBridge` uses **synthetic DTO shapes and the verified host unit constants**; it is **not** an actual imported Android app class, Room migration, Compose screen, or instrumentation test. Codex must add an internal app adapter, revalidate host version IDs and source rights, validate all mandatory food safety facts and user confirmation, then run app JVM/Room/lint/compileAndroidTestKotlin and actual device tests separately. Existing app phase-5 baseline does not implement this integration.
