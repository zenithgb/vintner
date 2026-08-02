package com.zenith.vintner.registry;

import com.zenith.vintner.Vintner;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

/** World-level settings for Vintner's native seasonal fallback. */
public final class ModGameRules {
    public static final int DEFAULT_SEASON_LENGTH_DAYS = 8;

    public static final GameRule<Integer> SEASON_LENGTH_DAYS =
            GameRuleBuilder.forInteger(DEFAULT_SEASON_LENGTH_DAYS)
                    .category(GameRuleCategory.UPDATES)
                    .range(1, 96)
                    .buildAndRegister(Identifier.fromNamespaceAndPath(
                            Vintner.MOD_ID,
                            "season_length_days"
                    ));

    private ModGameRules() {
    }

    public static void initialize() {
        // Loading this class registers the namespaced gamerule.
    }
}
