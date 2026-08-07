package xaero.hud.minimap.radar.icon.creator.render.form.model;

import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import net.minecraft.class_10017;
import net.minecraft.class_1043;
import net.minecraft.class_1058;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1921;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3879;
import net.minecraft.class_3882;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_572;
import net.minecraft.class_583;
import net.minecraft.class_597;
import net.minecraft.class_630;
import org.apache.logging.log4j.Logger;
import xaero.common.graphics.CustomRenderTypes;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.radar.icon.creator.render.form.model.part.ModelPartUtil;
import xaero.hud.minimap.radar.icon.creator.render.form.model.part.RadarIconModelPartPrerenderer;
import xaero.hud.minimap.radar.icon.creator.render.form.model.part.ResolvedFieldModelPartRenderer;
import xaero.hud.minimap.radar.icon.creator.render.form.model.resolver.RadarIconModelFieldResolver;
import xaero.hud.minimap.radar.icon.creator.render.form.model.resolver.ResolvedFieldModelRootPathListener;
import xaero.hud.minimap.radar.icon.creator.render.trace.ModelRenderTrace;
import xaero.hud.minimap.radar.icon.definition.BuiltInRadarIconDefinitions;
import xaero.hud.minimap.radar.icon.definition.form.model.config.RadarIconModelConfig;
import xaero.lib.client.graphics.ITextureBinding;
import xaero.lib.common.reflection.util.ReflectionUtils;

public class RadarIconModelPrerenderer {
   private static String[] EXTRA_PART_NAMES = new String[]{"beak", "nose", "jaw", "left_ear", "right_ear", "mane", "mouth", "eyes", "tongue", "hat"};
   private static final Object[] ONE_RENDERER_ARRAY = new Object[1];
   private static final Object[] ONE_OBJECT_ARRAY = new Object[1];
   public static final class_2960 LIGHTMAP = class_2960.method_60655("xaerominimap", "entity_icon_model_lightmap");
   private final RadarIconModelPartPrerenderer partPrerenderer = new RadarIconModelPartPrerenderer();
   private final ResolvedFieldModelPartRenderer resolvedFieldRenderer = new ResolvedFieldModelPartRenderer();
   private final ResolvedFieldModelRootPathListener modelRootPathListener = new ResolvedFieldModelRootPathListener();
   private class_630 mainPart;
   private final GpuTextureView lightTextureView;

   public RadarIconModelPrerenderer() {
      class_1043 lightmapTexture = new class_1043("entity_icon_lightmap", 16, 16, false);
      lightmapTexture.method_4525().method_4326(0, 0, 16, 16, -1);
      lightmapTexture.method_4524();
      class_310.method_1551().method_1531().method_4616(LIGHTMAP, lightmapTexture);
      this.lightTextureView = lightmapTexture.method_71659();
   }

   public class_630 renderModel(class_4587 matrixStack, class_4597.class_4598 bufferSource, class_10017 entityRenderState, class_3879 model, class_1297 entity, class_630 mainPart, Parameters parameters) {
      this.mainPart = mainPart;
      boolean forceFieldCheck = parameters.forceFieldCheck;
      boolean fullModelIcon = parameters.fullModelIcon;
      RadarIconModelConfig config = parameters.config;
      Object modelRoot = null;
      if (config.modelRootPath != null) {
         modelRoot = this.resolveModelRoot(model, config.modelRootPath, entity);
      }

      if (modelRoot == null) {
         modelRoot = BuiltInRadarIconDefinitions.getModelRoot(model);
      }

      boolean treatAsHierarchicalRoot = false;
      class_4588 vertexConsumer = this.getLayerModelVertexConsumer(bufferSource, parameters.textures, parameters.textureAtlasSprite, parameters.mrt);
      if (config.modelMainPartFieldAliases != null && !config.modelMainPartFieldAliases.isEmpty()) {
         this.searchAndRenderFields(matrixStack, vertexConsumer, modelRoot, config.modelMainPartFieldAliases, true, parameters);
      }

      if (forceFieldCheck || fullModelIcon || !(modelRoot instanceof class_572) && !(modelRoot instanceof class_597)) {
         if (!forceFieldCheck && (treatAsHierarchicalRoot || modelRoot instanceof class_583) && this.renderHierarchicalModel(modelRoot, treatAsHierarchicalRoot, matrixStack, vertexConsumer, parameters)) {
            bufferSource.method_22993();
            return this.mainPart;
         } else {
            if (!forceFieldCheck && modelRoot instanceof class_3882) {
               class_630 headPart = ((class_3882)modelRoot).method_2838();
               this.renderPart(matrixStack, vertexConsumer, headPart, parameters);
            }

            List<String> hardcodedMainPartAliases = parameters.hardcodedMainPartAliases;
            List<String> hardcodedModelPartsFields = parameters.hardcodedModelPartsFields;
            if (config.modelPartsFields == null) {
               this.searchAndRenderFields(matrixStack, vertexConsumer, modelRoot, hardcodedMainPartAliases, true, parameters);
            }

            List<String> headPartsFields = hardcodedModelPartsFields;
            if (fullModelIcon) {
               headPartsFields = null;
            } else if (config.modelPartsFields != null) {
               headPartsFields = config.modelPartsFields;
            }

            this.searchAndRenderFields(matrixStack, vertexConsumer, modelRoot, headPartsFields, false, parameters);
            bufferSource.method_22993();
            return this.mainPart;
         }
      } else {
         this.renderAgeableListModel((class_3879)modelRoot, matrixStack, vertexConsumer, parameters);
         bufferSource.method_22993();
         return this.mainPart;
      }
   }

   private void renderAgeableListModel(class_3879 modelRoot, class_4587 matrixStack, class_4588 vertexConsumer, Parameters parameters) {
      class_630 headPart;
      if (modelRoot instanceof class_572) {
         headPart = ((class_572)modelRoot).field_3398;
      } else {
         headPart = (class_630)ReflectionUtils.getReflectFieldValue(modelRoot, this.partPrerenderer.quadrupedHeadField);
      }

      this.renderPart(matrixStack, vertexConsumer, headPart, parameters);
   }

   private boolean renderHierarchicalModel(Object modelRoot, boolean treatAsHierarchicalRoot, class_4587 matrixStack, class_4588 vertexConsumer, Parameters parameters) {
      boolean success = false;
      class_630 rootPart;
      if (treatAsHierarchicalRoot) {
         rootPart = (class_630)modelRoot;
      } else {
         class_583<?> singlePartModel = (class_583)modelRoot;
         rootPart = singlePartModel.method_63512();
      }

      if (rootPart == null) {
         return false;
      } else {
         class_630 headPart;
         try {
            headPart = rootPart.method_32086("head");
         } catch (NoSuchElementException var19) {
            try {
               headPart = rootPart.method_32086("head_parts");
            } catch (NoSuchElementException var18) {
               if (parameters.fullModelIcon) {
                  headPart = null;
               } else {
                  try {
                     headPart = rootPart.method_32086("body").method_32086("head");
                  } catch (NoSuchElementException var17) {
                     try {
                        headPart = rootPart.method_32086("body").method_32086("head_parts");
                     } catch (NoSuchElementException var16) {
                        headPart = null;
                     }
                  }
               }
            }
         }

         if (headPart != null) {
            this.renderPart(matrixStack, vertexConsumer, headPart, parameters);
            success = true;
         }

         if (parameters.fullModelIcon) {
            Map<String, class_630> rootChildren = ModelPartUtil.getChildren(rootPart);
            this.mainPart = this.partPrerenderer.renderPartsIterable(rootChildren.values(), matrixStack, vertexConsumer, this.mainPart, parameters);
            return true;
         } else {
            for(String extraPartName : EXTRA_PART_NAMES) {
               class_630 extraPart;
               try {
                  extraPart = rootPart.method_32086(extraPartName);
               } catch (NoSuchElementException var15) {
                  extraPart = null;
               }

               if (extraPart != null) {
                  this.renderPart(matrixStack, vertexConsumer, extraPart, parameters);
                  success = true;
               }
            }

            return success;
         }
      }
   }

   private void renderPart(class_4587 matrixStack, class_4588 vertexConsumer, class_630 part, Parameters parameters) {
      if (this.mainPart == null) {
         this.mainPart = part;
      }

      this.partPrerenderer.renderPart(matrixStack, vertexConsumer, part, this.mainPart, parameters);
   }

   private void searchAndRenderFields(class_4587 matrixStack, class_4588 vertexBuilder, Object modelRoot, List<String> filter, boolean justOne, Parameters parameters) {
      this.resolvedFieldRenderer.prepare(matrixStack, vertexBuilder, justOne, this.mainPart, parameters, this.partPrerenderer);
      RadarIconModelFieldResolver.searchSuperclassFields(modelRoot, filter, this.resolvedFieldRenderer, ONE_RENDERER_ARRAY);
      this.mainPart = this.resolvedFieldRenderer.getMainPart();
   }

   public class_4588 getLayerModelVertexConsumer(class_4597.class_4598 renderTypeBuffer, Map<String, ITextureBinding> textures, class_1058 entityAtlasSprite, ModelRenderTrace mrt) {
      class_1921 renderType = CustomRenderTypes.entityIconRenderType(textures, mrt.textureTransform, mrt.useOverlay, mrt.affectsCrumbling, mrt.sortOnUpload, mrt.layeringTransform, mrt.layerPipeline);
      class_4588 regularConsumer = renderTypeBuffer.method_73477(renderType);
      return entityAtlasSprite != null ? entityAtlasSprite.method_24108(regularConsumer) : regularConsumer;
   }

   private Object resolveModelRoot(class_3879 model, ArrayList<ArrayList<String>> rootPath, class_1297 entity) {
      Object currentChainNode = model;

      for(ArrayList<String> pathStep : rootPath) {
         this.modelRootPathListener.prepare();
         RadarIconModelFieldResolver.searchSuperclassFields(currentChainNode, pathStep, this.modelRootPathListener, ONE_OBJECT_ARRAY);
         currentChainNode = this.modelRootPathListener.getCurrentNode();
         if (currentChainNode == null || this.modelRootPathListener.failed()) {
            MinimapLogs.LOGGER.info(String.format("The following entity icon model root path step couldn't be resolved for %s:", class_1299.method_5890(entity.method_5864())));
            Logger var10001 = MinimapLogs.LOGGER;
            Objects.requireNonNull(var10001);
            pathStep.forEach(var10001::info);
            return null;
         }
      }

      return currentChainNode;
   }

   public RadarIconModelPartPrerenderer getPartPrerenderer() {
      return this.partPrerenderer;
   }

   public static final class Parameters extends RadarIconModelPartPrerenderer.Parameters {
      public final RadarIconModelConfig defaultConfig;
      public final Map<String, ITextureBinding> textures;
      public final class_1058 textureAtlasSprite;
      public final boolean forceFieldCheck;
      public final boolean fullModelIcon;
      public final List<String> hardcodedMainPartAliases;
      public final List<String> hardcodedModelPartsFields;

      public Parameters(RadarIconModelConfig config, RadarIconModelConfig defaultConfig, Map<String, ITextureBinding> textures, class_1058 textureAtlasSprite, ModelRenderTrace mrt, boolean forceFieldCheck, boolean fullModelIcon, List<String> hardcodedMainPartAliases, List<String> hardcodedModelPartsFields, List<class_630> renderedDest) {
         super(config, mrt, renderedDest);
         this.defaultConfig = defaultConfig;
         this.textures = textures;
         this.textureAtlasSprite = textureAtlasSprite;
         this.forceFieldCheck = forceFieldCheck;
         this.fullModelIcon = fullModelIcon;
         this.hardcodedMainPartAliases = hardcodedMainPartAliases;
         this.hardcodedModelPartsFields = hardcodedModelPartsFields;
      }
   }
}
