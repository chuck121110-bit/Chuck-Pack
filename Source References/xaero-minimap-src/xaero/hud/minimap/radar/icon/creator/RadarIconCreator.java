package xaero.hud.minimap.radar.icon.creator;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import net.minecraft.class_10017;
import net.minecraft.class_10042;
import net.minecraft.class_10366;
import net.minecraft.class_11278;
import net.minecraft.class_12137;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_583;
import net.minecraft.class_897;
import net.minecraft.class_308.class_11274;
import xaero.common.exception.OpenGLException;
import xaero.common.graphics.ImprovedFramebuffer;
import xaero.common.icon.XaeroIcon;
import xaero.common.icon.XaeroIconAtlas;
import xaero.common.icon.XaeroIconAtlasManager;
import xaero.common.minimap.render.MinimapRendererHelper;
import xaero.hud.compat.mods.ImmediatelyFastHelper;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.element.render.MinimapElementGraphics;
import xaero.hud.minimap.radar.icon.RadarIconManager;
import xaero.hud.minimap.radar.icon.creator.render.form.IRadarIconFormPrerenderer;
import xaero.hud.minimap.radar.icon.creator.render.trace.EntityRenderTracer;
import xaero.hud.minimap.radar.icon.creator.render.trace.ModelRenderTrace;
import xaero.hud.minimap.radar.icon.definition.form.RadarIconForm;
import xaero.hud.minimap.radar.icon.definition.form.model.config.RadarIconModelConfig;
import xaero.lib.client.graphics.XaeroRenderType;
import xaero.lib.client.graphics.shader.PositionTexAlphaTestShaderHelper;
import xaero.lib.client.graphics.util.ImmediateRenderUtil;
import xaero.lib.client.graphics.util.TextureUtils;

public class RadarIconCreator {
   private static final int PREFERRED_ATLAS_WIDTH = 1024;
   public static final int ICON_WIDTH = 64;
   public static final int FAR_PLANE = 500;
   private ImprovedFramebuffer formRenderFramebuffer;
   private ImprovedFramebuffer iconRenderFramebuffer;
   private ImprovedFramebuffer atlasRenderFramebuffer;
   private final EntityRenderTracer renderTracer = new EntityRenderTracer();
   private final XaeroIconAtlasManager iconAtlasManager;
   private GpuBufferSlice projectionMatrixBackup;
   private class_10366 projectionTypeBackup;
   private final class_11278 iconOrthoProjectionCache;
   private final class_11278 atlasOrthoProjectionCache;

   public RadarIconCreator() {
      int maxTextureSize = RenderSystem.getDevice().getMaxTextureSize();
      int atlasTextureSize = Math.min(maxTextureSize, 1024) / 64 * 64;
      this.iconAtlasManager = new XaeroIconAtlasManager(64, atlasTextureSize, new ArrayList());
      this.initFramebuffers(atlasTextureSize);
      this.iconOrthoProjectionCache = new class_11278("icon creator ortho", -1.0F, 500.0F, true);
      this.atlasOrthoProjectionCache = new class_11278("icon creator atlas ortho", -1.0F, 500.0F, true);
   }

   public <S extends class_10017> XaeroIcon create(MinimapElementGraphics guiGraphics, class_897<?, ? super S> entityRenderer, S entityRenderState, class_1297 entity, class_276 defaultFramebuffer, Parameters parameters) {
      IRadarIconFormPrerenderer formPrerenderer = parameters.form.getPrerenderer();
      if (formPrerenderer == null) {
         MinimapLogs.LOGGER.error("Tried prerendering radar icon for {} variant {} but the icon form used doesn't have a prerenderer!", class_1299.method_5890(entity.method_5864()), parameters.variant);
         return RadarIconManager.FAILED;
      } else {
         OpenGLException.checkGLError();
         class_4587 matrixStack = guiGraphics.pose();
         guiGraphics.flush();
         ImmediatelyFastHelper.triggerBatchingBuffersFlush(matrixStack);
         this.formRenderFramebuffer.bindAsMainTarget(true);
         this.setupMatrices(matrixStack, 64, 500);
         OpenGLException.checkGLError();
         if (entityRenderState instanceof class_10042) {
            class_10042 livingEntityRenderState = (class_10042)entityRenderState;
            livingEntityRenderState.field_53457 = false;
         }

         List<ModelRenderTrace> traceResult = null;
         class_583<S> entityModel = null;
         if (formPrerenderer.requiresEntityModel()) {
            if (class_310.method_1551().method_1561().field_4686 != null) {
               traceResult = this.renderTracer.trace(matrixStack, entity, entityRenderer, entityRenderState);
               this.formRenderFramebuffer.bindAsMainTarget(true);
            } else {
               MinimapLogs.LOGGER.info("Render info was null for entity " + entity.method_5820());
            }

            entityModel = this.renderTracer.<S>getEntityRendererModel(entityRenderer);
            if (entityModel == null) {
               this.endFormRendering();
               this.bindDefaultFramebuffer(defaultFramebuffer);
               this.restoreMatrices(matrixStack);
               return RadarIconManager.FAILED;
            }
         }

         boolean formRenderResult = false;
         class_4587.class_4665 matrixEntryToRestore = matrixStack.method_23760();
         matrixStack.method_22903();

         try {
            TextureUtils.clearRenderTarget(this.formRenderFramebuffer, 0, 1.0F);
            formRenderResult = formPrerenderer.prerender(guiGraphics, entityRenderer, entityRenderState, entityModel, entity, traceResult, parameters);
         } catch (Throwable t) {
            MinimapLogs.LOGGER.error("Exception using the radar icon form prerenderer for entity {} variant {}!", class_1299.method_5890(entity.method_5864()), parameters.variant, t);
         } finally {
            guiGraphics.flush();

            while(matrixStack.method_23760() != matrixEntryToRestore) {
               matrixStack.method_22909();
            }

         }

         this.endFormRendering();
         XaeroIcon icon = parameters.form.getFailureResult();
         if (formRenderResult) {
            icon = this.getFinalIcon(matrixStack, entity, formPrerenderer, parameters);
         }

         this.restoreMatrices(matrixStack);
         this.bindDefaultFramebuffer(defaultFramebuffer);
         return icon;
      }
   }

   private XaeroIcon getFinalIcon(class_4587 matrixStack, class_1297 entity, IRadarIconFormPrerenderer formPrerenderer, Parameters parameters) {
      this.iconRenderFramebuffer.bindAsMainTarget(true);
      TextureUtils.clearRenderTarget(this.iconRenderFramebuffer, 0, 1.0F);
      if (parameters.debug) {
         matrixStack.method_22903();
         matrixStack.method_46416(18.0F, 10.0F, -10.0F);
         matrixStack.method_22905(1.0F, 1.0F, 1.0F);
         ImmediateRenderUtil.coloredRectangle(matrixStack, 0.0F, 0.0F, 9.0F, 9.0F, -16776961);
         matrixStack.method_22909();
      }

      boolean outlined = formPrerenderer.isOutlined();
      boolean flipped = formPrerenderer.isFlipped();
      if (outlined) {
         this.renderOutline(matrixStack);
      }

      ImmediateRenderUtil.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      PositionTexAlphaTestShaderHelper.setDiscardAlpha(0.05F);
      ImmediateRenderUtil.texturedRect(matrixStack, 0.0F, 0.0F, 0, 0, 64.0F, 64.0F, 64.0F, 64.0F, XaeroRenderType.RP_POSITION_TEX_ALPHA_NO_BLEND, this.formRenderFramebuffer.getTas());
      if (parameters.debug) {
         matrixStack.method_22903();
         matrixStack.method_46416(27.0F, 10.0F, -10.0F);
         matrixStack.method_22905(1.0F, 1.0F, 1.0F);
         ImmediateRenderUtil.coloredRectangle(matrixStack, 0.0F, 0.0F, 9.0F, 9.0F, -16711681);
         matrixStack.method_22909();
      }

      this.iconRenderFramebuffer.generateMipmaps();
      XaeroIcon icon = null;

      try {
         XaeroIconAtlas atlas = this.getCurrentAtlas();
         icon = atlas.createIcon();
         this.atlasRenderFramebuffer.bindAsMainTarget(false);
         TextureUtils.clearRenderTargetDepth(this.atlasRenderFramebuffer, 1.0F);
         GpuBufferSlice ortho = this.atlasOrthoProjectionCache.method_71092((float)this.atlasRenderFramebuffer.field_1482, (float)this.atlasRenderFramebuffer.field_1481);
         RenderSystem.setProjectionMatrix(ortho, class_10366.field_54954);
         matrixStack.method_46416((float)icon.getOffsetX(), (float)(this.atlasRenderFramebuffer.field_1481 - 64 - icon.getOffsetY()), 0.0F);
         this.atlasRenderFramebuffer.setColorTexture(atlas);
         if (flipped) {
            ImmediateRenderUtil.texturedRect(matrixStack, 0.0F, 0.0F, 0, 64, 64.0F, 64.0F, -64.0F, 64.0F, XaeroRenderType.RP_POSITION_TEX_NO_ALPHA_NO_BLEND, this.iconRenderFramebuffer.getTas());
         } else {
            ImmediateRenderUtil.texturedRect(matrixStack, 0.0F, 0.0F, 0, 0, 64.0F, 64.0F, 64.0F, 64.0F, XaeroRenderType.RP_POSITION_TEX_NO_ALPHA_NO_BLEND, this.iconRenderFramebuffer.getTas());
         }

         if (parameters.debug) {
            matrixStack.method_22903();
            matrixStack.method_46416(36.0F, 10.0F, -10.0F);
            matrixStack.method_22905(1.0F, 1.0F, 1.0F);
            ImmediateRenderUtil.coloredRectangle(matrixStack, 0.0F, 0.0F, 9.0F, 9.0F, -256);
            matrixStack.method_22909();
         }
      } catch (Throwable t) {
         MinimapLogs.LOGGER.error("Exception rendering to a entity icon atlas for {} {}!", class_1299.method_5890(entity.method_5864()), parameters.variant, t);
      }

      MinimapRendererHelper.restoreDefaultShaderBlendState();
      return icon;
   }

   private void renderOutline(class_4587 matrixStack) {
      ImmediateRenderUtil.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);

      for(int shadowOffsetX = -1; shadowOffsetX < 2; ++shadowOffsetX) {
         for(int shadowOffsetY = -1; shadowOffsetY < 2; ++shadowOffsetY) {
            if (shadowOffsetX != 0 || shadowOffsetY != 0) {
               ImmediateRenderUtil.drawOutlineLayer(matrixStack, (float)shadowOffsetX, (float)shadowOffsetY, 0, 0, 64.0F, 64.0F, 64.0F, 64.0F, this.formRenderFramebuffer.getTas(), 0.05F);
            }
         }
      }

   }

   private void setupMatrices(class_4587 matrixStack, int finalIconSize, int farPlane) {
      this.projectionMatrixBackup = RenderSystem.getProjectionMatrixBuffer();
      this.projectionTypeBackup = RenderSystem.getProjectionType();
      matrixStack.method_22903();
      matrixStack.method_34426();
      GpuBufferSlice ortho = this.iconOrthoProjectionCache.method_71092((float)finalIconSize, (float)finalIconSize);
      RenderSystem.setProjectionMatrix(ortho, class_10366.field_54954);
      RenderSystem.getModelViewStack().pushMatrix();
      RenderSystem.getModelViewStack().identity();
   }

   private void restoreMatrices(class_4587 matrixStack) {
      matrixStack.method_22909();
      RenderSystem.setProjectionMatrix(this.projectionMatrixBackup, this.projectionTypeBackup);
      RenderSystem.getModelViewStack().popMatrix();
   }

   private void endFormRendering() {
      class_310.method_1551().field_1773.method_71114().method_71034(class_11274.field_60026);
   }

   private void bindDefaultFramebuffer(class_276 defaultFramebuffer) {
      if (defaultFramebuffer != null) {
         if (defaultFramebuffer instanceof ImprovedFramebuffer) {
            ((ImprovedFramebuffer)defaultFramebuffer).bindAsMainTarget(true);
         } else {
            ImprovedFramebuffer.restoreMainRenderTarget();
         }
      } else {
         this.atlasRenderFramebuffer.bindDefaultFramebuffer(class_310.method_1551());
      }
   }

   public void reset() {
      this.iconAtlasManager.clearAtlases();
      this.atlasRenderFramebuffer.setColorTexture((GpuTexture)null, (GpuTextureView)null);
      this.renderTracer.reset();
   }

   public EntityRenderTracer getRenderTracer() {
      return this.renderTracer;
   }

   private XaeroIconAtlas getCurrentAtlas() {
      return this.iconAtlasManager.getCurrentAtlas();
   }

   private void initFramebuffers(int atlasTextureSize) {
      this.formRenderFramebuffer = new ImprovedFramebuffer(512, 512, true);
      this.iconRenderFramebuffer = new ImprovedFramebuffer(512, 512, true);
      this.formRenderFramebuffer.setSampler(XaeroRenderType.getSimpleSampler(FilterMode.NEAREST));
      String iconRenderColorTextureName = this.iconRenderFramebuffer.method_30277().getLabel();
      this.iconRenderFramebuffer.closeColorTexture();
      GpuTexture iconRenderColorTexture = RenderSystem.getDevice().createTexture(iconRenderColorTextureName, 15, TextureFormat.RGBA8, 512, 512, 1, 4);
      GpuTextureView iconRenderColorTextureView = RenderSystem.getDevice().createTextureView(iconRenderColorTexture);
      this.iconRenderFramebuffer.setColorTexture(iconRenderColorTexture, iconRenderColorTextureView);
      class_12137 iconRenderSampler = RenderSystem.getDevice().createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST, FilterMode.NEAREST, 1, OptionalDouble.of((double)4.0F));
      this.iconRenderFramebuffer.setSampler(iconRenderSampler);
      this.atlasRenderFramebuffer = new ImprovedFramebuffer(atlasTextureSize, atlasTextureSize, true);
      this.atlasRenderFramebuffer.closeColorTexture();
      this.atlasRenderFramebuffer.setColorTexture((GpuTexture)null, (GpuTextureView)null);
   }

   public static class Parameters {
      public final Object variant;
      public final float scale;
      public final RadarIconModelConfig defaultModelConfig;
      public final RadarIconForm form;
      public final boolean debug;

      public Parameters(Object variant, RadarIconModelConfig defaultModelConfig, RadarIconForm form, float scale, boolean debug) {
         this.variant = variant;
         this.scale = scale;
         this.defaultModelConfig = defaultModelConfig;
         this.form = form;
         this.debug = debug;
      }
   }
}
