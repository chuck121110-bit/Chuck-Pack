package xaero.hud.category.ui.entry;

import java.util.function.Supplier;
import net.minecraft.class_2561;
import net.minecraft.class_339;
import xaero.hud.category.ui.GuiCategoryEditor;
import xaero.lib.client.gui.widget.Tooltip;

public class EditorListEntryExpandingOptions extends EditorListEntryWidget {
   public EditorListEntryExpandingOptions(int entryX, int entryY, int entryW, int entryH, int index, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList, EditorListRootEntry root, class_339 widget, Supplier<class_2561> messageSupplier, Supplier<Tooltip> tooltipSupplier) {
      super(entryX, entryY, entryW, entryH, index, rowList, root, widget, tooltipSupplier);
      if (messageSupplier != null) {
         class_2561 optionTypeName = (class_2561)messageSupplier.get();
         if (!root.node.isExpanded()) {
            widget.method_25355(optionTypeName);
         } else {
            widget.method_25355(class_2561.method_43469("gui.xaero_category_expanded_options", new Object[]{optionTypeName}));
         }
      }
   }
}
