package com.zenith.vintner.item;

import com.zenith.vintner.network.AlmanacReportPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/** A small paged report rendered by Minecraft's native book interface. */
public final class AlmanacReport {
    private final List<Component> pages = new ArrayList<>();

    public AlmanacReport page(
            Component heading,
            Component... entries
    ) {
        MutableComponent page = heading.copy().withStyle(
                ChatFormatting.GOLD,
                ChatFormatting.BOLD
        );
        for (Component entry : entries) {
            page.append(Component.literal("\n\n"));
            page.append(entry);
        }
        pages.add(page);
        return this;
    }

    public boolean isEmpty() {
        return pages.isEmpty();
    }

    public void open(ServerPlayer player) {
        if (pages.isEmpty()) {
            return;
        }
        ServerPlayNetworking.send(
                player,
                new AlmanacReportPayload(pages)
        );
    }
}
