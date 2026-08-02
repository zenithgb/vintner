# Phase 6D — Wine Cooking Acceptance

## Scope

This milestone begins the roadmap's wine-cooking branch with three practical
crafting recipes:

- Red Wine Stew from red wine, a Bowl, Cooked Beef, Carrot, and Baked Potato;
- White Wine Fish from white wine, a Bowl, Cooked Cod, Carrot, and Baked Potato;
- two Poached Fruit from white wine, an Apple, and Sugar.

Fresh and aged bottles are accepted through red- and white-wine item tags. The
wine is consumed by cooking, while its Glass Bottle is returned. The finished
food does not retain or expose bottle metadata, keeping its tooltip compact.

## Food and pairing balance

| Food | Nutrition | Saturation modifier | Pairing |
| --- | ---: | ---: | --- |
| Red Wine Stew | 10 | 0.80 | Red |
| White Wine Fish | 8 | 0.75 | White |
| Poached Fruit | 6 | 0.60 | White |

Each food has dedicated 16-by-16 pixel art and appears in the Vintner and
Food & Drinks creative tabs.

## Automated acceptance

Run:

```bash
./gradlew clean build auditReleaseAssets runGameTest
```

Expected:

- all recipes and item assets load;
- every meal contains a food component;
- fresh and aged bottles match their colour's cooking tag;
- all wine items return a Glass Bottle when used in crafting;
- acquiring red or white wine unlocks the corresponding recipes.

## Manual in-game acceptance

1. Acquire Red Wine and confirm Red Wine Stew appears in the recipe book.
2. Acquire White Wine and confirm White Wine Fish and Poached Fruit appear.
3. Repeat each recipe with an aged bottle of the same colour.
4. Confirm every recipe returns exactly one Glass Bottle.
5. Eat each food in Survival and confirm hunger and saturation are restored.
6. Confirm each matching wine pairing produces the existing pairing feedback.
7. Check all three custom inventory icons at normal GUI scale.
