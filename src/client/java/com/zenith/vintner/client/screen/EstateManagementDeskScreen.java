package com.zenith.vintner.client.screen;

import com.zenith.vintner.network.EstateDeskPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/** A vanilla-scale estate dashboard styled as a wooden writing desk. */
public final class EstateManagementDeskScreen extends Screen {
    private static final int WOOD_DARK = 0xFF3B2115;
    private static final int WOOD = 0xFF6B4126;
    private static final int WOOD_LIGHT = 0xFF9B6A3A;
    private static final int LEATHER = 0xFF263F32;
    private static final int LEATHER_LIGHT = 0xFF3D5D49;
    private static final int PARCHMENT = 0xFFF1D99B;
    private static final int PARCHMENT_EDGE = 0xFFC79B58;
    private static final int INK = 0xFF312619;
    private static final int INK_MUTED = 0xFF66533A;
    private static final int BRASS = 0xFFD0A33B;

    private final EstateDeskPayload payload;
    private final List<Button> tabButtons = new ArrayList<>();
    private int selectedTab;
    private int scroll;
    private int maxScroll;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;

    public EstateManagementDeskScreen(EstateDeskPayload payload) {
        super(Component.translatable("screen.vintner.estate_desk.title"));
        this.payload = payload;
    }

    @Override
    protected void init() {
        panelWidth = Math.min(430, width - 20);
        panelHeight = Math.min(260, height - 20);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        tabButtons.clear();

        int tabCount = Math.max(1, payload.sections().size());
        int tabGap = 2;
        int tabWidth = Math.max(
                42,
                (panelWidth - 24 - (tabCount - 1) * tabGap) / tabCount
        );
        int tabX = left + 12;
        for (int index = 0; index < payload.sections().size(); index++) {
            final int target = index;
            Button button = Button.builder(
                    payload.sections().get(index).title(),
                    ignored -> selectTab(target)
            ).bounds(tabX, top + 47, tabWidth, 18).build();
            tabButtons.add(addRenderableWidget(button));
            tabX += tabWidth + tabGap;
        }

        addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                ignored -> onClose()
        ).bounds(left + panelWidth - 72, top + panelHeight - 25, 60, 16)
                .build());
        updateTabButtons();
    }

    private void selectTab(int index) {
        selectedTab = Mth.clamp(
                index,
                0,
                Math.max(0, payload.sections().size() - 1)
        );
        scroll = 0;
        updateTabButtons();
    }

    private void updateTabButtons() {
        for (int index = 0; index < tabButtons.size(); index++) {
            tabButtons.get(index).active = index != selectedTab;
        }
    }

    @Override
    public void extractBackground(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.fill(left, top, left + panelWidth, top + panelHeight, WOOD_DARK);
        graphics.fill(
                left + 4,
                top + 4,
                left + panelWidth - 4,
                top + panelHeight - 4,
                WOOD
        );
        graphics.outline(
                left + 7,
                top + 7,
                panelWidth - 14,
                panelHeight - 14,
                WOOD_LIGHT
        );

        graphics.fill(
                left + 11,
                top + 10,
                left + panelWidth - 11,
                top + 42,
                LEATHER
        );
        graphics.outline(
                left + 11,
                top + 10,
                panelWidth - 22,
                32,
                LEATHER_LIGHT
        );

        int paperTop = top + 69;
        int paperBottom = top + panelHeight - 31;
        graphics.fill(
                left + 13,
                paperTop,
                left + panelWidth - 13,
                paperBottom,
                PARCHMENT_EDGE
        );
        graphics.fill(
                left + 16,
                paperTop + 3,
                left + panelWidth - 16,
                paperBottom - 3,
                PARCHMENT
        );
        graphics.fill(
                left + 19,
                paperTop + 6,
                left + 22,
                paperBottom - 6,
                0x40A66F38
        );
        graphics.fill(
                left + panelWidth - 22,
                paperTop + 6,
                left + panelWidth - 19,
                paperBottom - 6,
                0x40A66F38
        );
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        graphics.text(
                font,
                payload.estateName(),
                left + 20,
                top + 16,
                BRASS,
                true
        );
        graphics.text(
                font,
                payload.subtitle(),
                left + 20,
                top + 29,
                0xFFD7C9A5,
                false
        );

        if (payload.sections().isEmpty()) {
            return;
        }

        EstateDeskPayload.Section section = payload.sections().get(
                Math.min(selectedTab, payload.sections().size() - 1)
        );
        int contentX = left + 28;
        int contentY = top + 79;
        int contentWidth = panelWidth - 56;
        int contentBottom = top + panelHeight - 42;

        graphics.text(
                font,
                section.title(),
                contentX,
                contentY,
                INK,
                false
        );
        graphics.fill(
                contentX,
                contentY + 11,
                contentX + contentWidth,
                contentY + 12,
                0x60825C2C
        );

        List<FormattedCharSequence> wrapped = wrappedLines(
                section.lines(),
                contentWidth
        );
        int visibleLines = Math.max(1, (contentBottom - contentY - 18) / 10);
        maxScroll = Math.max(0, wrapped.size() - visibleLines);
        scroll = Mth.clamp(scroll, 0, maxScroll);
        graphics.enableScissor(
                contentX,
                contentY + 16,
                contentX + contentWidth,
                contentBottom
        );
        for (int index = scroll;
                index < wrapped.size() && index - scroll < visibleLines;
                index++) {
            graphics.text(
                    font,
                    wrapped.get(index),
                    contentX,
                    contentY + 17 + (index - scroll) * 10,
                    index == scroll && scroll > 0 ? INK_MUTED : INK,
                    false
            );
        }
        graphics.disableScissor();

        if (maxScroll > 0) {
            graphics.text(
                    font,
                    Component.translatable(
                            "screen.vintner.estate_desk.scroll",
                            scroll + 1,
                            maxScroll + 1
                    ),
                    left + 18,
                    top + panelHeight - 21,
                    0xFFD7C9A5,
                    false
            );
        }
    }

    private List<FormattedCharSequence> wrappedLines(
            List<Component> lines,
            int width
    ) {
        List<FormattedCharSequence> wrapped = new ArrayList<>();
        for (Component line : lines) {
            wrapped.addAll(font.split(line, width));
            wrapped.add(FormattedCharSequence.EMPTY);
        }
        if (!wrapped.isEmpty()) {
            wrapped.removeLast();
        }
        return wrapped;
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontal,
            double vertical
    ) {
        if (vertical != 0.0D && maxScroll > 0) {
            scroll = Mth.clamp(
                    scroll - (int) Math.signum(vertical),
                    0,
                    maxScroll
            );
            return true;
        }
        return super.mouseScrolled(
                mouseX,
                mouseY,
                horizontal,
                vertical
        );
    }
}
