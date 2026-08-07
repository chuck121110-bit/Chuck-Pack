package xaero.common.gui;

import net.minecraft.class_1074;
import net.minecraft.class_11908;
import net.minecraft.class_11909;
import net.minecraft.class_11910;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_437;
import xaero.common.HudMod;
import xaero.common.IXaeroMinimap;
import xaero.common.settings.ModSettings;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.world.container.MinimapWorldRootContainer;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.client.graphics.util.TextureUtils;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.widget.MySmallButton;
import xaero.lib.client.render.util.GuiRenderUtil;

public class GuiWorldTpCommand extends ScreenBase {
   private MySmallButton confirmButton;
   private class_342 commandFormatTextField;
   private class_342 rotationCommandFormatTextField;
   private boolean usingDefault;
   private String commandFormat;
   private String rotationCommandFormat;
   private MinimapWorldRootContainer rootContainer;

   public GuiWorldTpCommand(IXaeroMinimap modMain, class_437 parent, class_437 escape, MinimapWorldRootContainer rootContainer) {
      super(parent, escape, class_2561.method_43471("gui.xaero_world_teleport_command"));
      this.rootContainer = rootContainer;
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      String defaultWaypointTPCommandFormatConfig = (String)configManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_DEFAULT_TELEPORT_FORMAT);
      String defaultWaypointTPCommandRotationFormatConfig = (String)configManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_DEFAULT_TELEPORT_ROTATION_FORMAT);
      this.commandFormat = rootContainer.getConfig().getServerTeleportCommandFormat() == null ? defaultWaypointTPCommandFormatConfig : rootContainer.getConfig().getServerTeleportCommandFormat();
      this.rotationCommandFormat = rootContainer.getConfig().getServerTeleportCommandRotationFormat() == null ? defaultWaypointTPCommandRotationFormatConfig : rootContainer.getConfig().getServerTeleportCommandRotationFormat();
      this.usingDefault = rootContainer.getConfig().isUsingDefaultTeleportCommand();
   }

   public void method_25426() {
      super.method_25426();
      this.parent.method_25410(this.field_22789, this.field_22790);
      this.commandFormatTextField = new class_342(this.field_22793, this.field_22789 / 2 - 100, this.field_22790 / 7 + 50, 200, 20, class_2561.method_43471("gui.xaero_world_teleport_command")) {
         public void method_1867(String textToWrite) {
            if (this.field_22763) {
               super.method_1867(textToWrite);
            }

         }

         public boolean method_25402(class_11909 event, boolean doubleClick) {
            return this.field_22763 ? super.method_25402(event, doubleClick) : false;
         }

         public void method_1878(int p_146175_1_) {
            if (this.field_22763) {
               super.method_1878(p_146175_1_);
            }

         }

         public void method_1877(int p_146177_1_) {
            if (this.field_22763) {
               super.method_1877(p_146177_1_);
            }

         }
      };
      this.commandFormatTextField.method_1880(128);
      this.rotationCommandFormatTextField = new class_342(this.field_22793, this.field_22789 / 2 - 100, this.field_22790 / 7 + 98, 200, 20, class_2561.method_43471("gui.xaero_world_teleport_command_with_rotation")) {
         public void method_1867(String textToWrite) {
            if (this.field_22763) {
               super.method_1867(textToWrite);
            }

         }

         public boolean method_25402(class_11909 event, boolean doubleClick) {
            return this.field_22763 ? super.method_25402(event, doubleClick) : false;
         }

         public void method_1878(int p_146175_1_) {
            if (this.field_22763) {
               super.method_1878(p_146175_1_);
            }

         }

         public void method_1877(int p_146177_1_) {
            if (this.field_22763) {
               super.method_1877(p_146177_1_);
            }

         }
      };
      this.rotationCommandFormatTextField.method_1880(128);
      this.commandFormatTextField.field_22763 = !this.usingDefault;
      this.rotationCommandFormatTextField.field_22763 = !this.usingDefault;
      this.commandFormatTextField.method_1852(this.commandFormat);
      this.rotationCommandFormatTextField.method_1852(this.rotationCommandFormat);
      this.method_25429(this.commandFormatTextField);
      this.method_25429(this.rotationCommandFormatTextField);
      this.method_37063(this.confirmButton = new MySmallButton(200, this.field_22789 / 2 - 155, this.field_22790 / 6 + 168, class_2561.method_43469("gui.xaero_confirm", new Object[0]), (b) -> {
         ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
         String defaultWaypointTPCommandFormatConfig = (String)configManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_DEFAULT_TELEPORT_FORMAT);
         String defaultWaypointTPCommandRotationFormatConfig = (String)configManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_DEFAULT_TELEPORT_ROTATION_FORMAT);
         if (this.commandFormat.equals(defaultWaypointTPCommandFormatConfig) && this.rotationCommandFormat.equals(defaultWaypointTPCommandRotationFormatConfig)) {
            this.usingDefault = true;
            this.commandFormat = null;
            this.rotationCommandFormat = null;
         }

         this.rootContainer.getConfig().setUsingDefaultTeleportCommand(this.usingDefault);
         this.rootContainer.getConfig().setServerTeleportCommandFormat(this.commandFormat);
         this.rootContainer.getConfig().setServerTeleportCommandRotationFormat(this.rotationCommandFormat);
         this.rootContainer.getSession().getWorldManagerIO().getRootConfigIO().save(this.rootContainer);
         this.goBack();
      }));
      this.method_37063(new MySmallButton(201, this.field_22789 / 2 + 5, this.field_22790 / 6 + 168, class_2561.method_43469("gui.xaero_cancel", new Object[0]), (b) -> this.goBack()));
      int var10004 = this.field_22789 / 2 - 75;
      int var10005 = this.field_22790 / 7 + 8;
      String var10006 = class_1074.method_4662("gui.xaero_use_default", new Object[0]);
      this.method_37063(new MySmallButton(202, var10004, var10005, class_2561.method_43470(var10006 + ": " + ModSettings.getTranslation(this.usingDefault)), (b) -> {
         this.usingDefault = !this.usingDefault;
         this.commandFormatTextField.field_22763 = !this.usingDefault;
         this.rotationCommandFormatTextField.field_22763 = !this.usingDefault;
         this.method_25423(this.field_22789, this.field_22790);
      }));
   }

   public void method_25420(class_332 guiGraphics, int i, int j, float f) {
      if (this.parent instanceof GuiWaypointsOptions) {
         ((GuiWaypointsOptions)this.parent).parent.method_47413(guiGraphics, 0, 0, f);
         GuiRenderUtil.flushGUI();
      }

      TextureUtils.clearRenderTargetDepth(this.field_22787.method_1522(), 1.0F);
      super.method_25420(guiGraphics, i, j, f);
      GuiRenderUtil.flushGUI();
      super.method_25420(guiGraphics, i, j, f);
      guiGraphics.method_27534(this.field_22793, this.field_22785, this.field_22789 / 2, 20, -1);
      guiGraphics.method_25300(this.field_22793, "{x} {y} {z} {name}", this.field_22789 / 2, this.field_22790 / 7 + 36, -5592406);
      guiGraphics.method_25300(this.field_22793, "{x} {y} {z} {name} {yaw}", this.field_22789 / 2, this.field_22790 / 7 + 84, -5592406);
   }

   public void method_25394(class_332 guiGraphics, int mouseX, int mouseY, float partial) {
      super.method_25394(guiGraphics, mouseX, mouseY, partial);
      if (this.usingDefault) {
         ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
         String defaultWaypointTPCommandFormatConfig = (String)configManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_DEFAULT_TELEPORT_FORMAT);
         String defaultWaypointTPCommandRotationFormatConfig = (String)configManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_DEFAULT_TELEPORT_ROTATION_FORMAT);
         this.commandFormatTextField.method_1852(defaultWaypointTPCommandFormatConfig);
         this.rotationCommandFormatTextField.method_1852(defaultWaypointTPCommandRotationFormatConfig);
         this.commandFormatTextField.method_1868(-11184811);
         this.rotationCommandFormatTextField.method_1868(-11184811);
      }

      this.commandFormatTextField.method_25394(guiGraphics, mouseX, mouseY, partial);
      this.rotationCommandFormatTextField.method_25394(guiGraphics, mouseX, mouseY, partial);
      if (this.usingDefault) {
         this.commandFormatTextField.method_1852(this.commandFormat);
         this.rotationCommandFormatTextField.method_1852(this.rotationCommandFormat);
         this.commandFormatTextField.method_1868(-1);
         this.rotationCommandFormatTextField.method_1868(-1);
      }

   }

   public void method_25393() {
      this.commandFormat = this.commandFormatTextField.method_1882();
      this.rotationCommandFormat = this.rotationCommandFormatTextField.method_1882();
      this.confirmButton.field_22763 = this.commandFormat != null && this.commandFormat.length() > 0 && this.rotationCommandFormat != null && this.rotationCommandFormat.length() > 0 || this.usingDefault;
   }

   public boolean method_25404(class_11908 event) {
      if (event.comp_4795() == 257 && (this.commandFormatTextField.method_25370() || this.rotationCommandFormatTextField.method_25370()) && this.commandFormat != null && this.commandFormat.length() > 0 && this.rotationCommandFormat != null && this.rotationCommandFormat.length() > 0) {
         this.confirmButton.method_25348(new class_11909((double)0.0F, (double)0.0F, new class_11910(0, 0)), false);
      }

      return super.method_25404(event);
   }
}
