package xaeroplus.settings;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_310;
import org.apache.commons.lang3.ArrayUtils;
import xaero.lib.common.gui.widget.TooltipInfo;
import xaeroplus.XaeroPlus;
import xaeroplus.feature.extensions.IXaeroPlusSettingEntry;
import xaeroplus.feature.extensions.XaeroPlusCustomSettingEntry;

public class EnumSetting<T extends Enum<T>> extends XaeroPlusSetting {
   private final T[] enumValues;
   private T value;
   private Consumer<T> settingChangeConsumer;

   private EnumSetting(final String settingName, final String settingNameTranslationKey, final String tooltipTranslationKey, final class_304 keyBinding, final T[] enumValues, final T defaultValue, final Consumer<T> settingChangeConsumer, final BooleanSupplier visibilitySupplier) {
      super(settingName, settingNameTranslationKey, tooltipTranslationKey, keyBinding, visibilitySupplier);
      this.enumValues = enumValues;
      this.value = defaultValue;
      this.settingChangeConsumer = settingChangeConsumer;
   }

   public static <E extends Enum<E>> EnumSetting<E> create(String settingName, String settingNameTranslationKey, E[] values, E defaultValue) {
      return new EnumSetting<E>("[XP] " + settingName, settingNameTranslationKey, buildTooltipTranslationKey(settingNameTranslationKey), (class_304)null, values, defaultValue, (Consumer)null, (BooleanSupplier)null);
   }

   public static <E extends Enum<E>> EnumSetting<E> create(String settingName, String settingNameTranslationKey, E[] values, E defaultValue, Consumer<E> settingChangeConsumer) {
      return new EnumSetting<E>("[XP] " + settingName, settingNameTranslationKey, buildTooltipTranslationKey(settingNameTranslationKey), (class_304)null, values, defaultValue, settingChangeConsumer, (BooleanSupplier)null);
   }

   public static <E extends Enum<E>> EnumSetting<E> create(String settingName, String settingNameTranslationKey, E[] values, E defaultValue, Consumer<E> settingChangeConsumer, BooleanSupplier visibilitySupplier) {
      return new EnumSetting<E>("[XP] " + settingName, settingNameTranslationKey, buildTooltipTranslationKey(settingNameTranslationKey), (class_304)null, values, defaultValue, settingChangeConsumer, visibilitySupplier);
   }

   public String getSerializedValue() {
      return Integer.toString(this.getValueIndex());
   }

   public void deserializeValue(String value) {
      int index = Integer.parseInt(value);
      if (index != this.getValueIndex()) {
         this.setValueIndex(index);
      }

   }

   public IXaeroPlusSettingEntry toXaeroSettingEntry() {
      return new XaeroPlusCustomSettingEntry(this, class_2561.method_43470(this.getTranslatedName()), new TooltipInfo(this.getTooltipTranslationKey()), false, this::get, 0, this.getIndexMax(), (v) -> this.getEnumValues()[v], (v) -> {
         if (v instanceof TranslatableSettingEnum translatableSettingEnum) {
            return class_2561.method_43471(translatableSettingEnum.getTranslationKey());
         } else {
            return class_2561.method_43470(v.toString());
         }
      }, (v1, v2) -> {
         this.setValue(v2);
         SettingHooks.saveSettings();
         class_310.method_1551().method_1507(class_310.method_1551().field_1755);
      }, this::isVisible);
   }

   public T get() {
      return this.value;
   }

   public void setValue(T newVal) {
      this.value = newVal;
      if (Objects.nonNull(this.getSettingChangeConsumer())) {
         try {
            this.getSettingChangeConsumer().accept(newVal);
         } catch (Exception e) {
            XaeroPlus.LOGGER.error("Error applying setting change consumer for setting: {}, value: {}", new Object[]{this.getSettingName(), newVal, e});
         }
      }

   }

   public int getValueIndex() {
      return ArrayUtils.indexOf(this.enumValues, this.get());
   }

   public void setValueIndex(final int index) {
      try {
         this.setValue(this.enumValues[index]);
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Unable to set enum value setting for {}, index {}", new Object[]{this.getSettingName(), index, e});
      }

   }

   public int getIndexMax() {
      return this.enumValues.length - 1;
   }

   public void setSettingChangeConsumer(final Consumer<T> settingChangeConsumer) {
      this.settingChangeConsumer = settingChangeConsumer;
   }

   public Consumer<T> getSettingChangeConsumer() {
      return this.settingChangeConsumer;
   }

   public T[] getEnumValues() {
      return this.enumValues;
   }

   public void init() {
      if (Objects.nonNull(this.settingChangeConsumer)) {
         this.settingChangeConsumer.accept(this.value);
      }

   }
}
