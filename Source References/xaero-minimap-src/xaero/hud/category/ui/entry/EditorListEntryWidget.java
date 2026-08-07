package xaero.hud.category.ui.entry;

import java.util.function.Supplier;
import net.minecraft.class_1109;
import net.minecraft.class_11905;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_11910;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_3417;
import net.minecraft.class_357;
import net.minecraft.class_4264;
import org.joml.Matrix3x2fStack;
import org.joml.Vector3f;
import xaero.hud.category.ui.GuiCategoryEditor;
import xaero.hud.category.ui.entry.widget.EditorTextField;
import xaero.lib.client.gui.widget.Tooltip;

public class EditorListEntryWidget extends EditorListEntryWithRootReference {
   protected class_339 widget;
   private boolean widgetPressed;

   public EditorListEntryWidget(int entryX, int entryY, int entryW, int entryH, int index, GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList rowList, EditorListRootEntry root, class_339 widget, Supplier<Tooltip> tooltipSupplier) {
      super(entryX, entryY, entryW, entryH, index, rowList, root, tooltipSupplier);
      this.widget = widget;
   }

   public boolean mouseClicked(GuiCategoryEditor<?, ?, ?, ?, ?, ?>.SettingRowList.Entry entry, double relativeMouseX, double relativeMouseY, int i, class_11910 buttonInfo, boolean doubleClick) {
      boolean result = super.mouseClicked(entry, relativeMouseX, relativeMouseY, i, buttonInfo, doubleClick);
      if (result) {
         return true;
      } else if (this.widget instanceof class_4264) {
         return false;
      } else if (!this.widget.method_25405(relativeMouseX, relativeMouseY)) {
         return false;
      } else {
         this.widgetPressed = true;
         return this.widget.method_25402(new class_11909(relativeMouseX, relativeMouseY, buttonInfo), doubleClick);
      }
   }

   public boolean mouseReleased(double relativeMouseX, double relativeMouseY, int i, class_11910 buttonInfo) {
      if (this.widgetPressed) {
         this.widget.method_25406(new class_11909(relativeMouseX, relativeMouseY, buttonInfo));
      }

      this.widgetPressed = false;
      super.mouseReleased(relativeMouseX, relativeMouseY, i, buttonInfo);
      return false;
   }

   public boolean mouseDragged(double relativeMouseX, double relativeMouseY, int i, double f, double g, class_11910 buttonInfo) {
      return this.widgetPressed && this.widget.method_25403(new class_11909(relativeMouseX, relativeMouseY, buttonInfo), f, g) ? true : super.mouseDragged(relativeMouseX, relativeMouseY, i, f, g, buttonInfo);
   }

   public boolean mouseScrolled(double relativeMouseX, double relativeMouseY, double f, double g) {
      return this.widget.method_25405(relativeMouseX, relativeMouseY) && this.widget.method_25401(relativeMouseX, relativeMouseY, f, g) ? true : super.mouseScrolled(relativeMouseX, relativeMouseY, f, g);
   }

   public void mouseMoved(double relativeMouseX, double relativeMouseY) {
      this.widget.method_16014(relativeMouseX, relativeMouseY);
      super.mouseMoved(relativeMouseX, relativeMouseY);
   }

   public boolean keyPressed(class_11908 event, boolean isRoot) {
      return this.widget.method_25404(event) ? true : super.keyPressed(event, isRoot);
   }

   public boolean keyReleased(class_11908 event) {
      return this.widget.method_16803(event) ? true : super.keyReleased(event);
   }

   public boolean charTyped(class_11905 event) {
      return this.widget.method_25400(event) ? true : super.charTyped(event);
   }

   public void tick() {
      super.tick();
   }

   public String getNarration() {
      return super.getNarration();
   }

   public String getHoverNarration() {
      return this.getNarration();
   }

   public class_2561 getMessage() {
      return this.widget.method_25369();
   }

   public class_2561 getNarrationMessage() {
      if (this.widget instanceof EditorTextField) {
         return ((EditorTextField)this.widget).method_25360();
      } else {
         return this.widget instanceof class_357 ? class_2561.method_43469("gui.narrate.slider", new Object[]{this.getMessage()}) : class_2561.method_43469("gui.narrate.button", new Object[]{this.getMessage()});
      }
   }

   public EditorListEntry render(class_332 guiGraphics, int index, int rowWidth, int rowHeight, int relativeMouseX, int relativeMouseY, boolean isMouseOver, float partialTicks, class_327 font, int globalMouseX, int globalMouseY, boolean includesSelected, boolean isRoot) {
      EditorListEntry result = super.render(guiGraphics, index, rowWidth, rowHeight, relativeMouseX, relativeMouseY, isMouseOver, partialTicks, font, globalMouseX, globalMouseY, includesSelected, isRoot);
      Matrix3x2fStack poseStack = guiGraphics.method_51448();
      Vector3f widgetPos = new Vector3f((float)this.widget.method_46426(), (float)this.widget.method_46427(), 1.0F);
      widgetPos.mul(poseStack);
      int xBU = this.widget.method_46426();
      int yBU = this.widget.method_46427();
      this.widget.method_46421((int)widgetPos.x());
      this.widget.method_46419((int)widgetPos.y());
      poseStack.pushMatrix();
      poseStack.identity();
      this.widget.method_25394(guiGraphics, globalMouseX, globalMouseY, partialTicks);
      poseStack.popMatrix();
      this.widget.method_46421(xBU);
      this.widget.method_46419(yBU);
      return this.widgetPressed ? null : result;
   }

   public void setFocused(boolean bl) {
      if (this.widget.field_22763 && this.widget.field_22764 && this.widget.method_25370() != bl) {
         this.widget.method_25365(bl);
      }

      super.setFocused(bl);
   }

   protected boolean selectAction() {
      if (this.widget instanceof class_4264 && this.widget.field_22763) {
         ((class_4264)this.widget).method_25306(new class_11908(257, 0, 0));
         class_310.method_1551().method_1483().method_4873(class_1109.method_47978(class_3417.field_15015, 1.0F));
         return false;
      } else {
         return false;
      }
   }
}
