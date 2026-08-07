package xaeroplus.settings;

import java.util.function.BooleanSupplier;
import net.minecraft.class_1074;
import net.minecraft.class_304;
import xaeroplus.feature.extensions.IXaeroPlusSettingEntry;

public abstract class XaeroPlusSetting {
   public static final String SETTING_PREFIX = "[XP] ";
   private final String settingName;
   private final String settingNameTranslationKey;
   private String tooltipTranslationKey;
   private static boolean ingameOnly = false;
   private static boolean requiresMinimap = false;
   private class_304 keyBinding;
   private BooleanSupplier visibilitySupplier;

   public XaeroPlusSetting(String settingName, String settingNameTranslationKey, String tooltipTranslationKey, class_304 keyBinding, BooleanSupplier visibilitySupplier) {
      this.settingName = settingName;
      this.settingNameTranslationKey = settingNameTranslationKey;
      this.tooltipTranslationKey = tooltipTranslationKey;
      this.keyBinding = keyBinding;
      this.visibilitySupplier = visibilitySupplier;
   }

   public abstract void init();

   public abstract String getSerializedValue();

   public abstract void deserializeValue(String value);

   public abstract IXaeroPlusSettingEntry toXaeroSettingEntry();

   public String getSettingName() {
      return this.settingName;
   }

   public String getSettingNameTranslationKey() {
      return this.settingNameTranslationKey;
   }

   public String getTranslatedName() {
      String var10000 = this.getSettingNameTranslationKey();
      return "[XP] " + class_1074.method_4662(var10000, new Object[0]);
   }

   public String getTooltipTranslationKey() {
      return this.tooltipTranslationKey;
   }

   public static String buildTooltipTranslationKey(String baseKey) {
      return baseKey + ".tooltip";
   }

   public boolean isIngameOnly() {
      return ingameOnly;
   }

   public boolean isRequiresMinimap() {
      return requiresMinimap;
   }

   public class_304 getKeyBinding() {
      return this.keyBinding;
   }

   public void setKeyBinding(class_304 keyBinding) {
      this.keyBinding = keyBinding;
   }

   public boolean isVisible() {
      return this.visibilitySupplier != null ? this.visibilitySupplier.getAsBoolean() : true;
   }
}
