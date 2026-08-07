package xaero.hud.category.ui.entry;

import java.util.function.Supplier;
import net.minecraft.class_1109;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_3417;
import xaero.hud.category.ui.GuiCategoryEditor;
import xaero.lib.client.gui.widget.Tooltip;

public class EditorListEntryTextWithAction extends EditorListEntryWithIconAndText {
   private final Runnable action;

   public EditorListEntryTextWithAction(int entryX, int entryY, int entryW, int entryH, int index, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList, EditorListRootEntry root, Runnable action, Supplier<Tooltip> tooltipSupplier) {
      this(entryX, entryY, entryW, entryH, index, rowList, root.node.getDisplayName(), root, action, tooltipSupplier);
   }

   public EditorListEntryTextWithAction(int entryX, int entryY, int entryW, int entryH, int index, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList, class_2561 text, EditorListRootEntry root, Runnable action, Supplier<Tooltip> tooltipSupplier) {
      super(entryX, entryY, entryW, entryH, index, rowList, text, root, tooltipSupplier);
      this.action = action;
   }

   public boolean selectAction() {
      this.action.run();
      class_310.method_1551().method_1483().method_4873(class_1109.method_47978(class_3417.field_15015, 1.0F));
      return false;
   }
}
