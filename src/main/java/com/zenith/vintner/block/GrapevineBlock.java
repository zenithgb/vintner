package com.zenith.vintner.block;

import com.zenith.vintner.registry.ModBlocks;
import com.zenith.vintner.vineyard.GrapeVariety;
import com.zenith.vintner.wine.GrapeQualityEvaluator;
import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineQuality;
import com.zenith.vintner.wine.VineyardConditionReport;
import com.zenith.vintner.wine.WinemakingEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GrapevineBlock
        extends TrellisBlock
        implements BonemealableBlock {

    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final BooleanProperty UPPER =
            BooleanProperty.create("upper");

    private static final VoxelShape YOUNG_VINE_SHAPE =
            Block.box(5.0, 0.0, 5.0, 11.0, 12.0, 11.0);

    private static final VoxelShape LOWER_VINE_SHAPE =
            Block.box(3.0, 0.0, 3.0, 13.0, 16.0, 13.0);

    private static final VoxelShape UPPER_VINE_SHAPE =
            Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

    private final GrapeVariety variety;

    protected GrapevineBlock(
            GrapeVariety variety,
            WoodVariant woodVariant,
            BlockBehaviour.Properties properties
    ) {
        super(woodVariant, properties);
        this.variety = variety;

        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(NORTH, RowConnection.NONE)
                        .setValue(EAST, RowConnection.NONE)
                        .setValue(SOUTH, RowConnection.NONE)
                        .setValue(WEST, RowConnection.NONE)
                        .setValue(ISOLATED, false)
                        .setValue(HAS_ABOVE, false)
                        .setValue(HAS_BELOW, false)
                        .setValue(UPPER, false)
                        .setValue(AGE, 0)
        );
    }

    protected abstract Item getGrapeItem();

    protected abstract Item getCuttingItem();

    public GrapeVariety getVariety() {
        return variety;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        if (state.getValue(UPPER)) {
            return UPPER_VINE_SHAPE;
        }

        return state.getValue(AGE) == 0
                ? YOUNG_VINE_SHAPE
                : LOWER_VINE_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return super.getCollisionShape(
                state,
                level,
                pos,
                context
        );
    }

    @Override
    public void destroy(
            LevelAccessor level,
            BlockPos pos,
            BlockState state
    ) {
        BlockPos rootPos = state.getValue(UPPER)
                ? pos.below()
                : pos;
        BlockPos upperPos = rootPos.above();
        BlockState rootState = state.getValue(UPPER)
                ? level.getBlockState(rootPos)
                : state;
        BlockState upperState = state.getValue(UPPER)
                ? state
                : level.getBlockState(upperPos);

        if (!state.getValue(UPPER)
                || isMatchingLower(rootState)) {
            restoreBareTrellis(level, rootPos, rootState);
        }

        if (state.getValue(UPPER)
                || isTrellisState(upperState)) {
            restoreBareTrellis(level, upperPos, upperState);
        }

        super.destroy(level, pos, state);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction directionToNeighbour,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random
    ) {
        if (state.getValue(UPPER)
                && directionToNeighbour == Direction.DOWN) {
            if (isMatchingLower(neighbourState)
                    && neighbourState.getValue(AGE) >= 2) {
                state = state.setValue(
                        AGE,
                        neighbourState.getValue(AGE)
                );
            } else {
                return copyTrellisProperties(
                        state,
                        ModBlocks.trellis(woodVariant())
                                .defaultBlockState()
                ).setValue(HAS_BELOW, false);
            }
        }

        if (!state.getValue(UPPER)
                && directionToNeighbour == Direction.UP
                && state.getValue(AGE) >= 2
                && !isMatchingUpper(neighbourState)) {
            state = state.setValue(AGE, 1);
        }

        return super.updateShape(
                state,
                level,
                ticks,
                pos,
                directionToNeighbour,
                neighbourPos,
                neighbourState,
                random
        );
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return !state.getValue(UPPER);
    }

    @Override
    protected void randomTick(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random
    ) {
        if (state.getValue(UPPER)) {
            return;
        }

        int age = state.getValue(AGE);

        if (age >= 2
                && !isMatchingUpper(
                        level.getBlockState(pos.above())
                )) {
            normalizeLinkedStructure(level, pos, state);
            return;
        }

        if (age < MAX_AGE
                && random.nextInt(
                        variety.growthChanceDenominator()
                ) == 0
                && level.getRawBrightness(pos.above(2), 0) >= 9) {
            advanceGrowth(level, pos, state);
        }
    }

    private boolean advanceGrowth(
            ServerLevel level,
            BlockPos rootPos,
            BlockState rootState
    ) {
        int age = rootState.getValue(AGE);

        if (age == 0) {
            BlockState grownState = rootState.setValue(AGE, 1);
            level.setBlock(rootPos, grownState, Block.UPDATE_ALL);
            level.gameEvent(
                    GameEvent.BLOCK_CHANGE,
                    rootPos,
                    GameEvent.Context.of(grownState)
            );
            return true;
        }

        BlockPos upperPos = rootPos.above();
        BlockState upperState = level.getBlockState(upperPos);

        if (age >= 2 && !isMatchingUpper(upperState)) {
            return normalizeLinkedStructure(
                    level,
                    rootPos,
                    rootState
            );
        }

        if (age == 1) {
            if (!isAvailableUpperTrellis(upperState)
                    && !isMatchingUpper(upperState)) {
                return false;
            }

            setLinkedAge(
                    level,
                    rootPos,
                    rootState,
                    upperState,
                    2
            );
            return true;
        }

        if (age == 2 && isMatchingUpper(upperState)) {
            setLinkedAge(
                    level,
                    rootPos,
                    rootState,
                    upperState,
                    3
            );
            return true;
        }

        return false;
    }

    private boolean normalizeLinkedStructure(
            ServerLevel level,
            BlockPos rootPos,
            BlockState rootState
    ) {
        BlockPos upperPos = rootPos.above();
        BlockState upperState = level.getBlockState(upperPos);

        if (isMatchingUpper(upperState)) {
            return false;
        }

        if (isAvailableUpperTrellis(upperState)) {
            setLinkedAge(
                    level,
                    rootPos,
                    rootState,
                    upperState,
                    rootState.getValue(AGE)
            );
            return true;
        }

        BlockState resetState = rootState.setValue(AGE, 1);
        level.setBlock(rootPos, resetState, Block.UPDATE_ALL);
        level.gameEvent(
                GameEvent.BLOCK_CHANGE,
                rootPos,
                GameEvent.Context.of(resetState)
        );
        return true;
    }

    private void setLinkedAge(
            ServerLevel level,
            BlockPos rootPos,
            BlockState rootState,
            BlockState upperState,
            int age
    ) {
        BlockState grownRoot = rootState
                .setValue(UPPER, false)
                .setValue(AGE, age);

        BlockState grownUpper = isMatchingUpper(upperState)
                ? upperState.setValue(AGE, age)
                : copyTrellisProperties(
                        upperState,
                        ModBlocks.grapevine(
                                variety,
                                ((TrellisBlock) upperState.getBlock())
                                        .woodVariant()
                        ).defaultBlockState()
                )
                .setValue(UPPER, true)
                .setValue(AGE, age);

        level.setBlock(
                rootPos.above(),
                grownUpper,
                Block.UPDATE_ALL
        );
        level.setBlock(rootPos, grownRoot, Block.UPDATE_ALL);
        level.gameEvent(
                GameEvent.BLOCK_CHANGE,
                rootPos.above(),
                GameEvent.Context.of(grownUpper)
        );
    }

    private boolean isMatchingUpper(BlockState state) {
        return state.getBlock() instanceof GrapevineBlock grapevine
                && grapevine.variety == variety
                && state.getValue(UPPER);
    }

    private boolean isMatchingLower(BlockState state) {
        return state.getBlock() instanceof GrapevineBlock grapevine
                && grapevine.variety == variety
                && !state.getValue(UPPER);
    }

    private static boolean isAvailableUpperTrellis(
            BlockState state
    ) {
        return state.getBlock() instanceof TrellisBlock
                && !(state.getBlock() instanceof GrapevineBlock);
    }

    private static BlockState copyTrellisProperties(
            BlockState source,
            BlockState target
    ) {
        return target
                .setValue(FACING, source.getValue(FACING))
                .setValue(NORTH, source.getValue(NORTH))
                .setValue(EAST, source.getValue(EAST))
                .setValue(SOUTH, source.getValue(SOUTH))
                .setValue(WEST, source.getValue(WEST))
                .setValue(ISOLATED, source.getValue(ISOLATED))
                .setValue(HAS_ABOVE, source.getValue(HAS_ABOVE))
                .setValue(HAS_BELOW, source.getValue(HAS_BELOW));
    }

    private static void restoreBareTrellis(
            LevelAccessor level,
            BlockPos pos,
            BlockState source
    ) {
        WoodVariant woodVariant =
                ((TrellisBlock) source.getBlock()).woodVariant();

        BlockState restored = copyTrellisProperties(
                source,
                ModBlocks.trellis(woodVariant).defaultBlockState()
        )
                .setValue(
                        HAS_ABOVE,
                        isTrellisState(
                                level.getBlockState(pos.above())
                        )
                )
                .setValue(
                        HAS_BELOW,
                        isTrellisState(
                                level.getBlockState(pos.below())
                        )
                );

        level.setBlock(pos, restored, Block.UPDATE_ALL);
    }

    private static Component conditionText(
            boolean favorable
    ) {
        return Component.translatable(
                favorable
                        ? "vineyard_condition.vintner.favorable"
                        : "vineyard_condition.vintner.poor"
        );
    }

    private static Component qualityText(
            WineQuality quality
    ) {
        return quality.displayName();
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (stack.is(Items.SHEARS)) {
            InteractionResult pruning = tryPruneCutting(
                    stack,
                    state,
                    level,
                    pos,
                    player,
                    hand
            );

            if (pruning.consumesAction()) {
                return pruning;
            }
        }

        if (state.getValue(UPPER)) {
            InteractionResult result =
                    VineyardSoilInteraction.useOnSoilBelow(
                            stack,
                            level,
                            pos.below(),
                            player
                    );

            if (result.consumesAction()) {
                return result;
            }
        }

        return super.useItemOn(
                stack,
                state,
                level,
                pos,
                player,
                hand,
                hitResult
        );
    }

    private InteractionResult tryPruneCutting(
            ItemStack shears,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand
    ) {
        BlockPos rootPos = state.getValue(UPPER)
                ? pos.below()
                : pos;
        BlockState rootState = level.getBlockState(rootPos);
        BlockState upperState = level.getBlockState(rootPos.above());

        if (!isMatchingLower(rootState)
                || rootState.getValue(AGE) < MAX_AGE
                || !isMatchingUpper(upperState)
                || upperState.getValue(AGE) < MAX_AGE) {
            return InteractionResult.PASS;
        }

        if (level instanceof ServerLevel serverLevel) {
            BlockState prunedRoot = rootState.setValue(AGE, 2);
            BlockState prunedUpper = upperState.setValue(AGE, 2);

            serverLevel.setBlock(
                    rootPos,
                    prunedRoot,
                    Block.UPDATE_ALL
            );
            serverLevel.setBlock(
                    rootPos.above(),
                    prunedUpper,
                    Block.UPDATE_ALL
            );
            Block.popResource(
                    serverLevel,
                    rootPos.above(),
                    new ItemStack(getCuttingItem())
            );
            serverLevel.playSound(
                    null,
                    rootPos.above(),
                    SoundEvents.SHEEP_SHEAR,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.1F
            );
            shears.hurtAndBreak(1, player, hand);
            serverLevel.gameEvent(
                    GameEvent.SHEAR,
                    rootPos.above(),
                    GameEvent.Context.of(player, prunedUpper)
            );
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        BlockPos rootPos = state.getValue(UPPER)
                ? pos.below()
                : pos;
        BlockState rootState = level.getBlockState(rootPos);

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                VineyardConditionReport report =
                        GrapeQualityEvaluator.inspect(
                                level,
                                rootPos
                        );

                player.sendSystemMessage(
                        Component.translatable(
                                "message.vintner.vineyard_conditions",
                                conditionText(report.openSky()),
                                conditionText(
                                        report.suitableTemperature()
                                ),
                                conditionText(
                                        report.precipitation()
                                ),
                                conditionText(
                                        report.preparedSoil()
                                ),
                                conditionText(report.matureVine()),
                                conditionText(report.healthyVine()),
                                conditionText(report.managedYield()),
                                conditionText(report.ripeHarvest()),
                                conditionText(
                                        report.dryHarvestWeather()
                                ),
                                qualityText(
                                        report.predictedQuality()
                                ),
                                report.qualityScore()
                        )
                );
            }

            return InteractionResult.SUCCESS;
        }

        if (!state.getValue(UPPER)
                || state.getValue(AGE) < MAX_AGE
                || !isMatchingLower(rootState)
                || rootState.getValue(AGE) < MAX_AGE) {
            return super.useWithoutItem(
                    state,
                    level,
                    pos,
                    player,
                    hitResult
            );
        }

        if (level instanceof ServerLevel serverLevel) {
            int harvestRange = variety.maximumHarvest()
                    - variety.minimumHarvest()
                    + 1;

            int grapeCount = variety.minimumHarvest()
                    + serverLevel.getRandom().nextInt(harvestRange);

            ItemStack grapes = new ItemStack(
                    getGrapeItem(),
                    grapeCount
            );

            int vintage = WineMetadata.vintageFromGameTime(
                    serverLevel.getGameTime()
            );

            VineyardConditionReport report =
                    GrapeQualityEvaluator.inspect(
                            serverLevel,
                            rootPos
                    );

            WineMetadata.applyProfile(
                    grapes,
                    vintage,
                    report.qualityProfile()
            );
            Block.popResource(
                    serverLevel,
                    pos,
                    grapes
            );

            serverLevel.playSound(
                    null,
                    pos,
                    SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                    SoundSource.BLOCKS,
                    1.0F,
                    0.8F
                            + serverLevel.getRandom().nextFloat()
                            * 0.4F
            );

            WinemakingEffects.harvest(
                    serverLevel,
                    pos,
                    getGrapeItem()
            );

            BlockState harvestedUpper = state.setValue(AGE, 2);
            BlockState harvestedRoot = rootState.setValue(AGE, 2);

            serverLevel.setBlock(
                    rootPos,
                    harvestedRoot,
                    Block.UPDATE_ALL
            );
            serverLevel.setBlock(
                    pos,
                    harvestedUpper,
                    Block.UPDATE_ALL
            );

            serverLevel.gameEvent(
                    GameEvent.BLOCK_CHANGE,
                    pos,
                    GameEvent.Context.of(player, harvestedUpper)
            );
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemStack getCloneItemStack(
            LevelReader level,
            BlockPos pos,
            BlockState state,
            boolean includeData
    ) {
        return new ItemStack(getCuttingItem());
    }

    @Override
    public boolean isValidBonemealTarget(
            LevelReader level,
            BlockPos pos,
            BlockState state
    ) {
        BlockPos rootPos = state.getValue(UPPER)
                ? pos.below()
                : pos;
        BlockState rootState = level.getBlockState(rootPos);

        if (!isMatchingLower(rootState)
                || rootState.getValue(AGE) >= MAX_AGE) {
            return false;
        }

        int age = rootState.getValue(AGE);

        if (age == 0) {
            return true;
        }

        BlockState upperState = level.getBlockState(rootPos.above());

        return age == 1
                ? isAvailableUpperTrellis(upperState)
                        || isMatchingUpper(upperState)
                : isMatchingUpper(upperState);
    }

    @Override
    public boolean isBonemealSuccess(
            Level level,
            RandomSource random,
            BlockPos pos,
            BlockState state
    ) {
        return isValidBonemealTarget(level, pos, state);
    }

    @Override
    public void performBonemeal(
            ServerLevel level,
            RandomSource random,
            BlockPos pos,
            BlockState state
    ) {
        BlockPos rootPos = state.getValue(UPPER)
                ? pos.below()
                : pos;
        BlockState rootState = level.getBlockState(rootPos);

        if (isMatchingLower(rootState)) {
            advanceGrowth(level, rootPos, rootState);
        }
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(
                FACING,
                NORTH,
                EAST,
                SOUTH,
                WEST,
                ISOLATED,
                HAS_ABOVE,
                HAS_BELOW,
                UPPER,
                AGE
        );
    }
}
