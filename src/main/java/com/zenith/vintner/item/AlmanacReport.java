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
    private static final int ESTIMATED_CHARACTERS_PER_LINE = 18;
    private static final int MAX_ESTIMATED_LINES_PER_PAGE = 13;
    private static final int ENTRY_GAP_LINES = 2;
    private final List<Component> pages = new ArrayList<>();

    public AlmanacReport page(
            Component heading,
            Component... entries
    ) {
        MutableComponent page = heading(heading);
        int estimatedLines = estimatedLines(heading);
        boolean hasEntry = false;
        for (Component entry : entries) {
            if (entry.getString().isBlank()) {
                continue;
            }
            int entryLines = ENTRY_GAP_LINES + estimatedLines(entry);
            if (hasEntry
                    && estimatedLines + entryLines
                    > MAX_ESTIMATED_LINES_PER_PAGE) {
                pages.add(page);
                page = heading(heading);
                estimatedLines = estimatedLines(heading);
                hasEntry = false;
            }
            page.append(Component.literal("\n\n"));
            page.append(entry);
            estimatedLines += entryLines;
            hasEntry = true;
        }
        pages.add(page);
        return this;
    }

    public int pageCount() {
        return pages.size();
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

    private static MutableComponent heading(Component heading) {
        return heading.copy().withStyle(
                ChatFormatting.GOLD,
                ChatFormatting.BOLD
        );
    }

    private static int estimatedLines(Component component) {
        int lines = 0;
        for (String line : component.getString().split("\n", -1)) {
            lines += Math.max(
                    1,
                    (line.length() + ESTIMATED_CHARACTERS_PER_LINE - 1)
                            / ESTIMATED_CHARACTERS_PER_LINE
            );
        }
        return lines;
    }
}
