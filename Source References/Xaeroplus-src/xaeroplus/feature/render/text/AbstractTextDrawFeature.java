package xaeroplus.feature.render.text;

import java.util.Collection;
import java.util.Objects;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_3532;
import net.minecraft.class_327.class_6415;
import org.joml.Matrix4f;
import xaeroplus.Globals;
import xaeroplus.feature.render.DrawContext;
import xaeroplus.feature.render.DrawFeature;

public abstract class AbstractTextDrawFeature implements DrawFeature {
   public abstract Collection<Text> getTexts();

   public void render(final DrawContext ctx) {
      class_327 font = class_310.method_1551().field_1772;
      Collection<Text> texts = this.getTexts();

      for(Text text : texts) {
         float xpMinimapScalar = (float)Globals.minimapScaleMultiplier / (float)Globals.minimapSizeMultiplier;
         float textScale = text.scale() * 2.0F * (float)class_3532.method_15350((double)(ctx.worldmap() ? 1.0F : xpMinimapScalar) / ctx.fboScale(), (double)(0.1F * (ctx.worldmap() ? 1.0F : xpMinimapScalar)), (double)1000.0F);
         float width = (float)font.method_1727(text.value());
         float relativeX = (float)((long)text.x() - (long)ctx.cameraBlockX());
         float relativeZ = (float)((long)text.z() - (long)ctx.cameraBlockZ());
         Matrix4f var10000 = (new Matrix4f(ctx.untranslatedMapViewMatrix())).translate(relativeX, relativeZ, 0.0F).scale(textScale, textScale, 1.0F);
         float var10001 = -width / 2.0F;
         Objects.requireNonNull(font);
         Matrix4f textMatrix = var10000.translate(var10001, (float)(-9) / 2.0F, 0.0F);
         font.method_27521(text.value(), 0.0F, 0.0F, text.color(), true, textMatrix, ctx.renderTypeBuffers(), class_6415.field_33993, 0, 15728880);
      }

      if (!texts.isEmpty()) {
         try {
            Globals.disableDrawCullingOverride = true;
            ctx.renderTypeBuffers().endBatch();
         } finally {
            Globals.disableDrawCullingOverride = false;
         }
      }

   }
}
