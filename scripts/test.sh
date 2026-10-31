#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
python3 scripts/restore_sources.py
mkdir -p build
kotlinc src/main/kotlin/de/eg/recipe/RecipeEngine.kt src/test/kotlin/de/eg/recipe/RecipeEngineTest.kt -include-runtime -d build/tests.jar
java -ea -jar build/tests.jar
kotlinc src/main/kotlin/de/eg/recipe/RecipeEngine.kt examples/Demo.kt -include-runtime -d build/demo.jar
java -jar build/demo.jar > build/recipe-draft.json
python3 scripts/contract_check.py build/recipe-draft.json
kotlinc src/main/kotlin/de/eg/recipe/RecipeEngine.kt src/main/kotlin/de/eg/recipe/CombinatorialGenerator.kt src/test/kotlin/de/eg/recipe/CombinatorialGeneratorTest.kt -include-runtime -d build/combo-tests.jar
java -ea -cp build/combo-tests.jar de.eg.recipe.CombinatorialGeneratorTest
kotlinc src/main/kotlin/de/eg/recipe/RecipeEngine.kt src/main/kotlin/de/eg/recipe/CombinatorialGenerator.kt src/main/kotlin/de/eg/recipe/GoalAwareRecipePlanner.kt src/test/kotlin/de/eg/recipe/GoalAwareRecipePlannerTest.kt -include-runtime -d build/goal-tests.jar
java -ea -cp build/goal-tests.jar de.eg.recipe.GoalAwareRecipePlannerTest
kotlinc src/main/kotlin/de/eg/recipe/RecipeEngine.kt src/main/kotlin/de/eg/recipe/CombinatorialGenerator.kt src/main/kotlin/de/eg/recipe/GoalAwareRecipePlanner.kt examples/SmartDemo.kt -include-runtime -d build/smart-demo.jar
java -jar build/smart-demo.jar > build/smart-draft.json
python3 scripts/contract_check.py build/smart-draft.json
kotlinc src/main/kotlin/de/eg/recipe/RecipeEngine.kt src/main/kotlin/de/eg/recipe/CombinatorialGenerator.kt src/main/kotlin/de/eg/recipe/OnDeviceRecipeGateway.kt src/test/kotlin/de/eg/recipe/OnDeviceRecipeGatewayTest.kt -include-runtime -d build/on-device-gateway-tests.jar
java -ea -cp build/on-device-gateway-tests.jar de.eg.recipe.OnDeviceRecipeGatewayTest
kotlinc src/main/kotlin/de/eg/recipe/RecipeEngine.kt src/main/kotlin/de/eg/recipe/CombinatorialGenerator.kt src/main/kotlin/de/eg/recipe/GoalAwareRecipePlanner.kt src/main/kotlin/de/eg/recipe/MealPlanDraftPlanner.kt src/main/kotlin/de/eg/recipe/MealPlanBuild.kt src/main/kotlin/de/eg/recipe/MealPlanView.kt src/main/kotlin/de/eg/recipe/MealPlanDemand.kt src/test/kotlin/de/eg/recipe/MealPlanHorizonTest.kt src/test/kotlin/de/eg/recipe/MealPlanSafetyTest.kt -include-runtime -d build/meal-plan-tests.jar
java -ea -cp build/meal-plan-tests.jar de.eg.recipe.MealPlanHorizonTest
java -ea -cp build/meal-plan-tests.jar de.eg.recipe.MealPlanSafetyTest
kotlinc src/main/kotlin/de/eg/recipe/RecipeEngine.kt src/main/kotlin/de/eg/recipe/CombinatorialGenerator.kt src/main/kotlin/de/eg/recipe/GoalAwareRecipePlanner.kt src/main/kotlin/de/eg/recipe/MealPlanDraftPlanner.kt src/main/kotlin/de/eg/recipe/MealPlanBuild.kt src/main/kotlin/de/eg/recipe/MealPlanView.kt src/main/kotlin/de/eg/recipe/MealPlanDemand.kt examples/MealPlanDemo.kt -include-runtime -d build/meal-plan-demo.jar
java -jar build/meal-plan-demo.jar > build/meal-plan-drafts.json
python3 -m json.tool build/meal-plan-drafts.json > /dev/null
