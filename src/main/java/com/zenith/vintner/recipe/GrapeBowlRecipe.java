package com.zenith.vintner.recipe;

import com.mojang.serialization.MapCodec;
import com.zenith.vintner.block.GrapeBowlBlock;
import com.zenith.vintner.item.GrapeBowlItem;
import com.zenith.vintner.registry.ModItems;
import com.zenith.vintner.vineyard.GrapeCultivar;
import com.zenith.vintner.vineyard.GrapeVariety;
import com.zenith.vintner.wine.WineMetadata;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** One discoverable recipe validates all eight grape components at craft time. */
public final class GrapeBowlRecipe extends NormalCraftingRecipe {
    public static final MapCodec<GrapeBowlRecipe> CODEC = MapCodec.unit(GrapeBowlRecipe::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, GrapeBowlRecipe> STREAM_CODEC =
            StreamCodec.unit(new GrapeBowlRecipe());
    public static final RecipeSerializer<GrapeBowlRecipe> SERIALIZER =
            new RecipeSerializer<>(CODEC, STREAM_CODEC);

    public GrapeBowlRecipe() {
        super(new Recipe.CommonInfo(true), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, "grape_bowl"));
    }

    @Override
    public RecipeSerializer<GrapeBowlRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(Ingredient.of(Items.BOWL));
        for (int grape = 0; grape < 8; grape++) {
            ingredients.add(Ingredient.of(ModItems.RED_GRAPES, ModItems.WHITE_GRAPES));
        }
        return PlacementInfo.create(ingredients);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return matchingCultivar(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        GrapeCultivar cultivar = matchingCultivar(input);
        return cultivar == null ? ItemStack.EMPTY : GrapeBowlItem.create(cultivar, GrapeBowlBlock.MAX_SERVINGS);
    }

    private static @Nullable GrapeCultivar matchingCultivar(CraftingInput input) {
        if (input.ingredientCount() != 9) {
            return null;
        }
        int bowls = 0;
        int grapes = 0;
        GrapeCultivar selected = null;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(Items.BOWL)) {
                if (++bowls > 1) {
                    return null;
                }
                continue;
            }
            GrapeVariety variety;
            if (stack.is(ModItems.RED_GRAPES)) {
                variety = GrapeVariety.RED;
            } else if (stack.is(ModItems.WHITE_GRAPES)) {
                variety = GrapeVariety.WHITE;
            } else {
                return null;
            }
            GrapeCultivar cultivar = WineMetadata.cultivar(stack, variety);
            if (cultivar.variety() != variety || selected != null && selected != cultivar) {
                return null;
            }
            selected = cultivar;
            grapes++;
        }
        return bowls == 1 && grapes == 8 ? selected : null;
    }

    @Override
    public List<RecipeDisplay> display() {
        List<RecipeDisplay> displays = new ArrayList<>();
        for (GrapeCultivar cultivar : GrapeCultivar.activeValues()) {
            ItemStack grapes = new ItemStack(cultivar.variety() == GrapeVariety.RED
                    ? ModItems.RED_GRAPES : ModItems.WHITE_GRAPES);
            WineMetadata.applyCultivar(grapes, cultivar);
            List<SlotDisplay> ingredients = new ArrayList<>();
            ingredients.add(new SlotDisplay.ItemSlotDisplay(Items.BOWL));
            SlotDisplay grape = new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(grapes));
            for (int count = 0; count < 8; count++) {
                ingredients.add(grape);
            }
            displays.add(new ShapelessCraftingRecipeDisplay(
                    ingredients,
                    new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(
                            GrapeBowlItem.create(cultivar, GrapeBowlBlock.MAX_SERVINGS))),
                    new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
            ));
        }
        return List.copyOf(displays);
    }
}
