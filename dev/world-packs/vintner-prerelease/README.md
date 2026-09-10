# Vintner pre-release world pack

Specification v0.1; no world saves have been constructed or manually accepted. Start with [version metadata](DEV_WORLD_VERSION.txt), [inventory](inventory.md), [layout and construction](layout.md), and [world preparation](worlds/README.md).

This is reusable QA infrastructure for the 1.4 foundation while the development mod version remains **1.3.1**. The inspected branch is newer than the requested d008358 baseline: d256340, clean, origin comparison 0 behind / 16 ahead. Commons 0.1.0 is now required. Neither Tilth nor Reeve is required. Source identity, not the unchanged mod version alone, distinguishes the candidate from stable 1.3.1.

## Three separate worlds

1. **Dev World:** Creative, cheats enabled, labelled functional districts, reusable frozen fixture snapshot and disposable run copies. No resource gathering. Prepare slow growth/ageing fixtures in advance through normal gameplay; a newly constructed world is not yet a ready smoke pack.
2. **Upgrade World — stable 1.3.1 master:** genuine stable-release save, frozen before candidate use. Always upgrade a copy. Current feature-branch builds cannot create this baseline.
3. **Showcase World:** separate permanent marketing work. Not created, modified, or stored here.

No automated GameTest worlds belong in this pack. No generated saves, player data, logs or screenshots are committed here. Use the external paths in worlds/README.md.

## Use

- Construct and inventory fixtures once following layout.md; mark unavailable or unfinished fixtures PENDING.
- Freeze the completed Dev World with its exact mod manifest, then work on disposable copies.
- Run [release smoke](checklists/release-smoke.md), [multiplayer](checklists/multiplayer.md), [world upgrade](checklists/world-upgrade.md), and [visual QA](checklists/visual-qa.md).
- Record evidence using [the run record](evidence/README.md). An unchecked or unavailable check is not a pass.
- Rebuild only the failed district on a fresh copy where possible. Never repair a failed save before preserving it.

Existing Gradle runClient supports a Commons JAR property; build.gradle also defines automated GameTest and release-server directories. These are launch/test facilities, not safe world builders. Use a separate launcher instance/game directory for manual QA. No safe existing world-construction utility was identified; this pack deliberately adds no save generator or launcher script.

This documentation-only change requires reference/link checks and Git hygiene, not another GameTest/build campaign. Automated historical passes do not establish manual acceptance. No gameplay, save format, test, version, Almanac, Archives or Commons content changes are part of this pack.
