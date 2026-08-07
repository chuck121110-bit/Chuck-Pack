package xaero.common.minimap.render;

import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import net.minecraft.class_1044;
import net.minecraft.class_12137;
import net.minecraft.class_12206;
import net.minecraft.class_12247;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_5321;
import net.minecraft.class_638;
import net.minecraft.class_308.class_11274;
import org.joml.Matrix4f;
import xaero.common.HudMod;
import xaero.common.graphics.CustomRenderTypes;
import xaero.common.graphics.OpenGlHelper;
import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.common.minimap.MinimapProcessor;
import xaero.common.minimap.mcworld.MinimapClientWorldDataHelper;
import xaero.common.misc.OptimizedMath;
import xaero.common.settings.ModSettings;
import xaero.hud.entity.EntityUtils;
import xaero.hud.minimap.Minimap;
import xaero.hud.minimap.common.config.MinimapConfigConstants;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.compass.render.CompassRenderer;
import xaero.hud.minimap.config.util.MinimapConfigClientUtils;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.radar.RadarSession;
import xaero.hud.minimap.radar.category.EntityRadarCategory;
import xaero.hud.minimap.radar.category.EntityRadarCategoryManager;
import xaero.hud.minimap.radar.category.setting.EntityRadarCategorySettings;
import xaero.hud.minimap.radar.color.RadarColor;
import xaero.hud.minimap.waypoint.render.WaypointMapRenderer;
import xaero.hud.render.TextureLocations;
import xaero.lib.XaeroLib;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.client.graphics.XaeroBufferProvider;
import xaero.lib.client.graphics.XaeroRenderType;
import xaero.lib.client.graphics.util.ImmediateRenderUtil;
import xaero.lib.client.graphics.util.TextureUtils;
import xaero.lib.client.gui.util.graphics.GuiGraphicsUtils;

public abstract class MinimapRenderer {
   public static final int black = -16777216;
   public static final int slime = -2142047936;
   protected HudMod modMain;
   protected class_310 mc;
   protected Minimap minimap;
   protected MinimapRendererHelper helper;
   protected WaypointMapRenderer waypointMapRenderer;
   private int lastMinimapSize;
   protected double zoom = (double)1.0F;
   private class_2338.class_2339 mutableBlockPos;
   protected final CompassRenderer compassRenderer;
   protected final class_4587 matrixStack;

   public MinimapRenderer(HudMod modMain, class_310 mc, WaypointMapRenderer waypointMapRenderer, Minimap minimap, CompassRenderer compassRenderer, class_4587 matrixStack) {
      this.modMain = modMain;
      this.mc = mc;
      this.waypointMapRenderer = waypointMapRenderer;
      this.minimap = minimap;
      this.matrixStack = matrixStack;
      this.helper = new MinimapRendererHelper();
      this.mutableBlockPos = new class_2338.class_2339();
      this.compassRenderer = compassRenderer;
   }

   public double getRenderAngle(boolean lockedNorth) {
      return lockedNorth ? (double)90.0F : this.getActualAngle();
   }

   private double getActualAngle() {
      double rotation = (double)this.mc.field_1773.method_19418().method_19330();
      return (double)-90.0F - rotation;
   }

   protected abstract void renderChunks(MinimapSession var1, MinimapProcessor var2, class_243 var3, class_5321<class_1937> var4, double var5, int var7, int var8, float var9, float var10, int var11, boolean var12, boolean var13, int var14, double var15, double var17, boolean var19, boolean var20, ModSettings var21, XaeroBufferProvider var22);

   public void renderMinimap(MinimapSession minimapSession, MinimapProcessor minimap, int x, int y, int width, int height, double scale, float minimapScale, int size, float partial, XaeroBufferProvider cvc) {
      ModSettings settings = this.modMain.getSettings();
      int minimapSizeConfig = MinimapConfigClientUtils.getEffectiveMinimapSize();
      if (minimapSizeConfig != this.lastMinimapSize) {
         this.lastMinimapSize = minimapSizeConfig;
         minimap.setToResetImage(true);
      }

      minimap.getRadarSession().getStateUpdater().setLastRenderViewEntity(this.mc.method_1560());
      int mapSize = minimapSession.getProcessor().getMinimapSize();
      int bufferSize = minimapSession.getProcessor().getMinimapBufferSize(mapSize);
      if (this.minimap.usingFBO()) {
         bufferSize = minimap.getFBOBufferSize();
      }

      ClientConfigManager configManager = this.modMain.getHudConfigs().getClientConfigManager();
      float mapScale = (float)(scale / (double)minimapScale);
      minimap.updateZoom();
      this.zoom = minimap.getMinimapZoom();
      this.mc.field_1773.method_71114().method_71034(class_11274.field_60026);
      ImmediateRenderUtil.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      OpenGlHelper.resetPixelStore();
      float sizeFix = 1.0F;
      int shape = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.SHAPE);
      boolean lockedNorth = MinimapConfigClientUtils.getEffectiveNorthLocked(mapSize / 2, shape);
      double angle = Math.toRadians(this.getRenderAngle(lockedNorth));
      double ps = Math.sin(Math.PI - angle);
      double pc = Math.cos(Math.PI - angle);
      boolean useWorldMap = this.modMain.getSupportMods().shouldUseWorldMapChunks() && !minimap.getMinimapWriter().isLoadedNonWorldMap();
      boolean lightingConfig = (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.LIGHTING);
      int lightLevel = (int)((1.0F - Math.min(1.0F, this.getSunBrightness(minimap, lightingConfig))) * (float)(minimap.getMinimapWriter().getLoadedLevels() - 1));
      boolean cave = minimap.isCaveModeDisplayed();
      boolean circleShape = shape == 1;
      double playerX = EntityUtils.getEntityX(this.mc.method_1560(), partial);
      double playerY = EntityUtils.getEntityY(this.mc.method_1560(), partial);
      double playerZ = EntityUtils.getEntityZ(this.mc.method_1560(), partial);
      double renderX = playerX;
      double renderZ = playerZ;
      double mapDimensionScale = this.mc.field_1687.method_8597().comp_646();
      class_5321<class_1937> mapDimension = this.mc.field_1687.method_27983();
      double playerDimDiv = (double)1.0F;
      if (useWorldMap) {
         double playerCoordinateScale = mapDimensionScale;
         mapDimensionScale = this.modMain.getSupportMods().worldmapSupport.getMapDimensionScale();
         mapDimension = this.modMain.getSupportMods().worldmapSupport.getMapDimension();
         if (mapDimensionScale == (double)0.0F) {
            mapDimensionScale = minimap.getLastMapDimensionScale();
            mapDimension = minimap.getLastMapDimension();
         }

         playerDimDiv = mapDimensionScale / playerCoordinateScale;
         renderX = playerX / playerDimDiv;
         renderZ = playerZ / playerDimDiv;
      }

      minimap.setLastMapDimensionScale(mapDimensionScale);
      minimap.setLastMapDimension(mapDimension);
      class_243 renderPos = new class_243(renderX, playerY, renderZ);
      this.matrixStack.method_22903();
      this.renderChunks(minimapSession, minimap, renderPos, mapDimension, mapDimensionScale, mapSize, bufferSize, sizeFix, partial, lightLevel, useWorldMap, lockedNorth, shape, ps, pc, cave, circleShape, settings, cvc);
      if (this.minimap.usingFBO()) {
         sizeFix = 1.0F;
      }

      this.matrixStack.method_22905(1.0F / mapScale, 1.0F / mapScale, 1.0F);
      int scaledX = (int)((float)x * mapScale);
      int scaledY = (int)((float)y * mapScale);
      int minimapFrameSize = (int)((float)(mapSize / 2) / sizeFix);
      int circleSides = Math.max(32, (int)Math.ceil(Math.PI * (double)(minimapFrameSize + 8) / (double)8.0F / (double)4.0F) * 4);
      double circleSeamAngle = (-Math.PI / 4D);
      int circleSeamWidth = 32;
      int circleFrameThickness = 4;
      double circleStartAngle = (double)0.0F;
      class_12247.class_12337 minimapViewTas = this.getTas();
      if (!circleShape) {
         this.getHelper().drawMyTexturedModalRect(this.matrixStack, (float)((int)((float)(scaledX + 9) / sizeFix)), (float)((int)((float)(scaledY + 9) / sizeFix)), 0, 256 - minimapFrameSize, (float)minimapFrameSize, (float)minimapFrameSize, (float)minimapFrameSize, 256.0F, minimapViewTas, true);
      } else {
         float outerRadius = (float)(mapSize / 4 + circleFrameThickness);
         circleStartAngle = circleSeamAngle - (double)((float)(circleSeamWidth / 2) / outerRadius);
         this.getHelper().drawTexturedElipseInsideRectangle(this.matrixStack, circleStartAngle, circleSides, (float)((int)((float)(scaledX + 9) / sizeFix)), (float)((int)((float)(scaledY + 9) / sizeFix)), 0, 256 - minimapFrameSize, (float)minimapFrameSize, 256.0F, minimapViewTas, true);
      }

      if (!this.minimap.usingFBO()) {
         this.matrixStack.method_22905(1.0F / sizeFix, 1.0F / sizeFix, 1.0F);
         ImmediateRenderUtil.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      int frameType = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.FRAME);
      boolean renderFrame = frameType < MinimapConfigConstants.FRAME_NAMES.length - 1;
      if (frameType > 0) {
         int frameColorConfig = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.FRAME_COLOR);
         int frameColor = MinimapConfigConstants.COLORS[frameColorConfig];
         ImmediateRenderUtil.setShaderColor((float)(frameColor >> 16 & 255) / 255.0F, (float)(frameColor >> 8 & 255) / 255.0F, (float)(frameColor & 255) / 255.0F, 1.0F);
      }

      MinimapRendererHelper helper = this.getHelper();
      class_12247.class_12337 minimapFrameTAS = null;
      if (renderFrame) {
         GpuTextureView minimapFrameView = TextureUtils.getView(TextureLocations.MINIMAP_FRAME_TEXTURES);
         class_12137 minimapFrameViewSampler = TextureUtils.getSampler(TextureLocations.MINIMAP_FRAME_TEXTURES);
         minimapFrameTAS = new class_12247.class_12337(minimapFrameView, minimapFrameViewSampler);
      }

      if (renderFrame && !circleShape) {
         int rightCornerStartX = scaledX + 9 + mapSize / 2 + 4 - 16;
         int bottomCornerStartY = scaledY + 9 + mapSize / 2 + 4 - 16;
         class_289 tessellator = class_289.method_1348();
         class_287 vertexBuffer = tessellator.method_60827(class_5596.field_27382, class_290.field_1585);
         Matrix4f matrix = this.matrixStack.method_23760().method_23761();
         int cornerTextureX = frameType == 0 ? 192 : (frameType == 1 ? 208 : 224);
         helper.addTexturedRectToExistingBuffer(matrix, vertexBuffer, (float)(scaledX + 9 - 4), (float)(scaledY + 9 - 4), cornerTextureX, 97, 16, 16);
         helper.addTexturedRectToExistingBuffer(matrix, vertexBuffer, (float)rightCornerStartX, (float)(scaledY + 9 - 4), cornerTextureX, 113, 16, 16);
         helper.addTexturedRectToExistingBuffer(matrix, vertexBuffer, (float)(scaledX + 9 - 4), (float)bottomCornerStartY, cornerTextureX, 129, 16, 16);
         helper.addTexturedRectToExistingBuffer(matrix, vertexBuffer, (float)rightCornerStartX, (float)bottomCornerStartY, cornerTextureX, 145, 16, 16);
         int horLineStartX = scaledX + 9 - 4 + 16;
         int horLineWidth = rightCornerStartX - horLineStartX;
         int horPieceTextureY = frameType == 0 ? 0 : (frameType == 1 ? 32 : 64);
         int horPieceWidth = 226;
         int horLineLength = (int)Math.ceil((double)horLineWidth / (double)horPieceWidth);

         for(int i = 0; i < horLineLength; ++i) {
            int pieceX = scaledX + 9 - 4 + 16 + i * horPieceWidth;
            int pieceW = horPieceWidth;
            if (i == horLineLength - 1 && pieceX + horPieceWidth > rightCornerStartX) {
               pieceW = rightCornerStartX - pieceX;
            }

            helper.addTexturedRectToExistingBuffer(matrix, vertexBuffer, (float)pieceX, (float)(scaledY + 9 - 4), 0, horPieceTextureY, pieceW, 16);
            helper.addTexturedRectToExistingBuffer(matrix, vertexBuffer, (float)pieceX, (float)(scaledY + 9 + mapSize / 2 - 12), 0, horPieceTextureY + 16, pieceW, 16);
         }

         int verLineStartY = scaledY + 9 - 4 + 16;
         int verLineHeight = bottomCornerStartY - verLineStartY;
         int verPieceTextureX = frameType == 0 ? 0 : (frameType == 1 ? 64 : 128);
         int verPieceHeight = 113;
         int vertLineLength = (int)Math.ceil((double)verLineHeight / (double)verPieceHeight);

         for(int i = 0; i < vertLineLength; ++i) {
            int pieceY = scaledY + 9 - 4 + 16 + i * verPieceHeight;
            int pieceU = verPieceTextureX + 32 * (i & 1);
            int pieceH = verPieceHeight;
            if (i == vertLineLength - 1 && pieceY + verPieceHeight > bottomCornerStartY) {
               pieceH = bottomCornerStartY - pieceY;
            }

            helper.addTexturedRectToExistingBuffer(matrix, vertexBuffer, (float)(scaledX + 9 - 4), (float)pieceY, pieceU, 97, 16, pieceH);
            helper.addTexturedRectToExistingBuffer(matrix, vertexBuffer, (float)(scaledX + 9 + mapSize / 2 - 12), (float)pieceY, pieceU + 16, 97, 16, pieceH);
         }

         ImmediateRenderUtil.drawImmediateMeshData(vertexBuffer.method_60794(), XaeroRenderType.RP_POSITION_TEX_NO_ALPHA, minimapFrameTAS);
      } else if (renderFrame) {
         int frameTextureY = frameType == 0 ? 210 : (frameType == 1 ? 214 : 218);
         double shadeStartAngle = (Math.PI / 4D) - circleStartAngle;
         int shadeStartIndex = (int)(shadeStartAngle / (double)2.0F / Math.PI * (double)circleSides);
         int circleLeftX = scaledX + 9;
         int circleTopY = scaledY + 9;
         int innerCircleDiameter = mapSize / 2;
         helper.drawTexturedElipseInsideRectangleFrame(this.matrixStack, false, false, circleStartAngle, 0, shadeStartIndex, circleSides, (float)circleFrameThickness, (float)circleLeftX, (float)circleTopY, 0, frameTextureY, (float)innerCircleDiameter, 73.0F, (float)circleFrameThickness, circleSeamWidth, 256.0F, minimapFrameTAS);
         helper.drawTexturedElipseInsideRectangleFrame(this.matrixStack, true, false, circleStartAngle, shadeStartIndex, shadeStartIndex + circleSides / 4, circleSides, (float)circleFrameThickness, (float)circleLeftX, (float)circleTopY, 138, frameTextureY, (float)innerCircleDiameter, 68.0F, (float)circleFrameThickness, 20, 256.0F, minimapFrameTAS);
         helper.drawTexturedElipseInsideRectangleFrame(this.matrixStack, true, true, circleStartAngle, shadeStartIndex + circleSides / 4, shadeStartIndex + circleSides / 2, circleSides, (float)circleFrameThickness, (float)circleLeftX, (float)circleTopY, 138, frameTextureY, (float)innerCircleDiameter, 68.0F, (float)circleFrameThickness, 20, 256.0F, minimapFrameTAS);
         helper.drawTexturedElipseInsideRectangleFrame(this.matrixStack, false, false, circleStartAngle, shadeStartIndex + circleSides / 2, circleSides, circleSides, (float)circleFrameThickness, (float)circleLeftX, (float)circleTopY, 0, frameTextureY, (float)innerCircleDiameter, 73.0F, (float)circleFrameThickness, circleSeamWidth, 256.0F, minimapFrameTAS);
      }

      ImmediateRenderUtil.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      this.matrixStack.method_22903();
      this.matrixStack.method_46416((float)(scaledX + 9), (float)(scaledY + 9), 0.0F);
      this.matrixStack.method_22905(1.0F / minimapScale, 1.0F / minimapScale, 1.0F);
      int halfFrame = (int)((float)mapSize * minimapScale / 2.0F / 2.0F);
      this.matrixStack.method_22904((double)halfFrame, (double)halfFrame, (double)0.5F);
      int specW = halfFrame + (int)(3.0F * minimapScale);
      boolean safeMode = this instanceof MinimapSafeModeRenderer;
      XaeroBufferProvider renderTypeBuffers = XaeroLib.INSTANCE.getClient().getBufferProvider();
      MultiTextureRenderTypeRendererProvider multiTextureRenderTypeRenderers = minimapSession.getMultiTextureRenderTypeRenderers();
      double scaledZoom = this.zoom * (double)minimapScale / (double)2.0F;
      boolean compassOverEverythingConfig = (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.COMPASS_OVER_EVERYTHING);
      if (!compassOverEverythingConfig) {
         this.renderCompass(this.matrixStack, settings, configManager, renderTypeBuffers, specW, specW, halfFrame, ps, pc, circleShape, minimapScale);
      }

      this.minimap.getOverMapRendererHandler().prepareRender(ps, pc, scaledZoom, specW, specW, halfFrame, halfFrame, circleShape, minimapScale);
      this.minimap.getOverMapRendererHandler().render(renderPos, partial, (class_276)null, mapDimensionScale, mapDimension);
      if (compassOverEverythingConfig) {
         this.renderCompass(this.matrixStack, settings, configManager, renderTypeBuffers, specW, specW, halfFrame, ps, pc, circleShape, minimapScale);
      }

      renderTypeBuffers.endBatch();
      this.matrixStack.method_22909();
      int mainEntityAs = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.RADAR_MAIN_ENTITY);
      boolean crosshairDisplayed = mainEntityAs == 0 && !lockedNorth;
      if (crosshairDisplayed) {
         this.matrixStack.method_22903();
         this.matrixStack.method_46416((float)(scaledX + 9), (float)(scaledY + 9), 0.0F);
         this.matrixStack.method_22905(0.5F, 0.5F, 1.0F);
         this.matrixStack.method_46416((float)(mapSize / 2), (float)(mapSize / 2), 0.0F);
         ImmediateRenderUtil.negativeColorRectangle(this.matrixStack, -5.0F, -1.0F, 5.0F, 1.0F);
         ImmediateRenderUtil.negativeColorRectangle(this.matrixStack, -1.0F, 3.0F, 1.0F, 5.0F);
         ImmediateRenderUtil.negativeColorRectangle(this.matrixStack, -1.0F, -5.0F, 1.0F, -3.0F);
         RadarSession radarSession = minimap.getRadarSession();
         EntityRadarCategoryManager categoryManager = radarSession.getCategoryManager();
         EntityRadarCategory mainEntityCategory = (EntityRadarCategory)categoryManager.getRuleResolver().resolve(categoryManager.getRootCategory(), this.mc.method_1560(), this.mc.field_1724);
         if (mainEntityCategory == null) {
            mainEntityCategory = categoryManager.getRootCategory();
         }

         RadarColor crosshairRadarColor = RadarColor.fromIndex(((Double)mainEntityCategory.getSettingValue(EntityRadarCategorySettings.COLOR)).intValue());
         RadarColor crosshairFallbackColor = radarSession.getColorHelper().getFallbackColor(mainEntityCategory, (EntityRadarCategory)null);
         int crosshairColor = radarSession.getColorHelper().getEntityColor(this.mc.method_1560(), 0.0F, false, 100, 100, false, crosshairRadarColor, crosshairFallbackColor);
         ImmediateRenderUtil.setShaderColor((float)(crosshairColor >> 16 & 255) / 255.0F, (float)(crosshairColor >> 8 & 255) / 255.0F, (float)(crosshairColor & 255) / 255.0F, 1.0F);
         this.getHelper().drawMyColoredRect(this.matrixStack, 1.0F, -1.0F, 3.0F, 1.0F, false);
         this.getHelper().drawMyColoredRect(this.matrixStack, -3.0F, -1.0F, -1.0F, 1.0F, false);
         this.getHelper().drawMyColoredRect(this.matrixStack, -1.0F, 1.0F, 1.0F, 3.0F, false);
         this.getHelper().drawMyColoredRect(this.matrixStack, -1.0F, -3.0F, 1.0F, -1.0F, false);
         ImmediateRenderUtil.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         this.matrixStack.method_22909();
      }

      double centerX = (double)(2 * scaledX + 18 + mapSize / 2);
      double centerY = (double)(2 * scaledY + 18 + mapSize / 2);
      this.matrixStack.method_22903();
      this.matrixStack.method_22905(0.5F, 0.5F, 1.0F);
      this.matrixStack.method_22904(centerX, centerY, (double)0.0F);
      class_1044 guiTextures = this.mc.method_1531().method_4619(TextureLocations.GUI_TEXTURES);
      class_1297 mainEntity = this.mc.method_1560();
      if (!safeMode && mainEntityAs == 1) {
         this.minimap.getMinimapFBORenderer().renderMainEntityDot(mainEntity, cave, cvc);
      }

      class_12247.class_12337 guiTexturesLinearTas = new class_12247.class_12337(guiTextures.method_71659(), XaeroRenderType.getSimpleSampler(FilterMode.LINEAR));
      if (lockedNorth || mainEntityAs == 2) {
         float arrowAngle = lockedNorth ? mainEntity.method_5705(partial) : 180.0F;
         int arrowOpacityInt = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.ARROW_OPACITY);
         float arrowOpacity = (float)arrowOpacityInt / 100.0F;
         int arrowColour = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.ARROW_COLOR);
         float r;
         float g;
         float b;
         float a;
         if (arrowColour != -1) {
            float[] c = MinimapConfigConstants.ARROW_COLORS[arrowColour];
            r = c[0];
            g = c[1];
            b = c[2];
            a = c[3];
         } else {
            int rgb = minimap.getRadarSession().getColorHelper().getTeamColor((class_1297)(this.mc.field_1724 == null ? mainEntity : this.mc.field_1724));
            if (rgb != -1) {
               r = (float)(rgb >> 16 & 255) / 255.0F;
               g = (float)(rgb >> 8 & 255) / 255.0F;
               b = (float)(rgb & 255) / 255.0F;
               a = 1.0F;
            } else {
               float[] c = MinimapConfigConstants.ARROW_COLORS[0];
               r = c[0];
               g = c[1];
               b = c[2];
               a = c[3];
            }
         }

         float shadowR = class_3532.method_16439(arrowOpacity, r, 0.0F);
         float shadowG = class_3532.method_16439(arrowOpacity, g, 0.0F);
         float shadowB = class_3532.method_16439(arrowOpacity, b, 0.0F);
         this.drawArrow(this.matrixStack, arrowAngle, (double)0.0F, (double)-2.0F, shadowR, shadowG, shadowB, arrowOpacity * arrowOpacity * 0.3F, guiTexturesLinearTas, configManager);
         this.drawArrow(this.matrixStack, arrowAngle, (double)0.0F, (double)2.0F, shadowR, shadowG, shadowB, arrowOpacity * arrowOpacity * 0.5F, guiTexturesLinearTas, configManager);
         a *= arrowOpacity;
         this.drawArrow(this.matrixStack, arrowAngle, (double)0.0F, (double)0.0F, r, g, b, a, guiTexturesLinearTas, configManager);
      }

      this.matrixStack.method_22909();
      this.matrixStack.method_22909();
      this.mc.field_1773.method_71114().method_71034(class_11274.field_60027);
   }

   public void renderOutsidePip(MinimapSession minimapSession, int x, int y, int width, int height, double scale, float minimapScale, int size, float partial, class_332 guiGraphics) {
      float mapScale = (float)(scale / (double)minimapScale);
      int scaledX = (int)((float)x * mapScale);
      int scaledY = (int)((float)y * mapScale);
      class_1297 mainEntity = this.mc.method_1560();
      int playerBlockX = OptimizedMath.myFloor(mainEntity.method_23317());
      int playerBlockY = OptimizedMath.myFloor(mainEntity.method_23318());
      int playerBlockZ = OptimizedMath.myFloor(mainEntity.method_23321());
      class_2338 pos = this.mutableBlockPos.method_10103(playerBlockX, playerBlockY, playerBlockZ);
      guiGraphics.method_51448().pushMatrix();
      guiGraphics.method_51448().scale(1.0F / mapScale, 1.0F / mapScale);
      this.minimap.getInfoDisplays().getRenderer().render(minimapSession, this.minimap, height, size, pos, scaledX, scaledY, mapScale, guiGraphics);
      guiGraphics.method_51448().popMatrix();
   }

   private void renderCompass(class_4587 matrixStack, ModSettings settings, ClientConfigManager configManager, XaeroBufferProvider renderTypeBuffers, int specW, int specH, int halfFrame, double ps, double pc, boolean circleShape, float minimapScale) {
      class_4588 nameBgBuilder = renderTypeBuffers.getBuffer(CustomRenderTypes.RADAR_NAME_BGS);
      int compassScale = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.COMPASS_SCALE);
      int compassLocation = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.COMPASS_LOCATION);
      if (compassScale <= 0) {
         compassScale = compassLocation == 1 ? (int)Math.ceil((double)(minimapScale / 2.0F)) : (int)minimapScale;
      } else {
         compassScale = (int)MinimapConfigClientUtils.getUIScale(configManager, MinimapProfiledConfigOptions.COMPASS_SCALE);
      }

      if (compassLocation == 1) {
         if (class_310.method_1551().method_1573()) {
            compassScale *= 2;
         }

         halfFrame = (int)((float)halfFrame - 7.0F * minimapScale / 2.0F);
         this.compassRenderer.drawCompass(matrixStack, halfFrame - 3 * compassScale, halfFrame - 3 * compassScale, ps, pc, (double)1.0F, circleShape, (float)compassScale, true, renderTypeBuffers, nameBgBuilder);
      } else if (compassLocation == 2) {
         this.compassRenderer.drawCompass(matrixStack, specW, specH, ps, pc, this.zoom, circleShape, (float)compassScale, false, renderTypeBuffers, (class_4588)null);
      }

   }

   private void drawArrow(class_4587 matrixStack, float angle, double arrowX, double arrowY, float r, float g, float b, float a, class_12247.class_12337 tas, ClientConfigManager configManager) {
      matrixStack.method_22903();
      matrixStack.method_22904(arrowX, arrowY, (double)0.0F);
      OptimizedMath.rotatePose(matrixStack, angle, OptimizedMath.ZP);
      double arrowScale = (Double)configManager.getEffective(MinimapProfiledConfigOptions.ARROW_SCALE);
      matrixStack.method_22905((float)((double)0.5F * arrowScale), (float)((double)0.5F * arrowScale), 1.0F);
      int offsetY = -6;
      int h = 28;
      int ty = 0;
      matrixStack.method_46416(-13.0F, (float)offsetY, 0.0F);
      ImmediateRenderUtil.setShaderColor(r, g, b, a);
      GuiGraphicsUtils.blit(tas, matrixStack, 0, 0, 49.0F, (float)ty, 26, h, false);
      ImmediateRenderUtil.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      matrixStack.method_22909();
   }

   public double getZoom() {
      return this.zoom;
   }

   public void setZoom(double zoom) {
      this.zoom = zoom;
   }

   public float getSunBrightness(MinimapProcessor minimap, boolean lighting) {
      class_638 world = this.mc.field_1687;
      float sunBrightness = ((Float)MinimapClientWorldDataHelper.getWorldData(world).getAttributeSystem().method_75697(class_12206.field_64346, class_2338.field_10980) - 0.24F) / 0.76F;
      float ambient = world.method_8597().comp_656() * 24.0F / 15.0F;
      if (ambient > 1.0F) {
         ambient = 1.0F;
      }

      return ambient + (1.0F - ambient) * class_3532.method_15363(sunBrightness, 0.0F, 1.0F);
   }

   public MinimapRendererHelper getHelper() {
      return this.helper;
   }

   public abstract class_12247.class_12337 getTas();
}
