package com.zenith.vintner.block;

import com.mojang.serialization.MapCodec;
import com.zenith.vintner.block.entity.WineGlassBlockEntity;
import com.zenith.vintner.item.GobletItem;
import com.zenith.vintner.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** A compact tabletop setting containing one to four exact metal goblets. */
public final class WineGlassBlock extends BaseEntityBlock {
    public static final MapCodec<WineGlassBlock> CODEC =
            simpleCodec(WineGlassBlock::new);
    public static final EnumProperty<Direction> FACING =
            BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = Block.box(
            2,
            0,
            2,
            14,
            11,
            14
    );

    public WineGlassBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any().setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    public MapCodec<WineGlassBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
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

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(
                FACING,
                context.getHorizontalDirection().getOpposite()
        );
    }

    @Override
    protected boolean canSurvive(
            BlockState state,
            LevelReader level,
            BlockPos pos
    ) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess tickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        if (direction == Direction.DOWN
                && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(
                state,
                level,
                tickAccess,
                pos,
                direction,
                neighborPos,
                neighborState,
                random
        );
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WineGlassBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return null;
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack heldStack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (!GobletItem.isGoblet(heldStack)) {
            return InteractionResult.PASS;
        }

        if (!(level.getBlockEntity(pos)
                instanceof WineGlassBlockEntity glassEntity)) {
            return InteractionResult.PASS;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        if (glassEntity.isFull()) {
            player.sendOverlayMessage(
                    Component.translatable(
                            "message.vintner.goblets.full"
                    )
            );
            return InteractionResult.SUCCESS;
        }

        if (!glassEntity.addGlass(heldStack)) {
            return InteractionResult.PASS;
        }

        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        serverLevel.playSound(
                null,
                pos,
                SoundEvents.GLASS_PLACE,
                SoundSource.BLOCKS,
                0.65F,
                1.25F
        );
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
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos)
                instanceof WineGlassBlockEntity glassEntity)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            serverLevel.setBlock(
                    pos,
                    state.setValue(
                            FACING,
                            state.getValue(FACING).getClockWise()
                    ),
                    Block.UPDATE_ALL
            );
            serverLevel.playSound(
                    null,
                    pos,
                    SoundEvents.ITEM_FRAME_ROTATE_ITEM,
                    SoundSource.BLOCKS,
                    0.55F,
                    1.2F
            );
            return InteractionResult.SUCCESS;
        }

        int index = nearestGlass(
                hitResult.getLocation(),
                pos,
                glassEntity.size(),
                state.getValue(FACING)
        );
        ItemStack glass = glassEntity.takeGlass(index);

        if (glass.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!player.addItem(glass)) {
            popResource(serverLevel, pos, glass);
        }

        if (glassEntity.size() == 0) {
            serverLevel.removeBlock(pos, false);
        }

        serverLevel.playSound(
                null,
                pos,
                SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                SoundSource.BLOCKS,
                0.6F,
                1.25F
        );
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(
            Level level,
            BlockPos pos,
            BlockState state,
            Player player
    ) {
        if (!level.isClientSide()
                && player.getAbilities().instabuild
                && level.getBlockEntity(pos)
                instanceof WineGlassBlockEntity glassEntity) {
            for (ItemStack glass : glassEntity.takeAll()) {
                popResource(level, pos, glass);
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected List<ItemStack> getDrops(
            BlockState state,
            LootParams.Builder params
    ) {
        List<ItemStack> drops = new ArrayList<>(super.getDrops(state, params));
        BlockEntity blockEntity = params.getOptionalParameter(
                LootContextParams.BLOCK_ENTITY
        );

        if (blockEntity instanceof WineGlassBlockEntity glassEntity) {
            drops.addAll(glassEntity.getGlasses());
        }

        return drops;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING);
    }

    public static Vec3 layoutPosition(
            int count,
            int index,
            Direction facing
    ) {
        double[][] layout = switch (count) {
            case 1 -> new double[][]{{0.5, 0.5}};
            case 2 -> new double[][]{{0.31, 0.5}, {0.69, 0.5}};
            case 3 -> new double[][]{
                    {0.30, 0.32},
                    {0.70, 0.32},
                    {0.5, 0.68}
            };
            default -> new double[][]{
                    {0.30, 0.30},
                    {0.70, 0.30},
                    {0.30, 0.70},
                    {0.70, 0.70}
            };
        };

        int safeIndex = Math.max(0, Math.min(index, layout.length - 1));
        double x = layout[safeIndex][0];
        double z = layout[safeIndex][1];

        return switch (facing) {
            case EAST -> new Vec3(1.0 - z, 0.0, x);
            case SOUTH -> new Vec3(1.0 - x, 0.0, 1.0 - z);
            case WEST -> new Vec3(z, 0.0, 1.0 - x);
            default -> new Vec3(x, 0.0, z);
        };
    }

    private static int nearestGlass(
            Vec3 hit,
            BlockPos pos,
            int count,
            Direction facing
    ) {
        if (count <= 1) {
            return 0;
        }

        double localX = hit.x - pos.getX();
        double localZ = hit.z - pos.getZ();
        int nearest = 0;
        double nearestDistance = Double.MAX_VALUE;

        for (int index = 0; index < count; index++) {
            Vec3 placement = layoutPosition(count, index, facing);
            double dx = localX - placement.x;
            double dz = localZ - placement.z;
            double distance = dx * dx + dz * dz;

            if (distance < nearestDistance) {
                nearest = index;
                nearestDistance = distance;
            }
        }

        return nearest;
    }

}
