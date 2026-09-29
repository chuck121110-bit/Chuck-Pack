package net.chuck.chuckpack.theme.gui.themes.base;

import meteordevelopment.meteorclient.utils.render.color.SettingColor;

/**
 * Glass theme — palette ported from the ImGui portfolio template (night).
 * Translucent black glass panels, indigo accent (#615DCE), borderless.
 */
public class GlassGuiTheme extends BaseGuiTheme {
    private static final Palette GLASS = new Palette();

    static {
        GLASS.accent = new SettingColor(97, 93, 206);
        GLASS.checkbox = new SettingColor(97, 93, 206);
        GLASS.plus = new SettingColor(50, 255, 50);
        GLASS.minus = new SettingColor(255, 112, 112);
        GLASS.favorite = new SettingColor(250, 215, 0);
        GLASS.text = new SettingColor(255, 255, 255);
        GLASS.textSecondary = new SettingColor(255, 255, 255, 153);
        GLASS.textHighlight = new SettingColor(97, 93, 206, 100);
        GLASS.titleText = new SettingColor(255, 255, 255);
        GLASS.loggedIn = new SettingColor(89, 199, 122);
        GLASS.placeholder = new SettingColor(255, 255, 255, 60);
        GLASS.bgNormal = new SettingColor(0, 0, 0, 128);
        GLASS.bgHovered = new SettingColor(0, 0, 0, 153);
        GLASS.bgPressed = new SettingColor(0, 0, 0, 179);
        GLASS.outlineNormal = new SettingColor(0, 0, 0, 64);
        GLASS.outlineHovered = new SettingColor(0, 0, 0, 64);
        GLASS.outlinePressed = new SettingColor(97, 93, 206, 115);
        GLASS.windowOutline = new SettingColor(0, 0, 0, 77);
        GLASS.windowOutlineThickness = 0;
        GLASS.separatorText = new SettingColor(255, 255, 255, 66);
        GLASS.separatorCenter = new SettingColor(255, 255, 255, 80);
        GLASS.separatorEdges = new SettingColor(255, 255, 255, 43);
        GLASS.scrollbarNormal = new SettingColor(0, 0, 0, 128);
        GLASS.scrollbarHovered = new SettingColor(0, 0, 0, 179);
        GLASS.scrollbarPressed = new SettingColor(37, 37, 37, 128);
        GLASS.sliderHandleNormal = new SettingColor(134, 134, 134);
        GLASS.sliderHandleHovered = new SettingColor(150, 150, 150);
        GLASS.sliderHandlePressed = new SettingColor(170, 170, 170);
        GLASS.sliderLeft = new SettingColor(134, 134, 134);
        GLASS.sliderRight = new SettingColor(43, 43, 43);
        GLASS.moduleHovered = new SettingColor(37, 37, 37, 128);
        GLASS.moduleInactive = new SettingColor(0, 0, 0, 102);
        GLASS.moduleActive = new SettingColor(37, 37, 37, 180);
        GLASS.moduleInactiveGradient = new SettingColor(0, 0, 0, 0);
        GLASS.moduleActiveGradient = new SettingColor(97, 93, 206, 70);
    }

    public GlassGuiTheme() {
        super("Glass");
    }

    @Override
    protected Palette palette() {
        return GLASS;
    }
}
