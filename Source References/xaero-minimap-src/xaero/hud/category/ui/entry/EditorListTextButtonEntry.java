package xaero.hud.category.ui.entry;

import java.util.function.Supplier;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import xaero.hud.category.ui.GuiCategoryEditor;
import xaero.lib.client.gui.widget.Tooltip;

public class EditorListTextButtonEntry extends EditorListEntryWithRootReference {
   private final class_2561 text;
   private final int color;
   private final int hoverColor;
   private final int frameSize;
   private final Supplier<Boolean> action;

   public EditorListTextButtonEntry(int entryX, int entryY, int index, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList, class_2561 text, int color, int hoverColor, int frameSize, Supplier<Boolean> action, EditorListRootEntry root, Supplier<Tooltip> tooltipSupplier) {
      super(entryX - class_310.method_1551().field_1772.method_27525(text) / 2 - frameSize, entryY, class_310.method_1551().field_1772.method_27525(text) + frameSize * 2, 9 + frameSize * 2, index, rowList, root, tooltipSupplier);
      this.text = text;
      this.color = color;
      this.hoverColor = hoverColor;
      this.frameSize = frameSize;
      this.action = action;
   }

   public EditorListEntry render(class_332 guiGraphics, int index, int rowWidth, int rowHeight, int relativeMouseX, int relativeMouseY, boolean isMouseOver, float partialTicks, class_327 font, int globalMouseX, int globalMouseY, boolean includesSelected, boolean isRoot) {
      EditorListEntry result = super.render(guiGraphics, index, rowWidth, rowHeight, relativeMouseX, relativeMouseY, isMouseOver, partialTicks, font, globalMouseX, globalMouseY, includesSelected, isRoot);
      int textX = this.frameSize + class_310.method_1551().field_1772.method_27525(this.text) / 2;
      guiGraphics.method_27534(font, this.text, textX, this.frameSize + 1, isMouseOver ? this.hoverColor : this.color);
      return result;
   }

   protected boolean selectAction() {
      return (Boolean)this.action.get();
   }

   public class_2561 getMessage() {
      return this.text;
   }
}
