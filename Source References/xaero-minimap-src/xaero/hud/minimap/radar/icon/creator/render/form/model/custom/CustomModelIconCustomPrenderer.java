package xaero.hud.minimap.radar.icon.creator.render.form.model.custom;

import java.util.List;
import net.minecraft.class_10017;
import net.minecraft.class_1297;
import net.minecraft.class_583;
import net.minecraft.class_630;
import xaero.hud.minimap.radar.icon.creator.render.form.model.part.RadarIconModelPartPrerenderer;

public abstract class CustomModelIconCustomPrenderer extends RenderTypeIconCustomPrerenderer {
   protected List<class_630> getRenderedDest(List<class_630> rendered) {
      return rendered;
   }

   protected abstract <S extends class_10017> Iterable<class_630> getModelParts(RadarIconModelPartPrerenderer var1, List<class_630> var2, class_1297 var3, class_583<S> var4);
}
