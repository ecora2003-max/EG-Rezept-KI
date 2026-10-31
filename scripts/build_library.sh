#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 scripts/restore_sources.py
mkdir -p build
# Kotlin-only JVM artifact. No fat jar, Android SDK, network or model library.
kotlinc \
  src/main/kotlin/de/eg/recipe/RecipeEngine.kt \
  src/main/kotlin/de/eg/recipe/CombinatorialGenerator.kt \
  src/main/kotlin/de/eg/recipe/GoalAwareRecipePlanner.kt \
  src/main/kotlin/de/eg/recipe/OnDeviceRecipeGateway.kt \
  src/main/kotlin/de/eg/recipe/AndroidLocalModelAdapter.kt \
  src/main/kotlin/de/eg/recipe/RecipeDraftImport.kt \
  src/main/kotlin/de/eg/recipe/RecipeDraftRevalidation.kt \
  src/main/kotlin/de/eg/recipe/BoundedDraftJson.kt \
  src/main/kotlin/de/eg/recipe/MealPlanDraftPlanner.kt \
  src/main/kotlin/de/eg/recipe/MealPlanBuild.kt \
  src/main/kotlin/de/eg/recipe/MealPlanView.kt \
  src/main/kotlin/de/eg/recipe/MealPlanDemand.kt \
  -d build/eg-rezept-ki-core.jar
python3 - <<'PY'
import zipfile
with zipfile.ZipFile('build/eg-rezept-ki-core.jar') as jar:
    names = set(jar.namelist())
    for item in (
        'de/eg/recipe/RecipeEngine.class',
        'de/eg/recipe/BoundedDraftJson.class',
        'de/eg/recipe/RecipeDraftImport.class',
        'de/eg/recipe/RecipeDraftRevalidation.class',
        'de/eg/recipe/AndroidLocalModelAdapter.class',
        'de/eg/recipe/MealPlanDemand.class',
    ):
        assert item in names, f'missing compiled source: {item}'
print('PASS compiled Kotlin-only release JAR with all required public ports')
PY
