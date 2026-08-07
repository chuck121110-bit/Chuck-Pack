package xaero.hud.pushbox.effect;

import java.util.Collection;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_6880;
import xaero.hud.pushbox.PushBox;

public class PotionEffectsPushBox extends PushBox implements IPotionEffectsPushBox {
   private boolean hasNegative;

   public PotionEffectsPushBox() {
      super(0, 0, 0, 0, 1.0F, 0.0F, 53);
   }

   public int getX(int width, int height) {
      return super.getX(width, height) - this.getW(width, height);
   }

   public void update() {
      super.update();
      this.hasNegative = false;
      this.w = calculatePotionDisplayWidth(this);
      this.h = this.hasNegative ? 53 : 27;
   }

   public void postUpdate() {
      super.postUpdate();
      this.active = false;
   }

   protected static int calculatePotionDisplayWidth(IPotionEffectsPushBox potionEffectBox) {
      class_310 mc = class_310.method_1551();
      class_1657 player = mc.field_1724;
      Collection<class_1293> collection = player.method_6026();
      if (collection != null && !collection.isEmpty()) {
         int positiveCount = 0;
         int negativeCount = 0;

         for(class_1293 effectInstance : collection) {
            if (effectInstance.method_5592()) {
               class_6880<class_1291> effect = effectInstance.method_5579();
               if (effect != null) {
                  if (!((class_1291)effect.comp_349()).method_5573()) {
                     potionEffectBox.setHasNegative(true);
                     ++negativeCount;
                  } else {
                     ++positiveCount;
                  }
               }
            }
         }

         if (positiveCount + negativeCount == 0) {
            return 0;
         } else {
            return Math.max(positiveCount, negativeCount) * 25 + 1;
         }
      } else {
         return 0;
      }
   }

   public void setHasNegative(boolean b) {
      this.hasNegative = b;
   }
}
