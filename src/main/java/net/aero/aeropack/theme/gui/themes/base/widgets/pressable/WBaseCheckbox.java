package net.aero.aeropack.theme.gui.themes.base.widgets.pressable;

import net.aero.aeropack.theme.gui.themes.base.BaseWidget;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import net.minecraft.util.Mth;

public class WBaseCheckbox extends WCheckbox implements BaseWidget {
    private double animProgress;

    public WBaseCheckbox(boolean checked) {
        super(checked);
        animProgress = checked ? 1 : 0;
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        animProgress += (checked ? 1 : -1) * delta * 14;
        animProgress = Mth.clamp(animProgress, 0, 1);

        renderBackground(renderer, this, pressed, mouseOver);

        if (animProgress > 0) {
            double cs = (width - theme().scale(2)) / 1.75 * animProgress;
            renderer.quad(x + (width - cs) / 2, y + (height - cs) / 2, cs, cs, theme().checkboxColor.get());
        }
    }
}
