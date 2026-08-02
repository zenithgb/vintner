# Phase 8B — Buyer Profiles

The Vintner's Almanac now compares an inspected bottle with ten stable buyer
profiles from the economy roadmap. It reports the bottle's best market fit,
the buyer preference premium, and a potential value without expanding the
normal item tooltip.

## Buyer profiles

- tavern keeper;
- village merchant;
- noble household;
- monastery;
- mining settlement;
- coastal settlement;
- cold-region settlement;
- festival organiser;
- travelling merchant;
- collector.

Preferences use wine facts that currently exist: red or white style, quality,
age stage, and whether the bottle is spoiled. Examples include dependable
young table wine for taverns, fresh white wine for coastal settlements, robust
red wine for cold or mining settlements, and rare peak wine for collectors.

## Manual acceptance

1. Inspect a young red table wine with the Vintner's Almanac. Its best fit
   should be a tavern keeper.
2. Inspect a young good white wine. Its best fit should be a coastal
   settlement.
3. Inspect a peak legendary wine. Its best fit should be a collector and its
   potential value should exceed the independent appraisal.
4. Inspect a spoiled bottle. It should retain zero appraisal value and should
   not display a market outlook.

## Deliberately deferred

This milestone describes market fit; it does not spawn pretend buyers or
silently alter vanilla merchant offers. Actual regional buyers, settlement
demand, contracts, live offers, and market events remain later Phase 8 work.
