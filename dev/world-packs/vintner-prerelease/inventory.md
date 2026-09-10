# Source-backed fixture inventory

All IDs below use the vintner namespace. This inventory is scoped to source commit in DEV_WORLD_VERSION.txt; recheck when refreshing. Obtain metadata-bearing variants from the current Creative inventory and normal gameplay, not invented NBT commands.

| System | Current fixtures and identities | Source |
|---|---|---|
| Cultivars | Ember Noir (ember_noir), Vale Pinot (vale_pinot), Suncrest (suncrest), River Garnet (river_garnet), Golden Vale (golden_vale), Frostling (frostling), Honeycrest (honeycrest), Stoneflower (stoneflower) | GrapeCultivar.activeValues; ModItems Creative entries |
| Grapes/vines | red_grapes, white_grapes; red_grape_cutting, white_grape_cutting; red_grapevine, white_grapevine | ModItems, ModBlocks, GrapevineBlock |
| Management | HIGH_YIELD, BALANCED, QUALITY_FOCUS; planted/growing/mature/harvested/regrowing and vine-age fixtures | VineYieldMode, GrapevineBlock, VineManagementSavedData |
| Propagation | nursery_bed, grafting_knife, rootstock_cutting, resistant_rootstock_cutting; OWN_ROOTS, ADAPTED, RESISTANT | ModItems, ModBlocks, VineRootstock |
| Soil/protection | vineyard_soil, compost, vineyard_netting; vanilla water channels | VineyardThreat, VineyardIrrigation, ModItems |
| Survey | soil_probe, surveyors_map_table; soil/climate/site/slope reports and recommendations | SoilProbeItem, SurveyorsMapTableBlock, EstateDeskReport |
| Winemaking | grape_press, red_must, white_must, fermentation_barrel, aging_barrel, chestnut_aging_barrel, neutral_aging_barrel, large_cask | ModBlocks, ModItems, AgingVessel |
| Cooperage | coopers_mallet, toasting_kit, seasoning_kit, cask_conversion_kit | ModItems |
| Wine | red_wine, white_wine, aged_red_wine, aged_white_wine; wine_bottle, tasting_service | ModItems, ModBlocks |
| Cellar | wine_rack, wine_crate, barrel_stand, labelled_cellar_shelf, tasting_cabinet, vintage_archive | ModBlocks, CellarConditions |
| Estate | estate_management_desk; named owners/plots, ledger/reputation/facilities; Atlas from nearby surveyors_map_table and explored vanilla maps | EstateDeskReport, VineyardPlotSavedData |
| Village | winemaker profession/POI uses grape presses; cooper profession/POI uses barrel stands | ModVillagers |
| Specialist access | vintner_almanac retained; Commons owns its own access and knowledge presentation | ModItems, VintnerCommons |

Cultivars are metadata variants of the red/white grape items and cuttings, **not eight separate registry item IDs**. Historical cultivar enum values outside activeValues are not current eight-cultivar fixtures.

WoodVariant supplies oak, spruce, birch, jungle, acacia, dark_oak, mangrove, cherry, pale_oak, bamboo, crimson and warped families. Build a labelled Creative-selected variant board for trellises, presses, barrels and furniture. Exact examples: oak_trellis, grape_press (oak), oak_aging_barrel; aging_barrel is the default dark-oak ageing barrel. Do not infer every ID from the same prefix rule. Cosmetic wood and functional ageing-vessel/treatment state are separate checks.

Threat names: HEALTHY, NUTRIENT_IMBALANCE, DROUGHT_STRESS, FROST_DAMAGE, HEAT_STRESS, MILDEW_RISK, ROT_RISK, PEST_PRESSURE, BIRD_PRESSURE. Inspect matching natural conditions; one assessment need not display every threat concurrently. Missing weather/season conditions mean PENDING, not protection proven.

Irrigation uses water at root height or one below, at nonzero horizontal Manhattan distance up to four blocks. There is no dedicated irrigation block in this inventory. Compare otherwise equivalent roots at distance four and five from an isolated channel; prevent other water from confounding the dry control.

Facilities scan within 16 blocks horizontally and eight vertically of the report location. Barrel Workshop requires two mounted ageing barrels; Controlled Cellar two ideal cellar stations; Warehouse four storage fixtures; Tasting Room a tasting cabinet and an archive. Keep fixtures near the desk and verify the reported recognition before freezing. Ideal-cellar construction needs actual environmental inspection, not an arbitrary decorative basement.

Plots cap at 16 per estate and 32 × 32 inclusive bounds. Atlas uses up to nine explored maps; place its table one block beside the desk (lookup covers horizontal offsets ±2, vertical ±1). Atlas is not a separately registered item.

Source roots: src/main/java/com/zenith/vintner/{registry,vineyard,estate,wine,block,item}; visible labels: src/main/resources/assets/vintner/lang/en_us.json. Existing in-game Almanac/tooltips supply current interaction instructions; retain screenshots of the inputs/results used rather than relying on old roadmap controls.
