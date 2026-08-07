package xaero.hud.minimap.radar.icon.creator.render.form.model.custom;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_10017;
import net.minecraft.class_1297;
import net.minecraft.class_1921;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_4597;
import net.minecraft.class_583;
import net.minecraft.class_630;
import net.minecraft.class_897;
import xaero.hud.minimap.radar.icon.creator.render.form.model.part.RadarIconModelPartPrerenderer;
import xaero.hud.minimap.radar.icon.creator.render.trace.ModelRenderTrace;
import xaero.hud.minimap.radar.icon.definition.form.model.config.RadarIconModelConfig;

public abstract class RenderTypeIconCustomPrerenderer extends RadarIconCustomPrerenderer {
   public <S extends class_10017> class_630 render(class_4587 matrixStack, class_4597.class_4598 bufferSource, class_897<?, ? super S> entityRenderer, S entityRenderState, class_1297 e, class_583<S> defaultModel, RadarIconModelPartPrerenderer partPrerenderer, List<class_630> rendered, class_630 mainPart, RadarIconModelConfig config, ModelRenderTrace mrt) {
      class_1921 renderType = this.getRenderType(entityRenderer, e);
      if (renderType == null) {
         return mainPart;
      } else {
         class_4588 vertexBuilder = bufferSource.method_73477(renderType);
         Iterable<class_630> modelParts = this.getModelParts(partPrerenderer, rendered, e, defaultModel);
         List<class_630> renderedDest = this.getRenderedDest(rendered);
         RadarIconModelPartPrerenderer.Parameters parameters = new RadarIconModelPartPrerenderer.Parameters(config, mrt, renderedDest);
         mainPart = partPrerenderer.renderPartsIterable(modelParts, matrixStack, vertexBuilder, mainPart, parameters);
         bufferSource.method_22993();
         return mainPart;
      }
   }

   protected <S extends class_10017> Iterable<class_630> getModelParts(RadarIconModelPartPrerenderer partPrerenderer, List<class_630> rendered, class_1297 entity, class_583<S> defaultModel) {
      return rendered;
   }

   protected List<class_630> getRenderedDest(List<class_630> rendered) {
      return new ArrayList();
   }

   protected abstract <S extends class_10017> class_1921 getRenderType(class_897<?, ? super S> var1, class_1297 var2);
}
