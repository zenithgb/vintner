# Vintner 1.4.0 advancement tutorial and discoverability audit

Audit date: 2026-09-13

Candidate: `0c030807107583ad59e36d3c8c01fb67b0b77862`

Branch: `feat/1.4.0-vineyard-management`

Version: `1.4.0`

Scope: read-only product and resource audit; no advancement or release-candidate changes

## 1. Executive verdict

**Verdict: HEALTHY — TUTORIAL EXPANSION RECOMMENDED POST-1.4.**

The visible advancement tree is a sound milestone backbone for making a first wine. It is not yet sufficient as Vintner's primary guided tutorial. A player who already knows that presses, fermentation barrels and ageing barrels exist can follow the tree from a cutting to aged wine. A player who knows nothing about Vintner encounters several unbridged transitions: no harvest step, no direction to build the three machines, no compatible-batch instruction, no propagation branch, and no estate progression at all.

This does not justify reopening the 1.4.0 candidate. Recipes unlock in Survival, cutting acquisition has three world-facing routes, the implemented interactions provide feedback, and Commons contains accurate explanatory articles. The gap is sequencing and signposting rather than a broken or inaccessible gameplay loop. The one materially misleading advancement is `choose_aging_style`: it claims the player refitted a barrel but completes when a treatment kit is merely crafted. Correct that trigger during the post-1.4 tutorial pass.

Headline counts:

- 196 advancement JSON resources total;
- 14 visible Vintner progression advancements;
- 182 non-display recipe-unlock helpers;
- 9 classified as Tutorial, 2 as Progression milestone, 2 as Mastery/challenge, and 1 as Flavor;
- 11 of the 14 are tutorial-relevant when Tutorial and Progression milestone entries are counted together.

## 2. Existing advancement inventory

All 14 display advancements are non-hidden and appear in the Vintner tab. `task` is the default frame where no frame is declared. “Any” means one criterion from the single requirement group; “all” means every listed requirement group must complete.

| ID | Title | Description | Parent | Trigger / criteria | Requirement | Reward | Frame | Intended action or concept | Classification |
|---|---|---|---|---|---|---|---|---|---|
| `vintner:vintner/root` | A New Vintage | Acquire a red or white grape cutting and begin your vineyard | none | `inventory_changed`: red cutting or white cutting | any colour | none | task | Enter Vintner through a starter cutting | Tutorial |
| `vintner:vintner/craft_trellis` | Lay the Groundwork | Craft a trellis for your first vine | root | `recipe_crafted`: any of 12 trellis recipes | any wood family | none | task | Obtain the support needed to plant | Tutorial |
| `vintner:vintner/plant_grapevine` | Put Down Roots | Plant a grapevine on a trellis | craft_trellis | server award after planting a red or white vine | either colour | none | task | Establish a vine correctly | Tutorial |
| `vintner:vintner/survey_vineyard` | Read the Land | Survey a vineyard site with a Soil Probe or the Vintner's Almanac | craft_trellis | server award after a valid Soil Probe survey or Almanac land/vine survey | one survey | 25 XP | task | Introduce live site inspection | Tutorial |
| `vintner:vintner/press_must` | First Pressing | Press grapes into fresh must | plant_grapevine | server award after a successful red or white press operation | either colour | none | task | Complete pressing | Tutorial |
| `vintner:vintner/ferment_wine` | Worth the Wait | Ferment must into a bottle of wine | press_must | server award when taking finished red or white wine from a fermentation barrel | either colour | none | task | Complete fermentation | Tutorial |
| `vintner:vintner/age_wine` | Cellar Master | Finish ageing a bottle of wine | ferment_wine | server award when taking finished aged red or white wine from an ageing barrel | either colour | 50 XP | goal | Complete the basic production loop | Progression milestone |
| `vintner:vintner/proper_pour` | A Proper Pour | Enjoy one serving from a tasting service | ferment_wine | server award after drinking one service cup | one serving | 15 XP | task | Introduce reusable shared serving | Tutorial |
| `vintner:vintner/to_good_company` | To Good Company | Share one bottle among four different guests | proper_pour | server award after the four-participant service condition | one shared bottle | 25 XP | task | Reward social serving | Flavor |
| `vintner:vintner/build_cellar` | Lay Down a Vintage | Craft a wine rack for bottle ageing and cellar storage | age_wine | `recipe_crafted`: any of 12 rack recipes | any wood family | 25 XP | task | Introduce passive storage and cellar building | Tutorial |
| `vintner:vintner/ideal_cellar` | Perfect Conditions | Inspect a sheltered, cool, dark cellar with water nearby and no heat | build_cellar | server award when an Almanac inspection rates a rack or crate IDEAL | one ideal inspection | 50 XP | goal | Reward cellar-environment mastery | Mastery/challenge |
| `vintner:vintner/inspect_wine` | Know Your Vintage | Use the Vintner's Almanac to inspect a bottle of wine | age_wine | server award after a successful carried or placed wine inspection | one inspection | 25 XP | task | Introduce the live bottle report | Tutorial |
| `vintner:vintner/choose_aging_style` | Choose an Aging Style | Refit an Aging Barrel for bold, gentle, or bulk maturation | age_wine | `recipe_crafted`: toasting, seasoning, or cask-conversion kit | any kit | 25 XP | task | Introduce specialist ageing choices | Progression milestone |
| `vintner:vintner/master_cooper` | Master Cooper | Craft a Cooper's Mallet and all three treatment kits | choose_aging_style | `recipe_crafted`: mallet plus all three kits | all four crafts | 75 XP | challenge | Reward full cooperage collection | Mastery/challenge |

There are no hidden display advancements and no redundant/unclear display entries. `choose_aging_style` has a clear purpose but a mismatched completion trigger, discussed below.

### Non-display recipe advancement inventory

The remaining 182 advancement files are recipe-book unlock helpers under `vintner:recipes/vintner/*`. They have no `display`, title, description, icon, frame or hidden flag, so they never appear in the Vintner progression tree. Every one is parented to `minecraft:recipes/root`, uses an `inventory_changed` material criterion plus `recipe_unlocked`, treats those criteria as alternatives, and rewards its same-named recipe. They are recipe discoverability infrastructure, not tutorial advancements.

The inventory is complete as follows:

- 12 recipe advancements for each of 14 wood-family groups: ageing barrel, barrel stand, Estate Management Desk, fermentation barrel, grape press, Labelled Cellar Shelf, Surveyor's Map Table, Tasting Cabinet, Tasting Service, trellis, Vintage Archive, Wine Basket, Wine Crate and Wine Rack (168 total). Each group covers the 12 supported wood appearances, using the relevant base ID where the oak or dark-oak form is the base recipe.
- 14 specialist recipe advancements: `cask_conversion_kit`, `compost`, `coopers_mallet`, `grafting_knife`, `grape_bowl`, `nursery_bed`, `resistant_rootstock_cutting`, `rootstock_cutting_from_red`, `rootstock_cutting_from_white`, `seasoning_kit`, `soil_probe`, `toasting_kit`, `vineyard_netting`, and `vintner_almanac`.

This accounts for all 196 advancement resources: 14 display plus 182 recipe helpers.

## 3. Current progression graph

The actual visible graph is acyclic and structurally simple:

```text
A New Vintage (obtain either cutting)
└─ Lay the Groundwork (craft any trellis)
   ├─ Put Down Roots (plant a vine)
   │  └─ First Pressing (successfully press)
   │     └─ Worth the Wait (collect fermented wine)
   │        ├─ Cellar Master (collect barrel-aged wine)
   │        │  ├─ Lay Down a Vintage (craft a rack)
   │        │  │  └─ Perfect Conditions (inspect an ideal rack/crate)
   │        │  ├─ Know Your Vintage (inspect wine)
   │        │  └─ Choose an Aging Style (craft any treatment kit)
   │        │     └─ Master Cooper (craft mallet and every kit)
   │        └─ A Proper Pour (drink from a tasting service)
   │           └─ To Good Company (four distinct guests)
   └─ Read the Land (perform a site survey)
```

The graph has no cycle, invalid parent, structurally forced crossing or advanced branch blocking the first-wine trunk. Its weakness is conceptual compression: the trunk skips harvest and all machine-acquisition steps, then ends at ageing without connecting to the major 1.4 estate progression. The optional cellar and serving branches are sensible. Surveying appears only after crafting a trellis, even though it is most useful before committing to a site or cultivar.

## 4. Core progression gaps

| Meaningful survival step | Existing coverage | Finding |
|---|---|---|
| Obtain a cutting | DIRECTLY GUIDED | Root triggers on possession; farmer, wandering-trader and village-chest routes are valid. |
| Obtain trellis materials | INDIRECTLY GUIDED | The child says to craft a trellis and the recipe book can expose recipes; materials and two-block construction are not taught. |
| Prepare vineyard soil | NOT COVERED | Optional, correctly non-blocking, but not signposted from the tree. |
| Plant on a two-block trellis | DIRECTLY GUIDED | Actual planting is verified, but “lower section of a two-block trellis” lives in Commons rather than the advancement. |
| Grow a vine | MILESTONE ONLY | Planting and later pressing imply growth; time, upper canopy and winter protection are not signposted. |
| Harvest grapes | NOT COVERED | This is the largest missing action inside the core vineyard-to-wine trunk. |
| Build a Grape Press | INDIRECTLY GUIDED | Recipe unlock exists; the advancement names pressing but not the block to craft. |
| Form a compatible four-grape batch | NOT COVERED | Commons and runtime rejection explain it, but the advancement does not prepare a novice. |
| Press grapes | DIRECTLY GUIDED | Award occurs on successful press, a strong teaching trigger. |
| Bottle must with a glass bottle | NOT COVERED | The press advancement fires before output collection. |
| Build a fermentation barrel | INDIRECTLY GUIDED | Recipe unlock exists; the next advancement only describes the completed process. |
| Fill with four compatible must bottles | NOT COVERED | Commons explains capacity and compatibility; advancement does not. |
| Collect fermented wine | DIRECTLY GUIDED | Award occurs when finished wine is removed. |
| Build/fill an ageing barrel | INDIRECTLY GUIDED | Recipe unlock exists; “finish ageing” assumes the player knows the vessel and full-batch rule. |
| Collect aged wine | DIRECTLY GUIDED | Award occurs on actual output collection. |
| Drink wine directly / placed bottle | NOT COVERED | Safe to leave outside the mandatory trunk. |
| Serve with Tasting Service | DIRECTLY GUIDED | Correct optional branch and actual consumption trigger. |
| Store in a rack | COVERED TOO LATE | Rack crafting is parented after ageing even though racks are a normal early storage/ageing option. |
| Inspect a bottle | COVERED TOO LATE | Almanac inspection works on fermented wine; the branch is hidden behind barrel ageing. |
| Create an ideal cellar | DIRECTLY GUIDED | Correct mastery branch once a player has an Almanac and rack/crate. |
| Catalogue a Vintage Archive | NOT COVERED | Major bridge from bottle history to estate registration is absent. |

The biggest “what next?” break is after planting: the player is never explicitly told to harvest the ripe upper canopy, craft a press, load four compatible grapes, press empty-handed, then collect must with a glass bottle. The subsequent fermentation and ageing descriptions also certify completion more than they guide setup.

## 5. Trigger-quality findings

### Strong teaching triggers

- `plant_grapevine` is awarded only after a valid cutting establishes a vine.
- `press_must` is awarded only after a successful press action.
- `ferment_wine` and `age_wine` are awarded when the player actually collects completed output.
- `survey_vineyard`, `inspect_wine`, `proper_pour`, `to_good_company` and `ideal_cellar` all verify the named interaction or state.
- `master_cooper` correctly behaves as an all-items collection challenge.

### Collection or timing limitations

- `root` is intentionally a collection trigger. This is appropriate for opening the tab, although it proves neither source discovery nor use.
- `craft_trellis` and `build_cellar` prove crafting, not placement/use. That is acceptable for their current wording, but `build_cellar` does not literally prove a cellar was built or a vintage was laid down.
- `press_must` fires when the press operation creates internal output, before a glass bottle collects the must. It therefore cannot teach the bottling transition by itself.
- `choose_aging_style` is the clear mismatch: its wording claims an ageing barrel was refitted, while any single treatment-kit craft completes it. The award can occur without owning a mallet, placing a barrel or applying the kit. A future teaching trigger should fire only after a successful treatment application.
- Impossible criteria are not inherently fragile here: each is explicitly awarded at a server-authoritative interaction. The advancement IDs and criterion names match the grant helper. They do, however, require continued regression coverage whenever interaction code changes.

## 6. Description-quality findings

Descriptions are concise and mostly truthful, but several are milestone labels rather than micro-tutorials.

| Advancement | Quality | Audit finding / future direction |
|---|---|---|
| A New Vintage | Good | Names the item and purpose. It need not enumerate every source. |
| Lay the Groundwork | Partial | Says what to craft but not that the planted structure must be two blocks high. Future: “Craft trellises and stack two to support your first vine.” |
| Put Down Roots | Partial | Correct object/action, but “Use a cutting on the lower block of a two-high trellis” would remove ambiguity. |
| Read the Land | Good | Names both valid tools and the action. Parent timing, not wording, is the larger issue. |
| First Pressing | Weak as instruction | Does not name the Grape Press, four compatible grapes, empty-hand press action, or glass-bottle collection. |
| Worth the Wait | Weak as instruction | Does not name the fermentation barrel, its four-bottle full-batch rule, or empty-hand collection. |
| Cellar Master | Weak as instruction | Does not name the ageing barrel, full compatible batch or collection action. |
| A Proper Pour | Good | Names the Tasting Service and one serving; the trigger matches. |
| To Good Company | Good | Clear challenge condition. |
| Lay Down a Vintage | Good for crafting | Correctly says craft a Wine Rack and why; title implies more than the trigger proves. |
| Perfect Conditions | Good | Concise state checklist and inspection verb. Runtime also checks stability/disturbance as part of the IDEAL rating, so this is guidance rather than an exhaustive predicate. |
| Know Your Vintage | Good | Names the tool, object and action. It could appear earlier. |
| Choose an Aging Style | Misleading trigger | Text describes the desired applied-barrel action accurately, but current completion only proves kit crafting. Keep the text and fix semantics later. |
| Master Cooper | Good | Exact collection challenge. |

## 7. Casual-player tutorial path

A viable casual route exists without advanced viticulture:

1. Find or buy either cutting.
2. Craft and stack two trellises; plant the lower trellis.
3. Let the vine grow and harvest its ripe upper canopy.
4. Craft a Grape Press; add four compatible grapes, press empty-handed, and bottle the must.
5. Fill a fermentation barrel with four compatible must bottles; collect finished wine.
6. Fill an ageing barrel with a compatible batch; collect aged wine.
7. Optionally inspect, store, place, drink or serve it.

Steps 1, 2, planting, pressing outcome, fermentation outcome and ageing outcome are represented. Harvesting, all three machine setup rules and must collection are not. Consequently, the advancement tab works as a checklist for a player who has already consulted recipes or Commons, but not as a self-contained “what next?” tutorial for a completely new player.

This remains appropriately cozy: soil optimization, threats, management modes, propagation, rootstocks, ideal cellars and estates are not required to make a first bottle.

## 8. Optional-depth branches

| System | Recommended role | Current advancement coverage |
|---|---|---|
| Site survey and cultivar fit | Early optional viticulture branch | Survey exists; cultivar comparison is Commons/Almanac only. |
| Compost, irrigation, protection, netting and threats | Optional knowledge, not trunk gates | No display advancement. Accurate Commons/Almanac advice exists. |
| Balanced / Quality Focus / High Yield modes | Optional mastery | No display advancement. Do not require for first wine. |
| Nursery propagation | Optional sustainability branch | Recipe unlock only. |
| Grafting and rootstocks | Optional advanced viticulture | Recipe unlocks only. |
| Specialist ageing treatments | Optional cellar progression | `choose_aging_style` plus `master_cooper`, with one trigger mismatch. |
| Ideal cellar design | Optional mastery | Well represented by `ideal_cellar`. |
| Social serving | Optional hospitality/flavor | Well represented by `proper_pour` and `to_good_company`. |

The absence of display advancements for threats and management modes is not itself a defect. Advancements should point toward a learning moment, while Commons and the Almanac explain mechanics and current state. Propagation deserves a small branch because it introduces new player actions and renewability, not because every vineyard modifier needs a toast.

## 9. Estate progression

Estate coverage is currently **NOT COVERED** across the visible tree:

| Estate step | Coverage | Current discoverability outside advancements |
|---|---|---|
| Craft/catalogue with a Vintage Archive | NOT COVERED | Recipe book, Commons archive article, README and block feedback |
| Found or name an estate with Almanac on Archive | NOT COVERED | Almanac tooltip/guide, Commons article and Archive messages |
| Register a two-corner vineyard plot | NOT COVERED | Almanac tooltip/guide and contextual messages |
| Build recognized facilities | NOT COVERED | Estate Management Desk reporting |
| Open Estate Management Desk | NOT COVERED | Recipe book and unregistered-screen guidance |
| Use Surveyor's Map Table / Estate Atlas | NOT COVERED | Desk UI, recipe book and live workstation behavior |
| Read ledger/reputation/market guidance | NOT COVERED | Desk and Vintage Archive surfaces |

This is the largest whole-system gap in 1.4's advancement representation. It does not block first wine, but it prevents the current tree from fulfilling the requested vineyard-to-wine-to-estate tutorial role. A minimal future estate branch should start from a recorded vintage, then teach estate founding, plot registration and the Desk/Atlas surfaces. Facilities, reputation ranks, ledger depth and markets should remain operational feedback or mastery, not mandatory toast chains.

## 10. Commons / Almanac / Desk boundaries

The implemented content follows the desired information hierarchy internally, but the advancement layer does not yet route players into it early enough.

- **Advancements — what next:** should supply the missing action sequence and branch entry points. It should not reproduce formulas, cultivar tables or every cellar modifier.
- **Commons — how it works:** nine current articles accurately cover introduction, grapevines, vineyard sites, pressing/must, fermentation, ageing/treatments, cellars, scoring and archives. They explain two-high trellises, harvest position, four-item compatible batches, full fermentation batches, ageing choices, site factors and estate/plot concepts.
- **Almanac — what is happening now:** correctly owns live vine, land, equipment, bottle, cellar, archive and plot readings plus operational estate/plot actions. The recipe requires a book plus both red and white grapes, so a one-colour starter cannot craft it immediately. The advancement tree first explicitly names bottle inspection only after ageing, which is late.
- **Estate Management Desk — how the estate performs:** correctly owns live estate, vineyard, cellar, market, ledger and map reporting. It should not be used to teach the basic wine loop, but an advancement should introduce it after estate registration.

Commons articles are accessible through the Commons Controls keybinding, while the Almanac is a separate live tool. A novice may not infer either route from the advancement tab. Future advancement descriptions should name Commons only sparingly; one early “learn more” cue or a clear root-toast hint is preferable to repeated documentation links.

## 11. Survival discoverability

Survival progression is mechanically attainable and non-circular:

- Either starter cutting is available from farmers, wandering traders, or a 25% bonus pool on supported village-house chests; established vines later provide renewable cuttings through pruning.
- Trellis, press and fermentation recipes use ordinary early materials. The base ageing barrel needs dark-oak planks and iron, while supported wood variants provide the wider furniture set.
- Recipe helpers unlock all 182 recipes from relevant inventory materials and do not depend on completing the display tree.
- The Vintner's Almanac is craftable after obtaining both grape colours, and a level-three Winemaker also sells it. This makes it obtainable but potentially later than the first one-colour vineyard.
- Runtime action-bar/chat feedback reports incomplete or incompatible machine batches. This supports recovery after a failed attempt, but reactive error text is not a substitute for proactive tutorial sequencing.
- Advanced soil, nursery, grafting, netting, estate and workstation systems are optional and do not gate the casual first-bottle route.

Survival accessibility verdict: **healthy; discoverability is weaker than attainability**.

## 12. Advancement-volume assessment

Fourteen visible advancements is low for the breadth of Vintner 1.4.0, not excessive. The tree is readable and free of toast spam. The 182 recipe helpers are invisible and should not be counted as progression volume, though unlocking many furniture variants from common planks can make the recipe book noisy.

A focused expansion to approximately 22 visible advancements remains reasonable if branches are shallow and optional. Do not add one advancement per cultivar, threat, wood variant, cellar furniture item, facility, reputation rank or decorative object. Those would turn the tree into a catalogue and dilute the action spine.

## 13. Proposed minimal tutorial tree

This is a proposal only. No JSON, language, trigger or ordering resource was changed.

### Graph

```text
A New Vintage
├─ Read the Land ── Propagate a Cutting ── Graft a Vine
└─ Lay the Groundwork ── Put Down Roots ── First Harvest
   └─ First Pressing ── Worth the Wait ── Cellar Master
      ├─ A Proper Pour ── To Good Company
      ├─ Lay Down a Vintage ── Perfect Conditions
      ├─ Know Your Vintage
      ├─ Choose an Aging Style ── Master Cooper
      └─ Preserve a Vintage ── Found an Estate ── Register a Plot
         └─ Run the Estate ── Map the Estate
```

### Proposed additions

| Proposed ID / working name | Title | Short description | Parent | Intended teaching trigger | Class | Existing/new | Commons support | Priority |
|---|---|---|---|---|---|---|---|---|
| `vintner:vintner/harvest_grapes` | First Harvest | Harvest ripe grapes from the upper canopy with an empty hand. | plant_grapevine | actual successful vine harvest | Tutorial | New | Grapevines | Post-1.4 high |
| `vintner:vintner/propagate_cutting` | Grow the Nursery | Use a Nursery Bed to propagate a grape cutting. | survey_vineyard | collect propagated cuttings from a Nursery Bed | Tutorial | New | Grapevines | Post-1.4 medium |
| `vintner:vintner/graft_vine` | Strong Foundations | Use a Grafting Knife to prepare a cutting with rootstock. | propagate_cutting | successful grafting action | Progression milestone | New | Grapevines / vineyard sites | Post-1.4 medium |
| `vintner:vintner/catalogue_vintage` | Preserve a Vintage | Record a wine batch in a Vintage Archive. | age_wine | successful new/update Archive record | Tutorial | New | Vintages and Archives | Post-1.4 high |
| `vintner:vintner/found_estate` | Found an Estate | Use a Vintner's Almanac on a Vintage Archive to found your estate. | catalogue_vintage | successful estate registration | Progression milestone | New | Introduction / vintages | Post-1.4 high |
| `vintner:vintner/register_plot` | Mark the Rows | Sneak-use the Almanac at two opposite corners to register a vineyard plot. | found_estate | successful plot registration | Tutorial | New | Vineyard Sites | Post-1.4 high |
| `vintner:vintner/manage_estate` | Run the Estate | Open an Estate Management Desk to review your registered estate. | register_plot | successfully open the registered-owner Desk screen | Tutorial | New | Introduction; live state remains Desk-owned | Post-1.4 high |
| `vintner:vintner/map_estate` | Map the Estate | Use a connected Surveyor's Map Table to review vineyard plots in the Estate Atlas. | manage_estate | open/use the Desk Map view with a compatible map source | Progression milestone | New | Vineyard Sites; live maps remain Desk-owned | Post-1.4 medium |

### Proposed changes to existing entries

| Existing ID | Proposed audit-only change | Reason | Priority |
|---|---|---|---|
| `survey_vineyard` | Parent directly to root; use “Use a Soil Probe to inspect a potential vineyard site” as the concise core wording, with Almanac remaining an alternate trigger. | Surveying should inform site/cultivar choice before construction. | Post-1.4 medium |
| `craft_trellis` | Mention stacking two trellises to support a vine. | Removes the main planting-structure ambiguity. | Post-1.4 high |
| `plant_grapevine` | Mention using a cutting on the lower part of a two-high trellis. | Converts a milestone label into an executable instruction. | Post-1.4 high |
| `press_must` | Parent to `harvest_grapes`; name the Grape Press, four compatible grapes and empty-hand press action. Consider awarding only after bottled must is collected, or add that collection to the next instruction. | Bridges harvest, compatibility, pressing and collection. | Post-1.4 high |
| `ferment_wine` | Mention filling a fermentation barrel with four compatible must bottles and collecting with an empty hand when ready. | Teaches setup, not only the result. | Post-1.4 high |
| `age_wine` | Mention filling an ageing barrel with compatible wine and collecting it when ready. | Teaches the final mandatory vessel transition. | Post-1.4 high |
| `build_cellar` | Parent from `ferment_wine` rather than `age_wine`; keep it optional. | Racks are useful before specialist barrel completion and should not appear only afterward. | Post-1.4 medium |
| `inspect_wine` | Parent from `ferment_wine` rather than `age_wine`. | The Almanac can inspect ordinary finished wine; current placement is late. | Post-1.4 medium |
| `choose_aging_style` | Keep the description, but award on successful treatment application rather than kit crafting. | Aligns the trigger with the claimed action. | Post-1.4 high |

Recommended scope: **8 new visible advancements and 9 targeted changes to existing entries**, producing 22 visible advancements. Some wording/parent edits can be combined in one future implementation pass; no balance or mechanic redesign is implied.

## 14. Before-1.4 findings

No advancement change is recommended before publishing 1.4.0.

- No invalid resource, broken parent, inaccessible mandatory item or circular survival dependency was found.
- The basic first-wine loop is represented by real, server-authoritative milestones.
- Existing recipe unlocks, runtime feedback, Commons articles and Almanac/Desk surfaces provide fallback discovery.
- `choose_aging_style` is semantically inaccurate, but it is an optional post-ageing branch and does not misdirect or block the core loop.
- The complete absence of estate advancements is a product gap, not a release-candidate correctness failure.

Reopen the candidate only if manual acceptance independently proves that ordinary Survival players cannot discover or complete the basic production loop with the shipped recipe book and feedback. This static audit does not provide that evidence.

## 15. Post-1.4 recommendations

1. Implement the high-priority trunk wording plus `harvest_grapes` first; validate the tree with a fresh-world novice playtest.
2. Correct the `choose_aging_style` trigger to award on actual treatment application.
3. Add the Archive-to-estate branch: catalogue, found, register plot, open Desk, then optional Atlas.
4. Add the small propagation/grafting branch without gating first wine.
5. Add focused GameTests for every new server-awarded criterion and for alternate red/white paths.
6. Verify advancement layout visually at common GUI scales and in a fresh profile; JSON graph validity cannot establish readable placement.
7. Measure whether Commons access is discovered. If not, add one early signpost rather than duplicating article content in every advancement.

## 16. Long-term mastery opportunities

Potential later challenges, only if playtesting shows players want more goals:

- harvest one wine from each cultivar;
- complete a vintage with a target quality tier;
- produce wines through all three ageing treatments;
- maintain a protected or irrigated registered plot through a season;
- fill a Vintage Archive with distinct batches;
- reach an estate reputation tier;
- complete each recognized estate facility;
- host a broader tasting event.

These should reward mastery after the tutorial branch, not gate ordinary production. Avoid wood-variant collection challenges and one-toast-per-threat/cultivar proliferation.

## 17. Release recommendation

**Keep the current 1.4.0 candidate in final manual acceptance.** The advancement tree is healthy as a compact milestone system and insufficient as the sole primary tutorial. Schedule one post-1.4 advancement/discoverability pass built around the proposed 22-node visible tree, with the first priority being the missing harvest/machine instructions and the Archive-to-estate bridge.

The next roadmap decision is singular: **approve a post-1.4 tutorial-tree pass that adds the eight proposed nodes and corrects the nine existing placements, descriptions or trigger semantics without changing gameplay balance.**

## Validation evidence

- Verified branch, exact candidate HEAD, version and upstream divergence before inspection.
- Inspected all 196 advancement JSON resources, all 14 display definitions, their English localization, parent graph, requirements, rewards and server grant sites.
- Cross-checked relevant recipes, 182 recipe-unlock helpers, cutting loot/trades, machine interactions, Almanac behavior, Commons knowledge entries, estate scope and current player guidance.
- JSON/resource checks and graph-reference checks are recorded in the task handoff.
- Full GameTest suites and clean build were deliberately not rerun; this audit does not supersede the existing 190/190 standard and Serene baseline.
- No advancement, production, language, recipe, release or candidate artifact was modified. This audit file remains uncommitted.
