package com.zenith.vintner.wine;

import com.zenith.vintner.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public record WineTastingProfile(
        Component fruit,
        Component character,
        Component finish
) {
    private static final String[] RED_FRUIT = {
            "red_fruit",
            "dark_fruit",
            "blackberry"
    };
    private static final String[] WHITE_FRUIT = {
            "orchard_fruit",
            "citrus",
            "stone_fruit"
    };
    private static final String[] RED_CHARACTER = {
            "firm_tannin",
            "earth_and_cedar",
            "warm_spice"
    };
    private static final String[] WHITE_CHARACTER = {
            "crisp_acidity",
            "floral_mineral",
            "fresh_herbs"
    };

    public static WineTastingProfile from(ItemStack stack) {
        boolean red = stack.is(ModItems.RED_WINE)
                || stack.is(ModItems.AGED_RED_WINE);
        boolean aged = stack.is(ModItems.AGED_RED_WINE)
                || stack.is(ModItems.AGED_WHITE_WINE);
        int seed = WineMetadata.tastingProfileSeed(stack);

        String[] fruitOptions = red ? RED_FRUIT : WHITE_FRUIT;
        String[] characterOptions = red
                ? RED_CHARACTER
                : WHITE_CHARACTER;

        String fruit = select(fruitOptions, seed);
        String character = aged
                ? (red ? "soft_oak" : "rounded_mineral")
                : select(characterOptions, seed >>> 4);
        String finish = finishKey(
                WineMetadata.quality(stack),
                WineMetadata.ageStage(stack),
                seed >>> 8
        );

        return new WineTastingProfile(
                note(fruit),
                note(character),
                note(finish)
        );
    }

    public Component description() {
        return Component.translatable(
                "tasting.vintner.profile",
                fruit,
                character,
                finish
        );
    }

    private static String finishKey(
            WineQuality quality,
            WineAgeStage stage,
            int seed
    ) {
        if (stage == WineAgeStage.SPOILED) {
            return "spoiled_finish";
        }
        if (stage == WineAgeStage.DECLINING) {
            return "fading_finish";
        }
        if (stage == WineAgeStage.PEAK
                || quality == WineQuality.EXCEPTIONAL) {
            return "long_finish";
        }
        if (quality == WineQuality.FINE) {
            return "balanced_finish";
        }
        return select(
                new String[]{"clean_finish", "short_finish"},
                seed
        );
    }

    private static String select(String[] values, int seed) {
        return values[Math.floorMod(seed, values.length)];
    }

    private static Component note(String key) {
        return Component.translatable(
                "tasting_note.vintner." + key
        );
    }
}
