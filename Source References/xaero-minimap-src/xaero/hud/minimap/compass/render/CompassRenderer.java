package xaero.hud.minimap.compass.render;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import xaero.common.HudMod;
import xaero.common.minimap.render.MinimapRendererHelper;
import xaero.common.misc.Misc;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.element.render.over.MinimapElementOverMapRendererHandler;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.render.util.RenderBufferUtil;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.client.graphics.XaeroBufferProvider;

public class CompassRenderer {
   private final HudMod modMain;
   private final class_310 mc;
   private double[] partialDest;

   public CompassRenderer(HudMod modMain, class_310 mc) {
      this.modMain = modMain;
      this.mc = mc;
      this.partialDest = new double[2];
   }

   public void drawCompass(class_4587 matrixStack, int specW, int specH, double ps, double pc, double zoom, boolean circle, float minimapScale, boolean background, XaeroBufferProvider textRenderTypeBuffer, class_4588 nameBgBuilder) {
      ClientConfigManager configManager = this.modMain.getHudConfigs().getClientConfigManager();
      int compassLocationConfig = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.COMPASS_LOCATION);
      if (compassLocationConfig != 0) {
         int shadowColorConfig = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.COMPASS_SHADOW_COLOR);
         WaypointColor defaultColor = WaypointColor.fromIndex(shadowColorConfig);
         MinimapRendererHelper rendererHelper = this.modMain.getMinimap().getMinimapFBORenderer().getHelper();
         int northShadowColorConfig = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.COMPASS_NORTH_SHADOW_COLOR);
         int effectiveNorthShadowColorConfig = northShadowColorConfig == -1 ? shadowColorConfig : northShadowColorConfig;

         for(int i = 0; i < 4; ++i) {
            double offX = (double)((i & 1) * (i == 1 ? 10000 : -10000));
            double offY = (double)((i + 1 & 1) * (i == 2 ? 10000 : -10000));
            matrixStack.method_22903();
            MinimapElementOverMapRendererHandler.translatePosition(matrixStack, specW, specH, specW, specH, ps, pc, offX, offY, zoom, circle, this.partialDest);
            matrixStack.method_46416(-1.0F, -1.0F, 0.0F);
            matrixStack.method_22905(minimapScale, minimapScale, 1.0F);
            class_2561 initials = CardinalDirection.values()[i].getInitials();
            int halfW = this.mc.field_1772.method_27525(initials) / 2 - 1;
            WaypointColor effectiveColor = i == 0 ? WaypointColor.fromIndex(effectiveNorthShadowColorConfig) : defaultColor;
            if (background) {
               int addedFrame = halfW > 3 ? halfW - 3 : 0;
               RenderBufferUtil.addColoredRect(matrixStack.method_23760().method_23761(), nameBgBuilder, (float)(-4 - addedFrame), (float)(-4 - addedFrame), 9 + 2 * addedFrame, 9 + 2 * addedFrame, -1879048192 | effectiveColor.getHex() & 16777215);
            }

            Misc.drawNormalText(matrixStack, (class_2561)initials, (float)(-halfW + 1), -2.0F, effectiveColor.getHex(), false, textRenderTypeBuffer);
            matrixStack.method_46416(0.0F, 0.0F, 1.0F);
            Misc.drawNormalText(matrixStack, (class_2561)initials, (float)(-halfW), -3.0F, -1, false, textRenderTypeBuffer);
            matrixStack.method_22909();
         }

         matrixStack.method_46416(0.0F, 0.0F, 2.0F);
      }
   }
}
