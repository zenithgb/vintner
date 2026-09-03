# Vintner 1.4.0 — Vineyard Expansion scope

Vintner 1.4.0 combines deeper vineyard simulation with the management tools
needed to understand it. The player still performs vineyard and cellar work in
the world; the management layer reports, organizes, and explains that work.

## Included vineyard systems

- Twelve named red and white cultivars carried through cutting, planting,
  grafting, harvest, pressing, fermentation, ageing, storage, and inspection.
- Persistent vine age with separate yield and quality trade-offs.
- Nursery Bed propagation, adapted and resistant rootstocks, and grafting.
- Balanced, Quality Focus, and High Yield vineyard management.
- Seasonal weather, deterministic vineyard threats, pest pressure, bird
  pressure, irrigation, protected cultivation, and vineyard netting.
- Terroir surveying, field instruments, cultivar recommendations, and
  contextual Almanac advice.
- Winemaker and Cooper professions, workshops, and generated village
  vineyards as the supporting world-facing vineyard layer.

## Included estate management

- Estate registration by using a Vintner's Almanac on a Vintage Archive, with
  optional anvil naming for a custom estate identity.
- Up to sixteen named 32 by 32 vineyard plots per estate.
- A bounded persistent estate ledger and reputation progression.
- Recognition of physical barrel workshop, controlled-cellar, warehouse,
  tasting-room, and improved-irrigation facilities.
- Read-only appraisal, buyer-fit, and regional-market guidance. No wine is sold
  and no currency or reward is created by this release.
- Estate Management Desks and Surveyor's Map Tables in all supported wood
  families.
- Six management tabs: Overview, Vineyards, Cellar, Markets, Ledger, and Map.
- An interactive Estate Atlas using up to nine compatible explored maps, with
  vineyard overlays, selection, panning, and zoom.

## Explicitly deferred

- Grape-press byproducts, pantry foods, wine cooking, Farmer's Delight
  integration, and shared feasts remain 1.5.0 work.
- Advanced estate facilities, hospitality, labels, awards, and deeper estate
  progression remain 1.6.0 work.
- Contract offers, correspondence boards, sales, negotiation, delivery,
  rewards, routes, and live market variation remain 1.7.0 work.
- Pumps, tanks, automated irrigation, workers, and logistics remain later
  automation work.

## Release gates

1. All asset generators are reproducible and the release-asset audit passes.
2. The complete GameTest suite passes with vineyard, estate, recipe, and 1.3
   serving regressions enabled together.
3. A dedicated server starts cleanly and a multiplayer ownership smoke test
   confirms that estate reports do not cross between players.
4. Existing worlds load safely; vine age, cultivar, rootstock, plot, ledger,
   reputation, desk, and atlas data survive save/reload and chunk unloading.
5. Every new recipe unlocks in Survival and every new item and block appears in
   the Vintner inventory tab.
6. Client QA covers every wood variant, all desk facings, each management tab,
   common GUI scales, overflowing plot lists, map selection, pan, and zoom.
7. The release JAR passes a clean-profile smoke test without development
   resources.
