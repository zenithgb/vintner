package com.zenith.vintner.block;

import com.mojang.serialization.MapCodec;
import com.zenith.vintner.vineyard.NurseryPlant;
import com.zenith.vintner.vineyard.GraftedCuttingData;
import com.zenith.vintner.vineyard.GrapeCultivar;
import com.zenith.vintner.vineyard.GrapeVariety;
import com.zenith.vintner.vineyard.VineManagementSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A compact propagation bed that turns one cutting into several cuttings. */
public final class NurseryBedBlock extends Block {
    public static final MapCodec<NurseryBedBlock> CODEC =
            simpleCodec(NurseryBedBlock::new);
    public static final BooleanProperty OCCUPIED =
            BooleanProperty.create("occupied");
    public static final IntegerProperty AGE =
            IntegerProperty.create("age", 0, 3);
    public static final EnumProperty<NurseryPlant> PLANT =
            EnumProperty.create("plant", NurseryPlant.class);
    public static final int MAX_AGE = 3;
    public static final int HARVEST_COUNT = 3;

    private static final VoxelShape SHAPE =
            Block.box(0, 0, 0, 16, 5, 16);

    public NurseryBedBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any()
                        .setValue(OCCUPIED, false)
                        .setValue(AGE, 0)
                        .setValue(PLANT, NurseryPlant.RED_GRAPE)
        );
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(OCCUPIED)
                && state.getValue(AGE) < MAX_AGE;
    }

    @Override
    protected void randomTick(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random
    ) {
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9
                && random.nextInt(3) == 0) {
            level.setBlock(
                    pos,
                    state.setValue(AGE, state.getValue(AGE) + 1),
                    Block.UPDATE_ALL
            );
        }
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
        NurseryPlant plant = NurseryPlant.fromItem(stack);

        if (plant == null || state.getValue(OCCUPIED)) {
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

        if (level instanceof ServerLevel serverLevel) {
            level.setBlock(
                    pos,
                    plantedState(state, plant),
                    Block.UPDATE_ALL
            );
            if (!plant.isRootstock()) {
                GrapeVariety variety = plant == NurseryPlant.RED_GRAPE
                        ? GrapeVariety.RED
                        : GrapeVariety.WHITE;
                VineManagementSavedData.get(serverLevel).setCultivar(
                        pos,
                        GraftedCuttingData.cultivar(stack, variety)
                );
            }
            level.playSound(
                    null,
                    pos,
                    SoundEvents.ROOTED_DIRT_PLACE,
                    SoundSource.BLOCKS,
                    0.8F,
                    1.15F
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
        if (!readyToHarvest(state)) {
            return super.useWithoutItem(
                    state,
                    level,
                    pos,
                    player,
                    hitResult
            );
        }

        if (level instanceof ServerLevel serverLevel) {
            NurseryPlant plant = state.getValue(PLANT);
            ItemStack harvest = new ItemStack(
                    plant.item(),
                    HARVEST_COUNT
            );
            if (!plant.isRootstock()) {
                GrapeVariety variety = plant == NurseryPlant.RED_GRAPE
                        ? GrapeVariety.RED
                        : GrapeVariety.WHITE;
                GrapeCultivar cultivar = VineManagementSavedData
                        .get(serverLevel)
                        .cultivar(pos, variety);
                GraftedCuttingData.applyCultivar(harvest, cultivar);
            }
            popResource(
                    level,
                    pos.above(),
                    harvest
            );
            VineManagementSavedData.get(serverLevel).remove(pos);
            level.setBlock(pos, emptiedState(state), Block.UPDATE_ALL);
            level.playSound(
                    null,
                    pos,
                    SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                    SoundSource.BLOCKS,
                    0.9F,
                    1.2F
            );
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    public void destroy(
            LevelAccessor level,
            BlockPos pos,
            BlockState state
    ) {
        if (level instanceof ServerLevel serverLevel) {
            VineManagementSavedData.get(serverLevel).remove(pos);
        }
        super.destroy(level, pos, state);
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(OCCUPIED, AGE, PLANT);
    }

    public static BlockState plantedState(
            BlockState state,
            NurseryPlant plant
    ) {
        return state
                .setValue(OCCUPIED, true)
                .setValue(AGE, 0)
                .setValue(PLANT, plant);
    }

    public static BlockState emptiedState(BlockState state) {
        return state
                .setValue(OCCUPIED, false)
                .setValue(AGE, 0);
    }

    public static boolean readyToHarvest(BlockState state) {
        return state.getValue(OCCUPIED)
                && state.getValue(AGE) == MAX_AGE;
    }

}
