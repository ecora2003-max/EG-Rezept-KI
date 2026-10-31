#!/usr/bin/env python3
"""Full draft 2020-12 schema contract validation. Requires installed 'jsonschema'."""
import copy
import json
import sys
from pathlib import Path

from jsonschema import Draft202012Validator, FormatChecker


def main() -> None:
    if len(sys.argv) != 3:
        raise SystemExit("usage: full_contract_check.py MESSAGE.json CONTRACT.schema.json")
    message = json.loads(Path(sys.argv[1]).read_text())
    schema = json.loads(Path(sys.argv[2]).read_text())
    Draft202012Validator.check_schema(schema)
    validator = Draft202012Validator(schema, format_checker=FormatChecker())
    failures = list(validator.iter_errors(message))
    assert not failures, "Positive recipe.draft was rejected: " + "\n".join(str(x) for x in failures)
    print("PASS full draft 2020-12 recipe.draft message")

    def bad(mutator):
        candidate = copy.deepcopy(message)
        mutator(candidate)
        assert not validator.is_valid(candidate), "Malformed recipe accepted"

    tests = (
        lambda m: m.__setitem__("contractVersion", "2.0.0"),
        lambda m: m["payload"]["flags"].__setitem__("considerOffers", "true"),
        lambda m: m["payload"].__setitem__("portionCount", 0),
        lambda m: m["payload"].__setitem__("ingredients", []),
        lambda m: m["payload"]["ingredients"][0]["quantity"].__setitem__("unit", "kg"),
        lambda m: m["payload"]["ingredients"][0].__setitem__("sourceStatus", "VERIFIED_BY_AI"),
        lambda m: m["meta"].pop("createdAt"),
        lambda m: m["payload"].__setitem__("status", "CONSUMED"),
    )
    for test in tests:
        bad(test)
    print(f"PASS {len(tests)} malformed messages rejected by full draft 2020-12 schema")


if __name__ == "__main__":
    main()
