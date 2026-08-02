package com.zenith.vintner.estate;

import com.zenith.vintner.network.EstateDeskPayload;
import com.zenith.vintner.vineyard.TerroirEvaluator;
import com.zenith.vintner.wine.WineMarketRegion;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/** Builds a fresh, read-only estate snapshot when a desk is opened. */
public final class EstateDeskReport {
    private static final int MAX_PLOT_REPORTS = 8;
    private static final int MAX_LEDGER_ENTRIES = 10;

    private EstateDeskReport() {
    }

    public static void open(
            ServerLevel level,
            BlockPos deskPos,
            ServerPlayer player
    ) {
        var estate = EstateSavedData.get(level).find(player.getUUID());
        if (estate.isEmpty()) {
            ServerPlayNetworking.send(
                    player,
                    unregisteredPayload()
            );
            return;
        }

        EstateProfile profile = estate.get();
        EstateLedgerSavedData ledger = EstateLedgerSavedData.get(level);
        List<EstateLedgerEvent> entries = ledger.entries(player.getUUID());
        EstateInfrastructureReport infrastructure =
                EstateInfrastructureReport.survey(level, deskPos);
        EstateReputationProfile reputation =
                EstateReputationSavedData.get(level)
                        .syncFromLedger(player.getUUID(), entries);
        reputation = EstateReputationSavedData.get(level)
                .recordInfrastructure(
                        player.getUUID(),
                        infrastructure
                );
        List<VineyardPlot> plots = VineyardPlotSavedData.get(level)
                .plots(player.getUUID());
        WineMarketRegion market = WineMarketRegion.from(
                level,
                deskPos,
                TerroirEvaluator.inspect(level, deskPos)
        );

        List<EstateDeskPayload.Section> sections = List.of(
                overview(profile, reputation, plots, infrastructure, market),
                vineyards(level, plots),
                cellar(infrastructure),
                markets(market),
                contracts(),
                ledger(entries)
        );

        ServerPlayNetworking.send(
                player,
                new EstateDeskPayload(
                        Component.literal(profile.estateName()),
                        Component.translatable(
                                "screen.vintner.estate_desk.subtitle",
                                profile.foundingYear(),
                                profile.homeRegionDisplayName()
                        ),
                        sections
                )
        );
    }

    private static EstateDeskPayload unregisteredPayload() {
        Component unavailable = Component.translatable(
                "screen.vintner.estate_desk.unavailable"
        ).withStyle(ChatFormatting.DARK_GRAY);
        List<EstateDeskPayload.Section> sections = new ArrayList<>();
        for (String key : List.of(
                "overview",
                "vineyards",
                "cellar",
                "markets",
                "contracts",
                "ledger"
        )) {
            sections.add(new EstateDeskPayload.Section(
                    Component.translatable(
                            "screen.vintner.estate_desk.tab." + key
                    ),
                    List.of(unavailable)
            ));
        }
        return new EstateDeskPayload(
                Component.translatable(
                        "screen.vintner.estate_desk.unregistered_title"
                ),
                Component.translatable(
                        "screen.vintner.estate_desk.unregistered_subtitle"
                ),
                sections
        );
    }

    private static EstateDeskPayload.Section overview(
            EstateProfile profile,
            EstateReputationProfile reputation,
            List<VineyardPlot> plots,
            EstateInfrastructureReport infrastructure,
            WineMarketRegion market
    ) {
        int area = plots.stream().mapToInt(VineyardPlot::area).sum();
        return section("overview",
                line("overview.reputation",
                        Component.translatable(
                                reputation.tier().translationKey()
                        ),
                        reputation.score()),
                line("overview.vineyards", plots.size(), area),
                line("overview.harvest",
                        reputation.harvestedGrapes(),
                        reputation.bestQuality()),
                line("overview.cellar",
                        infrastructure.agingBarrels(),
                        infrastructure.storageFixtures()),
                line("overview.market",
                        market.displayName(),
                        market.buyerType().displayName()),
                line("overview.label", profile.bottleLabel())
        );
    }

    private static EstateDeskPayload.Section vineyards(
            ServerLevel level,
            List<VineyardPlot> plots
    ) {
        List<Component> lines = new ArrayList<>();
        if (plots.isEmpty()) {
            lines.add(line("vineyards.empty"));
        } else {
            for (VineyardPlot plot : plots.stream()
                    .limit(MAX_PLOT_REPORTS)
                    .toList()) {
                VineyardPlotReport report = VineyardPlotReport.analyze(
                        level,
                        plot
                );
                lines.add(line("vineyards.plot",
                        plot.name(),
                        plot.width(),
                        plot.depth(),
                        report.vineCount(),
                        report.varietySummary()));
                lines.add(line("vineyards.condition",
                        report.healthPercent(),
                        report.projectedYield(),
                        report.projectedQuality(),
                        report.irrigationPercent())
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
            if (plots.size() > MAX_PLOT_REPORTS) {
                lines.add(line(
                        "vineyards.more",
                        plots.size() - MAX_PLOT_REPORTS
                ));
            }
        }
        return new EstateDeskPayload.Section(
                tab("vineyards"),
                lines
        );
    }

    private static EstateDeskPayload.Section cellar(
            EstateInfrastructureReport report
    ) {
        return section("cellar",
                line("cellar.barrels",
                        report.agingBarrels(),
                        report.mountedBarrels()),
                line("cellar.storage", report.storageFixtures()),
                line("cellar.stations", report.idealCellarStations()),
                line("cellar.collections",
                        report.tastingCabinets(),
                        report.archives()),
                line("cellar.facilities",
                        mark(report.hasBarrelWorkshop()),
                        mark(report.hasControlledCellar()),
                        mark(report.hasWarehouse()),
                        mark(report.hasTastingRoom()))
        );
    }

    private static EstateDeskPayload.Section markets(
            WineMarketRegion market
    ) {
        return section("markets",
                line("markets.local", market.displayName()),
                line("markets.buyer", market.buyerType().displayName()),
                line("markets.guidance")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );
    }

    private static EstateDeskPayload.Section contracts() {
        return section("contracts",
                line("contracts.empty"),
                line("contracts.future")
                        .withStyle(ChatFormatting.DARK_GRAY)
        );
    }

    private static EstateDeskPayload.Section ledger(
            List<EstateLedgerEvent> entries
    ) {
        List<Component> lines = new ArrayList<>();
        if (entries.isEmpty()) {
            lines.add(line("ledger.empty"));
        } else {
            for (EstateLedgerEvent event : entries.stream()
                    .limit(MAX_LEDGER_ENTRIES)
                    .toList()) {
                Component detail = event.detail().isBlank()
                        ? Component.translatable(
                                event.eventType().translationKey()
                        )
                        : Component.literal(event.detail());
                lines.add(line("ledger.entry",
                        event.day(),
                        Component.translatable(
                                event.eventType().translationKey()
                        ),
                        detail,
                        event.amount(),
                        event.quality()));
            }
        }
        return new EstateDeskPayload.Section(tab("ledger"), lines);
    }

    private static EstateDeskPayload.Section section(
            String id,
            Component... lines
    ) {
        return new EstateDeskPayload.Section(
                tab(id),
                List.of(lines)
        );
    }

    private static Component tab(String id) {
        return Component.translatable(
                "screen.vintner.estate_desk.tab." + id
        );
    }

    private static MutableComponent line(String id, Object... values) {
        return Component.translatable(
                "screen.vintner.estate_desk." + id,
                values
        );
    }

    private static Component mark(boolean present) {
        return Component.translatable(
                present
                        ? "screen.vintner.estate_desk.present"
                        : "screen.vintner.estate_desk.absent"
        ).withStyle(
                present ? ChatFormatting.DARK_GREEN : ChatFormatting.RED
        );
    }
}
