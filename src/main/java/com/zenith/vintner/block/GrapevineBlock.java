package com.zenith.vintner.block;

import com.zenith.vintner.item.CompostItem;

import com.zenith.vintner.registry.ModBlocks;
import com.zenith.vintner.registry.ModItems;
import net.minecraft.world.InteractionHand;
import com.zenith.vintner.vineyard.GrapeVariety;
import com.zenith.vintner.wine.GrapeQualityEvaluator;
import com.zenith.vintner.wine.WineMetadata;
import com.zenith.vintner.wine.WineQuality;
import com.zenith.vintner.wine.VineyardConditionReport;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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

    private static final VoxelShape NORTH_SOUTH_SHAPE =
            Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);

    private static final VoxelShape EAST_WEST_SHAPE =
            Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

    private final GrapeVariety variety;

    protected GrapevineBlock(
            GrapeVariety variety,
            BlockBehaviour.Properties properties
    ) {
        super(properties);
        this.variety = variety;

        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(LEFT, false)
                        .setValue(RIGHT, false)
                        .setValue(ISOLATED, false)
                        .setValue(AGE, 0)
        );
    }

    protected abstract Item getGrapeItem();

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
        return state.getValue(FACING).getAxis() == Direction.Axis.X
                ? EAST_WEST_SHAPE
                : NORTH_SOUTH_SHAPE;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    protected void randomTick(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random
    ) {
        int age = state.getValue(AGE);

        if (age < MAX_AGE
                && random.nextInt(
                        variety.growthChanceDenominator()
                ) == 0
                && level.getRawBrightness(pos.above(), 0) >= 9) {
            BlockState grownState = state.setValue(AGE, age + 1);

            level.setBlock(pos, grownState, Block.UPDATE_CLIENTS);
            level.gameEvent(
                    GameEvent.BLOCK_CHANGE,
                    pos,
                    GameEvent.Context.of(grownState)
            );
        }
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
        if (!player.isShiftKeyDown()
                || !stack.is(ModItems.COMPOST)) {
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

        BlockPos soilPos = pos.below();
        BlockState soilState = level.getBlockState(soilPos);

        if (!CompostItem.isSuitableGround(soilState)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            level.setBlockAndUpdate(
                    soilPos,
                    ModBlocks.VINEYARD_SOIL.defaultBlockState()
            );

            level.playSound(
                    null,
                    soilPos,
                    SoundEvents.HOE_TILL,
                    SoundSource.BLOCKS,
                    1.0F,
                    0.9F
            );

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
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
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                VineyardConditionReport report =
                        GrapeQualityEvaluator.inspect(
                                level,
                                pos
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
                                qualityText(
                                        report.predictedQuality()
                                )
                        )
                );
            }

            return InteractionResult.SUCCESS;
        }

        if (state.getValue(AGE) < MAX_AGE) {
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

            WineQuality quality =
                    GrapeQualityEvaluator.evaluate(
                            serverLevel,
                            pos
                    );

            WineMetadata.apply(
                    grapes,
                    vintage,
                    quality
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

            BlockState harvestedState = state.setValue(AGE, 1);

            serverLevel.setBlock(
                    pos,
                    harvestedState,
                    Block.UPDATE_CLIENTS
            );

            serverLevel.gameEvent(
                    GameEvent.BLOCK_CHANGE,
                    pos,
                    GameEvent.Context.of(player, harvestedState)
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
        return new ItemStack(getGrapeItem());
    }

    @Override
    public boolean isValidBonemealTarget(
            LevelReader level,
            BlockPos pos,
            BlockState state
    ) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean isBonemealSuccess(
            Level level,
            RandomSource random,
            BlockPos pos,
            BlockState state
    ) {
        return true;
    }

    @Override
    public void performBonemeal(
            ServerLevel level,
            RandomSource random,
            BlockPos pos,
            BlockState state
    ) {
        int newAge = Math.min(
                MAX_AGE,
                state.getValue(AGE) + 1
        );

        level.setBlock(
                pos,
                state.setValue(AGE, newAge),
                Block.UPDATE_CLIENTS
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING, LEFT, RIGHT, ISOLATED, AGE);
    }
}
