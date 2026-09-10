# Dev World construction and numbered route

## Build order

1. Create a **new**, separate manual-QA world: Creative, cheats on, Minecraft/mod versions from the manifest. Use a normal Overworld for real terrain, climate and generated-village checks; record seed and world settings. Never open an existing personal world.
2. Choose hub X=0/Z=0. Record local ground height H; construct supported level grass/solid-floor pads with walking surface at H+1. Clear only this new world's fixture footprints. Road width four; label coordinates and district numbers with signs. Real slopes/biomes are field trips, not a claim that flat pads provide every terroir.
3. Lay out the pads below. Coordinates are X/Z inclusive planning rectangles; local terrain may require elevation work. Label the actual Y for every station. Keep roofs/cellar floors and stairs accessible.
4. Use Creative Vintner inventory, tooltips and normal interactions to stock chests and make states. Label cultivar/rootstock/mode/date/batch and before-values. Prepare mature, regrowing, older-vine, fermenting and aged examples in advance; do not fabricate timestamps or saved data.
5. Build estate/desk/Atlas as one cluster, cellar facility fixtures within report range. Add remote profession and stress districts only after the main loop works.
6. Complete a fixture census, save/quit cleanly and freeze a Dev master. Copy it for every destructive, persistence, multiplayer or stress session.

## Spatial plan

Hub (-8..8, -8..8). The main districts form two rows joined by straight roads. Districts 1–8 use 32 × 32 pads with 16-block gaps; districts 9–10 extend the shared estate cluster without gaps. Numbered signs and recorded Creative teleport destinations make remote areas quick to reach.

| # | District | X range | Z range |
|---|---|---|---|
| 1 | Grapes & Cultivars | 16..47 | 16..47 |
| 2 | Growth & Management | 64..95 | 16..47 |
| 3 | Nursery / Rootstocks / Grafting | 112..143 | 16..47 |
| 4 | Threats / Netting / Irrigation | 160..191 | 16..47 |
| 5 | Terroir & Surveying | 208..239 | 16..47 |
| 6 | Winemaking | 16..47 | 64..95 |
| 7 | Cellar & Storage | 64..95 | 64..95 |
| 8 | Estate / Plots / Facilities | 112..143 | 64..95 |
| 9 | Management Desk | 144..159 | 64..95 |
| 10 | Estate Atlas | 160..175 | 64..95 |
| 11 | Isolated village stations | 512..527 and 672..687 | 0..15 |
| 12 | Lifecycle outpost | 1024..1055 | 0..31 |
| 13 | Maximum estate | 2048..2187 | 2048..2187 |

Districts 8–10 are adjacent labels within a shared estate fixture cluster: put desk at (144,H+1,80), map table at (145,H+1,80), and facility fixtures X=136..150/Z=74..86, within ±8 vertically. These fixtures are additional to the display cellar in district 7. Put the controlled-cellar desk/facilities underground together if needed to obtain an actual ideal rating; record that Y. Keep map table adjacent at that elevation.

No free-roaming villagers in districts 1–10. Stations 11 are 160 blocks apart and over 250 blocks from main workstations; use enclosed supported floors and no unrelated POIs inside 64 blocks. Reserve generated-village exploration at least 4096 blocks from hub, and verify distance from stations before accepting a site. Do not assume a village exists at that coordinate.

## District specifications

Every “blocker” below blocks release acceptance for the affected supported behavior. Reset means a disposable copy or normal removal/reconstruction after preserving failure evidence.

| # | Purpose and required fixtures | Expected behavior | Reset | Blockers |
|---|---|---|---|---|
| 1 | Eight labelled cultivar rows, Creative cuttings/grapes, trellis variant board | Plant identity and harvest cultivar/colour remain correct | Replant from labelled stock | Missing cultivar, wrong output identity, broken asset |
| 2 | Growth, mature, harvested/regrowth and old-vine lanes; shears, three management modes | Natural growth/regrowth; intended yield/quality mode and age survive reload | Restore prepared copy; new plants are not old vines | Lost age/mode, impossible harvest, duplication |
| 3 | Nursery beds, both rootstock cuttings, grafting knife, own-root/adapted/resistant comparisons | Propagation/grafting preserves expected cultivar/rootstock and consumes inputs correctly | Refill/replant from census | Lost identity, wrong propagation, item duplication |
| 4 | Paired protected/unprotected vines; netting, compost/soil, isolated water at distances four/five; natural threat-condition bank | Probe/report reflects conditions and protection without claiming immunity | Rebuild paired lanes; wait for recorded condition | Protection/state loss or report inconsistent with valid conditions |
| 5 | Soil Probe, prepared/unprepared soil, real slope and biome field-trip waypoints; optional separate Serene copy | Site/climate/soil recommendations match observed context | Restore plots/waypoint notes | Wrong dimension/site, crash, misleading unavailable report |
| 6 | Presses, red/white grape batches, must, fermenters, ageing vessels/treatments, bottles/Tasting Services | Input→must→wine→aged wine retains batch provenance and serving accounting | Preserve failed machine, restore copy | Loss/duplication, incompatible batch mixing, broken processing |
| 7 | Racks/crates/stands/shelves/cabinets/archive; ideal vs exposed cellar; filled/partial bottles | Inventory, archive records and ageing persist; conditions differ appropriately | Restore labelled bottle census | Metadata loss, duplicated servings, lost storage |
| 8 | Named estate, small nonoverlapping plots, deliberately rejected overlap, facility cluster | Ownership/bounds correct; overlap rejected without ledger/reputation mutation; facilities recognised once | Use fresh pre-registration copy | Cross-owner state, mutation on rejected action, duplicate rewards |
| 9 | Desk, distinct plot harvest/quality and ledger data; loaded and remote plot | Report identity/counts match fixtures; remote unavailable metrics are not invented live zeroes; guidance read-only | Restore owner/state census | Wrong owner, stale report, state mutation merely from opening |
| 10 | Adjacent table, nine explored maps with recorded regions/scales | Maps insert/select and Atlas pan/zoom/overlays correspond to actual plots | Remove maps normally or restore copy | Lost maps, wrong overlays, broken controls |
| 11 | Separate adult unemployed villagers, grape press and barrel stand; later deliberate competition copy | Natural Winemaker/Cooper acquisition; trade UI and competition coherent | Fresh station copy, never force profession/memories | Valid isolated acquisition cannot complete; wrong workstation mapping |
| 12 | Remote vine, active machine, partial bottle and storage; remote plot if owner has capacity | Save/restart and genuine unload/reload preserve state | Disposable lifecycle copy | Corruption, lost/duplicated state |
| 13 | Sixteen max-size plots, populated vines, desk/table, owner census | Repeated reports/Atlas coherent without visible disruptive hitching | Dedicated pre-registration Dev copy | Missing plots, leakage, repeatable disruptive stall |

For station 11 place workstation at (520,H+1,8); begin the villager block-centred at (520.5,H+1,4.5), on an unobstructed floor. Distance to POI centre is sqrt(4²+0.5²) ≈ 4.031 blocks. Enclose a broad walking area without blocking the route, and leave no edge/platform gap. Second station uses X=680. Never move the villager or force its memories/profession after release. This starts outside assignment range and avoids deliberately placing it in the historical ~2.010-block tolerance mismatch; it is not proof that all manual navigation will succeed. Record time, movement and competing entities if acquisition fails.

## Maximum-estate copy

Use a fresh Dev copy **before any plots are registered** for that owner, or a genuinely distinct second owner with no plots. Normal route plots count toward the same 16-plot limit.

For row r and column c in 0..3, set each plot's minimum X=2048+36c, minimum Z=2048+36r, maximums=min+31. Register exact inclusive 32 × 32 bounds; four-block gaps separate plots. Label 01–16, record owner and Y bounds, plant representative rows in every plot with wet/dry and netted/unprotected comparisons. Record actual vine counts; an empty rectangle is not populated-load evidence.

Place desk/table near the centre; use explored maps for this area. Load the complete plot analysis footprints, including the 12-block margin, by visiting them and allowing chunks to settle; record render/server view/simulation settings. Do not assume distant means unloaded or visible means fully loaded. Capture loaded indicators before treating a result as a loaded-estate stress check.

Open/reopen desk ten times, inspect all 16 plot entries and Atlas, then repeat simultaneous openings with two independently owned estates where available. Record visible hitching, server warnings and observation settings. No new profiler or numerical performance acceptance claim is introduced. Gate H's historical synthetic median improvement is background only, not manual acceptance.
