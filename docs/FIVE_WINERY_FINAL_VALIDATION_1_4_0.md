# Five player-built wineries: final integration evidence

This record applies to the `phase/1.4.0-five-winery-integration` branch. It
does not change the historical candidate records. The integration was committed
as `429a21fc636e2c158a0ac9b2598ed8e965424dfe`. Exact-client connection
remains unverified, so no replacement 1.4.0 candidate is frozen.

## Sources and runtime contracts

| Culture / piece | Immutable SHA-256 | Size | Runtime use |
|---|---|---:|---|
| Plains combined | `7914e27f3bce456181324a23c459c8159ce26c1b8d227d588f99b3b8986e8c94` | 17×11×11 source; 16×11×11 runtime | Existing Plains house-pool winery and vineyard; terrain-only margin removed and road connector raised |
| Desert combined | `7be4b217c7f987829e1b2a2eeabdc87bbf83054984daa2db56610eb1f712d805` | 13×10×13 | House-pool winery, tasting area, vineyard; one Jungle Grape Press |
| Savanna original combined | `60348dbd4b0a5c4443029af827e3f7806d1a22783f3b4f19bf5c29ce2bf1b80b` | 22×8×18 | Preserved for provenance; excluded from the house pool |
| Savanna winery | `7f50357028418cdd2599164fdf40d6ebbd625748832b7147f37128bcca1fa91a` | 15×8×18 | Post-jigsaw atomic pair; one Acacia Grape Press |
| Savanna field | `39733138bb05eed5f9b48e0028a0a2587b7b5bdefa147c5afd0b9cb4c5d2b799` | 7×3×18 | Paired field; zero workstations |
| Snowy combined | `1fdfc017cca2ea97318bb61b06296972ea3ebeef9208222f69430ff048e00549` | 13×16×10 | House-pool tavern, winery, vineyard; one Spruce Grape Press |
| Taiga original combined | `f3a924c609b69c36e9a0b627478585402fc2f7537eaadfc6b6f02ad91a9f2a9a` | 23×11×15 | Preserved for provenance; excluded from the house pool |
| Taiga winery | `8198a9905ceb011c55a7546087ca33ef74b6bdd19d74877d8504e8db189583fb` | 11×11×15 | Post-jigsaw atomic pair; one Spruce Grape Press |
| Taiga field | `b9ce1de6fdbf215960f310b59664e8b223111bab62eac98bc976ff69b95e45c3` | 11×3×13 | Paired field; zero workstations |

The four original combined exports are transformed deterministically only at
their technical connector. The four split runtime NBTs are byte-identical to
their maintained sources. The structure audit checks dimensions, identities,
forbidden blocks, block entities, press counts, and all four rotations.

## Placement and lake behavior

Savanna and Taiga append two ordinary serializable pool pieces after completed
jigsaw layout, before `StructureStart` construction. Both pieces are added only
after both candidate searches pass. The search has no static per-village state.
The 100-start tests covered both cultures, including pair serialization; a
24-start-per-culture parallel regression checks pair isolation.

`LakeFeatureVineyardProtectionMixin` intercepts only `LakeFeature.place`
block writes. If the exact destination cell already contains vineyard soil,
a grapevine, or a trellis, the lake write is declined. All other writes are
delegated to vanilla. A nearby vineyard outside the lake's actual writes does
not suppress the lake. Focused GameTests cover overlap, nearby negative control,
and ordinary terrain. This supersedes the broader whole-lake skip described in
the earlier lake diagnosis; that historical report is retained as investigation
evidence.

## Fresh natural sample, seed 1400

The saved disposable world is
`build/release-server-run/five-winery-natural-final-1400`. All listed villages
have distinct located coordinates and fully generated winery/field chunks. The
auditor compares each authored Vintner block and door against saved chunk data,
allowing only a supported trellis-to-vine growth transition.

| Culture | Villages | Features | Physically complete | Half pairs / duplicates | Pair distance min / median / max |
|---|---:|---:|---:|---:|---|
| Plains | 25 | 3 combined | 3/3 | 0 | n/a |
| Desert | 25 | 9 combined | 9/9 | 0 | n/a |
| Savanna | 25 | 13 pairs | 26/26 pieces | 0 | 15.3 / 44.0 / 121.2 blocks |
| Snowy | 25 | 3 combined | 3/3 | 0 | n/a |
| Taiga | 50 | 38 pairs | 76/76 pieces | 0 | 15.2 / 45.2 / 124.6 blocks |

No parsing or generation error appeared in the sample. The historical Taiga
location at `[-336, 6832]` was regenerated under the final lake write guard:
one pair, two complete pieces. The larger pair distances remain explicit manual
visual-review items.

## Automated checks

- Standard GameTests: 216/216 after the bounded parallel test.
- Serene Seasons GameTests: 216/216.
- Generator `--check`, village source/runtime audit, natural saved-world audit,
  advancement, release assets, Blockbench/model family, Nursery Bed, and serving
  contact audits passed.
- Separate build directory was used for a clean build, preserving ignored
  worlds and earlier validation logs.

Automated placement and workstation tests do not accept the visible buildings,
gameplay feel, real two-player behavior, or upgrade of a copy of a genuine
public 1.3.1 world. The exact-RC human checklist remains open.

## Exact artifact validation status

The clean build of committed `429a21f` produced a 1,628,753-byte JAR with
SHA-256 `02ce96df4421fc954db346de29b84fe18b689386476a6a76f03a8503481a3137`.
It is a validation artifact, not a new authoritative RC. An isolated Java 25,
Minecraft 26.3, Fabric Loader 0.19.5, Fabric API 0.161.0+26.3, and Commons
0.1.2 dedicated server started, reloaded, saved, stopped, and restarted. A
fresh Taiga village at `[1680, -1056]` had one complete winery and one complete
field both before and after restart. The same exact JAR loaded in an isolated
client and completed resource atlas initialization without a Vintner missing
model/texture error. The attempted quick-play client connection did not produce
a server join, so registry sync, visible generated winery, disconnect/reconnect,
and in-game Quit remain unverified. The launcher stopped the isolated client.

Evidence is preserved under
`/Users/zachariaheverson/Developer/Minecraft/vintner-release-evidence/1.4.0-five-winery-429a21f-20261001`.
The previous `9149278b...` RC remains authoritative.
