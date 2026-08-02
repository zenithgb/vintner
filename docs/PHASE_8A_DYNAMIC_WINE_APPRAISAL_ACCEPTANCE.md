# Phase 8A — Dynamic Wine Appraisal Foundation

The Vintner's Almanac now appraises a bottle from deterministic wine data
instead of exposing only the original quality-and-age estimate. Existing
independent bottles retain their previous values, while factors supported by
current gameplay can modify the result.

## Current factors

- base wine style value;
- quality adjustment;
- bottle age and maturity stage;
- the recorded producer's estate reputation;
- storage damage and spoilage condition.

Producer reputation is resolved from the bottle's persistent producer UUID.
Unknown and legacy producers receive no premium. Damaged bottles receive a
bounded penalty, and spoiled bottles have no appraisal or prestige value.

The normal item tooltip remains compact. Detailed market factors are shown
only when a player deliberately inspects the bottle with the Vintner's
Almanac.

## Manual acceptance

1. Hold a bottle of wine in one hand and the Vintner's Almanac in the other.
2. Use the Almanac and confirm it reports an estimated emerald value and
   prestige.
3. Compare a legacy or independent bottle with a bottle produced by an estate
   that has earned a higher reputation tier. The latter should show a positive
   estate-reputation factor.
4. Store an otherwise equivalent bottle in poor cellar conditions and inspect
   it again. It should show a negative bottle-condition factor.
5. Confirm a spoiled bottle appraises at zero value and zero prestige.

## Deliberately deferred

Regional demand, buyer preferences, vintage reputation, merchant offers,
contracts, and event-driven demand remain later Phase 8 milestones. This
foundation does not claim those systems exist and does not change villager
trade prices yet.
