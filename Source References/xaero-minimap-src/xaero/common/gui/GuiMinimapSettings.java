package xaero.common.gui;

import net.minecraft.class_11908;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_3675.class_307;
import xaero.common.HudMod;
import xaero.hud.minimap.Minimap;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.radar.category.EntityRadarCategoryManager;
import xaero.lib.client.controls.util.KeyMappingUtils;
import xaero.lib.client.gui.config.EditConfigScreen;
import xaero.lib.client.gui.config.context.IEditConfigScreenContext;
import xaero.lib.common.config.profile.ConfigProfile;

public abstract class GuiMinimapSettings extends EditConfigScreen {
   public GuiMinimapSettings(class_2561 title, class_437 par1Screen, class_437 escScreen, IEditConfigScreenContext context) {
      super(title, par1Screen, escScreen, context, HudMod.INSTANCE.getHudConfigs());
      if (!(par1Screen instanceof GuiMinimapSettings) && !(par1Screen instanceof GuiEntityRadarCategoryEditor)) {
         HudMod.INSTANCE.getEntityRadarCategoryManager().forgetEditedCategory();
      }

      this.canSkipWorldRender = false;
      this.shouldRenderEscapeScreen = false;
   }

   protected void onEditedProfileSwitch() {
      super.onEditedProfileSwitch();
      EntityRadarCategoryManager categoryManager = HudMod.INSTANCE.getEntityRadarCategoryManager();
      if (this.context.hasPermission(this.channel) && this.context.getSyncStatus(this.channel)) {
         ConfigProfile newConfig = this.getProfileOnUpdate();
         if (categoryManager.getEditedCategoryConfig() != newConfig) {
            categoryManager.loadEditedCategory(newConfig, this.context.isClientSide());
         }
      } else {
         if (categoryManager.getEditedCategory() != null) {
            categoryManager.forgetEditedCategory();
         }

      }
   }

   public void method_25394(class_332 guiGraphics, int par1, int par2, float par3) {
      super.method_25394(guiGraphics, par1, par2, par3);
      Minimap minimapInterface = HudMod.INSTANCE.getMinimap();
      boolean mapSafeMode = (Boolean)HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getEffective(MinimapProfiledConfigOptions.SAFE_MODE);
      if (!mapSafeMode && minimapInterface.getMinimapFBORenderer().isTriedFBO() && !minimapInterface.getMinimapFBORenderer().isLoadedFBO()) {
         guiGraphics.method_25300(this.field_22793, "§4You've been forced into safe mode! :(", this.field_22789 / 2, 11, 16777215);
      }

   }

   protected void handleChanges() {
      this.saveEditedCategoryIfNeeded();
      super.handleChanges();
   }

   private void saveEditedCategoryIfNeeded() {
      EntityRadarCategoryManager categoryManager = HudMod.INSTANCE.getEntityRadarCategoryManager();
      if (categoryManager.editedCategoryNeedsSaving()) {
         categoryManager.storeEditedCategory(this.context.isClientSide());
         this.handleChangesOnExit();
      }
   }

   public boolean method_25404(class_11908 event) {
      if (super.method_25404(event)) {
         return true;
      } else if ((!this.context.isClientSide() || !KeyMappingUtils.inputMatches(class_307.field_1668, event.comp_4795(), (class_304)HudMod.INSTANCE.getSettingsKey(), 0)) && (this.context.isClientSide() || !KeyMappingUtils.inputMatches(class_307.field_1668, event.comp_4795(), (class_304)HudMod.INSTANCE.getServerSettingsKey(), 0))) {
         return false;
      } else {
         this.onExit(this.escape);
         return true;
      }
   }
}
