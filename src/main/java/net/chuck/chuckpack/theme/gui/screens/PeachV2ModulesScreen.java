package net.chuck.chuckpack.theme.gui.screens;

import net.chuck.chuckpack.theme.gui.themes.base.PeachV2GuiTheme;
import net.chuck.chuckpack.theme.gui.themes.base.widgets.input.WBaseSlider;
import net.chuck.chuckpack.theme.gui.themes.base.widgets.input.WBaseTextBox;
import net.chuck.chuckpack.theme.gui.themes.base.widgets.pressable.WBaseCheckbox;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.input.WSlider;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Peach V2 click GUI — 1:1 layout port of the LazyUI overlay template.
 * Design units: 736x472 card, 56-wide icon rail, 2x304 module columns.
 * All drawing happens in widgets (framework renderer); the screen only
 * computes the template layout every frame in onRenderBefore.
 */
public class PeachV2ModulesScreen extends TabScreen {
    final PeachV2GuiTheme theme;

    // Design constants (template pixels)
    static final double CARD_W = 736, CARD_H = 472, CARD_R = 16;
    static final double RAIL_X = 12, RAIL_Y = 12, RAIL_W = 56, RAIL_H = 448, RAIL_R = 12;
    static final double BODY_X = 80, BODY_Y = 12, BODY_W = 644, BODY_H = 448, BODY_R = 12;
    static final double COL_W = 304, ROW_H = 40, ROW_GAP = 8, ROW_R = 8;
    static final double COL_TOP = 24, SIDE_PAD = 12;

    final Color bgFill = new Color(14, 10, 25, 210);
    final Color rowBg = new Color(255, 255, 255, 5);
    final Color rowLine = new Color(255, 255, 255, 10);
    final Color dim = new Color(255, 255, 255, 31);
    final Color faint = new Color(255, 255, 255, 10);
    final Color keyOnBg = new Color(255, 150, 118, 41);
    final Color activeBlue = new Color(81, 110, 234, 255);

    final List<Category> categories = new ArrayList<>();
    int selected = 0;
    Module expanded = null;
    double scroll = 0;

    double k = 1, cardX = 0, cardY = 0;

    static class BoundWidget {
        final WWidget widget;
        final Setting<?> setting;
        BoundWidget(WWidget widget, Setting<?> setting) {
            this.widget = widget;
            this.setting = setting;
        }
    }

    final Map<Module, List<BoundWidget>> widgetCache = new HashMap<>();
    final List<Cell<ModuleCard>> cardCells = new ArrayList<>();

    V2Root root;
    RailWidget rail;

    public PeachV2ModulesScreen(PeachV2GuiTheme theme) {
        super(theme, Tabs.get().getFirst());
        this.theme = theme;
        for (Category category : Modules.loopCategories()) {
            try {
                List<Module> modules = Modules.get().getGroup(category).stream()
                    .filter(m -> !Config.get().hiddenModules.get().contains(m))
                    .toList();
                if (!modules.isEmpty()) categories.add(category);
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public void initWidgets() {
        root = add(new V2Root()).widget();
        rail = new RailWidget();
        root.add(rail);
        rebuildCards();
    }

    List<Module> currentModules() {
        if (categories.isEmpty()) return List.of();
        try {
            return Modules.get().getGroup(categories.get(selected)).stream()
                .filter(m -> !Config.get().hiddenModules.get().contains(m))
                .toList();
        } catch (Throwable t) {
            return List.of();
        }
    }

    void rebuildCards() {
        for (Cell<ModuleCard> cell : cardCells) root.remove(cell);
        cardCells.clear();
        for (Module m : currentModules()) {
            ModuleCard card = new ModuleCard(m);
            cardCells.add(root.add(card));
        }
        hideAllSettingWidgets();
        if (expanded != null && !currentModules().contains(expanded)) expanded = null;
    }

    void selectCategory(int i) {
        selected = i;
        expanded = null;
        scroll = 0;
        hideAllSettingWidgets();
        rebuildCards();
        invalidate();
    }

    void toggleExpanded(Module m) {
        expanded = expanded == m ? null : m;
        if (expanded != null) widgetsFor(expanded);
        hideAllSettingWidgets();
        invalidate();
    }

    // ---------- geometry ----------

    double scaleK() {
        return Math.min(1, Math.min((width - 20) / CARD_W, (height - 20) / CARD_H));
    }

    double X(double dx) { return cardX + dx * k; }
    double Y(double dy) { return cardY + dy * k; }

    static double colX(int col) {
        return BODY_X + SIDE_PAD + col * (COL_W + 12);
    }

    static List<Setting<?>> flatSettings(Module m) {
        List<Setting<?>> out = new ArrayList<>();
        for (Object o : m.settings) {
            if (o instanceof Setting<?> s) out.add(s);
        }
        return out;
    }

    static double expandedSettingsHeight(Module m) {
        double h = 0;
        for (Setting<?> s : flatSettings(m)) {
            try {
                if (!s.isVisible()) continue;
            } catch (Throwable ignored) {}
            if (s instanceof BoolSetting) h += 22;
            else if (s instanceof DoubleSetting ds) { if (!ds.noSlider) h += 42; }
            else if (s instanceof IntSetting is) { if (!is.noSlider) h += 42; }
            else if (s instanceof EnumSetting) h += 58;
            else if (s instanceof StringSetting) h += 54;
        }
        return h == 0 ? 0 : h + 20;
    }

    static double expandedHeight(Module m) {
        return ROW_H + 8 + expandedSettingsHeight(m);
    }

    static class Row {
        final Module module;
        final int col;
        final double y;
        final boolean isExpanded;
        Row(Module module, int col, double y, boolean isExpanded) {
            this.module = module;
            this.col = col;
            this.y = y;
            this.isExpanded = isExpanded;
        }
    }

    List<Row> layoutRows() {
        List<Row> rows = new ArrayList<>();
        double[] y = { BODY_Y + COL_TOP, BODY_Y + COL_TOP };
        List<Module> modules = currentModules();
        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);
            int col = i % 2;
            boolean ex = m == expanded;
            rows.add(new Row(m, col, y[col], ex));
            y[col] += (ex ? expandedHeight(m) : ROW_H) + ROW_GAP;
        }
        return rows;
    }

    double contentHeight(List<Row> rows) {
        double h = 0;
        for (Row row : rows) {
            double bottom = row.y + (row.isExpanded ? expandedHeight(row.module) : ROW_H);
            h = Math.max(h, bottom - (BODY_Y + COL_TOP));
        }
        return h;
    }

    double maxScroll(List<Row> rows) {
        return Math.max(0, contentHeight(rows) - (BODY_H - COL_TOP - SIDE_PAD));
    }

    // ---------- layout pass ----------

    @Override
    protected void onRenderBefore(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.onRenderBefore(graphics, mouseX, mouseY, delta);
        k = scaleK();
        cardX = (width - CARD_W * k) / 2;
        cardY = (height - CARD_H * k) / 2;

        List<Row> rows = layoutRows();
        double ms = maxScroll(rows);
        if (scroll > ms) scroll = ms;
        if (scroll < 0) scroll = 0;

        double bodyTop = Y(BODY_Y), bodyBottom = Y(BODY_Y + BODY_H);

        // Rail
        rail.x = X(RAIL_X);
        rail.y = Y(RAIL_Y);
        rail.width = RAIL_W * k;
        rail.height = RAIL_H * k;

        hideAllSettingWidgets();
        if (expanded != null) widgetsFor(expanded);

        // Cards
        for (int i = 0; i < cardCells.size() && i < rows.size(); i++) {
            Row row = rows.get(i);
            ModuleCard card = cardCells.get(i).widget();
            double x = X(colX(row.col));
            double y = Y(row.y - scroll);
            double w = COL_W * k;
            double h = (row.isExpanded ? expandedHeight(row.module) : ROW_H) * k;
            card.x = x;
            card.y = y;
            card.width = w;
            card.height = h;
            card.visible = y + h >= bodyTop && y <= bodyBottom;
            if (row.isExpanded && card.visible) {
                positionSettings(row, x, y, bodyTop, bodyBottom);
            }
        }
    }

    void positionSettings(Row row, double x, double y, double bodyTop, double bodyBottom) {
        Module m = row.module;
        double sx = x + 14 * k;
        double sw = 276 * k;
        double sy = y + (ROW_H + 8) * k;

        for (Setting<?> s : flatSettings(m)) {
            try {
                if (!s.isVisible()) continue;
            } catch (Throwable ignored) {}
            BoundWidget bw = findWidget(m, s);
            double adv;
            if (s instanceof BoolSetting bs) {
                adv = 22 * k;
                if (bw != null) {
                    bw.widget.visible = inBody(sy, 14 * k, bodyTop, bodyBottom);
                    bw.widget.move(sx + sw - 14 * k, sy);
                    bw.widget.width = 14 * k;
                    bw.widget.height = 14 * k;
                    if (((WBaseCheckbox) bw.widget).checked != bs.get()) ((WBaseCheckbox) bw.widget).checked = bs.get();
                }
            } else if (s instanceof DoubleSetting ds) {
                if (ds.noSlider) continue;
                adv = 42 * k;
                if (bw != null) {
                    bw.widget.visible = inBody(sy, 30 * k, bodyTop, bodyBottom);
                    bw.widget.move(sx, sy + 18 * k);
                    bw.widget.width = sw;
                    bw.widget.height = 12 * k;
                    double wv = ((WSlider) bw.widget).get();
                    if (Math.abs(wv - ds.get()) > 1e-9) ((WSlider) bw.widget).set(ds.get());
                }
            } else if (s instanceof IntSetting is) {
                if (is.noSlider) continue;
                adv = 42 * k;
                if (bw != null) {
                    bw.widget.visible = inBody(sy, 30 * k, bodyTop, bodyBottom);
                    bw.widget.move(sx, sy + 18 * k);
                    bw.widget.width = sw;
                    bw.widget.height = 12 * k;
                    double wv = ((WSlider) bw.widget).get();
                    if (Math.abs(wv - is.get()) > 1e-9) ((WSlider) bw.widget).set(is.get());
                }
            } else if (s instanceof EnumSetting) {
                adv = 58 * k;
            } else if (s instanceof StringSetting) {
                adv = 54 * k;
                if (bw != null) {
                    bw.widget.visible = inBody(sy, 38 * k, bodyTop, bodyBottom);
                    bw.widget.move(sx, sy + 16 * k);
                    bw.widget.width = sw;
                    bw.widget.height = 22 * k;
                }
            } else {
                continue;
            }
            sy += adv;
        }
    }

    private static boolean inBody(double y, double h, double top, double bottom) {
        return y + h >= top && y <= bottom;
    }

    // ---------- setting widgets ----------

    List<BoundWidget> widgetsFor(Module m) {
        return widgetCache.computeIfAbsent(m, mod -> {
            List<BoundWidget> list = new ArrayList<>();
            for (Setting<?> s : flatSettings(mod)) {
                try {
                    if (!s.isVisible()) continue;
                } catch (Throwable ignored) {}
                try {
                    if (s instanceof BoolSetting bs) {
                        WBaseCheckbox cb = new WBaseCheckbox(bs.get()) {
                            @Override
                            protected void onPressed(int button) {
                                super.onPressed(button);
                                bs.set(checked);
                            }
                        };
                        root.add(cb);
                        cb.init();
                        list.add(new BoundWidget(cb, s));
                    } else if (s instanceof DoubleSetting ds) {
                        if (ds.noSlider) continue;
                        WSlider sl = theme.slider(ds.get(), ds.sliderMin, ds.sliderMax);
                        sl.action = () -> ds.set(sl.get());
                        root.add(sl);
                        sl.init();
                        list.add(new BoundWidget(sl, s));
                    } else if (s instanceof IntSetting is) {
                        if (is.noSlider) continue;
                        WSlider sl = theme.slider(is.get(), is.sliderMin, is.sliderMax);
                        sl.action = () -> is.set((int) Math.round(sl.get()));
                        root.add(sl);
                        sl.init();
                        list.add(new BoundWidget(sl, s));
                    } else if (s instanceof StringSetting ss) {
                        WTextBox tb = theme.textBox(ss.get());
                        tb.action = () -> ss.set(tb.get());
                        tb.actionOnUnfocused = () -> ss.set(tb.get());
                        root.add(tb);
                        tb.init();
                        list.add(new BoundWidget(tb, s));
                    }
                } catch (Throwable ignored) {}
            }
            return list;
        });
    }

    BoundWidget findWidget(Module m, Setting<?> s) {
        List<BoundWidget> list = widgetCache.get(m);
        if (list == null) return null;
        for (BoundWidget bw : list) {
            if (bw.setting == s) return bw;
        }
        return null;
    }

    void hideAllSettingWidgets() {
        for (List<BoundWidget> list : widgetCache.values()) {
            for (BoundWidget bw : list) bw.widget.visible = false;
        }
    }

    // ---------- scroll ----------

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double bx = X(BODY_X), by = Y(BODY_Y);
        if (mouseX >= bx && mouseX <= bx + BODY_W * k && mouseY >= by && mouseY <= by + BODY_H * k) {
            scroll -= verticalAmount * 24;
            double ms = maxScroll(layoutRows());
            if (scroll < 0) scroll = 0;
            if (scroll > ms) scroll = ms;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    // ---------- shared draw helpers ----------

    void rquad(GuiRenderer r, double x, double y, double w, double h, double rad, Color c) {
        if (w <= 0 || h <= 0) return;
        rad = Math.max(0, Math.min(rad, Math.min(w, h) / 2));
        if (rad <= 0.5) {
            r.quad(x, y, w, h, c);
            return;
        }
        r.quad(x + rad, y, w - 2 * rad, h, c);
        r.quad(x, y + rad, w, h - 2 * rad, c);
        double d = rad * 2;
        r.quad(x, y, d, d, GuiRenderer.CIRCLE, c);
        r.quad(x + w - d, y, d, d, GuiRenderer.CIRCLE, c);
        r.quad(x, y + h - d, d, d, GuiRenderer.CIRCLE, c);
        r.quad(x + w - d, y + h - d, d, d, GuiRenderer.CIRCLE, c);
    }

    static String fmt(double v) {
        if (v == Math.rint(v)) return String.valueOf((long) v);
        return String.valueOf(Math.round(v * 100) / 100.0);
    }

    static int indexOf(Object[] arr, Object o) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(o)) return i;
        }
        return 0;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void cycleEnum(EnumSetting<?> es) {
        try {
            EnumSetting raw = es;
            Object[] vals = ((Enum<?>) raw.get()).getDeclaringClass().getEnumConstants();
            int i = (indexOf(vals, raw.get()) + 1) % vals.length;
            raw.set(vals[i]);
        } catch (Throwable ignored) {}
    }

    // ---------- widgets ----------

    class V2Root extends WContainer {
        @Override
        protected void onCalculateWidgetPositions() {
            // Absolute positioning done by the screen; skip container flow.
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            PeachV2ModulesScreen s = PeachV2ModulesScreen.this;
            s.rquad(renderer, s.cardX, s.cardY, CARD_W * s.k, CARD_H * s.k, CARD_R * s.k, s.bgFill);
        }
    }

    class RailWidget extends WWidget {
        @Override
        protected void onCalculateSize() {
            width = RAIL_W;
            height = RAIL_H;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            PeachV2ModulesScreen s = PeachV2ModulesScreen.this;
            double kk = s.k;
            Color acc = PeachV2ModulesScreen.this.theme.accentColor.get();
            s.rquad(renderer, x, y, width, height, RAIL_R * kk, s.bgFill);

            double cx = x + width / 2;
            // Logo dot
            double logoY = y + 16 * kk;
            renderer.quad(cx - 8 * kk, logoY - 8 * kk, 16 * kk, 16 * kk, GuiRenderer.CIRCLE, acc);
            // Separator
            renderer.quad(x + 20 * kk, y + 56 * kk, 16 * kk, Math.max(1, kk), s.dim);
            // Category dots
            for (int i = 0; i < s.categories.size(); i++) {
                double dy = y + (73 + i * 40) * kk;
                boolean active = i == s.selected;
                Color c = active ? s.activeBlue : s.dim;
                renderer.quad(cx - 8 * kk, dy - 8 * kk, 16 * kk, 16 * kk, GuiRenderer.CIRCLE, c);
                if (active) {
                    renderer.quad(x + 6 * kk, dy - 8 * kk, 3 * kk, 16 * kk, s.activeBlue);
                }
            }
            // Avatar ring
            double avY = y + height - 28 * kk;
            renderer.quad(cx - 12 * kk, avY - 12 * kk, 24 * kk, 24 * kk, GuiRenderer.CIRCLE, acc);
            renderer.quad(cx - 10 * kk, avY - 10 * kk, 20 * kk, 20 * kk, GuiRenderer.CIRCLE, s.bgFill);
        }

        @Override
        public boolean onMouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (event.button() != 0) return false;
            PeachV2ModulesScreen s = PeachV2ModulesScreen.this;
            double kk = s.k;
            double cx = x + width / 2;
            double ex = event.x(), ey = event.y();
            for (int i = 0; i < s.categories.size(); i++) {
                double dy = y + (73 + i * 40) * kk;
                if (Math.abs(ex - cx) <= 12 * kk && Math.abs(ey - dy) <= 12 * kk) {
                    s.selectCategory(i);
                    return true;
                }
            }
            return false;
        }
    }

    class ModuleCard extends WWidget {
        final Module module;

        ModuleCard(Module module) {
            this.module = module;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            PeachV2ModulesScreen s = PeachV2ModulesScreen.this;
            double kk = s.k;
            Color acc = PeachV2ModulesScreen.this.theme.accentColor.get();
            Color text = PeachV2ModulesScreen.this.theme.textColor.get();
            boolean isEx = module == s.expanded;
            Color titleC = module.isActive() ? acc : text;

            // Card + outline
            s.rquad(renderer, x, y, width, height, ROW_R * kk, s.rowBg);
            double t = Math.max(1, kk);
            renderer.quad(x + 8 * kk, y, width - 16 * kk, t, s.rowLine);
            renderer.quad(x + 8 * kk, y + height - t, width - 16 * kk, t, s.rowLine);
            renderer.quad(x, y + 8 * kk, t, height - 16 * kk, s.rowLine);
            renderer.quad(x + width - t, y + 8 * kk, t, height - 16 * kk, s.rowLine);

            // Title
            renderer.text(trim(module.title), x + 14 * kk, y + 12 * kk, titleC, false);

            // Key box
            double keyX = x + 244 * kk, keyY = y + 12 * kk, keyS = 16 * kk;
            boolean bound = false;
            String keyName = "";
            try {
                bound = module.keybind.isSet();
                if (bound) keyName = module.keybind.toString();
            } catch (Throwable ignored) {}
            s.rquad(renderer, keyX, keyY, keyS, keyS, 4 * kk, bound ? s.keyOnBg : s.faint);
            if (!bound) {
                renderer.quad(keyX + 5 * kk, keyY + 7.5 * kk, 6 * kk, Math.max(1, kk), s.dim);
            } else if (keyName.length() <= 2) {
                renderer.text(keyName, keyX + 8 * kk - PeachV2ModulesScreen.this.theme.textWidth(keyName) / 2, keyY + 2 * kk, acc, false);
            }

            // Toggle pill
            double togX = x + 266 * kk, togY = y + 13 * kk, togW = 24 * kk, togH = 14 * kk;
            s.rquad(renderer, togX, togY, togW, togH, togH / 2, module.isActive() ? s.keyOnBg : s.faint);
            double knobX = togX + 2 * kk + (module.isActive() ? 10 * kk : 0);
            renderer.quad(knobX, togY + 2 * kk, 10 * kk, 10 * kk, GuiRenderer.CIRCLE, module.isActive() ? acc : s.dim);

            if (isEx) drawSettings(renderer, kk, acc, text);
        }

        private String trim(String title) {
            PeachV2GuiTheme th = PeachV2ModulesScreen.this.theme;
            if (th.textWidth(title) > 200) {
                while (title.length() > 4 && th.textWidth(title + "..") > 200) {
                    title = title.substring(0, title.length() - 1);
                }
                return title + "..";
            }
            return title;
        }

        private void drawSettings(GuiRenderer renderer, double kk, Color acc, Color text) {
            double sx = x + 14 * kk;
            double sw = 276 * kk;
            double sy = y + (ROW_H + 8) * kk;
            PeachV2GuiTheme th = PeachV2ModulesScreen.this.theme;

            for (Setting<?> st : flatSettings(module)) {
                try {
                    if (!st.isVisible()) continue;
                } catch (Throwable ignored) {}
                if (st instanceof BoolSetting) {
                    renderer.text(st.name, sx, sy, text, false);
                    sy += 22 * kk;
                } else if (st instanceof DoubleSetting ds) {
                    if (ds.noSlider) continue;
                    renderer.text(st.name, sx, sy, text, false);
                    String v = fmt(ds.get());
                    renderer.text(v, sx + sw - th.textWidth(v), sy, acc, false);
                    sy += 42 * kk;
                } else if (st instanceof IntSetting is) {
                    if (is.noSlider) continue;
                    renderer.text(st.name, sx, sy, text, false);
                    String v = String.valueOf(is.get());
                    renderer.text(v, sx + sw - th.textWidth(v), sy, acc, false);
                    sy += 42 * kk;
                } else if (st instanceof EnumSetting<?> es) {
                    renderer.text(st.name, sx, sy, text, false);
                    Object[] vals = ((Enum<?>) es.get()).getDeclaringClass().getEnumConstants();
                    String count = (indexOf(vals, es.get()) + 1) + "/" + vals.length;
                    renderer.text(count, sx + sw - th.textWidth(count), sy, acc, false);
                    PeachV2ModulesScreen.this.rquad(renderer, sx, sy + 16 * kk, sw, 30 * kk, 4 * kk,
                        PeachV2ModulesScreen.this.keyOnBg);
                    renderer.text(es.get().toString(), sx + 10 * kk, sy + 22 * kk, acc, false);
                    sy += 58 * kk;
                } else if (st instanceof StringSetting) {
                    renderer.text(st.name, sx, sy, text, false);
                    sy += 54 * kk;
                }
            }
        }

        @Override
        public boolean onMouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (event.button() != 0) return false;
            PeachV2ModulesScreen s = PeachV2ModulesScreen.this;
            double kk = s.k;
            double ex = event.x() - x, ey = event.y() - y;

            // Toggle pill (266,13 24x14 in design units)
            if (ex >= 262 * kk && ex <= 294 * kk && ey >= 9 * kk && ey <= 31 * kk) {
                module.toggle();
                return true;
            }

            // Enum cycle boxes
            if (module == s.expanded) {
                double sy = (ROW_H + 8) * kk;
                for (Setting<?> st : flatSettings(module)) {
                    try {
                        if (!st.isVisible()) continue;
                    } catch (Throwable ignored) {}
                    double adv;
                    if (st instanceof BoolSetting) adv = 22 * kk;
                    else if (st instanceof DoubleSetting ds) adv = ds.noSlider ? 0 : 42 * kk;
                    else if (st instanceof IntSetting is) adv = is.noSlider ? 0 : 42 * kk;
                    else if (st instanceof EnumSetting<?> es) {
                        double boxY = sy + 16 * kk;
                        if (ex >= 14 * kk && ex <= 290 * kk && ey >= boxY && ey <= boxY + 30 * kk) {
                            cycleEnum(es);
                            return true;
                        }
                        adv = 58 * kk;
                    } else if (st instanceof StringSetting) adv = 54 * kk;
                    else adv = 0;
                    sy += adv;
                }
            }

            s.toggleExpanded(module);
            return true;
        }
    }
}
