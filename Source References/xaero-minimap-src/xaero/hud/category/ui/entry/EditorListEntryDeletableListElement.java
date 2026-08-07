package xaero.hud.category.ui.entry;

import java.util.function.Supplier;
import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import xaero.hud.category.ui.GuiCategoryEditor;
import xaero.hud.category.ui.node.EditorNode;
import xaero.hud.category.ui.node.EditorSimpleDeletableWrapperNode;
import xaero.lib.client.gui.widget.Tooltip;

public class EditorListEntryDeletableListElement extends EditorListRootEntry {
   private final EditorSimpleDeletableWrapperNode.DeletionCallback deletionCallback;
   private final EditorNode parent;
   private static final Tooltip DELETE_TOOLTIP;

   public EditorListEntryDeletableListElement(int screenWidth, int index, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList, ConnectionLineType lineType, EditorSimpleDeletableWrapperNode<?> node, EditorNode parent, EditorSimpleDeletableWrapperNode.DeletionCallback deletionCallback, Supplier<Tooltip> tooltipSupplier, boolean readOnly) {
      super(screenWidth, index, rowList, lineType, node);
      this.deletionCallback = deletionCallback;
      this.parent = parent;
      this.withSubEntry((x, y, w, h, root) -> new EditorListEntryTextWithAction(x, y, w, h, index, rowList, this, node.getExpandAction(rowList), tooltipSupplier));
      if (!readOnly) {
         this.withSubEntry((x, y, w, h, root) -> new EditorListTextButtonEntry(x - 24, y + 2, index, rowList, class_2561.method_43470("x"), -5636096, -43691, 5, () -> deletionCallback.delete(parent, node, rowList), this, DELETE_TOOLTIP));
      }
   }

   public boolean keyPressed(class_11908 event, boolean isRoot) {
      if (event.comp_4795() == 261) {
         if (!this.rowList.readOnly && this.deletionCallback.delete(this.parent, (EditorSimpleDeletableWrapperNode)this.node, this.rowList)) {
            this.rowList.restoreScrollAfterUpdate();
            this.rowList.updateEntries();
         }

         return false;
      } else {
         return super.keyPressed(event, isRoot);
      }
   }

   public class_2561 getMessage() {
      return this.node.getDisplayName();
   }

   static {
      DELETE_TOOLTIP = new Tooltip("gui.xaero_category_delete_list_element", class_2583.field_24360, true);
   }
}
