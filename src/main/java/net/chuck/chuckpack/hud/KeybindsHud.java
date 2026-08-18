package net.chuck.chuckpack.hud;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KeybindsHud extends HudElement {
    public static final HudGroup AERO_GROUP = new HudGroup("Chuck Pack");
    public static final HudElementInfo<KeybindsHud> INFO = new HudElementInfo<>(AERO_GROUP, "keybinds", "Shows all modules with keybinds and their toggle state.", KeybindsHud::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgColors = settings.createGroup("Colors");
    private final SettingGroup sgScale = settings.createGroup("Scale");
    private final SettingGroup sgBackground = settings.createGroup("Background");

    // General

    private final Setting<SortMode> sortMode = sgGeneral.add(new EnumSetting.Builder<SortMode>()
            .name("sort-mode")
            .description("How to sort the keybind list.")
            .defaultValue(SortMode.Alphabetical)
            .build()
    );

    private final Setting<Boolean> sortReverse = sgGeneral.add(new BoolSetting.Builder()
            .name("sort-reverse")
            .description("Reverse the sort order.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> showKeybind = sgGeneral.add(new BoolSetting.Builder()
            .name("show-keybind")
            .description("Show the keybind next to the module name.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
            .name("shadow")
            .description("Renders shadow behind Component.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> outlines = sgGeneral.add(new BoolSetting.Builder()
            .name("outlines")
            .description("Whether or not to render outlines.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Integer> outlineWidth = sgGeneral.add(new IntSetting.Builder()
            .name("outline-width")
            .description("Outline width.")
            .defaultValue(2)
            .min(1)
            .sliderMin(1)
            .visible(outlines::get)
            .build()
    );

    private final Setting<SettingColor> outlineColor = sgGeneral.add(new ColorSetting.Builder()
            .name("outline-color")
            .description("Color of the outline.")
            .visible(outlines::get)
            .defaultValue(new SettingColor(255, 255, 255, 255))
            .build()
    );

    private final Setting<Alignment> alignment = sgGeneral.add(new EnumSetting.Builder<Alignment>()
            .name("alignment")
            .description("Horizontal alignment.")
            .defaultValue(Alignment.Auto)
            .build()
    );

    // Colors

    private final Setting<SettingColor> enabledColor = sgColors.add(new ColorSetting.Builder()
            .name("enabled-color")
            .description("Color for enabled modules.")
            .defaultValue(new SettingColor(255, 255, 255))
            .build()
    );

    private final Setting<SettingColor> disabledColor = sgColors.add(new ColorSetting.Builder()
            .name("disabled-color")
            .description("Color for disabled modules.")
            .defaultValue(new SettingColor(128, 128, 128))
            .build()
    );

    private final Setting<Boolean> colorKeybind = sgColors.add(new BoolSetting.Builder()
            .name("color-keybind")
            .description("Color the keybind Component separately.")
            .defaultValue(false)
            .build()
    );

    private final Setting<SettingColor> keybindColor = sgColors.add(new ColorSetting.Builder()
            .name("keybind-color")
            .description("Color for keybind Component when color-keybind is enabled.")
            .defaultValue(new SettingColor(175, 175, 175))
            .visible(colorKeybind::get)
            .build()
    );

    // Scale

    private final Setting<Boolean> customScale = sgScale.add(new BoolSetting.Builder()
            .name("custom-scale")
            .description("Applies a custom scale to this HUD element.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Double> scale = sgScale.add(new DoubleSetting.Builder()
            .name("scale")
            .description("Custom scale.")
            .visible(customScale::get)
            .defaultValue(1)
            .min(0.5)
            .sliderRange(0.5, 3)
            .build()
    );

    // Background

    private final Setting<Boolean> background = sgBackground.add(new BoolSetting.Builder()
            .name("background")
            .description("Displays background behind each line.")
            .defaultValue(false)
            .build()
    );

    private final Setting<SettingColor> backgroundColor = sgBackground.add(new ColorSetting.Builder()
            .name("background-color")
            .description("Color used for the background.")
            .visible(background::get)
            .defaultValue(new SettingColor(25, 25, 25, 50))
            .build()
    );

    private final List<Module> modules = new ArrayList<>();

    private double lastX;
    private double prevTextLength;

    public KeybindsHud() {
        super(INFO);
    }

    @Override
    public void tick(HudRenderer renderer) {
        modules.clear();

        for (Module module : Modules.get().getAll()) {
            if (module.keybind.isSet()) {
                modules.add(module);
            }
        }

        if (modules.isEmpty()) {
            if (isInEditor()) {
                setSize(renderer.textWidth("Keybinds", shadow.get(), getScale()), renderer.textHeight(shadow.get(), getScale()));
            }
            return;
        }

        Comparator<Module> comparator = switch (sortMode.get()) {
            case Alphabetical -> Comparator.comparing(m -> m.title);
            case TextWidth -> Comparator.comparingDouble(m -> getModuleWidth(renderer, m));
            case KeyLength -> Comparator.comparingInt(m -> m.keybind.toString().length());
            case ToggledFirst -> Comparator.<Module, Boolean>comparing(m -> !m.isActive())
                    .thenComparing(m -> m.title);
        };

        if (sortReverse.get()) {
            comparator = comparator.reversed();
        }

        modules.sort(comparator);

        double width = 0;
        double height = 0;

        for (Module module : modules) {
            width = Math.max(width, getModuleWidth(renderer, module));
            height += renderer.textHeight(shadow.get(), getScale());
        }

        setSize(width, height);
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = this.x;
        double y = this.y;

        if (modules.isEmpty()) {
            if (isInEditor()) {
                renderer.text("Keybinds", x, y, enabledColor.get(), shadow.get(), getScale());
            }
            return;
        }

        lastX = x;

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            Color color = module.isActive() ? enabledColor.get() : disabledColor.get();

            double offset = alignX(getModuleWidth(renderer, module), alignment.get());
            double lineX = x + offset;

            renderer.text(module.title, lineX, y, color, shadow.get(), getScale());

            double textHeight = renderer.textHeight(shadow.get(), getScale());
            double textLength = renderer.textWidth(module.title, shadow.get(), getScale());

            if (showKeybind.get()) {
                String keybindStr = " [" + module.keybind + "]";
                Color kbColor = colorKeybind.get() ? keybindColor.get() : color;
                renderer.text(keybindStr, lineX + textLength, y, kbColor, shadow.get(), getScale());
                textLength += renderer.textWidth(keybindStr, shadow.get(), getScale());
            }

            double lineStartY = y;
            double lineHeight = textHeight;

            if (outlines.get()) {
                Color olc = outlineColor.get();

                if (i == 0) {
                    lineStartY -= 2;
                    lineHeight += 2;

                    renderer.quad(lineX - 2 - outlineWidth.get(), lineStartY - outlineWidth.get(),
                        textLength + 4 + 2 * outlineWidth.get(),
                        outlineWidth.get(), olc);
                } else {
                    renderer.quad(Math.min(lastX, lineX) - 2 - outlineWidth.get(), Math.max(lastX, lineX) == lineX ? y : y - outlineWidth.get(),
                        (Math.max(lastX, lineX) - 2) - (Math.min(lastX, lineX) - 2 - outlineWidth.get()), outlineWidth.get(),
                        olc);

                    renderer.quad(Math.min(lastX + prevTextLength, lineX + textLength) + 2, Math.min(lastX + prevTextLength, lineX + textLength) == lineX + textLength ? y : y - outlineWidth.get(),
                        (Math.max(lastX + prevTextLength, lineX + textLength) + 2 + outlineWidth.get()) - (Math.min(lastX + prevTextLength, lineX + textLength) + 2), outlineWidth.get(),
                        olc);
                }

                if (i == modules.size() - 1) {
                    lineHeight += 2;

                    renderer.quad(lineX - 2 - outlineWidth.get(), lineStartY + lineHeight,
                        textLength + 4 + 2 * outlineWidth.get(), outlineWidth.get(),
                        olc);
                }

                renderer.quad(lineX - 2 - outlineWidth.get(), lineStartY, outlineWidth.get(), lineHeight,
                    olc);

                renderer.quad(lineX + textLength + 2, lineStartY, outlineWidth.get(), lineHeight,
                    olc);
            }

            if (background.get()) {
                renderer.quad(lineX - 2, lineStartY, textLength + 4, lineHeight, backgroundColor.get());
            }

            lastX = lineX;
            prevTextLength = textLength;
            y += textHeight;
        }
    }

    private double getModuleWidth(HudRenderer renderer, Module module) {
        double width = renderer.textWidth(module.title, shadow.get(), getScale());

        if (showKeybind.get()) {
            width += renderer.textWidth(" [" + module.keybind + "]", shadow.get(), getScale());
        }

        return width;
    }

    private double getScale() {
        return customScale.get() ? scale.get() : Hud.get().getTextScale();
    }

    public enum SortMode {
        Alphabetical,
        TextWidth,
        KeyLength,
        ToggledFirst
    }
}
