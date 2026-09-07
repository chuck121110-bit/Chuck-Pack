package net.chuck.chuckpack.modules.misc.oppstats;

import com.mojang.authlib.GameProfile;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.chuck.chuckpack.modules.misc.OppStats;
import net.chuck.chuckpack.modules.misc.OppStats.OppRecord;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class OppStatsScreen extends Screen {
    private static final ConcurrentMap<UUID, Identifier> mojangSkins = new ConcurrentHashMap<>();
    private static final Set<UUID> loadingMojangSkins = ConcurrentHashMap.newKeySet();

    private final Screen previous;
    private final OppStats module;
    private OppList list;
    private EditBox searchBox;
    private Button onlineButton;
    private Button historicalButton;
    private Button copyButton;
    private Button copyEventsButton;
    private boolean showOnline = true;
    private String searchQuery = "";
    private long nextReloadAt;
    private int infoScroll;
    private int infoMaxScroll;
    private int infoPanelX;
    private int infoPanelY;
    private int infoPanelW;
    private int infoPanelH;

    public OppStatsScreen(Screen previous, OppStats module) {
        super(Component.empty());
        this.previous = previous;
        this.module = module;
    }

    @Override
    protected void init() {
        int top = 68;
        int bottomPad = 44;
        int listHeight = height - top - bottomPad;
        int leftPanelX = 16;
        int leftPanelW = Math.min(440, Math.max(220, width / 2 - 36));
        list = new OppList(Minecraft.getInstance(), width / 2 - 20, listHeight,
            top, 24, module, showOnline, searchQuery);
        addRenderableWidget(list);

        addRenderableWidget(onlineButton = Button.builder(Component.literal("Online"), b -> {
            showOnline = true;
            list.reload(showOnline, searchQuery);
            updateModeButtonLabels();
        }).bounds(16, 12, 100, 20).build());

        addRenderableWidget(historicalButton = Button.builder(Component.literal("Historical"), b -> {
            showOnline = false;
            list.reload(showOnline, searchQuery);
            updateModeButtonLabels();
        }).bounds(122, 12, 110, 20).build());

        searchBox = new EditBox(font, leftPanelX, 36, leftPanelW, 18,
            Component.literal("Search"));
        searchBox.setHint(Component.literal("Search players..."));
        searchBox.setValue(searchQuery);
        searchBox.setResponder(value -> {
            searchQuery = value == null ? "" : value.trim();
            if (list != null)
                list.reload(showOnline, searchQuery);
        });
        addRenderableWidget(searchBox);

        copyButton = addRenderableWidget(Button.builder(Component.literal("Copy Profile"), b -> copySelected())
            .bounds(width - 360, height - 32, 110, 20).build());
        copyEventsButton = addRenderableWidget(Button.builder(Component.literal("Copy Events"), b -> copyEvents())
            .bounds(width - 242, height - 32, 110, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
            .bounds(width - 120, height - 32, 100, 20).build());
        updateModeButtonLabels();
    }

    @Override
    public void tick() {
        super.tick();
        if (System.currentTimeMillis() >= nextReloadAt) {
            list.reload(showOnline, searchQuery);
            nextReloadAt = System.currentTimeMillis() + 700L;
        }
        updateModeButtonLabels();
        boolean hasSelection = list.getSelectedRecord() != null;
        copyButton.active = hasSelection;
        copyEventsButton.active = hasSelection;
    }

    private void updateModeButtonLabels() {
        if (onlineButton != null)
            onlineButton.setMessage(Component.literal("Online (" + module.getOnlineRecords().size() + ")"));
        if (historicalButton != null)
            historicalButton.setMessage(Component.literal("Historical (" + module.getHistoricalRecords().size() + ")"));
    }

    private void copySelected() {
        OppRecord rec = list.getSelectedRecord();
        if (rec == null)
            return;
        net.minecraft.client.Minecraft.getInstance().keyboardHandler.setClipboard(module.formatForClipboard(rec));
        ChatUtils.sendMsg(Component.literal("Copied OppStats profile for " + rec.name + "."));
    }

    private void copyEvents() {
        OppRecord rec = list.getSelectedRecord();
        if (rec == null)
            return;
        net.minecraft.client.Minecraft.getInstance().keyboardHandler.setClipboard(String.join("\n", rec.events));
        ChatUtils.sendMsg(Component.literal("Copied event log for " + rec.name + "."));
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreenAndShow(previous);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent key) {
        if (searchBox == null || !searchBox.isFocused()) {
            if (key.key() == GLFW.GLFW_KEY_UP)
                return list != null && list.moveSelection(-1);
            if (key.key() == GLFW.GLFW_KEY_DOWN)
                return list != null && list.moveSelection(1);
        }
        return super.keyPressed(key);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount,
        double verticalAmount) {
        if (mouseX >= infoPanelX && mouseX <= infoPanelX + infoPanelW
            && mouseY >= infoPanelY && mouseY <= infoPanelY + infoPanelH
            && infoMaxScroll > 0) {
            infoScroll -= (int) Math.round(verticalAmount * 18.0);
            if (infoScroll < 0)
                infoScroll = 0;
            if (infoScroll > infoMaxScroll)
                infoScroll = infoMaxScroll;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(font, "OppStats", width / 2, 12, 0xFFFFFFFF);

        OppRecord selected = list.getSelectedRecord();
        if (selected == null)
            return;

        int panelX = width / 2 + 8;
        int panelW = width / 2 - 20;
        drawModelPanel(context, selected, panelX, 40);
        int equipmentY = 40;
        int equipmentH = drawEquipmentPanel(context, selected, panelX + 94,
            equipmentY, panelW - 96, mouseX, mouseY);
        int infoY = equipmentY + equipmentH + 8;
        drawInfoPanel(context, selected, panelX, infoY, panelW);
    }

    private void drawModelPanel(GuiGraphicsExtractor context, OppRecord selected, int x, int y) {
        Identifier skin = resolveSkin(selected.uuid);
        context.fill(x, y, x + 86, y + 120, 0x55000000);
        context.outline(x, y, x + 86, y + 120, 0x88808080);

        blit(context, skin, x + 28, y + 7, 8, 8, 30, 30, 8, 8, 64, 64);
        blit(context, skin, x + 28, y + 7, 40, 8, 30, 30, 8, 8, 64, 64);
        blit(context, skin, x + 31, y + 37, 20, 20, 24, 36, 8, 12, 64, 64);
        blit(context, skin, x + 19, y + 38, 44, 20, 12, 34, 4, 12, 64, 64);
        blit(context, skin, x + 55, y + 38, 44, 20, 12, 34, 4, 12, 64, 64);
        blit(context, skin, x + 31, y + 73, 4, 20, 12, 20, 4, 12, 64, 64);
        blit(context, skin, x + 43, y + 73, 4, 20, 12, 20, 4, 12, 64, 64);
    }

    private void blit(GuiGraphicsExtractor context, Identifier skin, int x, int y, int u, int v,
        int w, int h, int texW, int texH, int textureWidth, int textureHeight) {
        context.blit(RenderPipelines.GUI_TEXTURED, skin,
            x, y, u, v, w, h, texW, texH, textureWidth, textureHeight, 0xFFFFFFFF);
    }

    private int drawEquipmentPanel(GuiGraphicsExtractor context, OppRecord selected, int x, int y,
        int w, int mouseX, int mouseY) {
        int panelH = 254;
        context.fill(x, y, x + w, y + panelH, 0x55000000);
        context.outline(x, y, x + w, y + panelH, 0x88808080);

        SlotView[] slots = new SlotView[] {
            new SlotView("Head", selected.helmet),
            new SlotView("Chest", selected.chest),
            new SlotView("Legs", selected.legs),
            new SlotView("Feet", selected.boots),
            new SlotView("Main", selected.mainHand),
            new SlotView("Off", selected.offHand)
        };

        int rowH = 40;
        for (int i = 0; i < slots.length; i++) {
            int rowY = y + 8 + i * rowH;
            int iconX = x + 40;
            int labelX = x + 8;
            context.text(font, Component.literal(slots[i].label), labelX, rowY + 11,
                0xFFD0E0FF, false);
            ItemStack stack = parseStackIdToDisplay(slots[i].raw);
            if (!stack.isEmpty()) {
                context.item(stack, iconX, rowY + 6);
                context.itemDecorations(font, stack, iconX, rowY + 6);
            }
            String name = extractItemName(slots[i].raw);
            String durability = durabilityOnly(slots[i].raw);
            String nameLine = name.equals("N/A")
                ? "N/A"
                : name + (durability.isBlank() ? "" : "  |  " + durability);
            String enchantText = enchantsOnly(slots[i].raw);
            boolean hasEnchants = enchantText != null && !enchantText.isBlank()
                && !enchantText.equals("N/A") && !enchantText.equals("none");
            int contentX = iconX + 32;
            int contentW = w - (contentX - x) - 8;
            int nameY = hasEnchants ? rowY : rowY + 11;
            drawTrimmed(context, nameLine, contentX, nameY, contentW, 0xFFE0E0E0);
            drawEnchantLines(context, enchantText, contentX, rowY + 11, contentW, 2);
            if (mouseX >= iconX && mouseX <= iconX + 18 && mouseY >= rowY + 6
                && mouseY <= rowY + 24)
                showSlotTooltip(context, slots[i], mouseX, mouseY);
            else if (mouseX >= contentX && mouseX <= contentX + contentW
                && mouseY >= rowY && mouseY <= rowY + rowH)
                showSlotTooltip(context, slots[i], mouseX, mouseY);
        }
        return panelH;
    }

    private void showSlotTooltip(GuiGraphicsExtractor context, SlotView slot, int mouseX, int mouseY) {
        ArrayList<Component> lines = new ArrayList<>();
        String name = extractItemName(slot.raw);
        lines.add(Component.literal(slot.label + ": " + name));
        String dur = durabilityOnly(slot.raw);
        if (!dur.isBlank())
            lines.add(Component.literal(dur));
        String ench = enchantsOnly(slot.raw);
        if (ench.equals("N/A") || ench.equals("none") || ench.isBlank())
            lines.add(Component.literal("No enchantments"));
        else
            for (String part : ench.split("\\s*,\\s*"))
                if (!part.isBlank())
                    lines.add(Component.literal(part.trim()));
        context.setComponentTooltipForNextFrame(font, lines, mouseX, mouseY);
    }

    private void drawInfoPanel(GuiGraphicsExtractor context, OppRecord selected, int x, int y, int w) {
        infoPanelX = x;
        infoPanelY = y;
        infoPanelW = w;
        infoPanelH = height - 44 - y;
        context.fill(x, y, x + w, y + infoPanelH, 0x44000000);
        context.outline(x, y, x + w, y + infoPanelH, 0x66808080);

        ArrayList<InfoLine> lines = new ArrayList<>();
        lines.add(new InfoLine("Identity", true));
        lines.add(new InfoLine("Name: " + selected.name, false));
        lines.add(new InfoLine("UUID: " + selected.uuid, false));
        lines.add(new InfoLine("Last seen: " + module.formatLastSeen(selected), false));
        lines.add(new InfoLine("Last join: " + module.formatEpoch(selected.lastJoinAt), false));
        lines.add(new InfoLine("Last leave: " + module.formatEpoch(selected.lastLeaveAt), false));
        lines.add(new InfoLine("", false));
        lines.add(new InfoLine("Location", true));
        lines.add(new InfoLine("Pos: " + formatPos(selected) + " Dist: "
            + formatDistance(selected), false));
        lines.add(new InfoLine("", false));
        lines.add(new InfoLine("Status", true));
        lines.add(new InfoLine("HP: " + fmt(selected.health) + "  Abs: "
            + fmt(selected.absorption) + "  Armor: " + na(selected.armorValue)
            + "  Ping: " + na(getLivePing(selected)), false));
        lines.add(new InfoLine("GameType: " + na(selected.GameType), false));
        lines.add(new InfoLine("", false));
        lines.add(new InfoLine("Stats", true));
        lines.add(new InfoLine("Joins: " + selected.joinCount, false));
        lines.add(new InfoLine("", false));
        lines.add(new InfoLine("Recent observed items", true));
        if (selected.recentItems.isEmpty())
            lines.add(new InfoLine("N/A", false));
        else
            for (String item : selected.recentItems)
                lines.add(new InfoLine(item, false));
        lines.add(new InfoLine("", false));
        lines.add(new InfoLine("Recent events", true));
        for (String event : selected.events)
            lines.add(new InfoLine(event, false));

        int lineHeight = 11;
        int totalHeight = lines.size() * lineHeight + 6;
        infoMaxScroll = Math.max(0, totalHeight - (infoPanelH - 8));
        if (infoScroll > infoMaxScroll)
            infoScroll = infoMaxScroll;

        context.enableScissor(x + 2, y + 2, x + w - 2, y + infoPanelH - 2);
        int lineY = y + 6 - infoScroll;
        for (InfoLine line : lines) {
            if (lineY > y - lineHeight && lineY < y + infoPanelH - 2) {
                if (line.title)
                    drawSectionTitle(context, line.Component, x + 6, lineY);
                else if (!line.Component.isBlank())
                    drawLine(context, line.Component, x + 6, lineY, w - 12);
            }
            lineY += lineHeight;
        }
        context.disableScissor();
    }

    private void drawLine(GuiGraphicsExtractor context, String text, int x, int y, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            context.text(font, Component.literal(text), x, y, 0xFFE0E0E0, false);
            return;
        }
        drawScaledText(context, font, text, x, y, 0xFFE0E0E0, false, 0.82f);
    }

    private void drawScaledText(GuiGraphicsExtractor context, Font font, String text, int x,
        int y, int color, boolean shadow, float scale) {
        context.pose().pushMatrix();
        context.pose().scale(scale, scale);
        context.text(font, Component.literal(text), Math.round(x / scale),
            Math.round(y / scale), color, shadow);
        context.pose().popMatrix();
    }

    private void drawTrimmed(GuiGraphicsExtractor context, String text, int x, int y, int maxW,
        int color) {
        if (text == null || text.isBlank() || text.equals("N/A") || text.equals("none"))
            return;
        String t = text;
        while (font.width(t) > maxW && t.length() > 3)
            t = t.substring(0, t.length() - 1);
        if (!t.equals(text))
            t = t.substring(0, Math.max(1, t.length() - 1)) + "\u2026";
        context.text(font, Component.literal(t), x, y, color, false);
    }

    private void drawEnchantLines(GuiGraphicsExtractor context, String enchants, int x, int y,
        int maxW, int maxLines) {
        enchants = normalizeEnchantText(enchants);
        if (enchants == null || enchants.isBlank() || enchants.equals("N/A")
            || enchants.equals("none"))
            return;

        String[] parts = enchants.split("\\s*,\\s*");
        int line = 0;
        for (String part : parts) {
            if (part == null || part.isBlank())
                continue;
            if (line >= maxLines) {
                drawTrimmed(context, "+" + (parts.length - line) + " more", x,
                    y + line * 9, maxW, 0xFFB8FFB8);
                return;
            }
            drawTrimmed(context, part.trim(), x, y + line * 9, maxW, 0xFF96FF96);
            line++;
        }
    }

    private void drawSectionTitle(GuiGraphicsExtractor context, String title, int x, int y) {
        context.text(font, Component.literal(title), x, y, 0xFFD0E0FF, false);
    }

    private ItemStack parseStackIdToDisplay(String Component) {
        if (Component == null || Component.equals("N/A"))
            return ItemStack.EMPTY;
        int idStart = Component.indexOf("id=");
        if (idStart < 0)
            return ItemStack.EMPTY;
        int idEnd = Component.indexOf(',', idStart);
        String id = idEnd < 0 ? Component.substring(idStart + 3)
            : Component.substring(idStart + 3, idEnd);
        Identifier identifier = Identifier.tryParse(id.trim());
        if (identifier == null)
            return ItemStack.EMPTY;
        try {
            Item item = BuiltInRegistries.ITEM.getValue(identifier);
            return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    private String extractItemName(String stackText) {
        if (stackText == null || stackText.equals("N/A"))
            return "N/A";
        int cut = stackText.indexOf(" x");
        return cut > 0 ? stackText.substring(0, cut).trim() : stackText;
    }

    private String enchantsOnly(String stackText) {
        if (stackText == null || stackText.equals("N/A"))
            return "N/A";
        int i = stackText.indexOf("enchants=");
        if (i < 0)
            return "N/A";
        return normalizeEnchantText(stackText.substring(i + "enchants=".length()).trim());
    }

    private String normalizeEnchantText(String Component) {
        if (Component == null)
            return null;

        String normalized = Component.trim();
        while (normalized.endsWith("]"))
            normalized = normalized.substring(0, normalized.length() - 1).trim();
        return normalized;
    }

    private String durabilityOnly(String stackText) {
        if (stackText == null || stackText.equals("N/A"))
            return "";
        int i = stackText.indexOf("durability=");
        if (i < 0)
            return "";
        int end = stackText.indexOf(',', i);
        String v = end < 0 ? stackText.substring(i + "durability=".length())
            : stackText.substring(i + "durability=".length(), end);
        if (v.equalsIgnoreCase("n/a"))
            return "";
        return "Durability: " + v.trim();
    }

    private String formatPos(OppRecord r) {
        if (r.lastPos == null)
            return "N/A";
        return String.format("(%.2f, %.2f, %.2f)", r.lastPos.x, r.lastPos.y, r.lastPos.z);
    }

    private String formatDistance(OppRecord r) {
        if (Double.isNaN(r.distance))
            return "N/A";
        return String.format("%.2f", r.distance);
    }

    private String fmt(float v) {
        if (Float.isNaN(v))
            return "N/A";
        return String.format("%.1f", v);
    }

    private static String na(int value) {
        return value < 0 ? "N/A" : String.valueOf(value);
    }

    private String na(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    private static int getLivePing(OppRecord rec) {
        Minecraft mc = Minecraft.getInstance();
        if (rec == null || rec.uuid == null || mc.getConnection() == null)
            return rec == null ? -1 : rec.ping;

        PlayerInfo info = mc.getConnection().getPlayerInfo(rec.uuid);
        if (info != null)
            return info.getLatency();

        return rec.ping;
    }

    private static Identifier resolveSkin(UUID uuid) {
        requestMojangSkin(uuid);
        Identifier mojangSkin = mojangSkins.get(uuid);
        if (mojangSkin != null)
            return mojangSkin;

        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            PlayerInfo info = mc.getConnection().getPlayerInfo(uuid);
            if (info != null)
                return DefaultPlayerSkin.getDefaultTexture();
        }

        return DefaultPlayerSkin.getDefaultTexture();
    }

    private static void requestMojangSkin(UUID uuid) {
        if (uuid == null || !loadingMojangSkins.add(uuid))
            return;

        Minecraft mc = Minecraft.getInstance();
        CompletableFuture.supplyAsync(() -> {
            try {
                return new GameProfile(uuid, null);
            } catch (Exception e) {
                return null;
            }
        }).thenCompose(profile -> {
            if (profile == null)
                return CompletableFuture.completedFuture(Optional.<PlayerSkin>empty());
            return mc.getSkinManager().get(profile);
        }).thenAccept(optSkin -> {
            optSkin.ifPresent(skin -> mojangSkins.put(uuid, skin.body().texturePath()));
            loadingMojangSkins.remove(uuid);
        }).exceptionally(error -> {
            loadingMojangSkins.remove(uuid);
            return null;
        });
    }

    private record SlotView(String label, String raw) {}

    private record InfoLine(String Component, boolean title) {}

    private static final class OppList extends AbstractSelectionList<OppList.Entry> {
        private final OppStats module;
        private boolean showOnline;

        public OppList(Minecraft mc, int width, int height, int top, int itemHeight,
            OppStats module, boolean showOnline, String searchQuery) {
            super(mc, width, height, top, itemHeight);
            this.module = module;
            this.showOnline = showOnline;
            reload(showOnline, searchQuery);
            ensureSelection();
        }

        void reload(boolean showOnline, String searchQuery) {
            this.showOnline = showOnline;
            String prev = captureSelection();
            clearEntries();
            List<OppRecord> src = showOnline ? module.getOnlineRecords()
                : module.getHistoricalRecords();
            String query = searchQuery == null ? ""
                : searchQuery.trim().toLowerCase(Locale.ROOT);
            for (OppRecord rec : src) {
                if (!query.isBlank() && !matchesSearch(rec, query))
                    continue;
                addEntry(new Entry(this, rec));
            }
            restoreSelection(prev);
        }

        OppRecord getSelectedRecord() {
            Entry selected = getSelected();
            return selected == null ? null : selected.record;
        }

        boolean moveSelection(int delta) {
            if (delta == 0)
                return false;

            List<Entry> entries = children();
            if (entries.isEmpty())
                return false;

            Entry selected = getSelected();
            int index = entries.indexOf(selected);
            if (index < 0)
                index = delta > 0 ? -1 : entries.size();

            int next = Math.max(0, Math.min(entries.size() - 1, index + delta));
            if (next == index)
                return false;

            Entry entry = entries.get(next);
            setSelected(entry);
            centerScrollOn(entry);
            return true;
        }

        @Override
        public int getRowWidth() {
            return width - 20;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {
            this.defaultButtonNarrationText(builder);
        }

        private String captureSelection() {
            Entry selected = getSelected();
            return selected == null ? null : selected.record.uuid.toString();
        }

        private void restoreSelection(String uuid) {
            if (uuid != null) {
                for (Entry e : children()) {
                    if (e.record.uuid.toString().equals(uuid)) {
                        setSelected(e);
                        return;
                    }
                }
            }
            ensureSelection();
        }

        private void ensureSelection() {
            List<Entry> entries = children();
            if (!entries.isEmpty() && getSelected() == null)
                setSelected(entries.get(0));
        }

        private boolean matchesSearch(OppRecord rec, String query) {
            if (rec == null || query == null || query.isBlank())
                return true;

            String name = rec.name == null ? "" : rec.name.toLowerCase(Locale.ROOT);
            if (name.contains(query))
                return true;

            String uuid = rec.uuid == null ? ""
                : rec.uuid.toString().toLowerCase(Locale.ROOT);
            return uuid.contains(query);
        }

        private static final class Entry extends AbstractSelectionList.Entry<Entry> {
            private final OppRecord record;

            public Entry(OppList parent, OppRecord record) {
                super();
                this.record = record;
            }

            @Override
            public void extractContent(GuiGraphicsExtractor context, int x, int y, boolean hovered,
                float delta) {
                Identifier skin = resolveSkin(record.uuid);
                context.blit(RenderPipelines.GUI_TEXTURED,
                    skin, x + 2, y + 2, 8, 8, 16, 16, 8, 8, 64, 64, 0xFFFFFFFF);
                Minecraft mc = Minecraft.getInstance();
                context.text(mc.font, Component.literal(record.name), x + 24, y + 2,
                    0xFFFFFFFF, false);
                String pingText = record.online
                    ? "ping: " + na(getLivePing(record)) + " ms"
                    : "offline";
                context.text(mc.font, Component.literal(pingText), x + 24, y + 12,
                    record.online ? 0xFF55FF55 : 0xFFFF7777, false);
            }
        }
    }
}
