package com.zenith.vintner.client.screen;

import com.zenith.vintner.network.EstateDeskPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final int MAP_BORDER = 0xFF7A4A29;
    private static final int MAP_INNER_BORDER = 0xFFD2B267;
    private static final int PLOT = 0xFF7D2E2E;
    private static final int PLOT_SELECTED = 0xFFFFD24A;
    private static final int LIST_SELECTED = 0x60C48A32;
    private static final int MAP_SIZE = 128;
    private static final int PLOT_ROWS = 4;

    private final EstateDeskPayload payload;
    private final Map<Integer, MapRenderState> mapRenderStates =
            new HashMap<>();
    private final List<Button> tabButtons = new ArrayList<>();
    private int selectedTab;
    private int selectedPlot = -1;
    private int plotScroll;
    private int scroll;
    private int maxScroll;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private int mapX;
    private int mapY;
    private int mapSize;
    private int plotListX;
    private int plotListY;
    private int plotListWidth;
    private AtlasLayout atlasLayout;

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

        if (isMapTab()) {
            renderMapWorkspace(graphics, mouseX, mouseY);
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

    private boolean isMapTab() {
        return !payload.sections().isEmpty()
                && selectedTab == payload.sections().size() - 1;
    }

    private void renderMapWorkspace(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        atlasLayout = null;
        int contentX = left + 27;
        int contentY = top + 77;
        int contentRight = left + panelWidth - 27;
        int contentBottom = top + panelHeight - 39;

        graphics.text(
                font,
                Component.translatable(
                        "screen.vintner.estate_desk.map.title"
                ),
                contentX,
                contentY,
                INK,
                false
        );

        if (payload.maps().isEmpty()) {
            graphics.textWithWordWrap(
                    font,
                    Component.translatable(
                            payload.atlasConnected()
                                    ? "screen.vintner.estate_desk.map.empty_atlas"
                                    : "screen.vintner.estate_desk.map.missing_module"
                    ),
                    contentX + 18,
                    contentY + 40,
                    contentRight - contentX - 36,
                    INK_MUTED,
                    false
            );
            return;
        }

        List<MapTile> tiles = new ArrayList<>();
        if (minecraft.level != null) {
            for (EstateDeskPayload.MapInfo info : payload.maps()) {
                MapId id = new MapId(info.id());
                MapItemSavedData data = minecraft.level.getMapData(id);
                if (data == null) {
                    continue;
                }
                MapRenderState state = mapRenderStates.computeIfAbsent(
                        info.id(),
                        ignored -> new MapRenderState()
                );
                Minecraft.getInstance().getMapRenderer().extractRenderState(
                        id,
                        data,
                        state
                );
                tiles.add(new MapTile(info, state));
            }
        }
        if (tiles.isEmpty()) {
            graphics.textWithWordWrap(
                    font,
                    Component.translatable(
                            "screen.vintner.estate_desk.map.loading"
                    ),
                    contentX + 18,
                    contentY + 40,
                    contentRight - contentX - 36,
                    INK_MUTED,
                    false
            );
            return;
        }

        mapSize = Math.min(
                MAP_SIZE,
                contentBottom - contentY - 16
        );
        mapX = contentX;
        mapY = contentY + 15;
        graphics.fill(
                mapX - 4,
                mapY - 4,
                mapX + mapSize + 4,
                mapY + mapSize + 4,
                MAP_BORDER
        );
        graphics.outline(
                mapX - 2,
                mapY - 2,
                mapSize + 4,
                mapSize + 4,
                MAP_INNER_BORDER
        );
        atlasLayout = AtlasLayout.create(payload.maps(), mapX, mapY, mapSize);
        for (MapTile tile : tiles) {
            float tileX = atlasLayout.screenX(
                    tile.info().centerX() - atlasLayout.halfMapBlocks()
            );
            float tileY = atlasLayout.screenY(
                    tile.info().centerZ() - atlasLayout.halfMapBlocks()
            );
            graphics.pose().pushMatrix();
            graphics.pose().translate(tileX, tileY);
            graphics.pose().scale(
                    atlasLayout.pixelScale(),
                    atlasLayout.pixelScale()
            );
            graphics.map(tile.state());
            graphics.pose().popMatrix();
        }

        List<Integer> visible = visiblePlotIndexes(atlasLayout);
        renderPlotOutlines(graphics, atlasLayout, visible);

        plotListX = mapX + mapSize + 13;
        plotListY = mapY + 12;
        plotListWidth = contentRight - plotListX;
        graphics.text(
                font,
                Component.translatable(
                        "screen.vintner.estate_desk.map.plots",
                        visible.size()
                ),
                plotListX,
                mapY,
                INK,
                false
        );
        graphics.text(
                font,
                Component.translatable(
                        payload.atlasConnected()
                                ? "screen.vintner.estate_desk.map.atlas_info"
                                : "screen.vintner.estate_desk.map.legacy_info",
                        payload.maps().size(),
                        1 << payload.maps().getFirst().scale()
                ),
                plotListX,
                mapY + 10,
                INK_MUTED,
                false
        );

        int maxPlotScroll = Math.max(0, visible.size() - PLOT_ROWS);
        plotScroll = Mth.clamp(plotScroll, 0, maxPlotScroll);
        for (int row = 0; row < PLOT_ROWS; row++) {
            int visibleIndex = plotScroll + row;
            if (visibleIndex >= visible.size()) {
                break;
            }
            int plotIndex = visible.get(visibleIndex);
            EstateDeskPayload.PlotSummary plot = payload.plots().get(
                    plotIndex
            );
            int rowY = plotListY + 10 + row * 14;
            if (plotIndex == selectedPlot) {
                graphics.fill(
                        plotListX - 2,
                        rowY - 2,
                        contentRight,
                        rowY + 11,
                        LIST_SELECTED
                );
            }
            graphics.text(
                    font,
                    Component.literal((visibleIndex + 1) + ". " + plot.name()),
                    plotListX,
                    rowY,
                    plotIndex == selectedPlot ? 0xFF7A3D12 : INK,
                    false
            );
        }

        int detailsY = plotListY + 10 + PLOT_ROWS * 14 + 4;
        renderPlotDetails(graphics, contentRight, detailsY, visible);

        int outside = Math.max(0, payload.plots().size() - visible.size());
        if (outside > 0) {
            graphics.text(
                    font,
                    Component.translatable(
                            "screen.vintner.estate_desk.map.outside",
                            outside
                    ),
                    plotListX,
                    contentBottom - 9,
                    INK_MUTED,
                    false
            );
        }
    }

    private List<Integer> visiblePlotIndexes(AtlasLayout atlas) {
        List<Integer> result = new ArrayList<>();
        for (int index = 0; index < payload.plots().size(); index++) {
            EstateDeskPayload.PlotSummary plot = payload.plots().get(index);
            if (plot.dimension().equals(atlas.dimension())
                    && plot.maxX() >= atlas.minX()
                    && plot.minX() < atlas.maxXExclusive()
                    && plot.maxZ() >= atlas.minZ()
                    && plot.minZ() < atlas.maxZExclusive()) {
                result.add(index);
            }
        }
        return result;
    }

    private void renderPlotOutlines(
            GuiGraphicsExtractor graphics,
            AtlasLayout atlas,
            List<Integer> visible
    ) {
        for (int plotIndex : visible) {
            EstateDeskPayload.PlotSummary plot = payload.plots().get(
                    plotIndex
            );
            int x0 = Math.clamp(
                    Math.round(atlas.screenX(plot.minX())),
                    mapX,
                    mapX + mapSize - 1
            );
            int y0 = Math.clamp(
                    Math.round(atlas.screenY(plot.minZ())),
                    mapY,
                    mapY + mapSize - 1
            );
            int x1 = Math.clamp(
                    Math.round(atlas.screenX(plot.maxX())),
                    mapX,
                    mapX + mapSize - 1
            );
            int y1 = Math.clamp(
                    Math.round(atlas.screenY(plot.maxZ())),
                    mapY,
                    mapY + mapSize - 1
            );
            int color = plotIndex == selectedPlot
                    ? PLOT_SELECTED
                    : PLOT;
            graphics.outline(
                    Math.min(x0, x1),
                    Math.min(y0, y1),
                    Math.max(3, Math.abs(x1 - x0) + 1),
                    Math.max(3, Math.abs(y1 - y0) + 1),
                    color
            );
        }
    }

    private void renderPlotDetails(
            GuiGraphicsExtractor graphics,
            int contentRight,
            int detailsY,
            List<Integer> visible
    ) {
        if (selectedPlot < 0 || !visible.contains(selectedPlot)) {
            graphics.textWithWordWrap(
                    font,
                    Component.translatable(
                            visible.isEmpty()
                                    ? "screen.vintner.estate_desk.map.empty"
                                    : "screen.vintner.estate_desk.map.select"
                    ),
                    plotListX,
                    detailsY,
                    plotListWidth,
                    INK_MUTED,
                    false
            );
            return;
        }

        EstateDeskPayload.PlotSummary plot = payload.plots().get(selectedPlot);
        List<Component> lines = List.of(
                Component.translatable(
                        "screen.vintner.estate_desk.map.size",
                        plot.width(),
                        plot.depth(),
                        plot.area()
                ),
                Component.translatable(
                        "screen.vintner.estate_desk.map.vines",
                        plot.vineCount(),
                        plot.variety()
                ),
                Component.translatable(
                        "screen.vintner.estate_desk.map.condition",
                        plot.health(),
                        plot.projectedQuality()
                ),
                Component.translatable(
                        "screen.vintner.estate_desk.map.output",
                        plot.projectedYield(),
                        plot.irrigation()
                )
        );
        int y = detailsY;
        for (Component line : lines) {
            for (FormattedCharSequence wrapped : font.split(
                    line,
                    contentRight - plotListX
            )) {
                graphics.text(font, wrapped, plotListX, y, INK, false);
                y += 9;
            }
        }
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (isMapTab() && event.button() == 0 && atlasLayout != null) {
            List<Integer> visible = visiblePlotIndexes(atlasLayout);
            for (int row = 0; row < PLOT_ROWS; row++) {
                int visibleIndex = plotScroll + row;
                if (visibleIndex >= visible.size()) {
                    break;
                }
                int rowY = plotListY + 10 + row * 14;
                if (event.x() >= plotListX - 2
                        && event.x() <= plotListX + plotListWidth
                        && event.y() >= rowY - 2
                        && event.y() <= rowY + 11) {
                    selectedPlot = visible.get(visibleIndex);
                    return true;
                }
            }

            for (int plotIndex : visible) {
                EstateDeskPayload.PlotSummary plot = payload.plots().get(
                        plotIndex
                );
                int x0 = Math.round(atlasLayout.screenX(plot.minX()));
                int y0 = Math.round(atlasLayout.screenY(plot.minZ()));
                int x1 = Math.round(atlasLayout.screenX(plot.maxX()));
                int y1 = Math.round(atlasLayout.screenY(plot.maxZ()));
                if (event.x() >= Math.min(x0, x1) - 2
                        && event.x() <= Math.max(x0, x1) + 2
                        && event.y() >= Math.min(y0, y1) - 2
                        && event.y() <= Math.max(y0, y1) + 2) {
                    selectedPlot = plotIndex;
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontal,
            double vertical
    ) {
        if (isMapTab() && vertical != 0.0D && atlasLayout != null) {
            int visiblePlots = visiblePlotIndexes(atlasLayout).size();
            int maximum = Math.max(0, visiblePlots - PLOT_ROWS);
            if (maximum > 0) {
                plotScroll = Mth.clamp(
                        plotScroll - (int) Math.signum(vertical),
                        0,
                        maximum
                );
                return true;
            }
        }
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

    private record MapTile(
            EstateDeskPayload.MapInfo info,
            MapRenderState state
    ) {
    }

    private record AtlasLayout(
            int minX,
            int minZ,
            int maxXExclusive,
            int maxZExclusive,
            int blocksPerPixel,
            int halfMapBlocks,
            String dimension,
            float pixelScale,
            float originX,
            float originY
    ) {
        private static AtlasLayout create(
                List<EstateDeskPayload.MapInfo> maps,
                int canvasX,
                int canvasY,
                int canvasSize
        ) {
            EstateDeskPayload.MapInfo first = maps.getFirst();
            int blocksPerPixel = 1 << first.scale();
            int half = 64 * blocksPerPixel;
            int minX = maps.stream()
                    .mapToInt(map -> map.centerX() - half)
                    .min()
                    .orElse(first.centerX() - half);
            int minZ = maps.stream()
                    .mapToInt(map -> map.centerZ() - half)
                    .min()
                    .orElse(first.centerZ() - half);
            int maxX = maps.stream()
                    .mapToInt(map -> map.centerX() + half)
                    .max()
                    .orElse(first.centerX() + half);
            int maxZ = maps.stream()
                    .mapToInt(map -> map.centerZ() + half)
                    .max()
                    .orElse(first.centerZ() + half);
            float widthPixels = (maxX - minX) / (float) blocksPerPixel;
            float heightPixels = (maxZ - minZ) / (float) blocksPerPixel;
            float scale = canvasSize / Math.max(widthPixels, heightPixels);
            float renderedWidth = widthPixels * scale;
            float renderedHeight = heightPixels * scale;
            return new AtlasLayout(
                    minX,
                    minZ,
                    maxX,
                    maxZ,
                    blocksPerPixel,
                    half,
                    first.dimension(),
                    scale,
                    canvasX + (canvasSize - renderedWidth) / 2.0F,
                    canvasY + (canvasSize - renderedHeight) / 2.0F
            );
        }

        private float screenX(int worldX) {
            return originX
                    + (worldX - minX) / (float) blocksPerPixel
                    * pixelScale;
        }

        private float screenY(int worldZ) {
            return originY
                    + (worldZ - minZ) / (float) blocksPerPixel
                    * pixelScale;
        }
    }
}
