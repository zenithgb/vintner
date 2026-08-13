# Phase 3 acceptance — Vintage, Quality, and Ageing

This checklist maps the roadmap's Phase 3 milestones to the shipped gameplay
systems. Automated checks protect data, logic, recipes, and assets; the final
visual and feel checks remain an in-game release gate.

## Implemented scope

- Vintage identity: vineyard origin, estate, variety, year, style, batch,
  producer, quality, bottle age, storage history, bottle number, and tasting
  profile persist through pressing, fermentation, ageing, storage, and reload.
- Quality: a 0–100 production profile records vineyard, processing,
  fermentation, vessel ageing, and cellar contributions across all six tiers.
- Effects: quality changes benefit duration, signature-effect level, pairing
  strength, fault risk, ageing potential, estimated trade value, and prestige.
- Tasting: deterministic profiles draw fruit, floral, herbal, spice, earth,
  mineral, oak, acidity, tannin, body, and finish language from wine metadata.
- Vessels: oak, chestnut, neutral, and large-cask profiles have distinct
  capacity, speed, style affinity, quality contribution, and flavour.
- Bottle ageing: Young, Developing, Mature, Peak, Declining, and Spoiled stages
  respond to quality and accumulated cellar history.
- Cellars: shelter, depth, light, humidity, heat, temperature stability, sky
  exposure, and machinery disturbance affect gradual ageing quality.
- Storage: racks, stands, crates, labelled shelves, archives, and tasting
  cabinets are available, with all appropriate fixtures in twelve wood types.

## Manual release gate

1. Age the same red-wine batch in oak, chestnut, neutral, and large-cask
   vessels; confirm different timings and Almanac vessel/tasting reports.
2. Confirm the large cask waits for eight matching bottles before starting.
3. Put two bottles from one batch on a Labelled Cellar Shelf, then confirm a
   different batch is rejected.
4. Put mixed red, white, young, and aged bottles in a Tasting Cabinet; cycle
   them with the Almanac and retrieve them with an empty hand.
5. Confirm shelf and cabinet bottle models increase exactly one at a time and
   remain aligned for all facings and when placed side by side.
6. Break filled shelves and cabinets in Survival and Creative; verify all
   bottles drop with batch, bottle number, vessel, age, estate, and style intact.
7. Compare an exposed rack, a protected cellar rack, a rack beside heat, and a
   rack beside powered machinery; confirm the Almanac report and ageing differ.
8. Inspect bottles at all six age stages and confirm Hold, Drink now, Past peak,
   and Spoiled advice is sensible.
9. Verify every new block's inventory icon, recipe, loot, rotation, collision,
   comparator output, and all twelve wood variants.

Phase 3 is accepted only after the automated suite passes and this checklist
has been completed in the development client.
