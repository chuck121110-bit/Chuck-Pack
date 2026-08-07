package net.aero.aeropack.util.config;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import net.aero.aeropack.AeroPack;

public class AeroPackConfigModifier {
    private static AeroPackConfigModifier INSTANCE;

    public static final SettingGroup sgAeroPack = Config.get().settings.createGroup("Aero Pack");

    public final Setting<Boolean> separateCategory = sgAeroPack.add(new BoolSetting.Builder()
        .name("separate-category")
        .description("Moves all Aero Pack modules into their own 'Aero Pack' category instead of their natural categories.")
        .defaultValue(false)
        .onChanged(AeroPackConfigModifier::onSeparateCategoryChanged)
        .build()
    );

    public static AeroPackConfigModifier get() {
        if (INSTANCE == null) INSTANCE = new AeroPackConfigModifier();
        return INSTANCE;
    }

    public AeroPackConfigModifier() {
        boolean saved = CategoryConfig.isSeparateCategory();
        if (saved != separateCategory.get()) {
            separateCategory.set(saved);
        }
    }

    private static void onSeparateCategoryChanged(boolean value) {
        CategoryConfig.setSeparateCategory(value);
        AeroPack.setSeparateCategory(value);
    }
}
