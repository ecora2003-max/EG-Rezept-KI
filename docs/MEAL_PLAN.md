# Read-only 1/7/56-day planning port

## Scope

The planning extension supplies optional meal/day/week/56-day context without introducing a new interchange message type or any new persistence. Its public Kotlin API is:
- `MealPlanDraftPlanner.Request(planId, start, horizonDays, slots, recipeRequest, maximumCombinations)`
- `MealPlanDraftPlanner.propose(request, facts): Plan`
- `plan.view(firstDay, days): View`
- `MealPlanDemand.aggregate(occurrences): List<Demand>`

The request horizon must be **1, 7 or 56** calendar days; 1–8 distinct daily meal slots are allowed. One occurrence ID is derived from **the same plan ID, date and unique slot ID**. Each selected dish remains a validated, standalone `recipe.draft` message under `1.0.0-draft`; a Plan is **internal Kotlin data**, not an invented `plan.draft` interchange contract.

Week and day projections filter existing occurrences and calculate quantities from their IDs. **Do not add** the totals of the day view to the week or 56-day view; doing so would double count. Only planned *raw ingredient quantities* are summarized, never booked stock consumption, purchases, production or actual eating.

## Safety and unknowns

- `GoalAwareRecipePlanner` checks each recipe on the applicable future date. Confirmed restrictions, equipment, meal types, and context flags pass to each slot; blocked candidates are excluded. If no valid option is available, an explicit unfilled slot is returned.
- `MealPlanDemand` groups by food type, unit, mass basis and RAW/COOKED state, avoiding false conversions. Incomplete quantities or overflowing sums produce `amount=null`, not fabricated zero or negative quantities.
- Source snapshot flags remain independent. Shopping demand is **not** purchased inventory. A future offer must be valid for that slot's date; no static 56-day discount guarantee exists.
- Frozen portion allocation across repeated meals is **not implemented**. Nonzero `freezePortions` or specified `freezerCapacityPortions` is rejected rather than silently ignored. A host must plan freezer usage separately for each production event.
- Nährwert-, Allergen- und Kühlkettenlücken bleiben im jeweiligen Rezeptentwurf erhalten. No actual stock/inventory writes exist here.
- The rotation across candidate options is deterministic, **not** scientifically personalized. Planning does not by itself prove variety, nutritional sufficiency or food safety.
- Only 1/7/56-day *projection*, not automatic time-series optimization against a daily protein budget or available freezer volume.

## Reproducible checks

`bash scripts/test.sh` compiles and executes `MealPlanHorizonTest` (7 checks) and `MealPlanSafetyTest` (7 checks) plus the previously validated 78 engine/model-gateway checks. `examples/MealPlanDemo.kt` emits 56 synthetic `recipe.draft` objects as JSON. CI separately invokes `scripts/plan_contract_check.py` with the pinned canonical JSON Schema and checks all **56 messages**. Record actual CI run SHA, pass/fail and skip count in `docs/STATUS.md`.

**Production integration:** Codex must map explicit user-confirmed meal slots to the existing Planning model and ensure it never treats multiple views as additive demand. No Room, Google Sheet, network call or other module's repository is modified by this port.
