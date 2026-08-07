package xaeroplus.settings;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_304.class_11900;
import xaero.lib.common.gui.widget.TooltipInfo;
import xaeroplus.XaeroPlus;
import xaeroplus.feature.extensions.IXaeroPlusSettingEntry;
import xaeroplus.feature.extensions.XaeroPlusCustomSettingEntry;

public class BooleanSetting extends XaeroPlusSetting {
   public static final class_304.class_11900 KEYBIND_CATEGORY = class_11900.method_74698(class_2960.method_60655("xaeroplus", "keybindings"));
   private boolean value;
   private BooleanConsumer settingChangeConsumer;

   private BooleanSetting(final String settingName, final String settingNameTranslationKey, final String tooltipTranslationKey, final class_304 keyBinding, final boolean value, final BooleanConsumer settingChangeConsumer, final BooleanSupplier visibilitySupplier) {
      super(settingName, settingNameTranslationKey, tooltipTranslationKey, keyBinding, visibilitySupplier);
      this.value = value;
      this.settingChangeConsumer = settingChangeConsumer;
   }

   public static BooleanSetting create(String settingName, String settingNameTranslationKey, boolean defaultValue) {
      return create(settingName, settingNameTranslationKey, defaultValue, false);
   }

   public static BooleanSetting create(String settingName, String settingNameTranslationKey, boolean defaultValue, boolean keybind) {
      return new BooleanSetting("[XP] " + settingName, settingNameTranslationKey, buildTooltipTranslationKey(settingNameTranslationKey), keybind ? new class_304(settingNameTranslationKey, -1, KEYBIND_CATEGORY) : null, defaultValue, (BooleanConsumer)null, (BooleanSupplier)null);
   }

   public static BooleanSetting create(String settingName, String settingNameTranslationKey, boolean defaultValue, BooleanConsumer settingChangeConsumer) {
      return create(settingName, settingNameTranslationKey, defaultValue, false, settingChangeConsumer);
   }

   public static BooleanSetting create(String settingName, String settingNameTranslationKey, boolean defaultValue, boolean keybind, BooleanConsumer settingChangeConsumer) {
      return new BooleanSetting("[XP] " + settingName, settingNameTranslationKey, buildTooltipTranslationKey(settingNameTranslationKey), keybind ? new class_304(settingNameTranslationKey, -1, KEYBIND_CATEGORY) : null, defaultValue, settingChangeConsumer, (BooleanSupplier)null);
   }

   public static BooleanSetting create(String settingName, String settingNameTranslationKey, boolean defaultValue, BooleanSupplier visibilitySupplier) {
      return create(settingName, settingNameTranslationKey, defaultValue, false, visibilitySupplier);
   }

   public static BooleanSetting create(String settingName, String settingNameTranslationKey, boolean defaultValue, boolean keybind, BooleanSupplier visibilitySupplier) {
      return new BooleanSetting("[XP] " + settingName, settingNameTranslationKey, buildTooltipTranslationKey(settingNameTranslationKey), keybind ? new class_304(settingNameTranslationKey, -1, KEYBIND_CATEGORY) : null, defaultValue, (BooleanConsumer)null, visibilitySupplier);
   }

   public static BooleanSetting create(String settingName, String settingNameTranslationKey, boolean defaultValue, BooleanConsumer settingChangeConsumer, BooleanSupplier visibilitySupplier) {
      return create(settingName, settingNameTranslationKey, defaultValue, false, settingChangeConsumer, visibilitySupplier);
   }

   public static BooleanSetting create(String settingName, String settingNameTranslationKey, boolean defaultValue, boolean keybind, BooleanConsumer settingChangeConsumer, BooleanSupplier visibilitySupplier) {
      return new BooleanSetting("[XP] " + settingName, settingNameTranslationKey, buildTooltipTranslationKey(settingNameTranslationKey), keybind ? new class_304(settingNameTranslationKey, -1, KEYBIND_CATEGORY) : null, defaultValue, settingChangeConsumer, visibilitySupplier);
   }

   public String getSerializedValue() {
      return Boolean.toString(this.value);
   }

   public void deserializeValue(String value) {
      boolean v = Boolean.parseBoolean(value);
      if (v != this.get()) {
         this.setValue(v);
      }

   }

   public IXaeroPlusSettingEntry toXaeroSettingEntry() {
      return new XaeroPlusCustomSettingEntry(this, class_2561.method_43470(this.getTranslatedName()), new TooltipInfo(this.getTooltipTranslationKey()), false, this::get, 0, 1, (v) -> v == 1, (v) -> class_2561.method_43471(v ? "gui.xaero_on" : "gui.xaero_off"), (v1, v2) -> {
         this.setValue(v2);
         SettingHooks.saveSettings();
         class_310.method_1551().method_1507(class_310.method_1551().field_1755);
      }, this::isVisible);
   }

   public boolean get() {
      return this.value;
   }

   public void setValue(final boolean value) {
      this.value = value;
      if (Objects.nonNull(this.getSettingChangeConsumer())) {
         try {
            this.getSettingChangeConsumer().accept(value);
         } catch (Exception e) {
            XaeroPlus.LOGGER.warn("Error applying setting change consumer for {}", this.getSettingName(), e);
         }
      }

   }

   public Consumer<Boolean> getSettingChangeConsumer() {
      return this.settingChangeConsumer;
   }

   public void setSettingChangeConsumer(final BooleanConsumer settingChangeConsumer) {
      this.settingChangeConsumer = settingChangeConsumer;
   }

   public void init() {
      if (Objects.nonNull(this.settingChangeConsumer)) {
         this.settingChangeConsumer.accept(this.value);
      }

   }
}
