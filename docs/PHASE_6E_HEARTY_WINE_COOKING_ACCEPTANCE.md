# Phase 6E — Hearty Wine Cooking Acceptance

## Scope

This milestone completes two more Phase 6 cooking entries:

- Braised Meat combines red wine, Cooked Porkchop, Carrot, and Brown Mushroom;
- Vineyard Soup combines any wine, a Bowl, Beetroot, Potato, and Brown
  Mushroom.

Both recipes accept aged bottles as well as fresh wine and return the empty
Glass Bottle. They use the existing recipe book and pairing feedback, with no
new screen or expanded tooltip.

## Balance

| Food | Nutrition | Saturation modifier | Pairing |
| --- | ---: | ---: | --- |
| Braised Meat | 10 | 0.90 | Red |
| Vineyard Soup | 9 | 0.75 | White |

## Automated acceptance

Run:

```bash
./gradlew clean build auditReleaseAssets runGameTest
```

Expected:

- fresh and aged wines resolve through the shared wine tags;
- both foods load with food components and matching pairing tags;
- red and white wine inventory changes unlock the new recipes;
- the full required GameTest suite passes.

## Manual in-game acceptance

1. Craft Braised Meat with fresh Red Wine, then with Aged Red Wine.
2. Craft Vineyard Soup with each of the four core wine bottles.
3. Confirm each craft returns one empty Glass Bottle.
4. Eat both meals in Survival and confirm their hunger values feel appropriate.
5. Confirm red wine pairs with Braised Meat and white wine with Vineyard Soup.
6. Inspect both custom inventory icons at normal GUI scale.
