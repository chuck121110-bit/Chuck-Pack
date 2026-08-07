package xaero.hud.minimap.radar.icon.creator.render.trace;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_10017;
import net.minecraft.class_10042;
import net.minecraft.class_1058;
import net.minecraft.class_11684;
import net.minecraft.class_12075;
import net.minecraft.class_12245;
import net.minecraft.class_12247;
import net.minecraft.class_12250;
import net.minecraft.class_1297;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3879;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_4599;
import net.minecraft.class_4723;
import net.minecraft.class_583;
import net.minecraft.class_630;
import net.minecraft.class_895;
import net.minecraft.class_897;
import net.minecraft.class_898;
import net.minecraft.class_922;
import org.lwjgl.opengl.GL11;
import xaero.common.core.IBufferSource;
import xaero.common.exception.OpenGLException;
import xaero.common.misc.CachedFunction;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.render.pipeline.RenderPipelineWrapper;
import xaero.lib.client.graphics.IRenderType;
import xaero.lib.client.graphics.ITextureBinding;
import xaero.lib.client.graphics.XaeroRenderType;
import xaero.lib.common.reflection.util.ReflectionUtils;

public class EntityRenderTracer {
   public static boolean TRACING_MODEL_RENDERS;
   private class_1297 tracedEntity;
   private class_10017 tracedEntityRenderState;
   private class_897 tracedEntityRenderer;
   private Class<?> tracedEntityModelClass;
   private List<ModelRenderTrace> traceResultList;
   private ModelRenderTrace lastModelRenderDetected;
   private class_4597.class_4598 modelRenderDetectionRenderTypeBuffer;
   private Field enderDragonModelField;
   private Field renderBuffersBufferSourceField;
   private Field spriteCoordinateExpanderSpriteField;
   private Class<?> irisRenderLayerWrapperClass;
   private Method irisRenderLayerWrapperUnwrapMethod;
   private CachedFunction<RenderPipeline, RenderPipeline> fixAdditiveBlendFunction;

   public EntityRenderTracer() {
      this.initReflection();
      this.traceResultList = new ArrayList();
      this.fixAdditiveBlendFunction = new CachedFunction<RenderPipeline, RenderPipeline>(this::fixAdditiveBlendUncached);
   }

   public <S extends class_10017> List<ModelRenderTrace> trace(class_4587 matrixStack, class_1297 entity, class_897<?, ? super S> entityRenderer, S entityRenderState) {
      TRACING_MODEL_RENDERS = true;
      this.tracedEntity = entity;
      this.tracedEntityRenderState = entityRenderState;
      this.tracedEntityRenderer = entityRenderer;
      this.tracedEntityModelClass = null;
      this.traceResultList.clear();
      this.lastModelRenderDetected = null;
      class_4587.class_4665 matrixEntryToRestore = matrixStack.method_23760();
      matrixStack.method_22903();

      try {
         class_4597.class_4598 renderTypeBuffer = this.modelRenderDetectionRenderTypeBuffer = (class_4597.class_4598)ReflectionUtils.getReflectFieldValue(class_310.method_1551().method_22940(), this.renderBuffersBufferSourceField);
         ((IBufferSource)renderTypeBuffer).setXaero_lastRenderType((class_1921)null);
         class_310 mc = class_310.method_1551();
         class_898 renderDispatcher = mc.method_1561();
         class_11684 featureRenderDispatcher = class_310.method_1551().field_1773.method_72911();
         class_12075 cameraState = new class_12075();
         renderDispatcher.method_72976(entityRenderState, cameraState, (double)0.0F, (double)0.0F, (double)0.0F, matrixStack, featureRenderDispatcher.method_73003());
         featureRenderDispatcher.method_73002();
         renderTypeBuffer.method_22993();
         OpenGLException.checkGLError();
      } catch (Throwable e) {
         this.traceResultList.clear();
         MinimapLogs.LOGGER.error("Exception when calling the full entity renderer for {} before prerendering the icon.", entity.method_5820(), e);
      }

      TRACING_MODEL_RENDERS = false;
      this.tracedEntity = null;
      this.tracedEntityRenderState = null;
      this.tracedEntityRenderer = null;

      while(matrixStack.method_23760() != matrixEntryToRestore) {
         matrixStack.method_22909();
      }

      while(GL11.glGetError() != 0) {
      }

      return this.traceResultList;
   }

   public void onModelRender(class_3879 model, class_4588 vertexConsumer, int color) {
      this.lastModelRenderDetected = null;
      if (this.tracedEntityModelClass == null) {
         class_583<?> currentMainModel = this.getEntityRendererModel(this.tracedEntityRenderer);
         this.tracedEntityModelClass = currentMainModel == null ? null : currentMainModel.getClass();
         if (this.tracedEntityModelClass == null) {
            return;
         }
      }

      if (model.getClass().isAssignableFrom(this.tracedEntityModelClass) || this.tracedEntityModelClass.isAssignableFrom(model.getClass())) {
         class_1921 lastRenderType = this.getLastRenderType(this.modelRenderDetectionRenderTypeBuffer);
         if (lastRenderType == null && this.traceResultList.isEmpty()) {
            class_2960 textureLocation = null;

            try {
               class_897 var8 = this.tracedEntityRenderer;
               class_2960 var10000;
               if (var8 instanceof class_922) {
                  class_922 livingEntityRenderer = (class_922)var8;
                  var10000 = livingEntityRenderer.method_3885((class_10042)this.tracedEntityRenderState);
               } else {
                  var10000 = null;
               }

               class_2960 textureLocationUnchecked = var10000;
               textureLocation = textureLocationUnchecked;
            } catch (Throwable t) {
               MinimapLogs.LOGGER.error("Couldn't fetch main entity texture when trying to use an alternative render type for an icon!", t);
            }

            if (textureLocation != null) {
               lastRenderType = model.method_23500(textureLocation);
            }
         }

         if (lastRenderType != null) {
            class_12247 renderState = this.getRenderState(lastRenderType);
            if (renderState != null) {
               RenderPipeline renderPipeline = lastRenderType.method_73243();
               Map<String, ITextureBinding> textures = XaeroRenderType.getSetupTextures(renderState);
               class_12250 textureTransform = XaeroRenderType.getSetupTextureTransform(renderState);
               boolean useOverlay = XaeroRenderType.getSetupUseOverlay(renderState);
               class_12245 layeringTransform = XaeroRenderType.getSetupLayeringTransform(renderState);
               renderPipeline = this.fixAdditiveBlendFunction.apply(renderPipeline);
               class_1058 renderAtlasSprite = null;
               if (vertexConsumer instanceof class_4723) {
                  renderAtlasSprite = (class_1058)ReflectionUtils.getReflectFieldValue(vertexConsumer, this.spriteCoordinateExpanderSpriteField);
               }

               this.lastModelRenderDetected = new ModelRenderTrace(model, textures, textureTransform, useOverlay, lastRenderType.method_23037(), lastRenderType.method_60894(), layeringTransform, renderAtlasSprite, renderPipeline, color);
               this.traceResultList.add(this.lastModelRenderDetected);
            }
         }
      }
   }

   private RenderPipeline fixAdditiveBlendUncached(RenderPipeline renderPipeline) {
      RenderPipeline result = renderPipeline;
      BlendFunction blendFunction = (BlendFunction)renderPipeline.getBlendFunction().orElse((Object)null);
      if (blendFunction == null) {
         return renderPipeline;
      } else {
         DestFactor blendDestFactor = blendFunction.destColor();
         if (blendDestFactor == DestFactor.ONE) {
            SourceFactor blendSrcFactor = blendFunction.sourceColor();
            if (blendSrcFactor != SourceFactor.ZERO) {
               blendFunction = new BlendFunction(blendSrcFactor, blendDestFactor, SourceFactor.ZERO, DestFactor.ONE);
               RenderPipelineWrapper wrapper = new RenderPipelineWrapper(class_2960.method_60655("xaerominimap", "wrapper/" + renderPipeline.getLocation().method_12832()), renderPipeline);
               wrapper.setBlendFunctionOverride(Optional.of(blendFunction));
               result = wrapper;
            }
         }

         return result;
      }
   }

   public void onModelPartRender(class_630 modelRenderer, int color) {
      if (this.lastModelRenderDetected != null) {
         this.lastModelRenderDetected.addVisibleModelPart(modelRenderer, color);
      }

   }

   private class_1921 getLastRenderType(class_4597 renderTypeBuffer) {
      if (renderTypeBuffer instanceof IBufferSource xaeroBufferSource) {
         return xaeroBufferSource.getXaero_lastRenderType();
      } else {
         return null;
      }
   }

   private class_12247 getRenderState(class_1921 renderType) {
      while(renderType.getClass() == this.irisRenderLayerWrapperClass && this.irisRenderLayerWrapperUnwrapMethod != null) {
         renderType = (class_1921)ReflectionUtils.getReflectMethodValue(renderType, this.irisRenderLayerWrapperUnwrapMethod, new Object[0]);
      }

      return ((IRenderType)renderType).xaero_lib_getSetup();
   }

   public <S extends class_10017> class_583<S> getEntityRendererModel(class_897<?, ? super S> entityRenderer) {
      if (entityRenderer instanceof class_922) {
         return ((class_922)entityRenderer).method_4038();
      } else {
         return entityRenderer instanceof class_895 ? (class_583)ReflectionUtils.getReflectFieldValue(entityRenderer, this.enderDragonModelField) : null;
      }
   }

   private void initReflection() {
      this.enderDragonModelField = ReflectionUtils.getFieldReflection(class_895.class, "model", "field_21008", "Lnet/minecraft/class_625;", "f_114183_");
      this.renderBuffersBufferSourceField = ReflectionUtils.getFieldReflection(class_4599.class, "bufferSource", "field_46901", "Lnet/minecraft/class_4597$class_4598;", "f_110094_");
      this.spriteCoordinateExpanderSpriteField = ReflectionUtils.getFieldReflection(class_4723.class, "sprite", "field_21731", "Lnet/minecraft/class_1058;", "f_110796_");

      try {
         try {
            this.irisRenderLayerWrapperClass = Class.forName("net.coderbot.iris.layer.IrisRenderTypeWrapper");
         } catch (ClassNotFoundException var2) {
            this.irisRenderLayerWrapperClass = Class.forName("net.coderbot.iris.layer.IrisRenderLayerWrapper");
         }

         this.irisRenderLayerWrapperUnwrapMethod = ReflectionUtils.getMethodReflection(this.irisRenderLayerWrapperClass, "unwrap", "unwrap", "()Lnet/minecraft/class_1921;", "unwrap", new Class[0]);
         MinimapLogs.LOGGER.info("Old Iris detected and supported!");
      } catch (Exception var3) {
      }

   }

   public void reset() {
      this.fixAdditiveBlendFunction = new CachedFunction<RenderPipeline, RenderPipeline>(this::fixAdditiveBlendUncached);
   }
}
