package xaero.hud.category.ui.node.options.range.setting;

import java.util.function.Function;
import java.util.function.IntFunction;
import net.minecraft.class_2561;
import xaero.hud.category.setting.ObjectCategorySetting;
import xaero.hud.category.ui.entry.EditorListRootEntryFactory;
import xaero.hud.category.ui.node.options.EditorOptionsNode;
import xaero.hud.category.ui.node.options.range.EditorCompactRangeNode;
import xaero.hud.category.ui.node.tooltip.IEditorDataTooltipSupplier;
import xaero.hud.category.util.CategoryConstants;

public final class EditorCompactSettingNode<V> extends EditorCompactRangeNode<V> implements IEditorSettingNode<V> {
   private final ObjectCategorySetting<V> setting;
   private final boolean rootSettings;

   private EditorCompactSettingNode(ObjectCategorySetting<V> setting, class_2561 displayName, V settingValue, boolean rootSettings, boolean hasNullOption, int currentIndex, int optionCount, int minNumber, IntFunction<V> numberReader, Function<V, class_2561> valueNamer, boolean movable, EditorListRootEntryFactory listEntryFactory, IEditorDataTooltipSupplier tooltipSupplier, EditorOptionsNode.IOptionsNodeIsActiveSupplier isActiveSupplier) {
      super(displayName, settingValue, currentIndex, optionCount, minNumber, hasNullOption, numberReader, valueNamer, movable, listEntryFactory, tooltipSupplier, isActiveSupplier);
      this.setting = setting;
      this.rootSettings = rootSettings;
   }

   public ObjectCategorySetting<V> getSetting() {
      return this.setting;
   }

   public V getSettingValue() {
      return (V)this.getCurrentRangeValue();
   }

   public boolean isRootSettings() {
      return this.rootSettings;
   }

   public static <T> class_2561 getValueName(ObjectCategorySetting<T> setting, Object value) {
      return value == null ? CategoryConstants.INHERIT : (class_2561)setting.getWidgetValueNameProvider().apply(value);
   }

   public static final class Builder<V> extends EditorCompactRangeNode.Builder<V, Builder<V>> implements IEditorSettingNodeBuilder<V, EditorCompactSettingNode<V>> {
      private ObjectCategorySetting<V> setting;
      private boolean rootSettings;

      private Builder() {
      }

      public Builder<V> setDefault() {
         this.setSetting((ObjectCategorySetting)null);
         this.setRootSettings(false);
         return (Builder)super.setDefault();
      }

      public Builder<V> setSetting(ObjectCategorySetting<V> setting) {
         this.setting = setting;
         if (setting == null) {
            this.setValueNamer((Function)null);
            this.setNumberReader((IntFunction)null);
            this.setNumberWriter((Function)null);
            this.setMinNumber(0);
            this.setMaxNumber(0);
            this.setTooltipSupplier((IEditorDataTooltipSupplier)null);
         } else {
            this.setValueNamer((v) -> EditorCompactSettingNode.getValueName(setting, v));
            this.setNumberReader(setting.getIndexReader());
            this.setNumberWriter(setting.getIndexWriter());
            this.setMinNumber(setting.getUiFirstOption());
            this.setMaxNumber(setting.getUiLastOption());
            this.setTooltipInfoSupplier((parent, data) -> setting.getTooltip());
         }

         return this;
      }

      public Builder<V> setSettingValue(V settingValue) {
         this.setCurrentRangeValue(settingValue);
         return this;
      }

      public Builder<V> setRootSettings(boolean rootSettings) {
         this.rootSettings = rootSettings;
         this.setHasNullOption(!rootSettings);
         return this;
      }

      public Builder<V> setSlider(boolean slider) {
         return (Builder)super.setSlider(slider);
      }

      public EditorCompactSettingNode<V> build() {
         if (this.setting == null) {
            throw new IllegalStateException("required fields not set!");
         } else {
            if (this.displayName == null) {
               this.setDisplayName(this.setting.getDisplayName());
            }

            return (EditorCompactSettingNode)super.build();
         }
      }

      public static <V> Builder<V> begin() {
         return (new Builder()).setDefault();
      }

      protected EditorCompactRangeNode<V> buildInternally(int currentIndex, int optionCount, EditorListRootEntryFactory listEntryFactory) {
         return new EditorCompactSettingNode<V>(this.setting, this.displayName, this.currentRangeValue, this.rootSettings, this.hasNullOption, currentIndex, optionCount, this.minNumber, this.numberReader, this.valueNamer, this.movable, listEntryFactory, this.tooltipSupplier, this.isActiveSupplier);
      }
   }
}
