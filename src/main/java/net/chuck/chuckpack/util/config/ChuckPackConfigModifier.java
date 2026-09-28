package net.chuck.chuckpack.util.config;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
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

    public final Setting<Boolean> checkForUpdates = sgChuckPack.add(new BoolSetting.Builder()
        .name("check-for-updates")
        .description("Check for Chuck Pack updates from GitHub on Minecraft startup.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> autoDownloadUpdates = sgChuckPack.add(new BoolSetting.Builder()
        .name("auto-download-updates")
        .description("Automatically download and install updates when found (requires restart).")
        .defaultValue(true)
        .build()
    );

    public final Setting<String> githubToken = sgChuckPack.add(new StringSetting.Builder()
        .name("github-token")
        .description("Fine-grained GitHub personal access token (read-only) for the private Chuck-Pack repo. Required for the auto-updater to see releases.")
        .defaultValue("")
        .build()
    );

    public final Setting<Boolean> debugLogging = sgChuckPack.add(new BoolSetting.Builder()
        .name("debug-logging")
        .description("Master switch for debug logging (auto-connect, auto-updater, swarm) — shows detailed logs to help find issues.")
        .defaultValue(false)
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
