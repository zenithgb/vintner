package com.zenith.vintner.vineyard;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class TerroirMessages {
    private TerroirMessages() {
    }

    public static void sendSoilReport(
            Player player,
            TerroirReport report
    ) {
        player.sendSystemMessage(
                Component.translatable(
                        "message.vintner.soil_probe.title",
                        report.soil().type().displayName()
                ).withStyle(ChatFormatting.GOLD)
        );
        player.sendSystemMessage(
                Component.translatable(
                        "message.vintner.soil_probe.properties",
                        report.soil().drainageRating().displayName(),
                        report.soil().fertilityRating().displayName(),
                        report.soil().waterRetentionRating().displayName(),
                        report.soil().rootDepthRating().displayName()
                ).withStyle(ChatFormatting.GRAY)
        );
        player.sendSystemMessage(
                Component.translatable(
                        "message.vintner.terroir.potential",
                        report.siteScore(),
                        report.siteRating().displayName()
                ).withStyle(ChatFormatting.DARK_GREEN)
        );
    }

    public static void sendFullReport(
            Player player,
            TerroirReport report
    ) {
        player.sendSystemMessage(
                Component.translatable(
                        "message.vintner.terroir.title"
                ).withStyle(ChatFormatting.GOLD)
        );
        player.sendSystemMessage(
                Component.translatable(
                        "message.vintner.terroir.climate",
                        report.climate().band().displayName(),
                        report.climate().rainfallRating().displayName(),
                        report.climate().humidityRating().displayName(),
                        report.climate().frostRiskRating().displayName(),
                        report.climate().heatStressRating().displayName(),
                        report.climate().growingSeasonDays()
                ).withStyle(ChatFormatting.GRAY)
        );
        player.sendSystemMessage(
                Component.translatable(
                        "message.vintner.terroir.soil",
                        report.soil().type().displayName(),
                        report.soil().drainageRating().displayName(),
                        report.soil().fertilityRating().displayName(),
                        report.soil().rootDepthRating().displayName(),
                        report.soil().mineralRating().displayName()
                ).withStyle(ChatFormatting.GRAY)
        );
        player.sendSystemMessage(
                Component.translatable(
                        "message.vintner.terroir.terrain",
                        report.terrain().elevation(),
                        report.terrain().slope().displayName(),
                        aspectName(report.terrain()),
                        report.terrain().sunRating().displayName(),
                        report.terrain().waterDistance(),
                        report.terrain().windRating().displayName(),
                        yesNo(report.terrain().terraced()),
                        yesNo(report.terrain().frostPocket())
                ).withStyle(ChatFormatting.DARK_GRAY)
        );
        player.sendSystemMessage(
                Component.translatable(
                        "message.vintner.terroir.potential",
                        report.siteScore(),
                        report.siteRating().displayName()
                ).withStyle(ChatFormatting.DARK_GREEN)
        );
    }

    private static Component directionName(
            net.minecraft.core.Direction direction
    ) {
        return Component.translatable(
                "direction.vintner." + direction.getSerializedName()
        );
    }

    private static Component aspectName(TerrainProfile terrain) {
        if (terrain.slope() == SlopeClass.FLAT) {
            return Component.translatable(
                    "terrain_aspect.vintner.level"
            );
        }
        return directionName(terrain.aspect());
    }

    private static Component yesNo(boolean value) {
        return Component.translatable(
                value ? "gui.yes" : "gui.no"
        );
    }
}
