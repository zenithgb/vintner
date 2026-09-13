package com.zenith.vintner.registry;

import com.zenith.vintner.Vintner;
import com.zenith.vintner.recipe.GrapeBowlRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class ModRecipes {
    public static final RecipeSerializer<GrapeBowlRecipe> GRAPE_BOWL =
            Registry.register(
                    BuiltInRegistries.RECIPE_SERIALIZER,
                    Identifier.fromNamespaceAndPath(
                            Vintner.MOD_ID,
                            "grape_bowl"
                    ),
                    GrapeBowlRecipe.SERIALIZER
            );

    private ModRecipes() {
    }

    public static void initialize() {
    }
}
