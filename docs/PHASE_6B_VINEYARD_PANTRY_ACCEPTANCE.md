# Phase 6B — Vineyard Pantry Acceptance

## Scope

This milestone establishes a compact survival cooking loop without adding a
new workstation or screen:

- smoke either red or white grapes into two Raisins;
- combine Bread, two Raisins, and Grape Seeds into Vineyard Bread;
- combine Wheat, Sugar, an Egg, and two Raisins into two Grape Tarts.

Each item has dedicated pixel art and appears with the other vineyard foods in
the Vintner creative tab.

## Food and pairing balance

| Food | Nutrition | Saturation modifier | Wine pairing |
| --- | ---: | ---: | --- |
| Raisins | 2 | 0.25 | White |
| Vineyard Bread | 7 | 0.70 | Red and white |
| Grape Tart | 8 | 0.55 | White |

Vineyard Bread is the practical travel food and gives Grape Seeds an immediate
use. The tart is the richer pantry recipe. Neither item adds a new tooltip; the
recipe book and existing pairing feedback teach the loop in a vanilla-style
way.

## Automated acceptance

Run:

```bash
./gradlew clean build auditReleaseAssets runGameTest
```

Expected:

- release asset audit passes;
- all required GameTests pass;
- pantry foods contain food components;
- the pairing tags activate the existing wine pairing system;
- red grapes, white grapes, Pomace, and Raisins unlock their related recipes.

## Manual in-game acceptance

1. Put one Red Grapes in a smoker and confirm two Raisins are produced.
2. Repeat with White Grapes.
3. Confirm both smoking recipes appear after acquiring their grape ingredient.
4. Acquire Raisins and confirm Vineyard Bread and Grape Tart appear in the
   recipe book.
5. Craft and eat all three foods in Survival.
6. Drink red wine and eat Vineyard Bread; confirm the pairing feedback appears.
7. Drink white wine and eat each of the three foods; confirm pairing feedback.
8. Check the three inventory icons at normal GUI scale for clarity and centring.
