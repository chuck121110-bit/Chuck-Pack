package xaero.hud.category.ui.node.options.range.setting;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import javax.annotation.Nonnull;
import net.minecraft.class_2561;
import xaero.common.misc.ListFactory;
import xaero.hud.category.setting.ObjectCategorySetting;
import xaero.hud.category.ui.entry.EditorListRootEntryFactory;
import xaero.hud.category.ui.node.options.EditorOptionNode;
import xaero.hud.category.ui.node.options.EditorOptionsNode;
import xaero.hud.category.ui.node.options.range.EditorExpandingRangeNode;
import xaero.hud.category.ui.node.tooltip.IEditorDataTooltipSupplier;

public final class EditorExpandingSettingNode<V> extends EditorExpandingRangeNode<V> implements IEditorSettingNode<V> {
   private final ObjectCategorySetting<V> setting;
   private final boolean rootSettings;

   private EditorExpandingSettingNode(ObjectCategorySetting<V> setting, class_2561 displayName, V settingValue, boolean rootSettings, IntFunction<V> numberReader, EditorOptionNode<Integer> currentValue, List<EditorOptionNode<Integer>> options, boolean movable, @Nonnull EditorListRootEntryFactory listEntryFactory, IEditorDataTooltipSupplier tooltipSupplier, EditorOptionsNode.IOptionsNodeIsActiveSupplier isActiveSupplier) {
      super(displayName, settingValue, numberReader, currentValue, options, movable, listEntryFactory, tooltipSupplier, isActiveSupplier);
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

   public static final class Builder<V> extends EditorExpandingRangeNode.Builder<V, Builder<V>> implements IEditorSettingNodeBuilder<V, EditorExpandingSettingNode<V>> {
      private ObjectCategorySetting<V> setting;
      private boolean rootSettings;

      private Builder(ListFactory listFactory) {
         super(listFactory);
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
            return this;
         } else {
            this.setValueNamer((v) -> EditorCompactSettingNode.getValueName(setting, v));
            this.setNumberReader(setting.getIndexReader());
            this.setNumberWriter(setting.getIndexWriter());
            this.setMinNumber(setting.getUiFirstOption());
            this.setMaxNumber(setting.getUiLastOption());
            this.setTooltipInfoSupplier((parent, data) -> setting.getTooltip());
            return this;
         }
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

      public EditorExpandingSettingNode<V> build() {
         if (this.setting == null) {
            throw new IllegalStateException("required fields not set!");
         } else {
            if (this.displayName == null) {
               this.setDisplayName(this.setting.getDisplayName());
            }

            this.optionBuilders.clear();
            return (EditorExpandingSettingNode)super.build();
         }
      }

      protected EditorExpandingSettingNode<V> buildInternally(EditorOptionNode<Integer> currentValueData, List<EditorOptionNode<Integer>> options) {
         return new EditorExpandingSettingNode<V>(this.setting, this.displayName, this.currentRangeValue, this.rootSettings, this.numberReader, currentValueData, options, this.movable, this.listEntryFactory, this.tooltipSupplier, this.isActiveSupplier);
      }

      public static <V> Builder<V> begin(ListFactory listFactory) {
         return (new Builder(listFactory)).setDefault();
      }
   }
}
