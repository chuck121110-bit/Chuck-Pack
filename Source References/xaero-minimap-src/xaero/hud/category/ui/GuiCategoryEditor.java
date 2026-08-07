package xaero.hud.category.ui;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_364;
import net.minecraft.class_410;
import net.minecraft.class_4185;
import net.minecraft.class_4280;
import net.minecraft.class_437;
import net.minecraft.class_6381;
import net.minecraft.class_6382;
import net.minecraft.class_8016;
import net.minecraft.class_8023;
import net.minecraft.class_8028;
import org.joml.Matrix3x2fStack;
import xaero.common.IXaeroMinimap;
import xaero.hud.category.ObjectCategory;
import xaero.hud.category.ui.entry.ConnectionLineType;
import xaero.hud.category.ui.entry.EditorListEntry;
import xaero.hud.category.ui.entry.EditorListRootEntry;
import xaero.hud.category.ui.node.EditorCategoryNode;
import xaero.hud.category.ui.node.EditorNode;
import xaero.hud.category.ui.node.EditorSettingsNode;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.widget.Tooltip;

public abstract class GuiCategoryEditor<C extends ObjectCategory<?, C>, ED extends EditorCategoryNode<C, SD, ED>, CB extends ObjectCategory.Builder<C, CB>, SD extends EditorSettingsNode<?>, SDB extends EditorSettingsNode.Builder<SD, SDB>, EDB extends EditorCategoryNode.Builder<C, ED, SD, SDB, EDB>> extends ScreenBase {
   public static final class_2561 READ_ONLY_COMPONENT;
   private static final int FRAME_TOP_SIZE = 32;
   private static final int FRAME_BOTTOM_SIZE = 48;
   public static final int ROW_HEIGHT = 24;
   public static final int ROW_WIDTH = 220;
   private GuiCategoryEditor<C, ED, CB, SD, SDB, EDB>.SettingRowList rowList;
   private final EditorCategoryNodeConverter<C, ED, CB, SD, SDB, EDB> dataConverter;
   private ED editorData;
   protected ED cutCategory;
   protected ED cutCategorySuper;
   protected final IXaeroMinimap modMain;
   protected final boolean readOnly;

   protected GuiCategoryEditor(IXaeroMinimap modMain, class_437 parent, class_437 escape, class_2561 title, EditorCategoryNodeConverter<C, ED, CB, SD, SDB, EDB> dataConverter, boolean readOnly) {
      super(parent, escape, title);
      this.modMain = modMain;
      this.dataConverter = dataConverter;
      this.readOnly = readOnly;
      this.editorData = this.constructEditorData(dataConverter);
   }

   protected abstract ED constructEditorData(EditorCategoryNodeConverter<C, ED, CB, SD, SDB, EDB> var1);

   protected abstract ED constructDefaultData(EditorCategoryNodeConverter<C, ED, CB, SD, SDB, EDB> var1);

   protected abstract void onConfigConfirmed(C var1);

   public void method_25426() {
      super.method_25426();
      if (this.readOnly) {
         this.method_37063(class_4185.method_46430(class_2561.method_43471("gui.xaero_back"), (b) -> this.cancel(this.parent)).method_46434(this.field_22789 / 2 - 100, this.field_22790 - 32, 200, 20).method_46431());
      } else {
         this.method_37063(class_4185.method_46430(class_2561.method_43471("gui.xaero_category_settings_cancel"), (b) -> this.field_22787.method_1507(new class_410((result) -> {
               if (result) {
                  this.cancel(this.parent);
               } else {
                  this.field_22787.method_1507(this);
               }

            }, class_2561.method_43471("gui.xaero_category_settings_cancel_confirm"), class_2561.method_43470("")))).method_46434(this.field_22789 / 2 + 5, this.field_22790 - 32, 150, 20).method_46431());
         this.method_37063(class_4185.method_46430(class_2561.method_43471("gui.xaero_category_settings_confirm"), (b) -> this.confirm()).method_46434(this.field_22789 / 2 - 155, this.field_22790 - 32, 150, 20).method_46431());
      }

      class_4185 resetButton = class_4185.method_46430(class_2561.method_43471("gui.xaero_category_settings_reset"), (b) -> {
         if (!this.readOnly) {
            this.field_22787.method_1507(new class_410((result) -> {
               if (result) {
                  this.editorData = this.constructDefaultData(this.dataConverter);
               }

               this.field_22787.method_1507(this);
            }, class_2561.method_43471("gui.xaero_category_settings_reset_confirm1"), class_2561.method_43471("gui.xaero_category_settings_reset_confirm2")));
         }
      }).method_46434(6, 6, 120, 20).method_46431();
      resetButton.field_22763 = !this.readOnly;
      this.method_37063(resetButton);
      this.rowList = new SettingRowList(this.dataConverter);
      this.method_25429(this.rowList);
   }

   private void confirm() {
      super.onExit(this.parent);
      if (!this.readOnly) {
         this.onConfigConfirmed(this.dataConverter.getConfiguredBuilder(this.editorData).build());
      }

   }

   public void onExit(class_437 screen) {
      if (this.readOnly) {
         super.onExit(screen);
      } else {
         this.field_22787.method_1507(new class_410((result) -> {
            if (result) {
               this.confirm();
            }

            this.cancel(screen);
         }, class_2561.method_43471("gui.xaero_category_settings_save_confirm"), class_2561.method_43470("")) {
            public boolean method_25404(class_11908 event) {
               return event.comp_4795() == 256 ? true : super.method_25404(event);
            }
         });
      }
   }

   protected void cancel(class_437 screen) {
      super.onExit(screen);
   }

   public void method_25420(class_332 guiGraphics, int i, int j, float f) {
      super.method_25420(guiGraphics, i, j, f);
      this.rowList.method_25394(guiGraphics, i, j, f);
      guiGraphics.method_27534(this.field_22787.field_1772, this.field_22785, this.field_22789 / 2, 5, 16777215);
   }

   public void method_25394(class_332 guiGraphics, int i, int j, float f) {
      super.method_25394(guiGraphics, i, j, f);
      if (this.readOnly) {
         guiGraphics.method_27535(this.field_22793, READ_ONLY_COMPONENT, this.field_22789 - 5 - this.field_22793.method_27525(READ_ONLY_COMPONENT), 5, -1);
      }

      if (this.rowList.hovered != null) {
         if (this.openDropdown == null || !this.openDropdown.method_49606()) {
            Supplier<Tooltip> tooltipSupplier = this.rowList.hovered.getTooltipSupplier();
            if (tooltipSupplier != null) {
               Tooltip tooltip = (Tooltip)tooltipSupplier.get();
               if (tooltip != null) {
                  tooltip.drawBox(guiGraphics, i, j, this.field_22789, this.field_22790);
               }
            }
         }
      }
   }

   public boolean method_25404(class_11908 event) {
      return this.rowList.method_25370() && event.comp_4795() == 257 && this.rowList.confirmSelection() ? true : super.method_25404(event);
   }

   public void method_25393() {
      this.rowList.tick();
      super.method_25393();
   }

   public GuiCategoryEditor<C, ED, CB, SD, SDB, EDB>.SettingRowList getRowList() {
      return this.rowList;
   }

   static {
      READ_ONLY_COMPONENT = class_2561.method_43471("gui.xaero_category_editor_read_only").method_27692(class_124.field_1054);
   }

   public class SettingRowList extends class_4280<GuiCategoryEditor<C, ED, CB, SD, SDB, EDB>.SettingRowList.Entry> {
      private EditorNode lastExpandedData;
      private boolean restoreScrollAfterUpdate;
      private EditorListEntry hovered;
      private final EditorCategoryNodeConverter<C, ED, CB, SD, SDB, EDB> dataConverter;
      public final boolean readOnly;
      private static final class_2561 USAGE_NARRATION = class_2561.method_43471("narration.selection.usage");
      private static final class_2561 LEFT_RIGHT_USAGE = class_2561.method_43471("narration.xaero_ui_list_left_right_usage");

      public SettingRowList(EditorCategoryNodeConverter<C, ED, CB, SD, SDB, EDB> dataConverter) {
         super(GuiCategoryEditor.this.field_22787, GuiCategoryEditor.this.field_22789, Math.max(4, GuiCategoryEditor.this.field_22790 - 48 - 32), 32, 24);
         this.dataConverter = dataConverter;
         this.readOnly = GuiCategoryEditor.this.readOnly;
         this.updateEntries();
      }

      protected boolean method_73379() {
         return false;
      }

      public boolean hasCut() {
         if (GuiCategoryEditor.this.cutCategory == null) {
            return false;
         } else if (GuiCategoryEditor.this.cutCategorySuper.getSubCategories().contains(GuiCategoryEditor.this.cutCategory)) {
            return true;
         } else {
            this.setCutCategory((EditorCategoryNode)null, (EditorCategoryNode)null);
            return false;
         }
      }

      public ED getCut() {
         return GuiCategoryEditor.this.cutCategory;
      }

      public boolean isCut(ED category) {
         return GuiCategoryEditor.this.cutCategory == category ? this.hasCut() : false;
      }

      public void setCutCategory(ED cutCategory, ED cutCategorySuper) {
         GuiCategoryEditor.this.cutCategory = cutCategory;
         GuiCategoryEditor.this.cutCategorySuper = cutCategorySuper;
      }

      public void pasteTo(ED destination) {
         if (GuiCategoryEditor.this.cutCategory != null) {
            if (destination != GuiCategoryEditor.this.cutCategory && destination != GuiCategoryEditor.this.cutCategorySuper) {
               destination.getExpandAction(this).run();
               this.setLastExpandedData(GuiCategoryEditor.this.cutCategory);
               GuiCategoryEditor.this.cutCategorySuper.getSubCategories().remove(GuiCategoryEditor.this.cutCategory);
               destination.getSubCategories().add(0, GuiCategoryEditor.this.cutCategory);
               this.setCutCategory((EditorCategoryNode)null, (EditorCategoryNode)null);
            } else {
               this.setCutCategory((EditorCategoryNode)null, (EditorCategoryNode)null);
               this.updateEntries();
            }
         }
      }

      public boolean method_25370() {
         return GuiCategoryEditor.this.method_25399() == this;
      }

      public void setLastExpandedData(EditorNode lastExpandedData) {
         this.lastExpandedData = lastExpandedData;
      }

      public void restoreScrollAfterUpdate() {
         this.restoreScrollAfterUpdate = true;
      }

      public void updateEntries() {
         double scrollBackup = this.method_44387();
         this.method_25339();
         GuiCategoryEditor.this.editorData.setExpanded(true);
         this.addEntriesForExpanded(GuiCategoryEditor.this.editorData, (EditorNode)null);
         if (this.method_25334() != null) {
            this.method_25324((Entry)this.method_25334());
         }

         if (this.restoreScrollAfterUpdate) {
            this.method_44382(scrollBackup);
            this.restoreScrollAfterUpdate = false;
         }

      }

      private void addEntriesForExpanded(EditorNode data, EditorNode parent) {
         int nextIndex = this.method_25396().size();
         List<EditorNode> subExpandables = data.getSubNodes();
         if (subExpandables != null) {
            EditorNode expandedData = null;

            for(EditorNode sed : subExpandables) {
               if (sed.isExpanded()) {
                  expandedData = sed;
                  break;
               }
            }

            EditorListRootEntry wrappedEntry = data.getListEntryFactory().get(data, parent, nextIndex, nextIndex == 0 ? ConnectionLineType.NONE : ConnectionLineType.PATH, this, this.field_22758, expandedData == null);
            GuiCategoryEditor<C, ED, CB, SD, SDB, EDB>.SettingRowList.Entry currentEntry = new Entry(wrappedEntry, nextIndex++);
            this.method_25321(currentEntry);
            if (data == this.lastExpandedData) {
               this.method_25395(currentEntry);
            }

            if (expandedData != null) {
               this.addEntriesForExpanded(expandedData, data);
            } else {
               if (this.lastExpandedData == null && data.isExpanded()) {
                  this.method_25395(currentEntry);
               }

               boolean first = true;

               for(EditorNode sed : subExpandables) {
                  wrappedEntry = sed.getListEntryFactory().get(sed, data, nextIndex, first ? ConnectionLineType.HEAD_LEAF : ConnectionLineType.TAIL_LEAF, this, this.field_22758, false);
                  GuiCategoryEditor<C, ED, CB, SD, SDB, EDB>.SettingRowList.Entry leafEntry = new Entry(wrappedEntry, nextIndex++);
                  this.method_25321(leafEntry);
                  if (sed == this.lastExpandedData) {
                     this.method_25395(leafEntry);
                  }

                  first = false;
               }

            }
         }
      }

      public boolean method_25402(class_11909 event, boolean doubleClick) {
         if (!this.method_25405(event.comp_4798(), event.comp_4799())) {
            this.method_25395((class_364)null);
         }

         return super.method_25402(event, doubleClick);
      }

      public void method_16014(double d, double e) {
         if (this.method_25334() != null) {
            ((Entry)this.method_25334()).method_16014(d, e);
         }

         super.method_16014(d, e);
      }

      public boolean method_16803(class_11908 event) {
         return this.method_25334() != null && ((Entry)this.method_25334()).method_16803(event) ? true : super.method_16803(event);
      }

      public boolean method_25400(class_11905 event) {
         if (this.method_25334() != null) {
            boolean result = ((Entry)this.method_25334()).method_25400(event);
            if (result) {
               return true;
            }
         }

         return super.method_25400(event);
      }

      public void tick() {
         if (this.method_25334() != null) {
            ((Entry)this.method_25334()).tick();
         }

      }

      public boolean confirmSelection() {
         GuiCategoryEditor<C, ED, CB, SD, SDB, EDB>.SettingRowList.Entry entry = (Entry)this.method_25334();
         if (entry == null) {
            return false;
         } else {
            EditorListEntry selectedSubEntry = entry.wrappedEntry.confirmSelection();
            return selectedSubEntry != null;
         }
      }

      public void method_25395(class_364 guiEventListener) {
         if ((guiEventListener == null || this.method_25396().contains(guiEventListener)) && this.method_25336() != guiEventListener) {
            if (this.method_25334() != null) {
               ((Entry)this.method_25334()).wrappedEntry.unfocusRecursively();
            }

            if (this.method_25336() != null) {
               ((Entry)this.method_25336()).method_25365(false);
            }

            GuiCategoryEditor<C, ED, CB, SD, SDB, EDB>.SettingRowList.Entry entry = (Entry)guiEventListener;
            if (entry != null) {
               entry.wrappedEntry.focusFirstRecursively();
            }

            super.method_25395(guiEventListener);
            if (guiEventListener == null) {
               this.setSelected((Entry)null);
            }

            this.narrateSelection();
         }
      }

      public void setSelected(GuiCategoryEditor<C, ED, CB, SD, SDB, EDB>.SettingRowList.Entry entry) {
         super.method_25313(entry);
      }

      public int method_25322() {
         return this.field_22758;
      }

      protected int method_65507() {
         return this.field_22758 / 2 + 164;
      }

      public void narrateSelection() {
         GuiCategoryEditor.this.method_37070();
      }

      public void method_47399(class_6382 narrationElementOutput) {
         super.method_47399(narrationElementOutput);
         if (this.method_25370()) {
            narrationElementOutput.method_37035(class_6381.field_33791, new class_2561[]{USAGE_NARRATION, LEFT_RIGHT_USAGE});
         }

      }

      public void method_48579(class_332 guiGraphics, int i, int j, float f) {
         this.hovered = null;
         super.method_48579(guiGraphics, i, j, f);
      }

      public class_8016 method_48205(class_8023 focusNavigationEvent) {
         if (focusNavigationEvent instanceof class_8023.class_8024 arrowNavigation) {
            if (arrowNavigation.comp_1191() == class_8028.field_41828 || arrowNavigation.comp_1191() == class_8028.field_41829) {
               return null;
            }
         }

         return super.method_48205(focusNavigationEvent);
      }

      public EditorCategoryNodeConverter<C, ED, CB, SD, SDB, EDB> getDataConverter() {
         return this.dataConverter;
      }

      public class Entry extends class_4280.class_4281<GuiCategoryEditor<C, ED, CB, SD, SDB, EDB>.SettingRowList.Entry> {
         private EditorListRootEntry wrappedEntry;
         private int index;
         private int lastX;
         private int lastY;

         public Entry(EditorListRootEntry entryInfo, int index) {
            this.wrappedEntry = entryInfo;
            this.index = index;
         }

         public void method_25343(class_332 guiGraphics, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            Matrix3x2fStack poseStack = guiGraphics.method_51448();
            int x = this.method_46426();
            int y = this.method_46427() + 2;
            this.lastX = x;
            this.lastY = y;
            poseStack.pushMatrix();
            poseStack.translate((float)x, (float)y);
            boolean includesSelected = SettingRowList.this.method_25334() == this;
            this.wrappedEntry.preRender(guiGraphics, includesSelected, true);
            EditorListEntry hoveredInRow = this.wrappedEntry.render(guiGraphics, this.index, SettingRowList.this.method_25322(), this.method_25364(), mouseX - x - this.wrappedEntry.getEntryRelativeX(), mouseY - y - this.wrappedEntry.getEntryRelativeY(), isMouseOver, partialTicks, GuiCategoryEditor.this.field_22793, mouseX, mouseY, includesSelected, true);
            this.wrappedEntry.postRender(guiGraphics);
            poseStack.popMatrix();
            if (hoveredInRow != null) {
               SettingRowList.this.hovered = hoveredInRow;
            }

         }

         public boolean method_25402(class_11909 event, boolean doubleClick) {
            SettingRowList.this.method_25395(this);
            double relativeMouseX = event.comp_4798() - (double)this.lastX - (double)this.wrappedEntry.getEntryRelativeX();
            double relativeMouseY = event.comp_4799() - (double)this.lastY - (double)this.wrappedEntry.getEntryRelativeY();
            this.wrappedEntry.mouseClicked(this, relativeMouseX, relativeMouseY, this.index, event.comp_4800(), doubleClick);
            return true;
         }

         public boolean method_25406(class_11909 event) {
            double relativeMouseX = event.comp_4798() - (double)this.lastX - (double)this.wrappedEntry.getEntryRelativeX();
            double relativeMouseY = event.comp_4799() - (double)this.lastY - (double)this.wrappedEntry.getEntryRelativeY();
            this.wrappedEntry.mouseReleased(relativeMouseX, relativeMouseY, this.index, event.comp_4800());
            return super.method_25406(event);
         }

         public boolean method_25401(double mouseX, double mouseY, double f, double g) {
            double relativeMouseX = mouseX - (double)this.lastX - (double)this.wrappedEntry.getEntryRelativeX();
            double relativeMouseY = mouseY - (double)this.lastY - (double)this.wrappedEntry.getEntryRelativeY();
            return this.wrappedEntry.mouseScrolled(relativeMouseX, relativeMouseY, f, g) ? true : super.method_25401(mouseX, mouseY, f, g);
         }

         public boolean method_25403(class_11909 event, double f, double g) {
            double relativeMouseX = event.comp_4798() - (double)this.lastX - (double)this.wrappedEntry.getEntryRelativeX();
            double relativeMouseY = event.comp_4799() - (double)this.lastY - (double)this.wrappedEntry.getEntryRelativeY();
            return this.wrappedEntry.mouseDragged(relativeMouseX, relativeMouseY, this.index, f, g, event.comp_4800()) ? true : super.method_25403(event, f, g);
         }

         public boolean method_25404(class_11908 event) {
            return this.wrappedEntry.keyPressed(event, true) ? true : super.method_25404(event);
         }

         public boolean method_16803(class_11908 event) {
            return this.wrappedEntry.keyReleased(event) ? true : super.method_16803(event);
         }

         public boolean method_25400(class_11905 event) {
            return this.wrappedEntry.charTyped(event) ? true : super.method_25400(event);
         }

         public void method_25365(boolean bl) {
            this.wrappedEntry.setFocused(bl);
            super.method_25365(bl);
         }

         public void tick() {
            this.wrappedEntry.tick();
         }

         public class_2561 method_37006() {
            String selectedNarrationString = this.wrappedEntry.getSubNarration();
            return selectedNarrationString == null ? class_2561.method_43470("") : class_2561.method_43469("narrator.select", new Object[]{selectedNarrationString});
         }
      }
   }
}
