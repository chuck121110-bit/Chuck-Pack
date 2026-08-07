package xaero.hud.category.ui.entry;

import java.util.function.Supplier;
import net.minecraft.class_10799;
import net.minecraft.class_2561;
import net.minecraft.class_327;
import net.minecraft.class_332;
import xaero.hud.category.ui.GuiCategoryEditor;
import xaero.hud.render.TextureLocations;
import xaero.lib.client.gui.widget.Tooltip;

public class EditorListEntryWithIcon extends EditorListEntryWithRootReference {
   private final int iconU;
   private final int iconV;
   private final int iconW;
   private final int iconH;

   public EditorListEntryWithIcon(int entryX, int entryY, int entryW, int entryH, int index, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList, int iconU, int iconV, int iconW, int iconH, EditorListRootEntry root, Supplier<Tooltip> tooltipSupplier) {
      super(entryX, entryY, entryW, entryH, index, rowList, root, tooltipSupplier);
      this.iconU = iconU;
      this.iconV = iconV;
      this.iconW = iconW;
      this.iconH = iconH;
   }

   public int getIconX() {
      return this.iconU;
   }

   public int getIconY() {
      return this.iconV;
   }

   public int getIconW() {
      return this.iconW;
   }

   public int getIconH() {
      return this.iconH;
   }

   public EditorListEntry render(class_332 guiGraphics, int index, int rowWidth, int rowHeight, int relativeMouseX, int relativeMouseY, boolean isMouseOver, float partialTicks, class_327 font, int globalMouseX, int globalMouseY, boolean includesSelected, boolean isRoot) {
      EditorListEntry result = super.render(guiGraphics, index, rowWidth, rowHeight, relativeMouseX, relativeMouseY, isMouseOver, partialTicks, font, globalMouseX, globalMouseY, includesSelected, isRoot);
      guiGraphics.method_25290(class_10799.field_56883, TextureLocations.GUI_TEXTURES, 0, 0, (float)this.iconU, (float)this.iconV, this.iconW, this.iconH, 256, 256);
      return result;
   }

   protected boolean selectAction() {
      return false;
   }

   public class_2561 getMessage() {
      return class_2561.method_43470("unnamed");
   }
}
