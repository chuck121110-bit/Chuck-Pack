package xaero.hud.category.ui.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.class_1109;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11910;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3417;
import org.joml.Matrix3x2fStack;
import xaero.hud.category.ui.GuiCategoryEditor;
import xaero.lib.client.gui.widget.Tooltip;

public abstract class EditorListEntry {
   protected final int entryRelativeX;
   protected final int entryRelativeY;
   protected final int entryW;
   protected final int entryH;
   protected final int index;
   protected final GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList;
   protected final List<EditorListEntry> subEntries;
   protected final Supplier<Tooltip> tooltipSupplier;
   protected int focusedSubEntryIndex;
   protected EditorListEntry hoveredSubEntry;

   public EditorListEntry(int entryX, int entryY, int entryW, int entryH, int index, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList, Supplier<Tooltip> tooltipSupplier) {
      this.entryRelativeX = entryX;
      this.entryRelativeY = entryY;
      this.entryW = entryW;
      this.entryH = entryH;
      this.index = index;
      this.rowList = rowList;
      this.subEntries = new ArrayList();
      this.focusedSubEntryIndex = -1;
      this.tooltipSupplier = tooltipSupplier;
   }

   public EditorListEntry onSelected() {
      if (!this.subEntries.isEmpty() && this.focusedSubEntryIndex >= 0) {
         EditorListEntry subEntry = (EditorListEntry)this.subEntries.get(this.focusedSubEntryIndex);
         return subEntry.onSelected();
      } else if (!this.selectAction()) {
         return this;
      } else {
         if (!(this instanceof EditorListEntryWidget)) {
            class_310.method_1551().method_1483().method_4873(class_1109.method_47978(class_3417.field_15015, 1.0F));
         }

         this.rowList.updateEntries();
         return this;
      }
   }

   public boolean mouseClicked(GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList.Entry entry, double relativeMouseX, double relativeMouseY, int i, class_11910 buttonInfo, boolean doubleClick) {
      for(int subIndex = 0; subIndex < this.subEntries.size(); ++subIndex) {
         EditorListEntry subEntry = (EditorListEntry)this.subEntries.get(subIndex);
         if (subEntry.isHoveredOver(relativeMouseX, relativeMouseY)) {
            double subRelativeMouseX = relativeMouseX - (double)subEntry.entryRelativeX;
            double subRelativeMouseY = relativeMouseY - (double)subEntry.entryRelativeY;
            if (this.focusedSubEntryIndex != subIndex) {
               this.unfocusRecursively();
               this.focusedSubEntryIndex = subIndex;
            }

            if (!subEntry.mouseClicked(entry, subRelativeMouseX, subRelativeMouseY, subIndex, buttonInfo, doubleClick)) {
               subEntry.confirmSelection();
            }

            return true;
         }
      }

      return false;
   }

   public EditorListEntry confirmSelection() {
      return this.focusedSubEntryIndex >= 0 ? ((EditorListEntry)this.subEntries.get(this.focusedSubEntryIndex)).confirmSelection() : this.onSelected();
   }

   public boolean mouseReleased(double relativeMouseX, double relativeMouseY, int i, class_11910 buttonInfo) {
      for(EditorListEntry subEntry : this.subEntries) {
         double subRelativeMouseX = relativeMouseX - (double)subEntry.entryRelativeX;
         double subRelativeMouseY = relativeMouseY - (double)subEntry.entryRelativeY;
         subEntry.mouseReleased(subRelativeMouseX, subRelativeMouseY, i, buttonInfo);
      }

      return false;
   }

   public boolean mouseScrolled(double relativeMouseX, double relativeMouseY, double f, double g) {
      for(EditorListEntry subEntry : this.subEntries) {
         double subRelativeMouseX = relativeMouseX - (double)subEntry.entryRelativeX;
         double subRelativeMouseY = relativeMouseY - (double)subEntry.entryRelativeY;
         if (subEntry.isHoveredOver(relativeMouseX, relativeMouseY)) {
            return subEntry.mouseScrolled(subRelativeMouseX, subRelativeMouseY, f, g);
         }
      }

      return false;
   }

   public boolean mouseDragged(double relativeMouseX, double relativeMouseY, int i, double f, double g, class_11910 buttonInfo) {
      for(EditorListEntry subEntry : this.subEntries) {
         double subRelativeMouseX = relativeMouseX - (double)subEntry.entryRelativeX;
         double subRelativeMouseY = relativeMouseY - (double)subEntry.entryRelativeY;
         subEntry.mouseDragged(subRelativeMouseX, subRelativeMouseY, i, f, g, buttonInfo);
      }

      return false;
   }

   public void mouseMoved(double relativeMouseX, double relativeMouseY) {
   }

   public boolean keyPressed(class_11908 event, boolean isRoot) {
      if (isRoot) {
         if (event.comp_4795() == 263 && this.moveFocus(-1)) {
            return false;
         }

         if (event.comp_4795() == 262 && this.moveFocus(1)) {
            return false;
         }
      }

      if (!this.subEntries.isEmpty() && this.focusedSubEntryIndex >= 0) {
         EditorListEntry subEntry = (EditorListEntry)this.subEntries.get(this.focusedSubEntryIndex);
         return subEntry.keyPressed(event, false);
      } else {
         return false;
      }
   }

   public boolean keyReleased(class_11908 event) {
      if (this.subEntries.isEmpty()) {
         return false;
      } else {
         for(EditorListEntry subEntry : this.subEntries) {
            subEntry.keyReleased(event);
         }

         return false;
      }
   }

   public boolean charTyped(class_11905 event) {
      if (!this.subEntries.isEmpty() && this.focusedSubEntryIndex >= 0) {
         EditorListEntry subEntry = (EditorListEntry)this.subEntries.get(this.focusedSubEntryIndex);
         return subEntry.charTyped(event);
      } else {
         return false;
      }
   }

   public void tick() {
      if (!this.subEntries.isEmpty()) {
         for(EditorListEntry subEntry : this.subEntries) {
            subEntry.tick();
         }

      }
   }

   public String getSubNarration() {
      return this.hoveredSubEntry == null ? this.getSelectedNarration() : this.getHoveredNarration();
   }

   public String getHoveredNarration() {
      return this.hoveredSubEntry == null ? this.getHoverNarration() : this.hoveredSubEntry.getHoveredNarration();
   }

   public String getSelectedNarration() {
      if (this.focusedSubEntryIndex == -1) {
         return this.getNarration();
      } else {
         EditorListEntry subEntry = (EditorListEntry)this.subEntries.get(this.focusedSubEntryIndex);
         return subEntry.getSelectedNarration();
      }
   }

   public Supplier<Tooltip> getTooltipSupplier() {
      return this.tooltipSupplier;
   }

   public abstract class_2561 getMessage();

   public class_2561 getNarrationMessage() {
      return this.getMessage();
   }

   public String getNarration() {
      StringBuilder narrationBuilder = new StringBuilder();
      narrationBuilder.append(this.getNarrationMessage().getString());
      if (this.tooltipSupplier == null) {
         return narrationBuilder.toString();
      } else {
         Tooltip tooltip = (Tooltip)this.tooltipSupplier.get();
         if (tooltip != null) {
            narrationBuilder.append(" . ").append(((Tooltip)this.tooltipSupplier.get()).getPlainText());
         }

         return narrationBuilder.toString();
      }
   }

   public String getHoverNarration() {
      return this.getNarration();
   }

   public void preRender(class_332 guiGraphics, boolean includesSelected, boolean isRoot) {
      Matrix3x2fStack poseStack = guiGraphics.method_51448();
      poseStack.pushMatrix();
      poseStack.translate((float)this.entryRelativeX, (float)this.entryRelativeY);
      if (includesSelected && this.focusedSubEntryIndex == -1) {
         guiGraphics.method_25294(0, 0, this.entryW, this.entryH, this.rowList.method_25370() ? -1 : -8355712);
         guiGraphics.method_25294(1, 1, this.entryW - 1, this.entryH - 1, -16777216);
      }
   }

   public EditorListEntry render(class_332 guiGraphics, int index, int rowWidth, int rowHeight, int relativeMouseX, int relativeMouseY, boolean isMouseOver, float partialTicks, class_327 font, int globalMouseX, int globalMouseY, boolean includesSelected, boolean isRoot) {
      this.hoveredSubEntry = null;
      EditorListEntry result = isMouseOver ? this : null;

      for(int i = 0; i < this.subEntries.size(); ++i) {
         EditorListEntry subEntry = (EditorListEntry)this.subEntries.get(i);
         boolean subIsHovered = subEntry.isHoveredOver((double)relativeMouseX, (double)relativeMouseY);
         boolean subIncludesSelected = includesSelected && this.focusedSubEntryIndex == i;
         subEntry.preRender(guiGraphics, subIncludesSelected, false);
         EditorListEntry subResult = subEntry.render(guiGraphics, index, rowWidth, rowHeight, relativeMouseX - subEntry.entryRelativeX, relativeMouseY - subEntry.entryRelativeY, subIsHovered, partialTicks, font, globalMouseX, globalMouseY, subIncludesSelected, false);
         subEntry.postRender(guiGraphics);
         if (subIsHovered) {
            this.hoveredSubEntry = subEntry;
            result = subResult;
         }
      }

      return result;
   }

   public void postRender(class_332 guiGraphics) {
      Matrix3x2fStack poseStack = guiGraphics.method_51448();
      poseStack.popMatrix();
   }

   public boolean isHoveredOver(double relativeMouseX, double relativeMouseY) {
      return relativeMouseX >= (double)this.entryRelativeX && relativeMouseX < (double)(this.entryRelativeX + this.entryW) && relativeMouseY >= (double)this.entryRelativeY && relativeMouseY < (double)(this.entryRelativeY + this.entryH);
   }

   protected abstract boolean selectAction();

   public void setFocused(boolean bl) {
   }

   public void unhoverRecursively() {
      if (this.hoveredSubEntry != null) {
         this.hoveredSubEntry.unhoverRecursively();
         this.hoveredSubEntry = null;
      }
   }

   public boolean moveFocus(int direction) {
      this.unhoverRecursively();
      if (!this.moveFocus(direction, true)) {
         return false;
      } else {
         this.rowList.narrateSelection();
         return true;
      }
   }

   public boolean moveFocus(int direction, boolean isRoot) {
      if (this.subEntries.isEmpty()) {
         return false;
      } else {
         if (this.focusedSubEntryIndex >= 0) {
            EditorListEntry focusedSub = (EditorListEntry)this.subEntries.get(this.focusedSubEntryIndex);
            if (focusedSub.moveFocus(direction, false)) {
               return true;
            }
         }

         int potentialValue = this.focusedSubEntryIndex + direction;
         if (potentialValue < 0 || potentialValue >= this.subEntries.size()) {
            if (!isRoot) {
               return false;
            }

            potentialValue = potentialValue < 0 ? this.subEntries.size() - 1 : 0;
         }

         if (this.focusedSubEntryIndex == potentialValue) {
            return false;
         } else {
            this.focusedSubEntryIndex = potentialValue;
            EditorListEntry focusedSub = (EditorListEntry)this.subEntries.get(this.focusedSubEntryIndex);
            if (direction < 0) {
               focusedSub.focusLastRecursively();
               return true;
            } else {
               focusedSub.focusFirstRecursively();
               return true;
            }
         }
      }
   }

   public void unfocusRecursively() {
      this.setFocused(false);
      if (!this.subEntries.isEmpty()) {
         if (this.focusedSubEntryIndex >= 0) {
            ((EditorListEntry)this.subEntries.get(this.focusedSubEntryIndex)).unfocusRecursively();
         }

         this.focusedSubEntryIndex = -1;
      }
   }

   public void focusFirstRecursively() {
      this.setFocused(true);
      if (!this.subEntries.isEmpty()) {
         this.focusedSubEntryIndex = 0;
         ((EditorListEntry)this.subEntries.get(this.focusedSubEntryIndex)).focusFirstRecursively();
      }
   }

   public void focusLastRecursively() {
      this.setFocused(true);
      if (!this.subEntries.isEmpty()) {
         this.focusedSubEntryIndex = this.subEntries.size() - 1;
         ((EditorListEntry)this.subEntries.get(this.focusedSubEntryIndex)).focusLastRecursively();
      }
   }

   public EditorListEntry withSubEntry(EditorListEntry entry) {
      this.subEntries.add(entry);
      return this;
   }

   public int getEntryRelativeX() {
      return this.entryRelativeX;
   }

   public int getEntryRelativeY() {
      return this.entryRelativeY;
   }
}
