import json, sys
from pathlib import Path
from jsonschema import Draft202012Validator, FormatChecker
drafts = json.loads(Path(sys.argv[1]).read_text())
schema = json.loads(Path(sys.argv[2]).read_text())
validator = Draft202012Validator(schema, format_checker=FormatChecker())
assert len(drafts) == 56
assert len({x["payload"]["draftId"] for x in drafts}) == 56
assert all(x["messageType"] == "recipe.draft" and
           x["payload"]["status"] == "DRAFT" and validator.is_valid(x) and
           x["payload"]["xRecipeKi"]["mealPrep"]["consumptionRecorded"] is False
           for x in drafts)
print("PASS 56 plan drafts satisfy pinned draft-2020-12 schema")
