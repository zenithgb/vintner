# Vintner Release Schedule

Last updated: September 2, 2026

This schedule replaces the original `0.x` proposal in the full development
roadmap. Vintner is already publicly released at `1.0.1`, so future feature
updates use stable minor versions. Patch versions are reserved for fixes.

Dates are targets, not promises. Features may move to a later release rather
than weakening a release's stability.

## Release policy

- Public Modrinth versions are marked **Release**, not Alpha or Beta.
- New features ship in minor versions such as `1.1.0`.
- Fix-only updates ship in patch versions such as `1.1.1`.
- Each feature release freezes on a Monday and releases on the following
  Friday after a dedicated QA week.
- An unlisted `-rc.1` build may be used during QA when external testing is
  useful.
- Critical fixes may ship immediately. Normal fixes are grouped into the next
  Friday patch.

## Release train

| Version | Release | Development window | Freeze and QA | Roadmap scope |
| --- | --- | --- | --- | --- |
| **1.1.0 — Useful First Vintage** | **August 7, 2026** | July 23–August 2 | August 3–6 | Complete Phase 2: wine profiles, consumption limits, first food pairings, survival recipe audit, advancement coverage, save/reload verification |
| **1.2.0 — Cellar and Identity** | **October 2, 2026** | August 10–September 20 | September 21–October 1 | Phase 3: deeper batch identity, tasting profiles, bottle ageing, cellar conditions, wine racks and storage |
| **1.3.0 — Wine at the Table** | **September 2, 2026** | August 14–27 | August 28–September 2 | Placeable wine bottles, persistent partial servings, shareable Tasting Services, unified bottle presentation, and multiplayer-safe serving |
| **1.4.0 — Vineyard Expansion** | **February 12, 2027** | December 7–January 31 | February 1–11 | Phase 5: more varieties, vine age, propagation, grafting, threats and yield management |
| **1.5.0 — Table and Pantry** | **April 16, 2027** | February 15–April 4 | April 5–15 | Phase 6: expanded benefits, cooking, meal pairings, Farmer's Delight integration, feasts and by-products |
| **1.6.0 — Estates** | **June 25, 2027** | April 19–June 13 | June 14–24 | Phase 7: estate registration, plots, ledger, upgrades, reputation and labels |
| **1.7.0 — Trade** | **September 3, 2027** | June 28–August 22 | August 23–September 2 | Phase 8: pricing, buyers, regional demand, contracts, routes and market variation |
| **1.8.0 — World and Exploration** | **November 12, 2027** | September 6–October 31 | November 1–11 | Phase 9: wild grapes, abandoned vineyards, winery structures, rare cuttings and exploration rewards |
| **1.9.0 — Settlements** | **February 4, 2028** | November 15–January 23 | January 24–February 3 | Phase 10: optional Modern Settlements integration, vineyard professions, construction, production, caravans and cultural wines |
| **2.0.0 — Complete Vineyard Experience** | **May 12, 2028** | February 7–April 30 | May 1–11 | Stable vineyard-to-estate progression, compatibility programme, multiplayer hardening, accessibility, balancing, documentation and visual/audio polish |
| **2.1.0 — Advanced Winemaking** | **August 18, 2028** | May 15–August 6 | August 7–17 | Phase 11: rosé, sparkling, dessert, fortified and orange wines, brandy and vinegar |
| **2.2.0 — Master Vintner** | **November 17, 2028** | August 21–November 5 | November 6–16 | Phases 12–13: blending, yeast, advanced cellars, mastery, tastings, judging, festivals and presentation |
| **2.3.0 — Great Estates** | **February 23, 2029** | November 20–February 11 | February 12–22 | Phase 14 and remaining Phase 15–16 work: automation, logistics, workers, management tools, integrations and server controls |

## Required release gate

Every feature release must pass all of the following before publication:

1. Clean build and full automated game-test suite.
2. Client visual QA of every new block, item, screen and tooltip.
3. Dedicated-server startup and multiplayer smoke test.
4. Existing-world upgrade, save/reload and chunk-unload test.
5. Survival recipe and advancement audit.
6. Missing-model, missing-texture and language-key audit.
7. Changelog, screenshots, version metadata and Modrinth dependencies checked.
8. Release JAR installed into a clean profile and tested without development
   resources.

If a gate fails, the affected feature moves out of the release or the release
date moves. A known destructive save issue, crash, duplication exploit or
missing-asset problem always blocks release.

## Immediate timetable: 1.1.0

- **July 23:** Phase 2A implementation and manual QA complete.
- **July 24–29:** Add and test the first red/white food pairings.
- **July 30–August 2:** Recipe, advancement, save/reload and dedicated-server
  audit.
- **August 3:** Feature freeze; create `1.1.0-rc.1` if an external test build is
  useful.
- **August 3–6:** Regression testing, screenshots, changelog and Modrinth
  preparation.
- **August 7:** Publish Vintner `1.1.0`.
