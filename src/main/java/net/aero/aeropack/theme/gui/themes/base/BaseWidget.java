package net.aero.aeropack.theme.gui.themes.base;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.utils.render.color.Color;

public interface BaseWidget extends meteordevelopment.meteorclient.gui.utils.BaseWidget {
    default BaseGuiTheme theme() {
        return (BaseGuiTheme) getTheme();
    }

    default void renderBackground(GuiRenderer renderer, WWidget widget, Color outlineColor, Color backgroundColor) {
        BaseGuiTheme theme = theme();
        double s = theme.scale(2);

        renderer.quad(widget.x + s, widget.y + s, widget.width - s * 2, widget.height - s * 2, backgroundColor);

        if (outlineColor != null) {
            renderer.quad(widget.x, widget.y, widget.width, s, outlineColor);
            renderer.quad(widget.x, widget.y + widget.height - s, widget.width, s, outlineColor);
            renderer.quad(widget.x, widget.y + s, s, widget.height - s * 2, outlineColor);
            renderer.quad(widget.x + widget.width - s, widget.y + s, s, widget.height - s * 2, outlineColor);
        }
    }

    default void renderBackground(GuiRenderer renderer, WWidget widget, boolean pressed, boolean mouseOver) {
        BaseGuiTheme theme = theme();
        renderBackground(renderer, widget, theme.outlineColor.get(pressed, mouseOver), theme.backgroundColor.get(pressed, mouseOver));
    }
}
