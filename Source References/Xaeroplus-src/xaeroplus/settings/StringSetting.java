package xaeroplus.settings;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.class_304;
import net.minecraft.class_437;
import xaeroplus.XaeroPlus;
import xaeroplus.feature.extensions.IXaeroPlusSettingEntry;
import xaeroplus.feature.extensions.XaeroPlusScreenSwitchSettingEntry;

public class StringSetting extends XaeroPlusSetting {
   private String value;
   private Consumer<String> settingChangeConsumer;
   private ScreenSupplier screenSupplier;

   public StringSetting(final String settingName, final String settingNameTranslationKey, final String tooltipTranslationKey, final String defaultValue, final Consumer<String> settingChangeConsumer, final BooleanSupplier visibilitySupplier, final ScreenSupplier screenSupplier) {
      super(settingName, settingNameTranslationKey, tooltipTranslationKey, (class_304)null, visibilitySupplier);
      this.value = defaultValue;
      this.settingChangeConsumer = settingChangeConsumer;
      this.screenSupplier = screenSupplier;
   }

   public static StringSetting create(String settingName, String settingNameTranslationKey, String defaultValue, Consumer<String> settingChangeConsumer, ScreenSupplier screenSupplier, BooleanSupplier visibilitySupplier) {
      return new StringSetting("[XP] " + settingName, settingNameTranslationKey, buildTooltipTranslationKey(settingNameTranslationKey), defaultValue, settingChangeConsumer, visibilitySupplier, screenSupplier);
   }

   public static StringSetting create(String settingName, String settingNameTranslationKey, String defaultValue, Consumer<String> settingChangeConsumer, ScreenSupplier screenSupplier) {
      return create(settingName, settingNameTranslationKey, defaultValue, settingChangeConsumer, screenSupplier, () -> true);
   }

   public static StringSetting create(String settingName, String settingNameTranslationKey, String defaultValue, ScreenSupplier screenSupplier) {
      return create(settingName, settingNameTranslationKey, defaultValue, (Consumer)null, screenSupplier, () -> true);
   }

   public static StringSetting create(String settingName, String settingNameTranslationKey, String defaultValue, ScreenSupplier screenSupplier, BooleanSupplier visibilitySupplier) {
      return create(settingName, settingNameTranslationKey, defaultValue, (Consumer)null, screenSupplier, visibilitySupplier);
   }

   public void init() {
      if (Objects.nonNull(this.settingChangeConsumer)) {
         this.settingChangeConsumer.accept(this.value);
      }

   }

   public String getSerializedValue() {
      return this.value;
   }

   public void deserializeValue(final String value) {
      if (!value.equals(this.value)) {
         this.setValue(value);
      }

   }

   public void setValue(final String value) {
      this.value = value;
      if (Objects.nonNull(this.getSettingChangeConsumer())) {
         try {
            this.getSettingChangeConsumer().accept(value);
         } catch (Exception e) {
            XaeroPlus.LOGGER.warn("Error applying setting change consumer for {}", this.getSettingName(), e);
         }
      }

   }

   public String get() {
      return this.value;
   }

   public Consumer<String> getSettingChangeConsumer() {
      return this.settingChangeConsumer;
   }

   public void setSettingChangeConsumer(final Consumer<String> settingChangeConsumer) {
      this.settingChangeConsumer = settingChangeConsumer;
   }

   public ScreenSupplier getScreenSupplier() {
      return this.screenSupplier;
   }

   public IXaeroPlusSettingEntry toXaeroSettingEntry() {
      return new XaeroPlusScreenSwitchSettingEntry(this);
   }

   public interface ScreenSupplier {
      class_437 getScreen(class_437 parent, class_437 escape, StringSetting setting);
   }
}
