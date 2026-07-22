package com.zenith.vintner.item;

import com.zenith.vintner.advancement.ModAdvancements;
import com.zenith.vintner.block.TrellisBlock;
import com.zenith.vintner.wine.WineMetadata;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class GrapeItem extends Item {
    private final Supplier<Block> grapevineSupplier;

    public GrapeItem(
            Supplier<Block> grapevineSupplier,
            Properties properties
    ) {
        super(properties);
        this.grapevineSupplier = grapevineSupplier;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(pos);

        if (!(clickedState.getBlock() instanceof TrellisBlock)
                || clickedState.getBlock()
                instanceof com.zenith.vintner.block.GrapevineBlock) {
            return super.useOn(context);
        }

        if (level instanceof ServerLevel serverLevel) {
            Player player = context.getPlayer();
            BlockState plantedState = grapevineSupplier.get()
                    .defaultBlockState()
                    .setValue(
                            TrellisBlock.FACING,
                            clickedState.getValue(TrellisBlock.FACING)
                    )
                    .setValue(
                            TrellisBlock.NORTH,
                            clickedState.getValue(TrellisBlock.NORTH)
                    )
                    .setValue(
                            TrellisBlock.EAST,
                            clickedState.getValue(TrellisBlock.EAST)
                    )
                    .setValue(
                            TrellisBlock.SOUTH,
                            clickedState.getValue(TrellisBlock.SOUTH)
                    )
                    .setValue(
                            TrellisBlock.WEST,
                            clickedState.getValue(TrellisBlock.WEST)
                    )
                    .setValue(
                            TrellisBlock.ISOLATED,
                            clickedState.getValue(TrellisBlock.ISOLATED)
                    );

            serverLevel.setBlock(
                    pos,
                    plantedState,
                    Block.UPDATE_ALL
            );

            serverLevel.gameEvent(
                    GameEvent.BLOCK_CHANGE,
                    pos,
                    GameEvent.Context.of(
                            context.getPlayer(),
                            plantedState
                    )
            );

            if (player instanceof ServerPlayer serverPlayer) {
                ModAdvancements.grantPlanting(
                        serverPlayer,
                        grapevineSupplier.get()
                );
            }

            if (player == null
                    || !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(
                stack,
                context,
                display,
                tooltip,
                flag
        );

        tooltip.accept(
                WineMetadata.qualityTooltip(stack)
                        .copy()
                        .withStyle(ChatFormatting.GRAY)
        );

        tooltip.accept(
                WineMetadata.vintageTooltip(stack)
                        .copy()
                        .withStyle(ChatFormatting.DARK_GRAY)
        );
    }
}
