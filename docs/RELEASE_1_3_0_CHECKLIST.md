# Vintner 1.3.0 release checklist

`1.3.0 — Wine at the Table` makes wine a placeable, shareable four-serving
object while preserving the cellar and provenance systems introduced in
1.2.0. This checklist records evidence separately from manual acceptance.

## Automated candidate gate

- [x] Clean build
- [x] Release asset audit (1,135 JSON files)
- [x] Full required GameTest suite
- [x] Legacy bottles without serving data default to four servings
- [x] Bottle placement preserves metadata
- [x] Survival and Creative retrieval preserve metadata
- [x] Red, white, aged red, and aged white bottle data are supported
- [x] Every tasting removes exactly one serving
- [x] A competing final pour cannot duplicate the last serving
- [x] Final serving leaves one reusable empty bottle
- [x] Partial bottle save/reload preserves servings and provenance
- [x] Tasting-service servings use one-quarter bottle strength
- [x] Four tasting-service servings equal one full bottle effect
- [x] Direct partial-bottle drinking scales to remaining servings
- [x] Diminishing returns and impairment use shared consumption state
- [x] Existing racks, crates, shelves, cabinets, archives, and ageing tests pass
- [x] Fresh dedicated-server startup
- [x] Dedicated-server world creation, save, and clean shutdown
- [ ] Packaged JAR installed in a clean client profile

## Accepted visual gate

- [x] Canonical red and white placed-bottle models
- [x] Bottle labels and white-wine colour
- [x] Four-cup tasting-service layout
- [x] Red and white tasting liquid fill
- [x] Tasting liquid seams and clipping
- [x] Partial-serving visual states

## Manual in-game gate

- [ ] Load an existing 1.2.0 world and inspect old wine bottles
- [ ] Place old bottles and verify that each begins with four servings
- [ ] Pour 4, 3, 2, 1, and final servings from red, white, and aged bottles
- [ ] Confirm the bottle model and tasting service match each serving count
- [ ] Test pickup and breaking in Survival and Creative with a full inventory
- [ ] Unload and reload chunks containing full and partial bottles
- [ ] Save, quit, and reopen full and partial bottles and tasting services
- [ ] Test two real multiplayer clients targeting the final serving
- [ ] Verify bottle placement, pouring, drinking, emptying, and pickup sounds
- [ ] Verify the `A Proper Pour` advancement appears once
- [ ] Verify concise bottle tooltips and Almanac serving information
- [ ] Craft oak and non-oak tasting services in Survival
- [ ] Recheck racks, crates, labelled shelves, tasting cabinets, archives,
  ageing vessels, and cellar catch-up with partial bottles

## Release preparation gate

- [ ] Update `docs/RELEASE_SCHEDULE.md` so 1.3.0 is `Wine at the Table`
- [ ] Freeze the candidate commit
- [ ] Change release metadata only after explicit approval
- [ ] Build the final candidate JAR
- [ ] Install the JAR in a clean Minecraft profile
- [ ] Record and verify the final SHA-256
- [ ] Prepare changelog and screenshots
- [ ] Upload, tag, and publish only after explicit approval
