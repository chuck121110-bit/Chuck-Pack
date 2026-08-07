package xaero.hud.category.ui;

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.class_2561;
import net.minecraft.class_339;
import xaero.hud.category.ObjectCategory;
import xaero.hud.category.setting.ObjectCategorySetting;
import xaero.hud.category.ui.node.options.range.setting.EditorCompactSettingNode;
import xaero.hud.category.ui.setting.EditorSettingType;
import xaero.lib.client.gui.CustomSettingEntry;

public class RootCategorySettingEntry<T> extends CustomSettingEntry<T> {
   private final Supplier<ObjectCategory<?, ?>> rootCategorySupplier;

   public RootCategorySettingEntry(ObjectCategorySetting<T> setting, Supplier<ObjectCategory<?, ?>> rootCategorySupplier, BooleanSupplier allowNull) {
      this(rootCategorySupplier, allowNull, setting, (class_2561)null);
   }

   public RootCategorySettingEntry(Supplier<ObjectCategory<?, ?>> rootCategorySupplier, BooleanSupplier allowNull, ObjectCategorySetting<T> setting, class_2561 customName) {
      this(rootCategorySupplier, allowNull, setting, customName, (BiConsumer)null);
   }

   public RootCategorySettingEntry(Supplier<ObjectCategory<?, ?>> rootCategorySupplier, BooleanSupplier allowNull, ObjectCategorySetting<T> setting, class_2561 customName, BiConsumer<T, T> onValueChange) {
      super(allowNull, customName == null ? setting.getDisplayName() : customName, setting.getTooltip(), setting.getSettingUIType() == EditorSettingType.SLIDER, () -> {
         ObjectCategory<?, ?> editedCategory = (ObjectCategory)rootCategorySupplier.get();
         return editedCategory == null ? null : editedCategory.getSettingValue(setting);
      }, setting.getUiFirstOption(), setting.getUiLastOption(), setting.getIndexReader(), (v) -> EditorCompactSettingNode.getValueName(setting, v), (oldValue, newValue) -> {
         ObjectCategory<?, ?> editedCategory = (ObjectCategory)rootCategorySupplier.get();
         editedCategory.setSettingValue(setting, newValue);
         if (onValueChange != null) {
            onValueChange.accept(oldValue, newValue);
         }

      }, () -> rootCategorySupplier.get() != null);
      this.rootCategorySupplier = rootCategorySupplier;
   }

   public String getStringForSearch() {
      ObjectCategory<?, ?> editedCategory = (ObjectCategory)this.rootCategorySupplier.get();
      return editedCategory == null ? "" : super.getStringForSearch();
   }

   public class_339 createWidget(int x, int y, int w) {
      ObjectCategory<?, ?> editedCategory = (ObjectCategory)this.rootCategorySupplier.get();
      return editedCategory == null ? null : super.createWidget(x, y, w);
   }
}
