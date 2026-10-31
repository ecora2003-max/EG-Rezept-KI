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
