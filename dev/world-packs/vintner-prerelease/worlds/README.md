# Save storage and preparation

No actual worlds are included. Canonical external root:
`/Users/zachariaheverson/Developer/Minecraft/vintner-qa-worlds/`

Planned paths:

- `dev/master/`: frozen prepared Dev save.
- `dev/runs/<run-id>/`: disposable Dev copies.
- `upgrade-1.3.1/master/`: frozen genuine stable save.
- `upgrade-1.3.1/runs/<candidate-run-id>/`: upgrade copies.
- `evidence/<run-id>/`: manifests, screenshots, logs, census and checksums.

Use separate launcher game directories for stable and candidate; never share their saves directories. Install copies of selected saves into those instances, preserving canonical masters outside them. Record both storage and active instance paths. Do not point a running game at a canonical master.

## Current candidate

Use Java 25 / Minecraft 26.2 / Loader 0.19.3 / API 0.158.0+26.2, current Vintner candidate and separate Commons 0.1.0. Record both JAR hashes. Commons source/artifact reference is in DEV_WORLD_VERSION.txt. Optional Serene Seasons runs use a separate copied instance and exact compatible version manifest, not an undocumented change to the base master. No Tilth/Reeve required.

The established Gradle development command is `./gradlew -PcommonsJar=/absolute/path/to/zenithgb-commons-0.1.0.jar runClient`. Its normal run directory can contain existing saves: do not use it to open personal worlds or the frozen stable master. Prefer an explicitly separate manual launcher profile. No launch is performed by this pack.

## Stable master provenance gate

Local tag v1.3.1 resolves to 8a55c91128c4789504687d5c41d53dd535a99c3f. Its metadata uses MC 26.2, Loader 0.19.3, API 0.155.2+26.2 and no Commons requirement. This is a useful source reference, **not independently verified public-release artifact provenance**.

Before creating the master, obtain the actual stable 1.3.1 release JAR and record release source, SHA-256 and matching dependency metadata. If reproducing from source, use a separate checkout of the verified release reference and its own toolchain; do not switch/reset this feature branch. Stop baseline preparation if provenance cannot be established. Do not substitute build/libs/vintner-1.3.1.jar from the current branch.

In the stable instance create a new world and a labelled census of each supported state:

- vines in growing, mature and regrowing states;
- active/completed pressing, fermentation and ageing machines;
- red/white/aged bottles with batch, vintage, provenance and quality;
- partially served bottles and Tasting Services;
- racks/crates/casks/shelves/cabinets and cellar-aged stock;
- representative player progression/advancements where useful.

Verify availability in stable Creative/tooltips first. Mark unsupported features N/A with evidence; never insert candidate-only blocks to fill gaps. Record coordinates, contents, counts, metadata and screenshots before closing. Freeze only after successful stable save/restart verification.

## Freeze and copy safely (macOS)

Close client and server cleanly first. Archive a completed master outside the active instance and record its checksum, for example:

```sh
master='/Users/zachariaheverson/Developer/Minecraft/vintner-qa-worlds/upgrade-1.3.1/master'
archive='/Users/zachariaheverson/Developer/Minecraft/vintner-qa-worlds/evidence/stable-master-frozen.zip'
if [ -d "$master" ] && [ ! -e "$archive" ]; then
  ditto -c -k --keepParent "$master" "$archive"
  shasum -a 256 "$archive"
else
  echo 'Stop: master missing or archive already exists.'
fi
```

Save the checksum beside the run manifest. Keep a backup of that archive. Change run ID to a new unique value for each copy:

```sh
master='/Users/zachariaheverson/Developer/Minecraft/vintner-qa-worlds/upgrade-1.3.1/master'
destination='/Users/zachariaheverson/Developer/Minecraft/vintner-qa-worlds/upgrade-1.3.1/runs/candidate-001'
if [ -d "$master" ] && [ ! -e "$destination" ]; then
  ditto "$master" "$destination"
else
  echo 'Stop: master missing or destination already exists.'
fi
```

These procedures are manual examples, not executed preparation. Never overwrite an existing destination. Install a copy into the candidate instance's saves folder and record that path. Upgrade only that copy; retain failed copies and logs. Verify the archived master checksum before the next migration run. Reuse the same copy discipline for Dev resets. Never downgrade an upgraded copy or copy it back over a master.
