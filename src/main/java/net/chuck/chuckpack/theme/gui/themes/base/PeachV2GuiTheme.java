package net.chuck.chuckpack.theme.gui.themes.base;

import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import net.chuck.chuckpack.theme.gui.screens.PeachV2ModulesScreen;
import net.minecraft.client.gui.screens.Screen;

/**
 * Peach V2 — true 1:1 port of the LazyUI overlay template:
 * icon rail sidebar, two-column module cards, pill toggles, inline settings.
 */
public class PeachV2GuiTheme extends PeachGuiTheme {
    public PeachV2GuiTheme() {
        super("Peach V2");
    }

    @Override
    protected Palette palette() {
        return PEACH;
    }

    @Override
    public TabScreen modulesScreen() {
        return new PeachV2ModulesScreen(this);
    }

    @Override
    public boolean isModulesScreen(Screen screen) {
        return screen instanceof PeachV2ModulesScreen;
    }
}
