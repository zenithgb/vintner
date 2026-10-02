# Five-winery vertical anchoring: validation record

This correction applies only to the current five-winery integration branch.
It does not change the immutable player-authored NBT sources, village weights,
paired search, lake protection, Commons, or the authoritative 1.4.0 candidate.

## Cause and correction

The combined Desert and Snowy templates had technical entrance connectors at
local Y=0 while their street contact needed to be one block higher relative to
the authored ground plane. The runtime generator now replaces a boundary block
at local Y=1, lowering each rigid jigsaw piece by one world block. Desert's
connector moves one block beside the authored door so the door and its rotated
facing remain player-authored. Plains retains its earlier Y=2 connector.

Post-jigsaw Savanna and Taiga pieces derive their origins from the first free
terrain height at a completed street edge. Their previous contact expression
placed the selected pieces above Zach's observed grade. The shared contact is
one block lower; the Taiga building gets one more block because its authored
walkable floor is above a deeper base. The paired search, rotation, collision,
and atomic commit order are unchanged.

Desert's short foundation gaps are supported after the template places in each
chunk. Only authored sandstone-family base cells of the Desert winery qualify.
One or two air cells are filled with the matching full sandstone material only
when solid dry terrain is immediately below; deeper caves, water, unrelated
blocks, and cells outside the piece/chunk are untouched. This follows the
small, buried-support principle visible in vanilla Desert house templates,
without terrain flattening or a rectangular platform.

## Measured vertical change

Saved structure starts from the same seed-1400 village coordinates were compared
before and after the correction. Every matched piece moved by the intended
amount:

| Culture | Matched pieces | New Y minus old Y |
| --- | ---: | ---: |
| Desert combined | 6 | -1 |
| Savanna winery / field | 13 / 13 | -1 / -1 |
| Snowy combined | 2 | -1 |
| Taiga winery / field | 21 / 21 | -2 / -1 |

Plains runtime and source are unchanged. The structure audit confirms all five
player-source hashes, deterministic combined runtime templates, split-piece
byte identity, presses, forbidden-block exclusions, and four rotations.

## Automated and natural evidence

Standard GameTests: 217/217 passed. Serene Seasons GameTests: 217/217 passed.
The new focused test covers flat sand, one- and two-block dips, a deeper void,
and a cell outside the Desert footprint. The paired 100-start tests check the
new contact expression as well as atomicity and serialization. Desert and
Snowy 100-start selection counts remain 18 and 22 respectively, matching the
prior committed validation.

Fresh saved seed-1400 world, 25 distinct village starts per affected culture:

| Culture | Features | Authored pieces complete | Half pairs / duplicates |
| --- | ---: | ---: | ---: |
| Desert | 6 combined | 6/6 | 0 |
| Savanna | 13 pairs | 26/26 | 0 |
| Snowy | 2 combined | 2/2 | 0 |
| Taiga | 21 pairs | 42/42 | 0 |

The Desert foundation audit found no base cell with air directly beneath it.
It counted 20 sandstone-family cells beneath authored foundation positions;
that count includes natural sandstone and added support. The field-soil audit
found 23 Savanna soil cells over air in one field near a deeper underlying
void. All 45 authored soil cells and all authored vines of that field are
present, but its underside and visible edge still require in-game inspection.
No Taiga field had soil directly over air in this sample. Pair separation was
unchanged for Savanna and remained within the established Taiga range.

The disposable natural-sampling server saved the audited world, then exhausted
its 4 GiB heap during shutdown after many widely separated forced chunks.
Re-reading the saved world after terminating that disposable process reproduced
the four passing physical-block audits. This shutdown is **not** a clean exact
server runtime pass; a smaller exact-JAR smoke run remains required.

Generator `--check`, village structure, advancement, release-resource,
Blockbench/model-family, Nursery Bed, and serving/contact audits passed.
Exterior, entrance, and field screenshots from the corrected exact JAR have
not yet been captured. Zach's visual assessment remains open, including the
Savanna void case and larger winery-field separations.

No new candidate is frozen. The earlier
`9149278b342a60f702f1069f11bdf37132069daf3a7b91ca4484cf45fb475ba6`
candidate remains authoritative until exact committed-JAR server and client
validation, followed by Zach's manual acceptance.

External logs, coordinate lists, saved-world audits, and comparison probes are
under `vintner-release-evidence/1.4.0-vertical-anchoring-20261001`.
