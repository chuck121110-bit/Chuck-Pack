package xaero.hud.category.ui.entry;

import java.util.function.Supplier;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import xaero.hud.category.ui.GuiCategoryEditor;
import xaero.lib.client.gui.widget.Tooltip;

public class EditorListEntryWithIconAndText extends EditorListEntryWithIcon {
   protected class_2561 text;
   protected int color;
   protected int hoverColor;

   public EditorListEntryWithIconAndText(int entryX, int entryY, int entryW, int entryH, int index, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList, class_2561 text, EditorListRootEntry root, Supplier<Tooltip> tooltipSupplier) {
      super(entryX, entryY, entryW, entryH, index, rowList, 0, 0, 0, 0, root, tooltipSupplier);
      this.text = text;
      this.color = -5592406;
      this.hoverColor = -1;
   }

   public void setColor(int color) {
      this.color = color;
   }

   public void setHoverColor(int hoverColor) {
      this.hoverColor = hoverColor;
   }

   public int getColor() {
      return this.color;
   }

   public int getHoverColor() {
      return this.hoverColor;
   }

   public EditorListEntry render(class_332 guiGraphics, int index, int rowWidth, int rowHeight, int relativeMouseX, int relativeMouseY, boolean isMouseOver, float partialTicks, class_327 font, int globalMouseX, int globalMouseY, boolean includesSelected, boolean isRoot) {
      EditorListEntry result = super.render(guiGraphics, index, rowWidth, rowHeight, relativeMouseX, relativeMouseY, isMouseOver, partialTicks, font, globalMouseX, globalMouseY, includesSelected, isRoot);
      int textColor = isMouseOver ? this.getHoverColor() : this.getColor();
      guiGraphics.method_27535(font, this.text, 4, 8, textColor);
      return result;
   }

   public class_2561 getMessage() {
      return this.text;
   }
}
