# Commons Integration B — Almanac and knowledge-surface audit

## Verdict and boundary

**Phase 6B PASS — audit only.** Recommend **retain specialist Almanac (strategy C)**,
with Commons supplying complementary static reference. Vintage Archives remain
Vintner-owned. No content, interface, access path, packet, gameplay or version is
changed. Phase 6C is not started; Phase 6 remains incomplete.

This is a source/resource audit of executable interaction paths, not a fresh
visual or runtime acceptance test. Source was sufficient to establish ownership;
no client/server launch or permanent instrumentation was necessary. Earlier Phase
6A runtime/GameTest results are not counted as new Phase 6B validation.

## Verified baseline

On 2026-09-08, Vintner at `/Users/zachariaheverson/Developer/Minecraft/vintner`
was clean on `feat/1.4.0-vineyard-management`, HEAD
`18719c8d0c47e1569cf2b6802db643ac46456f56`, origin
`https://github.com/zenithgb/vintner.git`, 0 behind / 11 ahead. Nothing staged or
unrelated was present. `gradle.properties` still declares version **1.3.1**.

`build.gradle` requires an explicit external `commonsJar` file via `implementation
files(...)`. Production metadata requires `zenithgb_library: 0.1.0-dev`.
`Vintner.onInitialize` calls `VintnerCommons.initialize`, registering
`vintner:vintner`, `Vintner`, with the runtime Fabric metadata version. Main and
client source sets remain split; the separate `gametest` source set/entrypoint is
not production content. Main/client entrypoints remain `Vintner` and
`client.VintnerClient`; data generation uses `client.VintnerDataGenerator`.

Commons at `/Users/zachariaheverson/Developer/Minecraft/zenithgb-commons` was clean
on `main`, HEAD `be3ae97b4db6a05c2718daec21186bbbb0e213f6`. It is read-only throughout.

## Evidence index / current source of truth

Paths below are relative to the repository. Method names identify the relevant
branches; localized prose is not a substitute for these production rules.

| Ref | Sources inspected and responsibility |
| --- | --- |
| A | `src/main/java/com/zenith/vintner/item/VintnerAlmanacItem.java`: `use`, `useOn`, `openGuide`, `openWineReport`, `inspectPlacedWine`, survey/plot helpers and tooltip |
| B | `wine/AlmanacInspection.java`: `classify`, `inspectLand`, `inspectGrapevine`, `inspectFermentation`, `inspectAgeing`; `item/AlmanacReport.java`: pagination/opening |
| C | `block/VintageArchiveBlock.java`: recording, `useAlmanac`, `registerEstate`, empty-hand ledger/summary; `block/entity/VintageArchiveBlockEntity.java`: `record`, `reportNext`, save/load |
| D | `estate/EstateDeskReport.java`, `EstateInfrastructureReport.java`, `VineyardPlotReport.java`; `block/EstateManagementDeskBlock.java`, `SurveyorsMapTableBlock.java` and their block entities |
| E | `src/client/java/com/zenith/vintner/client/VintnerClient.java`, `src/client/java/com/zenith/vintner/client/screen/EstateManagementDeskScreen.java`; `network/ModNetworking.java`, `AlmanacReportPayload.java`, `EstateDeskPayload.java`; `util/VintnerNotifications.java` |
| F | `block/WineRackBlock.java`, `WineCrateBlock.java`, `CellarCollectionBlock.java`, `WineBottleBlock.java`, `TastingServiceBlock.java`; corresponding persistent inventory entities; `item/SoilProbeItem.java` |
| G | `block/GrapePressBlock.java`, `FermentationBarrelBlock.java`, `AgingBarrelBlock.java` and entities; `wine/AgingVessel.java`, `CellarConditions.java`, `WineQualityProfile.java`, `WineMarketOutlook.java`, `WineMetadata.java` and report call sites |
| H | `vineyard/GrapeCultivar.java`, `SeasonalContext.java`, `VineyardManagementAdvice.java`, `VineyardProtection.java`, `VineyardIrrigation.java`, `VineyardThreat.java`, `TerroirEvaluator.java`; `registry/ModVillagers.java`, `ModTrades.java` |
| I | `item/WineItem.java`, `GrapeItem.java`, `MustItem.java`, `GrapeCuttingItem.java`, `GraftingKnifeItem.java`, `CoopersMalletItem.java`, `CompostItem.java`, `VineyardNettingItem.java`; localized names, feedback and tooltips |
| J | `src/main/resources/assets/vintner/lang/en_us.json`; `data/vintner/recipe/vintner_almanac.json`; recipe/advancement inventory; README's cultivation, winemaking, cellar, archive and village guidance |

Unless explicitly qualified, Java paths in B–I start at
`src/main/java/com/zenith/vintner/`. Commons reference inspection covered
`module/ZenithgbModule`, `ModuleRegistry`, `knowledge/KnowledgeEntry`, contribution
loading/presentation/transport, and client `KnowledgeAccess`, `KnowledgeScreen`
and synchronized state. Its article strings are literal title/body text, with
identifier-based categories/related entries/module conditions, not Vintner
translation Components or live gameplay queries.

## Current Almanac inventory

There is **no dedicated Almanac screen class, article registry, category catalogue,
tab system, unlock catalogue, search, or static reference JSON tree**. The client
receiver opens vanilla `BookViewScreen` with server-authored Components. Native
page navigation/closing is the shell; it is not the Estate Desk's six-tab screen.
`AlmanacReport.page` may split a logical section into multiple physical pages
using estimates of 18 characters/line and 13 lines/page. The payload codec caps
the page list at 32. Do not append a whole article library to this transport.

| Current Almanac feature / logical sections | Static reference? | Dynamic / world-player dependency? | Actions or side effects | Rendering/navigation | Commons fully replaces? / future direction |
| --- | --- | --- | --- | --- | --- |
| Air-use guide: inspect/wine instructions; estate/plot instructions | Yes, concise usage help | Guide selection depends on other-hand wine | Opens report | Native book pages | Only duplicate explanatory snippets could eventually retire; keep contextual help during transition |
| Guide: estate registered/unregistered status, plot count/area, desk hint | Mixed | Owner UUID, EstateSavedData, VineyardPlotSavedData | Reads current owner status | Native pages | No; retain, optional future Learn more |
| Guide: survey bookmark or no-corner help | Mixed | Item-stored dimension, position, climate, soil, score/rating and desired plot name | Reads custom item name/bookmark | Native pages | No; saved working state stays local |
| Wine identity/tasting/style/estate/provenance | Mostly dynamic | Other-hand WineItem metadata and legacy fallback | `ensureDefaults`; successful inspection grants advancement | Native pages | No; Commons explains metadata, never carries the bottle record |
| Wine quality/vintage conditions/age/bottle numbering | Dynamic with labels | Quality components, captured year/weather, bottle age/count | Same inspection lifecycle | Native pages | No; static scoring/vintage concepts complement it |
| Wine market/value/prestige/readiness | Dynamic | Bottle appraisal, producer reputation, local terroir/region and buyer model | Same report | Native pages | No; estimates remain specialist data |
| Aged wine vessel/storage/servings section | Mixed | Only aged red/white wine; actual vessel, storage history and servings | Same report | Native pages | Static vessel explanations may be reused editorially; live fields remain |
| Land/trellis/prepared-soil survey: terroir; season/weather/cultivation | Mixed | Clicked land, current calendar/weather, irrigation/protection | Grants survey advancement | Native pages | No; current location assessment stays local |
| Vine report: cultivar/ripeness/age; health/yield/rootstock/quality/advice | Mixed | Actual root/cultivar, growth, threats, terroir, management state | Grants survey advancement | Native pages | No; cultivar concepts and general tending guidance are Commons candidates |
| Fermentation: status, fill/progress/time | Dynamic | Actual barrel count/progress/readiness | Reads process | Native pages | No |
| Ageing: status, time, cellar conditions, mounting contribution | Dynamic | Vessel/barrel/world/infrastructure | Reads process | Native pages | No |
| Sneak-inspect ageing vessel: guide and crafting hint | Static selected by current vessel | Vessel determines which profile is explained | Adds reference section, does not treat barrel | Native pages | Deeper treatment article can replace duplicated long explanation only after proven parity |
| Containing owned plot appended to non-sneaking block inspection | Dynamic | Owner, dimension/bounds, live analysis, yield/quality/irrigation | Reads scan result | Native pages | No |
| Sneak-use two vineyard-site corners | Operational | Registered estate, item bookmark, same dimension, bounds/name/overlap/limit rules | Saves/clears bookmark, registers/updates plot, writes ledger, opens resulting report | World interaction plus pages/messages | Never replace with an article |
| Unsupported target / invalid state messages | Contextual static wording | Target determines failure | No-reading report | Native pages | Keep local; no separate article needed |

Additional block-owned Almanac branches are part of the access surface even though
they do not use `AlmanacInspection`: Archive record cycling/estate actions,
rack/crate cellar reports, collection selection, and placed wine/service reports.
`AlmanacInspection.classify` itself only selects fermentation, ageing, grapevine,
vineyard site or none. A press is not a hidden Almanac article category.

The guide is not gated by a knowledge unlock. Branches reflect current item/world
state; inspection/survey advancements are rewards, not permission checks for
articles. Custom Almanac names affect estate/plot naming. Default plot names and
some runtime labels are Java literals rather than translation keys.

## Complete surface classification

Disposition labels describe recommendations, not implemented links. In
particular, there is no currently approved cross-screen opening contract. A
Commons article can explain how to use a world block without opening it remotely.

| Surface / Topic | Current implementation | Static / Dynamic / Mixed | Current source of truth | Commons disposition | Vintner-retained responsibility | Migration phase | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Introduction/getting started | README, recipe/advancement progression, short guide | Static | A/J; actual block interactions | MIGRATE_TO_COMMONS | Contextual usage and progression | 6C | Author reference, not export of an existing article |
| Almanac guide help snippets | Air-use instruction sections | Mixed | A/J | REMAIN_VINTNER + RETIRE_AFTER_REPLACEMENT | State-aware guide and access | Later decision | Retirement applies only to redundant static prose, not item/tool |
| Almanac book shell | Component pages in vanilla book | Dynamic | B/E | REMAIN_VINTNER | Report pagination and navigation | Keep | No wholesale Commons redirect |
| Wine inspection | Identity, tasting, score, history, value | Dynamic | A/G, WineMetadata/appraisal | REMAIN_VINTNER + VINTNER_LINK_TO_COMMONS | Current item values | Keep; links later | No player/bottle state in articles |
| Grapevines | Live ripeness/health plus growing loop | Mixed | B/H; GrapevineBlock; J | MIGRATE_TO_COMMONS + REMAIN_VINTNER | Growth and live inspection | 6C concept | Keep local harvesting/tending feedback |
| Cultivars | Selected cultivar profile, trade roster | Mixed | GrapeCultivar.activeValues, ModTrades, B/J | MIGRATE_TO_COMMONS + REMAIN_VINTNER | Active/legacy identity, fit calculation | Later depth | Eight active cultivars, not every enum value |
| Vineyards | Land survey and general site practice | Mixed | B/H/J | MIGRATE_TO_COMMONS + REMAIN_VINTNER | Actual survey/world state | 6C site article | Not equivalent to registered plots |
| Soil/terroir | Soil Probe and Almanac location reports | Mixed | TerroirEvaluator/Messages, SoilProbeItem | MIGRATE_TO_COMMONS + REMAIN_VINTNER | Probe durability, site evaluation | 6C basics; later detail | No soil gameplay migration |
| Threats | Current health, event mitigation/advice | Mixed | GrapeQualityEvaluator; VineyardThreat/WeatherEvent; B/H | MIGRATE_TO_COMMONS + REMAIN_VINTNER | Threat generation and live recommendations | Later | Separate generic remedies from current diagnosis |
| Irrigation/netting/shelter | Checks, canopy item, contextual advice | Mixed | VineyardIrrigation/Protection; VineyardNettingItem; GrapevineBlock | MIGRATE_TO_COMMONS + REMAIN_VINTNER | Connectivity, mitigation, item actions | Later | Do not conflate roofs and bird netting |
| Pressing | Block action/status, four grapes per press | Mixed | GrapePressBlock/entity; WinemakingFeedback | MIGRATE_TO_COMMONS + REMAIN_VINTNER | Processing/inventory/batch identity | 6C | No separate press GUI/article today |
| Must | Intermediate items and compatibility rules | Mixed | MustItem, press/fermentation entities | MIGRATE_TO_COMMONS + REMAIN_VINTNER | Metadata/stack matching | 6C combined with pressing | Separate article not needed initially |
| Fermentation | Live barrel report and status messages | Mixed | FermentationBarrel/entity; B/G | MIGRATE_TO_COMMONS + REMAIN_VINTNER | Actual batch/progress/collection | 6C | General sequence only in Commons |
| Ageing/treatments | Live report, conditional static vessel guide | Mixed | AgingVessel; AgingBarrel; mallet/kit handling | MIGRATE_TO_COMMONS + REMAIN_VINTNER | Treatment actions/progress | 6C | Preserve cosmetic wood vs functional treatment distinction |
| Cellars/storage | Rack/crate/collection readings | Mixed | F/G; storage entities, CellarConditions | MIGRATE_TO_COMMONS + REMAIN_VINTNER | Inventory, aging catch-up, selection | 6C concepts | No storage UI replacement |
| Wine scoring | Numeric contribution report and quality labels | Mixed | WineQualityProfile; appraisal/tasting models | MIGRATE_TO_COMMONS + REMAIN_VINTNER | Actual score and derived effects/value | 6C | Score is clamped; don't imply unbounded sum |
| Vintages/provenance | Bottle and Archive snapshot identity | Mixed | WineMetadata/Provenance/VintageConditions; SeasonalContext | MIGRATE_TO_COMMONS + REMAIN_VINTNER | Generated year/batch/bottle history | 6C | Explanation is not a history viewer |
| Vintage Archive records | Persistent 16-slot snapshots and cursor | Dynamic | C; saved ItemStacks/SelectedIndex | REMAIN_VINTNER | Recording, update, cycle, persistence | Keep | Not static knowledge and not a dedicated screen |
| Archive explanatory cross-links | No current cross-screen route | Static | C plus future article | UNRESOLVED + COMMONS_LINK_TO_VINTNER + VINTNER_LINK_TO_COMMONS | World context and permissions | Later design | Start with textual use instructions; links are conditional proposals |
| Placeable bottles/serving | Bottle/service use and metadata feedback | Mixed | WineBottleBlock, TastingServiceBlock, A/I | MIGRATE_TO_COMMONS + REMAIN_VINTNER | Servings, consumption, placement, effects | Later | Small contextual tooltips stay |
| Estates | Archive founding/naming/crest, guide status | Mixed | A/C; EstateSavedData/Profile | MIGRATE_TO_COMMONS + REMAIN_VINTNER | Identity, ownership, writes | Later | Operational Almanac cannot retire |
| Vineyard plots/bookmark | Two-corner registration and report | Dynamic | A; VineyardSurveyRecord, VineyardPlotSavedData/Report | REMAIN_VINTNER + VINTNER_LINK_TO_COMMONS | Bounds, owner, name, survey, analysis | Keep; tutorial later | No Commons actions or live plots |
| Estate Desk overview | Owner-specific status and facilities | Dynamic | D/E; ledger/reputation/estate data | REMAIN_VINTNER | Current estate report | Keep | Opening can recognize facilities/sync reputation |
| Desk Vineyards/Cellar tabs | Loaded plot conditions, local infrastructure | Dynamic | D, VineyardPlotReport | REMAIN_VINTNER | Scans, availability and summaries | Keep | Unloaded data is unavailable, not zero measurements |
| Desk Markets | Local region/buyer plus instruction | Mixed | D; WineMarketRegion/Outlook; J | REMAIN_VINTNER + VINTNER_LINK_TO_COMMONS | Current geography/value guidance | Keep; concept later | No price/offer guarantee |
| Desk Ledger | Recent owner events | Dynamic | D; EstateLedgerSavedData | REMAIN_VINTNER | History, amounts, quality | Keep | Distinct from Archive block snapshots |
| Desk Map / Surveyor's Map Table | Vanilla map atlas, pan/zoom, plot overlays | Dynamic | D/E; map saved data and block inventories | REMAIN_VINTNER | World maps, item storage and selection | Keep | UI selection/pan/zoom is client-local, no new edit packet |
| Reputation/best vintage | Ledger synchronization, scores/facility bits | Dynamic | EstateReputationSavedData; ledger/report paths | REMAIN_VINTNER | Owner progression and persistence | Keep | General concepts can later be explained |
| Village roles | Winemaker/Cooper POIs and merchant offers | Mixed | ModVillagers, ModTrades, names/README | MIGRATE_TO_COMMONS + REMAIN_VINTNER | Profession AI, resident state, actual trades | Later | No dedicated Vintner resident dashboard found |
| Tooltips/status/error messages | Components and server system/overlay messages | Mixed | I/E/J; consuming block methods | REMAIN_VINTNER | Immediate instructions/feedback | Keep | Deeper articles complement, not replace, short help |
| Recipe book/advancements | Vanilla recipe JSON and progression | Mixed | J; ModAdvancements | REMAIN_VINTNER | Crafting/progression/unlocks | Keep | Commons can explain recipes, not own recipe transport |

## Vintage Archives decision and access inventory

**REMAIN_VINTNER.** `record` copies one wine ItemStack into one of 16 slots without
consuming the held bottle. An existing batch ID updates its snapshot; a new batch
uses an empty slot. The block entity saves the records and `SelectedIndex`.
`reportNext` sends batch/tasting/provenance/quality/age/readiness messages, advances
the cursor and calls `setChanged`. The cursor belongs to the world block, not to
each viewer. These are snapshots, not a live wine-storage inventory or a global
private archive for every player. Block drops preserve records.

Archive interactions also use the player's separate estate data: first Almanac
use can found an estate; custom names/banner branches select registration/update
instead of browsing. Empty-hand use reports capacity; sneak-empty-hand reports
the owner's ledger, best vintage and recent events. No Archive-specific custom
packet, screen class, article tab or browser button exists.

| Access | Actual production behavior | Recommendation |
| --- | --- | --- |
| Crafted Almanac (book + red grapes + white grapes) used in air | Other-hand WineItem selects report; otherwise guide/status/bookmark | Keep unchanged throughout first content phase |
| Almanac on world blocks | Block-specific handlers or inspection classification; sneaking changes relevant branches | Retain tool actions and live reports |
| Almanac on Archive | Found/update estate or cycle records, depending on state/name/crest | Never blindly redirect to Commons |
| Empty-hand Desk | Opens owner's report; sneak-use removes installed map/ledger accessory | Keep local; document distinction |
| Desk accessories / Map Table | Dye, books, maps and atlas storage alter the fixture; map-table empty-hand status/sneak removal | Keep all operational controls |
| Rack/crate/collection with Almanac | Messages about actual cellar/inventory; collection cycles selected bottle | Keep local |
| Placed bottle or tasting fixture inspection | `inspectPlacedWine` messages | Keep live servings/identity/readiness |
| Soil Probe on valid land | Site messages, durability cost and survey advancement | Keep tool; deeper soil article later |
| Commons binding | `key.zenithgb_library.open`, unbound by default; requires world and player; opens supplied snapshot | Coexist, configure in normal controls; Vintner adds no binding |

A future Vintages article can explain recording/reviewing Archives now in prose.
Clickable Commons→Archive needs a location/interaction/permission contract because
there is no Archive screen to open. Archive→Commons similarly needs a supported
entry-opening API and an appropriate message/widget affordance. Neither exists
as a consumer contract established by Phase 6A; do not construct Commons screens
directly or replace specialist state with static articles.

## Networking and state ownership

- `AlmanacReportPayload` (`vintner:almanac_report`) carries up to 32 Components.
  It mixes usage strings with dynamic reports. `AlmanacReport.open` sends only to
  the requesting player; `VintnerClient` opens native `BookViewScreen.BookAccess`.
  It remains Vintner-owned even if some explanatory paragraphs gain Commons peers.
- `EstateDeskPayload` (`vintner:estate_desk`) carries estate/subtitle, sections,
  map metadata, atlas flag and structured plot summaries (dimension/bounds/area,
  loaded flag and metrics). `EstateDeskReport.open` looks up the player's UUID,
  syncs ledger-derived reputation and records infrastructure before sending.
  The client has six tabs: Overview, Vineyards, Cellar, Markets, Ledger, Map;
  Done, text scrolling and map pan/zoom/plot selection are local presentation.
  No Vintner serverbound UI-action payload is registered in `ModNetworking`.
- Map imagery uses full vanilla `ClientboundMapItemDataPacket` updates because
  installed maps need not be carried. Map tables/fixture inventories persist in
  block entities and use normal Minecraft synchronization where applicable.
- Archive/placed wine/storage/probe/status feedback uses `VintnerNotifications`
  → server `sendSystemMessage`, sometimes overlay. There is no Archive packet
  waiting to be moved to Commons.
- Persistent owners/plots/ledger/reputation live in Vintner SavedData; bottle
  provenance, quality and survey bookmarks live in item data; process/inventory
  state lives in block entities. Current threats, market readings and seasonal
  data are computed from gameplay. All remain Vintner-owned.
- Commons-appropriate data is authored static title/body/category/related IDs.
  Commons' existing server filtering and synchronization can deliver that data;
  it must not acquire Vintner's live fields, operational callbacks or packets.

The Desk's unloaded plot numeric placeholders are transport details. Its screen
branches on `loaded` and shows unavailable data rather than treating placeholders
as measurements. The Almanac's local containing-plot scan is a different path;
do not generalize Desk loading/performance guarantees to it in an article.

## Localization and authoring constraint

Only `assets/vintner/lang/en_us.json` is currently present. Most report titles,
labels, help and messages are `Component.translatable` templates with runtime
arguments; player/estate names, plot names and a few formatting labels are literal.
The book report wraps by `Component.getString()` estimates on the server, while
the client ultimately renders Components. This is not a locale-independent article
layout system. No localization overhaul or visual-fit claim is made here.

Commons' current plain English strings cannot simply reference these keys or
interpolate current bottles/worlds. Phase 6C should author concise English JSON
from verified behavior, using existing English wording as editorial input, not
copy report templates with `%s` placeholders. Keep Vintner keys/tooltips for local
feedback. Record source provenance in a documentation matrix and review later
behavior changes against it; do not add a content generator or gameplay query API.

## Minimal category architecture and initial scope

Recommend four categories initially (ordering is a product recommendation, not
a claim about new sort fields in the Commons schema):

1. **Getting Started** — Introduction (6C).
2. **Viticulture** — Grapevines; Vineyard Sites (6C). Cultivars, Threats,
   Irrigation/Netting and deeper Soil/Terroir later.
3. **Winemaking** — Pressing and Must; Fermentation; Ageing and Treatments (6C).
4. **Cellaring & Quality** — Cellars; Wine Scoring; Vintages and Archives (6C).
   Placeable Bottles/Serving later.

Add **Estates & Trade** only when authoring its later articles: Estate Founding,
Plot Surveying, Reputation/Facilities, Winemaker/Cooper and Market Guidance.
All actual live reports, maps, inventories and controls stay Vintner-only.

Exactly nine proposed first entries, all under the existing `vintner:vintner`
module, with no new modules or optional specialist dependency:

| Proposed entry | Source / maturity | Why first / boundary |
| --- | --- | --- |
| Introduction | README, recipes/advancements, A; implemented loop | Orient player to reference vs specialist tools; no fictitious article unlocks |
| Grapevines | GrapevineBlock, cutting/compost items, B/H; implemented | Planting, tending and harvest; explain how to inspect rather than snapshot a vine |
| Vineyard Sites | TerroirEvaluator/Messages, SoilProbeItem, B/H; implemented | Basic soil/climate/site choice; distinguishes site survey from owned plot registration |
| Pressing and Must | Press block/entity, MustItem; implemented | Four matching grapes per press and bottle collection; explain metadata compatibility |
| Fermentation | Fermentation barrel/entity, B; implemented | Full four-bottle batch, 1,200 ticks at nominal 20 TPS, collection; no live countdown |
| Ageing and Treatments | AgingVessel, barrel/mallet/kit handlers; implemented | Full vessel, cosmetic wood vs treatment, shift-inspection; correct risk wording first |
| Cellars | CellarConditions and storage blocks/entities; implemented | Shelter/darkness/water/heat/disturbance and storage choices; no inventory duplication |
| Wine Scoring | WineQualityProfile, appraisal/report; implemented | Contribution stages and clamped 0–100 score; distinguish quality from appraisal |
| Vintages and Archives | WineMetadata/Provenance, SeasonalContext, C; implemented | Explain year/batch/bottle and recorded snapshots; textual Archive use, no link/action |

Other requested topics are not rejected: cultivar depth, threats, irrigation,
netting, serving, estates, plots, roles and markets are implemented but need a
separate, focused explanatory review. They are deferred to keep the first set
buildable, not because enum/resource presence alone establishes complete guides.
Must is covered with pressing; vintages with Archive concepts. Do not migrate
every translation key or add generic articles for short contextual errors.

## Current accuracy and completeness issues (not fixed)

1. **Shift-use omitted from a README instruction.** README asks players to use the
   Almanac directly on an ageing vessel to compare its role/recipe. In
   `AlmanacInspection.inspectAgeing`, the guide/crafting page is added only when
   `player.isShiftKeyDown()`. Phase 6C must state the shift condition explicitly.
2. **Season heading reuses a formatted value key.** `inspectLand` passes
   `message.vintner.almanac.season` as a heading without arguments; en_us defines
   `%s, Year %s | day %s of %s`. The actual season entry supplies four arguments.
   Source confirms the mismatch; visible rendering was not reproduced here.
   Do not copy that template as an article heading. A separate production-text
   correction would need its own authorization/validation.
3. **Ready-process helper text.** Fermentation/ageing inspection chooses progress
   only for full, not-ready vessels, otherwise `fill_to_start`. A ready vessel
   therefore pairs its ready status with fill-to-start guidance. This existing
   source-level presentation issue is not a reason to move the live UI to Commons.
4. **Risk terminology needs precision.** AgingVessel exposes oxygen/tannin/risk
   prose, but its quality contribution uses deterministic risk/style penalties.
   Do not describe an unverified random batch-spoilage simulation or imply each
   flavor descriptor is a separate simulated subsystem.
5. **Active vs legacy cultivars.** Only Ember Noir, Vale Pinot, Suncrest, River
   Garnet, Golden Vale, Frostling, Honeycrest and Stoneflower are active. Other enum
   identities are retained for compatibility. Do not publish the entire enum as
   the obtainable roster. Likewise ordinary wood appearance is not a treatment;
   legacy specialist blocks are not the recommended current acquisition route.
6. **Market estimates vs offers.** Bottle outlook/appraisal is computed separately
   from actual `ModTrades` merchant offers. Do not promise that an estimated value
   is the exact transaction offered by a particular villager.
7. **Missing reference depth.** The current short guide is usage help, not a
   complete introduction/cultivation/processing/scoring textbook. Static articles
   require new authoring; there is no existing category tree to translate wholesale.
   Threat management, rootstock/yield mode, facilities/reputation and map operations
   are more extensive than the guide's short instructions.
8. **Terminology/localization.** Player-facing “ageing” coexists with Aging class/
   item naming; `OAK` is the common functional profile even for other wood families.
   Choose clear visible terminology without renaming stored identifiers. The
   season/calendar has native and Serene Seasons paths; do not hard-code one global
   year length. Version remains 1.3.1 on this development branch; implemented
   expansion content is not evidence that a public 1.4.0 release occurred.

The inspected four-grapes press ratio, four-bottle fermentation capacity and
one-minute nominal fermentation match README. No confirmed planned-only feature
presented as current was found in the inspected player-facing instructions;
compatibility identifiers and deeper roadmap promises must still not become
article claims. This is a bounded content audit, not an exhaustive gameplay bug scan.

## Future links, unresolved decisions and roadmap recommendation

| Candidate | Kind | Boundary |
| --- | --- | --- |
| Vineyard Soil ↔ Tilth Soil Health | Knowledge-only conceptual comparison | Explain distinct soil systems; do not imply shared state |
| Vineyard preparation ↔ Tilth Compost | Knowledge-only initially | Shared input acceptance would require a separate gameplay contract |
| Agricultural inputs/cultivation ↔ Tilth crops | Conceptual relation, gameplay kept separate | No automatic crop/commodity/tag interoperability |

No Tilth IDs or behavior were verified in this phase, so these are topic proposals,
not approved entry identifiers. Later links must verify actual target articles and
use Commons' existing optional-reference/module-condition rules where appropriate.
They must not make Tilth a required dependency. Shared compost, crop, soil or
commodity APIs are wholly deferred.

Product decisions still needed: approve nine-article scope and visible terms;
choose future article order/IDs against the real schema; decide whether a supported
contextual Learn more API is desirable; decide whether redundant guide paragraphs
ever retire after parity and visual testing. Do not retire the specialist Almanac
item, survey bookmark, registration actions or report packet on the strength of
nine static articles. Cross-screen Archive links need a world-context design first.

**Ready for bounded Phase 6C authoring**, subject to roadmap approval of the nine
entries above and accurate wording of the listed issues. Keep all existing access
and specialist functionality; contribute only static English reference through
the existing Commons module. No links, localization overhaul or gameplay fixes
belong in that initial migration by default. Accept **Phase 6B only** now.

## Validation and changes

Only this audit and a cross-reference in `COMMONS_INTEGRATION_A.md` change.
Production Java/resources, build files, README and Commons are unchanged. No new
GameTests, runtime launches, clean build or release audit are warranted for this
documentation-only phase. Common, client and GameTest compilation passed using
the existing explicit Commons artifact:

```sh
./gradlew compileJava compileClientJava compileGametestJava --rerun-tasks \
  -PcommonsJar=/Users/zachariaheverson/Developer/Minecraft/zenithgb-commons/build/libs/zenithgb-commons-0.1.0-dev.jar \
  --console=plain
```

Evidence: `/tmp/vintner-phase6b-NfDJgZ/compile.log`, exit 0, four executed tasks.
The 59 existing mock-player removal warnings remain. This was compilation, not
a fresh GameTest execution. No historical test count is claimed as this phase's
result. A documentation check verified all 31 classification rows have eight
columns and only the allowed disposition labels. No Commons knowledge resources
exist under Vintner's production data path.

Meaningful inspection used `git status`, `rev-parse`, `remote -v`, `rg --files`,
targeted `rg`/`sed`/`cat`, and Python JSON reading. One exploratory search for
`HydrometerItem.java`/`CellarThermometerItem.java` exited 2 because neither class
exists: hydrometer/ageing report names are not evidence of standalone tools.
Some broad tool outputs were truncated and followed by narrower reads. Searches
with no matching screen-send call are absence evidence, not failed validation.
No package inspection or runtime screen inspection was performed in Phase 6B.

`git diff --check` passed; the only changes are the two intentional documentation
files, with Commons still clean and no production/build/resource modifications.
The scoped local documentation commit is `docs: audit Vintner knowledge surfaces for Commons`.
Do not push or begin Phase 6C. Search/indexing, icons, player-specific conditions,
Archive links, Tilth links, interoperability, Reeve and final visual polish remain
deferred.
