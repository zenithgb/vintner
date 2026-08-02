# Phase 6H — Farmer's Delight Compatibility Acceptance

This milestone keeps Farmer's Delight optional while integrating its
established kitchen workflow with Vintner.

## Standalone checks

- Vintner starts and loads every resource without Farmer's Delight installed.
- Existing crafting recipes remain available and unchanged.
- Optional Farmer's Delight recipe serializers are never decoded while the
  mod is absent.

## Integrated checks

Run the compatibility test lane:

```bash
./gradlew -PfarmersDelightTest runGameTest
```

Then verify in a client with Farmer's Delight Refabricated installed:

- Red Wine Stew, Braised Meat, White Wine Fish, Vineyard Soup, and Poached
  Fruit appear in the Cooking Pot recipe book and cook successfully.
- Cutting either colour of grapes with a knife on a Cutting Board always
  recovers Grape Seeds and has a 50% chance to recover Pomace.
- Vintner's four substantial cooked meals are recognised as Farmer's Delight
  meals.
- Meat-heavy Farmer's Delight meals pair with red wine.
- Fish, vegetable, and lighter Farmer's Delight meals pair with white wine.
- The original Vintner crafting recipes still work alongside the integration.

## Compatibility boundary

Farmer's Delight is not declared as a required dependency. The optional
recipes use `fabric:load_conditions`, and all pairing entries are optional tag
values, so a standalone Vintner installation remains supported.
