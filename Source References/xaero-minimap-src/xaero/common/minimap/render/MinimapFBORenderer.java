package xaero.common.minimap.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.class_10366;
import net.minecraft.class_1044;
import net.minecraft.class_12247;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_3879;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_5321;
import net.minecraft.class_630;
import net.minecraft.class_308.class_11274;
import org.joml.Matrix4fStack;
import xaero.common.HudMod;
import xaero.common.graphics.CustomRenderTypes;
import xaero.common.graphics.ImprovedFramebuffer;
import xaero.common.graphics.OpenGlHelper;
import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRenderer;
import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider;
import xaero.common.minimap.MinimapProcessor;
import xaero.common.minimap.region.MinimapChunk;
import xaero.common.misc.OptimizedMath;
import xaero.common.settings.ModSettings;
import xaero.hud.compat.mods.ImmediatelyFastHelper;
import xaero.hud.minimap.Minimap;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.common.config.MinimapConfigConstants;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.compass.render.CompassRenderer;
import xaero.hud.minimap.config.util.MinimapConfigClientUtils;
import xaero.hud.minimap.element.render.MinimapElementGraphics;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.element.render.map.MinimapElementMapRendererHandler;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.radar.icon.RadarIconManager;
import xaero.hud.minimap.radar.icon.creator.RadarIconCreator;
import xaero.hud.minimap.radar.render.element.RadarRenderer;
import xaero.hud.minimap.waypoint.render.WaypointMapRenderer;
import xaero.hud.render.TextureLocations;
import xaero.hud.render.util.MultiTextureRenderUtil;
import xaero.hud.render.util.RenderBufferUtil;
import xaero.lib.XaeroLib;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.client.graphics.GpuTextureAndView;
import xaero.lib.client.graphics.XaeroBufferProvider;
import xaero.lib.client.graphics.XaeroRenderType;
import xaero.lib.client.graphics.shader.FramebufferLinesShaderHelper;
import xaero.lib.client.graphics.shader.PositionTexAlphaTestShaderHelper;
import xaero.lib.client.graphics.util.ImmediateRenderUtil;
import xaero.lib.client.graphics.util.TextureUtils;

public class MinimapFBORenderer extends MinimapRenderer {
   private ImprovedFramebuffer scalingFramebuffer;
   private ImprovedFramebuffer rotationFramebuffer;
   private class_12247.class_12337 scalingFramebufferNearestTAS;
   private MinimapElementMapRendererHandler minimapElementMapRendererHandler;
   private RadarRenderer entityRadarRenderer;
   private RadarIconManager radarIconManager;
   private boolean triedFBO;
   private boolean loadedFBO;

   public MinimapFBORenderer(HudMod modMain, class_310 mc, WaypointMapRenderer waypointMapRenderer, Minimap minimap, CompassRenderer compassRenderer, class_4587 matrixStack) {
      super(modMain, mc, waypointMapRenderer, minimap, compassRenderer, matrixStack);
   }

   public void loadFrameBuffer(MinimapProcessor minimapProcessor) {
      if (!minimapProcessor.canUseFrameBuffer()) {
         MinimapLogs.LOGGER.info("FBO mode not supported! Using minimap safe mode.");
      } else {
         this.scalingFramebuffer = new ImprovedFramebuffer(512, 512, true);
         this.rotationFramebuffer = new ImprovedFramebuffer(512, 512, true);
         this.radarIconManager = new RadarIconManager(new RadarIconCreator());
         this.loadedFBO = this.scalingFramebuffer.method_30277() != null;
         this.minimapElementMapRendererHandler = ((MinimapElementMapRendererHandler.Builder)MinimapElementMapRendererHandler.Builder.begin().setPoseStack(this.matrixStack)).build();
         this.entityRadarRenderer = RadarRenderer.Builder.begin().setRadarIconManager(this.radarIconManager).setMinimap(this.minimap).build();
         this.minimapElementMapRendererHandler.add(this.entityRadarRenderer);
         this.minimap.getOverMapRendererHandler().add(this.entityRadarRenderer);
         if (this.modMain.getSupportMods().worldmap()) {
            this.modMain.getSupportMods().worldmapSupport.createRadarRenderWrapper(this.entityRadarRenderer);
         }

         if (this.loadedFBO) {
            this.scalingFramebuffer.setSampler(XaeroRenderType.getSimpleSampler(FilterMode.LINEAR));
            this.scalingFramebufferNearestTAS = new class_12247.class_12337(this.scalingFramebuffer.method_71639(), XaeroRenderType.getSimpleSampler(FilterMode.NEAREST));
            this.rotationFramebuffer.setSampler(XaeroRenderType.getSimpleSampler(FilterMode.LINEAR));
         }
      }

      this.triedFBO = true;
   }

   private void restoreDefaultTarget() {
      this.scalingFramebuffer.bindDefaultFramebuffer(class_310.method_1551());
   }

   protected void renderChunks(MinimapSession minimapSession, MinimapProcessor minimap, class_243 renderPos, class_5321<class_1937> mapDimension, double mapDimensionScale, int mapSize, int bufferSize, float sizeFix, float partial, int lightLevel, boolean useWorldMap, boolean lockedNorth, int shape, double ps, double pc, boolean cave, boolean circle, ModSettings settings, XaeroBufferProvider cvc) {
      synchronized(minimap.getMinimapWriter()) {
         try {
            this.renderChunksToFBO(minimapSession, this.matrixStack, minimap, renderPos, mapDimension, mapDimensionScale, mapSize, partial, lightLevel, useWorldMap, lockedNorth, shape, ps, pc, cave, cvc);
         } catch (Throwable t) {
            ImprovedFramebuffer.restoreMainRenderTarget();
            throw t;
         }
      }

      this.restoreDefaultTarget();
   }

   public void renderChunksToFBO(MinimapSession minimapSession, class_4587 matrixStack, MinimapProcessor minimap, class_243 renderPos, class_5321<class_1937> mapDimension, double mapDimensionScale, int viewW, float partial, int level, boolean useWorldMap, boolean lockedNorth, int shape, double ps, double pc, boolean cave, XaeroBufferProvider cvc) {
      ClientConfigManager configManager = this.modMain.getHudConfigs().getClientConfigManager();
      GpuBufferSlice projectionMatrixBackup = RenderSystem.getProjectionMatrixBuffer();
      class_10366 projectionTypeBackup = RenderSystem.getProjectionType();
      matrixStack.method_22903();
      matrixStack.method_34426();
      MultiTextureRenderTypeRendererProvider multiTextureRenderTypeRenderers = minimapSession.getMultiTextureRenderTypeRenderers();
      double maxVisibleLength = !lockedNorth && shape != 1 ? (double)viewW * Math.sqrt((double)2.0F) : (double)viewW;
      double halfMaxVisibleLength = maxVisibleLength / (double)2.0F;
      double radiusBlocks = halfMaxVisibleLength / this.zoom;
      int xFloored = OptimizedMath.myFloor(renderPos.field_1352);
      int zFloored = OptimizedMath.myFloor(renderPos.field_1350);
      if (this.zoom == (double)0.5F) {
         xFloored = xFloored >> 1 << 1;
         zFloored = zFloored >> 1 << 1;
      }

      int playerChunkX = xFloored >> 6;
      int playerChunkZ = zFloored >> 6;
      int offsetX = xFloored & 63;
      int offsetZ = zFloored & 63;
      boolean zooming = (double)((int)this.zoom) != this.zoom;
      ImmediatelyFastHelper.triggerBatchingBuffersFlush(matrixStack);
      this.scalingFramebuffer.bindAsMainTarget(true);
      TextureUtils.clearRenderTarget(this.scalingFramebuffer, 0, 1.0F);
      this.mc.field_1773.method_71114().method_71034(class_11274.field_60026);
      long before = System.currentTimeMillis();
      int clearAlpha = (int)((float)(Integer)configManager.getEffective(MinimapProfiledConfigOptions.UNDISCOVERED_OPACITY) / 100.0F * 255.0F);
      TextureUtils.clearRenderTarget(this.scalingFramebuffer, clearAlpha << 24, 1.0F);
      this.helper.defaultOrtho(this.scalingFramebuffer);
      Matrix4fStack shaderMatrixStack = RenderSystem.getModelViewStack();
      shaderMatrixStack.pushMatrix();
      shaderMatrixStack.identity();
      before = System.currentTimeMillis();
      double xInsidePixel = renderPos.field_1352 - (double)xFloored;
      double zInsidePixel = renderPos.field_1350 - (double)zFloored;
      float halfWView = (float)viewW / 2.0F;
      float angle = (float)((double)90.0F - this.getRenderAngle(lockedNorth));
      shaderMatrixStack.translate(256.0F, 256.0F, -2000.0F);
      shaderMatrixStack.scale((float)this.zoom, (float)this.zoom, 1.0F);
      ImmediateRenderUtil.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      XaeroBufferProvider renderTypeBuffers = XaeroLib.INSTANCE.getClient().getBufferProvider();
      class_4588 overlayBufferBuilder = renderTypeBuffers.getBuffer(CustomRenderTypes.MAP_CHUNK_OVERLAY);
      float chunkGridAlphaMultiplier = 1.0F;
      int minX = playerChunkX + (int)Math.floor(((double)offsetX - radiusBlocks) / (double)64.0F);
      int minZ = playerChunkZ + (int)Math.floor(((double)offsetZ - radiusBlocks) / (double)64.0F);
      int maxX = playerChunkX + (int)Math.floor(((double)(offsetX + 1) + radiusBlocks) / (double)64.0F);
      int maxZ = playerChunkZ + (int)Math.floor(((double)(offsetZ + 1) + radiusBlocks) / (double)64.0F);
      if (!cave || MinimapConfigClientUtils.getEffectiveCaveModeAllowed()) {
         if (useWorldMap) {
            chunkGridAlphaMultiplier = this.modMain.getSupportMods().worldmapSupport.getMinimapBrightness();
            this.modMain.getSupportMods().worldmapSupport.drawMinimap(minimapSession, matrixStack, this.getHelper(), xFloored, zFloored, minX, minZ, maxX, maxZ, zooming, this.zoom, mapDimensionScale, overlayBufferBuilder, multiTextureRenderTypeRenderers);
         } else if (minimap.getMinimapWriter().getLoadedBlocks() != null && level >= 0) {
            int loadedLevels = minimap.getMinimapWriter().getLoadedLevels();
            chunkGridAlphaMultiplier = loadedLevels <= 1 ? 1.0F : 0.375F + 0.625F * (1.0F - (float)level / (float)(loadedLevels - 1));
            int loadedMapChunkX = minimap.getMinimapWriter().getLoadedMapChunkX();
            int loadedMapChunkZ = minimap.getMinimapWriter().getLoadedMapChunkZ();
            int loadedWidth = minimap.getMinimapWriter().getLoadedBlocks().length;
            boolean slimeChunks = MinimapConfigClientUtils.getEffectiveSlimeChunks(minimapSession);
            int loadedMinX = Math.max(minX, loadedMapChunkX);
            int loadedMinZ = Math.max(minZ, loadedMapChunkZ);
            int loadedMaxX = Math.min(maxX, loadedMapChunkX + loadedWidth - 1);
            int loadedMaxZ = Math.min(maxZ, loadedMapChunkZ + loadedWidth - 1);
            class_1044 guiTextures = this.mc.method_1531().method_4619(TextureLocations.GUI_TEXTURES);
            MultiTextureRenderTypeRenderer multiTextureRenderTypeRenderer = multiTextureRenderTypeRenderers.getRenderer(MultiTextureRenderTypeRendererProvider::defaultTextureBind, zooming ? CustomRenderTypes.MINIMAP_ZOOM : CustomRenderTypes.MINIMAP);
            MinimapRendererHelper helper = this.getHelper();

            for(int X = loadedMinX; X <= loadedMaxX; ++X) {
               int canvasX = X - minimap.getMinimapWriter().getLoadedMapChunkX();

               for(int Z = loadedMinZ; Z <= loadedMaxZ; ++Z) {
                  int canvasZ = Z - minimap.getMinimapWriter().getLoadedMapChunkZ();
                  MinimapChunk mchunk = minimap.getMinimapWriter().getLoadedBlocks()[canvasX][canvasZ];
                  if (mchunk != null) {
                     GpuTextureAndView texture = mchunk.bindTexture(level);
                     if (mchunk.isHasSomething() && level < mchunk.getLevelsBuffered() && texture != null) {
                        int drawX = (X - playerChunkX) * 64 - offsetX;
                        int drawZ = (Z - playerChunkZ) * 64 - offsetZ;
                        MultiTextureRenderUtil.prepareTexturedColoredRect(matrixStack.method_23760().method_23761(), (float)drawX, (float)drawZ, 0, 64, 64.0F, 64.0F, -64.0F, 64.0F, 1.0F, 1.0F, 1.0F, 1.0F, texture.texture, multiTextureRenderTypeRenderer);
                        if (slimeChunks) {
                           for(int t = 0; t < 16; ++t) {
                              if (mchunk.getTile(t % 4, t / 4) != null && mchunk.getTile(t % 4, t / 4).isSlimeChunk()) {
                                 int slimeDrawX = drawX + 16 * (t % 4);
                                 int slimeDrawZ = drawZ + 16 * (t / 4);
                                 RenderBufferUtil.addColoredRect(matrixStack.method_23760().method_23761(), overlayBufferBuilder, (float)slimeDrawX, (float)slimeDrawZ, 16, 16, -2142047936);
                              }
                           }
                        }
                     }
                  }
               }
            }

            multiTextureRenderTypeRenderers.draw(multiTextureRenderTypeRenderer);
         }
      }

      int chunkGridConfig = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.CHUNK_GRID);
      if (chunkGridConfig > -1) {
         class_4588 lineBufferBuilder = renderTypeBuffers.getBuffer(CustomRenderTypes.MAP_LINES);
         int grid = MinimapConfigConstants.COLORS[chunkGridConfig];
         int r = grid >> 16 & 255;
         int g = grid >> 8 & 255;
         int b = grid & 255;
         FramebufferLinesShaderHelper.setFrameSize((float)this.scalingFramebuffer.field_1482, (float)this.scalingFramebuffer.field_1481);
         float red = (float)r / 255.0F;
         float green = (float)g / 255.0F;
         float blue = (float)b / 255.0F;
         float alpha = 0.8F;
         red *= chunkGridAlphaMultiplier;
         green *= chunkGridAlphaMultiplier;
         blue *= chunkGridAlphaMultiplier;
         int chunkGridLineWidthConfig = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.CHUNK_GRID_LINE_WIDTH);
         int bias = 1;
         class_4587.class_4665 matrices = matrixStack.method_23760();
         float halfLineLength = (float)radiusBlocks;

         for(int X = minX; X <= maxX; ++X) {
            int drawX = (X - playerChunkX + 1) * 64 - offsetX;

            for(int i = 0; i < 4; ++i) {
               float lineX = (float)drawX + (float)(-16 * i);
               this.helper.addColoredLineToExistingBuffer(matrices, lineBufferBuilder, lineX, -halfLineLength, lineX, halfLineLength + (float)bias, red, green, blue, alpha, (float)chunkGridLineWidthConfig);
            }
         }

         for(int Z = minZ; Z <= maxZ; ++Z) {
            int drawZ = (Z - playerChunkZ + 1) * 64 - offsetZ;

            for(int i = 0; i < 4; ++i) {
               float lineZ = (float)drawZ + (float)((double)(-16 * i) - (double)1.0F / this.zoom);
               this.helper.addColoredLineToExistingBuffer(matrices, lineBufferBuilder, -halfLineLength, lineZ, halfLineLength + (float)bias, lineZ, red, green, blue, alpha, (float)chunkGridLineWidthConfig);
            }
         }
      }

      renderTypeBuffers.endBatch();
      this.rotationFramebuffer.bindAsMainTarget(false);
      TextureUtils.clearRenderTarget(this.rotationFramebuffer, 0, 1.0F);
      shaderMatrixStack.identity();
      boolean antiAliasing = (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.ANTI_ALIASING);
      class_12247.class_12337 scalingFramebufferTAS;
      if (antiAliasing) {
         scalingFramebufferTAS = this.scalingFramebuffer.getTas();
      } else {
         scalingFramebufferTAS = this.scalingFramebufferNearestTAS;
      }

      shaderMatrixStack.translate(halfWView, halfWView, -2980.0F);
      shaderMatrixStack.pushMatrix();
      if (!lockedNorth) {
         OptimizedMath.rotateMatrix(shaderMatrixStack, -angle, OptimizedMath.ZP);
      }

      shaderMatrixStack.translate((float)(-xInsidePixel * this.zoom), (float)(-zInsidePixel * this.zoom), 0.0F);
      int opacityConfig = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.OPACITY);
      float opacityF = (float)opacityConfig / 100.0F;
      ImmediateRenderUtil.setShaderColor(opacityF, opacityF, opacityF, opacityF);
      PositionTexAlphaTestShaderHelper.setDiscardAlpha(0.0F);
      ImmediateRenderUtil.texturedRect(matrixStack, -256.0F, -256.0F, 0, 0, 512.0F, 512.0F, 512.0F, 512.0F, XaeroRenderType.RP_POSITION_TEX_ALPHA_PRE, scalingFramebufferTAS);
      ImmediateRenderUtil.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      shaderMatrixStack.popMatrix();
      before = System.currentTimeMillis();
      OpenGlHelper.fixOtherMods();
      this.minimapElementMapRendererHandler.prepareRender(ps, pc, this.zoom, halfWView);
      this.minimapElementMapRendererHandler.render(renderPos, partial, this.rotationFramebuffer, mapDimensionScale, mapDimension);
      renderTypeBuffers.endBatch();
      ImmediatelyFastHelper.triggerBatchingBuffersFlush(matrixStack);
      ImmediateRenderUtil.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.setProjectionMatrix(projectionMatrixBackup, projectionTypeBackup);
      shaderMatrixStack.popMatrix();
      matrixStack.method_22909();
   }

   public void deleteFramebuffers() {
      this.scalingFramebuffer.method_1238();
      this.rotationFramebuffer.method_1238();
      if (this.radarIconManager != null) {
         this.radarIconManager.reset();
      }

   }

   public boolean isLoadedFBO() {
      return this.loadedFBO;
   }

   public void setLoadedFBO(boolean loadedFBO) {
      this.loadedFBO = loadedFBO;
   }

   public boolean isTriedFBO() {
      return this.triedFBO;
   }

   public boolean assumeUsingFBO() {
      boolean mapSafeMode = (Boolean)this.modMain.getHudConfigs().getClientConfigManager().getEffective(MinimapProfiledConfigOptions.SAFE_MODE);
      return !this.isTriedFBO() && !mapSafeMode || this.minimap.usingFBO();
   }

   public void resetEntityIcons() {
      if (this.radarIconManager != null) {
         this.radarIconManager.reset();
      }

   }

   public void resetEntityIconsResources() {
      if (this.radarIconManager != null) {
         this.radarIconManager.resetResources();
      }

   }

   public void onRadarIconModelRenderTrace(class_3879 model, class_4588 vertexConsumer, int color) {
      this.radarIconManager.onModelRenderTrace(model, vertexConsumer, color);
   }

   public void onEntityIconModelPartRenderTrace(class_630 modelRenderer, int color) {
      this.radarIconManager.onModelPartRenderTrace(modelRenderer, color);
   }

   public void renderMainEntityDot(class_1297 renderEntity, boolean cave, XaeroBufferProvider renderTypeBuffers) {
      MinimapElementGraphics guiGraphics = this.minimapElementMapRendererHandler.getGuiGraphics();
      guiGraphics.pose().method_22903();
      this.entityRadarRenderer.renderSingleEntity(renderEntity, cave, false, 2.0F, false, false, MinimapElementRenderLocation.OVER_MINIMAP, (class_276)null, guiGraphics, false);
      guiGraphics.flush();
      guiGraphics.pose().method_22909();
   }

   public class_12247.class_12337 getTas() {
      return this.rotationFramebuffer.getTas();
   }

   public RadarRenderer getEntityRadarRenderer() {
      return this.entityRadarRenderer;
   }
}
