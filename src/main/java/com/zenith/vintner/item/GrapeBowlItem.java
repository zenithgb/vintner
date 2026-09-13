package com.zenith.vintner.item;

import com.zenith.vintner.block.GrapeBowlBlock;
import com.zenith.vintner.registry.ModBlocks;
import com.zenith.vintner.vineyard.GrapeCultivar;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

/** The ordinary block-state component carries both identity and remaining food. */
public final class GrapeBowlItem extends BlockItem {
    public GrapeBowlItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static ItemStack create(GrapeCultivar cultivar, int servings) {
        ItemStack result = new ItemStack(ModBlocks.GRAPE_BOWL);
        int remaining = Math.clamp(servings, 0, GrapeBowlBlock.MAX_SERVINGS);
        result.set(
                DataComponents.BLOCK_STATE,
                BlockItemStateProperties.EMPTY
                        .with(GrapeBowlBlock.CULTIVAR, cultivar)
                        .with(GrapeBowlBlock.SERVINGS, remaining)
        );
        result.set(
                DataComponents.ITEM_MODEL,
                Identifier.fromNamespaceAndPath(
                        "vintner",
                        "grape_bowl_" + cultivar.visualName() + "_" + remaining
                )
        );
        return result;
    }

    public static GrapeCultivar cultivar(ItemStack stack) {
        GrapeCultivar cultivar = state(stack).get(GrapeBowlBlock.CULTIVAR);
        return cultivar == null ? GrapeCultivar.EMBER_NOIR : cultivar;
    }

    public static int servings(ItemStack stack) {
        Integer servings = state(stack).get(GrapeBowlBlock.SERVINGS);
        return servings == null ? GrapeBowlBlock.MAX_SERVINGS : servings;
    }

    private static BlockItemStateProperties state(ItemStack stack) {
        return stack.getOrDefault(
                DataComponents.BLOCK_STATE,
                BlockItemStateProperties.EMPTY
        );
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(
                "item.vintner.grape_bowl_named",
                cultivar(stack).displayName()
        );
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
        tooltip.accept(Component.translatable(
                "tooltip.vintner.grape_bowl.servings",
                servings(stack),
                GrapeBowlBlock.MAX_SERVINGS
        ).withStyle(ChatFormatting.GRAY));
    }
}
