package xaeroplus.feature.extensions;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_350;
import net.minecraft.class_364;
import net.minecraft.class_4185;
import net.minecraft.class_4280;
import net.minecraft.class_437;
import xaeroplus.module.impl.TickTaskExecutor;
import xaeroplus.settings.SettingHooks;
import xaeroplus.settings.Settings;
import xaeroplus.util.ColorHelper;
import xaeroplus.util.DrawOrderHelper;

public class DrawOrderScreen extends class_437 {
   static class_310 mc = class_310.method_1551();
   class_437 parent;
   DrawFeatureList drawFeatureList;
   List<String> drawFeatureIdOrder;
   int selected;

   public DrawOrderScreen(class_437 parent, class_437 escapeScreen) {
      super(class_2561.method_43471("xaeroplus.gui.draw_order.title"));
      this.parent = parent;
      this.drawFeatureIdOrder = new ArrayList();
      this.selected = -1;
   }

   public void method_25426() {
      this.drawFeatureIdOrder = this.loadEntries();
      this.drawFeatureList = new DrawFeatureList(this);
      this.method_37063(class_4185.method_46430(class_2561.method_43471("gui.done"), (b) -> {
         this.method_25419();
         mc.method_1507(this.parent);
      }).method_46434(this.field_22789 / 2 - 100, this.field_22790 - 34, 200, 20).method_46431());
      this.method_37063(class_4185.method_46430(class_2561.method_43471("xaeroplus.gui.draw_order.reset"), (b) -> TickTaskExecutor.INSTANCE.execute(() -> {
            Settings.REGISTRY.drawOrderSetting.setValue("");
            this.method_25423(this.field_22789, this.field_22790);
         })).method_46434(this.field_22789 - 82, 2, 80, 20).method_46431());
      this.method_25429(this.drawFeatureList);
      if (!this.drawFeatureIdOrder.isEmpty()) {
         this.selected = 0;
         this.drawFeatureList.method_25395(this.drawFeatureList.getFirstElement());
      }

   }

   public boolean method_25406(class_11909 event) {
      if (this.drawFeatureList != null) {
         this.drawFeatureList.releaseDrag();
      }

      return super.method_25406(event);
   }

   public void method_25394(class_332 guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.drawFeatureList.method_25394(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.method_27534(mc.field_1772, this.field_22785, this.field_22789 / 2, 5, ColorHelper.getColor(255, 255, 255, 255));
      guiGraphics.method_27534(mc.field_1772, class_2561.method_43471("xaeroplus.gui.draw_order.subtitle"), this.field_22789 / 2, this.field_22790 - 52, ColorHelper.getColor(255, 255, 255, 255));
      super.method_25394(guiGraphics, mouseX, mouseY, partialTick);
   }

   public void method_25420(class_332 guiGraphics, int mouseX, int mouseY, float partialTick) {
   }

   public void method_25419() {
      SettingHooks.saveSettings();
   }

   public List<String> loadEntries() {
      return DrawOrderHelper.load();
   }

   public void saveEntries(List<String> entries) {
      Settings.REGISTRY.drawOrderSetting.setValue(DrawOrderHelper.serialize(entries));
   }

   public static class DrawFeatureList extends class_4280<DrawFeatureEntry> {
      boolean dragging;
      int dragStartX;
      int dragStartY;
      int dragged;
      int draggedOffsetX;
      int draggedOffsetY;
      DrawOrderScreen drawOrderScreen;

      public DrawFeatureList(DrawOrderScreen drawOrderScreen) {
         super(DrawOrderScreen.mc, drawOrderScreen.field_22789, drawOrderScreen.field_22790 - 91, 30, 24);
         this.drawOrderScreen = drawOrderScreen;
         this.dragged = -1;
         this.createEntries().forEach((x$0) -> this.method_25321(x$0));
         if (drawOrderScreen.selected != -1) {
            this.method_25395(this.getEntry(drawOrderScreen.selected));
         }

      }

      public DrawFeatureEntry getEntry(int index) {
         return index >= 0 && index < this.method_25396().size() ? (DrawFeatureEntry)this.method_25396().get(index) : null;
      }

      public DrawFeatureEntry getFirstElement() {
         return this.getEntry(0);
      }

      public boolean method_25370() {
         return this.drawOrderScreen.method_25399() == this;
      }

      public void method_25395(class_364 guiEventListener) {
         if (guiEventListener instanceof DrawFeatureEntry entry) {
            this.drawOrderScreen.selected = entry.index;
         }

         if (guiEventListener == null) {
            this.drawOrderScreen.selected = -1;
         }

         super.method_25395(guiEventListener);
         if (this.method_25336() == null) {
            this.method_25313((class_350.class_351)null);
         }

      }

      List<DrawFeatureEntry> createEntries() {
         List<DrawFeatureEntry> entries = new ArrayList();

         for(int i = 0; i < this.drawOrderScreen.drawFeatureIdOrder.size(); ++i) {
            DrawFeatureEntry entry = new DrawFeatureEntry(this.drawOrderScreen, this, i);
            entries.add(entry);
         }

         return entries;
      }

      void releaseDrag() {
         this.dragging = false;
         this.dragged = -1;
         this.drawOrderScreen.saveEntries(this.drawOrderScreen.drawFeatureIdOrder);
      }

      public int method_65507() {
         return this.field_22758 / 2 + 164;
      }

      public int method_25322() {
         return 300;
      }

      public void method_48579(class_332 guiGraphics, int mouseX, int mouseY, float partialTicks) {
         this.renderBackdrop(guiGraphics);
         super.method_48579(guiGraphics, mouseX, mouseY, partialTicks);
         if (this.dragging) {
            DrawFeatureEntry draggedEntry = this.getEntry(this.dragged);
            draggedEntry.renderEntryText(guiGraphics, mouseX + this.draggedOffsetX, mouseY + this.draggedOffsetY);
            DrawFeatureEntry hoveredEntry = (DrawFeatureEntry)this.method_25308((double)mouseX, (double)mouseY);
            int hoveredIndex = hoveredEntry == null ? -1 : hoveredEntry.index;
            if (hoveredIndex != -1 && hoveredIndex != this.dragged) {
               String draggedId = (String)this.drawOrderScreen.drawFeatureIdOrder.get(this.dragged);
               int slideDirection = hoveredIndex < this.dragged ? 1 : -1;

               for(int i = this.dragged; i != hoveredIndex; i -= slideDirection) {
                  this.drawOrderScreen.drawFeatureIdOrder.set(i, (String)this.drawOrderScreen.drawFeatureIdOrder.get(i - slideDirection));
               }

               this.drawOrderScreen.drawFeatureIdOrder.set(hoveredIndex, draggedId);
               this.dragged = hoveredIndex;
            }
         } else if (this.dragged != -1 && (Math.abs(mouseX - this.dragStartX) > 5 || Math.abs(mouseY - this.dragStartY) > 5)) {
            this.dragging = true;
            this.method_25395((class_364)null);
         }

      }

      public void renderBackdrop(class_332 guiGraphics) {
         guiGraphics.method_25294(0, 0, this.drawOrderScreen.field_22789, this.drawOrderScreen.field_22790, ColorHelper.getColor(0, 0, 0, 100));
      }

      public void method_57715(class_332 guiGraphics) {
      }

      public void method_57713(class_332 guiGraphics) {
      }
   }

   public static class DrawFeatureEntry extends class_4280.class_4281<DrawFeatureEntry> {
      DrawOrderScreen drawOrderScreen;
      DrawFeatureList drawFeatureList;
      int index;
      int lastRenderX;
      int lastRenderY;
      int lastMouseX;
      int lastMouseY;

      public DrawFeatureEntry(DrawOrderScreen drawOrderScreen, DrawFeatureList drawFeatureList, int index) {
         this.drawOrderScreen = drawOrderScreen;
         this.drawFeatureList = drawFeatureList;
         this.index = index;
      }

      public void renderEntryText(class_332 guiGraphics, int x, int y) {
         String id = (String)this.drawOrderScreen.drawFeatureIdOrder.get(this.index);
         guiGraphics.method_25303(DrawOrderScreen.mc.field_1772, id, x + 6, y + 6, ColorHelper.getColor(255, 255, 255, 255));
      }

      public class_2561 method_37006() {
         return class_2561.method_43473();
      }

      public void method_25343(class_332 guiGraphics, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
         if (this.drawFeatureList.method_25334() != this) {
            guiGraphics.method_25294(this.method_73380(), this.method_73382(), this.method_73380() + this.method_73387(), this.method_73382() + this.method_73384(), ColorHelper.getColor(68, 68, 68, 150));
            guiGraphics.method_25294(this.method_73380() + 1, this.method_73382() + 1, this.method_73380() + this.method_73387() - 1, this.method_73382() + this.method_73384() - 1, ColorHelper.getColor(0, 0, 0, 150));
         }

         this.lastRenderX = this.method_46426();
         this.lastRenderY = this.method_46427();
         this.lastMouseX = mouseX;
         this.lastMouseY = mouseY;
         if (!this.drawFeatureList.dragging || this.drawFeatureList.dragged != this.index) {
            this.renderEntryText(guiGraphics, this.method_46426(), this.method_46427() + 2);
         }

      }

      public boolean method_25402(class_11909 event, boolean doubleClick) {
         if (event.method_74245() == 0) {
            this.drawFeatureList.dragging = false;
            this.drawFeatureList.dragged = this.index;
            this.drawFeatureList.draggedOffsetX = (int)((double)this.lastRenderX - event.comp_4798());
            this.drawFeatureList.draggedOffsetY = (int)((double)this.lastRenderY - event.comp_4799());
            this.drawFeatureList.dragStartX = (int)event.comp_4798();
            this.drawFeatureList.dragStartY = (int)event.comp_4799();
            if (this.drawFeatureList.method_25334() != this) {
               return true;
            }

            this.drawFeatureList.method_25395((class_364)null);
         } else {
            this.drawFeatureList.method_25395((class_364)null);
         }

         return super.method_25402(event, doubleClick);
      }

      public void method_16014(double mouseX, double mouseY) {
         this.lastMouseX = (int)mouseX;
         this.lastMouseY = (int)mouseY;
         super.method_16014(mouseX, mouseY);
      }

      public boolean method_25403(class_11909 event, double dragX, double dragY) {
         this.lastMouseX = (int)event.comp_4798();
         this.lastMouseY = (int)event.comp_4799();
         return super.method_25403(event, dragX, dragY);
      }
   }
}
