# Phase 6C — Circular Winery Byproducts Acceptance

## Scope

This milestone makes every early winery byproduct useful with familiar vanilla
interactions:

- craft two Grape Seeds and a Glass Bottle into Grape Seed Oil;
- use Grape Seed Oil in Vineyard Bread and recover the empty bottle;
- feed Grape Seeds to chickens;
- feed Pomace to cows and sheep;
- compost all grape and rootstock cuttings;
- burn cuttings as a small, stick-like furnace fuel.

Grape Seeds remain a byproduct rather than a planting item. Cultivar cuttings
continue to be the reliable way to propagate grapes, reflecting that grapevines
grown from seed do not reliably preserve the parent cultivar.

## Balance

| Byproduct | Use | Value |
| --- | --- | --- |
| Grape Seeds | Oil recipe | 2 seeds per bottle |
| Grape Seeds | Chicken feed | Vanilla breeding interaction |
| Pomace | Cow and sheep feed | Vanilla breeding interaction |
| Pomace | Composting | 0.85 chance |
| Grape Seeds | Composting | 0.50 chance |
| Cuttings | Composting | 0.65 chance |
| Cuttings | Furnace fuel | Half one base smelt time |

## Automated acceptance

Run:

```bash
./gradlew clean build auditReleaseAssets runGameTest
```

Expected:

- release asset audit passes;
- all required GameTests pass;
- Grape Seed Oil returns a Glass Bottle after crafting;
- feed tags load for chickens, cows, and sheep;
- cuttings register as compostables and furnace fuel;
- acquiring Grape Seeds unlocks the oil recipe.

## Manual in-game acceptance

1. Acquire Grape Seeds and confirm Grape Seed Oil appears in the recipe book.
2. Craft the oil and check its custom icon at normal GUI scale.
3. Craft Vineyard Bread with the oil and confirm the Glass Bottle returns.
4. Feed Grape Seeds to chickens and confirm breeding hearts.
5. Feed Pomace to cows and sheep and confirm breeding hearts.
6. Put each cutting type into a composter and confirm it is accepted.
7. Use a cutting as furnace fuel and confirm it burns briefly.
