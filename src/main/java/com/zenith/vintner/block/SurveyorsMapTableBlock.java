package com.zenith.vintner.block;

import com.mojang.serialization.MapCodec;
import com.zenith.vintner.block.entity.SurveyorsMapTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** A nearby estate-desk module that assembles up to nine explored maps. */
public final class SurveyorsMapTableBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING =
            BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty HAS_MAPS =
            BooleanProperty.create("has_maps");
    public static final EnumProperty<DeskModuleConnection> CONNECTION =
            EnumProperty.create(
                    "connection",
                    DeskModuleConnection.class
            );
    public static final MapCodec<SurveyorsMapTableBlock> CODEC =
            simpleCodec(SurveyorsMapTableBlock::new);
    private static final VoxelShape SHAPE = Block.box(
            0.5, 0, 0.5, 15.5, 14.5, 15.5
    );

    public SurveyorsMapTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(HAS_MAPS, false)
                        .setValue(
                                CONNECTION,
                                DeskModuleConnection.NONE
                        )
        );
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
        if (!heldStack.is(Items.FILLED_MAP)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable(
                    "message.vintner.desk_module.use_desk"
            ));
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
        if (!(level.getBlockEntity(pos)
                instanceof SurveyorsMapTableBlockEntity table)) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown()) {
            ItemStack recovered = table.takeLastMap();
            if (recovered.isEmpty()) {
                return InteractionResult.PASS;
            }
            if (level instanceof ServerLevel serverLevel) {
                Block.popResource(serverLevel, pos, recovered);
                serverLevel.playSound(
                        null,
                        pos,
                        SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                        SoundSource.BLOCKS,
                        0.8F,
                        1.0F
                );
            }
            return InteractionResult.SUCCESS;
        }
        player.sendSystemMessage(
                Component.translatable(
                        "message.vintner.surveyors_map_table.status_desk",
                        table.getMapCount(),
                        SurveyorsMapTableBlockEntity.CAPACITY
                )
        );
        return InteractionResult.SUCCESS;
    }

    @Override
    public MapCodec<SurveyorsMapTableBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SurveyorsMapTableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        return updateAttachment(
                defaultBlockState().setValue(FACING, facing),
                context.getLevel(),
                context.getClickedPos()
        );
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
        if (directionToNeighbour.getAxis().isHorizontal()) {
            return updateAttachment(state, level, pos);
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

    private static BlockState updateAttachment(
            BlockState state,
            LevelReader level,
            BlockPos pos
    ) {
        DeskModuleConnection.Attachment attachment =
                DeskModuleConnection.find(
                        level,
                        pos,
                        state.getValue(FACING)
                );
        return state
                .setValue(FACING, attachment.facing())
                .setValue(CONNECTION, attachment.connection());
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
    protected List<ItemStack> getDrops(
            BlockState state,
            LootParams.Builder params
    ) {
        List<ItemStack> drops =
                new ArrayList<>(super.getDrops(state, params));
        BlockEntity blockEntity = params.getOptionalParameter(
                LootContextParams.BLOCK_ENTITY
        );
        if (blockEntity instanceof SurveyorsMapTableBlockEntity table) {
            drops.addAll(table.getMapCopies());
        }
        return drops;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING, HAS_MAPS, CONNECTION);
    }
}
