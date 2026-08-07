package xaero.common.gui;

import java.io.IOException;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_437;
import xaero.common.HudMod;
import xaero.common.IXaeroMinimap;
import xaero.common.XaeroMinimapSession;
import xaero.hud.module.HudModule;
import xaero.hud.module.ModuleManager;
import xaero.hud.module.ModuleSession;
import xaero.hud.module.ModuleTransform;
import xaero.hud.preset.HudPreset;
import xaero.hud.pushbox.PushboxHandler;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.widget.MySmallButton;
import xaero.lib.client.gui.widget.Tooltip;

public class GuiEditMode extends ScreenBase {
   public static final class_2561 CENTERED_COMPONENT = class_2561.method_43471("gui.xaero_centered");
   public static final class_2561 FLIPPED_COMPONENT = class_2561.method_43471("gui.xaero_flipped");
   public static final class_2561 TRUE_COMPONENT = class_2561.method_43471("gui.yes");
   public static final class_2561 FALSE_COMPONENT = class_2561.method_43471("gui.no");
   public static final class_2561 PRESS_C_COMPONENT = class_2561.method_43471("gui.xaero_press_c");
   public static final class_2561 PRESS_F_COMPONENT = class_2561.method_43471("gui.xaero_press_f");
   public static final class_2561 NOT_INGAME = class_2561.method_43471("gui.xaero_not_ingame");
   private final int NORMAL_COLOR = 1354612157;
   private final int HOVERED_COLOR = 1694498815;
   private final int SELECTED_COLOR = -2097152001;
   private final boolean instructions;
   private final class_2561 message;
   private HudModule<?> draggedModule;
   private HudModule<?> selectedModule;
   private HudModule<?> lastFrameHoveredModule;
   private int dragOffsetX;
   private int dragOffsetY;
   private final IXaeroMinimap modMain;

   public GuiEditMode(IXaeroMinimap modMain, class_437 parent, class_437 escape, boolean instructions, class_2561 message) {
      super(parent, escape, class_2561.method_43471("gui.xaero_edit_mode"));
      this.modMain = modMain;
      this.instructions = instructions;
      this.message = message;
   }

   protected void method_25426() {
      super.method_25426();
      this.draggedModule = null;
      this.selectedModule = null;
      this.method_37063(new MySmallButton(200, this.field_22789 / 2 - 155, this.field_22790 / 6 + 143, class_2561.method_43471("gui.xaero_confirm"), (b) -> this.confirm()));
      this.method_37063(new MySmallButton(202, this.field_22789 / 2 + 5, this.field_22790 / 6 + 143, class_2561.method_43471("gui.xaero_choose_a_preset"), (b) -> this.field_22787.method_1507(new GuiChoosePreset(this.modMain, this, this.escape))));
      if (this.instructions) {
         this.method_37063(new MySmallButton(201, this.field_22789 / 2 + 5, this.field_22790 / 6 + 168, class_2561.method_43471("gui.xaero_cancel"), (b) -> this.cancel()));
         this.method_37063(new MySmallButton(203, this.field_22789 / 2 - 155, this.field_22790 / 6 + 168, class_2561.method_43471("gui.xaero_instructions"), (b) -> this.field_22787.method_1507(new GuiInstructions(this.modMain, this, this.escape))));
      } else {
         this.method_37063(new MySmallButton(201, this.field_22789 / 2 + 5, this.field_22790 / 6 + 168, class_2561.method_43471("gui.xaero_cancel"), (b) -> this.cancel()));
      }
   }

   private void confirm() {
      for(HudPreset preset : this.modMain.getHud().getPresetManager().getPresets()) {
         preset.confirm();
      }

      ModuleManager manager = this.modMain.getHud().getModuleManager();

      for(HudModule<?> module : manager.getModules()) {
         module.confirmTransform();
      }

      try {
         this.modMain.getSettings().saveSettings();
      } catch (IOException e) {
         HudMod.LOGGER.error("suppressed exception", e);
      }

      this.field_22787.method_1507(this.parent);
   }

   private void cancel() {
      for(HudPreset preset : this.modMain.getHud().getPresetManager().getPresets()) {
         preset.cancel();
      }

      ModuleManager manager = this.modMain.getHud().getModuleManager();

      for(HudModule<?> module : manager.getModules()) {
         module.cancelTransform();
      }

      this.goBack();
   }

   public void method_25420(class_332 guiGraphics, int mouseX, int mouseY, float partial) {
      if (this.field_22787.field_1724 == null) {
         super.method_25420(guiGraphics, mouseX, mouseY, partial);
         guiGraphics.method_27534(this.field_22793, NOT_INGAME, this.field_22789 / 2, this.field_22790 / 6 + 128, -1);
      } else {
         guiGraphics.method_27534(this.field_22793, this.message, this.field_22789 / 2, this.field_22790 / 6 + 128, -1);
      }

   }

   private void applyPushes() {
      double screenScale = (double)this.field_22787.method_22683().method_4495();
      ModuleManager manager = this.modMain.getHud().getModuleManager();

      for(HudModule<?> module : manager.getModules()) {
         ModuleSession<?> session = module.getCurrentSession();
         if (session.isActive()) {
            PushboxHandler.State pushState = module.getPushState();
            pushState.resetForModule(session, this.field_22789, this.field_22790, screenScale);
            this.modMain.getHudRenderer().getPushboxHandler().applyScreenEdges(pushState, this.field_22789, this.field_22790, screenScale);
         }
      }

   }

   public void method_25394(class_332 guiGraphics, int mouseX, int mouseY, float partialTicks) {
      if (XaeroMinimapSession.getCurrentSession() == null) {
         super.method_25394(guiGraphics, mouseX, mouseY, partialTicks);
      } else {
         double screenScale = (double)this.field_22787.method_22683().method_4495();
         this.handleDraggedModule(mouseX, mouseY, screenScale);
         this.applyPushes();
         ModuleManager manager = this.modMain.getHud().getModuleManager();
         HudModule<?> hoveredModule = this.lastFrameHoveredModule = this.getHoveredModule(mouseX, mouseY);

         for(HudModule<?> module : manager.getModules()) {
            if (module.getCurrentSession().isActive()) {
               this.renderModuleBox(module, hoveredModule, screenScale, guiGraphics);
            }
         }

         super.method_25394(guiGraphics, mouseX, mouseY, partialTicks);
         if (this.openDropdown == null || !this.openDropdown.method_49606()) {
            if (hoveredModule != null && this.draggedModule == null) {
               Tooltip tooltip = new Tooltip(this.getTooltipText(hoveredModule));
               tooltip.setStartWidth(150);
               tooltip.drawBox(guiGraphics, mouseX, mouseY, this.field_22789, this.field_22790);
            }
         }
      }
   }

   private <MS extends ModuleSession<MS>> void renderModuleBox(HudModule<MS> module, HudModule<?> hoveredModule, double screenScale, class_332 guiGraphics) {
      MS session = module.getCurrentSession();
      int moduleW = session.getWidth(screenScale);
      int moduleH = session.getHeight(screenScale);
      boolean hovered = hoveredModule == module;
      PushboxHandler.State pushState = module.getPushState();
      int boxX = pushState.x;
      int boxY = pushState.y;
      guiGraphics.method_25294(boxX, boxY, boxX + moduleW, boxY + moduleH, this.selectedModule == module ? -2097152001 : (hovered ? 1694498815 : 1354612157));
   }

   private HudModule<?> getHoveredModule(int mouseX, int mouseY) {
      ModuleManager manager = this.modMain.getHud().getModuleManager();
      HudModule<?> result = null;
      int resultSize = 0;

      for(HudModule<?> module : manager.getModules()) {
         if (module.getCurrentSession().isActive() && this.isHovered(module, mouseX, mouseY)) {
            int moduleW = module.getPushState().w;
            int moduleH = module.getPushState().h;
            if (module == this.selectedModule || result == null || moduleW * moduleH <= resultSize) {
               result = module;
               resultSize = moduleW * moduleH;
            }
         }
      }

      return result;
   }

   private boolean isHovered(HudModule<?> module, int mouseX, int mouseY) {
      PushboxHandler.State pushState = module.getPushState();
      int boxX = pushState.x;
      int boxY = pushState.y;
      return mouseX >= boxX && mouseX < boxX + pushState.w && mouseY >= boxY && mouseY < boxY + pushState.h;
   }

   public boolean method_25402(class_11909 event, boolean doubleClick) {
      boolean clickResult = super.method_25402(event, doubleClick);
      if (!clickResult && XaeroMinimapSession.getCurrentSession() != null) {
         this.applyPushes();
         double mouseX = event.comp_4798();
         double mouseY = event.comp_4799();
         this.draggedModule = this.getHoveredModule((int)mouseX, (int)mouseY);
         this.selectedModule = this.draggedModule;
         if (this.draggedModule != null) {
            this.dragOffsetX = this.draggedModule.getPushState().x - (int)mouseX;
            this.dragOffsetY = this.draggedModule.getPushState().y - (int)mouseY;
            return true;
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   public boolean method_25406(class_11909 event) {
      this.draggedModule = null;
      return super.method_25406(event);
   }

   public boolean method_25404(class_11908 event) {
      int code = event.comp_4795();
      HudModule<?> affectedModule = this.selectedModule != null ? this.selectedModule : this.lastFrameHoveredModule;
      if (affectedModule != null && (code == 67 || code == 70 || code == 83)) {
         ModuleTransform transform = affectedModule.getUnconfirmedTransform();
         switch (code) {
            case 67:
               int oldModuleY = affectedModule.getCurrentSession().getEffectiveY(this.field_22790, (double)this.field_22787.method_22683().method_4495());
               transform.centered = !transform.centered;
               if (this.draggedModule != null) {
                  int newModuleY = affectedModule.getCurrentSession().getEffectiveY(this.field_22790, (double)this.field_22787.method_22683().method_4495());
                  this.dragOffsetY += newModuleY - oldModuleY;
               }
               break;
            case 70:
               boolean curFlippedHor = transform.flippedHor;
               transform.flippedHor = !transform.flippedVer;
               transform.flippedVer = curFlippedHor;
               break;
            case 83:
               class_437 configScreen = (class_437)affectedModule.getConfigScreenFactory().apply(this);
               if (configScreen != null) {
                  this.field_22787.method_1507(configScreen);
               }
         }
      }

      return super.method_25404(event);
   }

   private void handleDraggedModule(int mouseX, int mouseY, double screenScale) {
      if (this.draggedModule != null) {
         ModuleSession<?> session = this.draggedModule.getCurrentSession();
         if (session != null) {
            ModuleTransform transform = this.draggedModule.getUnconfirmedTransform();
            transform.y = mouseY + this.dragOffsetY;
            transform.fromBottom = false;
            int moduleH = session.getHeight(screenScale);
            int yFromBottom = this.field_22790 - transform.y - moduleH;
            if (transform.y > yFromBottom) {
               transform.fromBottom = true;
               transform.y = yFromBottom;
            }

            if (!transform.centered) {
               transform.x = mouseX + this.dragOffsetX;
               transform.fromRight = false;
               int moduleW = session.getWidth(screenScale);
               int xFromRight = this.field_22789 - transform.x - moduleW;
               if (transform.x > xFromRight) {
                  transform.fromRight = true;
                  transform.x = xFromRight;
               }

            }
         }
      }
   }

   private class_2561 getTooltipText(HudModule<?> hoveredModule) {
      ModuleTransform transform = hoveredModule.getUnconfirmedTransform();
      class_2561 centeredLine = class_2561.method_43469("%s %s %s", new Object[]{CENTERED_COMPONENT, transform.centered ? TRUE_COMPONENT : FALSE_COMPONENT, PRESS_C_COMPONENT});
      class_2561 flippedLine = class_2561.method_43469("%s %s %s %s", new Object[]{FLIPPED_COMPONENT, transform.flippedHor ? TRUE_COMPONENT : FALSE_COMPONENT, transform.flippedVer ? TRUE_COMPONENT : FALSE_COMPONENT, PRESS_F_COMPONENT});
      return class_2561.method_43469("%s \n %s \n %s", new Object[]{hoveredModule.getDisplayName(), centeredLine, flippedLine});
   }
}
