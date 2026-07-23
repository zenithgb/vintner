package com.zenith.vintner.wine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;

public final class WinemakingEffects {
    private WinemakingEffects() {
    }

    public static void press(
            ServerLevel level,
            BlockPos pos,
            Item grapeItem
    ) {
        level.sendParticles(
                new ItemParticleOption(
                        ParticleTypes.ITEM,
                        grapeItem
                ),
                pos.getX() + 0.5,
                pos.getY() + 0.85,
                pos.getZ() + 0.5,
                10,
                0.25,
                0.12,
                0.25,
                0.08
        );
    }

    public static void harvest(
            ServerLevel level,
            BlockPos pos,
            Item grapeItem
    ) {
        level.sendParticles(
                new ItemParticleOption(
                        ParticleTypes.ITEM,
                        grapeItem
                ),
                pos.getX() + 0.5,
                pos.getY() + 0.55,
                pos.getZ() + 0.5,
                8,
                0.4,
                0.3,
                0.4,
                0.08
        );

        level.sendParticles(
                ParticleTypes.COMPOSTER,
                pos.getX() + 0.5,
                pos.getY() + 0.7,
                pos.getZ() + 0.5,
                4,
                0.4,
                0.25,
                0.4,
                0.03
        );
    }

    public static void fermentationActive(
            ServerLevel level,
            BlockPos pos
    ) {
        level.sendParticles(
                ParticleTypes.BUBBLE_POP,
                pos.getX() + 0.5,
                pos.getY() + 1.03,
                pos.getZ() + 0.5,
                2,
                0.18,
                0.02,
                0.18,
                0.02
        );
    }

    public static void fermentationComplete(
            ServerLevel level,
            BlockPos pos
    ) {
        level.sendParticles(
                ParticleTypes.BUBBLE_POP,
                pos.getX() + 0.5,
                pos.getY() + 1.05,
                pos.getZ() + 0.5,
                8,
                0.28,
                0.08,
                0.28,
                0.05
        );

        level.sendParticles(
                ParticleTypes.SPLASH,
                pos.getX() + 0.5,
                pos.getY() + 1.0,
                pos.getZ() + 0.5,
                4,
                0.2,
                0.04,
                0.2,
                0.03
        );

        level.playSound(
                null,
                pos,
                SoundEvents.BREWING_STAND_BREW,
                SoundSource.BLOCKS,
                0.7F,
                1.1F
        );
    }

    public static void agingActive(
            ServerLevel level,
            BlockPos pos
    ) {
        level.sendParticles(
                ParticleTypes.WAX_OFF,
                pos.getX() + 0.5,
                pos.getY() + 1.02,
                pos.getZ() + 0.5,
                1,
                0.15,
                0.02,
                0.15,
                0.01
        );
    }

    public static void agingComplete(
            ServerLevel level,
            BlockPos pos
    ) {
        level.sendParticles(
                ParticleTypes.WAX_ON,
                pos.getX() + 0.5,
                pos.getY() + 1.03,
                pos.getZ() + 0.5,
                8,
                0.28,
                0.08,
                0.28,
                0.04
        );

        level.playSound(
                null,
                pos,
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS,
                0.6F,
                0.8F
        );
    }
}
