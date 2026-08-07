package xaero.hud.minimap.radar.icon.creator.render.form.model.custom;

import java.util.List;
import net.minecraft.class_10017;
import net.minecraft.class_1297;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_583;
import net.minecraft.class_630;
import net.minecraft.class_897;
import xaero.hud.minimap.radar.icon.creator.render.form.model.part.RadarIconModelPartPrerenderer;
import xaero.hud.minimap.radar.icon.creator.render.trace.ModelRenderTrace;
import xaero.hud.minimap.radar.icon.definition.form.model.config.RadarIconModelConfig;

public class LayeredIconCustomPrerenderer extends RadarIconCustomPrerenderer {
   private List<RadarIconCustomPrerenderer> layers;

   public LayeredIconCustomPrerenderer(List<RadarIconCustomPrerenderer> layers) {
      this.layers = layers;
   }

   public <S extends class_10017> class_630 render(class_4587 matrixStack, class_4597.class_4598 bufferSource, class_897<?, ? super S> entityRenderer, S entityRenderState, class_1297 e, class_583<S> defaultModel, RadarIconModelPartPrerenderer partPrerenderer, List<class_630> rendered, class_630 mainPart, RadarIconModelConfig modelConfig, ModelRenderTrace mrt) {
      for(RadarIconCustomPrerenderer layer : this.layers) {
         mainPart = layer.render(matrixStack, bufferSource, entityRenderer, entityRenderState, e, defaultModel, partPrerenderer, rendered, mainPart, modelConfig, mrt);
      }

      return mainPart;
   }
}
