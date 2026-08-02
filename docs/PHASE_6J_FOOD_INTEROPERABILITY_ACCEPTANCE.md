# Phase 6J — Food Interoperability Acceptance

Vintner pantry foods now advertise conventional food categories so recipe
viewers and compatible mods can classify them without a hard dependency.
Soup-like meals also return their bowl after they are eaten.

## Conventional categories

- Fruit: red grapes, white grapes, raisins, and poached fruit.
- Bread: vineyard bread.
- Pie: grape tart.
- Soup: red wine stew and vineyard soup.
- Cooked meat: braised meat.
- Cooked fish: white wine fish.

The existing optional Farmer's Delight meal tag remains available when that
mod is installed and now has a readable fallback label during development.

## Manual checks

1. In Survival, eat Red Wine Stew and confirm an empty bowl is retained.
2. Eat White Wine Fish and Vineyard Soup and confirm each also retains its
   bowl.
3. Confirm recipe viewers group each pantry food under the expected broad food
   category when convention-tag support is present.
4. Launch without Farmer's Delight and confirm Vintner loads normally.
5. Launch with Farmer's Delight and confirm the cutting-board and cooking-pot
   integrations still load and display normally.

