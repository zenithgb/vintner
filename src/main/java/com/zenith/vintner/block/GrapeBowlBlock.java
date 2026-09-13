package com.zenith.vintner.block;

import com.mojang.serialization.MapCodec;
import com.zenith.vintner.item.GrapeBowlItem;
import com.zenith.vintner.item.GrapeItem;
import com.zenith.vintner.vineyard.GrapeCultivar;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/** A four-serving tabletop food; all persistent state uses vanilla block states. */
public final class GrapeBowlBlock extends Block {
    public static final int MAX_SERVINGS = 4;
    public static final MapCodec<GrapeBowlBlock> CODEC = simpleCodec(GrapeBowlBlock::new);
    public static final IntegerProperty SERVINGS = IntegerProperty.create("servings", 0, MAX_SERVINGS);
    public static final EnumProperty<GrapeCultivar> CULTIVAR = EnumProperty.create("cultivar", GrapeCultivar.class);
    private static final VoxelShape SHAPE = Block.box(2.7, 0, 2.7, 13.3, 3.5, 13.3);

    public GrapeBowlBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(SERVINGS, MAX_SERVINGS)
                .setValue(CULTIVAR, GrapeCultivar.EMBER_NOIR));
    }

    @Override
    public MapCodec<GrapeBowlBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        ItemStack stack = context.getItemInHand();
        return defaultBlockState()
                .setValue(CULTIVAR, GrapeBowlItem.cultivar(stack))
                .setValue(SERVINGS, GrapeBowlItem.servings(stack));
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult
    ) {
        // Full bowls behave like cake. An empty bowl only falls through to
        // retrieval when the player is actually using an empty hand.
        return state.getValue(SERVINGS) > 0 || stack.isEmpty()
                ? InteractionResult.TRY_WITH_EMPTY_HAND
                : InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult
    ) {
        int servings = state.getValue(SERVINGS);
        if (servings == 0) {
            if (!player.getMainHandItem().isEmpty()) {
                return InteractionResult.PASS;
            }
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.removeBlock(pos, false);
                ItemStack bowl = new ItemStack(Items.BOWL);
                if (!player.addItem(bowl)) {
                    popResource(serverLevel, pos, bowl);
                }
                serverLevel.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
            }
            return InteractionResult.SUCCESS;
        }

        GrapeCultivar cultivar = state.getValue(CULTIVAR);
        FoodProperties food = GrapeItem.servingFood(cultivar);
        if (!player.canEat(food.canAlwaysEat())) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            // Commit the shared block state first: subsequent interactions see
            // the reduced serving count, including interactions by other players.
            serverLevel.setBlock(pos, state.setValue(SERVINGS, servings - 1), Block.UPDATE_ALL);
            player.getFoodData().eat(food);
            serverLevel.playSound(null, pos, SoundEvents.GENERIC_EAT.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            serverLevel.gameEvent(player, GameEvent.EAT, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        int servings = state.getValue(SERVINGS);
        return List.of(servings == 0
                ? new ItemStack(Items.BOWL)
                : GrapeBowlItem.create(state.getValue(CULTIVAR), servings));
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return state.getValue(SERVINGS) == 0
                ? new ItemStack(Items.BOWL)
                : GrapeBowlItem.create(state.getValue(CULTIVAR), state.getValue(SERVINGS));
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CULTIVAR, SERVINGS);
    }
}
