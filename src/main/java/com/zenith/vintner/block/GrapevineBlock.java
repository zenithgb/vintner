package com.zenith.vintner.block;

import com.mojang.serialization.MapCodec;
import com.zenith.vintner.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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

public final class GrapevineBlock extends TrellisBlock implements BonemealableBlock {
    public static final MapCodec<GrapevineBlock> CODEC = simpleCodec(GrapevineBlock::new);

    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    private static final VoxelShape NORTH_SOUTH_SHAPE =
            Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);

    private static final VoxelShape EAST_WEST_SHAPE =
            Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

    public GrapevineBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, net.minecraft.core.Direction.NORTH)
                        .setValue(AGE, 0)
        );
    }

    @Override
    public MapCodec<GrapevineBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return state.getValue(FACING).getAxis()
                == net.minecraft.core.Direction.Axis.X
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
                && random.nextInt(5) == 0
                && level.getRawBrightness(pos.above(), 0) >= 9) {
            BlockState newState = state.setValue(AGE, age + 1);

            level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
            level.gameEvent(
                    GameEvent.BLOCK_CHANGE,
                    pos,
                    GameEvent.Context.of(newState)
            );
        }
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (state.getValue(AGE) < MAX_AGE) {
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }

        if (level instanceof ServerLevel serverLevel) {
            int grapeCount = 2 + serverLevel.getRandom().nextInt(2);

            Block.popResource(
                    serverLevel,
                    pos,
                    new ItemStack(ModItems.GRAPES, grapeCount)
            );

            serverLevel.playSound(
                    null,
                    pos,
                    SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                    SoundSource.BLOCKS,
                    1.0F,
                    0.8F + serverLevel.getRandom().nextFloat() * 0.4F
            );

            BlockState harvestedState = state.setValue(AGE, 1);
            serverLevel.setBlock(pos, harvestedState, Block.UPDATE_CLIENTS);
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
        return new ItemStack(ModItems.GRAPES);
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
        int newAge = Math.min(MAX_AGE, state.getValue(AGE) + 1);

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
        builder.add(FACING, AGE);
    }
}
