# Vintner 1.3.1 release checklist

`1.3.1 — Cellar Crafting Patch` improves three cellar recipes and strengthens
permanent recipe validation without changing saved world data.

## Automated candidate gate

- [x] Regenerate all wood-family resources from the source generator
- [x] Validate all 138 Vintner recipes and matching unlock advancements
- [x] Confirm no duplicate or conflicting crafting inputs
- [x] Confirm every Vintner recipe output has an item definition
- [x] Clean build
- [x] Full required GameTest suite (95 tests)
- [x] Recipe unlocks for all eleven furniture families and twelve woods
- [x] Recipe unlocks for the Cooper's Mallet and all treatment kits
- [x] Fresh dedicated-server startup
- [x] Dedicated-server world creation, save, and clean shutdown

## Recipe acceptance

- [x] Labelled Cellar Shelves use paper instead of name tags
- [x] Cask Conversion Kits accept the vanilla planks tag
- [x] Barrel Stand recipes produce two stands
- [x] All twelve wood families retain their matching plank and slab ingredients

## Publication gate

- [x] Explicit patch-release approval received
- [x] Build the final `vintner-1.3.1.jar`
- [x] Record and verify the final SHA-256
- [x] Commit and push the final release metadata
- [ ] Tag the release commit as `v1.3.1`
- [ ] Publish the GitHub release and candidate JAR
- [ ] Upload the matching file to Modrinth
- [ ] Verify the public GitHub and Modrinth artifact checksums

Final candidate artifact:

- `vintner-1.3.1.jar`
- SHA-256: `8e6a260228ccbe6573b1d37180fb686b487fa01c5c2bb8af90801060c31e766d`
- Modrinth version: pending
- GitHub release: pending
