package com.zenith.vintner.test;

import com.zenith.vintner.block.AgingBarrelBlock;
import com.zenith.vintner.block.CellarGlassColor;
import com.zenith.vintner.block.CellarCollectionBlock;
import com.zenith.vintner.block.FermentationBarrelBlock;
import com.zenith.vintner.block.GrapevineBlock;
import com.zenith.vintner.block.TrellisBlock;
import com.zenith.vintner.block.WineBottleBlock;
import com.zenith.vintner.block.WineCrateBlock;
import com.zenith.vintner.block.WineRackBlock;
import com.zenith.vintner.block.WoodVariant;
import com.zenith.vintner.block.entity.AgingBarrelBlockEntity;
import com.zenith.vintner.block.entity.CellarCollectionBlockEntity;
import com.zenith.vintner.block.entity.FermentationBarrelBlockEntity;
import com.zenith.vintner.block.entity.GrapePressBlockEntity;
import com.zenith.vintner.block.entity.TastingServiceBlockEntity;
import com.zenith.vintner.block.entity.VintageArchiveBlockEntity;
import com.zenith.vintner.block.entity.WineCrateBlockEntity;
import com.zenith.vintner.block.entity.WineBottleBlockEntity;
import com.zenith.vintner.block.entity.WineRackBlockEntity;
import com.zenith.vintner.item.WineEffectProfile;
import com.zenith.vintner.item.FilledWineGlassItem;
import com.zenith.vintner.registry.ModAttachments;
import com.zenith.vintner.registry.ModBlockEntities;
import com.zenith.vintner.registry.ModBlocks;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.wine.CellarConditions;
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
import com.zenith.vintner.wine.WineReadiness;
import com.zenith.vintner.wine.WineTastingProfile;
import com.zenith.vintner.wine.WineStyle;
import net.minecraft.advancements.AdvancementHolder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.DyeColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class VintnerGameTests {
    private static final BlockPos FIRST = new BlockPos(2, 1, 2);
    private static final BlockPos EAST = FIRST.east();
    private static final BlockPos UPPER = FIRST.above();

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
    public void partialBottleDrinkScalesConsumptionAndReturnsBottle(
            GameTestHelper helper
    ) {
        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        ItemStack wine = new ItemStack(ModItems.RED_WINE);
        WineMetadata.setServings(wine, 2);

        ItemStack result = wine.finishUsingItem(
                helper.getLevel(),
                player
        );

        helper.assertTrue(
                result.is(Items.GLASS_BOTTLE),
                "Drinking a partial bottle should return its empty bottle"
        );
        helper.assertValueEqual(
                WineConsumptionManager.state(
                        player,
                        helper.getLevel().getGameTime()
                ).effectiveServingUnits(),
                2,
                "Two remaining servings should count as half a bottle"
        );
        helper.assertValueEqual(
                WineEffectProfile.RED.remainingDuration(player),
                Math.round(
                        20 * 20
                                * WineQuality.TABLE.durationMultiplier()
                                * WineMetadata.ageStage(wine)
                                        .benefitMultiplier()
                                * 0.5F
                ),
                "Half a table-red bottle should give half its base duration"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void fourPouredGlassesEqualOneBottleEffect(
            GameTestHelper helper
    ) {
        var glassTaster = helper.makeMockServerPlayerInLevel();
        var bottleTaster = helper.makeMockServerPlayerInLevel();
        glassTaster.setGameMode(GameType.SURVIVAL);
        bottleTaster.setGameMode(GameType.SURVIVAL);
        ItemStack source = new ItemStack(ModItems.RED_WINE);
        WineMetadata.apply(source, 1, WineQuality.TABLE);
        WineMetadata.setEffectProfile(source, WineEffectProfile.RED.id());

        for (int serving = 0; serving < 4; serving++) {
            ItemStack glass = FilledWineGlassItem.fromBottle(source);
            ItemStack result = glass.finishUsingItem(
                    helper.getLevel(),
                    glassTaster
            );
            helper.assertTrue(
                    result.is(ModItems.WINE_GLASS),
                    "Every poured serving should return a reusable wine glass"
            );
        }

        WineConsumptionManager.consume(
                helper.getLevel(),
                bottleTaster,
                WineEffectProfile.RED,
                WineQuality.TABLE,
                WineMetadata.ageStage(source)
        );

        helper.assertValueEqual(
                WineConsumptionManager.state(
                        glassTaster,
                        helper.getLevel().getGameTime()
                ).effectiveServingUnits(),
                4,
                "Four glasses should equal one tracked bottle"
        );
        helper.assertValueEqual(
                WineEffectProfile.RED.remainingDuration(glassTaster),
                WineEffectProfile.RED.remainingDuration(bottleTaster),
                "Four servings should equal one full bottle effect"
        );
        helper.assertFalse(
                glassTaster.hasEffect(MobEffects.NAUSEA),
                "One bottle divided into glasses should not cause impairment"
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
        long batchId = WineMetadata.batchId(must);

        helper.assertTrue(
                batchId != 0L,
                "Pressing should establish a stable batch identity"
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
        helper.assertValueEqual(
                WineMetadata.batchId(wine),
                batchId,
                "Fermentation must preserve batch identity"
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
                batchId,
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
        WineMetadata.setServings(wine, 4);
        WineMetadata.setEffectProfile(wine, WineEffectProfile.AGED_RED.id());
        WineMetadata.ensureDefaults(wine);
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
                "red",
                "Batch provenance should identify the grape variety"
        );
        helper.assertValueEqual(
                provenance.producerName(),
                producer.getGameProfile().name(),
                "Batch provenance should identify the producer"
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

        helper.assertTrue(
                ItemStack.isSameItemSameComponents(first, second),
                "Matching grapes must stack regardless of source vine"
        );
        helper.assertTrue(
                !WineMetadata.provenance(first).known()
                        && !WineMetadata.provenance(second).known(),
                "Provenance should begin when a batch is pressed"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void wineBottlePlacementPreservesExactMetadata(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, Blocks.STONE);

        ItemStack wine = new ItemStack(ModItems.AGED_RED_WINE);
        WineMetadata.apply(wine, 4, WineQuality.EXCEPTIONAL);
        WineMetadata.ensureBatchIdentity(wine, 99117L);
        WineMetadata.applyProvenance(
                wine,
                new WineProvenance(
                        "aged_red",
                        1234L,
                        "minecraft:overworld",
                        7,
                        8,
                        9,
                        "VintnerDev",
                        "VintnerDev"
                )
        );
        WineMetadata.assignBottleNumber(wine, 3, 4);
        WineMetadata.ensureDefaults(wine);
        WineMetadata.setEffectProfile(
                wine,
                WineEffectProfile.AGED_RED.id()
        );

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        player.setItemInHand(InteractionHand.MAIN_HAND, wine.copy());
        BlockPos lowerPos = helper.absolutePos(FIRST);

        player.gameMode.useItemOn(
                player,
                helper.getLevel(),
                player.getItemInHand(InteractionHand.MAIN_HAND),
                InteractionHand.MAIN_HAND,
                new BlockHitResult(
                        Vec3.atCenterOf(lowerPos),
                        Direction.UP,
                        lowerPos,
                        false
                )
        );

        helper.assertBlockPresent(ModBlocks.WINE_BOTTLE, UPPER);
        WineBottleBlockEntity bottleEntity = helper.getBlockEntity(
                UPPER,
                WineBottleBlockEntity.class
        );
        ItemStack stored = bottleEntity.getBottleCopy();

        helper.assertTrue(
                ItemStack.isSameItemSameComponents(wine, stored),
                "Placed bottles must preserve every item component"
        );
        helper.assertValueEqual(
                player.getItemInHand(InteractionHand.MAIN_HAND).getCount(),
                0,
                "Placing a bottle should consume one held bottle"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void wineBottleSerializationPreservesMetadata(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.WINE_BOTTLE);
        WineBottleBlockEntity original = helper.getBlockEntity(
                FIRST,
                WineBottleBlockEntity.class
        );
        ItemStack wine = new ItemStack(ModItems.WHITE_WINE);
        WineMetadata.apply(wine, 2, WineQuality.FINE);
        WineMetadata.ensureBatchIdentity(wine, 99118L);
        WineMetadata.assignBottleNumber(wine, 1, 2);
        WineMetadata.setServings(wine, 4);
        WineMetadata.setEffectProfile(wine, WineEffectProfile.WHITE.id());
        original.setBottle(wine);

        BlockEntity restored = reload(helper, original);

        helper.assertTrue(
                restored instanceof WineBottleBlockEntity,
                "A placed bottle must deserialize as its bottle entity"
        );
        helper.assertTrue(
                ItemStack.isSameItemSameComponents(
                        wine,
                        ((WineBottleBlockEntity) restored).getBottleCopy()
                ),
                "Bottle metadata must survive save and reload"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void wineBottleCanBeRemovedWithoutLosingMetadata(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.WINE_BOTTLE);
        WineBottleBlockEntity bottleEntity = helper.getBlockEntity(
                FIRST,
                WineBottleBlockEntity.class
        );
        ItemStack wine = new ItemStack(ModItems.AGED_WHITE_WINE);
        WineMetadata.apply(wine, 3, WineQuality.GOOD);
        WineMetadata.ensureBatchIdentity(wine, 99119L);
        WineMetadata.setServings(wine, 4);
        WineMetadata.setEffectProfile(wine, WineEffectProfile.AGED_WHITE.id());
        bottleEntity.setBottle(wine);

        ItemStack removed = bottleEntity.takeBottle();

        helper.assertTrue(
                ItemStack.isSameItemSameComponents(wine, removed),
                "Removing a bottle must return the exact stored stack"
        );
        helper.assertTrue(
                bottleEntity.getBottleCopy().isEmpty(),
                "Removing a bottle must leave the block empty"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void creativeWineBottleBreakDropsStoredBottle(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.WINE_BOTTLE);
        WineBottleBlockEntity bottleEntity = helper.getBlockEntity(
                FIRST,
                WineBottleBlockEntity.class
        );
        ItemStack wine = new ItemStack(ModItems.AGED_RED_WINE);
        WineMetadata.apply(wine, 4, WineQuality.EXCEPTIONAL);
        WineMetadata.ensureBatchIdentity(wine, 99120L);
        WineMetadata.assignBottleNumber(wine, 4, 4);
        WineMetadata.setServings(wine, 4);
        WineMetadata.setEffectProfile(wine, WineEffectProfile.AGED_RED.id());
        bottleEntity.setBottle(wine);

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);
        player.gameMode.destroyBlock(helper.absolutePos(FIRST));

        helper.assertBlockNotPresent(ModBlocks.WINE_BOTTLE, FIRST);
        helper.assertItemEntityPresent(
                ModItems.AGED_RED_WINE,
                FIRST,
                2.0
        );
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(
                ItemEntity.class,
                new AABB(helper.absolutePos(FIRST)).inflate(2.0)
        );
        helper.assertTrue(
                drops.stream().anyMatch(drop ->
                        WineMetadata.batchId(drop.getItem()) == 99120L
                                && WineMetadata.bottleNumber(
                                drop.getItem()
                        ) == 4
                ),
                "Creative breaking must preserve placed bottle metadata"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void placedBottlePoursFourExactMetadataServings(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.WINE_BOTTLE);
        WineBottleBlockEntity bottleEntity = helper.getBlockEntity(
                FIRST,
                WineBottleBlockEntity.class
        );
        ItemStack wine = new ItemStack(ModItems.AGED_RED_WINE);
        WineMetadata.apply(wine, 7, WineQuality.EXCEPTIONAL);
        WineMetadata.ensureBatchIdentity(wine, 99121L);
        WineMetadata.assignBottleNumber(wine, 2, 4);
        WineMetadata.setEffectProfile(
                wine,
                WineEffectProfile.AGED_RED.id()
        );
        bottleEntity.setBottle(wine);

        for (int remaining = 3; remaining >= 0; remaining--) {
            ItemStack glass = bottleEntity.pourServing();
            helper.assertTrue(
                    glass.is(ModItems.FILLED_WINE_GLASS),
                    "Each valid pour should create one filled glass"
            );
            helper.assertValueEqual(
                    WineMetadata.batchId(glass),
                    99121L,
                    "A poured glass must preserve batch identity"
            );
            helper.assertValueEqual(
                    WineMetadata.bottleNumber(glass),
                    2,
                    "A poured glass must preserve bottle numbering"
            );
            helper.assertValueEqual(
                    WineMetadata.effectProfile(glass),
                    WineEffectProfile.AGED_RED.id(),
                    "A poured glass must preserve the source effect profile"
            );
            helper.assertValueEqual(
                    bottleEntity.servings(),
                    remaining,
                    "A pour must remove exactly one serving"
            );
            helper.assertBlockProperty(
                    FIRST,
                    WineBottleBlock.SERVINGS,
                    remaining
            );
        }

        helper.assertTrue(
                bottleEntity.pourServing().isEmpty(),
                "An empty bottle must reject additional pours"
        );
        helper.assertTrue(
                bottleEntity.getBottleCopy().is(Items.GLASS_BOTTLE),
                "The final pour should leave a reusable empty bottle"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void partialPlacedBottlePersistsWithoutDuplication(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.WINE_BOTTLE);
        WineBottleBlockEntity bottleEntity = helper.getBlockEntity(
                FIRST,
                WineBottleBlockEntity.class
        );
        ItemStack wine = new ItemStack(ModItems.WHITE_WINE);
        WineMetadata.ensureBatchIdentity(wine, 99122L);
        WineMetadata.setEffectProfile(wine, WineEffectProfile.WHITE.id());
        bottleEntity.setBottle(wine);

        helper.assertFalse(
                bottleEntity.pourServing().isEmpty(),
                "The first serving should pour"
        );
        helper.assertFalse(
                bottleEntity.pourServing().isEmpty(),
                "The second serving should pour"
        );

        WineBottleBlockEntity restored =
                (WineBottleBlockEntity) reload(helper, bottleEntity);
        helper.assertValueEqual(
                restored.servings(),
                2,
                "A partial bottle must preserve its remaining servings"
        );
        ItemStack recovered = restored.takeBottle();
        helper.assertValueEqual(
                WineMetadata.servings(recovered),
                2,
                "Removing a partial bottle must not restore consumed servings"
        );
        helper.assertValueEqual(
                WineMetadata.batchId(recovered),
                99122L,
                "Removing a partial bottle must preserve its batch"
        );
        helper.assertTrue(
                restored.takeBottle().isEmpty(),
                "A bottle entity may surrender its stored bottle only once"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void tastingServicePreservesBottleMetadataAndServings(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.TASTING_SERVICE);
        TastingServiceBlockEntity service = helper.getBlockEntity(
                FIRST,
                TastingServiceBlockEntity.class
        );
        ItemStack wine = new ItemStack(ModItems.AGED_RED_WINE);
        WineMetadata.apply(wine, 8, WineQuality.EXCEPTIONAL);
        WineMetadata.ensureBatchIdentity(wine, 81301L);
        WineMetadata.assignBottleNumber(wine, 2, 4);
        WineMetadata.setServings(wine, 4);
        WineMetadata.setEffectProfile(
                wine,
                WineEffectProfile.AGED_RED.id()
        );

        helper.assertTrue(
                service.insertBottle(wine),
                "The service should accept a full bottle"
        );
        helper.assertFalse(
                service.insertBottle(wine),
                "The service must reject a second bottle"
        );
        helper.assertValueEqual(
                service.servings(),
                4,
                "A full bottle should expose four tastings"
        );

        ItemStack serving = service.pourServing();
        helper.assertTrue(
                serving.is(ModItems.FILLED_WINE_GLASS),
                "Pouring should return the existing filled-glass item"
        );
        helper.assertValueEqual(
                WineMetadata.batchId(serving),
                81301L,
                "A serving must retain its source batch"
        );
        helper.assertValueEqual(
                service.servings(),
                3,
                "One pour must consume exactly one serving"
        );

        TastingServiceBlockEntity restored =
                (TastingServiceBlockEntity) reload(helper, service);
        helper.assertValueEqual(
                restored.servings(),
                3,
                "Remaining servings must survive save and reload"
        );
        ItemStack removed = restored.removeBottle();
        helper.assertValueEqual(
                WineMetadata.batchId(removed),
                81301L,
                "Removing the bottle must preserve its batch"
        );
        helper.assertValueEqual(
                WineMetadata.bottleNumber(removed),
                2,
                "Removing the bottle must preserve its bottle number"
        );
        helper.assertValueEqual(
                WineMetadata.servings(removed),
                3,
                "Removing the bottle must preserve remaining servings"
        );
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void tastingServiceTracksWhiteWineVisualState(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.TASTING_SERVICE);
        TastingServiceBlockEntity service = helper.getBlockEntity(
                FIRST,
                TastingServiceBlockEntity.class
        );
        ItemStack white = new ItemStack(ModItems.AGED_WHITE_WINE);
        WineMetadata.ensureDefaults(white);
        WineMetadata.setServings(white, 4);
        WineMetadata.setEffectProfile(
                white,
                WineEffectProfile.WHITE.id()
        );

        helper.assertTrue(
                service.insertBottle(white),
                "The service should accept white wine"
        );
        helper.assertTrue(
                helper.getBlockState(FIRST).getValue(
                        com.zenith.vintner.block.TastingServiceBlock.WHITE_WINE
                ),
                "White wine should select the white visual state"
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
}
