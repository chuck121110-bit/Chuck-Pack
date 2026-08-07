package xaero.hud.minimap.radar.icon.creator.render.form.sprite;

import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.class_10017;
import net.minecraft.class_1044;
import net.minecraft.class_12137;
import net.minecraft.class_12247;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_583;
import net.minecraft.class_897;
import net.minecraft.class_308.class_11274;
import xaero.hud.minimap.element.render.MinimapElementGraphics;
import xaero.hud.minimap.radar.icon.creator.RadarIconCreator;
import xaero.hud.minimap.radar.icon.creator.render.form.IRadarIconFormPrerenderer;
import xaero.hud.minimap.radar.icon.creator.render.trace.ModelRenderTrace;
import xaero.hud.minimap.radar.icon.definition.form.sprite.RadarIconSpriteForm;
import xaero.lib.client.graphics.XaeroRenderType;
import xaero.lib.client.graphics.util.ImmediateRenderUtil;

public class RadarIconSpriteFormPrerenderer implements IRadarIconFormPrerenderer {
   private final boolean flipped;
   private final boolean outlined;

   public RadarIconSpriteFormPrerenderer(boolean flipped, boolean outlined) {
      this.flipped = flipped;
      this.outlined = outlined;
   }

   public boolean requiresEntityModel() {
      return false;
   }

   public boolean isFlipped() {
      return this.flipped;
   }

   public boolean isOutlined() {
      return this.outlined;
   }

   public <S extends class_10017> boolean prerender(MinimapElementGraphics guiGraphics, class_897<?, ? super S> entityRenderer, S entityRenderState, @Nullable class_583<S> entityModel, class_1297 entity, @Nullable List<ModelRenderTrace> traceResult, RadarIconCreator.Parameters parameters) {
      class_4587 matrixStack = guiGraphics.pose();
      RadarIconSpriteForm spriteForm = (RadarIconSpriteForm)parameters.form;
      class_2960 sprite = spriteForm.getSpriteLocation();
      class_310.method_1551().field_1773.method_71114().method_71034(class_11274.field_60026);
      class_1044 texture = class_310.method_1551().method_1531().method_4619(sprite);
      GpuTextureView gpuTextureView = texture.method_71659();
      int halfIcon = 32;
      matrixStack.method_46416((float)halfIcon, (float)halfIcon, 1.0F);
      float scale = parameters.scale;
      if (scale < 1.0F) {
         matrixStack.method_22905(scale, scale, 1.0F);
      }

      class_12137 sampler = XaeroRenderType.getSimpleSampler(FilterMode.LINEAR, FilterMode.NEAREST);
      class_12247.class_12337 tas = new class_12247.class_12337(gpuTextureView, sampler);
      ImmediateRenderUtil.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      ImmediateRenderUtil.texturedRect(matrixStack, (float)(-halfIcon), (float)(-halfIcon), 0, 64, 64.0F, 64.0F, -64.0F, 64.0F, XaeroRenderType.RP_POSITION_TEX_ALPHA_NO_CULL, tas);
      return true;
   }
}
