package xaero.hud.minimap.radar.icon.creator.render.form.model.part;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_630;
import xaero.hud.minimap.radar.icon.creator.render.form.model.RadarIconModelPrerenderer;
import xaero.hud.minimap.radar.icon.creator.render.form.model.resolver.RadarIconModelFieldResolver;

public class ResolvedFieldModelPartRenderer implements RadarIconModelFieldResolver.Listener {
   private class_4587 matrixStack;
   private class_4588 vertexConsumer;
   private boolean justOne;
   private class_630 mainPart;
   private RadarIconModelPartPrerenderer modelPartPrerenderer;
   private RadarIconModelPrerenderer.Parameters parameters;
   private boolean stop;

   public void prepare(class_4587 matrixStack, class_4588 vertexConsumer, boolean justOne, class_630 mainPart, RadarIconModelPrerenderer.Parameters parameters, RadarIconModelPartPrerenderer modelPartPrerenderer) {
      this.matrixStack = matrixStack;
      this.vertexConsumer = vertexConsumer;
      this.justOne = justOne;
      this.mainPart = mainPart;
      this.parameters = parameters;
      this.modelPartPrerenderer = modelPartPrerenderer;
      this.stop = false;
   }

   public boolean isFieldAllowed(Field f) {
      try {
         f.getType().asSubclass(class_630.class);
      } catch (ClassCastException var9) {
         try {
            f.getType().asSubclass(class_630[].class);
         } catch (ClassCastException var8) {
            try {
               f.getType().asSubclass(Collection.class);
            } catch (ClassCastException var7) {
               try {
                  f.getType().asSubclass(Map.class);
               } catch (ClassCastException var6) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   public boolean shouldStop() {
      return this.stop;
   }

   public void onFieldResolved(Object[] resolved, String matchedFilterElement) {
      class_4587 matrixStack = this.matrixStack;
      class_4588 vertexConsumer = this.vertexConsumer;
      boolean justOne = this.justOne;
      RadarIconModelPartPrerenderer modelPartPrerenderer = this.modelPartPrerenderer;

      for(Object o : resolved) {
         if (o instanceof class_630 part) {
            if (this.mainPart == null) {
               this.mainPart = part;
            }

            modelPartPrerenderer.renderPart(matrixStack, vertexConsumer, part, this.mainPart, this.parameters);
            if (justOne) {
               this.stop = true;
               break;
            }
         }
      }

   }

   public class_630 getMainPart() {
      return this.mainPart;
   }
}
