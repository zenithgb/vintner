# Phase 6A — Press By-products Acceptance

## Scope

Pressing one complete batch of four grapes now produces:

- one bottle of must in the press;
- one Pomace item; and
- one Grape Seeds item.

By-products go directly into the pressing player's inventory. If that inventory
cannot accept them, they drop beside the press instead. A failed press never
creates by-products.

## Uses

- Pomace and Grape Seeds can both be added to a vanilla composter.
- Two Pomace craft into one Vintner Compost.
- Grape Seeds are deliberately not plantable. Cultivars are propagated with
  cuttings because grape seedlings do not grow true to the parent vine. Seeds
  remain a processing resource for later pantry recipes.

## Automated acceptance

Run:

```bash
./gradlew clean build auditReleaseAssets runGameTest
```

Expected:

- release asset audit passes;
- all required GameTests pass;
- `pressingRecoversWineryByproducts` proves exact one-per-cycle recovery;
- existing press capacity, batch metadata, provenance, and must-output tests
  remain green.

## Manual in-game acceptance

1. Put four matching grapes in a press.
2. Empty-hand activate the press once.
3. Confirm one Pomace and one Grape Seeds appear in the inventory.
4. Confirm the press contains one must output.
5. Try an empty-hand activation again and confirm no extra by-products appear.
6. Put both by-products in a vanilla composter and confirm they are accepted.
7. Craft two Pomace together and confirm one Compost is produced.
8. Fill the player inventory, press another batch, and confirm both by-products
   drop at the press rather than being lost.
