package net.aero.mapintegration.gui;

import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import xaero.lib.client.gui.ISettingEntry;

import java.util.function.Consumer;

public class ToggleEntry implements ISettingEntry {
    private final String label;
    private final boolean defaultValue;
    private final Consumer<Boolean> setter;
    private boolean currentValue;

    public ToggleEntry(String label, boolean defaultValue, Consumer<Boolean> setter) {
        this.label = label;
        this.defaultValue = defaultValue;
        this.setter = setter;
        this.currentValue = defaultValue;
    }

    @Override
    public ClickableWidget createWidget(int x, int y, int w) {
        currentValue = defaultValue;
        String display = label + ": " + (currentValue ? "ON" : "OFF");
        return net.minecraft.client.gui.widget.ButtonWidget.builder(
            Text.literal(display),
            b -> {
                currentValue = !currentValue;
                b.setMessage(Text.literal(label + ": " + (currentValue ? "ON" : "OFF")));
                setter.accept(currentValue);
            }
        ).dimensions(x, y, w, 20).build();
    }

    @Override
    public String getStringForSearch() {
        return label.toLowerCase();
    }
}
