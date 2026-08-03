package com.zenith.vintner.test;

import com.mojang.datafixers.util.Pair;
import com.zenith.vintner.block.AgingBarrelBlock;
import com.zenith.vintner.block.CellarGlassColor;
import com.zenith.vintner.block.CellarCollectionBlock;
import com.zenith.vintner.block.DeskBlotterColor;
import com.zenith.vintner.block.EstateManagementDeskBlock;
import com.zenith.vintner.block.FermentationBarrelBlock;
import com.zenith.vintner.block.GrapevineBlock;
import com.zenith.vintner.block.NurseryBedBlock;
import com.zenith.vintner.block.SurveyorsMapTableBlock;
import com.zenith.vintner.block.TrellisBlock;
import com.zenith.vintner.block.WineCrateBlock;
import com.zenith.vintner.block.WineRackBlock;
import com.zenith.vintner.block.WoodVariant;
import com.zenith.vintner.block.entity.AgingBarrelBlockEntity;
import com.zenith.vintner.block.entity.CellarCollectionBlockEntity;
import com.zenith.vintner.block.entity.FermentationBarrelBlockEntity;
import com.zenith.vintner.block.entity.EstateManagementDeskBlockEntity;
import com.zenith.vintner.block.entity.GrapePressBlockEntity;
import com.zenith.vintner.block.entity.SurveyorsMapTableBlockEntity;
import com.zenith.vintner.block.entity.VintageArchiveBlockEntity;
import com.zenith.vintner.block.entity.WineCrateBlockEntity;
import com.zenith.vintner.block.entity.WineRackBlockEntity;
import com.zenith.vintner.estate.EstateProfile;
import com.zenith.vintner.estate.EstateInfrastructureReport;
import com.zenith.vintner.estate.EstateLedgerEvent;
import com.zenith.vintner.estate.EstateLedgerSavedData;
import com.zenith.vintner.estate.EstateReputationProfile;
import com.zenith.vintner.estate.EstateReputationSavedData;
import com.zenith.vintner.estate.EstateReputationTier;
import com.zenith.vintner.estate.EstateSavedData;
import com.zenith.vintner.estate.LedgerEventType;
import com.zenith.vintner.estate.VineyardPlot;
import com.zenith.vintner.estate.VineyardPlotReport;
import com.zenith.vintner.estate.VineyardPlotSavedData;
import com.zenith.vintner.item.WineEffectProfile;
import com.zenith.vintner.item.GraftingKnifeItem;
import com.zenith.vintner.registry.ModAttachments;
import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.registry.ModBlocks;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.registry.ModGameRules;
import com.zenith.vintner.registry.ModTrades;
import com.zenith.vintner.registry.ModVillageStructures;
import com.zenith.vintner.registry.ModVillagers;
import com.zenith.vintner.wine.CellarConditions;
import com.zenith.vintner.wine.AlmanacInspection;
import com.zenith.vintner.wine.CellarRating;
import com.zenith.vintner.wine.AgingVessel;
import com.zenith.vintner.wine.GrapeQualityEvaluator;
import com.zenith.vintner.wine.WineConsumptionManager;
import com.zenith.vintner.wine.WineConsumptionState;
import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WinePairingManager;
import com.zenith.vintner.wine.WineProvenance;
import com.zenith.vintner.wine.WineQuality;
import com.zenith.vintner.wine.WineQualityProfile;
import com.zenith.vintner.wine.WineAgeStage;
import com.zenith.vintner.wine.WineAppraisal;
import com.zenith.vintner.wine.WineBuyerType;
import com.zenith.vintner.wine.WineMarketOutlook;
import com.zenith.vintner.wine.WineMarketRegion;
import com.zenith.vintner.wine.WineReadiness;
import com.zenith.vintner.wine.WineTastingProfile;
import com.zenith.vintner.wine.WineStyle;
import com.zenith.vintner.wine.WineVintageConditions;
import com.zenith.vintner.vineyard.ClimateProfile;
import com.zenith.vintner.vineyard.GrapeVariety;
import com.zenith.vintner.vineyard.GrapeCultivar;
import com.zenith.vintner.vineyard.GraftedCuttingData;
import com.zenith.vintner.vineyard.NurseryPlant;
import com.zenith.vintner.vineyard.SoilProfile;
import com.zenith.vintner.vineyard.SoilType;
import com.zenith.vintner.vineyard.TerrainProfile;
import com.zenith.vintner.vineyard.TerroirEvaluator;
import com.zenith.vintner.vineyard.TerroirReport;
import com.zenith.vintner.vineyard.VineyardSurveyRecord;
import com.zenith.vintner.vineyard.SeasonalContext;
import com.zenith.vintner.vineyard.VineyardSeason;
import com.zenith.vintner.vineyard.VineyardWeatherEvent;
import com.zenith.vintner.vineyard.VineyardProtection;
import com.zenith.vintner.vineyard.VineyardIrrigation;
import com.zenith.vintner.vineyard.VineyardManagementAdvice;
import com.zenith.vintner.vineyard.VineAgeSavedData;
import com.zenith.vintner.vineyard.VineAgeStage;
import com.zenith.vintner.vineyard.VineManagementSavedData;
import com.zenith.vintner.vineyard.VineRootstock;
import com.zenith.vintner.vineyard.VineYieldMode;
import com.zenith.vintner.vineyard.VineyardThreat;
import net.minecraft.advancements.AdvancementHolder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class VintnerGameTests {
    private static final BlockPos FIRST = new BlockPos(2, 1, 2);
    private static final BlockPos EAST = FIRST.east();
    private static final BlockPos UPPER = FIRST.above();

    @GameTest(maxTicks = 40)
    public void estateManagementDeskIsDirectionalAndRegistered(
            GameTestHelper helper
    ) {
        helper.assertTrue(
                ModBlocks.ESTATE_MANAGEMENT_DESK
                        instanceof EstateManagementDeskBlock,
                "The estate desk should use its interactive block type"
        );
        helper.assertTrue(
                ModBlocks.ESTATE_MANAGEMENT_DESK.asItem()
                        != Items.AIR,
                "The estate desk should have a registered block item"
        );
        helper.assertValueEqual(
                ModBlocks.ESTATE_MANAGEMENT_DESK.defaultBlockState()
                        .getValue(EstateManagementDeskBlock.FACING),
                Direction.NORTH,
                "The estate desk should have a stable default facing"
        );
        helper.assertValueEqual(
                ModBlocks.ESTATE_MANAGEMENT_DESK.defaultBlockState()
                        .getValue(
                                EstateManagementDeskBlock.BLOTTER_COLOR
                        ),
                DeskBlotterColor.GREEN,
                "The estate desk should begin with a green blotter"
        );
        helper.assertFalse(
                ModBlocks.ESTATE_MANAGEMENT_DESK.defaultBlockState()
                        .getValue(EstateManagementDeskBlock.HAS_LEDGER),
                "The estate desk should begin without an installed ledger"
        );
        helper.assertFalse(
                ModBlocks.ESTATE_MANAGEMENT_DESK.defaultBlockState()
                        .getValue(EstateManagementDeskBlock.HAS_MAP),
                "The estate desk should begin without an installed map"
        );
        helper.setBlock(
                FIRST,
                ModBlocks.ESTATE_MANAGEMENT_DESK.defaultBlockState()
                        .setValue(
                                EstateManagementDeskBlock.FACING,
                                Direction.EAST
                        )
        );
        helper.assertValueEqual(
                helper.getBlockState(FIRST).getValue(
                        EstateManagementDeskBlock.FACING
                ),
                Direction.EAST,
                "The estate desk should retain horizontal orientation"
        );
        helper.assertFalse(
                helper.getBlockState(FIRST)
                        .getShape(helper.getLevel(), helper.absolutePos(FIRST))
                        .isEmpty(),
                "The estate desk should expose a usable outline"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void estateManagementDeskAccessoriesPersistAndReturn(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.ESTATE_MANAGEMENT_DESK);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        BlockPos absolutePos = helper.absolutePos(FIRST);
        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(absolutePos),
                Direction.UP,
                absolutePos,
                false
        );

        ItemStack dye = new ItemStack(Items.DYE.pick(DyeColor.RED));
        player.setItemInHand(InteractionHand.MAIN_HAND, dye);
        player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                dye,
                InteractionHand.MAIN_HAND,
                hit
        );
        helper.assertBlockProperty(
                FIRST,
                EstateManagementDeskBlock.BLOTTER_COLOR,
                DeskBlotterColor.RED
        );
        helper.assertTrue(
                dye.isEmpty(),
                "Recoloring a desk should consume one dye in survival"
        );

        ItemStack ledger = new ItemStack(Items.WRITABLE_BOOK);
        player.setItemInHand(InteractionHand.MAIN_HAND, ledger);
        player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                ledger,
                InteractionHand.MAIN_HAND,
                hit
        );
        helper.assertBlockProperty(
                FIRST,
                EstateManagementDeskBlock.HAS_LEDGER,
                true
        );

        ItemStack map = MapItem.create(
                helper.getLevel(),
                absolutePos.getX(),
                absolutePos.getZ(),
                (byte) 0,
                true,
                false
        );
        var mapId = map.get(DataComponents.MAP_ID);
        player.setItemInHand(InteractionHand.MAIN_HAND, map);
        player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                map,
                InteractionHand.MAIN_HAND,
                hit
        );
        helper.assertBlockProperty(
                FIRST,
                EstateManagementDeskBlock.HAS_MAP,
                true
        );
        helper.assertTrue(
                helper.getLevel().getBlockEntity(absolutePos)
                        instanceof EstateManagementDeskBlockEntity desk
                        && mapId != null
                        && mapId.equals(
                        desk.getMapCopy().get(DataComponents.MAP_ID)
                ),
                "The desk should retain the installed filled map data"
        );

        List<ItemStack> drops = Block.getDrops(
                helper.getBlockState(FIRST),
                helper.getLevel(),
                absolutePos,
                helper.getLevel().getBlockEntity(absolutePos)
        );
        helper.assertTrue(
                drops.stream().anyMatch(stack ->
                        stack.is(Items.WRITABLE_BOOK)
                ),
                "Breaking a customized desk should return its ledger"
        );
        helper.assertTrue(
                drops.stream().anyMatch(stack ->
                        stack.is(Items.FILLED_MAP)
                                && mapId != null
                                && mapId.equals(stack.get(
                                DataComponents.MAP_ID
                        ))
                ),
                "Breaking a customized desk should return its exact map"
        );

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setShiftKeyDown(true);
        player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                ItemStack.EMPTY,
                InteractionHand.MAIN_HAND,
                hit
        );
        player.setShiftKeyDown(false);
        helper.assertBlockProperty(
                FIRST,
                EstateManagementDeskBlock.HAS_MAP,
                false
        );
        helper.assertItemEntityPresent(Items.FILLED_MAP, FIRST, 2.0);
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void surveyorsMapTableBuildsACompatibleAtlas(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.SURVEYORS_MAP_TABLE);
        BlockPos absolutePos = helper.absolutePos(FIRST);
        helper.assertTrue(
                helper.getLevel().getBlockEntity(absolutePos)
                        instanceof SurveyorsMapTableBlockEntity,
                "The Surveyor's Map Table should create its block entity"
        );
        SurveyorsMapTableBlockEntity table =
                (SurveyorsMapTableBlockEntity) helper.getLevel()
                        .getBlockEntity(absolutePos);

        ItemStack firstMap = MapItem.create(
                helper.getLevel(),
                absolutePos.getX(),
                absolutePos.getZ(),
                (byte) 0,
                true,
                false
        );
        ItemStack eastMap = MapItem.create(
                helper.getLevel(),
                absolutePos.getX() + 128,
                absolutePos.getZ(),
                (byte) 0,
                true,
                false
        );

        helper.assertValueEqual(
                table.addMap(firstMap, helper.getLevel()),
                SurveyorsMapTableBlockEntity.AddResult.ADDED,
                "The first explored map should begin an estate atlas"
        );
        helper.assertValueEqual(
                table.addMap(firstMap, helper.getLevel()),
                SurveyorsMapTableBlockEntity.AddResult.DUPLICATE,
                "The same map should not occupy two atlas slots"
        );
        helper.assertValueEqual(
                table.addMap(eastMap, helper.getLevel()),
                SurveyorsMapTableBlockEntity.AddResult.ADDED,
                "Adjacent map coverage should extend the estate atlas"
        );
        helper.assertValueEqual(
                table.getMapCount(),
                2,
                "The table should retain both compatible maps"
        );
        helper.assertBlockProperty(
                FIRST,
                SurveyorsMapTableBlock.HAS_MAPS,
                true
        );

        var firstId = firstMap.get(DataComponents.MAP_ID);
        var eastId = eastMap.get(DataComponents.MAP_ID);
        List<ItemStack> drops = Block.getDrops(
                helper.getBlockState(FIRST),
                helper.getLevel(),
                absolutePos,
                table
        );
        helper.assertTrue(
                drops.stream().anyMatch(stack -> firstId != null
                        && firstId.equals(stack.get(DataComponents.MAP_ID))),
                "Breaking the table should return the first exact map"
        );
        helper.assertTrue(
                drops.stream().anyMatch(stack -> eastId != null
                        && eastId.equals(stack.get(DataComponents.MAP_ID))),
                "Breaking the table should return the adjacent exact map"
        );

        ItemStack recovered = table.takeLastMap();
        helper.assertTrue(
                eastId != null
                        && eastId.equals(recovered.get(DataComponents.MAP_ID)),
                "Sneak-removal should return the most recently added map"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void allWoodVariantRegistriesAreComplete(
            GameTestHelper helper
    ) {
        int expected = WoodVariant.values().length;

        helper.assertValueEqual(
                ModBlocks.TRELLISES.size(),
                expected,
                "Every wood family should have a trellis"
        );
        helper.assertValueEqual(
                ModBlocks.GRAPE_PRESSES.size(),
                expected,
                "Every wood family should have a grape press"
        );
        helper.assertValueEqual(
                ModBlocks.FERMENTATION_BARRELS.size(),
                expected,
                "Every wood family should have a fermentation barrel"
        );
        helper.assertValueEqual(
                ModBlocks.AGING_BARRELS.size(),
                expected,
                "Every wood family should have an aging barrel"
        );
        helper.assertValueEqual(
                ModBlocks.WINE_RACKS.size(),
                expected,
                "Every wood family should have a wine rack"
        );
        helper.assertValueEqual(
                ModBlocks.WINE_CRATES.size(),
                expected,
                "Every wood family should have a wine crate"
        );
        helper.assertValueEqual(
                ModBlocks.VINTAGE_ARCHIVES.size(),
                expected,
                "Every wood family should have a vintage archive"
        );
        helper.assertValueEqual(
                ModBlocks.BARREL_STANDS.size(),
                expected,
                "Every wood family should have a barrel stand"
        );
        helper.assertValueEqual(
                ModBlocks.LABELLED_CELLAR_SHELVES.size(),
                expected,
                "Every wood family should have a labelled cellar shelf"
        );
        helper.assertValueEqual(
                ModBlocks.TASTING_CABINETS.size(),
                expected,
                "Every wood family should have a tasting cabinet"
        );
        helper.assertValueEqual(
                ModBlocks.ESTATE_MANAGEMENT_DESKS.size(),
                expected,
                "Every wood family should have an estate desk"
        );
        helper.assertValueEqual(
                ModBlocks.SURVEYORS_MAP_TABLES.size(),
                expected,
                "Every wood family should have a Surveyor's Map Table"
        );
        helper.assertValueEqual(
                ModBlocks.RED_GRAPEVINES.size(),
                expected,
                "Every wood family should retain red-vine supports"
        );
        helper.assertValueEqual(
                ModBlocks.WHITE_GRAPEVINES.size(),
                expected,
                "Every wood family should retain white-vine supports"
        );

        for (WoodVariant woodVariant : WoodVariant.values()) {
            helper.assertTrue(
                    ModBlockEntities.GRAPE_PRESS.isValid(
                            ModBlocks.grapePress(woodVariant)
                                    .defaultBlockState()
                    ),
                    woodVariant.id()
                            + " grape press should support its block entity"
            );
            helper.assertTrue(
                    ModBlockEntities.FERMENTATION_BARREL.isValid(
                            ModBlocks.fermentationBarrel(woodVariant)
                                    .defaultBlockState()
                    ),
                    woodVariant.id()
                            + " fermentation barrel should support its block entity"
            );
            helper.assertTrue(
                    ModBlockEntities.AGING_BARREL.isValid(
                            ModBlocks.agingBarrel(woodVariant)
                                    .defaultBlockState()
                    ),
                    woodVariant.id()
                            + " aging barrel should support its block entity"
            );
            helper.assertTrue(
                    ModBlockEntities.WINE_RACK.isValid(
                            ModBlocks.wineRack(woodVariant)
                                    .defaultBlockState()
                    ),
                    woodVariant.id()
                            + " wine rack should support its block entity"
            );
            helper.assertTrue(
                    ModBlockEntities.WINE_CRATE.isValid(
                            ModBlocks.wineCrate(woodVariant)
                                    .defaultBlockState()
                    ),
                    woodVariant.id()
                            + " wine crate should support its block entity"
            );
            helper.assertTrue(
                    ModBlockEntities.VINTAGE_ARCHIVE.isValid(
                            ModBlocks.vintageArchive(woodVariant)
                                    .defaultBlockState()
                    ),
                    woodVariant.id()
                            + " vintage archive should support its block entity"
            );
            helper.assertTrue(
                    ModBlockEntities.CELLAR_COLLECTION.isValid(
                            ModBlocks.labelledCellarShelf(woodVariant)
                                    .defaultBlockState()
                    ),
                    woodVariant.id()
                            + " labelled shelf should support its block entity"
            );
            helper.assertTrue(
                    ModBlockEntities.CELLAR_COLLECTION.isValid(
                            ModBlocks.tastingCabinet(woodVariant)
                                    .defaultBlockState()
                    ),
                    woodVariant.id()
                            + " tasting cabinet should support its block entity"
            );
            helper.assertTrue(
                    ModBlockEntities.ESTATE_MANAGEMENT_DESK.isValid(
                            ModBlocks.estateManagementDesk(woodVariant)
                                    .defaultBlockState()
                    ),
                    woodVariant.id()
                            + " estate desk should support its block entity"
            );
            helper.assertTrue(
                    ModBlockEntities.SURVEYORS_MAP_TABLE.isValid(
                            ModBlocks.surveyorsMapTable(woodVariant)
                                    .defaultBlockState()
                    ),
                    woodVariant.id()
                            + " map table should support its block entity"
            );
        }

        helper.assertTrue(
                ModBlockEntities.AGING_BARREL.isValid(
                        ModBlocks.CHESTNUT_AGING_BARREL.defaultBlockState()
                ) && ModBlockEntities.AGING_BARREL.isValid(
                        ModBlocks.NEUTRAL_AGING_BARREL.defaultBlockState()
                ) && ModBlockEntities.AGING_BARREL.isValid(
                        ModBlocks.LARGE_CASK.defaultBlockState()
                ),
                "Every specialist ageing vessel should support barrel data"
        );

        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void villageProfessionsRecognizeEveryWorkstationVariant(
            GameTestHelper helper
    ) {
        Holder<PoiType> winemakerPoi = BuiltInRegistries
                .POINT_OF_INTEREST_TYPE
                .get(ModVillagers.WINEMAKER_POI_KEY)
                .orElseThrow();
        Holder<PoiType> cooperPoi = BuiltInRegistries
                .POINT_OF_INTEREST_TYPE
                .get(ModVillagers.COOPER_POI_KEY)
                .orElseThrow();

        helper.assertTrue(
                ModVillagers.WINEMAKER_PROFESSION
                        .heldJobSite()
                        .test(winemakerPoi),
                "The Winemaker must claim grape presses"
        );
        helper.assertFalse(
                ModVillagers.WINEMAKER_PROFESSION
                        .heldJobSite()
                        .test(cooperPoi),
                "The Winemaker must not claim barrel stands"
        );
        helper.assertTrue(
                ModVillagers.COOPER_PROFESSION
                        .heldJobSite()
                        .test(cooperPoi),
                "The Cooper must claim barrel stands"
        );
        helper.assertFalse(
                ModVillagers.COOPER_PROFESSION
                        .heldJobSite()
                        .test(winemakerPoi),
                "The Cooper must not claim grape presses"
        );
        helper.assertTrue(
                VillagerProfession.ALL_ACQUIRABLE_JOBS.test(winemakerPoi),
                "Unemployed villagers must discover grape presses"
        );
        helper.assertTrue(
                VillagerProfession.ALL_ACQUIRABLE_JOBS.test(cooperPoi),
                "Unemployed villagers must discover barrel stands"
        );

        for (Block press : ModBlocks.grapePressBlocks()) {
            helper.assertTrue(
                    ModVillagers.WINEMAKER_POI.is(
                            press.defaultBlockState()
                    ),
                    "Every grape press variant must be a Winemaker POI"
            );
        }

        for (Block stand : ModBlocks.barrelStandBlocks()) {
            helper.assertTrue(
                    ModVillagers.COOPER_POI.is(
                            stand.defaultBlockState()
                    ),
                    "Every barrel stand variant must be a Cooper POI"
            );
        }

        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void vintnerStructuresJoinEveryVillageCulture(
            GameTestHelper helper
    ) {
        for (
                ResourceKey<StructureTemplatePool> poolKey
                : ModVillageStructures.housePools()
        ) {
            StructureTemplatePool pool = helper.getLevel()
                    .registryAccess()
                    .lookupOrThrow(Registries.TEMPLATE_POOL)
                    .getValueOrThrow(poolKey);
            List<Pair<StructurePoolElement, Integer>> houses =
                    pool.getTemplates();

            helper.assertTrue(
                    ModVillageStructures.isInjected(
                            helper.getLevel().registryAccess(),
                            poolKey
                    ),
                    "Specialist houses must be injected into "
                            + poolKey.identifier()
            );
            helper.assertTrue(
                    houses.size() >= 3,
                    "Every village culture must contain Vintner structures"
            );
            helper.assertValueEqual(
                    houses.get(houses.size() - 3).getSecond(),
                    1,
                    "Winemaker house weight"
            );
            helper.assertValueEqual(
                    houses.get(houses.size() - 2).getSecond(),
                    1,
                    "Cooper house weight"
            );
            helper.assertValueEqual(
                    houses.get(houses.size() - 1).getSecond(),
                    24,
                    "Vineyard farm weight"
            );

            StructurePoolElement vineyard = houses
                    .get(houses.size() - 1)
                    .getFirst();
            helper.assertTrue(
                    ModVillageStructures.isVineyardElement(vineyard),
                    "Vineyard pool entries must be identifiable for the "
                            + "one-per-village placement guard"
            );
            var structureManager = helper.getLevel()
                    .getServer()
                    .getStructureManager();

            helper.assertValueEqual(
                    vineyard.getSize(
                            structureManager,
                            Rotation.NONE
                    ),
                    new net.minecraft.core.Vec3i(11, 5, 9),
                    "Vineyard pool element size"
            );
            helper.assertValueEqual(
                    vineyard.getShuffledJigsawBlocks(
                            structureManager,
                            BlockPos.ZERO,
                            Rotation.NONE,
                            RandomSource.create(0L)
                    ).size(),
                    1,
                    "Vineyard must expose one village path connector"
            );
        }

        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void villageVineyardsContainCompleteTrellisedRows(
            GameTestHelper helper
    ) {
        helper.assertValueEqual(
                ModVillageStructures.vineyardTemplates().size(),
                5,
                "One vineyard template per vanilla village culture"
        );

        StructurePlaceSettings settings =
                new StructurePlaceSettings();

        for (Identifier templateId
                : ModVillageStructures.vineyardTemplates()) {
            var template = helper.getLevel()
                    .getServer()
                    .getStructureManager()
                    .get(templateId)
                    .orElse(null);

            helper.assertTrue(
                    template != null,
                    "Vineyard template must load: " + templateId
            );
            helper.assertValueEqual(
                    template.getSize().getX(),
                    11,
                    "Vineyard width"
            );
            helper.assertValueEqual(
                    template.getSize().getY(),
                    5,
                    "Vineyard height"
            );
            helper.assertValueEqual(
                    template.getSize().getZ(),
                    9,
                    "Vineyard depth"
            );
            helper.assertValueEqual(
                    template.filterBlocks(
                            BlockPos.ZERO,
                            settings,
                            ModBlocks.VINEYARD_SOIL
                    ).size(),
                    16,
                    "Vineyard soil plots"
            );
            int foundationBlocks = template.filterBlocks(
                    BlockPos.ZERO,
                    settings,
                    Blocks.DIRT
            ).size() + template.filterBlocks(
                    BlockPos.ZERO,
                    settings,
                    Blocks.SANDSTONE
            ).size();
            int retainingBorder = template.filterBlocks(
                    BlockPos.ZERO,
                    settings,
                    Blocks.STRIPPED_OAK_LOG
            ).size() + template.filterBlocks(
                    BlockPos.ZERO,
                    settings,
                    Blocks.STRIPPED_ACACIA_LOG
            ).size() + template.filterBlocks(
                    BlockPos.ZERO,
                    settings,
                    Blocks.STRIPPED_SPRUCE_LOG
            ).size();
            helper.assertValueEqual(
                    retainingBorder,
                    35,
                    "Culture-matched buried retaining border"
            );
            helper.assertValueEqual(
                    foundationBlocks + retainingBorder,
                    99,
                    "Vineyard must contain a complete buried foundation"
            );
            helper.assertValueEqual(
                    template.filterBlocks(
                            BlockPos.ZERO,
                            settings,
                            Blocks.AIR
                    ).size(),
                    264,
                    "Vineyard must clear vegetation from the full plot"
            );

            int grapevineBlocks = 0;
            for (Block grapevine : ModBlocks.RED_GRAPEVINES.values()) {
                grapevineBlocks += template.filterBlocks(
                        BlockPos.ZERO,
                        settings,
                        grapevine
                ).size();
            }
            for (Block grapevine : ModBlocks.WHITE_GRAPEVINES.values()) {
                grapevineBlocks += template.filterBlocks(
                        BlockPos.ZERO,
                        settings,
                        grapevine
                ).size();
            }

            helper.assertValueEqual(
                    grapevineBlocks,
                    32,
                    "Two complete two-block-tall grapevine rows"
            );
            helper.assertValueEqual(
                    template.filterBlocks(
                            BlockPos.ZERO,
                            settings,
                            Blocks.JIGSAW
                    ).size(),
                    1,
                    "Village path connection"
            );
            String entranceState = template.filterBlocks(
                    BlockPos.ZERO,
                    settings,
                    Blocks.JIGSAW
            ).getFirst().nbt().getStringOr("final_state", "");
            helper.assertTrue(
                    entranceState.contains("_stairs[")
                            && entranceState.contains("facing=east"),
                    "The village path entrance must become an ascending stair"
            );
            helper.assertValueEqual(
                    template.filterBlocks(
                            BlockPos.ZERO,
                            settings,
                            Blocks.COMPOSTER
                    ).size(),
                    1,
                    "Vineyard composter"
            );
        }

        helper.succeed();
    }

    @GameTest(maxTicks = 300)
    public void unemployedVillagerClaimsGrapePress(
            GameTestHelper helper
    ) {
        BlockPos pressPos = FIRST;

        helper.setBlock(pressPos, ModBlocks.GRAPE_PRESS);
        helper.getLevel()
                .dimensionType()
                .defaultClock()
                .ifPresent(clock -> helper.getLevel()
                        .clockManager()
                        .setTotalTicks(clock, 2000L));

        Villager winemaker = helper.spawn(
                EntityTypes.VILLAGER,
                pressPos.west()
        );

        helper.succeedWhen(() -> {
            helper.assertTrue(
                    winemaker.getVillagerData()
                            .profession()
                            .is(ModVillagers.WINEMAKER),
                    "The villager beside a grape press should become a Winemaker"
            );
        });
    }

    @GameTest(maxTicks = 300)
    public void unemployedVillagerClaimsBarrelStand(
            GameTestHelper helper
    ) {
        BlockPos standPos = FIRST;

        helper.setBlock(standPos, ModBlocks.BARREL_STAND);
        helper.getLevel()
                .dimensionType()
                .defaultClock()
                .ifPresent(clock -> helper.getLevel()
                        .clockManager()
                        .setTotalTicks(clock, 2000L));

        Villager cooper = helper.spawn(
                EntityTypes.VILLAGER,
                standPos.west()
        );

        helper.succeedWhen(() -> {
            helper.assertTrue(
                    cooper.getVillagerData()
                            .profession()
                            .is(ModVillagers.COOPER),
                    "The villager beside a barrel stand should become a Cooper"
            );
        });
    }

    @GameTest(maxTicks = 40)
    public void specialistVillagerTradesUnlockAcrossFiveLevels(
            GameTestHelper helper
    ) {
        Villager winemaker = specialistVillager(
                helper,
                ModVillagers.WINEMAKER,
                5,
                FIRST
        );
        Villager cooper = specialistVillager(
                helper,
                ModVillagers.COOPER,
                5,
                EAST
        );

        assertTradeProgression(
                helper,
                winemaker,
                new int[]{4, 9, 15, 19, 23},
                "Winemaker"
        );
        assertTradeProgression(
                helper,
                cooper,
                new int[]{3, 5, 8, 11, 13},
                "Cooper"
        );

        helper.assertValueEqual(
                winemaker.getOffers().size(),
                23,
                "A master Winemaker should expose all five trade tiers"
        );
        helper.assertValueEqual(
                cooper.getOffers().size(),
                13,
                "A master Cooper should expose all five trade tiers"
        );

        assertTrade(
                helper,
                winemaker.getOffers(),
                ModItems.RED_GRAPES,
                Items.EMERALD,
                "Winemakers should buy grapes"
        );
        assertTrade(
                helper,
                winemaker.getOffers(),
                Items.EMERALD,
                ModItems.VINTNER_ALMANAC,
                "Journeyman Winemakers should sell the almanac"
        );
        assertTrade(
                helper,
                winemaker.getOffers(),
                Items.EMERALD,
                ModBlocks.VINTAGE_ARCHIVE.asItem(),
                "Master Winemakers should sell vintage archives"
        );
        assertTrade(
                helper,
                cooper.getOffers(),
                Items.EMERALD,
                ModItems.COOPERS_MALLET,
                "Novice Coopers should sell their reusable mallet"
        );
        assertTrade(
                helper,
                cooper.getOffers(),
                Items.EMERALD,
                ModItems.CASK_CONVERSION_KIT,
                "Expert Coopers should sell cask conversion kits"
        );
        assertTrade(
                helper,
                cooper.getOffers(),
                Items.EMERALD,
                ModBlocks.LABELLED_CELLAR_SHELF.asItem(),
                "Master Coopers should sell labelled cellar shelves"
        );

        ModTrades.refreshVillagerOffers(winemaker);
        ModTrades.refreshVillagerOffers(cooper);
        helper.assertValueEqual(
                winemaker.getOffers().size(),
                23,
                "Refreshing Winemaker trades must not create duplicates"
        );
        helper.assertValueEqual(
                cooper.getOffers().size(),
                13,
                "Refreshing Cooper trades must not create duplicates"
        );

        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void differentWoodTrellisesConnect(
            GameTestHelper helper
    ) {
        helper.setBlock(
                FIRST,
                ModBlocks.trellis(WoodVariant.SPRUCE)
        );
        helper.setBlock(
                EAST,
                ModBlocks.trellis(WoodVariant.BAMBOO)
        );

        helper.succeedWhen(() -> {
            helper.assertBlockProperty(
                    FIRST,
                    TrellisBlock.EAST,
                    TrellisBlock.RowConnection.LEVEL
            );
            helper.assertBlockProperty(
                    EAST,
                    TrellisBlock.WEST,
                    TrellisBlock.RowConnection.LEVEL
            );
        });
    }

    @GameTest(maxTicks = 40)
    public void templatePlacedTrellisesRefreshSavedConnections(
            GameTestHelper helper
    ) {
        BlockState stale = ModBlocks.OAK_TRELLIS.defaultBlockState();
        int templateFlags = Block.UPDATE_CLIENTS
                | Block.UPDATE_KNOWN_SHAPE;

        for (BlockPos pos : List.of(
                FIRST,
                FIRST.above(),
                EAST,
                EAST.above()
        )) {
            helper.getLevel().setBlock(
                    helper.absolutePos(pos),
                    stale,
                    templateFlags
            );
        }

        helper.succeedWhen(() -> {
            helper.assertBlockProperty(
                    FIRST,
                    TrellisBlock.HAS_ABOVE,
                    true
            );
            helper.assertBlockProperty(
                    FIRST,
                    TrellisBlock.EAST,
                    TrellisBlock.RowConnection.LEVEL
            );
            helper.assertBlockProperty(
                    EAST,
                    TrellisBlock.WEST,
                    TrellisBlock.RowConnection.LEVEL
            );
            helper.assertBlockProperty(
                    FIRST.above(),
                    TrellisBlock.HAS_BELOW,
                    true
            );
            helper.assertBlockProperty(
                    FIRST.above(),
                    TrellisBlock.EAST,
                    TrellisBlock.RowConnection.LEVEL
            );
            helper.assertBlockProperty(
                    EAST.above(),
                    TrellisBlock.WEST,
                    TrellisBlock.RowConnection.LEVEL
            );
        });
    }

    @GameTest(maxTicks = 40)
    public void grapeCuttingPreservesTrellisWood(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayer(GameType.SURVIVAL);
        Block expected = ModBlocks.redGrapevine(
                WoodVariant.SPRUCE
        );

        helper.setBlock(
                FIRST,
                ModBlocks.trellis(WoodVariant.SPRUCE)
        );
        helper.placeAt(
                player,
                new ItemStack(ModItems.RED_GRAPE_CUTTING),
                FIRST.above(),
                net.minecraft.core.Direction.DOWN
        );

        helper.assertBlockPresent(expected, FIRST);
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void upperGrapevinePreservesItsTrellisWood(
            GameTestHelper helper
    ) {
        Block rootVine = ModBlocks.redGrapevine(
                WoodVariant.SPRUCE
        );
        Block upperVine = ModBlocks.redGrapevine(
                WoodVariant.MANGROVE
        );

        helper.setBlock(
                FIRST,
                rootVine.defaultBlockState()
                        .setValue(GrapevineBlock.AGE, 1)
        );
        helper.setBlock(
                UPPER,
                ModBlocks.trellis(WoodVariant.MANGROVE)
        );

        ((GrapevineBlock) rootVine).performBonemeal(
                helper.getLevel(),
                RandomSource.create(1L),
                helper.absolutePos(FIRST),
                helper.getBlockState(FIRST)
        );

        helper.assertBlockPresent(upperVine, UPPER);
        helper.assertBlockProperty(
                UPPER,
                GrapevineBlock.UPPER,
                true
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void trellisesConnectAndDisconnect(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.OAK_TRELLIS);
        helper.setBlock(EAST, ModBlocks.OAK_TRELLIS);

        helper.runAfterDelay(1, () -> {
            helper.assertBlockProperty(
                    FIRST,
                    TrellisBlock.EAST,
                    TrellisBlock.RowConnection.LEVEL
            );
            helper.assertBlockProperty(
                    EAST,
                    TrellisBlock.WEST,
                    TrellisBlock.RowConnection.LEVEL
            );

            helper.destroyBlock(EAST);
        });

        helper.succeedWhen(() ->
                helper.assertBlockProperty(
                        FIRST,
                        TrellisBlock.EAST,
                        TrellisBlock.RowConnection.NONE
                )
        );
    }

    @GameTest(maxTicks = 40)
    public void isolatedTrellisDoesNotConnect(
            GameTestHelper helper
    ) {
        BlockState isolated = ModBlocks.OAK_TRELLIS
                .defaultBlockState()
                .setValue(TrellisBlock.ISOLATED, true);

        helper.setBlock(FIRST, isolated);
        helper.setBlock(EAST, ModBlocks.OAK_TRELLIS);

        helper.succeedWhen(() -> {
            helper.assertBlockProperty(
                    FIRST,
                    TrellisBlock.EAST,
                    TrellisBlock.RowConnection.NONE
            );
            helper.assertBlockProperty(
                    EAST,
                    TrellisBlock.WEST,
                    TrellisBlock.RowConnection.NONE
            );
        });
    }

    @GameTest(maxTicks = 40)
    public void fourWayTrellisUpdatesOnlyRemovedSide(
            GameTestHelper helper
    ) {
        BlockPos center = new BlockPos(3, 1, 3);

        helper.setBlock(center, ModBlocks.OAK_TRELLIS);
        helper.setBlock(center.north(), ModBlocks.OAK_TRELLIS);
        helper.setBlock(center.east(), ModBlocks.OAK_TRELLIS);
        helper.setBlock(center.south(), ModBlocks.OAK_TRELLIS);
        helper.setBlock(center.west(), ModBlocks.OAK_TRELLIS);

        helper.runAfterDelay(1, () -> {
            helper.assertBlockProperty(
                    center,
                    TrellisBlock.NORTH,
                    TrellisBlock.RowConnection.LEVEL
            );
            helper.assertBlockProperty(
                    center,
                    TrellisBlock.EAST,
                    TrellisBlock.RowConnection.LEVEL
            );
            helper.assertBlockProperty(
                    center,
                    TrellisBlock.SOUTH,
                    TrellisBlock.RowConnection.LEVEL
            );
            helper.assertBlockProperty(
                    center,
                    TrellisBlock.WEST,
                    TrellisBlock.RowConnection.LEVEL
            );
            helper.destroyBlock(center.north());
        });

        helper.succeedWhen(() -> {
            helper.assertBlockProperty(
                    center,
                    TrellisBlock.NORTH,
                    TrellisBlock.RowConnection.NONE
            );
            helper.assertBlockProperty(
                    center,
                    TrellisBlock.EAST,
                    TrellisBlock.RowConnection.LEVEL
            );
            helper.assertBlockProperty(
                    center,
                    TrellisBlock.SOUTH,
                    TrellisBlock.RowConnection.LEVEL
            );
            helper.assertBlockProperty(
                    center,
                    TrellisBlock.WEST,
                    TrellisBlock.RowConnection.LEVEL
            );
        });
    }

    @GameTest(maxTicks = 40)
    public void verticalTrellisesDoNotCreateWireConnections(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.OAK_TRELLIS);
        helper.setBlock(UPPER, ModBlocks.OAK_TRELLIS);

        helper.succeedWhen(() -> {
            assertNoWireConnections(helper, FIRST);
            assertNoWireConnections(helper, UPPER);
            helper.assertBlockProperty(
                    FIRST,
                    TrellisBlock.HAS_ABOVE,
                    true
            );
            helper.assertBlockProperty(
                    FIRST,
                    TrellisBlock.HAS_BELOW,
                    false
            );
            helper.assertBlockProperty(
                    UPPER,
                    TrellisBlock.HAS_ABOVE,
                    false
            );
            helper.assertBlockProperty(
                    UPPER,
                    TrellisBlock.HAS_BELOW,
                    true
            );
        });
    }

    @GameTest(maxTicks = 40)
    public void verticalTrellisStateUpdatesAfterUpperIsRemoved(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.OAK_TRELLIS);
        helper.setBlock(UPPER, ModBlocks.OAK_TRELLIS);

        helper.runAfterDelay(1, () -> helper.destroyBlock(UPPER));

        helper.succeedWhen(() -> {
            helper.assertBlockPresent(ModBlocks.OAK_TRELLIS, FIRST);
            helper.assertBlockProperty(
                    FIRST,
                    TrellisBlock.HAS_ABOVE,
                    false
            );
            helper.assertBlockProperty(
                    FIRST,
                    TrellisBlock.HAS_BELOW,
                    false
            );
        });
    }

    @GameTest(maxTicks = 40)
    public void differentHeightColumnsDoNotPartiallyConnect(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.OAK_TRELLIS);
        helper.setBlock(UPPER, ModBlocks.OAK_TRELLIS);
        helper.setBlock(EAST, ModBlocks.OAK_TRELLIS);

        helper.succeedWhen(() -> {
            helper.assertBlockProperty(
                    FIRST,
                    TrellisBlock.EAST,
                    TrellisBlock.RowConnection.NONE
            );
            helper.assertBlockProperty(
                    EAST,
                    TrellisBlock.WEST,
                    TrellisBlock.RowConnection.NONE
            );
        });
    }

    @GameTest(maxTicks = 40)
    public void matchingTwoHighColumnsConnectAtBothLevels(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.OAK_TRELLIS);
        helper.setBlock(UPPER, ModBlocks.OAK_TRELLIS);
        helper.setBlock(EAST, ModBlocks.OAK_TRELLIS);
        helper.setBlock(EAST.above(), ModBlocks.OAK_TRELLIS);

        helper.succeedWhen(() -> {
            helper.assertBlockProperty(
                    FIRST,
                    TrellisBlock.EAST,
                    TrellisBlock.RowConnection.LEVEL
            );
            helper.assertBlockProperty(
                    EAST,
                    TrellisBlock.WEST,
                    TrellisBlock.RowConnection.LEVEL
            );
            helper.assertBlockProperty(
                    UPPER,
                    TrellisBlock.EAST,
                    TrellisBlock.RowConnection.LEVEL
            );
            helper.assertBlockProperty(
                    EAST.above(),
                    TrellisBlock.WEST,
                    TrellisBlock.RowConnection.LEVEL
            );
        });
    }

    @GameTest(maxTicks = 40)
    public void breakingUpperVineRestoresBothTrellises(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);

        helper.setBlock(FIRST, matureLowerVine());
        helper.setBlock(UPPER, matureUpperVine());
        player.gameMode.destroyBlock(helper.absolutePos(UPPER));

        helper.succeedWhen(() -> {
            helper.assertBlockPresent(ModBlocks.OAK_TRELLIS, FIRST);
            helper.assertBlockPresent(ModBlocks.OAK_TRELLIS, UPPER);
        });
    }

    @GameTest(maxTicks = 40)
    public void breakingLowerVineRestoresBothTrellises(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);

        helper.setBlock(FIRST, matureLowerVine());
        helper.setBlock(UPPER, matureUpperVine());
        player.gameMode.destroyBlock(helper.absolutePos(FIRST));

        helper.succeedWhen(() -> {
            helper.assertBlockPresent(ModBlocks.OAK_TRELLIS, FIRST);
            helper.assertBlockPresent(ModBlocks.OAK_TRELLIS, UPPER);
        });
    }

    @GameTest(maxTicks = 40)
    public void newWineProfileReplacesPreviousProfile(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayerInLevel();

        WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.RED,
                WineQuality.TABLE
        );

        helper.assertTrue(
                WineEffectProfile.RED.isActive(player),
                "The first wine profile should become active"
        );

        WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.WHITE,
                WineQuality.TABLE
        );

        helper.assertFalse(
                WineEffectProfile.RED.isActive(player),
                "A new wine must remove the previous Vintner profile"
        );
        helper.assertTrue(
                WineEffectProfile.WHITE.isActive(player),
                "The newly consumed wine profile should be active"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void drinkingWineItemUsesConsumptionSystem(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        ItemStack wine = new ItemStack(ModItems.RED_WINE);

        ItemStack result = wine.finishUsingItem(
                helper.getLevel(),
                player
        );

        helper.assertTrue(
                result.is(Items.GLASS_BOTTLE),
                "Drinking wine should return a glass bottle"
        );
        helper.assertTrue(
                WineEffectProfile.RED.isActive(player),
                "Drinking the item should activate its wine profile"
        );
        helper.assertValueEqual(
                WineConsumptionManager.state(
                        player,
                        helper.getLevel().getGameTime()
                ).drinks(),
                1,
                "Drinking the item should update consumption history"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void wineProfilesUseRoadmapBenefits(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayerInLevel();
        double baseKnockbackResistance = player.getAttributeValue(
                Attributes.KNOCKBACK_RESISTANCE
        );
        double baseBreakSpeed = player.getAttributeValue(
                Attributes.BLOCK_BREAK_SPEED
        );

        WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.RED,
                WineQuality.TABLE
        );

        helper.assertTrue(
                player.getAttributeValue(
                        Attributes.KNOCKBACK_RESISTANCE
                ) > baseKnockbackResistance,
                "Red wine should increase knockback resistance"
        );
        helper.assertValueEqual(
                WineConsumptionManager.adjustMeleeExhaustion(
                        player,
                        1.0F
                ),
                0.5F,
                "Red wine should reduce melee exhaustion"
        );
        helper.assertValueEqual(
                WineConsumptionManager.adjustGeneralExhaustion(
                        player,
                        1.0F
                ),
                1.0F,
                "Red wine should not reduce unrelated exhaustion"
        );

        WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.WHITE,
                WineQuality.TABLE
        );

        helper.assertTrue(
                player.getAttributeValue(
                        Attributes.BLOCK_BREAK_SPEED
                ) > baseBreakSpeed,
                "White wine should increase block break speed"
        );
        helper.assertValueEqual(
                player.getAttributeValue(
                        Attributes.KNOCKBACK_RESISTANCE
                ),
                baseKnockbackResistance,
                "Replacing red wine should remove its attribute bonus"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void whiteWineReducesHungerDrain(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayerInLevel();
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(0.0F);

        WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.WHITE,
                WineQuality.TABLE
        );
        player.causeFoodExhaustion(4.1F);
        player.getFoodData().tick(player);

        helper.assertValueEqual(
                player.getFoodData().getFoodLevel(),
                20,
                "White wine should delay hunger loss from exhaustion"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void repeatedWineHasDiminishingBenefits(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayerInLevel();

        var first = WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.RED,
                WineQuality.TABLE
        );
        int firstDuration =
                WineEffectProfile.RED.remainingDuration(player);

        var second = WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.RED,
                WineQuality.TABLE
        );
        int secondDuration =
                WineEffectProfile.RED.remainingDuration(player);

        var third = WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.RED,
                WineQuality.TABLE
        );
        int thirdDuration =
                WineEffectProfile.RED.remainingDuration(player);

        helper.assertTrue(
                first.benefitMultiplier()
                        > second.benefitMultiplier()
                        && second.benefitMultiplier()
                        > third.benefitMultiplier(),
                "Each repeated drink should reduce its benefit"
        );
        helper.assertTrue(
                firstDuration > secondDuration
                        && secondDuration > thirdDuration,
                "Diminishing returns should shorten benefit duration"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void excessiveWineCausesTemporaryImpairment(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayerInLevel();

        for (int drink = 0; drink < 3; drink++) {
            WineConsumptionManager.consume(
                    helper.getLevel(),
                    player,
                    WineEffectProfile.WHITE,
                    WineQuality.TABLE
            );
        }

        helper.assertTrue(
                player.hasEffect(MobEffects.NAUSEA),
                "The third drink should cause temporary nausea"
        );

        WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.WHITE,
                WineQuality.TABLE
        );

        helper.assertTrue(
                player.hasEffect(MobEffects.SLOWNESS),
                "Further drinking should cause temporary slowness"
        );
        helper.assertTrue(
                player.hasEffect(MobEffects.WEAKNESS),
                "Further drinking should cause temporary weakness"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void wineToleranceRecoversAfterWindow(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayerInLevel();
        long gameTime = helper.getLevel().getGameTime();

        ((AttachmentTarget) player).setAttached(
                ModAttachments.WINE_CONSUMPTION,
                new WineConsumptionState(4, gameTime - 1)
        );

        var result = WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.AGED_RED,
                WineQuality.FINE
        );

        helper.assertValueEqual(
                result.drinkCount(),
                1,
                "Expired consumption history should reset"
        );
        helper.assertFalse(
                result.impaired(),
                "The first drink after recovery should not impair"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void switchingWinePreservesUnrelatedEffects(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayerInLevel();

        player.addEffect(
                new net.minecraft.world.effect.MobEffectInstance(
                        MobEffects.FIRE_RESISTANCE,
                        20 * 60,
                        0
                )
        );
        player.addEffect(
                new net.minecraft.world.effect.MobEffectInstance(
                        MobEffects.REGENERATION,
                        20 * 60,
                        0
                )
        );

        WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.RED,
                WineQuality.TABLE
        );
        WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.WHITE,
                WineQuality.TABLE
        );

        helper.assertTrue(
                player.hasEffect(MobEffects.FIRE_RESISTANCE),
                "Changing wine must not remove unrelated status effects"
        );
        helper.assertTrue(
                player.hasEffect(MobEffects.REGENERATION),
                "Wine profiles must not replace healing effects"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void matchingMealExtendsActiveWineOnce(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayerInLevel();

        WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.RED,
                WineQuality.TABLE
        );
        int originalDuration =
                WineEffectProfile.RED.remainingDuration(player);

        WinePairingManager.onMealConsumed(
                helper.getLevel(),
                player,
                new ItemStack(Items.COOKED_BEEF)
        );
        int pairedDuration =
                WineEffectProfile.RED.remainingDuration(player);

        WinePairingManager.onMealConsumed(
                helper.getLevel(),
                player,
                new ItemStack(Items.COOKED_MUTTON)
        );

        helper.assertValueEqual(
                pairedDuration,
                Math.round(
                        originalDuration
                                * WinePairingManager.DURATION_MULTIPLIER
                ),
                "A matching meal should extend the wine duration"
        );
        helper.assertValueEqual(
                WineEffectProfile.RED.remainingDuration(player),
                pairedDuration,
                "One wine serving must not pair more than once"
        );
        helper.assertTrue(
                WinePairingManager.state(
                        player,
                        helper.getLevel().getGameTime()
                ).paired(),
                "The wine serving should remember that it was paired"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void recentMealPairsWithNextMatchingWine(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayerInLevel();

        WinePairingManager.onMealConsumed(
                helper.getLevel(),
                player,
                new ItemStack(Items.COOKED_SALMON)
        );
        WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.WHITE,
                WineQuality.TABLE
        );

        helper.assertValueEqual(
                WineEffectProfile.WHITE.remainingDuration(player),
                Math.round(
                        45 * 20
                                * WinePairingManager
                                        .DURATION_MULTIPLIER
                ),
                "A recent matching meal should pair when wine is drunk"
        );
        helper.assertTrue(
                WinePairingManager.state(
                        player,
                        helper.getLevel().getGameTime()
                ).paired(),
                "Meal-first pairing should mark the serving as paired"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void mismatchedMealDoesNotExtendWine(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayerInLevel();

        WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.RED,
                WineQuality.TABLE
        );
        int originalDuration =
                WineEffectProfile.RED.remainingDuration(player);

        WinePairingManager.onMealConsumed(
                helper.getLevel(),
                player,
                new ItemStack(Items.COOKED_COD)
        );

        helper.assertValueEqual(
                WineEffectProfile.RED.remainingDuration(player),
                originalDuration,
                "Fish should not extend a red wine profile"
        );
        helper.assertFalse(
                WinePairingManager.state(
                        player,
                        helper.getLevel().getGameTime()
                ).paired(),
                "A mismatched meal must not consume the pairing"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void finishingTaggedFoodUsesPairingSystem(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayerInLevel();

        WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.WHITE,
                WineQuality.TABLE
        );
        int originalDuration =
                WineEffectProfile.WHITE.remainingDuration(player);

        new ItemStack(Items.BREAD).finishUsingItem(
                helper.getLevel(),
                player
        );

        helper.assertTrue(
                WineEffectProfile.WHITE.remainingDuration(player)
                        > originalDuration,
                "Finishing a tagged food should invoke wine pairing"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void survivalIngredientsUnlockVintnerRecipes(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayerInLevel();

        for (WoodVariant woodVariant : WoodVariant.values()) {
            triggerInventoryChange(
                    player,
                    planksFor(woodVariant)
            );
        }
        triggerInventoryChange(player, Items.BONE_MEAL);
        triggerInventoryChange(player, ModItems.RED_GRAPES);
        triggerInventoryChange(player, ModItems.WHITE_GRAPES);
        triggerInventoryChange(player, Items.BOOK);
        triggerInventoryChange(player, Items.COPPER_INGOT);

        helper.succeedWhen(() -> {
            for (WoodVariant woodVariant : WoodVariant.values()) {
                assertRecipeKnown(
                        helper,
                        player,
                        woodVariant.trellisId()
                );
                assertRecipeKnown(
                        helper,
                        player,
                        woodVariant.grapePressId()
                );
                assertRecipeKnown(
                        helper,
                        player,
                        woodVariant.fermentationBarrelId()
                );
                assertRecipeKnown(
                        helper,
                        player,
                        woodVariant.agingBarrelId()
                );
                assertRecipeKnown(
                        helper,
                        player,
                        woodVariant.wineRackId()
                );
                assertRecipeKnown(
                        helper,
                        player,
                        woodVariant.wineCrateId()
                );
            }
            assertRecipeKnown(
                    helper,
                    player,
                    "compost"
            );
            assertRecipeKnown(
                    helper,
                    player,
                    "vintner_almanac"
            );
            assertRecipeKnown(
                    helper,
                    player,
                    "soil_probe"
            );
        });
    }

    private static Item planksFor(WoodVariant woodVariant) {
        return switch (woodVariant) {
            case OAK -> Items.OAK_PLANKS;
            case SPRUCE -> Items.SPRUCE_PLANKS;
            case BIRCH -> Items.BIRCH_PLANKS;
            case JUNGLE -> Items.JUNGLE_PLANKS;
            case ACACIA -> Items.ACACIA_PLANKS;
            case DARK_OAK -> Items.DARK_OAK_PLANKS;
            case MANGROVE -> Items.MANGROVE_PLANKS;
            case CHERRY -> Items.CHERRY_PLANKS;
            case PALE_OAK -> Items.PALE_OAK_PLANKS;
            case BAMBOO -> Items.BAMBOO_PLANKS;
            case CRIMSON -> Items.CRIMSON_PLANKS;
            case WARPED -> Items.WARPED_PLANKS;
        };
    }

    @GameTest(maxTicks = 40)
    public void pressEnforcesCapacityAndConvertsGrapes(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.GRAPE_PRESS);

        GrapePressBlockEntity press = helper.getBlockEntity(
                FIRST,
                GrapePressBlockEntity.class
        );
        ItemStack grapes = new ItemStack(ModItems.RED_GRAPES, 16);

        helper.assertValueEqual(
                press.insert(grapes, 16),
                GrapePressBlockEntity.CAPACITY,
                "The press must stop accepting grapes at capacity"
        );
        helper.assertTrue(press.press(), "The first press should succeed");
        helper.assertTrue(press.press(), "The second press should succeed");
        helper.assertFalse(press.canPress(), "No grapes should remain to press");
        helper.assertValueEqual(
                press.getOutput().getCount(),
                2,
                "Eight grapes should create two bottles of must"
        );
        helper.assertTrue(
                press.getOutput().is(ModItems.RED_MUST),
                "Red grapes should create red must"
        );
        helper.assertValueEqual(
                press.getComparatorSignal(),
                4,
                "Two bottles of must should emit comparator level four"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void pressPreservesFinalBatchMetadata(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.GRAPE_PRESS);

        GrapePressBlockEntity press = helper.getBlockEntity(
                FIRST,
                GrapePressBlockEntity.class
        );
        ItemStack grapes = new ItemStack(
                ModItems.WHITE_GRAPES,
                GrapePressBlockEntity.GRAPES_PER_PRESS
        );
        WineMetadata.apply(grapes, 12, WineQuality.EXCEPTIONAL);

        helper.assertValueEqual(
                press.insert(
                        grapes,
                        GrapePressBlockEntity.GRAPES_PER_PRESS
                ),
                GrapePressBlockEntity.GRAPES_PER_PRESS,
                "The complete grape batch should fit in the press"
        );
        helper.assertTrue(
                press.press(),
                "A complete final batch should press successfully"
        );
        helper.assertTrue(
                press.getInput().isEmpty(),
                "The complete grape batch should be consumed"
        );
        helper.assertValueEqual(
                WineMetadata.vintage(press.getOutput()),
                12,
                "Pressing the final grapes must preserve vintage"
        );
        helper.assertValueEqual(
                WineMetadata.quality(press.getOutput()),
                WineQuality.EXCEPTIONAL,
                "Pressing the final grapes must preserve quality"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void pressRejectsMixedGrapeBatches(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.GRAPE_PRESS);

        GrapePressBlockEntity press = helper.getBlockEntity(
                FIRST,
                GrapePressBlockEntity.class
        );

        helper.assertValueEqual(
                press.insert(new ItemStack(ModItems.RED_GRAPES), 1),
                1,
                "The first grape should establish the batch"
        );
        helper.assertFalse(
                press.canInsert(new ItemStack(ModItems.WHITE_GRAPES)),
                "A red batch must reject white grapes"
        );
        helper.assertValueEqual(
                press.insert(new ItemStack(ModItems.WHITE_GRAPES), 1),
                0,
                "Rejected grapes must not alter the inventory"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void fermentationCompletesAndProducesWine(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.FERMENTATION_BARREL);

        FermentationBarrelBlockEntity barrel = helper.getBlockEntity(
                FIRST,
                FermentationBarrelBlockEntity.class
        );
        ItemStack must = new ItemStack(ModItems.WHITE_MUST);
        WineMetadata.apply(must, 7, WineQuality.FINE);

        for (int bottle = 0;
             bottle < FermentationBarrelBlockEntity.CAPACITY;
             bottle++) {
            helper.assertTrue(
                    barrel.insertOne(must),
                    "A full matching batch of must should be accepted"
            );
        }

        for (int tick = 0;
             tick < FermentationBarrelBlockEntity.FERMENTATION_TIME;
             tick++) {
            FermentationBarrelBlockEntity.serverTick(
                    helper.getLevel(),
                    helper.absolutePos(FIRST),
                    helper.getBlockState(FIRST),
                    barrel
            );
        }

        helper.assertTrue(barrel.isReady(), "Fermentation should complete");
        helper.assertValueEqual(
                barrel.getComparatorSignal(),
                15,
                "Ready wine should emit comparator level fifteen"
        );
        helper.assertBlockProperty(
                FIRST,
                FermentationBarrelBlock.STATUS,
                2
        );

        ItemStack wine = barrel.takeOneWine();
        helper.assertTrue(
                wine.is(ModItems.WHITE_WINE),
                "White must should become white wine"
        );
        helper.assertValueEqual(
                WineMetadata.vintage(wine),
                7,
                "Fermentation should preserve vintage"
        );
        helper.assertValueEqual(
                WineMetadata.quality(wine),
                WineQuality.FINE,
                "Fermentation should preserve quality"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void fermentationEnforcesBatchTypeAndCapacity(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.FERMENTATION_BARREL);

        FermentationBarrelBlockEntity barrel = helper.getBlockEntity(
                FIRST,
                FermentationBarrelBlockEntity.class
        );
        ItemStack redMust = new ItemStack(ModItems.RED_MUST);

        for (int bottle = 0;
             bottle < FermentationBarrelBlockEntity.CAPACITY;
             bottle++) {
            helper.assertTrue(
                    barrel.insertOne(redMust),
                    "Matching must should fill the barrel"
            );
        }

        helper.assertFalse(
                barrel.insertOne(redMust),
                "The fermentation barrel must reject overfilling"
        );
        helper.assertFalse(
                barrel.canInsert(new ItemStack(ModItems.WHITE_MUST)),
                "A red batch must reject white must"
        );
        helper.assertValueEqual(
                barrel.getBottleCount(),
                FermentationBarrelBlockEntity.CAPACITY,
                "Rejected inputs must not change bottle count"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void fermentationCombinesCompatiblePressLots(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.FERMENTATION_BARREL);
        FermentationBarrelBlockEntity barrel = helper.getBlockEntity(
                FIRST,
                FermentationBarrelBlockEntity.class
        );
        ItemStack firstLot = new ItemStack(ModItems.RED_MUST);
        ItemStack secondLot = new ItemStack(ModItems.RED_MUST);
        WineMetadata.apply(firstLot, 3, WineQuality.GOOD);
        WineMetadata.apply(secondLot, 3, WineQuality.GOOD);
        WineMetadata.ensureBatchIdentity(firstLot, 101L);
        WineMetadata.ensureBatchIdentity(secondLot, 202L);

        helper.assertTrue(
                barrel.insertOne(firstLot) && barrel.insertOne(firstLot),
                "The first two-bottle press lot should be accepted"
        );
        helper.assertTrue(
                barrel.insertOne(secondLot) && barrel.insertOne(secondLot),
                "A compatible second press lot should complete fermentation"
        );
        helper.assertValueEqual(
                barrel.getBottleCount(),
                FermentationBarrelBlockEntity.CAPACITY,
                "Compatible press lots should fill one fermentation batch"
        );
        long fermentationBatch = WineMetadata.batchId(
                barrel.getStoredContentsCopy()
        );
        helper.assertTrue(
                fermentationBatch != 101L && fermentationBatch != 202L,
                "Fermentation should assign the combined finished batch id"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void fermentationRejectsDifferentVineyardProvenance(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.FERMENTATION_BARREL);
        FermentationBarrelBlockEntity barrel = helper.getBlockEntity(
                FIRST,
                FermentationBarrelBlockEntity.class
        );
        ItemStack estateLot = new ItemStack(ModItems.RED_MUST);
        ItemStack otherEstateLot = new ItemStack(ModItems.RED_MUST);
        WineMetadata.apply(estateLot, 3, WineQuality.GOOD);
        WineMetadata.apply(otherEstateLot, 3, WineQuality.GOOD);
        WineMetadata.applyProvenance(
                estateLot,
                new WineProvenance(
                        "red",
                        24000L,
                        "minecraft:overworld",
                        1,
                        64,
                        1,
                        "estate-a",
                        "Estate A"
                )
        );
        WineMetadata.applyProvenance(
                otherEstateLot,
                new WineProvenance(
                        "red",
                        24000L,
                        "minecraft:overworld",
                        2,
                        64,
                        2,
                        "estate-b",
                        "Estate B"
                )
        );

        helper.assertTrue(
                barrel.insertOne(estateLot),
                "The first vineyard lot should establish provenance"
        );
        helper.assertFalse(
                barrel.canInsert(otherEstateLot),
                "Must from a different vineyard must not be blended"
        );
        helper.assertValueEqual(
                barrel.getBottleCount(),
                1,
                "Rejected provenance must not change the batch"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void barrelsWaitForFullBatchBeforeProcessing(
            GameTestHelper helper
    ) {
        BlockPos agingPos = new BlockPos(3, 1, 1);
        helper.setBlock(FIRST, ModBlocks.FERMENTATION_BARREL);
        helper.setBlock(agingPos, ModBlocks.AGING_BARREL);

        FermentationBarrelBlockEntity fermentation =
                helper.getBlockEntity(
                        FIRST,
                        FermentationBarrelBlockEntity.class
                );
        ItemStack must = new ItemStack(ModItems.RED_MUST);

        for (int bottle = 1;
             bottle < FermentationBarrelBlockEntity.CAPACITY;
             bottle++) {
            helper.assertTrue(
                    fermentation.insertOne(must),
                    "Partial must should remain stored while waiting"
            );
        }
        FermentationBarrelBlockEntity.serverTick(
                helper.getLevel(),
                helper.absolutePos(FIRST),
                helper.getBlockState(FIRST),
                fermentation
        );
        helper.assertValueEqual(
                fermentation.getProgressPercent(),
                0,
                "A partial fermentation batch must not progress"
        );
        helper.assertBlockProperty(
                FIRST,
                FermentationBarrelBlock.STATUS,
                0
        );

        helper.assertTrue(
                fermentation.insertOne(must),
                "The fourth must bottle should start fermentation"
        );
        for (int tick = 0; tick < 20; tick++) {
            FermentationBarrelBlockEntity.serverTick(
                    helper.getLevel(),
                    helper.absolutePos(FIRST),
                    helper.getBlockState(FIRST),
                    fermentation
            );
        }
        helper.assertTrue(
                fermentation.getProgressPercent() > 0,
                "A full fermentation batch should progress"
        );
        helper.assertBlockProperty(
                FIRST,
                FermentationBarrelBlock.STATUS,
                1
        );

        AgingBarrelBlockEntity aging = helper.getBlockEntity(
                agingPos,
                AgingBarrelBlockEntity.class
        );
        ItemStack wine = new ItemStack(ModItems.WHITE_WINE);

        for (int bottle = 1;
             bottle < AgingBarrelBlockEntity.CAPACITY;
             bottle++) {
            helper.assertTrue(
                    aging.insertOne(wine),
                    "Partial wine should remain stored while waiting"
            );
        }
        AgingBarrelBlockEntity.serverTick(
                helper.getLevel(),
                helper.absolutePos(agingPos),
                helper.getBlockState(agingPos),
                aging
        );
        helper.assertValueEqual(
                aging.getProgressPercent(),
                0,
                "A partial ageing batch must not progress"
        );
        helper.assertBlockProperty(
                agingPos,
                AgingBarrelBlock.STATUS,
                0
        );

        helper.assertTrue(
                aging.insertOne(wine),
                "The fourth wine bottle should start ageing"
        );
        for (int tick = 0; tick < 20; tick++) {
            AgingBarrelBlockEntity.serverTick(
                    helper.getLevel(),
                    helper.absolutePos(agingPos),
                    helper.getBlockState(agingPos),
                    aging
            );
        }
        helper.assertTrue(
                aging.getProgressPercent() > 0,
                "A full ageing batch should progress"
        );
        helper.assertBlockProperty(
                agingPos,
                AgingBarrelBlock.STATUS,
                1
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void fermentationAssignsPersistentBottleNumbers(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.FERMENTATION_BARREL);

        FermentationBarrelBlockEntity barrel = helper.getBlockEntity(
                FIRST,
                FermentationBarrelBlockEntity.class
        );
        ItemStack must = new ItemStack(ModItems.RED_MUST);
        WineMetadata.apply(must, 12, WineQuality.FINE);
        WineMetadata.ensureBatchIdentity(must, 13579L);

        for (int bottle = 0;
             bottle < FermentationBarrelBlockEntity.CAPACITY;
             bottle++) {
            helper.assertTrue(
                    barrel.insertOne(must),
                    "The numbered test batch should fill the barrel"
            );
        }

        for (int tick = 0;
             tick < FermentationBarrelBlockEntity.FERMENTATION_TIME;
             tick++) {
            FermentationBarrelBlockEntity.serverTick(
                    helper.getLevel(),
                    helper.absolutePos(FIRST),
                    helper.getBlockState(FIRST),
                    barrel
            );
        }

        ItemStack firstBottle = barrel.takeOneWine();
        helper.assertValueEqual(
                WineMetadata.bottleNumber(firstBottle),
                1,
                "The first extracted wine should be bottle one"
        );
        helper.assertValueEqual(
                WineMetadata.batchBottleCount(firstBottle),
                FermentationBarrelBlockEntity.CAPACITY,
                "Bottle identity should record the original batch size"
        );

        FermentationBarrelBlockEntity restored =
                (FermentationBarrelBlockEntity) reload(helper, barrel);
        ItemStack secondBottle = restored.takeOneWine();

        helper.assertValueEqual(
                WineMetadata.bottleNumber(secondBottle),
                2,
                "Bottle numbering should survive a save/load cycle"
        );
        helper.assertValueEqual(
                WineMetadata.batchBottleCount(secondBottle),
                FermentationBarrelBlockEntity.CAPACITY,
                "The restored barrel should preserve its batch size"
        );
        helper.assertValueEqual(
                WineMetadata.batchId(secondBottle),
                WineMetadata.batchId(firstBottle),
                "Numbered bottles should retain one batch identity"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void agingCompletesAndImprovesQuality(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.AGING_BARREL);

        AgingBarrelBlockEntity barrel = helper.getBlockEntity(
                FIRST,
                AgingBarrelBlockEntity.class
        );
        ItemStack wine = new ItemStack(ModItems.RED_WINE);
        WineMetadata.apply(wine, 4, WineQuality.TABLE);

        for (int bottle = 0;
             bottle < AgingBarrelBlockEntity.CAPACITY;
             bottle++) {
            helper.assertTrue(
                    barrel.insertOne(wine),
                    "A full matching wine batch should be accepted"
            );
        }

        for (int tick = 0;
             tick < AgingBarrelBlockEntity.AGING_TIME;
             tick++) {
            AgingBarrelBlockEntity.serverTick(
                    helper.getLevel(),
                    helper.absolutePos(FIRST),
                    helper.getBlockState(FIRST),
                    barrel
            );
        }

        helper.assertTrue(barrel.isReady(), "Aging should complete");
        helper.assertValueEqual(
                barrel.getComparatorSignal(),
                15,
                "Ready aged wine should emit comparator level fifteen"
        );
        helper.assertBlockProperty(
                FIRST,
                AgingBarrelBlock.STATUS,
                2
        );

        ItemStack agedWine = barrel.takeOneAgedWine();
        helper.assertTrue(
                agedWine.is(ModItems.AGED_RED_WINE),
                "Red wine should become aged red wine"
        );
        helper.assertValueEqual(
                WineMetadata.vintage(agedWine),
                4,
                "Aging should preserve vintage"
        );
        helper.assertValueEqual(
                WineMetadata.quality(agedWine),
                WineQuality.GOOD,
                "Aging should improve table wine to good quality"
        );
        helper.assertValueEqual(
                WineMetadata.bottleNumber(agedWine),
                1,
                "The first aged output should be bottle one"
        );
        helper.assertValueEqual(
                WineMetadata.batchBottleCount(agedWine),
                AgingBarrelBlockEntity.CAPACITY,
                "Aged bottle identity should record the batch size"
        );

        AgingBarrelBlockEntity restored =
                (AgingBarrelBlockEntity) reload(helper, barrel);
        ItemStack secondAgedWine = restored.takeOneAgedWine();

        helper.assertValueEqual(
                WineMetadata.bottleNumber(secondAgedWine),
                2,
                "Aged bottle numbering should survive save/load"
        );
        helper.assertValueEqual(
                WineMetadata.batchBottleCount(secondAgedWine),
                AgingBarrelBlockEntity.CAPACITY,
                "The restored aging barrel should preserve batch size"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void bottleStorageHistoryTracksCellarTime(
            GameTestHelper helper
    ) {
        ItemStack wine = new ItemStack(ModItems.AGED_RED_WINE);
        WineMetadata.apply(wine, 14, WineQuality.EXCEPTIONAL);
        WineMetadata.markBottled(wine, 100L);

        WineMetadata.ageBottle(
                wine,
                24000L,
                com.zenith.vintner.wine.CellarRating.BASIC
        );
        WineMetadata.ageBottle(
                wine,
                48000L,
                com.zenith.vintner.wine.CellarRating.IDEAL
        );

        helper.assertValueEqual(
                WineMetadata.storageTicks(
                        wine,
                        com.zenith.vintner.wine.CellarRating.BASIC
                ),
                24000L,
                "Basic-cellar time should be recorded"
        );
        helper.assertValueEqual(
                WineMetadata.storageTicks(
                        wine,
                        com.zenith.vintner.wine.CellarRating.IDEAL
                ),
                48000L,
                "Ideal-cellar time should be recorded"
        );
        helper.assertValueEqual(
                WineMetadata.totalStorageDays(wine),
                3L,
                "Storage history should report total elapsed days"
        );
        helper.assertValueEqual(
                WineMetadata.dominantCellarRating(wine),
                com.zenith.vintner.wine.CellarRating.IDEAL,
                "The longest storage condition should be dominant"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void agingEnforcesBatchTypeAndCapacity(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.AGING_BARREL);

        AgingBarrelBlockEntity barrel = helper.getBlockEntity(
                FIRST,
                AgingBarrelBlockEntity.class
        );
        ItemStack redWine = new ItemStack(ModItems.RED_WINE);

        for (int bottle = 0;
             bottle < AgingBarrelBlockEntity.CAPACITY;
             bottle++) {
            helper.assertTrue(
                    barrel.insertOne(redWine),
                    "Matching wine should fill the barrel"
            );
        }

        helper.assertFalse(
                barrel.insertOne(redWine),
                "The aging barrel must reject overfilling"
        );
        helper.assertFalse(
                barrel.canInsert(new ItemStack(ModItems.WHITE_WINE)),
                "A red batch must reject white wine"
        );
        helper.assertValueEqual(
                barrel.getBottleCount(),
                AgingBarrelBlockEntity.CAPACITY,
                "Rejected inputs must not change bottle count"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void machineContentsSurviveSerialization(
            GameTestHelper helper
    ) {
        BlockPos pressPos = new BlockPos(1, 1, 1);
        BlockPos fermentationPos = new BlockPos(3, 1, 1);
        BlockPos agingPos = new BlockPos(5, 1, 1);

        helper.setBlock(pressPos, ModBlocks.GRAPE_PRESS);
        helper.setBlock(
                fermentationPos,
                ModBlocks.FERMENTATION_BARREL
        );
        helper.setBlock(agingPos, ModBlocks.AGING_BARREL);

        GrapePressBlockEntity press = helper.getBlockEntity(
                pressPos,
                GrapePressBlockEntity.class
        );
        press.insert(new ItemStack(ModItems.RED_GRAPES), 8);
        press.press();

        FermentationBarrelBlockEntity fermentation =
                helper.getBlockEntity(
                        fermentationPos,
                        FermentationBarrelBlockEntity.class
                );
        ItemStack must = new ItemStack(ModItems.WHITE_MUST);
        WineMetadata.apply(must, 11, WineQuality.EXCEPTIONAL);
        fermentation.insertOne(must);

        AgingBarrelBlockEntity aging = helper.getBlockEntity(
                agingPos,
                AgingBarrelBlockEntity.class
        );
        ItemStack wine = new ItemStack(ModItems.RED_WINE);
        WineMetadata.apply(wine, 9, WineQuality.FINE);
        aging.insertOne(wine);

        GrapePressBlockEntity restoredPress =
                (GrapePressBlockEntity) reload(helper, press);
        FermentationBarrelBlockEntity restoredFermentation =
                (FermentationBarrelBlockEntity) reload(
                        helper,
                        fermentation
                );
        AgingBarrelBlockEntity restoredAging =
                (AgingBarrelBlockEntity) reload(helper, aging);

        helper.assertValueEqual(
                restoredPress.getInput().getCount(),
                4,
                "Press input must survive a save/load round trip"
        );
        helper.assertValueEqual(
                restoredPress.getOutput().getCount(),
                1,
                "Press output must survive a save/load round trip"
        );

        ItemStack restoredMust =
                restoredFermentation.getStoredContentsCopy();
        helper.assertTrue(
                restoredMust.is(ModItems.WHITE_MUST),
                "Fermentation batch type must survive save/load"
        );
        helper.assertValueEqual(
                WineMetadata.vintage(restoredMust),
                11,
                "Fermentation vintage must survive save/load"
        );
        helper.assertValueEqual(
                WineMetadata.quality(restoredMust),
                WineQuality.EXCEPTIONAL,
                "Fermentation quality must survive save/load"
        );

        ItemStack restoredWine = restoredAging.getStoredContentsCopy();
        helper.assertTrue(
                restoredWine.is(ModItems.RED_WINE),
                "Aging batch type must survive save/load"
        );
        helper.assertValueEqual(
                WineMetadata.vintage(restoredWine),
                9,
                "Aging vintage must survive save/load"
        );
        helper.assertValueEqual(
                WineMetadata.quality(restoredWine),
                WineQuality.FINE,
                "Aging quality must survive save/load"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void wineIdentitySurvivesTheFullPipeline(
            GameTestHelper helper
    ) {
        BlockPos pressPos = new BlockPos(1, 1, 1);
        BlockPos fermentationPos = new BlockPos(3, 1, 1);
        BlockPos agingPos = new BlockPos(5, 1, 1);
        helper.setBlock(pressPos, ModBlocks.GRAPE_PRESS);
        helper.setBlock(
                fermentationPos,
                ModBlocks.FERMENTATION_BARREL
        );
        helper.setBlock(agingPos, ModBlocks.AGING_BARREL);

        GrapePressBlockEntity press = helper.getBlockEntity(
                pressPos,
                GrapePressBlockEntity.class
        );
        ItemStack grapes = new ItemStack(
                ModItems.RED_GRAPES,
                GrapePressBlockEntity.GRAPES_PER_PRESS
        );
        WineMetadata.apply(grapes, 18, WineQuality.FINE);
        press.insert(
                grapes,
                GrapePressBlockEntity.GRAPES_PER_PRESS
        );
        helper.assertTrue(press.press(), "Grapes should press");
        ItemStack must = press.bottleOneMust();
        long pressBatchId = WineMetadata.batchId(must);

        helper.assertTrue(
                pressBatchId != 0L,
                "Pressing should establish a press-lot identity"
        );

        FermentationBarrelBlockEntity fermentation =
                helper.getBlockEntity(
                        fermentationPos,
                        FermentationBarrelBlockEntity.class
                );
        for (int bottle = 0;
             bottle < FermentationBarrelBlockEntity.CAPACITY;
             bottle++) {
            helper.assertTrue(
                    fermentation.insertOne(must),
                    "Identified must should fill fermentation"
            );
        }

        for (int tick = 0;
             tick < FermentationBarrelBlockEntity.FERMENTATION_TIME;
             tick++) {
            FermentationBarrelBlockEntity.serverTick(
                    helper.getLevel(),
                    helper.absolutePos(fermentationPos),
                    helper.getBlockState(fermentationPos),
                    fermentation
            );
        }

        ItemStack wine = fermentation.takeOneWine();
        long fermentationBatchId = WineMetadata.batchId(wine);
        helper.assertTrue(
                fermentationBatchId != 0L
                        && fermentationBatchId != pressBatchId,
                "Fermentation should establish the combined wine batch"
        );

        AgingBarrelBlockEntity aging = helper.getBlockEntity(
                agingPos,
                AgingBarrelBlockEntity.class
        );
        for (int bottle = 0;
             bottle < AgingBarrelBlockEntity.CAPACITY;
             bottle++) {
            helper.assertTrue(
                    aging.insertOne(wine),
                    "Identified wine should fill barrel ageing"
            );
        }

        for (int tick = 0;
             tick < AgingBarrelBlockEntity.AGING_TIME;
             tick++) {
            AgingBarrelBlockEntity.serverTick(
                    helper.getLevel(),
                    helper.absolutePos(agingPos),
                    helper.getBlockState(agingPos),
                    aging
            );
        }

        ItemStack agedWine = aging.takeOneAgedWine();
        helper.assertValueEqual(
                WineMetadata.batchId(agedWine),
                fermentationBatchId,
                "Barrel aging must preserve batch identity"
        );
        helper.assertValueEqual(
                WineMetadata.vintage(agedWine),
                18,
                "The final bottle must preserve vintage"
        );
        helper.assertValueEqual(
                WineMetadata.quality(agedWine),
                WineQuality.EXCEPTIONAL,
                "Barrel aging should improve fine wine"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void tastingProfileIsStableForABatch(
            GameTestHelper helper
    ) {
        ItemStack first = new ItemStack(ModItems.RED_WINE);
        WineMetadata.apply(first, 8, WineQuality.FINE);
        WineMetadata.ensureBatchIdentity(first, 123456L);

        ItemStack sameBatch = first.copy();
        ItemStack differentBatch = new ItemStack(ModItems.RED_WINE);
        WineMetadata.apply(differentBatch, 8, WineQuality.FINE);
        WineMetadata.ensureBatchIdentity(differentBatch, 654321L);

        helper.assertValueEqual(
                WineTastingProfile.from(first).description(),
                WineTastingProfile.from(sameBatch).description(),
                "One batch should always produce the same tasting notes"
        );
        helper.assertFalse(
                WineMetadata.tastingProfileSeed(first)
                        == WineMetadata.tastingProfileSeed(
                                differentBatch
                        ),
                "Distinct batches should have distinct tasting seeds"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void almanacInspectionGrantsAdvancement(
            GameTestHelper helper
    ) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack almanac = new ItemStack(ModItems.VINTNER_ALMANAC);
        ItemStack wine = new ItemStack(ModItems.AGED_RED_WINE);
        WineMetadata.apply(wine, 9, WineQuality.FINE);
        WineMetadata.ensureBatchIdentity(wine, 112233L);
        player.setItemInHand(InteractionHand.MAIN_HAND, almanac);
        player.setItemInHand(InteractionHand.OFF_HAND, wine);

        almanac.use(
                helper.getLevel(),
                player,
                InteractionHand.MAIN_HAND
        );

        AdvancementHolder advancement = helper.getLevel()
                .getServer()
                .getAdvancements()
                .get(
                        Identifier.fromNamespaceAndPath(
                                "vintner",
                                "vintner/inspect_wine"
                        )
                );

        helper.assertTrue(
                advancement != null,
                "The wine-inspection advancement should load"
        );
        helper.assertTrue(
                player.getAdvancements()
                        .getOrStartProgress(advancement)
                        .isDone(),
                "Inspecting wine with the almanac should grant progress"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void wineRackStoresAgesAndReturnsBottle(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.WINE_RACK);
        WineRackBlockEntity rack = helper.getBlockEntity(
                FIRST,
                WineRackBlockEntity.class
        );
        ItemStack wine = new ItemStack(ModItems.WHITE_WINE);
        WineMetadata.apply(wine, 5, WineQuality.TABLE);
        WineMetadata.ensureBatchIdentity(wine, 987654L);
        WineMetadata.markBottled(wine, 10L);

        helper.assertTrue(
                rack.insertOne(wine),
                "The rack should accept a bottle of wine"
        );
        helper.assertValueEqual(
                rack.getBottleCount(),
                1,
                "The rack should store the inserted bottle"
        );

        rack = reloadRackWithElapsedTime(
                helper,
                rack,
                20L
        );

        for (int tick = 0; tick < 20; tick++) {
            WineRackBlockEntity.serverTick(
                    helper.getLevel(),
                    helper.absolutePos(FIRST),
                    helper.getBlockState(FIRST),
                    rack
            );
        }

        ItemStack stored = rack.getBottleCopy(0);
        helper.assertTrue(
                WineMetadata.bottleAge(stored) > 0L,
                "Stored wine should age with the cellar conditions"
        );
        helper.assertValueEqual(
                WineMetadata.ageStage(stored),
                WineAgeStage.YOUNG,
                "A newly stored bottle should remain young"
        );
        helper.assertValueEqual(
                WineMetadata.batchId(stored),
                987654L,
                "Physical storage must preserve bottle identity"
        );

        ItemStack returned = rack.takeLastBottle();
        helper.assertValueEqual(
                WineMetadata.batchId(returned),
                987654L,
                "Removing a bottle must return the same batch"
        );
        helper.assertValueEqual(
                rack.getBottleCount(),
                0,
                "Removing the bottle should empty the rack"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void wineRackEmptyHandInteractionReturnsBottle(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.WINE_RACK);
        WineRackBlockEntity rack = helper.getBlockEntity(
                FIRST,
                WineRackBlockEntity.class
        );
        ItemStack wine = new ItemStack(ModItems.RED_WINE);
        WineMetadata.ensureBatchIdentity(wine, 135790L);
        helper.assertTrue(
                rack.insertOne(wine),
                "The interaction test rack should accept wine"
        );
        var player = helper.makeMockServerPlayer(
                GameType.SURVIVAL
        );

        helper.useBlock(FIRST, player);

        helper.assertValueEqual(
                rack.getBottleCount(),
                0,
                "Empty-hand use should remove the latest bottle"
        );
        helper.assertTrue(
                player.getInventory().contains(
                        stack -> WineMetadata.batchId(stack)
                                == 135790L
                ),
                "Empty-hand use should return the same bottle"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void creativeRackBreakDropsStoredBottle(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.WINE_RACK);
        WineRackBlockEntity rack = helper.getBlockEntity(
                FIRST,
                WineRackBlockEntity.class
        );
        ItemStack wine = new ItemStack(ModItems.WHITE_WINE);
        WineMetadata.ensureBatchIdentity(wine, 975310L);
        WineMetadata.assignBottleNumber(wine, 2, 4);
        helper.assertTrue(
                rack.insertOne(wine),
                "The creative-break rack should accept wine"
        );
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);

        player.gameMode.destroyBlock(helper.absolutePos(FIRST));

        helper.assertBlockNotPresent(ModBlocks.WINE_RACK, FIRST);
        helper.assertItemEntityPresent(
                ModItems.WHITE_WINE,
                FIRST,
                2.0
        );
        List<ItemEntity> drops = helper.getLevel()
                .getEntitiesOfClass(
                        ItemEntity.class,
                        new AABB(helper.absolutePos(FIRST)).inflate(2.0)
                );
        helper.assertTrue(
                drops.stream().anyMatch(drop ->
                        WineMetadata.batchId(drop.getItem()) == 975310L
                                && WineMetadata.bottleNumber(
                                drop.getItem()
                        ) == 2
                ),
                "Creative breaking should preserve bottle metadata"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void wineRackContentsSurviveSerialization(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.WINE_RACK);
        WineRackBlockEntity rack = helper.getBlockEntity(
                FIRST,
                WineRackBlockEntity.class
        );
        ItemStack wine = new ItemStack(ModItems.AGED_RED_WINE);
        WineMetadata.apply(wine, 3, WineQuality.EXCEPTIONAL);
        WineMetadata.ensureBatchIdentity(wine, 246810L);
        rack.insertOne(wine);

        WineRackBlockEntity restored =
                (WineRackBlockEntity) reload(helper, rack);
        ItemStack restoredBottle = restored.getBottleCopy(0);

        helper.assertValueEqual(
                restored.getBottleCount(),
                1,
                "Rack contents must survive save and load"
        );
        helper.assertValueEqual(
                WineMetadata.batchId(restoredBottle),
                246810L,
                "Rack serialization must preserve batch metadata"
        );
        helper.assertValueEqual(
                WineMetadata.quality(restoredBottle),
                WineQuality.EXCEPTIONAL,
                "Rack serialization must preserve wine quality"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void wineCrateStoresSixteenMixedBottles(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.WINE_CRATE);
        WineCrateBlockEntity crate = helper.getBlockEntity(
                FIRST,
                WineCrateBlockEntity.class
        );

        for (int index = 0;
             index < WineCrateBlockEntity.CAPACITY;
             index++) {
            ItemStack wine = new ItemStack(
                    index % 2 == 0
                            ? ModItems.RED_WINE
                            : ModItems.AGED_WHITE_WINE
            );
            WineMetadata.ensureBatchIdentity(
                    wine,
                    800000L + index
            );
            WineMetadata.assignBottleNumber(
                    wine,
                    index + 1,
                    WineCrateBlockEntity.CAPACITY
            );
            helper.assertTrue(
                    crate.insertOne(wine),
                    "The crate should accept bottle " + (index + 1)
            );
            helper.assertBlockProperty(
                    FIRST,
                    WineCrateBlock.BOTTLE_COUNT,
                    index + 1
            );
        }

        helper.assertValueEqual(
                crate.getBottleCount(),
                WineCrateBlockEntity.CAPACITY,
                "The crate should store sixteen bottles"
        );
        helper.assertValueEqual(
                crate.getComparatorSignal(),
                15,
                "A full crate should output comparator strength 15"
        );
        helper.assertBlockProperty(
                FIRST,
                WineCrateBlock.BOTTLE_COUNT,
                16
        );
        helper.assertFalse(
                crate.insertOne(new ItemStack(ModItems.RED_WINE)),
                "A full crate must reject a seventeenth bottle"
        );

        ItemStack returned = crate.takeLastBottle();
        helper.assertValueEqual(
                WineMetadata.batchId(returned),
                800015L,
                "Crate retrieval should return the latest bottle"
        );
        helper.assertValueEqual(
                WineMetadata.bottleNumber(returned),
                16,
                "Crate retrieval must preserve bottle numbering"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void wineCrateEmptyHandInteractionReturnsBottle(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.WINE_CRATE);
        WineCrateBlockEntity crate = helper.getBlockEntity(
                FIRST,
                WineCrateBlockEntity.class
        );
        ItemStack wine = new ItemStack(ModItems.AGED_RED_WINE);
        WineMetadata.ensureBatchIdentity(wine, 864200L);
        helper.assertTrue(
                crate.insertOne(wine),
                "The interaction test crate should accept wine"
        );
        var player = helper.makeMockServerPlayer(
                GameType.SURVIVAL
        );

        helper.useBlock(FIRST, player);

        helper.assertValueEqual(
                crate.getBottleCount(),
                0,
                "Empty-hand use should remove the latest crate bottle"
        );
        helper.assertBlockProperty(
                FIRST,
                WineCrateBlock.BOTTLE_COUNT,
                0
        );
        helper.assertTrue(
                player.getInventory().contains(
                        stack -> WineMetadata.batchId(stack)
                                == 864200L
                ),
                "Empty-hand use should preserve the bottle metadata"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void wineCratesCanBePlacedDirectlyOnEachOther(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.WINE_CRATE);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Block upperCrate = ModBlocks.wineCrate(
                WoodVariant.SPRUCE
        );
        ItemStack crateItem = new ItemStack(upperCrate.asItem());
        player.setItemInHand(InteractionHand.MAIN_HAND, crateItem);
        BlockPos lowerPos = helper.absolutePos(FIRST);

        player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                crateItem,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atBottomCenterOf(lowerPos.above()),
                        Direction.UP,
                        lowerPos,
                        false
                )
        );

        helper.assertBlockPresent(ModBlocks.WINE_CRATE, FIRST);
        helper.assertBlockPresent(upperCrate, UPPER);
        helper.assertTrue(
                helper.getBlockState(FIRST)
                        .getShape(helper.getLevel(), lowerPos)
                        .max(Direction.Axis.Y) == 1.0,
                "A stacked crate must support the crate above it"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void wineCrateContentsSurviveSerialization(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.WINE_CRATE);
        WineCrateBlockEntity crate = helper.getBlockEntity(
                FIRST,
                WineCrateBlockEntity.class
        );
        ItemStack first = new ItemStack(ModItems.WHITE_WINE);
        ItemStack second = new ItemStack(ModItems.AGED_RED_WINE);
        WineMetadata.ensureBatchIdentity(first, 112233L);
        WineMetadata.ensureBatchIdentity(second, 445566L);
        WineMetadata.apply(
                second,
                9,
                WineQuality.EXCEPTIONAL
        );
        crate.insertOne(first);
        crate.insertOne(second);

        WineCrateBlockEntity restored =
                (WineCrateBlockEntity) reload(helper, crate);

        helper.assertValueEqual(
                restored.getBottleCount(),
                2,
                "Crate contents must survive save and load"
        );
        helper.assertValueEqual(
                WineMetadata.batchId(restored.getBottleCopy(0)),
                112233L,
                "The first stored batch must survive serialization"
        );
        helper.assertValueEqual(
                WineMetadata.quality(restored.getBottleCopy(1)),
                WineQuality.EXCEPTIONAL,
                "Serialized crate wine must retain quality"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void creativeCrateBreakDropsStoredBottle(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.WINE_CRATE);
        WineCrateBlockEntity crate = helper.getBlockEntity(
                FIRST,
                WineCrateBlockEntity.class
        );
        ItemStack wine = new ItemStack(ModItems.AGED_WHITE_WINE);
        WineMetadata.ensureBatchIdentity(wine, 778899L);
        WineMetadata.assignBottleNumber(wine, 3, 4);
        helper.assertTrue(
                crate.insertOne(wine),
                "The creative-break crate should accept wine"
        );
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);

        player.gameMode.destroyBlock(helper.absolutePos(FIRST));

        helper.assertBlockNotPresent(ModBlocks.WINE_CRATE, FIRST);
        helper.assertItemEntityPresent(
                ModItems.AGED_WHITE_WINE,
                FIRST,
                2.0
        );
        List<ItemEntity> drops = helper.getLevel()
                .getEntitiesOfClass(
                        ItemEntity.class,
                        new AABB(helper.absolutePos(FIRST)).inflate(2.0)
                );
        helper.assertTrue(
                drops.stream().anyMatch(drop ->
                        WineMetadata.batchId(drop.getItem()) == 778899L
                                && WineMetadata.bottleNumber(
                                drop.getItem()
                        ) == 3
                ),
                "Creative breaking must preserve crate bottle metadata"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void wineCrateCatchesUpAfterChunkReload(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.WINE_CRATE);
        WineCrateBlockEntity crate = helper.getBlockEntity(
                FIRST,
                WineCrateBlockEntity.class
        );
        ItemStack wine = new ItemStack(ModItems.RED_WINE);
        WineMetadata.apply(wine, 6, WineQuality.FINE);
        helper.assertTrue(
                crate.insertOne(wine),
                "The crate should accept the catch-up test bottle"
        );

        long elapsedTicks = 24000L;
        WineCrateBlockEntity restored =
                reloadCrateWithElapsedTime(
                        helper,
                        crate,
                        elapsedTicks
                );

        for (int tick = 0; tick < 20; tick++) {
            WineCrateBlockEntity.serverTick(
                    helper.getLevel(),
                    helper.absolutePos(FIRST),
                    helper.getBlockState(FIRST),
                    restored
            );
        }

        CellarConditions conditions = CellarConditions.evaluate(
                helper.getLevel(),
                helper.absolutePos(FIRST)
        );
        long expectedMinimum = Math.round(
                elapsedTicks * conditions.rating().ageRate()
        );

        helper.assertTrue(
                WineMetadata.bottleAge(
                        restored.getBottleCopy(0)
                ) >= expectedMinimum,
                "A reloaded crate should catch up for unloaded world time"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void registeredEstateBrandsNewBatches(
            GameTestHelper helper
    ) {
        ServerPlayer producer = helper.makeMockServerPlayerInLevel();
        BlockPos archivePos = new BlockPos(1, 1, 1);
        helper.setBlock(archivePos, ModBlocks.VINTAGE_ARCHIVE);
        ItemStack almanac = new ItemStack(ModItems.VINTNER_ALMANAC);
        almanac.set(
                DataComponents.CUSTOM_NAME,
                Component.literal("Stone Hill Estate")
        );
        producer.setItemInHand(InteractionHand.MAIN_HAND, almanac);
        producer.setItemInHand(
                InteractionHand.OFF_HAND,
                new ItemStack(Blocks.BANNER.blue())
        );
        producer.setShiftKeyDown(true);
        BlockPos absoluteArchive = helper.absolutePos(archivePos);
        producer.gameMode.useItemOn(
                producer,
                helper.getLevel(),
                almanac,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atCenterOf(absoluteArchive),
                        Direction.UP,
                        absoluteArchive,
                        false
                )
        );
        producer.setShiftKeyDown(false);

        EstateSavedData estates = EstateSavedData.get(helper.getLevel());
        EstateProfile founded = estates.find(producer.getUUID())
                .orElse(null);

        helper.assertTrue(
                founded != null,
                "Sneak-using a named Almanac should found an estate"
        );
        helper.assertValueEqual(
                founded.estateName(),
                "Stone Hill Estate",
                "A named Almanac should establish the estate name"
        );
        helper.assertValueEqual(
                founded.vineyardName(),
                "Stone Hill Estate Vineyard",
                "Registration should establish a vineyard identity"
        );
        helper.assertValueEqual(
                founded.bottleLabel(),
                "Stone Hill Estate",
                "Registration should establish a bottle label"
        );
        helper.assertValueEqual(
                founded.crestColor(),
                DyeColor.BLUE.getName(),
                "An offhand banner should establish the crest colour"
        );

        BlockPos pressPos = new BlockPos(3, 1, 1);
        helper.setBlock(pressPos, ModBlocks.GRAPE_PRESS);
        GrapePressBlockEntity press = helper.getBlockEntity(
                pressPos,
                GrapePressBlockEntity.class
        );
        ItemStack grapes = new ItemStack(
                ModItems.RED_GRAPES,
                GrapePressBlockEntity.GRAPES_PER_PRESS
        );
        press.insert(grapes, grapes.getCount());

        helper.assertTrue(
                press.press(producer),
                "A registered producer should be able to press grapes"
        );
        ItemStack estateMust = press.bottleOneMust();
        helper.assertValueEqual(
                WineMetadata.estateName(estateMust),
                founded.estateName(),
                "New batches should inherit the registered estate name"
        );
        List<EstateLedgerEvent> foundingHistory =
                EstateLedgerSavedData.get(helper.getLevel())
                        .entries(producer.getUUID());
        helper.assertTrue(
                foundingHistory.stream().anyMatch(event ->
                        event.eventType() == LedgerEventType.FOUNDING),
                "Founding through the archive should enter the estate ledger"
        );
        helper.assertTrue(
                foundingHistory.stream().anyMatch(event ->
                        event.eventType()
                                == LedgerEventType.BATCH_PRESSED
                                && event.batchId()
                                == WineMetadata.batchId(
                                        estateMust
                                )),
                "Pressing should record the real batch in the estate ledger"
        );

        EstateProfile renamed = estates.register(
                producer,
                helper.getLevel(),
                helper.absolutePos(new BlockPos(6, 1, 1)),
                "Stone Hill Cellars",
                null
        );
        helper.assertValueEqual(
                renamed.foundingYear(),
                founded.foundingYear(),
                "Renaming must preserve the estate founding year"
        );
        helper.assertValueEqual(
                renamed.homeRegion(),
                founded.homeRegion(),
                "Renaming must preserve the estate home region"
        );
        helper.assertValueEqual(
                renamed.crestColor(),
                founded.crestColor(),
                "Renaming without a banner must preserve the crest"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void namedPlotsTrackBoundariesAndLiveVines(
            GameTestHelper helper
    ) {
        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        EstateSavedData.get(helper.getLevel()).register(
                owner,
                helper.getLevel(),
                helper.absolutePos(new BlockPos(1, 1, 1)),
                "Stone Hill Estate",
                DyeColor.BLUE
        );

        BlockPos firstGround = new BlockPos(1, 0, 1);
        BlockPos secondGround = new BlockPos(5, 0, 5);
        helper.setBlock(firstGround, Blocks.CALCITE);
        helper.setBlock(secondGround, Blocks.CALCITE);
        BlockPos firstCorner = TerroirEvaluator.resolveSitePosition(
                helper.getLevel(),
                helper.absolutePos(firstGround)
        );
        ItemStack almanac = new ItemStack(ModItems.VINTNER_ALMANAC);
        almanac.set(
                DataComponents.CUSTOM_NAME,
                Component.literal("Upper Slope")
        );
        VineyardSurveyRecord.capture(
                helper.getLevel(),
                firstCorner,
                TerroirEvaluator.inspect(helper.getLevel(), firstCorner)
        ).save(almanac);
        owner.setItemInHand(InteractionHand.MAIN_HAND, almanac);
        owner.setShiftKeyDown(true);
        BlockPos absoluteSecond = helper.absolutePos(secondGround);
        owner.gameMode.useItemOn(
                owner,
                helper.getLevel(),
                almanac,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atCenterOf(absoluteSecond),
                        Direction.UP,
                        absoluteSecond,
                        false
                )
        );
        owner.setShiftKeyDown(false);

        var plots = VineyardPlotSavedData.get(helper.getLevel())
                .plots(owner.getUUID());
        helper.assertValueEqual(
                plots.size(),
                1,
                "Completing two corners should register one named plot"
        );
        VineyardPlot plot = plots.getFirst();
        helper.assertValueEqual(
                plot.name(),
                "Upper Slope",
                "The renamed Almanac should become the plot name"
        );
        helper.assertValueEqual(
                plot.area(),
                25,
                "Inclusive plot corners should produce the expected area"
        );
        helper.assertTrue(
                VineyardSurveyRecord.read(almanac).isEmpty(),
                "Completing a plot should clear the corner bookmark"
        );
        helper.assertTrue(
                EstateLedgerSavedData.get(helper.getLevel())
                        .entries(owner.getUUID())
                        .stream()
                        .anyMatch(event -> event.eventType()
                                == LedgerEventType.PLOT_REGISTERED
                                && event.detail().equals("Upper Slope")
                                && event.amount() == 25),
                "Registering a plot should record its name and area"
        );

        BlockPos redPos = new BlockPos(2, 1, 2);
        BlockPos whitePos = new BlockPos(4, 1, 3);
        helper.setBlock(redPos.below(), ModBlocks.VINEYARD_SOIL);
        helper.setBlock(whitePos.below(), ModBlocks.VINEYARD_SOIL);
        helper.setBlock(
                redPos,
                ModBlocks.RED_GRAPEVINE.defaultBlockState()
                        .setValue(GrapevineBlock.UPPER, false)
                        .setValue(
                                GrapevineBlock.AGE,
                                GrapevineBlock.MAX_AGE
                        )
        );
        helper.setBlock(
                whitePos,
                ModBlocks.WHITE_GRAPEVINE.defaultBlockState()
                        .setValue(GrapevineBlock.UPPER, false)
                        .setValue(
                                GrapevineBlock.AGE,
                                GrapevineBlock.MAX_AGE
                        )
        );

        VineyardPlotReport report = VineyardPlotReport.analyze(
                helper.getLevel(),
                plot
        );
        helper.assertValueEqual(
                report.vineCount(),
                2,
                "The plot report should count root vines inside its boundary"
        );
        helper.assertValueEqual(
                report.redVines(),
                1,
                "The plot report should count red vines"
        );
        helper.assertValueEqual(
                report.whiteVines(),
                1,
                "The plot report should count white vines"
        );
        helper.assertValueEqual(
                report.varietySummary(),
                "Mixed",
                "A red and white planting should report a mixed plot"
        );
        helper.assertTrue(
                report.projectedYield() > 0,
                "Mature vines should contribute to projected yield"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void namedPlotRecognizesPhysicalIrrigation(
            GameTestHelper helper
    ) {
        VineyardPlot plot = new VineyardPlot(
                java.util.UUID.randomUUID().toString(),
                "Water Meadow",
                helper.getLevel().dimension().identifier().toString(),
                helper.absolutePos(new BlockPos(1, 1, 1)).getX(),
                helper.absolutePos(new BlockPos(1, 1, 1)).getZ(),
                helper.absolutePos(new BlockPos(8, 1, 2)).getX(),
                helper.absolutePos(new BlockPos(8, 1, 2)).getZ(),
                helper.absolutePos(new BlockPos(1, 1, 1)).getY(),
                0L
        );
        for (int x : new int[]{1, 3, 5, 8}) {
            BlockPos vinePos = new BlockPos(x, 1, 1);
            helper.setBlock(vinePos.below(), ModBlocks.VINEYARD_SOIL);
            helper.setBlock(
                    vinePos,
                    ModBlocks.RED_GRAPEVINE.defaultBlockState()
                            .setValue(GrapevineBlock.UPPER, false)
            );
        }
        helper.setBlock(new BlockPos(3, 0, 2), Blocks.WATER);

        VineyardPlotReport irrigated = VineyardPlotReport.analyze(
                helper.getLevel(),
                plot
        );
        helper.assertValueEqual(
                irrigated.irrigatedVines(),
                3,
                "One physical channel should cover three of four vines"
        );
        helper.assertValueEqual(
                irrigated.irrigationPercent(),
                75,
                "Plot irrigation should report exact coverage"
        );
        helper.assertTrue(
                irrigated.hasImprovedIrrigation(),
                "Four vines at 75 percent coverage should qualify"
        );

        helper.setBlock(new BlockPos(3, 0, 2), Blocks.AIR);
        VineyardPlotReport dry = VineyardPlotReport.analyze(
                helper.getLevel(),
                plot
        );
        helper.assertValueEqual(
                dry.irrigatedVines(),
                0,
                "Removing the channel should remove irrigation coverage"
        );
        helper.assertFalse(
                dry.hasImprovedIrrigation(),
                "Improved irrigation must remain a physical upgrade"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void estateLedgerAggregatesAndBoundsHistory(
            GameTestHelper helper
    ) {
        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        EstateSavedData.get(helper.getLevel()).register(
                owner,
                helper.getLevel(),
                helper.absolutePos(new BlockPos(1, 1, 1)),
                "Stone Hill Estate",
                DyeColor.BLUE
        );
        ItemStack wine = new ItemStack(ModItems.AGED_RED_WINE);
        WineMetadata.applyProfile(
                wine,
                8,
                WineQualityProfile.vineyard(82)
        );
        WineMetadata.ensureBatchIdentity(wine, 8675309L);

        EstateLedgerSavedData ledger =
                EstateLedgerSavedData.get(helper.getLevel());
        ledger.recordWine(
                owner,
                LedgerEventType.BOTTLING,
                wine,
                1
        );
        ledger.recordWine(
                owner,
                LedgerEventType.BOTTLING,
                wine,
                1
        );

        List<EstateLedgerEvent> aggregated =
                ledger.entries(owner.getUUID());
        helper.assertValueEqual(
                aggregated.size(),
                1,
                "Identical same-day events should aggregate"
        );
        helper.assertValueEqual(
                aggregated.getFirst().amount(),
                2,
                "Aggregated bottling should retain the total amount"
        );
        helper.assertValueEqual(
                ledger.bestVintage(owner.getUUID()).batchId(),
                WineMetadata.batchId(wine),
                "The ledger should identify the best recorded vintage"
        );

        for (int index = 0;
                index < EstateLedgerSavedData.MAX_EVENTS_PER_ESTATE + 4;
                index++) {
            ledger.record(
                    owner,
                    LedgerEventType.PLOT_UPDATED,
                    "Plot " + index,
                    1,
                    0L,
                    0
            );
        }
        List<EstateLedgerEvent> bounded = ledger.entries(owner.getUUID());
        helper.assertValueEqual(
                bounded.size(),
                EstateLedgerSavedData.MAX_EVENTS_PER_ESTATE,
                "The estate ledger should discard only its oldest events"
        );
        helper.assertValueEqual(
                bounded.getFirst().detail(),
                "Plot "
                        + (EstateLedgerSavedData.MAX_EVENTS_PER_ESTATE + 3),
                "Ledger review should present the newest event first"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void physicalEstateFacilitiesAreRecognized(
            GameTestHelper helper
    ) {
        BlockPos secondStand = FIRST.east(2);
        helper.setBlock(FIRST, ModBlocks.BARREL_STAND);
        helper.setBlock(FIRST.above(), ModBlocks.AGING_BARREL);
        helper.setBlock(secondStand, ModBlocks.BARREL_STAND);
        helper.setBlock(secondStand.above(), ModBlocks.AGING_BARREL);
        helper.setBlock(FIRST.south(2), ModBlocks.WINE_RACK);
        helper.setBlock(FIRST.south(3), ModBlocks.WINE_CRATE);
        helper.setBlock(FIRST.south(4), ModBlocks.LABELLED_CELLAR_SHELF);
        helper.setBlock(FIRST.south(5), ModBlocks.TASTING_CABINET);
        helper.setBlock(secondStand.south(5), ModBlocks.VINTAGE_ARCHIVE);

        EstateInfrastructureReport report =
                EstateInfrastructureReport.survey(
                        helper.getLevel(),
                        helper.absolutePos(FIRST)
                );

        helper.assertTrue(
                report.hasBarrelWorkshop(),
                "Two mounted aging barrels should form a barrel workshop"
        );
        helper.assertTrue(
                report.hasWarehouse(),
                "Four physical storage fixtures should form a warehouse"
        );
        helper.assertTrue(
                report.hasTastingRoom(),
                "A tasting cabinet and archive should form a tasting room"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void physicalCellarUpgradesAffectAgeingQuality(
            GameTestHelper helper
    ) {
        helper.assertValueEqual(
                EstateInfrastructureReport.ageingContribution(
                        CellarRating.IDEAL,
                        true
                ),
                3,
                "An ideal cellar and barrel stand should stack their benefits"
        );
        helper.assertValueEqual(
                EstateInfrastructureReport.ageingContribution(
                        CellarRating.GOOD,
                        true
                ),
                2,
                "A mounted barrel in a good cellar should gain two quality"
        );
        helper.assertValueEqual(
                EstateInfrastructureReport.ageingContribution(
                        CellarRating.POOR,
                        false
                ),
                0,
                "An unmounted barrel should preserve legacy ageing quality"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void mountedBarrelLocksFacilityBonusAtCompletion(
            GameTestHelper helper
    ) {
        BlockPos barrelPos = FIRST.above();
        helper.setBlock(FIRST, ModBlocks.BARREL_STAND);
        helper.setBlock(barrelPos, ModBlocks.AGING_BARREL);
        AgingBarrelBlockEntity barrel = helper.getBlockEntity(
                barrelPos,
                AgingBarrelBlockEntity.class
        );
        ItemStack wine = new ItemStack(ModItems.RED_WINE);
        WineMetadata.apply(wine, 3, WineQuality.TABLE);
        for (int bottle = 0;
                bottle < AgingBarrelBlockEntity.CAPACITY;
                bottle++) {
            helper.assertTrue(
                    barrel.insertOne(wine),
                    "A mounted barrel should accept a matching full batch"
            );
        }

        for (int tick = 0;
                tick < AgingBarrelBlockEntity.AGING_TIME;
                tick++) {
            AgingBarrelBlockEntity.serverTick(
                    helper.getLevel(),
                    helper.absolutePos(barrelPos),
                    helper.getBlockState(barrelPos),
                    barrel
            );
        }

        int expectedFacility =
                EstateInfrastructureReport.ageingContribution(
                        CellarConditions.evaluate(
                                helper.getLevel(),
                                helper.absolutePos(barrelPos)
                        ).rating(),
                        true
                );
        helper.assertValueEqual(
                barrel.getFacilityContribution(),
                expectedFacility,
                "The completed batch should lock in its physical facility bonus"
        );
        AgingBarrelBlockEntity restored =
                (AgingBarrelBlockEntity) reload(helper, barrel);
        helper.assertValueEqual(
                restored.getFacilityContribution(),
                expectedFacility,
                "The locked facility bonus should survive save/load"
        );
        ItemStack aged = restored.takeOneAgedWine();
        helper.assertValueEqual(
                WineMetadata.qualityProfile(aged).ageing(),
                AgingVessel.OAK.qualityContribution(1)
                        + expectedFacility,
                "The locked facility bonus should reach bottle metadata"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void estateReputationUsesVerifiedAchievements(
            GameTestHelper helper
    ) {
        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        EstateSavedData.get(helper.getLevel()).register(
                owner,
                helper.getLevel(),
                helper.absolutePos(FIRST),
                "Stone Hill Estate",
                DyeColor.BLUE
        );
        EstateLedgerSavedData ledger =
                EstateLedgerSavedData.get(helper.getLevel());
        ledger.record(
                owner,
                LedgerEventType.PLOT_REGISTERED,
                "Upper Slope",
                64,
                0L,
                0
        );
        ledger.record(
                owner,
                LedgerEventType.PLOT_REGISTERED,
                "Lower Terrace",
                64,
                0L,
                0
        );
        ledger.record(
                owner,
                LedgerEventType.PLANTING,
                "red",
                160,
                0L,
                0
        );
        ledger.record(
                owner,
                LedgerEventType.HARVEST,
                "red",
                320,
                0L,
                0
        );
        ItemStack wine = new ItemStack(ModItems.AGED_RED_WINE);
        WineMetadata.applyProfile(
                wine,
                9,
                new WineQualityProfile(20, 60, 4, 3, 5, 0)
        );
        WineMetadata.ensureBatchIdentity(wine, 711L);
        ledger.recordWine(owner, LedgerEventType.BOTTLING, wine, 4);
        ledger.recordWine(owner, LedgerEventType.ARCHIVED, wine, 1);

        EstateReputationSavedData reputationData =
                EstateReputationSavedData.get(helper.getLevel());
        EstateReputationProfile earned = reputationData.profile(
                owner.getUUID()
        );
        helper.assertValueEqual(
                earned.score(),
                100,
                "Verified vineyard and vintage achievements should score"
        );
        helper.assertValueEqual(
                earned.tier(),
                EstateReputationTier.RESPECTED,
                "One exceptional first vintage should establish respect"
        );

        ledger.recordWine(owner, LedgerEventType.BOTTLING, wine, 1);
        helper.assertValueEqual(
                reputationData.profile(owner.getUUID()).score(),
                100,
                "Repeated bottles from one batch must not farm reputation"
        );

        EstateInfrastructureReport completeFacilities =
                new EstateInfrastructureReport(2, 2, 2, 4, 1, 1);
        EstateReputationProfile upgraded =
                reputationData.recordInfrastructure(
                        owner.getUUID(),
                        completeFacilities
                );
        helper.assertValueEqual(
                upgraded.score(),
                140,
                "Four physical facilities should add forty reputation"
        );
        helper.assertValueEqual(
                upgraded.tier(),
                EstateReputationTier.RENOWNED,
                "A complete working estate should become renowned"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void estateReputationBatchEvidenceIsBounded(
            GameTestHelper helper
    ) {
        String ownerId = java.util.UUID.randomUUID().toString();
        List<EstateLedgerEvent> events = new ArrayList<>();
        for (int batch = 1;
                batch <= EstateReputationProfile.MAX_TRACKED_BATCHES + 8;
                batch++) {
            events.addFirst(new EstateLedgerEvent(
                    ownerId,
                    LedgerEventType.BOTTLING.serializedName(),
                    batch,
                    "Batch " + batch,
                    4,
                    batch,
                    80
            ));
        }
        EstateReputationProfile profile =
                EstateReputationProfile.empty(ownerId).sync(events);

        helper.assertValueEqual(
                profile.bottledBatches().size(),
                EstateReputationProfile.MAX_TRACKED_BATCHES,
                "Reputation evidence should remain bounded per estate"
        );
        helper.assertValueEqual(
                profile.bottledBatches().getFirst(),
                9L,
                "Bounded evidence should retain the newest batch identities"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void estateReputationOutlivesTheLedgerWindow(
            GameTestHelper helper
    ) {
        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        EstateSavedData.get(helper.getLevel()).register(
                owner,
                helper.getLevel(),
                helper.absolutePos(FIRST),
                "Stone Hill Estate",
                DyeColor.BLUE
        );
        EstateLedgerSavedData ledger =
                EstateLedgerSavedData.get(helper.getLevel());
        for (int vine = 0; vine < 160; vine++) {
            ledger.record(
                    owner,
                    LedgerEventType.PLANTING,
                    "Vine " + vine,
                    1,
                    0L,
                    0
            );
        }

        helper.assertValueEqual(
                ledger.entries(owner.getUUID()).size(),
                EstateLedgerSavedData.MAX_EVENTS_PER_ESTATE,
                "The operating ledger should remain bounded"
        );
        EstateReputationProfile reputation =
                EstateReputationSavedData.get(helper.getLevel())
                        .profile(owner.getUUID());
        helper.assertValueEqual(
                reputation.plantedVines(),
                160,
                "Persistent reputation counters must outlive ledger trimming"
        );
        helper.assertValueEqual(
                reputation.score(),
                25,
                "One hundred sixty plantings should earn the capped 20 points"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void provenanceSurvivesTheFullWinemakingPath(
            GameTestHelper helper
    ) {
        BlockPos pressPos = new BlockPos(1, 1, 1);
        BlockPos fermentationPos = new BlockPos(3, 1, 1);
        BlockPos agingPos = new BlockPos(5, 1, 1);
        helper.setBlock(pressPos, ModBlocks.GRAPE_PRESS);
        helper.setBlock(
                fermentationPos,
                ModBlocks.FERMENTATION_BARREL
        );
        helper.setBlock(agingPos, ModBlocks.AGING_BARREL);

        ItemStack grapes = new ItemStack(
                ModItems.RED_GRAPES,
                GrapePressBlockEntity.GRAPES_PER_PRESS
        );
        WineMetadata.applyProfile(
                grapes,
                4,
                WineQualityProfile.vineyard(50)
        );
        WineVintageConditions vintageConditions =
                WineVintageConditions.harvested(
                        SeasonalContext.atDay(20L, 8),
                        VineyardWeatherEvent.COOL_RIPENING,
                        true,
                        true
                );
        WineMetadata.applyVintageConditions(
                grapes,
                vintageConditions
        );

        GrapePressBlockEntity press = helper.getBlockEntity(
                pressPos,
                GrapePressBlockEntity.class
        );
        press.insert(grapes, grapes.getCount());
        ServerPlayer producer =
                helper.makeMockServerPlayerInLevel();
        helper.assertTrue(
                press.press(producer),
                "Grapes should press"
        );
        ItemStack must = press.bottleOneMust();
        WineProvenance provenance =
                WineMetadata.provenance(must);
        helper.assertTrue(
                provenance.known(),
                "Pressing should establish batch provenance"
        );
        helper.assertValueEqual(
                provenance.variety(),
                GrapeCultivar.EMBER_NOIR.serializedName(),
                "Batch provenance should identify the grape variety"
        );
        helper.assertValueEqual(
                provenance.producerName(),
                producer.getGameProfile().name(),
                "Batch provenance should identify the producer"
        );
        helper.assertValueEqual(
                provenance.vintageConditions(),
                vintageConditions,
                "Pressing should preserve native vintage conditions"
        );

        FermentationBarrelBlockEntity fermentation =
                helper.getBlockEntity(
                        fermentationPos,
                        FermentationBarrelBlockEntity.class
                );
        for (int bottle = 0;
             bottle < FermentationBarrelBlockEntity.CAPACITY;
             bottle++) {
            fermentation.insertOne(must);
        }
        for (int tick = 0;
             tick < FermentationBarrelBlockEntity.FERMENTATION_TIME;
             tick++) {
            FermentationBarrelBlockEntity.serverTick(
                    helper.getLevel(),
                    helper.absolutePos(fermentationPos),
                    helper.getBlockState(fermentationPos),
                    fermentation
            );
        }
        ItemStack wine = fermentation.takeOneWine();
        helper.assertValueEqual(
                WineMetadata.provenance(wine),
                provenance,
                "Fermentation should preserve batch provenance"
        );

        AgingBarrelBlockEntity aging = helper.getBlockEntity(
                agingPos,
                AgingBarrelBlockEntity.class
        );
        for (int bottle = 0;
             bottle < AgingBarrelBlockEntity.CAPACITY;
             bottle++) {
            aging.insertOne(wine);
        }
        for (int tick = 0;
             tick < AgingBarrelBlockEntity.AGING_TIME;
             tick++) {
            AgingBarrelBlockEntity.serverTick(
                    helper.getLevel(),
                    helper.absolutePos(agingPos),
                    helper.getBlockState(agingPos),
                    aging
            );
        }
        ItemStack agedWine = aging.takeOneAgedWine();
        helper.assertValueEqual(
                WineMetadata.provenance(agedWine),
                provenance,
                "Barrel ageing should preserve batch provenance"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void matchingGrapesFromDifferentVinesCanStack(
            GameTestHelper helper
    ) {
        ItemStack first = new ItemStack(ModItems.RED_GRAPES, 3);
        ItemStack second = new ItemStack(ModItems.RED_GRAPES, 4);
        WineQualityProfile profile =
                WineQualityProfile.vineyard(55);

        WineMetadata.applyProfile(first, 2, profile);
        WineMetadata.applyProfile(second, 2, profile);
        WineVintageConditions conditions =
                WineVintageConditions.harvested(
                        SeasonalContext.atDay(9L, 8),
                        VineyardWeatherEvent.CALM,
                        false
                );
        WineMetadata.applyVintageConditions(first, conditions);
        WineMetadata.applyVintageConditions(second, conditions);

        helper.assertTrue(
                ItemStack.isSameItemSameComponents(first, second),
                "Grapes harvested under matching conditions must stack"
        );
        helper.assertTrue(
                !WineMetadata.provenance(first).known()
                        && !WineMetadata.provenance(second).known(),
                "Provenance should begin when a batch is pressed"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void vintageArchiveCataloguesUniqueBatches(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.VINTAGE_ARCHIVE);
        VintageArchiveBlockEntity archive = helper.getBlockEntity(
                FIRST,
                VintageArchiveBlockEntity.class
        );
        ItemStack first = new ItemStack(ModItems.RED_WINE);
        WineMetadata.apply(first, 3, WineQuality.FINE);
        WineMetadata.ensureBatchIdentity(first, 1001L);
        WineMetadata.applyProvenance(
                first,
                new WineProvenance(
                        "red",
                        72000L,
                        "minecraft:overworld",
                        1,
                        2,
                        3,
                        "",
                        "Archivist"
                )
        );

        helper.assertValueEqual(
                archive.record(first),
                VintageArchiveBlockEntity.RecordResult.ADDED,
                "The first batch should create an archive record"
        );
        WineMetadata.ageBottle(
                first,
                WineAgeStage.PEAK_AT,
                CellarRating.IDEAL
        );
        helper.assertValueEqual(
                archive.record(first),
                VintageArchiveBlockEntity.RecordResult.UPDATED,
                "Scanning the same batch should update its snapshot"
        );

        ItemStack second = new ItemStack(ModItems.WHITE_WINE);
        WineMetadata.apply(second, 4, WineQuality.GOOD);
        WineMetadata.ensureBatchIdentity(second, 1002L);
        helper.assertValueEqual(
                archive.record(second),
                VintageArchiveBlockEntity.RecordResult.ADDED,
                "A different batch should create a second record"
        );
        helper.assertValueEqual(
                archive.getRecordCount(),
                2,
                "The archive should count unique batches"
        );
        helper.assertValueEqual(
                WineReadiness.from(archive.getRecordCopy(0)),
                WineReadiness.DRINK_NOW,
                "An updated peak record should be marked drink now"
        );

        VintageArchiveBlockEntity restored =
                (VintageArchiveBlockEntity) reload(helper, archive);
        helper.assertValueEqual(
                restored.getRecordCount(),
                2,
                "Archive records should survive save and load"
        );
        helper.assertValueEqual(
                WineMetadata.provenance(
                        restored.getRecordCopy(0)
                ).producerName(),
                "Archivist",
                "Archive serialization should preserve provenance"
        );

        ItemStack archiveDrop = Block.getDrops(
                helper.getBlockState(FIRST),
                helper.getLevel(),
                helper.absolutePos(FIRST),
                restored
        ).stream().findFirst().orElse(ItemStack.EMPTY);
        helper.assertTrue(
                archiveDrop.has(DataComponents.BLOCK_ENTITY_DATA),
                "A filled archive drop should retain its catalogue"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void cellarHumidityRequiresNearbyWater(
            GameTestHelper helper
    ) {
        for (Direction direction : Direction.values()) {
            helper.setBlock(
                    FIRST.relative(direction),
                    Blocks.AIR
            );
        }

        CellarConditions dryConditions = CellarConditions.evaluate(
                helper.getLevel(),
                helper.absolutePos(FIRST)
        );

        helper.assertFalse(
                dryConditions.humid(),
                "A dry cellar must not receive the nearby-water bonus"
        );

        helper.setBlock(FIRST.east(), Blocks.WATER);

        CellarConditions humidConditions = CellarConditions.evaluate(
                helper.getLevel(),
                helper.absolutePos(FIRST)
        );

        helper.assertTrue(
                humidConditions.humid(),
                "A water block beside the rack should count as humidity"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void wineRackCatchesUpAfterChunkReload(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.WINE_RACK);
        WineRackBlockEntity rack = helper.getBlockEntity(
                FIRST,
                WineRackBlockEntity.class
        );
        ItemStack wine = new ItemStack(ModItems.RED_WINE);
        WineMetadata.apply(wine, 6, WineQuality.FINE);

        helper.assertTrue(
                rack.insertOne(wine),
                "The rack should accept the catch-up test bottle"
        );

        long elapsedTicks = 24000L;
        WineRackBlockEntity restored = reloadRackWithElapsedTime(
                helper,
                rack,
                elapsedTicks
        );

        for (int tick = 0; tick < 20; tick++) {
            WineRackBlockEntity.serverTick(
                    helper.getLevel(),
                    helper.absolutePos(FIRST),
                    helper.getBlockState(FIRST),
                    restored
            );
        }

        CellarConditions conditions = CellarConditions.evaluate(
                helper.getLevel(),
                helper.absolutePos(FIRST)
        );
        long expectedMinimum = Math.round(
                elapsedTicks * conditions.rating().ageRate()
        );

        helper.assertTrue(
                WineMetadata.bottleAge(
                        restored.getBottleCopy(0)
                ) >= expectedMinimum,
                "A reloaded rack should catch up for unloaded world time"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void idealCellarRulesAndAdvancementAreRegistered(
            GameTestHelper helper
    ) {
        helper.assertValueEqual(
                CellarConditions.ratingFor(
                        true,
                        true,
                        true,
                        true,
                        false
                ),
                com.zenith.vintner.wine.CellarRating.IDEAL,
                "All four cellar protections should be ideal"
        );
        helper.assertValueEqual(
                CellarConditions.ratingFor(
                        true,
                        true,
                        true,
                        true,
                        true
                ),
                com.zenith.vintner.wine.CellarRating.BASIC,
                "A heat source should prevent an ideal cellar"
        );

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        com.zenith.vintner.advancement.ModAdvancements
                .grantIdealCellar(player);

        AdvancementHolder advancement = helper.getLevel()
                .getServer()
                .getAdvancements()
                .get(
                        Identifier.fromNamespaceAndPath(
                                "vintner",
                                "vintner/ideal_cellar"
                        )
                );

        helper.assertTrue(
                advancement != null,
                "The ideal-cellar advancement should load"
        );
        helper.assertTrue(
                player.getAdvancements()
                        .getOrStartProgress(advancement)
                        .isDone(),
                "The ideal-cellar grant should award progress"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void bottleAgeChangesWineBenefit(
            GameTestHelper helper
    ) {
        var peakTaster = helper.makeMockServerPlayerInLevel();
        WineConsumptionManager.consume(
                helper.getLevel(),
                peakTaster,
                WineEffectProfile.RED,
                WineQuality.TABLE,
                WineAgeStage.PEAK
        );

        helper.assertValueEqual(
                WineEffectProfile.RED.remainingDuration(peakTaster),
                500,
                "Peak wine should provide a 25 percent longer benefit"
        );

        var spoiledTaster = helper.makeMockServerPlayerInLevel();
        var result = WineConsumptionManager.consume(
                helper.getLevel(),
                spoiledTaster,
                WineEffectProfile.WHITE,
                WineQuality.TABLE,
                WineAgeStage.SPOILED
        );

        helper.assertTrue(
                result.impaired(),
                "Spoiled wine should cause impairment"
        );
        helper.assertTrue(
                spoiledTaster.hasEffect(MobEffects.NAUSEA),
                "Spoiled wine should cause nausea"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void villageChestsCanSupplyBothGrapeVarieties(
            GameTestHelper helper
    ) {
        LootTable villageLoot = helper.getLevel()
                .getServer()
                .reloadableRegistries()
                .getLootTable(BuiltInLootTables.VILLAGE_PLAINS_HOUSE);

        LootParams params = new LootParams.Builder(helper.getLevel())
                .withParameter(
                        LootContextParams.ORIGIN,
                        Vec3.atCenterOf(helper.absolutePos(FIRST))
                )
                .create(LootContextParamSets.CHEST);

        boolean foundRed = false;
        boolean foundWhite = false;

        for (long seed = 0;
             seed < 256 && (!foundRed || !foundWhite);
             seed++) {
            for (ItemStack stack : villageLoot.getRandomItems(
                    params,
                    RandomSource.create(seed)
            )) {
                foundRed |= stack.is(ModItems.RED_GRAPE_CUTTING);
                foundWhite |= stack.is(ModItems.WHITE_GRAPE_CUTTING);

                if (stack.is(ModItems.RED_GRAPE_CUTTING)
                        || stack.is(ModItems.WHITE_GRAPE_CUTTING)) {
                    helper.assertTrue(
                            stack.getCount() >= 1
                                    && stack.getCount() <= 2,
                            "Village loot should contain one or two cuttings"
                    );
                }
            }
        }

        helper.assertTrue(
                foundRed,
                "Village house loot should contain red grape cuttings"
        );
        helper.assertTrue(
                foundWhite,
                "Village house loot should contain white grape cuttings"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void grapeCuttingsPlantButHarvestedFruitDoesNot(
            GameTestHelper helper
    ) {
        BlockPos fruitTrellis = new BlockPos(2, 1, 2);
        BlockPos cuttingTrellis = new BlockPos(4, 1, 2);
        var player = helper.makeMockServerPlayer(GameType.SURVIVAL);

        helper.setBlock(fruitTrellis, ModBlocks.OAK_TRELLIS);
        helper.setBlock(cuttingTrellis, ModBlocks.OAK_TRELLIS);

        helper.placeAt(
                player,
                new ItemStack(ModItems.RED_GRAPES),
                fruitTrellis.above(),
                net.minecraft.core.Direction.DOWN
        );
        helper.assertBlockPresent(ModBlocks.OAK_TRELLIS, fruitTrellis);

        helper.placeAt(
                player,
                new ItemStack(ModItems.RED_GRAPE_CUTTING),
                cuttingTrellis.above(),
                net.minecraft.core.Direction.DOWN
        );
        helper.assertBlockPresent(
                ModBlocks.RED_GRAPEVINE,
                cuttingTrellis
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void matureVinesCanBePrunedForRenewableCuttings(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayer(GameType.SURVIVAL);
        ItemStack shears = new ItemStack(Items.SHEARS);

        helper.setBlock(FIRST, matureLowerVine());
        helper.setBlock(UPPER, matureUpperVine());
        player.setItemInHand(
                net.minecraft.world.InteractionHand.MAIN_HAND,
                shears
        );
        helper.useBlock(UPPER, player);

        helper.assertBlockProperty(FIRST, GrapevineBlock.AGE, 2);
        helper.assertBlockProperty(UPPER, GrapevineBlock.AGE, 2);
        helper.assertItemEntityPresent(
                ModItems.RED_GRAPE_CUTTING,
                UPPER,
                2.0
        );
        helper.assertValueEqual(
                shears.getDamageValue(),
                1,
                "Pruning should use one point of shears durability"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void matureUpperVinesHarvestAndReturnToRegrowth(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayer(GameType.SURVIVAL);

        helper.setBlock(FIRST, matureLowerVine());
        helper.setBlock(UPPER, matureUpperVine());
        helper.useBlock(UPPER, player);

        helper.assertBlockProperty(FIRST, GrapevineBlock.AGE, 2);
        helper.assertBlockProperty(UPPER, GrapevineBlock.AGE, 2);
        helper.assertItemEntityPresent(
                ModItems.RED_GRAPES,
                UPPER,
                2.0
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void qualityTiersCoverRoadmapScoreBands(
            GameTestHelper helper
    ) {
        helper.assertValueEqual(
                WineQuality.fromScore(0),
                WineQuality.ROUGH,
                "Score zero should produce rough wine"
        );
        helper.assertValueEqual(
                WineQuality.fromScore(30),
                WineQuality.TABLE,
                "Score thirty should produce table wine"
        );
        helper.assertValueEqual(
                WineQuality.fromScore(45),
                WineQuality.GOOD,
                "Score forty-five should produce good wine"
        );
        helper.assertValueEqual(
                WineQuality.fromScore(60),
                WineQuality.FINE,
                "Score sixty should produce fine wine"
        );
        helper.assertValueEqual(
                WineQuality.fromScore(75),
                WineQuality.EXCEPTIONAL,
                "Score seventy-five should produce exceptional wine"
        );
        helper.assertValueEqual(
                WineQuality.fromScore(90),
                WineQuality.LEGENDARY,
                "Score ninety should produce legendary wine"
        );
        helper.assertValueEqual(
                WineQuality.ROUGH.durationMultiplier(),
                0.75F,
                "Rough wine should have reduced benefit duration"
        );
        helper.assertValueEqual(
                WineQuality.LEGENDARY.durationMultiplier(),
                1.75F,
                "Legendary wine should have the longest duration"
        );
        helper.assertValueEqual(
                WineQuality.LEGENDARY.signatureEffectAmplifier(),
                2,
                "Legendary wine should grant signature effect level III"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void vineyardQualityUsesAllPhaseThreeInputs(
            GameTestHelper helper
    ) {
        helper.assertValueEqual(
                GrapeQualityEvaluator.score(
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true
                ),
                60,
                "An ideal mature, managed, ripe, dry harvest should score sixty"
        );
        helper.assertValueEqual(
                GrapeQualityEvaluator.score(
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false
                ),
                0,
                "Poor site, vine, yield, ripeness, and weather should score zero"
        );
        helper.assertTrue(
                GrapeQualityEvaluator.score(
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        true,
                        false
                ) < 60,
                "Wet harvest weather should reduce an otherwise ideal score"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void legacyQualityIdsRemainReadable(
            GameTestHelper helper
    ) {
        helper.assertValueEqual(
                WineQuality.byId(0),
                WineQuality.TABLE,
                "Legacy common quality ID should migrate to table"
        );
        helper.assertValueEqual(
                WineQuality.byId(1),
                WineQuality.FINE,
                "Legacy fine quality ID should remain fine"
        );
        helper.assertValueEqual(
                WineQuality.byId(2),
                WineQuality.EXCEPTIONAL,
                "Legacy exceptional quality ID should remain exceptional"
        );
        ItemStack legacyBottle = new ItemStack(ModItems.RED_WINE);
        CompoundTag legacyTag = new CompoundTag();
        legacyTag.putInt("VintnerVintage", 8);
        legacyTag.putInt("VintnerQuality", 2);
        legacyBottle.set(
                DataComponents.CUSTOM_DATA,
                CustomData.of(legacyTag)
        );
        helper.assertValueEqual(
                WineMetadata.quality(legacyBottle),
                WineQuality.EXCEPTIONAL,
                "A legacy bottle without a profile should retain its tier"
        );
        helper.assertValueEqual(
                WineMetadata.qualityScore(legacyBottle),
                WineQuality.EXCEPTIONAL.baselineScore(),
                "A legacy bottle should receive a stable baseline score"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void qualityProfileAccumulatesThroughWinemaking(
            GameTestHelper helper
    ) {
        BlockPos pressPos = new BlockPos(1, 1, 1);
        BlockPos fermentationPos = new BlockPos(3, 1, 1);
        BlockPos agingPos = new BlockPos(5, 1, 1);
        helper.setBlock(pressPos, ModBlocks.GRAPE_PRESS);
        helper.setBlock(
                fermentationPos,
                ModBlocks.FERMENTATION_BARREL
        );
        helper.setBlock(agingPos, ModBlocks.AGING_BARREL);

        ItemStack grapes = new ItemStack(
                ModItems.RED_GRAPES,
                GrapePressBlockEntity.GRAPES_PER_PRESS
        );
        WineMetadata.applyProfile(
                grapes,
                21,
                WineQualityProfile.vineyard(60)
        );

        GrapePressBlockEntity press = helper.getBlockEntity(
                pressPos,
                GrapePressBlockEntity.class
        );
        press.insert(grapes, grapes.getCount());
        helper.assertTrue(press.press(), "Grapes should press");
        ItemStack must = press.bottleOneMust();
        helper.assertValueEqual(
                WineMetadata.qualityScore(must),
                65,
                "Controlled pressing should add five quality points"
        );

        FermentationBarrelBlockEntity fermentation =
                helper.getBlockEntity(
                        fermentationPos,
                        FermentationBarrelBlockEntity.class
                );
        for (int bottle = 0;
             bottle < FermentationBarrelBlockEntity.CAPACITY;
             bottle++) {
            helper.assertTrue(
                    fermentation.insertOne(must),
                    "Scored must should fill fermentation"
            );
        }
        for (int tick = 0;
             tick < FermentationBarrelBlockEntity.FERMENTATION_TIME;
             tick++) {
            FermentationBarrelBlockEntity.serverTick(
                    helper.getLevel(),
                    helper.absolutePos(fermentationPos),
                    helper.getBlockState(fermentationPos),
                    fermentation
            );
        }
        ItemStack wine = fermentation.takeOneWine();
        helper.assertValueEqual(
                WineMetadata.qualityScore(wine),
                70,
                "Controlled fermentation should add five points"
        );

        AgingBarrelBlockEntity aging = helper.getBlockEntity(
                agingPos,
                AgingBarrelBlockEntity.class
        );
        for (int bottle = 0;
             bottle < AgingBarrelBlockEntity.CAPACITY;
             bottle++) {
            helper.assertTrue(
                    aging.insertOne(wine),
                    "Scored wine should fill barrel ageing"
            );
        }
        for (int tick = 0;
             tick < AgingBarrelBlockEntity.AGING_TIME;
             tick++) {
            AgingBarrelBlockEntity.serverTick(
                    helper.getLevel(),
                    helper.absolutePos(agingPos),
                    helper.getBlockState(agingPos),
                    aging
            );
        }
        ItemStack agedWine = aging.takeOneAgedWine();
        helper.assertValueEqual(
                WineMetadata.qualityScore(agedWine),
                80,
                "Successful barrel ageing should add ten points"
        );
        helper.assertValueEqual(
                WineMetadata.quality(agedWine),
                WineQuality.EXCEPTIONAL,
                "The accumulated score should determine the final tier"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void almanacExplainsPlacedAgeingVessels(
            GameTestHelper helper
    ) {
        BlockPos barrelPos = new BlockPos(1, 1, 1);
        helper.setBlock(barrelPos, ModBlocks.CHESTNUT_AGING_BARREL);
        AgingBarrelBlockEntity barrel = helper.getBlockEntity(
                barrelPos,
                AgingBarrelBlockEntity.class
        );
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack almanac = new ItemStack(ModItems.VINTNER_ALMANAC);
        player.setItemInHand(InteractionHand.MAIN_HAND, almanac);
        BlockPos absoluteBarrel = helper.absolutePos(barrelPos);

        player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                almanac,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atCenterOf(absoluteBarrel),
                        Direction.NORTH,
                        absoluteBarrel,
                        false
                )
        );

        helper.assertValueEqual(
                almanac.getCount(),
                1,
                "Reading a vessel guide must not consume the Almanac"
        );
        helper.assertValueEqual(
                barrel.getBottleCount(),
                0,
                "Reading a vessel guide must not modify the barrel"
        );
        helper.assertValueEqual(
                AgingVessel.CHESTNUT.agingTimeSeconds(),
                75,
                "The displayed Chestnut ageing time should stay accurate"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void specialistAgeingVesselsHaveDistinctProfiles(
            GameTestHelper helper
    ) {
        BlockPos chestnutPos = new BlockPos(1, 1, 1);
        BlockPos neutralPos = new BlockPos(3, 1, 1);
        BlockPos caskPos = new BlockPos(5, 1, 1);
        helper.setBlock(chestnutPos, ModBlocks.CHESTNUT_AGING_BARREL);
        helper.setBlock(neutralPos, ModBlocks.NEUTRAL_AGING_BARREL);
        helper.setBlock(caskPos, ModBlocks.LARGE_CASK);

        AgingBarrelBlockEntity chestnut = helper.getBlockEntity(
                chestnutPos,
                AgingBarrelBlockEntity.class
        );
        AgingBarrelBlockEntity neutral = helper.getBlockEntity(
                neutralPos,
                AgingBarrelBlockEntity.class
        );
        AgingBarrelBlockEntity cask = helper.getBlockEntity(
                caskPos,
                AgingBarrelBlockEntity.class
        );

        helper.assertValueEqual(
                chestnut.getVessel(),
                AgingVessel.CHESTNUT,
                "The chestnut barrel should use its specialist profile"
        );
        helper.assertValueEqual(
                neutral.getVessel(),
                AgingVessel.NEUTRAL,
                "The neutral barrel should use its specialist profile"
        );
        helper.assertValueEqual(
                cask.getCapacity(),
                8,
                "The large cask should hold eight matching bottles"
        );
        helper.assertTrue(
                chestnut.getAgingTime() < neutral.getAgingTime()
                        && neutral.getAgingTime() < cask.getAgingTime(),
                "Vessel oxygen exposure should create distinct ageing speeds"
        );
        helper.assertTrue(
                AgingVessel.CHESTNUT.spoilageRiskPenalty()
                        > AgingVessel.NEUTRAL.spoilageRiskPenalty(),
                "Higher-exposure chestnut should carry more spoilage risk"
        );
        helper.assertTrue(
                AgingVessel.CHESTNUT.qualityContribution(1)
                        > AgingVessel.CHESTNUT.qualityContribution(2),
                "Chestnut should have a meaningful red-wine style affinity"
        );
        helper.assertTrue(
                !AgingVessel.OAK.tastingNote(true).equals(
                        AgingVessel.LARGE_CASK.tastingNote(true)
                ),
                "Vessel tannin and flavour should change tasting notes"
        );

        ItemStack bottle = new ItemStack(ModItems.RED_WINE);
        WineMetadata.applyProfile(
                bottle,
                12,
                new WineQualityProfile(0, 55, 5, 5, 0, 0)
        );
        WineMetadata.ensureBatchIdentity(bottle, 3012001L);
        for (int index = 0; index < cask.getCapacity(); index++) {
            helper.assertTrue(
                    cask.insertOne(bottle),
                    "The large cask should accept bottle " + (index + 1)
            );
        }
        for (int tick = 0; tick < cask.getAgingTime(); tick++) {
            AgingBarrelBlockEntity.serverTick(
                    helper.getLevel(),
                    helper.absolutePos(caskPos),
                    helper.getBlockState(caskPos),
                    cask
            );
        }
        ItemStack result = cask.takeOneAgedWine();
        helper.assertValueEqual(
                WineMetadata.agingVessel(result),
                AgingVessel.LARGE_CASK,
                "Finished wine should remember its ageing vessel"
        );
        helper.assertValueEqual(
                WineMetadata.qualityProfile(result).ageing(),
                AgingVessel.LARGE_CASK.qualityContribution(1),
                "The cask profile should contribute to final quality"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void cooperageKitsPreserveWoodAndConfigureEmptyBarrels(
            GameTestHelper helper
    ) {
        AgingVessel[] profiles = {
                AgingVessel.CHESTNUT,
                AgingVessel.NEUTRAL,
                AgingVessel.LARGE_CASK
        };
        Item[] kits = {
                ModItems.TOASTING_KIT,
                ModItems.SEASONING_KIT,
                ModItems.CASK_CONVERSION_KIT
        };
        Block barrelBlock = ModBlocks.agingBarrel(
                WoodVariant.CHERRY
        );
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        ItemStack mallet = new ItemStack(ModItems.COOPERS_MALLET);
        player.setItemInHand(InteractionHand.MAIN_HAND, mallet);

        for (int index = 0; index < profiles.length; index++) {
            BlockPos pos = new BlockPos(1 + index * 2, 1, 1);
            helper.setBlock(pos, barrelBlock);
            Item kitItem = kits[index];
            ItemStack kit = new ItemStack(kitItem);
            player.setItemInHand(InteractionHand.OFF_HAND, kit);
            BlockPos absolutePos = helper.absolutePos(pos);

            player.gameMode.useItemOn(
                    player,
                    helper.getLevel(),
                    mallet,
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(
                            Vec3.atCenterOf(absolutePos),
                            Direction.NORTH,
                            absolutePos,
                            false
                    )
            );

            helper.assertBlockPresent(barrelBlock, pos);
            helper.assertBlockProperty(
                    pos,
                    AgingBarrelBlock.VESSEL,
                    profiles[index]
            );
            AgingBarrelBlockEntity barrel = helper.getBlockEntity(
                    pos,
                    AgingBarrelBlockEntity.class
            );
            helper.assertValueEqual(
                    barrel.getVessel(),
                    profiles[index],
                    "The applied kit should control barrel behaviour"
            );
            helper.assertValueEqual(
                    kit.getCount(),
                    0,
                    "Applying a treatment should consume its kit"
            );
            helper.assertValueEqual(
                    mallet.getDamageValue(),
                    index + 1,
                    "Each treatment should use one mallet durability"
            );

            List<ItemStack> drops = Block.getDrops(
                    helper.getBlockState(pos),
                    helper.getLevel(),
                    absolutePos,
                    barrel
            );
            helper.assertTrue(
                    drops.stream().anyMatch(stack -> stack.is(
                            barrelBlock.asItem()
                    )),
                    "The upgraded barrel should retain its wood item"
            );
            helper.assertTrue(
                    drops.stream().anyMatch(stack -> stack.is(kitItem)),
                    "Breaking an upgraded barrel should return its kit"
            );
        }

        BlockPos toastedPos = new BlockPos(1, 1, 1);
        ItemStack replacementKit = new ItemStack(
                ModItems.SEASONING_KIT
        );
        player.setItemInHand(
                InteractionHand.OFF_HAND,
                replacementKit
        );
        BlockPos absoluteToastedPos = helper.absolutePos(toastedPos);
        player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                mallet,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atCenterOf(absoluteToastedPos),
                        Direction.NORTH,
                        absoluteToastedPos,
                        false
                )
        );
        helper.assertBlockProperty(
                toastedPos,
                AgingBarrelBlock.VESSEL,
                AgingVessel.CHESTNUT
        );
        helper.assertValueEqual(
                replacementKit.getCount(),
                1,
                "Refitting a treated barrel must not destroy either kit"
        );
        helper.assertValueEqual(
                mallet.getDamageValue(),
                profiles.length,
                "A rejected refit must not damage the mallet"
        );

        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void cooperageTreatmentCannotChangeMidBatch(
            GameTestHelper helper
    ) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.AGING_BARREL);
        AgingBarrelBlockEntity barrel = helper.getBlockEntity(
                pos,
                AgingBarrelBlockEntity.class
        );
        helper.assertTrue(
                barrel.insertOne(new ItemStack(ModItems.RED_WINE)),
                "The test barrel should accept its first bottle"
        );

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        ItemStack mallet = new ItemStack(ModItems.COOPERS_MALLET);
        ItemStack kit = new ItemStack(ModItems.TOASTING_KIT);
        player.setItemInHand(InteractionHand.MAIN_HAND, mallet);
        player.setItemInHand(InteractionHand.OFF_HAND, kit);
        BlockPos absolutePos = helper.absolutePos(pos);
        player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                mallet,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atCenterOf(absolutePos),
                        Direction.NORTH,
                        absolutePos,
                        false
                )
        );

        helper.assertBlockProperty(
                pos,
                AgingBarrelBlock.VESSEL,
                AgingVessel.OAK
        );
        helper.assertValueEqual(
                kit.getCount(),
                1,
                "A rejected treatment must not consume the kit"
        );
        helper.assertValueEqual(
                barrel.getBottleCount(),
                1,
                "A rejected treatment must not disturb the batch"
        );
        helper.assertValueEqual(
                mallet.getDamageValue(),
                0,
                "A rejected treatment must not damage the mallet"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void coopersMalletRemovesTreatmentsFromEmptyBarrels(
            GameTestHelper helper
    ) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.agingBarrel(WoodVariant.OAK));
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        ItemStack mallet = new ItemStack(ModItems.COOPERS_MALLET);
        ItemStack kit = new ItemStack(ModItems.TOASTING_KIT);
        player.setItemInHand(InteractionHand.MAIN_HAND, mallet);
        player.setItemInHand(InteractionHand.OFF_HAND, kit);
        BlockPos absolutePos = helper.absolutePos(pos);
        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(absolutePos),
                Direction.NORTH,
                absolutePos,
                false
        );

        player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                mallet,
                InteractionHand.MAIN_HAND,
                hit
        );
        helper.assertBlockProperty(
                pos,
                AgingBarrelBlock.VESSEL,
                AgingVessel.CHESTNUT
        );

        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        player.setShiftKeyDown(true);
        player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                mallet,
                InteractionHand.MAIN_HAND,
                hit
        );
        player.setShiftKeyDown(false);

        helper.assertBlockProperty(
                pos,
                AgingBarrelBlock.VESSEL,
                AgingVessel.OAK
        );
        helper.assertValueEqual(
                mallet.getDamageValue(),
                2,
                "Applying and removing should each damage the mallet"
        );
        helper.assertTrue(
                player.getInventory().contains(
                        stack -> stack.is(ModItems.TOASTING_KIT)
                ),
                "Removing a treatment should return its kit"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void coopersMalletRotatesCellarFixtures(
            GameTestHelper helper
    ) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(
                pos,
                ModBlocks.wineRack(WoodVariant.OAK).defaultBlockState()
                        .setValue(WineRackBlock.FACING, Direction.NORTH)
        );
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        ItemStack mallet = new ItemStack(ModItems.COOPERS_MALLET);
        player.setItemInHand(InteractionHand.MAIN_HAND, mallet);
        BlockPos absolutePos = helper.absolutePos(pos);

        player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                mallet,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atCenterOf(absolutePos),
                        Direction.UP,
                        absolutePos,
                        false
                )
        );

        helper.assertBlockProperty(
                pos,
                WineRackBlock.FACING,
                Direction.EAST
        );
        helper.assertValueEqual(
                mallet.getDamageValue(),
                1,
                "Rotating a fixture should use mallet durability"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void cellarFixturesSeparateBatchAndTastingStorage(
            GameTestHelper helper
    ) {
        BlockPos shelfPos = new BlockPos(1, 1, 1);
        BlockPos cabinetPos = new BlockPos(3, 1, 1);
        helper.setBlock(shelfPos, ModBlocks.LABELLED_CELLAR_SHELF);
        helper.setBlock(cabinetPos, ModBlocks.TASTING_CABINET);
        CellarCollectionBlockEntity shelf = helper.getBlockEntity(
                shelfPos,
                CellarCollectionBlockEntity.class
        );
        CellarCollectionBlockEntity cabinet = helper.getBlockEntity(
                cabinetPos,
                CellarCollectionBlockEntity.class
        );
        ItemStack first = new ItemStack(ModItems.AGED_RED_WINE);
        ItemStack matching = new ItemStack(ModItems.AGED_RED_WINE);
        ItemStack other = new ItemStack(ModItems.AGED_WHITE_WINE);
        WineMetadata.apply(first, 8, WineQuality.FINE);
        WineMetadata.apply(matching, 8, WineQuality.FINE);
        WineMetadata.apply(other, 9, WineQuality.EXCEPTIONAL);
        WineMetadata.ensureBatchIdentity(first, 88001L);
        WineMetadata.ensureBatchIdentity(matching, 88001L);
        WineMetadata.ensureBatchIdentity(other, 99001L);

        helper.assertTrue(
                shelf.insertOne(first) && shelf.insertOne(matching),
                "A labelled shelf should accept bottles from its batch"
        );
        helper.assertFalse(
                shelf.insertOne(other),
                "A labelled shelf should reject a different batch"
        );
        helper.assertTrue(
                cabinet.insertOne(first) && cabinet.insertOne(other),
                "A tasting cabinet should accept mixed vintages"
        );
        helper.assertBlockProperty(
                shelfPos,
                CellarCollectionBlock.BOTTLE_COUNT,
                2
        );
        helper.assertBlockProperty(
                cabinetPos,
                CellarCollectionBlock.BOTTLE_COUNT,
                2
        );

        CellarCollectionBlockEntity restored =
                (CellarCollectionBlockEntity) reload(helper, cabinet);
        helper.assertValueEqual(
                restored.getBottleCount(),
                2,
                "Tasting cabinet contents should survive save and load"
        );
        helper.assertValueEqual(
                WineMetadata.batchId(restored.takeLastBottle()),
                99001L,
                "Cabinet retrieval should preserve bottle identity"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void cellarFixtureGlassCanBeDyedWithoutLosingWine(
            GameTestHelper helper
    ) {
        BlockPos shelfPos = new BlockPos(1, 1, 1);
        BlockPos cabinetPos = new BlockPos(3, 1, 1);
        helper.setBlock(shelfPos, ModBlocks.LABELLED_CELLAR_SHELF);
        helper.setBlock(cabinetPos, ModBlocks.TASTING_CABINET);
        CellarCollectionBlockEntity shelf = helper.getBlockEntity(
                shelfPos,
                CellarCollectionBlockEntity.class
        );
        ItemStack bottle = new ItemStack(ModItems.AGED_RED_WINE);
        WineMetadata.ensureBatchIdentity(bottle, 77119L);
        helper.assertTrue(
                shelf.insertOne(bottle),
                "The dye test shelf should accept its bottle"
        );
        helper.assertBlockProperty(
                shelfPos,
                CellarCollectionBlock.GLASS_COLOR,
                CellarGlassColor.CLEAR
        );

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        ItemStack redDye = new ItemStack(
                Items.DYE.pick(DyeColor.RED)
        );
        player.setItemInHand(InteractionHand.MAIN_HAND, redDye);
        BlockPos absoluteShelf = helper.absolutePos(shelfPos);
        player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                redDye,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atCenterOf(absoluteShelf),
                        Direction.NORTH,
                        absoluteShelf,
                        false
                )
        );

        helper.assertBlockProperty(
                shelfPos,
                CellarCollectionBlock.GLASS_COLOR,
                CellarGlassColor.RED
        );
        helper.assertValueEqual(
                redDye.getCount(),
                0,
                "Survival dyeing should consume one dye"
        );
        helper.assertValueEqual(
                shelf.getBottleCount(),
                1,
                "Dyeing glass must preserve stored wine"
        );

        ItemStack blueDye = new ItemStack(
                Items.DYE.pick(DyeColor.BLUE)
        );
        player.setItemInHand(InteractionHand.MAIN_HAND, blueDye);
        BlockPos absoluteCabinet = helper.absolutePos(cabinetPos);
        player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                blueDye,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atCenterOf(absoluteCabinet),
                        Direction.NORTH,
                        absoluteCabinet,
                        false
                )
        );

        helper.assertBlockProperty(
                cabinetPos,
                CellarCollectionBlock.GLASS_COLOR,
                CellarGlassColor.BLUE
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void completeVintageIdentitySupportsCellarDecisions(
            GameTestHelper helper
    ) {
        helper.assertValueEqual(
                WineStyle.from(new ItemStack(ModItems.WHITE_GRAPES)),
                WineStyle.WHITE,
                "White grapes should establish white-wine style metadata"
        );
        helper.assertValueEqual(
                WineStyle.from(new ItemStack(ModItems.WHITE_MUST)),
                WineStyle.WHITE,
                "White must should retain white-wine style metadata"
        );
        ItemStack bottle = new ItemStack(ModItems.AGED_WHITE_WINE);
        WineMetadata.apply(bottle, 14, WineQuality.LEGENDARY);
        WineMetadata.ensureBatchIdentity(bottle, 140014L);
        WineMetadata.applyProvenance(
                bottle,
                new WineProvenance(
                        "white",
                        336000L,
                        "minecraft:overworld",
                        12,
                        64,
                        -8,
                        "producer-id",
                        "North Hill"
                )
        );
        WineMetadata.setEstateName(bottle, "North Hill Estate");
        WineMetadata.markBottled(bottle, 336000L);
        WineMetadata.ageBottle(
                bottle,
                WineAgeStage.PEAK_AT,
                CellarRating.IDEAL
        );

        helper.assertValueEqual(
                WineMetadata.wineStyle(bottle),
                WineStyle.WHITE,
                "Wine style should be stored or inferred from the bottle"
        );
        helper.assertValueEqual(
                WineMetadata.estateName(bottle),
                "North Hill Estate",
                "Wine identity should preserve its estate"
        );
        helper.assertTrue(
                WineMetadata.estimatedTradeValue(bottle)
                        > WineQuality.TABLE.tradeValue(),
                "Peak legendary wine should advertise a premium value"
        );
        helper.assertTrue(
                WineMetadata.settlementPrestige(bottle) > 0,
                "Collectible wine should expose a prestige value"
        );
        helper.assertTrue(
                WineTastingProfile.from(bottle).body() != null,
                "The tasting profile should include body as well as notes"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void dynamicWineAppraisalPreservesBaselineAndAppliesRealFactors(
            GameTestHelper helper
    ) {
        ItemStack bottle = new ItemStack(ModItems.RED_WINE);
        WineMetadata.applyProfile(
                bottle,
                0,
                WineQualityProfile.legacy(WineQuality.TABLE)
        );

        WineAppraisal baseline = WineAppraisal.independent(bottle);
        helper.assertValueEqual(
                baseline.totalValue(),
                WineQuality.TABLE.tradeValue(),
                "An undamaged independent bottle should retain its legacy value"
        );
        helper.assertValueEqual(
                WineMetadata.estimatedTradeValue(bottle),
                baseline.totalValue(),
                "The compatibility API should use the independent appraisal"
        );

        WineAppraisal renowned = WineAppraisal.evaluate(
                bottle,
                EstateReputationTier.RENOWNED
        );
        helper.assertValueEqual(
                renowned.producerAdjustment(),
                EstateReputationTier.RENOWNED.ordinal(),
                "A renowned producer should add its reputation premium"
        );
        helper.assertValueEqual(
                renowned.totalValue() - baseline.totalValue(),
                EstateReputationTier.RENOWNED.ordinal(),
                "Producer reputation should change only the producer factor"
        );

        WineMetadata.ageBottle(bottle, 20L, CellarRating.POOR);
        WineAppraisal damaged = WineAppraisal.independent(bottle);
        helper.assertTrue(
                damaged.conditionAdjustment() < 0,
                "Poor storage should create a visible condition penalty"
        );
        helper.assertTrue(
                damaged.totalValue() < baseline.totalValue(),
                "Bottle damage should reduce the appraisal"
        );

        WineMetadata.ageBottle(
                bottle,
                WineAgeStage.SPOILED_DAMAGE * 20L,
                CellarRating.POOR
        );
        WineAppraisal spoiled = WineAppraisal.evaluate(
                bottle,
                EstateReputationTier.CELEBRATED
        );
        helper.assertValueEqual(
                spoiled.totalValue(),
                0,
                "Spoiled wine should have no appraisal value"
        );
        helper.assertValueEqual(
                spoiled.prestige(),
                0,
                "Spoiled wine should provide no settlement prestige"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void buyerProfilesIdentifyDistinctWineMarkets(
            GameTestHelper helper
    ) {
        ItemStack tavernBottle = new ItemStack(ModItems.RED_WINE);
        WineMetadata.applyProfile(
                tavernBottle,
                0,
                WineQualityProfile.legacy(WineQuality.TABLE)
        );
        WineMarketOutlook tavernMarket = WineMarketOutlook.bestFor(
                tavernBottle,
                WineAppraisal.independent(tavernBottle)
        );
        helper.assertValueEqual(
                tavernMarket.bestBuyer(),
                WineBuyerType.TAVERN_KEEPER,
                "Young table wine should fit the dependable tavern market"
        );

        ItemStack coastalBottle = new ItemStack(ModItems.WHITE_WINE);
        WineMetadata.applyProfile(
                coastalBottle,
                0,
                WineQualityProfile.legacy(WineQuality.GOOD)
        );
        WineMarketOutlook coastalMarket = WineMarketOutlook.bestFor(
                coastalBottle,
                WineAppraisal.independent(coastalBottle)
        );
        helper.assertValueEqual(
                coastalMarket.bestBuyer(),
                WineBuyerType.COASTAL_SETTLEMENT,
                "Fresh white wine should fit a coastal settlement"
        );

        ItemStack collectible = new ItemStack(ModItems.AGED_RED_WINE);
        WineMetadata.applyProfile(
                collectible,
                0,
                WineQualityProfile.legacy(WineQuality.LEGENDARY)
        );
        WineMetadata.ageBottle(
                collectible,
                WineAgeStage.PEAK_AT,
                CellarRating.IDEAL
        );
        WineAppraisal collectibleAppraisal = WineAppraisal.evaluate(
                collectible,
                EstateReputationTier.RENOWNED
        );
        WineMarketOutlook collectorMarket = WineMarketOutlook.bestFor(
                collectible,
                collectibleAppraisal
        );
        helper.assertValueEqual(
                collectorMarket.bestBuyer(),
                WineBuyerType.COLLECTOR,
                "Peak legendary wine should fit the collector market"
        );
        helper.assertValueEqual(
                collectorMarket.estimatedValue(),
                collectibleAppraisal.totalValue()
                        + collectorMarket.buyerAdjustment(),
                "The market outlook should add only the buyer premium"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void regionalMarketsFollowRealClimateAndTerrain(
            GameTestHelper helper
    ) {
        helper.assertValueEqual(
                WineMarketRegion.classify(
                        com.zenith.vintner.vineyard.ClimateBand.COLD,
                        2,
                        140,
                        com.zenith.vintner.vineyard.SlopeClass.STEEP
                ),
                WineMarketRegion.COLD,
                "Cold climates should create cold-region demand"
        );
        helper.assertValueEqual(
                WineMarketRegion.classify(
                        com.zenith.vintner.vineyard.ClimateBand.TEMPERATE,
                        4,
                        64,
                        com.zenith.vintner.vineyard.SlopeClass.FLAT
                ),
                WineMarketRegion.COASTAL,
                "Temperate sites beside water should create coastal demand"
        );
        helper.assertValueEqual(
                WineMarketRegion.classify(
                        com.zenith.vintner.vineyard.ClimateBand.WARM,
                        20,
                        132,
                        com.zenith.vintner.vineyard.SlopeClass.MODERATE
                ),
                WineMarketRegion.MINING,
                "High inland sites should create mining-settlement demand"
        );
        helper.assertValueEqual(
                WineMarketRegion.classify(
                        com.zenith.vintner.vineyard.ClimateBand.TEMPERATE,
                        20,
                        64,
                        com.zenith.vintner.vineyard.SlopeClass.GENTLE
                ),
                WineMarketRegion.AGRICULTURAL,
                "Ordinary temperate lowlands should use agricultural demand"
        );

        ItemStack whiteWine = new ItemStack(ModItems.WHITE_WINE);
        WineMetadata.applyProfile(
                whiteWine,
                0,
                WineQualityProfile.legacy(WineQuality.GOOD)
        );
        WineAppraisal appraisal = WineAppraisal.independent(whiteWine);
        WineMarketOutlook coastalQuote = WineMarketOutlook.forBuyer(
                whiteWine,
                appraisal,
                WineMarketRegion.COASTAL.buyerType()
        );
        WineMarketOutlook miningQuote = WineMarketOutlook.forBuyer(
                whiteWine,
                appraisal,
                WineMarketRegion.MINING.buyerType()
        );
        helper.assertTrue(
                coastalQuote.estimatedValue() > miningQuote.estimatedValue(),
                "Fresh white wine should be worth more in a coastal market"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void cellarStabilityAndDisturbanceAffectRating(
            GameTestHelper helper
    ) {
        helper.assertValueEqual(
                CellarConditions.ratingFor(
                        true,
                        true,
                        true,
                        true,
                        false,
                        true,
                        false
                ),
                CellarRating.IDEAL,
                "A stable protected cellar should be ideal"
        );
        helper.assertTrue(
                CellarConditions.ratingFor(
                        true,
                        true,
                        true,
                        true,
                        false,
                        true,
                        true
                ) != CellarRating.IDEAL,
                "Nearby machinery disturbance should reduce cellar quality"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void cellarHistoryChangesStoredQualityScore(
            GameTestHelper helper
    ) {
        ItemStack idealBottle = new ItemStack(ModItems.AGED_RED_WINE);
        WineMetadata.applyProfile(
                idealBottle,
                9,
                new WineQualityProfile(0, 60, 5, 5, 10, 0)
        );
        WineMetadata.markBottled(idealBottle, 0L);
        WineMetadata.ageBottle(
                idealBottle,
                40L * 24000L,
                CellarRating.IDEAL
        );
        helper.assertValueEqual(
                WineMetadata.qualityScore(idealBottle),
                90,
                "Long ideal storage should improve quality to ninety"
        );
        helper.assertValueEqual(
                WineMetadata.quality(idealBottle),
                WineQuality.LEGENDARY,
                "Ideal storage should make an excellent bottle legendary"
        );

        ItemStack poorBottle = new ItemStack(ModItems.AGED_RED_WINE);
        WineMetadata.applyProfile(
                poorBottle,
                9,
                new WineQualityProfile(0, 60, 5, 5, 10, 0)
        );
        WineMetadata.markBottled(poorBottle, 0L);
        WineMetadata.ageBottle(
                poorBottle,
                20L * 24000L,
                CellarRating.POOR
        );
        helper.assertTrue(
                WineMetadata.qualityScore(poorBottle) < 80,
                "Poor storage should progressively reduce quality"
        );
        helper.succeed();
    }

    private static void triggerInventoryChange(
            ServerPlayer player,
            Item item
    ) {
        ItemStack stack = new ItemStack(item);
        player.getInventory().add(stack);
        CriteriaTriggers.INVENTORY_CHANGED.trigger(
                player,
                player.getInventory(),
                stack
        );
    }

    private static void assertRecipeKnown(
            GameTestHelper helper,
            ServerPlayer player,
            String recipePath
    ) {
        ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(
                Registries.RECIPE,
                Identifier.fromNamespaceAndPath(
                        "vintner",
                        recipePath
                )
        );

        helper.assertTrue(
                player.getRecipeBook().contains(recipeKey),
                "Survival progression should unlock "
                        + recipePath
        );
    }

    private static BlockState matureLowerVine() {
        return ModBlocks.RED_GRAPEVINE
                .defaultBlockState()
                .setValue(GrapevineBlock.UPPER, false)
                .setValue(GrapevineBlock.AGE, GrapevineBlock.MAX_AGE);
    }

    @GameTest(maxTicks = 40)
    public void terroirRecognizesRoadmapSoilFamilies(
            GameTestHelper helper
    ) {
        helper.assertValueEqual(
                TerroirEvaluator.classifySoil(
                        Blocks.CLAY.defaultBlockState()
                ),
                SoilType.CLAY,
                "Clay should produce the clay soil profile"
        );
        helper.assertValueEqual(
                TerroirEvaluator.classifySoil(
                        Blocks.DRIPSTONE_BLOCK.defaultBlockState()
                ),
                SoilType.LIMESTONE,
                "Dripstone should represent limestone geology"
        );
        helper.assertValueEqual(
                TerroirEvaluator.classifySoil(
                        Blocks.CALCITE.defaultBlockState()
                ),
                SoilType.CHALK,
                "Calcite should represent chalk geology"
        );
        helper.assertValueEqual(
                TerroirEvaluator.classifySoil(
                        Blocks.GRAVEL.defaultBlockState()
                ),
                SoilType.GRAVEL,
                "Gravel should produce the gravel profile"
        );
        helper.assertValueEqual(
                TerroirEvaluator.classifySoil(
                        Blocks.SAND.defaultBlockState()
                ),
                SoilType.SAND,
                "Sand should produce the sand profile"
        );
        helper.assertValueEqual(
                TerroirEvaluator.classifySoil(
                        Blocks.DIRT.defaultBlockState()
                ),
                SoilType.LOAM,
                "Ordinary dirt should be interpreted as loam"
        );
        helper.assertValueEqual(
                TerroirEvaluator.classifySoil(
                        Blocks.TUFF.defaultBlockState()
                ),
                SoilType.VOLCANIC,
                "Tuff should produce the volcanic profile"
        );
        helper.assertValueEqual(
                TerroirEvaluator.classifySoil(
                        Blocks.MUD.defaultBlockState()
                ),
                SoilType.ALLUVIAL,
                "Mud should produce the alluvial profile"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void soilProfilesExposeDistinctVineyardTradeoffs(
            GameTestHelper helper
    ) {
        SoilProfile loam = SoilProfile.of(SoilType.LOAM);
        SoilProfile sand = SoilProfile.of(SoilType.SAND);
        SoilProfile volcanic = SoilProfile.of(SoilType.VOLCANIC);

        helper.assertTrue(
                sand.drainage() > loam.drainage(),
                "Sand should drain faster than loam"
        );
        helper.assertTrue(
                loam.fertility() > sand.fertility(),
                "Loam should be more fertile than sand"
        );
        helper.assertTrue(
                volcanic.mineralCharacter() > loam.mineralCharacter(),
                "Volcanic ground should have stronger mineral character"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void temperateClimateOutperformsExtremeSites(
            GameTestHelper helper
    ) {
        ClimateProfile temperate = ClimateProfile.evaluate(
                0.82F,
                true,
                false,
                70,
                false
        );
        ClimateProfile frozen = ClimateProfile.evaluate(
                0.0F,
                true,
                false,
                140,
                true
        );
        ClimateProfile hot = ClimateProfile.evaluate(
                2.0F,
                false,
                false,
                70,
                false
        );

        helper.assertTrue(
                temperate.suitability() > frozen.suitability(),
                "Temperate climates should outperform frost-prone sites"
        );
        helper.assertTrue(
                temperate.suitability() > hot.suitability(),
                "Temperate climates should outperform heat-stressed sites"
        );
        helper.assertTrue(
                frozen.frostRisk() > temperate.frostRisk(),
                "Cold high ground should report greater frost risk"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void southernTerracesImproveTerrainSuitability(
            GameTestHelper helper
    ) {
        TerrainProfile southern = TerrainProfile.evaluate(
                80,
                2,
                Direction.SOUTH,
                true,
                5,
                55,
                false,
                true
        );
        TerrainProfile northern = TerrainProfile.evaluate(
                80,
                2,
                Direction.NORTH,
                true,
                5,
                55,
                false,
                false
        );

        helper.assertTrue(
                southern.suitability() > northern.suitability(),
                "A terraced south-facing site should outperform north-facing ground"
        );
        helper.assertTrue(
                southern.terraced(),
                "The terrain report should retain detected terracing"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void soilProbeSurveysLandAndGrantsAdvancement(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, Blocks.CALCITE);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        ItemStack probe = new ItemStack(ModItems.SOIL_PROBE);
        player.setItemInHand(InteractionHand.MAIN_HAND, probe);
        BlockPos absolute = helper.absolutePos(FIRST);

        player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                probe,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atCenterOf(absolute),
                        Direction.UP,
                        absolute,
                        false
                )
        );

        AdvancementHolder advancement = helper.getLevel()
                .getServer()
                .getAdvancements()
                .get(
                        Identifier.fromNamespaceAndPath(
                                "vintner",
                                "vintner/survey_vineyard"
                        )
                );

        helper.assertTrue(
                advancement != null,
                "The vineyard-survey advancement should load"
        );
        helper.assertTrue(
                player.getAdvancements()
                        .getOrStartProgress(advancement)
                        .isDone(),
                "Using a Soil Probe should grant survey progress"
        );
        helper.assertValueEqual(
                probe.getDamageValue(),
                1,
                "A survival soil survey should use one durability"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void soilProbeRejectsUnrelatedBlocksWithoutWear(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, Blocks.OAK_PLANKS);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        ItemStack probe = new ItemStack(ModItems.SOIL_PROBE);
        player.setItemInHand(InteractionHand.MAIN_HAND, probe);
        BlockPos absolute = helper.absolutePos(FIRST);

        player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                probe,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atCenterOf(absolute),
                        Direction.UP,
                        absolute,
                        false
                )
        );

        helper.assertValueEqual(
                probe.getDamageValue(),
                0,
                "Invalid soil-survey targets must not use durability"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void terroirUsesExistingVineyardQualityBudget(
            GameTestHelper helper
    ) {
        helper.assertValueEqual(
                GrapeQualityEvaluator.scoreWithTerroir(
                        28,
                        true,
                        true,
                        true,
                        true,
                        true
                ),
                60,
                "An ideal terroir and harvest should retain the sixty-point vineyard cap"
        );
        helper.assertValueEqual(
                GrapeQualityEvaluator.scoreWithTerroir(
                        0,
                        false,
                        false,
                        false,
                        false,
                        false
                ),
                0,
                "A failed site and harvest should score zero"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void nativeCalendarUsesConfigurableSeasonBoundaries(
            GameTestHelper helper
    ) {
        helper.assertValueEqual(
                SeasonalContext.atDay(0, 8).season(),
                VineyardSeason.SPRING,
                "The native calendar should begin in spring"
        );
        helper.assertValueEqual(
                SeasonalContext.atDay(8, 8).season(),
                VineyardSeason.SUMMER,
                "The configured eighth day boundary should begin summer"
        );
        helper.assertValueEqual(
                SeasonalContext.atDay(24, 8).season(),
                VineyardSeason.WINTER,
                "The fourth configured season should be winter"
        );
        SeasonalContext secondYear = SeasonalContext.atDay(32, 8);
        helper.assertValueEqual(
                secondYear.year(),
                2,
                "A complete four-season cycle should advance the vintage year"
        );
        helper.assertValueEqual(
                helper.getLevel().getGameRules().get(
                        ModGameRules.SEASON_LENGTH_DAYS
                ),
                ModGameRules.DEFAULT_SEASON_LENGTH_DAYS,
                "New worlds should use the documented default season length"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void externalSeasonCycleMapsToVintnerCalendar(
            GameTestHelper helper
    ) {
        SeasonalContext context = SeasonalContext.fromExternalCycle(
                VineyardSeason.AUTUMN,
                800_000L,
                410_000,
                24_000,
                192_000,
                768_000
        );
        helper.assertValueEqual(
                context.season(),
                VineyardSeason.AUTUMN,
                "An external season should retain its Vintner equivalent"
        );
        helper.assertValueEqual(
                context.dayInSeason(),
                2,
                "Cycle position should map to the correct day within the season"
        );
        helper.assertValueEqual(
                context.seasonLengthDays(),
                8,
                "External season duration should determine the displayed season length"
        );
        helper.assertValueEqual(
                context.year(),
                2,
                "The monotonic world clock should advance the vintage year"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void seasonsControlGrowthWithoutDestroyingVines(
            GameTestHelper helper
    ) {
        helper.assertTrue(
                VineyardSeason.SPRING.shouldGrow(
                        RandomSource.create(41L),
                        1
                ),
                "Spring should permit normal vine growth"
        );
        helper.assertTrue(
                !VineyardSeason.WINTER.shouldGrow(
                        RandomSource.create(41L),
                        1
                ),
                "Winter should make vines dormant rather than deleting them"
        );
        helper.assertValueEqual(
                VineyardSeason.WINTER.growthChanceDenominator(8, true),
                16,
                "Protected cultivation should permit winter growth at half speed"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void glassCoverProtectsVinesFromShelterRelevantWeather(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.RED_GRAPEVINE);
        helper.setBlock(FIRST.above(3), Blocks.GLASS);

        helper.assertTrue(
                VineyardProtection.isProtected(
                        helper.getLevel(),
                        helper.absolutePos(FIRST)
                ),
                "Overhead glass should register as protected cultivation"
        );
        helper.assertValueEqual(
                VineyardWeatherEvent.HAIL.mitigatedBy(true),
                VineyardWeatherEvent.CALM,
                "Glass cover should shelter vines from hail"
        );
        helper.assertValueEqual(
                VineyardWeatherEvent.HEATWAVE.mitigatedBy(true),
                VineyardWeatherEvent.HEATWAVE,
                "Glass cover should not erase heat pressure"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void weatherOutlooksAreStableAndQualityBounded(
            GameTestHelper helper
    ) {
        ClimateProfile climate = ClimateProfile.evaluate(
                0.8F,
                true,
                false,
                72,
                false
        );
        SeasonalContext context = SeasonalContext.atDay(12, 8);
        VineyardWeatherEvent first = VineyardWeatherEvent.forSite(
                173L,
                4,
                -3,
                climate,
                context
        );
        VineyardWeatherEvent second = VineyardWeatherEvent.forSite(
                173L,
                4,
                -3,
                climate,
                context
        );

        helper.assertValueEqual(
                first,
                second,
                "A region's seasonal weather outlook must remain stable"
        );
        for (VineyardWeatherEvent event : VineyardWeatherEvent.values()) {
            int points = event.harvestQualityPoints(false);
            helper.assertTrue(
                    points >= 0 && points <= 7,
                    "Weather must stay inside the existing harvest-quality budget"
            );
        }
        helper.assertValueEqual(
                GrapeQualityEvaluator.scoreWithTerroirAndWeather(
                        28,
                        true,
                        true,
                        true,
                        true,
                        7
                ),
                60,
                "Ideal seasonal weather should preserve the sixty-point cap"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void nearbyWaterChannelsMitigateDrought(
            GameTestHelper helper
    ) {
        BlockPos channel = FIRST.offset(4, -1, 0);
        helper.setBlock(channel, Blocks.WATER);
        helper.assertTrue(
                VineyardIrrigation.isIrrigated(
                        helper.getLevel(),
                        helper.absolutePos(FIRST)
                ),
                "A water channel within four blocks should irrigate a vine"
        );
        helper.assertValueEqual(
                VineyardWeatherEvent.DROUGHT.mitigatedBy(false, true),
                VineyardWeatherEvent.CALM,
                "Irrigation should mitigate drought pressure"
        );
        helper.assertValueEqual(
                VineyardWeatherEvent.HAIL.mitigatedBy(false, true),
                VineyardWeatherEvent.HAIL,
                "Irrigation should not shelter vines from hail"
        );

        helper.setBlock(channel, Blocks.AIR);
        helper.setBlock(FIRST.offset(5, -1, 0), Blocks.WATER);
        helper.assertFalse(
                VineyardIrrigation.isIrrigated(
                        helper.getLevel(),
                        helper.absolutePos(FIRST)
                ),
                "Water beyond the four-block channel radius should not count"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void terroirRecommendsClimateAppropriateVarieties(
            GameTestHelper helper
    ) {
        SoilProfile soil = SoilProfile.of(SoilType.VOLCANIC);
        TerrainProfile terrain = TerrainProfile.evaluate(
                72,
                2,
                Direction.SOUTH,
                true,
                8,
                55,
                false,
                false
        );
        TerroirReport warmSite = new TerroirReport(
                ClimateProfile.evaluate(
                        1.15F,
                        true,
                        false,
                        72,
                        false
                ),
                soil,
                terrain,
                80
        );
        TerroirReport coolSite = new TerroirReport(
                ClimateProfile.evaluate(
                        0.5F,
                        true,
                        false,
                        72,
                        false
                ),
                soil,
                terrain,
                80
        );

        helper.assertValueEqual(
                warmSite.recommendedVariety(),
                GrapeVariety.RED,
                "A warm, sunny site should recommend red grapes"
        );
        helper.assertValueEqual(
                coolSite.recommendedVariety(),
                GrapeVariety.WHITE,
                "A cooler site should recommend white grapes"
        );
        helper.assertTrue(
                warmSite.vineyardQualityPoints(GrapeVariety.RED)
                        > warmSite.vineyardQualityPoints(GrapeVariety.WHITE),
                "Following the recommendation should improve vineyard quality"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void vineyardAdvicePrioritizesActionableManagement(
            GameTestHelper helper
    ) {
        helper.assertValueEqual(
                VineyardManagementAdvice.recommend(
                        false,
                        VineyardWeatherEvent.DROUGHT,
                        false,
                        false,
                        false
                ),
                VineyardManagementAdvice.PREPARE_SOIL,
                "Preparing soil should be the first management action"
        );
        helper.assertValueEqual(
                VineyardManagementAdvice.recommend(
                        true,
                        VineyardWeatherEvent.DROUGHT,
                        false,
                        false,
                        false
                ),
                VineyardManagementAdvice.IRRIGATE,
                "An unmanaged drought should recommend irrigation"
        );
        helper.assertValueEqual(
                VineyardManagementAdvice.recommend(
                        true,
                        VineyardWeatherEvent.HAIL,
                        false,
                        false,
                        false
                ),
                VineyardManagementAdvice.PROTECT,
                "Hail risk should recommend protected cultivation"
        );
        helper.assertValueEqual(
                VineyardManagementAdvice.recommend(
                        true,
                        VineyardWeatherEvent.CALM,
                        false,
                        false,
                        true
                ),
                VineyardManagementAdvice.HARVEST,
                "A ripe managed vine should recommend harvesting"
        );
        helper.succeed();
    }

    private static BlockState matureUpperVine() {
        return ModBlocks.RED_GRAPEVINE
                .defaultBlockState()
                .setValue(GrapevineBlock.UPPER, true)
                .setValue(GrapevineBlock.AGE, GrapevineBlock.MAX_AGE);
    }

    private static void assertNoWireConnections(
            GameTestHelper helper,
            BlockPos pos
    ) {
        helper.assertBlockProperty(
                pos,
                TrellisBlock.NORTH,
                TrellisBlock.RowConnection.NONE
        );
        helper.assertBlockProperty(
                pos,
                TrellisBlock.EAST,
                TrellisBlock.RowConnection.NONE
        );
        helper.assertBlockProperty(
                pos,
                TrellisBlock.SOUTH,
                TrellisBlock.RowConnection.NONE
        );
        helper.assertBlockProperty(
                pos,
                TrellisBlock.WEST,
                TrellisBlock.RowConnection.NONE
        );
    }

    private static Villager specialistVillager(
            GameTestHelper helper,
            ResourceKey<VillagerProfession> professionKey,
            int level,
            BlockPos position
    ) {
        Holder<VillagerProfession> profession = BuiltInRegistries
                .VILLAGER_PROFESSION
                .get(professionKey)
                .orElseThrow();
        Villager villager = helper.spawnWithNoFreeWill(
                EntityTypes.VILLAGER,
                position
        );

        villager.setVillagerData(
                villager.getVillagerData()
                        .withProfession(profession)
                        .withLevel(level)
        );
        villager.setOffers(new MerchantOffers());
        return villager;
    }

    private static void assertTradeProgression(
            GameTestHelper helper,
            Villager villager,
            int[] expectedCounts,
            String professionName
    ) {
        for (int level = 1; level <= expectedCounts.length; level++) {
            villager.setVillagerData(
                    villager.getVillagerData().withLevel(level)
            );
            villager.setOffers(new MerchantOffers());
            ModTrades.refreshVillagerOffers(villager);
            helper.assertValueEqual(
                    villager.getOffers().size(),
                    expectedCounts[level - 1],
                    professionName + " level " + level
                            + " should expose the expected trade tier"
            );
        }
    }

    private static void assertTrade(
            GameTestHelper helper,
            MerchantOffers offers,
            Item payment,
            Item result,
            String message
    ) {
        boolean present = offers.stream().anyMatch(offer ->
                offer.getBaseCostA().is(payment)
                        && offer.getResult().is(result)
        );
        helper.assertTrue(present, message);
    }

    private static BlockEntity reload(
            GameTestHelper helper,
            BlockEntity original
    ) {
        CompoundTag saved = original.saveWithFullMetadata(
                helper.getLevel().registryAccess()
        );

        BlockEntity restored = BlockEntity.loadStatic(
                original.getBlockPos(),
                original.getBlockState(),
                saved,
                helper.getLevel().registryAccess()
        );

        helper.assertTrue(
                restored != null,
                "A saved block entity must deserialize"
        );
        return restored;
    }

    private static WineRackBlockEntity reloadRackWithElapsedTime(
            GameTestHelper helper,
            WineRackBlockEntity original,
            long elapsedTicks
    ) {
        CompoundTag saved = original.saveWithFullMetadata(
                helper.getLevel().registryAccess()
        );
        saved.putLong(
                "LastAgingGameTime",
                helper.getLevel().getGameTime() - elapsedTicks
        );

        BlockEntity restored = BlockEntity.loadStatic(
                original.getBlockPos(),
                original.getBlockState(),
                saved,
                helper.getLevel().registryAccess()
        );

        helper.assertTrue(
                restored instanceof WineRackBlockEntity,
                "The persisted wine rack should deserialize"
        );
        restored.setLevel(helper.getLevel());
        return (WineRackBlockEntity) restored;
    }

    private static WineCrateBlockEntity reloadCrateWithElapsedTime(
            GameTestHelper helper,
            WineCrateBlockEntity original,
            long elapsedTicks
    ) {
        CompoundTag saved = original.saveWithFullMetadata(
                helper.getLevel().registryAccess()
        );
        saved.putLong(
                "LastAgingGameTime",
                helper.getLevel().getGameTime() - elapsedTicks
        );

        BlockEntity restored = BlockEntity.loadStatic(
                original.getBlockPos(),
                original.getBlockState(),
                saved,
                helper.getLevel().registryAccess()
        );

        helper.assertTrue(
                restored instanceof WineCrateBlockEntity,
                "The persisted wine crate should deserialize"
        );
        restored.setLevel(helper.getLevel());
        return (WineCrateBlockEntity) restored;
    }

    @GameTest(maxTicks = 40)
    public void almanacRoutesOnlyRecognizedInspectionTargets(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, Blocks.CALCITE);
        helper.assertValueEqual(
                AlmanacInspection.classify(
                        helper.getLevel(),
                        helper.absolutePos(FIRST)
                ),
                AlmanacInspection.Target.VINEYARD_SITE,
                "Recognized soil should route to the vineyard survey"
        );

        helper.setBlock(FIRST, matureLowerVine());
        helper.assertValueEqual(
                AlmanacInspection.classify(
                        helper.getLevel(),
                        helper.absolutePos(FIRST)
                ),
                AlmanacInspection.Target.GRAPEVINE,
                "Grapevines should route to the ripeness report"
        );

        helper.setBlock(FIRST, ModBlocks.FERMENTATION_BARREL);
        helper.assertValueEqual(
                AlmanacInspection.classify(
                        helper.getLevel(),
                        helper.absolutePos(FIRST)
                ),
                AlmanacInspection.Target.FERMENTATION,
                "Fermentation barrels should route to the hydrometer report"
        );

        helper.setBlock(FIRST, ModBlocks.AGING_BARREL);
        helper.assertValueEqual(
                AlmanacInspection.classify(
                        helper.getLevel(),
                        helper.absolutePos(FIRST)
                ),
                AlmanacInspection.Target.AGEING,
                "Aging barrels should route to the cellar report"
        );

        helper.setBlock(FIRST, Blocks.OAK_PLANKS);
        helper.assertValueEqual(
                AlmanacInspection.classify(
                        helper.getLevel(),
                        helper.absolutePos(FIRST)
                ),
                AlmanacInspection.Target.NONE,
                "Unrelated construction blocks should not produce a land report"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void processInstrumentsExposeRealRemainingTime(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.FERMENTATION_BARREL);
        FermentationBarrelBlockEntity fermentation =
                helper.getBlockEntity(
                        FIRST,
                        FermentationBarrelBlockEntity.class
                );
        helper.assertValueEqual(
                fermentation.getRemainingSeconds(),
                FermentationBarrelBlockEntity.FERMENTATION_TIME / 20,
                "An idle fermentation barrel should report its full process time"
        );

        helper.setBlock(EAST, ModBlocks.AGING_BARREL);
        AgingBarrelBlockEntity ageing =
                helper.getBlockEntity(
                        EAST,
                        AgingBarrelBlockEntity.class
                );
        helper.assertValueEqual(
                ageing.getRemainingSeconds(),
                ageing.getAgingTime() / 20,
                "An idle aging barrel should report its vessel process time"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void almanacSurveyBookmarksPersistOnTheItem(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, Blocks.CALCITE);
        BlockPos absolute = helper.absolutePos(FIRST);
        ItemStack almanac = new ItemStack(ModItems.VINTNER_ALMANAC);
        var report = TerroirEvaluator.inspect(
                helper.getLevel(),
                absolute
        );
        VineyardSurveyRecord captured = VineyardSurveyRecord.capture(
                helper.getLevel(),
                absolute,
                report
        );
        captured.save(almanac);

        VineyardSurveyRecord restored = VineyardSurveyRecord.read(
                almanac.copy()
        ).orElseThrow();
        helper.assertValueEqual(
                restored,
                captured,
                "A copied Almanac should retain its vineyard survey bookmark"
        );
        helper.assertValueEqual(
                restored.siteScore(),
                report.siteScore(),
                "The bookmark should preserve the evaluated site score"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void vineAgeStagesCreateYieldQualityTradeoffs(
            GameTestHelper helper
    ) {
        helper.assertValueEqual(
                VineAgeStage.atDays(0),
                VineAgeStage.NEW_PLANTING,
                "A newly planted vine should begin as a new planting"
        );
        helper.assertValueEqual(
                VineAgeStage.atDays(8),
                VineAgeStage.YOUNG,
                "Eight vineyard days should produce a young vine"
        );
        helper.assertValueEqual(
                VineAgeStage.atDays(32),
                VineAgeStage.MATURE,
                "Thirty-two vineyard days should produce a mature vine"
        );
        helper.assertValueEqual(
                VineAgeStage.atDays(96),
                VineAgeStage.OLD,
                "Ninety-six vineyard days should produce an old vine"
        );
        helper.assertValueEqual(
                VineAgeStage.atDays(192),
                VineAgeStage.ANCIENT,
                "A long-lived vine should eventually become ancient"
        );
        helper.assertTrue(
                VineAgeStage.YOUNG.harvestAdjustment()
                        > VineAgeStage.OLD.harvestAdjustment(),
                "Young vines should favor yield over old vines"
        );
        helper.assertTrue(
                VineAgeStage.OLD.qualityPoints()
                        > VineAgeStage.YOUNG.qualityPoints(),
                "Old vines should favor concentration over young vines"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void vinePlantingDatesPersistPerRootPosition(
            GameTestHelper helper
    ) {
        BlockPos first = helper.absolutePos(FIRST);
        BlockPos east = helper.absolutePos(EAST);
        VineAgeSavedData ages = VineAgeSavedData.get(helper.getLevel());

        ages.remove(first);
        ages.remove(east);
        ages.plant(first, 12L);

        helper.assertValueEqual(
                ages.ageDays(first, 44L),
                32L,
                "A vine should retain its original planting day"
        );
        helper.assertValueEqual(
                ages.stage(first, 44L),
                VineAgeStage.MATURE,
                "Persisted planting time should determine the age category"
        );
        helper.assertValueEqual(
                ages.ageDays(east, 44L),
                0L,
                "A legacy vine should initialize safely when first inspected"
        );

        ages.remove(first);
        ages.remove(east);
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void vineAgeUsesExistingQualityBudget(
            GameTestHelper helper
    ) {
        int young = GrapeQualityEvaluator.scoreWithTerroirAgeAndWeather(
                28,
                VineAgeStage.YOUNG.qualityPoints(),
                true,
                true,
                true,
                7
        );
        int ancient = GrapeQualityEvaluator.scoreWithTerroirAgeAndWeather(
                28,
                VineAgeStage.ANCIENT.qualityPoints(),
                true,
                true,
                true,
                7
        );

        helper.assertTrue(
                ancient > young,
                "Ancient roots should produce more concentrated fruit"
        );
        helper.assertValueEqual(
                ancient,
                60,
                "Ancient vines should complete, not exceed, the vineyard budget"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void graftingChangesVarietyButPreservesOldRoots(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayer(GameType.SURVIVAL);
        Block redRoot = ModBlocks.redGrapevine(WoodVariant.SPRUCE);
        Block redUpper = ModBlocks.redGrapevine(WoodVariant.MANGROVE);
        Block whiteRoot = ModBlocks.whiteGrapevine(WoodVariant.SPRUCE);
        Block whiteUpper = ModBlocks.whiteGrapevine(WoodVariant.MANGROVE);
        BlockPos absoluteRoot = helper.absolutePos(FIRST);

        helper.setBlock(
                FIRST,
                redRoot.defaultBlockState()
                        .setValue(GrapevineBlock.AGE, 3)
        );
        helper.setBlock(
                UPPER,
                redUpper.defaultBlockState()
                        .setValue(GrapevineBlock.UPPER, true)
                        .setValue(GrapevineBlock.AGE, 3)
        );

        VineAgeSavedData ages = VineAgeSavedData.get(helper.getLevel());
        ages.remove(absoluteRoot);
        ages.plant(absoluteRoot, 12L);

        ItemStack knife = new ItemStack(ModItems.GRAFTING_KNIFE);
        ItemStack cutting = new ItemStack(ModItems.WHITE_GRAPE_CUTTING);

        helper.assertValueEqual(
                GraftingKnifeItem.graft(
                        helper.getLevel(),
                        absoluteRoot,
                        player,
                        knife,
                        InteractionHand.MAIN_HAND,
                        cutting
                ),
                InteractionResult.SUCCESS,
                "A different cutting should graft onto a trained vine"
        );
        helper.assertBlockPresent(whiteRoot, FIRST);
        helper.assertBlockPresent(whiteUpper, UPPER);
        helper.assertBlockProperty(FIRST, GrapevineBlock.AGE, 2);
        helper.assertBlockProperty(UPPER, GrapevineBlock.AGE, 2);
        helper.assertValueEqual(
                ages.ageDays(absoluteRoot, 108L),
                96L,
                "Grafting must preserve the root system's planting date"
        );
        helper.assertValueEqual(
                cutting.getCount(),
                0,
                "Survival grafting should consume one cutting"
        );
        helper.assertValueEqual(
                knife.getDamageValue(),
                1,
                "Grafting should consume one knife durability"
        );

        ages.remove(absoluteRoot);
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void shearsSelectPersistentYieldStrategy(
            GameTestHelper helper
    ) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        Block vine = ModBlocks.redGrapevine(WoodVariant.OAK);
        BlockPos absoluteRoot = helper.absolutePos(FIRST);

        helper.setBlock(
                FIRST,
                vine.defaultBlockState().setValue(GrapevineBlock.AGE, 3)
        );
        helper.setBlock(
                UPPER,
                vine.defaultBlockState()
                        .setValue(GrapevineBlock.UPPER, true)
                        .setValue(GrapevineBlock.AGE, 3)
        );

        VineManagementSavedData management =
                VineManagementSavedData.get(helper.getLevel());
        management.remove(absoluteRoot);
        ItemStack shears = new ItemStack(Items.SHEARS);
        player.setItemInHand(InteractionHand.MAIN_HAND, shears);
        player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                shears,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atCenterOf(absoluteRoot),
                        Direction.NORTH,
                        absoluteRoot,
                        false
                )
        );

        helper.assertValueEqual(
                management.mode(absoluteRoot),
                VineYieldMode.QUALITY_FOCUS,
                "Shearing the lower trunk should select Quality Focus"
        );
        helper.assertBlockProperty(FIRST, GrapevineBlock.AGE, 2);
        helper.assertBlockProperty(UPPER, GrapevineBlock.AGE, 2);
        helper.assertValueEqual(
                shears.getDamageValue(),
                1,
                "Yield management should consume shears durability"
        );
        helper.assertTrue(
                GrapeQualityEvaluator.scoreWithTerroirAgeWeatherAndYield(
                        28,
                        6,
                        true,
                        VineYieldMode.QUALITY_FOCUS.qualityPoints(),
                        true,
                        7
                )
                        > GrapeQualityEvaluator.scoreWithTerroirAgeWeatherAndYield(
                                28,
                                6,
                                true,
                                VineYieldMode.HIGH_YIELD.qualityPoints(),
                                true,
                                7
                        ),
                "Quality Focus should outperform High Yield on vintage score"
        );

        management.remove(absoluteRoot);
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void vineyardThreatsAreReadableAndNonRandom(
            GameTestHelper helper
    ) {
        TerroirReport humidSite = new TerroirReport(
                ClimateProfile.evaluate(
                        0.8F,
                        true,
                        true,
                        72,
                        false
                ),
                SoilProfile.of(SoilType.LOAM),
                TerrainProfile.evaluate(
                        72,
                        1,
                        Direction.SOUTH,
                        true,
                        3,
                        20,
                        false,
                        false
                ),
                70
        );

        helper.assertValueEqual(
                VineyardThreat.assess(
                        false,
                        false,
                        humidSite,
                        VineyardWeatherEvent.HEAVY_RAIN
                ),
                VineyardThreat.NUTRIENT_IMBALANCE,
                "Unprepared soil should be the first actionable threat"
        );
        helper.assertValueEqual(
                VineyardThreat.assess(
                        true,
                        false,
                        humidSite,
                        VineyardWeatherEvent.HEAVY_RAIN
                ),
                VineyardThreat.MILDEW_RISK,
                "Humid rain with poor airflow should create mildew risk"
        );
        helper.assertValueEqual(
                VineyardThreat.assess(
                        true,
                        true,
                        humidSite,
                        VineyardWeatherEvent.HEAVY_RAIN
                ),
                VineyardThreat.ROT_RISK,
                "Ripe fruit in heavy rain should prioritize rot risk"
        );
        helper.assertTrue(
                VineyardThreat.HEALTHY.healthPoints()
                        > VineyardThreat.ROT_RISK.healthPoints(),
                "Threat pressure should lower the vine-health contribution"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void seasonalBioticThreatsHaveReadableCounters(
            GameTestHelper helper
    ) {
        TerroirReport healthySite = new TerroirReport(
                ClimateProfile.evaluate(
                        0.65F,
                        false,
                        false,
                        72,
                        false
                ),
                SoilProfile.of(SoilType.LOAM),
                TerrainProfile.evaluate(
                        72,
                        1,
                        Direction.SOUTH,
                        true,
                        3,
                        55,
                        false,
                        false
                ),
                70
        );

        helper.assertValueEqual(
                VineyardThreat.assess(
                        true,
                        true,
                        true,
                        false,
                        false,
                        healthySite,
                        VineyardWeatherEvent.CALM,
                        SeasonalContext.atDay(18L, 8)
                ),
                VineyardThreat.BIRD_PRESSURE,
                "Unnetted ripe fruit should attract birds in autumn"
        );
        helper.assertValueEqual(
                VineyardThreat.assess(
                        true,
                        true,
                        true,
                        false,
                        true,
                        healthySite,
                        VineyardWeatherEvent.CALM,
                        SeasonalContext.atDay(18L, 8)
                ),
                VineyardThreat.HEALTHY,
                "Vineyard netting should counter autumn bird pressure"
        );
        helper.assertValueEqual(
                VineyardThreat.assess(
                        true,
                        true,
                        false,
                        false,
                        false,
                        healthySite,
                        VineyardWeatherEvent.CALM,
                        SeasonalContext.atDay(10L, 8)
                ),
                VineyardThreat.PEST_PRESSURE,
                "An unmanaged mature canopy should attract summer pests"
        );
        helper.assertValueEqual(
                VineyardThreat.assess(
                        true,
                        true,
                        false,
                        true,
                        false,
                        healthySite,
                        VineyardWeatherEvent.CALM,
                        SeasonalContext.atDay(10L, 8)
                ),
                VineyardThreat.HEALTHY,
                "Yield management should counter summer pest pressure"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void vineyardNettingInstallsAndRecoversWithShears(
            GameTestHelper helper
    ) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        Block vine = ModBlocks.redGrapevine(WoodVariant.OAK);
        BlockPos absoluteRoot = helper.absolutePos(FIRST);

        helper.setBlock(
                FIRST,
                vine.defaultBlockState()
                        .setValue(GrapevineBlock.AGE, 3)
        );
        helper.setBlock(
                UPPER,
                vine.defaultBlockState()
                        .setValue(GrapevineBlock.UPPER, true)
                        .setValue(GrapevineBlock.AGE, 3)
        );

        VineManagementSavedData management =
                VineManagementSavedData.get(helper.getLevel());
        management.remove(absoluteRoot);
        ItemStack netting = new ItemStack(ModItems.VINEYARD_NETTING);
        player.setItemInHand(InteractionHand.MAIN_HAND, netting);
        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(absoluteRoot),
                Direction.NORTH,
                absoluteRoot,
                false
        );

        helper.assertValueEqual(
                player.gameMode.useItemOn(
                        player,
                        helper.getLevel(),
                        netting,
                        InteractionHand.MAIN_HAND,
                        hit
                ),
                InteractionResult.SUCCESS,
                "A trained vine should accept vineyard netting"
        );
        helper.assertTrue(
                management.netted(absoluteRoot),
                "Installed netting should persist on the vine root"
        );
        helper.assertValueEqual(
                netting.getCount(),
                0,
                "Installing netting in survival should consume one item"
        );

        ItemStack shears = new ItemStack(Items.SHEARS);
        player.setItemInHand(InteractionHand.MAIN_HAND, shears);
        BlockPos absoluteUpper = helper.absolutePos(UPPER);
        BlockHitResult upperHit = new BlockHitResult(
                Vec3.atCenterOf(absoluteUpper),
                Direction.NORTH,
                absoluteUpper,
                false
        );
        helper.assertValueEqual(
                player.gameMode.useItemOn(
                        player,
                        helper.getLevel(),
                        shears,
                        InteractionHand.MAIN_HAND,
                        upperHit
                ),
                InteractionResult.SUCCESS,
                "Using shears on the canopy should remove installed netting"
        );

        helper.assertFalse(
                management.netted(absoluteRoot),
                "Removed netting must clear its persistent state"
        );
        helper.assertValueEqual(
                shears.getDamageValue(),
                1,
                "Recovering netting should consume one shears durability"
        );
        helper.assertTrue(
                player.getInventory().contains(
                        stack -> stack.is(ModItems.VINEYARD_NETTING)
                ),
                "Survival removal should recover the vineyard netting"
        );

        management.remove(absoluteRoot);
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void nurseryBedPropagatesEitherCutting(
            GameTestHelper helper
    ) {
        BlockState empty = ModBlocks.NURSERY_BED.defaultBlockState();
        helper.assertFalse(
                empty.getValue(NurseryBedBlock.OCCUPIED),
                "A placed nursery bed should begin empty"
        );

        BlockState planted = NurseryBedBlock.plantedState(
                empty,
                NurseryPlant.WHITE_GRAPE
        );
        helper.assertTrue(
                planted.getValue(NurseryBedBlock.OCCUPIED),
                "Inserting a cutting should occupy the nursery bed"
        );
        helper.assertValueEqual(
                planted.getValue(NurseryBedBlock.PLANT),
                NurseryPlant.WHITE_GRAPE,
                "The bed should remember the inserted cutting variety"
        );

        BlockState mature = planted.setValue(
                NurseryBedBlock.AGE,
                NurseryBedBlock.MAX_AGE
        );
        helper.assertTrue(
                NurseryBedBlock.readyToHarvest(mature),
                "A fully rooted cutting should be harvestable"
        );
        helper.assertValueEqual(
                NurseryPlant.WHITE_GRAPE.item(),
                ModItems.WHITE_GRAPE_CUTTING,
                "Harvesting should preserve the propagated variety"
        );
        helper.assertValueEqual(
                NurseryBedBlock.HARVEST_COUNT,
                3,
                "One rooted cutting should produce three cuttings"
        );
        helper.assertFalse(
                NurseryBedBlock.emptiedState(mature)
                        .getValue(NurseryBedBlock.OCCUPIED),
                "Harvesting should leave the nursery bed reusable"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void matureRootstockCanBeGraftedToACutting(
            GameTestHelper helper
    ) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        BlockState matureRootstock = NurseryBedBlock.plantedState(
                ModBlocks.NURSERY_BED.defaultBlockState(),
                NurseryPlant.RESISTANT_ROOTSTOCK
        ).setValue(
                NurseryBedBlock.AGE,
                NurseryBedBlock.MAX_AGE
        );
        helper.setBlock(FIRST, matureRootstock);

        ItemStack knife = new ItemStack(ModItems.GRAFTING_KNIFE);
        ItemStack cutting = new ItemStack(ModItems.RED_GRAPE_CUTTING);
        helper.assertValueEqual(
                GraftingKnifeItem.graftNurseryRootstock(
                        helper.getLevel(),
                        helper.absolutePos(FIRST),
                        player,
                        knife,
                        InteractionHand.MAIN_HAND,
                        cutting
                ),
                InteractionResult.SUCCESS,
                "A mature rootstock should accept a grape cutting"
        );
        helper.assertBlockProperty(
                FIRST,
                NurseryBedBlock.OCCUPIED,
                false
        );
        helper.assertValueEqual(
                cutting.getCount(),
                0,
                "Nursery grafting should consume one scion cutting"
        );
        helper.assertValueEqual(
                knife.getDamageValue(),
                1,
                "Nursery grafting should consume knife durability"
        );
        helper.assertTrue(
                player.getInventory().contains(stack ->
                        stack.is(ModItems.RED_GRAPE_CUTTING)
                                && GraftedCuttingData.rootstock(stack)
                                == VineRootstock.RESISTANT
                ),
                "The resulting cutting should carry its resistant roots"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void graftedCuttingPlantsPersistentRootstock(
            GameTestHelper helper
    ) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        BlockPos absoluteRoot = helper.absolutePos(FIRST);
        VineManagementSavedData management =
                VineManagementSavedData.get(helper.getLevel());
        management.remove(absoluteRoot);
        helper.setBlock(FIRST, ModBlocks.OAK_TRELLIS);

        ItemStack cutting = new ItemStack(ModItems.WHITE_GRAPE_CUTTING);
        GraftedCuttingData.apply(cutting, VineRootstock.ADAPTED);
        helper.assertValueEqual(
                GraftedCuttingData.rootstock(cutting),
                VineRootstock.ADAPTED,
                "The grafted cutting should carry adapted roots before use"
        );
        player.setItemInHand(InteractionHand.MAIN_HAND, cutting);
        helper.assertValueEqual(
                player.gameMode.useItemOn(
                        player,
                        helper.getLevel(),
                        cutting,
                        InteractionHand.MAIN_HAND,
                        new BlockHitResult(
                                Vec3.atCenterOf(absoluteRoot),
                                Direction.UP,
                                absoluteRoot,
                                false
                        )
                ),
                InteractionResult.SUCCESS,
                "A grafted cutting should plant on a bare trellis"
        );

        helper.assertBlockPresent(ModBlocks.WHITE_GRAPEVINE, FIRST);
        helper.assertValueEqual(
                management.rootstock(absoluteRoot),
                VineRootstock.ADAPTED,
                "Planting should preserve the cutting's selected roots"
        );
        management.remove(absoluteRoot);
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void ordinaryCuttingClearsStaleRootstockData(
            GameTestHelper helper
    ) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        BlockPos absoluteRoot = helper.absolutePos(FIRST);
        VineManagementSavedData management =
                VineManagementSavedData.get(helper.getLevel());
        management.setRootstock(
                absoluteRoot,
                VineRootstock.RESISTANT
        );
        helper.setBlock(FIRST, ModBlocks.OAK_TRELLIS);

        helper.placeAt(
                player,
                new ItemStack(ModItems.RED_GRAPE_CUTTING),
                FIRST.above(),
                Direction.DOWN
        );

        helper.assertBlockPresent(ModBlocks.RED_GRAPEVINE, FIRST);
        helper.assertValueEqual(
                management.rootstock(absoluteRoot),
                VineRootstock.OWN_ROOTS,
                "Ordinary planting should not inherit stale rootstock data"
        );
        management.remove(absoluteRoot);
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void rootstockChoicesHaveDistinctStrengths(
            GameTestHelper helper
    ) {
        helper.assertTrue(
                VineRootstock.ADAPTED.healthBonus(
                        VineyardThreat.DROUGHT_STRESS
                )
                        > VineRootstock.RESISTANT.healthBonus(
                                VineyardThreat.DROUGHT_STRESS
                        ),
                "Adapted roots should be strongest against drought"
        );
        helper.assertTrue(
                VineRootstock.RESISTANT.healthBonus(
                        VineyardThreat.ROT_RISK
                )
                        > VineRootstock.ADAPTED.healthBonus(
                                VineyardThreat.ROT_RISK
                        ),
                "Resistant roots should be strongest against rot"
        );
        helper.assertValueEqual(
                VineRootstock.OWN_ROOTS.healthBonus(
                        VineyardThreat.HEALTHY
                ),
                0,
                "Own-root vines should remain the simple baseline"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void expandedCultivarRosterHasDistinctStrategies(
            GameTestHelper helper
    ) {
        helper.assertValueEqual(
                GrapeCultivar.values().length,
                12,
                "The expanded roster should contain twelve cultivars"
        );
        helper.assertTrue(
                GrapeCultivar.SUNCREST.maximumHarvest()
                        > GrapeCultivar.EMBER_NOIR.maximumHarvest(),
                "Suncrest should trade cellar potential for higher yield"
        );
        helper.assertTrue(
                GrapeCultivar.IRONWOOD_RED.ageingPotential()
                        > GrapeCultivar.SUNCREST.ageingPotential(),
                "Ironwood Red should be the stronger cellaring choice"
        );
        helper.assertTrue(
                GrapeCultivar.FROSTLING.healthBonus(
                        VineyardThreat.FROST_DAMAGE
                ) > GrapeCultivar.HONEYCREST.healthBonus(
                        VineyardThreat.FROST_DAMAGE
                ),
                "Frostling should reward cold-climate planting"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void cultivarCuttingPlantsPersistentIdentity(
            GameTestHelper helper
    ) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        BlockPos absoluteRoot = helper.absolutePos(FIRST);
        VineManagementSavedData management =
                VineManagementSavedData.get(helper.getLevel());
        management.remove(absoluteRoot);
        helper.setBlock(FIRST, ModBlocks.OAK_TRELLIS);

        ItemStack cutting = ModItems.cultivarCutting(
                GrapeCultivar.NIGHTBERRY
        );
        player.setItemInHand(InteractionHand.MAIN_HAND, cutting);
        helper.assertValueEqual(
                player.gameMode.useItemOn(
                        player,
                        helper.getLevel(),
                        cutting,
                        InteractionHand.MAIN_HAND,
                        new BlockHitResult(
                                Vec3.atCenterOf(absoluteRoot),
                                Direction.UP,
                                absoluteRoot,
                                false
                        )
                ),
                InteractionResult.SUCCESS,
                "A named cultivar cutting should plant normally"
        );
        helper.assertValueEqual(
                management.cultivar(absoluteRoot, GrapeVariety.RED),
                GrapeCultivar.NIGHTBERRY,
                "Planting should preserve cultivar identity"
        );
        management.remove(absoluteRoot);
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void nurseryPropagationPreservesCultivar(
            GameTestHelper helper
    ) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        BlockPos absoluteBed = helper.absolutePos(FIRST);
        helper.setBlock(FIRST, ModBlocks.NURSERY_BED);
        ItemStack cutting = ModItems.cultivarCutting(
                GrapeCultivar.SILVERLEAF
        );
        player.setItemInHand(InteractionHand.MAIN_HAND, cutting);
        player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                cutting,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atCenterOf(absoluteBed),
                        Direction.UP,
                        absoluteBed,
                        false
                )
        );
        helper.setBlock(
                FIRST,
                helper.getBlockState(FIRST).setValue(
                        NurseryBedBlock.AGE,
                        NurseryBedBlock.MAX_AGE
                )
        );
        helper.useBlock(FIRST, player);

        helper.assertEntityPresent(
                EntityTypes.ITEM,
                FIRST.above(),
                2.0
        );
        boolean found = helper.getEntities(
                EntityTypes.ITEM,
                FIRST.above(),
                2.0
        ).stream().map(ItemEntity::getItem).anyMatch(stack ->
                stack.is(ModItems.WHITE_GRAPE_CUTTING)
                        && stack.getCount() == NurseryBedBlock.HARVEST_COUNT
                        && GraftedCuttingData.cultivar(
                                stack,
                                GrapeVariety.WHITE
                        ) == GrapeCultivar.SILVERLEAF
        );
        helper.assertTrue(
                found,
                "Nursery propagation should preserve the named cultivar"
        );
        VineManagementSavedData.get(helper.getLevel()).remove(absoluteBed);
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void cultivarIdentityStacksAndReachesWineProvenance(
            GameTestHelper helper
    ) {
        ItemStack first = new ItemStack(ModItems.RED_GRAPES, 3);
        ItemStack second = new ItemStack(ModItems.RED_GRAPES, 4);
        ItemStack different = new ItemStack(ModItems.RED_GRAPES, 4);
        WineMetadata.applyProfile(first, 2, WineQualityProfile.vineyard(55));
        WineMetadata.applyProfile(second, 2, WineQualityProfile.vineyard(55));
        WineMetadata.applyProfile(different, 2, WineQualityProfile.vineyard(55));
        WineMetadata.applyCultivar(first, GrapeCultivar.IRONWOOD_RED);
        WineMetadata.applyCultivar(second, GrapeCultivar.IRONWOOD_RED);
        WineMetadata.applyCultivar(different, GrapeCultivar.VALE_PINOT);
        helper.assertTrue(
                ItemStack.isSameItemSameComponents(first, second),
                "Matching cultivar harvests should remain stackable"
        );
        helper.assertFalse(
                ItemStack.isSameItemSameComponents(first, different),
                "Different cultivars should retain distinct harvest identity"
        );

        helper.setBlock(FIRST, ModBlocks.GRAPE_PRESS);
        ItemStack pressLoad = first.copy();
        pressLoad.setCount(GrapePressBlockEntity.GRAPES_PER_PRESS);
        GrapePressBlockEntity press = helper.getBlockEntity(
                FIRST,
                GrapePressBlockEntity.class
        );
        press.insert(pressLoad, pressLoad.getCount());
        helper.assertTrue(
                press.press(helper.makeMockServerPlayerInLevel()),
                "Cultivar grapes should press normally"
        );
        helper.assertValueEqual(
                WineMetadata.provenance(press.bottleOneMust()).variety(),
                GrapeCultivar.IRONWOOD_RED.serializedName(),
                "Pressing should establish cultivar-specific provenance"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void cultivarAgeingPotentialChangesWineTimeline(
            GameTestHelper helper
    ) {
        long cellarAge = 50L * 24000L;
        WineAgeStage fastAgeing = WineAgeStage.from(
                cellarAge,
                0,
                WineQuality.TABLE,
                GrapeCultivar.SUNCREST.ageingMultiplier()
        );
        WineAgeStage cellarWorthy = WineAgeStage.from(
                cellarAge,
                0,
                WineQuality.TABLE,
                GrapeCultivar.IRONWOOD_RED.ageingMultiplier()
        );

        helper.assertValueEqual(
                fastAgeing,
                WineAgeStage.PEAK,
                "Low-ageing-potential cultivars should reach their peak sooner"
        );
        helper.assertValueEqual(
                cellarWorthy,
                WineAgeStage.MATURE,
                "Cellar-worthy cultivars should develop more slowly"
        );
        helper.succeed();
    }
}
