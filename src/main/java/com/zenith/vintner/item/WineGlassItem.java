package com.zenith.vintner.item;

import com.zenith.vintner.block.WineGlassBlock;
import com.zenith.vintner.block.entity.WineGlassBlockEntity;
import com.zenith.vintner.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Consumer;

/**
 * A wine glass remains an ordinary usable item, but using it on a surface
 * sets it down as tabletop decoration. Filled glasses inherit this behavior;
 * using one in the air still drinks it through its consumable component.
 */
public class WineGlassItem extends Item {
    public WineGlassItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(
                Component.translatable(
                        "tooltip.vintner.wine_glass.place"
                ).withStyle(ChatFormatting.DARK_GRAY)
        );
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPlaceContext placement = new BlockPlaceContext(context);

        if (!placement.canPlace()) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        BlockPos pos = placement.getClickedPos();
        BlockState state = ModBlocks.WINE_GLASSES
                .getStateForPlacement(placement);

        if (state == null || !state.canSurvive(level, pos)) {
            return InteractionResult.FAIL;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        serverLevel.setBlock(pos, state, Block.UPDATE_ALL);

        if (!(serverLevel.getBlockEntity(pos)
                instanceof WineGlassBlockEntity glassEntity)) {
            serverLevel.removeBlock(pos, false);
            return InteractionResult.FAIL;
        }

        ItemStack glass = context.getItemInHand().copyWithCount(1);
        if (!glassEntity.addGlass(glass)) {
            serverLevel.removeBlock(pos, false);
            return InteractionResult.FAIL;
        }

        serverLevel.gameEvent(
                GameEvent.BLOCK_PLACE,
                pos,
                GameEvent.Context.of(context.getPlayer(), state)
        );
        serverLevel.playSound(
                null,
                pos,
                SoundEvents.GLASS_PLACE,
                SoundSource.BLOCKS,
                0.65F,
                1.25F
        );

        if (context.getPlayer() == null
                || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.SUCCESS;
    }
}
