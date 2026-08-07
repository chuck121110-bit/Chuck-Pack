package xaeroplus.settings;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_3532;
import xaero.lib.common.gui.widget.TooltipInfo;
import xaeroplus.XaeroPlus;
import xaeroplus.feature.extensions.IXaeroPlusSettingEntry;
import xaeroplus.feature.extensions.XaeroPlusCustomSettingEntry;

public class DoubleSetting extends XaeroPlusSetting {
   private final double valueMin;
   private final double valueMax;
   private final double valueStep;
   private double value;
   private DoubleConsumer settingChangeConsumer;

   private DoubleSetting(final String settingName, final String settingNameTranslationKey, final String tooltipTranslationKey, final class_304 keyBinding, final double valueMin, final double valueMax, final double valueStep, final double defaultValue, final DoubleConsumer settingChangeConsumer, final BooleanSupplier visibilitySupplier) {
      super(settingName, settingNameTranslationKey, tooltipTranslationKey, keyBinding, visibilitySupplier);
      this.valueMin = valueMin;
      this.valueMax = valueMax;
      this.valueStep = valueStep;
      this.value = defaultValue;
      this.settingChangeConsumer = settingChangeConsumer;
   }

   public static DoubleSetting create(String settingName, String settingNameTranslationKey, double valueMin, double valueMax, double valueStep, double defaultValue) {
      return new DoubleSetting("[XP] " + settingName, settingNameTranslationKey, buildTooltipTranslationKey(settingNameTranslationKey), (class_304)null, valueMin, valueMax, valueStep, defaultValue, (DoubleConsumer)null, (BooleanSupplier)null);
   }

   public static DoubleSetting create(String settingName, String settingNameTranslationKey, double valueMin, double valueMax, double valueStep, double defaultValue, DoubleConsumer changeConsumer) {
      return new DoubleSetting("[XP] " + settingName, settingNameTranslationKey, buildTooltipTranslationKey(settingNameTranslationKey), (class_304)null, valueMin, valueMax, valueStep, defaultValue, changeConsumer, (BooleanSupplier)null);
   }

   public static DoubleSetting create(String settingName, String settingNameTranslationKey, double valueMin, double valueMax, double valueStep, double defaultValue, BooleanSupplier visibilitySupplier) {
      return new DoubleSetting("[XP] " + settingName, settingNameTranslationKey, buildTooltipTranslationKey(settingNameTranslationKey), (class_304)null, valueMin, valueMax, valueStep, defaultValue, (DoubleConsumer)null, visibilitySupplier);
   }

   public static DoubleSetting create(String settingName, String settingNameTranslationKey, double valueMin, double valueMax, double valueStep, double defaultValue, DoubleConsumer changeConsumer, BooleanSupplier visibilitySupplier) {
      return new DoubleSetting("[XP] " + settingName, settingNameTranslationKey, buildTooltipTranslationKey(settingNameTranslationKey), (class_304)null, valueMin, valueMax, valueStep, defaultValue, changeConsumer, visibilitySupplier);
   }

   public String getSerializedValue() {
      return Double.toString(this.value);
   }

   public void deserializeValue(String value) {
      double f = Double.parseDouble(value);
      if (f != this.get()) {
         this.setValue(f);
      }

   }

   public IXaeroPlusSettingEntry toXaeroSettingEntry() {
      int numIndeces = (int)((this.valueMax - this.valueMin) / this.valueStep);
      return new XaeroPlusCustomSettingEntry(this, class_2561.method_43470(this.getTranslatedName()), new TooltipInfo(this.getTooltipTranslationKey()), true, this::get, 0, numIndeces, (v) -> class_3532.method_15350(this.valueMin + (double)v * this.valueStep, this.valueMin, this.valueMax), (v) -> class_2561.method_43470(String.format("%.2f", v)), (v1, v2) -> {
         this.setValue(v2);
         SettingHooks.saveSettings();
      }, this::isVisible);
   }

   public double getValueMin() {
      return this.valueMin;
   }

   public double getValueMax() {
      return this.valueMax;
   }

   public double getValueStep() {
      return this.valueStep;
   }

   public double get() {
      return this.value;
   }

   public int getAsInt() {
      return (int)this.value;
   }

   public void setValue(final double value) {
      this.value = value;
      if (Objects.nonNull(this.getSettingChangeConsumer())) {
         try {
            this.getSettingChangeConsumer().accept(value);
         } catch (Exception e) {
            XaeroPlus.LOGGER.warn("Error applying setting change consumer for {}", this.getSettingName(), e);
         }
      }

   }

   public DoubleConsumer getSettingChangeConsumer() {
      return this.settingChangeConsumer;
   }

   public void setSettingChangeConsumer(final DoubleConsumer settingChangeConsumer) {
      this.settingChangeConsumer = settingChangeConsumer;
   }

   public void init() {
      if (Objects.nonNull(this.settingChangeConsumer)) {
         this.settingChangeConsumer.accept(this.value);
      }

   }
}
