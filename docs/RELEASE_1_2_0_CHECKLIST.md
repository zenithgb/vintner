# Vintner 1.2.0 release checklist

`1.2.0 — Cellar and Identity` contains the completed Phase 3 scope only.
Later terroir, vineyard expansion, estate, trade, and village work remains on
its development branch and is not part of this release.

## Automated candidate gate

- [x] Clean build
- [x] Release asset audit
- [x] Full required GameTest suite (77 tests)
- [x] Fresh dedicated-server startup
- [x] Dedicated-server world save and clean shutdown
- [ ] Packaged JAR installed in a clean client profile

## Manual in-game gate

- [ ] Compare oak, chestnut, neutral, and large-cask ageing with one batch.
- [ ] Confirm the large cask waits for eight matching bottles.
- [ ] Confirm Labelled Cellar Shelves reject a second batch.
- [ ] Confirm Tasting Cabinets accept mixed red, white, young, and aged wine.
- [ ] Verify shelf and cabinet bottles fill one position at a time for every
  facing and remain aligned side by side.
- [ ] Break filled shelves and cabinets in Survival and Creative; verify every
  bottle keeps its batch, number, vessel, age, estate, and style.
- [ ] Compare exposed, protected, heated, and machinery-disturbed storage.
- [ ] Inspect all six bottle-age stages and their Almanac advice.
- [ ] Check inventory icons, recipes, loot, rotation, collision, comparator
  output, glass dyeing, and all twelve wood variants.
- [ ] Upgrade an existing 1.1.0 world, save, reload, and unload/reload chunks.

## Publication gate

- [ ] Replace the release-candidate version with `1.2.0`.
- [ ] Date the `1.2.0` changelog section.
- [ ] Rebuild and record the final JAR SHA-256.
- [ ] Upload the final JAR to Modrinth as a public Release for Minecraft 26.2,
  Fabric, client and server required on both sides, with Fabric API required.
- [ ] Tag `v1.2.0` only after the uploaded artifact is verified.
