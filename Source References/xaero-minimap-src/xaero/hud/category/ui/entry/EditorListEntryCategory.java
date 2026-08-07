package xaero.hud.category.ui.entry;

import java.util.function.Supplier;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import xaero.hud.category.ObjectCategory;
import xaero.hud.category.ui.GuiCategoryEditor;
import xaero.hud.category.ui.node.EditorCategoryNode;
import xaero.lib.client.gui.widget.Tooltip;

public class EditorListEntryCategory<C extends ObjectCategory<?, C>, ED extends EditorCategoryNode<C, ?, ED>> extends EditorListRootEntry {
   private static final Tooltip HELP_TOOLTIP;
   private static final Tooltip PROTECTED_TOOLTIP;
   private static final Tooltip UP_TOOLTIP;
   private static final Tooltip DOWN_TOOLTIP;

   public EditorListEntryCategory(int screenWidth, int index, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList, ConnectionLineType lineType, EditorCategoryNode<?, ?, ?> node, EditorCategoryNode<?, ?, ?> parent, Supplier<Tooltip> tooltipSupplier, boolean isFinalExpanded) {
      super(screenWidth, index, rowList, lineType, node);
      int subIndex = parent == null ? -1 : parent.getSubCategories().indexOf(node);
      boolean isCut = rowList.isCut(node);
      ED currentCut = rowList.getCut();
      this.withSubEntry(this.getCategoryNameEntryFactory(node, rowList, isCut, tooltipSupplier));
      EditorListRootEntry.CenteredEntryFactory pasteEntryFactory = this.getPasteEntryFactory(currentCut, isCut, node, rowList);
      if (!node.isExpanded() && node.isMovable()) {
         if (!rowList.readOnly && !node.getSettingsNode().getProtection()) {
            this.withSubEntry(this.getDuplicateEntryFactory(subIndex, parent, rowList));
         }

         if (rowList.hasCut()) {
            this.withSubEntry(pasteEntryFactory);
         }

         if (node.getSettingsNode().getProtection()) {
            this.withSubEntry(this.getProtectedEntryFactory());
         } else {
            if (!rowList.readOnly && !rowList.hasCut()) {
               this.withSubEntry(this.getCutEntryFactory(node, parent, rowList));
            }

            if (!rowList.readOnly && parent.getSubCategories().size() > 1) {
               this.withSubEntry(this.getPriorityEntryFactory(-1, parent, subIndex));
               this.withSubEntry(this.getPriorityEntryFactory(1, parent, subIndex));
            }
         }
      } else {
         if (rowList.hasCut()) {
            this.withSubEntry(pasteEntryFactory);
         }

         if (isFinalExpanded) {
            this.addHelpElement(HELP_TOOLTIP);
         }

      }
   }

   private EditorListRootEntry.CenteredEntryFactory getCategoryNameEntryFactory(ED dataCast, GuiCategoryEditor<C, ED, ?, ?, ?, ?>.SettingRowList rowListCast, boolean isCut, Supplier<Tooltip> tooltipSupplier) {
      return (x, y, w, h, root) -> {
         Runnable action = isCut ? () -> rowListCast.pasteTo(dataCast) : dataCast.getExpandAction(rowListCast);
         EditorListEntryTextWithAction result = new EditorListEntryTextWithAction(x, y, w, h, this.index, this.rowList, this, action, tooltipSupplier);
         if (isCut) {
            result.setColor(-5636096);
            result.setHoverColor(-43691);
         }

         return result;
      };
   }

   private EditorListRootEntry.CenteredEntryFactory getPasteEntryFactory(ED currentCut, boolean isCut, ED dataCast, GuiCategoryEditor<C, ED, ?, ?, ?, ?>.SettingRowList rowListCast) {
      Tooltip pasteTooltip = this.getPasteTooltip(currentCut, isCut);
      if (pasteTooltip != null) {
         pasteTooltip.setAutoLinebreak(false);
      }

      return (x, y, w, h, root) -> new EditorListTextButtonEntry(x + 248, y + 2, this.index, this.rowList, class_2561.method_43470("←"), -5592406, -1, 5, ((EditorCategoryNode)dataCast).getPasteAction(rowListCast), this, pasteTooltip);
   }

   private Tooltip getPasteTooltip(ED currentCut, boolean isCut) {
      if (currentCut == null) {
         return null;
      } else if (isCut) {
         return new Tooltip("gui.xaero_category_paste_cancel", class_2583.field_24360, true);
      } else {
         class_2561 component = class_2561.method_43469("gui.xaero_category_paste", new Object[]{currentCut.getDisplayName(), this.node.getDisplayName()});
         return new Tooltip(component, true);
      }
   }

   private EditorListRootEntry.CenteredEntryFactory getDuplicateEntryFactory(int subIndex, ED parentCast, GuiCategoryEditor<C, ED, ?, ?, ?, ?>.SettingRowList rowListCast) {
      class_2561 duplicateTooltipComponent = class_2561.method_43469("gui.xaero_category_duplicate", new Object[]{this.node.getDisplayName()});
      Tooltip duplicateTooltip = new Tooltip(duplicateTooltipComponent, true);
      duplicateTooltip.setAutoLinebreak(false);
      return (x, y, w, h, root) -> new EditorListTextButtonEntry(x + 230, y + 2, this.index, this.rowList, class_2561.method_43470("+"), -5592406, -1, 5, ((EditorCategoryNode)parentCast).getDuplicateAction(subIndex, rowListCast), this, duplicateTooltip);
   }

   private EditorListRootEntry.CenteredEntryFactory getProtectedEntryFactory() {
      return (x, y, w, h, root) -> new EditorListTextButtonEntry(x - 24, y + 2, this.index, this.rowList, class_2561.method_43470("!"), -1644980, -171, 5, () -> false, this, PROTECTED_TOOLTIP);
   }

   private EditorListRootEntry.CenteredEntryFactory getCutEntryFactory(ED dataCast, ED parentCast, GuiCategoryEditor<C, ED, ?, ?, ?, ?>.SettingRowList rowListCast) {
      class_2561 cutTooltipComponent = class_2561.method_43469("gui.xaero_category_cut", new Object[]{this.node.getDisplayName()});
      Tooltip cutTooltip = new Tooltip(cutTooltipComponent, true);
      cutTooltip.setAutoLinebreak(false);
      return (x, y, w, h, root) -> new EditorListTextButtonEntry(x + 248, y + 2, this.index, this.rowList, class_2561.method_43470("↔"), -5592406, -1, 5, ((EditorCategoryNode)dataCast).getCutAction(parentCast, rowListCast), this, cutTooltip);
   }

   private EditorListRootEntry.CenteredEntryFactory getPriorityEntryFactory(int direction, ED parentCast, int subIndex) {
      String label = direction < 0 ? "↑" : "↓";
      Tooltip tooltip = direction < 0 ? UP_TOOLTIP : DOWN_TOOLTIP;
      return (x, y, w, h, root) -> new EditorListTextButtonEntry(x - 32 + 8 * direction, y + 2, this.index, this.rowList, class_2561.method_43470(label), -5592406, -1, 5, ((EditorCategoryNode)parentCast).getMoveAction(subIndex, direction, this.rowList), this, tooltip);
   }

   public class_2561 getMessage() {
      return class_2561.method_43470("");
   }

   static {
      HELP_TOOLTIP = new Tooltip("gui.xaero_category_help2", class_2583.field_24360, true);
      PROTECTED_TOOLTIP = new Tooltip("gui.xaero_category_protected_category", class_2583.field_24360, true);
      UP_TOOLTIP = new Tooltip("gui.xaero_category_category_move_up", class_2583.field_24360, true);
      DOWN_TOOLTIP = new Tooltip("gui.xaero_category_category_move_down", class_2583.field_24360, true);
   }
}
