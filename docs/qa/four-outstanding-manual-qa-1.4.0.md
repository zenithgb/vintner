# Vintner 1.4.0 — four-correction manual acceptance

Reconciled status: **18 A — HISTORICALLY HUMAN-ACCEPTED; 1 C — SUPERSEDED**.

The preserved 2026-09-21 project acceptance covers this checklist except the
“all affected wood variants” sweep, whose exact scope later expanded with
Poplar. The original checkbox record is retained rather than retroactively
rewritten. See `docs/releases/1.4.0-release-state.md` and the exact-current-RC
delta checklist for the remaining representative Poplar check.

Use the replacement 1.4.0 candidate in a disposable profile and world copy.
Run once with shaders disabled, then repeat the visual/contact observations in
the normal showcase setup. Record screenshots and failures outside Git.

## Tasting Service pick behavior

- [ ] In Creative, target the displayed bottle and use the configured Pick
      Block/Pick Item control. Confirm the selected bottle has the exact wine
      item, custom name, cultivar/style, ageing, quality, vintage, batch,
      bottle number, producer/provenance, and remaining servings shown by the
      source service.
- [ ] Confirm the Tasting Service still contains the same unchanged bottle and
      servings after picking.
- [ ] Target the tray, stand, and another non-bottle part. Confirm normal block
      picking selects the matching Tasting Service.
- [ ] Confirm an empty service never supplies wine.
- [ ] Repeat the bottle and tray picks for all four horizontal facings.
- [ ] In Survival, confirm the same Pick control does not create a wine copy.

## Individual bottle identification

- [ ] Put visibly different wines in neighbouring slots of a Wine Rack, Wine
      Crate, Tasting Cabinet, and Labelled Cellar Shelf. Point at each bottle
      and confirm only that bottle's canonical name appears.
- [ ] Repeat with the display facing north, east, south, and west. For the Wine
      Crate, also check the top-down rows and columns.
- [ ] Point at empty slots, gaps, frames, labels, glass, and unrelated furniture
      surfaces. Confirm no other occupied slot is substituted.
- [ ] Remove, replace, and move the crosshair between bottles. Confirm the label
      clears or updates immediately and never survives a block/content change.
- [ ] Confirm the existing close-range and opaque-block obstruction behavior,
      plus single placed-bottle and Wine Basket identification.

## Wine appearance and contact

- [ ] Compare ordinary red, ordinary white, aged red, and aged white together
      in inventory/hand, as placed bottles, and in racks, crates, Tasting
      Cabinets, Labelled Cellar Shelves, Tasting Services, and Wine Baskets.
- [ ] Confirm red/white body colour is immediately readable and the restrained
      gold finish distinguishes aged wine within both colours.
- [ ] Confirm mixed storage follows the actual slot contents rather than an
      alternating or aggregate approximation.
- [ ] Check all affected wood variants and representative partial-fill states.
- [ ] With shaders off and in the normal showcase setup, inspect UVs, lighting,
      pivots, bottle contact, clipping, and facing transforms.

## Empty Nursery Bed item

- [ ] Confirm a newly crafted Nursery Bed shows only its timber frame,
      propagation medium, and divider in GUI, first-person, third-person,
      ground, and fixed/item-frame views.
- [ ] Confirm no cultivar, rootstock, shoot, cutting, or foliage appears in the
      empty item.
- [ ] Place and populate Nursery Beds and confirm all existing families and
      growth stages still render and behave as before.

Broader ordinary-gameplay, real two-player, and genuine stable-1.3.1 copy-upgrade
acceptance remain separate release gates.
