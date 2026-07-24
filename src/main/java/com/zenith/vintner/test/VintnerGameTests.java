package com.zenith.vintner.test;

import com.zenith.vintner.block.AgingBarrelBlock;
import com.zenith.vintner.block.FermentationBarrelBlock;
import com.zenith.vintner.block.GrapevineBlock;
import com.zenith.vintner.block.TrellisBlock;
import com.zenith.vintner.block.entity.AgingBarrelBlockEntity;
import com.zenith.vintner.block.entity.FermentationBarrelBlockEntity;
import com.zenith.vintner.block.entity.GrapePressBlockEntity;
import com.zenith.vintner.item.WineEffectProfile;
import com.zenith.vintner.registry.ModAttachments;
import com.zenith.vintner.registry.ModBlocks;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.wine.WineConsumptionManager;
import com.zenith.vintner.wine.WineConsumptionState;
import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineQuality;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public final class VintnerGameTests {
    private static final BlockPos FIRST = new BlockPos(2, 1, 2);
    private static final BlockPos EAST = FIRST.east();
    private static final BlockPos UPPER = FIRST.above();

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
                WineQuality.COMMON
        );

        helper.assertTrue(
                WineEffectProfile.RED.isActive(player),
                "The first wine profile should become active"
        );

        WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.WHITE,
                WineQuality.COMMON
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
                WineQuality.COMMON
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
                WineQuality.COMMON
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
                WineQuality.COMMON
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
                WineQuality.COMMON
        );
        int firstDuration =
                WineEffectProfile.RED.remainingDuration(player);

        var second = WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.RED,
                WineQuality.COMMON
        );
        int secondDuration =
                WineEffectProfile.RED.remainingDuration(player);

        var third = WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.RED,
                WineQuality.COMMON
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
                    WineQuality.COMMON
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
                WineQuality.COMMON
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
                WineQuality.COMMON
        );
        WineConsumptionManager.consume(
                helper.getLevel(),
                player,
                WineEffectProfile.WHITE,
                WineQuality.COMMON
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

        helper.assertTrue(barrel.insertOne(must), "Must should be accepted");

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
    public void agingCompletesAndImprovesQuality(
            GameTestHelper helper
    ) {
        helper.setBlock(FIRST, ModBlocks.AGING_BARREL);

        AgingBarrelBlockEntity barrel = helper.getBlockEntity(
                FIRST,
                AgingBarrelBlockEntity.class
        );
        ItemStack wine = new ItemStack(ModItems.RED_WINE);
        WineMetadata.apply(wine, 4, WineQuality.COMMON);

        helper.assertTrue(barrel.insertOne(wine), "Wine should be accepted");

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
                WineQuality.FINE,
                "Aging should improve common wine to fine quality"
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
}
