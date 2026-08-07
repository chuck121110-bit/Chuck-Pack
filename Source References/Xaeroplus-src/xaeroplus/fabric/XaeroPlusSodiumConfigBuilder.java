package xaeroplus.fabric;

import java.util.Objects;
import java.util.function.Consumer;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.structure.BooleanOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.IntegerOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ModOptionsBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionPageBuilder;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import xaeroplus.settings.BooleanSetting;
import xaeroplus.settings.DoubleSetting;
import xaeroplus.settings.SettingHooks;
import xaeroplus.settings.Settings;

public class XaeroPlusSodiumConfigBuilder implements ConfigEntryPoint {
   public void registerConfigLate(final ConfigBuilder builder) {
      if (Settings.REGISTRY.sodiumSettingIntegration.get()) {
         ModOptionsBuilder var10000 = builder.registerModOptions("xaeroplus");
         OptionPageBuilder var10001 = builder.createOptionPage().setName(class_2561.method_43470("XaeroPlus"));
         OptionGroupBuilder var10002 = builder.createOptionGroup().setName(class_2561.method_43470("XaeroPlus"));
         BooleanOptionBuilder var10003 = builder.createBooleanOption(class_2960.method_60655("xaeroplus", "minimap_fps_limiter")).setName(class_2561.method_43471("xaeroplus.setting.fps_limiter")).setTooltip(class_2561.method_43471("xaeroplus.setting.fps_limiter.tooltip"));
         BooleanSetting var10004 = Settings.REGISTRY.minimapFpsLimiter;
         Objects.requireNonNull(var10004);
         Consumer var6 = var10004::setValue;
         BooleanSetting var10005 = Settings.REGISTRY.minimapFpsLimiter;
         Objects.requireNonNull(var10005);
         var10002 = var10002.addOption(var10003.setBinding(var6, var10005::get).setStorageHandler(SettingHooks::saveSettings).setDefaultValue(false));
         IntegerOptionBuilder var4 = builder.createIntegerOption(class_2960.method_60655("xaeroplus", "minimap_fps_limit")).setName(class_2561.method_43471("xaeroplus.setting.fps_limiter_limit")).setTooltip(class_2561.method_43471("xaeroplus.setting.fps_limiter_limit.tooltip")).setRange((int)Settings.REGISTRY.minimapFpsLimit.getValueMin(), (int)Settings.REGISTRY.minimapFpsLimit.getValueMax(), (int)Settings.REGISTRY.minimapFpsLimit.getValueStep());
         DoubleSetting var7 = Settings.REGISTRY.minimapFpsLimit;
         Objects.requireNonNull(var7);
         Consumer var8 = var7::setValue;
         DoubleSetting var11 = Settings.REGISTRY.minimapFpsLimit;
         Objects.requireNonNull(var11);
         var10002 = var10002.addOption(var4.setBinding(var8, var11::getAsInt).setStorageHandler(SettingHooks::saveSettings).setValueFormatter((i) -> class_2561.method_43470(String.valueOf(i))).setDefaultValue(60));
         var4 = builder.createIntegerOption(class_2960.method_60655("xaeroplus", "minimap_scaling")).setName(class_2561.method_43471("xaeroplus.setting.minimap_scaling")).setTooltip(class_2561.method_43471("xaeroplus.setting.minimap_scaling.tooltip")).setRange((int)Settings.REGISTRY.minimapScaleMultiplierSetting.getValueMin(), (int)Settings.REGISTRY.minimapScaleMultiplierSetting.getValueMax(), (int)Settings.REGISTRY.minimapScaleMultiplierSetting.getValueStep());
         DoubleSetting var9 = Settings.REGISTRY.minimapScaleMultiplierSetting;
         Objects.requireNonNull(var9);
         Consumer var10 = var9::setValue;
         var11 = Settings.REGISTRY.minimapScaleMultiplierSetting;
         Objects.requireNonNull(var11);
         var10000.addPage(var10001.addOptionGroup(var10002.addOption(var4.setBinding(var10, var11::getAsInt).setStorageHandler(SettingHooks::saveSettings).setValueFormatter((i) -> class_2561.method_43470(String.valueOf(i))).setDefaultValue(1))));
      }
   }
}
