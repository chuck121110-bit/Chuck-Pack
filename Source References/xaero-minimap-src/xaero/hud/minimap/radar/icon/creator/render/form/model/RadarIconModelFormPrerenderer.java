package xaero.hud.minimap.radar.icon.creator.render.form.model;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.class_10017;
import net.minecraft.class_10042;
import net.minecraft.class_1058;
import net.minecraft.class_12137;
import net.minecraft.class_12245;
import net.minecraft.class_12250;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3879;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_583;
import net.minecraft.class_591;
import net.minecraft.class_630;
import net.minecraft.class_897;
import net.minecraft.class_922;
import net.minecraft.class_9799;
import net.minecraft.class_308.class_11274;
import xaero.common.exception.OpenGLException;
import xaero.common.misc.OptimizedMath;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.element.render.MinimapElementGraphics;
import xaero.hud.minimap.radar.icon.creator.RadarIconCreator;
import xaero.hud.minimap.radar.icon.creator.entity.LivingEntityPoseResetter;
import xaero.hud.minimap.radar.icon.creator.render.form.IRadarIconFormPrerenderer;
import xaero.hud.minimap.radar.icon.creator.render.form.model.custom.RadarIconCustomPrerenderer;
import xaero.hud.minimap.radar.icon.creator.render.form.model.part.RadarIconModelPartPrerenderer;
import xaero.hud.minimap.radar.icon.creator.render.trace.ModelRenderTrace;
import xaero.hud.minimap.radar.icon.definition.BuiltInRadarIconDefinitions;
import xaero.hud.minimap.radar.icon.definition.form.model.RadarIconModelForm;
import xaero.hud.minimap.radar.icon.definition.form.model.config.RadarIconModelConfig;
import xaero.lib.client.graphics.CustomTextureBinding;
import xaero.lib.client.graphics.ITextureBinding;
import xaero.lib.client.graphics.XaeroRenderType;
import xaero.lib.client.graphics.util.ImmediateRenderUtil;

public class RadarIconModelFormPrerenderer implements IRadarIconFormPrerenderer {
   private final LivingEntityPoseResetter livingEntityPoseResetter = new LivingEntityPoseResetter();
   private ModelRenderTrace mainModelTrace;
   private final ArrayList<class_630> mainRenderedModels = new ArrayList();
   private final RadarIconModelPrerenderer modelPrerenderer = new RadarIconModelPrerenderer();
   private class_630 mainPart;
   private List<String> hardcodedMainPartAliases;
   private List<String> hardcodedModelPartsFields;
   private boolean forceFieldCheck;
   private boolean fullModelIcon;
   private class_4597.class_4598 entityIconRenderTypeBuffer = class_4597.method_22991(new class_9799(256));

   public boolean requiresEntityModel() {
      return true;
   }

   public boolean isFlipped() {
      return false;
   }

   public boolean isOutlined() {
      return true;
   }

   public <S extends class_10017> boolean prerender(MinimapElementGraphics guiGraphics, class_897<?, ? super S> entityRenderer, S entityRenderState, @Nullable class_583<S> entityModel, class_1297 entity, @Nullable List<ModelRenderTrace> traceList, RadarIconCreator.Parameters parameters) {
      class_4587 matrixStack = guiGraphics.pose();
      RadarIconModelForm modelForm = (RadarIconModelForm)parameters.form;
      class_4597.class_4598 renderTypeBuffer = this.entityIconRenderTypeBuffer;
      class_310.method_1551().field_1773.method_71114().method_71034(class_11274.field_60026);
      if (parameters.debug) {
         matrixStack.method_22903();
         matrixStack.method_46416(0.0F, 10.0F, -10.0F);
         matrixStack.method_22905(1.0F, 1.0F, 1.0F);
         ImmediateRenderUtil.coloredRectangle(matrixStack, 0.0F, 0.0F, 9.0F, 9.0F, -65536);
         matrixStack.method_22909();
      }

      RadarIconModelConfig config = parameters.defaultModelConfig;
      RadarIconModelConfig variantModelConfig = modelForm.getConfig();
      if (variantModelConfig != null) {
         config = variantModelConfig;
      }

      matrixStack.method_22903();
      matrixStack.method_46416(32.0F, 32.0F, -450.0F);
      matrixStack.method_46416(config.offsetX, config.offsetY, 0.0F);
      int mainScale = 32;
      matrixStack.method_22905((float)mainScale, (float)mainScale, (float)(-mainScale));
      float scale = parameters.scale;
      if (scale < 1.0F) {
         matrixStack.method_22905(scale, scale, scale);
      }

      matrixStack.method_22905(config.baseScale, config.baseScale, config.baseScale);
      OptimizedMath.rotatePose(matrixStack, config.rotationY, OptimizedMath.YP);
      OptimizedMath.rotatePose(matrixStack, config.rotationX, OptimizedMath.XP);
      OptimizedMath.rotatePose(matrixStack, config.rotationZ, OptimizedMath.ZP);
      BuiltInRadarIconDefinitions.defaultTransformation(matrixStack, entityModel, entity);
      if (entityRenderState instanceof class_10042 livingEntityRenderState) {
         this.livingEntityPoseResetter.resetValues(livingEntityRenderState);
      }

      boolean result = this.renderLayers(matrixStack, renderTypeBuffer, entityRenderer, entityRenderState, entityModel, traceList, entity, config, parameters.defaultModelConfig);
      BuiltInRadarIconDefinitions.defaultPostIconModelRender(matrixStack, entityModel, entity);
      matrixStack.method_22909();
      if (parameters.debug) {
         matrixStack.method_22903();
         matrixStack.method_46416(9.0F, 10.0F, -10.0F);
         matrixStack.method_22905(1.0F, 1.0F, 1.0F);
         ImmediateRenderUtil.coloredRectangle(matrixStack, 0.0F, 0.0F, 9.0F, 9.0F, -16711936);
         matrixStack.method_22909();
      }

      return result;
   }

   private <S extends class_10017> boolean renderLayers(class_4587 matrixStack, class_4597.class_4598 bufferSource, class_897<?, ? super S> entityRenderer, S entityRenderState, class_583<S> mainEntityModel, List<ModelRenderTrace> traceList, class_1297 entity, RadarIconModelConfig config, RadarIconModelConfig defaultConfig) {
      this.forceFieldCheck = (config.renderingFullModel == null || !config.renderingFullModel) && (config.modelPartsFields != null || BuiltInRadarIconDefinitions.forceFieldCheck(mainEntityModel));
      this.fullModelIcon = config.renderingFullModel == null ? !this.forceFieldCheck && BuiltInRadarIconDefinitions.fullModelIcon(mainEntityModel) : config.renderingFullModel;
      boolean renderedSomething = false;
      if (traceList.isEmpty()) {
         this.addDefaultLayer(traceList, entityRenderer, entityRenderState, mainEntityModel, entity);
      }

      boolean allEmpty = true;

      for(ModelRenderTrace mrt : traceList) {
         if (!mrt.isEmpty()) {
            allEmpty = false;
            break;
         }
      }

      if (allEmpty) {
         for(ModelRenderTrace mrt : traceList) {
            mrt.allVisible = true;
         }
      }

      this.mainPart = null;
      this.mainModelTrace = null;
      this.hardcodedMainPartAliases = BuiltInRadarIconDefinitions.getMainModelPartFields(entityRenderer, mainEntityModel, entity);
      this.hardcodedModelPartsFields = BuiltInRadarIconDefinitions.getSecondaryModelPartsFields(entityRenderer, mainEntityModel, entity);
      this.mainRenderedModels.clear();

      for(ModelRenderTrace mrt : traceList) {
         if (!mrt.isEmpty() && (!renderedSomething || config.layersAllowed)) {
            int result = this.renderLayer(matrixStack, bufferSource, mrt, entityRenderState, mainEntityModel, entity, config, defaultConfig);
            if (result == -1) {
               break;
            }

            if (result == 1) {
               renderedSomething = true;
            }
         }
      }

      this.hardcodedMainPartAliases = null;
      this.hardcodedModelPartsFields = null;
      if (!this.mainRenderedModels.isEmpty() && config.layersAllowed) {
         RadarIconCustomPrerenderer extraLayer = BuiltInRadarIconDefinitions.getCustomLayer(entityRenderer, entity);
         RadarIconModelPartPrerenderer partPrerenderer = this.modelPrerenderer.getPartPrerenderer();
         if (extraLayer != null) {
            this.mainPart = extraLayer.render(matrixStack, bufferSource, entityRenderer, entityRenderState, entity, mainEntityModel, partPrerenderer, this.mainRenderedModels, this.mainPart, config, this.mainModelTrace);
         }

         return renderedSomething;
      } else {
         return renderedSomething;
      }
   }

   private <S extends class_10017> void addDefaultLayer(List<ModelRenderTrace> traceList, class_897<?, ? super S> entityRenderer, S entityRenderState, class_583<S> mainEntityModel, class_1297 entity) {
      class_2960 mainEntityTexture = null;

      try {
         class_2960 var10000;
         if (entityRenderer instanceof class_922 livingEntityRenderer) {
            var10000 = livingEntityRenderer.method_3885((class_10042)entityRenderState);
         } else {
            var10000 = null;
         }

         mainEntityTexture = var10000;
      } catch (Throwable t) {
         MinimapLogs.LOGGER.error("Couldn't fetch main entity texture when prerendering an icon with nothing detected!", t);
      }

      if (mainEntityTexture != null) {
         Map<String, ITextureBinding> textures = new HashMap();
         textures.put("Sampler0", new CustomTextureBinding(mainEntityTexture, () -> null));
         RenderPipeline basicPipeline = XaeroRenderType.getBasicRenderPipeline();
         traceList.add(new ModelRenderTrace(mainEntityModel, textures, class_12250.field_64068, true, true, true, class_12245.field_63975, (class_1058)null, basicPipeline, -1));
      }
   }

   private <S extends class_10017> int renderLayer(class_4587 matrixStack, class_4597.class_4598 bufferSource, ModelRenderTrace mrt, S entityRenderState, class_583<S> mainEntityModel, class_1297 entity, RadarIconModelConfig config, RadarIconModelConfig defaultConfig) {
      class_3879 traceModel = mrt.model;
      if (entity instanceof class_1657 && traceModel != mainEntityModel && traceModel instanceof class_591) {
         return 0;
      } else {
         Map<String, ITextureBinding> traceTextures = mrt.textures;
         class_1058 traceAtlasSprite = mrt.renderAtlasSprite;
         boolean mainModel = traceModel == mainEntityModel;
         boolean mainPartsVisibility = mainModel && this.mainModelTrace != null && mrt.sameVisibility(this.mainModelTrace);
         if (mainModel && !mainPartsVisibility) {
            if (traceTextures == null) {
               return 0;
            } else if (!this.resetModelRotations(entityRenderState, traceModel)) {
               return -1;
            } else {
               this.mainRenderedModels.clear();
               RadarIconModelPrerenderer.Parameters parameters = new RadarIconModelPrerenderer.Parameters(config, defaultConfig, traceTextures, traceAtlasSprite, mrt, this.forceFieldCheck, this.fullModelIcon, this.hardcodedMainPartAliases, this.hardcodedModelPartsFields, this.mainRenderedModels);
               this.mainPart = this.modelPrerenderer.renderModel(matrixStack, bufferSource, entityRenderState, traceModel, entity, this.mainPart, parameters);
               this.mainModelTrace = mrt;
               return !this.mainRenderedModels.isEmpty() ? 1 : 0;
            }
         } else if (!mainModel) {
            if (traceTextures == null) {
               return 0;
            } else if (!this.resetModelRotations(entityRenderState, traceModel)) {
               return -1;
            } else {
               ArrayList<class_630> renderedModels = new ArrayList();
               RadarIconModelPrerenderer.Parameters parameters = new RadarIconModelPrerenderer.Parameters(config, defaultConfig, traceTextures, traceAtlasSprite, mrt, this.forceFieldCheck, this.fullModelIcon, this.hardcodedMainPartAliases, this.hardcodedModelPartsFields, renderedModels);
               this.mainPart = this.modelPrerenderer.renderModel(matrixStack, bufferSource, entityRenderState, traceModel, entity, this.mainPart, parameters);
               return !renderedModels.isEmpty() ? 1 : 0;
            }
         } else if (this.mainRenderedModels.isEmpty()) {
            return 0;
         } else {
            class_4588 vertexConsumer = this.modelPrerenderer.getLayerModelVertexConsumer(bufferSource, traceTextures, traceAtlasSprite, mrt);
            RadarIconModelPartPrerenderer.Parameters parameters = new RadarIconModelPartPrerenderer.Parameters(config, mrt, new ArrayList());
            this.modelPrerenderer.getPartPrerenderer().renderPartsIterable(this.mainRenderedModels, matrixStack, vertexConsumer, this.mainPart, parameters);
            bufferSource.method_22993();
            return 0;
         }
      }
   }

   private <T extends class_1297> boolean resetModelRotations(class_10017 entityRenderState, class_3879 model) {
      if (model instanceof class_583 entityModel) {
         try {
            entityModel.method_2819(entityRenderState);
            OpenGLException.checkGLError();
            return true;
         } catch (Throwable t) {
            MinimapLogs.LOGGER.error("suppressed exception", t);
            return false;
         }
      } else {
         return true;
      }
   }
}
