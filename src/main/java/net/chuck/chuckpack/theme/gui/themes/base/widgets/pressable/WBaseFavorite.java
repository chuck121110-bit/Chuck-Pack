package net.chuck.chuckpack.theme.gui.themes.base.widgets.pressable;

import net.chuck.chuckpack.theme.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.gui.widgets.pressable.WFavorite;
import meteordevelopment.meteorclient.utils.render.color.Color;

public class WBaseFavorite extends WFavorite implements BaseWidget {
    public WBaseFavorite(boolean checked) {
        super(checked);
    }

    @Override
    protected Color getColor() {
        return theme().favoriteColor.get();
    }
}
