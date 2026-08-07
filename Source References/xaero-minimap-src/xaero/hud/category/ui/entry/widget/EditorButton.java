package xaero.hud.category.ui.entry.widget;

import java.util.function.Supplier;
import net.minecraft.class_2561;
import net.minecraft.class_4185;
import net.minecraft.class_5250;
import xaero.common.gui.IXaeroNarratableWidget;
import xaero.hud.category.ui.GuiCategoryEditor;
import xaero.hud.category.ui.node.EditorNode;

public class EditorButton extends class_4185.class_12231 implements IXaeroNarratableWidget {
   protected Supplier<class_2561> messageSupplier;
   private EditorNode parent;
   private GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList;

   public EditorButton(EditorNode parent, boolean active, int w, int h, EditorNode node, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList) {
      this(parent, () -> node.getDisplayName(), active, w, h, (b) -> node.getExpandAction(rowList).run(), rowList);
   }

   public EditorButton(EditorNode parent, Supplier<class_2561> messageSupplier, boolean active, int w, int h, class_4185.class_4241 onPress, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList) {
      super(2, 2, w, h, class_2561.method_43470(""), onPress, field_40754);
      this.field_22763 = active;
      this.messageSupplier = messageSupplier;
      this.rowList = rowList;
      this.parent = parent;
      this.updateMessage();
   }

   protected void updateMessage() {
      this.method_25355((class_2561)this.messageSupplier.get());
   }

   public class_5250 method_25360() {
      return class_2561.method_43470("");
   }

   public abstract static class PressActionWithContext implements class_4185.class_4241 {
      public void onPress(class_4185 button) {
         this.onPress((EditorButton)button, ((EditorButton)button).parent, ((EditorButton)button).rowList);
      }

      public abstract void onPress(EditorButton var1, EditorNode var2, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList var3);
   }
}
