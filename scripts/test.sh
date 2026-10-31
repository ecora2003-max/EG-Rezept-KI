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
