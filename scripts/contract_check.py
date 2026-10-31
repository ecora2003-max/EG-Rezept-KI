#!/usr/bin/env python3
"""Portable, dependency-free semantics check, not a full JSON-Schema validator."""
import datetime
import json
import sys
from pathlib import Path

if len(sys.argv) != 2:
    raise SystemExit("usage: python scripts/contract_check.py <draft.json>")
message = json.loads(Path(sys.argv[1]).read_text())
assert message["contractVersion"] == "1.0.0-draft"
assert message["messageType"] == "recipe.draft"
meta = message["meta"]
assert meta["producer"] and meta["traceId"]
datetime.datetime.fromisoformat(meta["createdAt"])
payload = message["payload"]
assert payload["status"] == "DRAFT" and payload["draftId"] and payload["title"]
assert type(payload["portionCount"]) is int and payload["portionCount"] > 0
assert payload["ingredients"] and payload["steps"]
assert all(type(payload["flags"][key]) is bool for key in ("considerStock", "considerShopping", "considerOffers"))
assert all(ingredient["label"] and ingredient["sourceStatus"] in ("CONFIRMED", "SOURCED", "ESTIMATED", "UNKNOWN")
           and ingredient["quantity"]["unit"] in ("g", "ml", "piece", "portion")
           and (ingredient["quantity"]["amount"] is None or
                (type(ingredient["quantity"]["amount"]) is int and ingredient["quantity"]["amount"] >= 0))
           for ingredient in payload["ingredients"])
assert all(isinstance(x, str) for x in payload["warnings"])
assert payload["xRecipeKi"]["mealPrep"]["rawIngredientsCharged"] is False
assert payload["xRecipeKi"]["mealPrep"]["consumptionRecorded"] is False
print("PASS recipe.draft basic contract structure; complete Draft-2020-12 schema validation remains pending")
