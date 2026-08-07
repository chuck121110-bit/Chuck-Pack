package xaero.hud.minimap.waypoint.render;

import net.minecraft.class_1041;
import net.minecraft.class_1074;
import net.minecraft.class_10799;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import xaero.common.HudMod;
import xaero.common.IXaeroMinimap;
import xaero.common.effect.Effects;
import xaero.common.graphics.CustomRenderTypes;
import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.common.minimap.render.MinimapRendererHelper;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointUtil;
import xaero.common.misc.Misc;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.config.util.MinimapConfigClientUtils;
import xaero.hud.minimap.element.render.MinimapElementGraphics;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.element.render.MinimapElementRenderer;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.WaypointSession;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.render.TextureLocations;
import xaero.hud.render.util.RenderBufferUtil;
import xaero.lib.XaeroLib;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.client.graphics.XaeroBufferProvider;

public final class WaypointMapRenderer extends MinimapElementRenderer<Waypoint, WaypointMapRenderContext> {
   private MinimapRendererHelper helper;
   private int scale;
   private boolean temporaryWaypointsGlobal;
   private double waypointsDistance;
   private boolean dimensionScaleDistance;
   private int opacity;
   private XaeroBufferProvider minimapBufferSource;
   private class_4588 texturedIconConsumer;
   private class_4588 waypointBackgroundConsumer;

   private WaypointMapRenderer(WaypointMapRenderReader elementReader, WaypointMapRenderProvider provider, WaypointMapRenderContext context) {
      super(elementReader, provider, context);
   }

   public boolean renderElement(Waypoint w, boolean highlighted, boolean outOfBounds, double optionalDepth, float optionalScale, double partialX, double partialY, MinimapElementRenderInfo renderInfo, MinimapElementGraphics guiGraphics, class_4597.class_4598 vanillaBufferSource) {
      double waypointPosDivider = renderInfo.backgroundCoordinateScale / (this.context).dimCoordinateScale;
      double wX = (double)w.getX(waypointPosDivider) + (double)0.5F;
      double wZ = (double)w.getZ(waypointPosDivider) + (double)0.5F;
      double offX = wX - renderInfo.renderPos.field_1352;
      double offZ = wZ - renderInfo.renderPos.field_1350;
      double distance2D = Math.sqrt(offX * offX + offZ * offZ);
      double distanceScale = this.dimensionScaleDistance ? renderInfo.backgroundCoordinateScale : (double)1.0F;
      double scaledDistance2D = distance2D * distanceScale;
      if (!w.isDestination() && w.getPurpose() != WaypointPurpose.DEATH && !w.isGlobal() && (!w.isTemporary() || !this.temporaryWaypointsGlobal) && this.waypointsDistance != (double)0.0F && scaledDistance2D > this.waypointsDistance) {
         return false;
      } else {
         class_4587 matrixStack = guiGraphics.pose();
         MinimapElementRenderLocation location = renderInfo.location;
         matrixStack.method_22904((double)-1.0F, (double)-1.0F, optionalDepth);
         if (this.scale > 0 && location == MinimapElementRenderLocation.OVER_MINIMAP) {
            matrixStack.method_22905((float)this.scale, (float)this.scale, 1.0F);
         } else {
            matrixStack.method_22905(optionalScale, optionalScale, 1.0F);
         }

         this.drawIcon(guiGraphics, this.helper, w, 0, 0, this.opacity, this.minimapBufferSource, this.waypointBackgroundConsumer, this.texturedIconConsumer);
         return true;
      }
   }

   public void preRender(MinimapElementRenderInfo renderInfo, class_4597.class_4598 vanillaBufferSource, MultiTextureRenderTypeRendererProvider multiTextureRenderTypeRenderers) {
      vanillaBufferSource.method_22993();
      this.minimapBufferSource = XaeroLib.INSTANCE.getClient().getBufferProvider();
      this.waypointBackgroundConsumer = this.minimapBufferSource.getBuffer(CustomRenderTypes.COLORED_WAYPOINTS_BGS);
      this.texturedIconConsumer = this.minimapBufferSource.getBuffer(CustomRenderTypes.GUI_NEAREST);
      this.helper = HudMod.INSTANCE.getMinimap().getMinimapFBORenderer().getHelper();
      MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
      MinimapWorld currentWorld = session.getWorldManager().getCurrentWorld();
      (this.context).dimCoordinateScale = session.getDimensionHelper().getDimCoordinateScale(currentWorld);
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      this.scale = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_ICON_SCALE_ON_MINIMAP);
      if (this.scale > 0) {
         this.scale = (int)MinimapConfigClientUtils.getUIScale(configManager, MinimapProfiledConfigOptions.WAYPOINT_ICON_SCALE_ON_MINIMAP);
      }

      this.temporaryWaypointsGlobal = (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.TEMPORARY_WAYPOINTS_GLOBAL);
      this.waypointsDistance = (double)(Integer)configManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_MAX_DISTANCE);
      this.dimensionScaleDistance = (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_MAX_DISTANCE_DIMENSION_SCALE);
      this.opacity = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_OPACITY_ON_MINIMAP);
   }

   public void postRender(MinimapElementRenderInfo renderInfo, class_4597.class_4598 vanillaBufferSource, MultiTextureRenderTypeRendererProvider multiTextureRenderTypeRenderers) {
      this.minimapBufferSource.endBatch();
      this.waypointBackgroundConsumer = null;
   }

   public void drawIcon(MinimapElementGraphics guiGraphics, MinimapRendererHelper rendererHelper, Waypoint w, int drawX, int drawY, int opacity, XaeroBufferProvider renderTypeBuffer, class_4588 waypointBackgroundConsumer, class_4588 texturedIconConsumer) {
      int color = w.getWaypointColor().getHex();
      int r = color >> 16 & 255;
      int g = color >> 8 & 255;
      int b = color & 255;
      float a = (float)opacity / 100.0F;
      int initialsWidth = w.getPurpose() == WaypointPurpose.DEATH ? 7 : class_310.method_1551().field_1772.method_1727(w.getInitials());
      int addedFrame = WaypointUtil.getAddedMinimapIconFrame(initialsWidth);
      int rectX1 = drawX - 4 - addedFrame;
      int rectY1 = drawY - 4;
      int rectX2 = drawX + 5 + addedFrame;
      int rectY2 = drawY + 5;
      this.drawIcon(guiGraphics, w, drawX, drawY, rectX1, rectY1, rectX2, rectY2, r, g, b, a, initialsWidth, renderTypeBuffer, waypointBackgroundConsumer, texturedIconConsumer);
   }

   public void drawIconGUI(class_332 guiGraphics, Waypoint w, int drawX, int drawY, int opacity) {
      int color = w.getWaypointColor().getHex();
      int r = color >> 16 & 255;
      int g = color >> 8 & 255;
      int b = color & 255;
      float a = (float)opacity / 100.0F;
      int initialsWidth = w.getPurpose() == WaypointPurpose.DEATH ? 7 : class_310.method_1551().field_1772.method_1727(w.getInitials());
      int addedFrame = WaypointUtil.getAddedMinimapIconFrame(initialsWidth);
      int rectX1 = drawX - 4 - addedFrame;
      int rectY1 = drawY - 4;
      int rectX2 = drawX + 5 + addedFrame;
      int rectY2 = drawY + 5;
      this.drawIconGUI(guiGraphics, w, drawX, drawY, rectX1, rectY1, rectX2, rectY2, r, g, b, a, initialsWidth);
   }

   private void drawIcon(MinimapElementGraphics guiGraphics, Waypoint w, int drawX, int drawY, int rectX1, int rectY1, int rectX2, int rectY2, int r, int g, int b, float a, int initialsWidth, XaeroBufferProvider renderTypeBuffer, class_4588 waypointBackgroundConsumer, class_4588 texturedIconConsumer) {
      class_4587 matrixStack = guiGraphics.pose();
      RenderBufferUtil.addColoredRect(matrixStack.method_23760().method_23761(), waypointBackgroundConsumer, (float)rectX1, (float)rectY1, rectX2 - rectX1, rectY2 - rectY1, (float)r / 255.0F, (float)g / 255.0F, (float)b / 255.0F, a);
      if (w.getPurpose() == WaypointPurpose.DEATH) {
         RenderBufferUtil.addTexturedColoredRect(matrixStack.method_23760().method_23761(), texturedIconConsumer, (float)(rectX1 + 1), (float)(rectY1 + 1), 0, 87, 9, 9, 9, -9, 0.2431F, 0.2431F, 0.2431F, 1.0F, 256.0F);
         RenderBufferUtil.addTexturedColoredRect(matrixStack.method_23760().method_23761(), texturedIconConsumer, (float)rectX1, (float)rectY1, 0, 87, 9, 9, 9, -9, 0.9882F, 0.9882F, 0.9882F, 1.0F, 256.0F);
      } else {
         Misc.drawNormalText(matrixStack, (String)w.getInitials(), (float)(drawX + 1 - initialsWidth / 2), (float)(drawY - 3), -1, true, renderTypeBuffer);
      }
   }

   private void drawIconGUI(class_332 guiGraphics, Waypoint w, int drawX, int drawY, int rectX1, int rectY1, int rectX2, int rectY2, int r, int g, int b, float a, int initialsWidth) {
      int aByte = (int)(a * 255.0F);
      int color = aByte << 24 | r << 16 | g << 8 | b;
      guiGraphics.method_25294(rectX1, rectY1, rectX2, rectY2, color);
      if (w.getPurpose() == WaypointPurpose.DEATH) {
         int shadowColor = -12698050;
         int skullColor = -197380;
         guiGraphics.method_25293(class_10799.field_56883, TextureLocations.GUI_TEXTURES, rectX1 + 1, rectY1 + 1, 0.0F, 78.0F, 9, 9, 9, 9, 256, 256, shadowColor);
         guiGraphics.method_25293(class_10799.field_56883, TextureLocations.GUI_TEXTURES, rectX1, rectY1, 0.0F, 78.0F, 9, 9, 9, 9, 256, 256, skullColor);
      } else {
         guiGraphics.method_51433(class_310.method_1551().field_1772, w.getInitials(), drawX + 1 - initialsWidth / 2, drawY - 3, -1, true);
      }
   }

   public void drawSetChange(MinimapSession session, class_332 guiGraphics, class_1041 res) {
      MinimapWorld minimapWorld = session.getWorldManager().getCurrentWorld();
      if (minimapWorld != null) {
         WaypointSession waypointSession = session.getWaypointSession();
         if (waypointSession.getSetChangedTime() != 0L) {
            int passed = (int)(System.currentTimeMillis() - waypointSession.getSetChangedTime());
            if (passed >= 1500) {
               waypointSession.setSetChangedTime(0L);
            } else {
               int fadeTime = 300;
               boolean fading = passed > 1500 - fadeTime;
               float fadeFactor = fading ? (float)(1500 - passed) / (float)fadeTime : 1.0F;
               int alpha = 3 + (int)(252.0F * fadeFactor);
               int c = 16777215 | alpha << 24;
               guiGraphics.method_25300(class_310.method_1551().field_1772, class_1074.method_4662(minimapWorld.getCurrentWaypointSet().getName(), new Object[0]), res.method_4486() / 2, res.method_4502() / 2 + 50, c);
            }
         }
      }
   }

   public boolean shouldRender(MinimapElementRenderLocation location) {
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      if ((location == MinimapElementRenderLocation.OVER_MINIMAP || location == MinimapElementRenderLocation.IN_MINIMAP) && !(Boolean)configManager.getEffective(MinimapProfiledConfigOptions.WAYPOINTS_ON_MINIMAP)) {
         return false;
      } else {
         return !Misc.hasEffect(Effects.NO_WAYPOINTS) && !Misc.hasEffect(Effects.NO_WAYPOINTS_HARMFUL);
      }
   }

   public int getOrder() {
      return 100;
   }

   public static final class Builder {
      private WaypointDeleter waypointDeleter;
      private final IXaeroMinimap modMain;

      private Builder(IXaeroMinimap modMain) {
         this.modMain = modMain;
      }

      private Builder setDefault() {
         this.setWaypointDeleter((WaypointDeleter)null);
         return this;
      }

      public Builder setWaypointDeleter(WaypointDeleter waypointDeleter) {
         this.waypointDeleter = waypointDeleter;
         return this;
      }

      public WaypointMapRenderer build() {
         if (this.waypointDeleter == null) {
            throw new IllegalStateException();
         } else {
            WaypointMapRenderContext context = new WaypointMapRenderContext();
            return new WaypointMapRenderer(new WaypointMapRenderReader(), new WaypointMapRenderProvider(), context);
         }
      }

      public static Builder begin(IXaeroMinimap modMain) {
         return (new Builder(modMain)).setDefault();
      }
   }
}
