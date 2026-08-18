package net.chuck.chuckpack.util.config;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import net.chuck.chuckpack.ChuckPack;

public class ChuckPackConfigModifier {
    private static ChuckPackConfigModifier INSTANCE;

    public static final SettingGroup sgChuckPack = Config.get().settings.createGroup("Chuck Pack");

    public final Setting<Boolean> separateCategory = sgChuckPack.add(new BoolSetting.Builder()
        .name("separate-category")
        .description("Moves all Chuck Pack modules into their own 'Chuck Pack' category instead of their natural categories.")
        .defaultValue(false)
        .onChanged(ChuckPackConfigModifier::onSeparateCategoryChanged)
        .build()
    );

    public static ChuckPackConfigModifier get() {
        if (INSTANCE == null) INSTANCE = new ChuckPackConfigModifier();
        return INSTANCE;
    }

    public ChuckPackConfigModifier() {
        boolean saved = CategoryConfig.isSeparateCategory();
        if (saved != separateCategory.get()) {
            separateCategory.set(saved);
        }
    }

    private static void onSeparateCategoryChanged(boolean value) {
        CategoryConfig.setSeparateCategory(value);
        ChuckPack.setSeparateCategory(value);
    }
}
