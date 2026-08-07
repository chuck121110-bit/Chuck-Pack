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

public abstract class RadarIconCustomPrerenderer {
   public abstract <S extends class_10017> class_630 render(class_4587 var1, class_4597.class_4598 var2, class_897<?, ? super S> var3, S var4, class_1297 var5, class_583<S> var6, RadarIconModelPartPrerenderer var7, List<class_630> var8, class_630 var9, RadarIconModelConfig var10, ModelRenderTrace var11);
}
