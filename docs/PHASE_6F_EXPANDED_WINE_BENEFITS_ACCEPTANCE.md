# Phase 6F — Expanded Wine Benefits Acceptance

This milestone deepens the four established wine profiles without adding new
wine categories before their production loops exist.

## Player-facing behaviour

- Red wine keeps its steady-footing and combat-exhaustion identity, while also
  reducing movement penalties and making small falls safer.
- White wine keeps its work and hunger-efficiency identity, while improving
  underwater mining and water movement.
- Aged red wine adds a modest armour benefit to its stronger combat profile.
- Aged white wine adds luck to its stronger work, water, and hunger profile.
- Existing quality scaling, diminishing returns, impairment, pairing, and
  profile replacement rules remain unchanged.
- Tooltips stay as a single concise benefit line. Detailed effect information
  remains available through the Vintner's Almanac and the inventory effect UI.

## Automated acceptance

- `wineProfilesUseRoadmapBenefits` checks red agility and white water benefits.
- `agedWineProfilesAddCellarWorthyBenefits` checks aged armour/luck benefits and
  confirms that changing profiles removes the previous profile's attributes.
- The full release asset audit and GameTest suite must pass.

## Deferred families

Dessert, fortified, and sparkling benefits remain deferred until those wine
styles have complete production, item, balance, and art pipelines. They should
not be represented by renamed versions of the current four bottles.
