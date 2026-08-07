package xaero.hud.minimap.radar.icon.creator.entity;

import net.minecraft.class_10034;
import net.minecraft.class_10042;

public class LivingEntityPoseResetter {
   public void resetValues(class_10042 livingEntityRenderState) {
      livingEntityRenderState.field_53451 = 0.0F;
      livingEntityRenderState.field_53447 = 0.0F;
      livingEntityRenderState.field_53448 = 0.0F;
      livingEntityRenderState.field_53446 = 0.0F;
      livingEntityRenderState.field_53328 = 10.0F;
      if (livingEntityRenderState instanceof class_10034 humanoidRenderState) {
         humanoidRenderState.field_63604 = 0.0F;
         humanoidRenderState.field_53403 = 0.0F;
         humanoidRenderState.field_53410 = false;
         humanoidRenderState.field_53411 = false;
         humanoidRenderState.field_53412 = false;
         humanoidRenderState.field_53407 = 0.0F;
      }

   }
}
