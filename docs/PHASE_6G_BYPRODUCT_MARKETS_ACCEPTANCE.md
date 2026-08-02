# Phase 6G — By-product Markets Acceptance

This milestone gives press by-products two additional low-friction uses while
keeping the future vinegar and distillation systems in Advanced Winemaking.

## Player-facing behaviour

- Two Pomace craft into two Purple Dye, mirroring vanilla crop-to-dye recipes.
- Novice Winemakers buy twelve Pomace for one Emerald.
- Apprentice Winemakers buy sixteen Grape Seeds for one Emerald.
- Existing compost, animal feed, oil, fuel, and composting uses remain intact.
- Trade refreshes remain idempotent and do not duplicate offers.

## Automated acceptance

- Recipe advancement audit confirms Pomace unlocks the dye recipe.
- Specialist trade progression checks both new buy offers at the correct tiers.
- Full asset audit, build, and GameTest suite must pass.

## Deferred

Vinegar and distillation remain part of the planned Advanced Winemaking release
because both require their own production, balance, art, and failure-recovery
loops. This milestone does not weaken that progression with generic crafting.
