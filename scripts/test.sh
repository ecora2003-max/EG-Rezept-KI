#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 scripts/restore_sources.py
mkdir -p build

# Compile all synthetic JVM tests, public ports and examples together once.
# This also catches cross-port symbol and packaging conflicts while reducing
# GitHub Free Actions compiler minutes versus recompiling each test suite.
kotlinc src/main/kotlin/de/eg/recipe/*.kt \
  src/test/kotlin/de/eg/recipe/*.kt examples/*.kt \
  -include-runtime -d build/offline-all-tests.jar

for klass in \
  RecipeEngineTestKt \
  CombinatorialGeneratorTest \
  GoalAwareRecipePlannerTest \
  OnDeviceRecipeGatewayTest \
  MealPlanHorizonTest \
  MealPlanSafetyTest \
  MealPlanDemandTest \
  RecipeDraftImportTest \
  RecipeDraftRevalidationTest \
  AndroidLocalModelAdapterTest \
  BoundedDraftJsonTest \
  AppQuantityBridgeTest
do
  java -ea -cp build/offline-all-tests.jar "de.eg.recipe.$klass"
done

java -ea -cp build/offline-all-tests.jar de.eg.recipe.DemoKt > build/recipe-draft.json
python3 scripts/contract_check.py build/recipe-draft.json
java -ea -cp build/offline-all-tests.jar de.eg.recipe.SmartDemoKt > build/smart-draft.json
python3 scripts/contract_check.py build/smart-draft.json
java -ea -cp build/offline-all-tests.jar de.eg.recipe.MealPlanDemoKt > build/meal-plan-drafts.json
python3 -m json.tool build/meal-plan-drafts.json > /dev/null

# Build the runtime-free Kotlin core JAR separately; not an Android APK/AAR.
bash scripts/build_library.sh
