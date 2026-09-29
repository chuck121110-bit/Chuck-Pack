package net.chuck.chuckpack.theme.gui.themes.base;

import meteordevelopment.meteorclient.utils.render.color.SettingColor;

/**
 * Peach theme — palette ported from the LazyUI/ImGui overlay menu template.
 * Dark plum backgrounds (14,10,25), peach accent (255,150,118), pill controls.
 */
public class PeachGuiTheme extends BaseGuiTheme {
    static final Palette PEACH = new Palette();

    static {
        PEACH.accent = new SettingColor(255, 150, 118);
        PEACH.checkbox = new SettingColor(255, 150, 118);
        PEACH.plus = new SettingColor(50, 255, 50);
        PEACH.minus = new SettingColor(255, 50, 50);
        PEACH.favorite = new SettingColor(250, 215, 0);
        PEACH.text = new SettingColor(255, 255, 255);
        PEACH.textSecondary = new SettingColor(255, 255, 255, 61);
        PEACH.textHighlight = new SettingColor(255, 150, 118, 100);
        PEACH.titleText = new SettingColor(255, 255, 255);
        PEACH.loggedIn = new SettingColor(45, 225, 45);
        PEACH.placeholder = new SettingColor(255, 255, 255, 20);
        PEACH.bgNormal = new SettingColor(14, 10, 25, 184);
        PEACH.bgHovered = new SettingColor(20, 14, 36, 190);
        PEACH.bgPressed = new SettingColor(26, 18, 46, 200);
        PEACH.outlineNormal = new SettingColor(255, 255, 255, 10);
        PEACH.outlineHovered = new SettingColor(255, 255, 255, 15);
        PEACH.outlinePressed = new SettingColor(255, 150, 118, 41);
        PEACH.windowOutline = new SettingColor(255, 150, 118);
        PEACH.windowOutlineThickness = 1;
        PEACH.separatorText = new SettingColor(255, 255, 255, 122);
        PEACH.separatorCenter = new SettingColor(255, 150, 118, 180);
        PEACH.separatorEdges = new SettingColor(255, 255, 255, 60);
        PEACH.scrollbarNormal = new SettingColor(14, 10, 25, 200);
        PEACH.scrollbarHovered = new SettingColor(24, 18, 44, 200);
        PEACH.scrollbarPressed = new SettingColor(34, 26, 60, 200);
        PEACH.sliderHandleNormal = new SettingColor(255, 150, 118);
        PEACH.sliderHandleHovered = new SettingColor(255, 164, 137);
        PEACH.sliderHandlePressed = new SettingColor(255, 173, 148);
        PEACH.sliderLeft = new SettingColor(255, 164, 137);
        PEACH.sliderRight = new SettingColor(255, 255, 255, 10);
        PEACH.moduleHovered = new SettingColor(255, 150, 118, 41);
        PEACH.moduleInactive = new SettingColor(255, 255, 255, 5);
        PEACH.moduleActive = new SettingColor(255, 150, 118, 25);
        PEACH.moduleInactiveGradient = new SettingColor(255, 255, 255, 0);
        PEACH.moduleActiveGradient = new SettingColor(255, 150, 118, 70);
    }

    public PeachGuiTheme() {
        super("Peach V1");
    }

    protected PeachGuiTheme(String name) {
        super(name);
    }

    @Override
    protected Palette palette() {
        return PEACH;
    }
}
