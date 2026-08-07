package xaero.hud.pushbox.effect;

import xaero.hud.pushbox.FullHeightShiftPushBox;

public class PotionEffectsShiftPushBox extends FullHeightShiftPushBox implements IPotionEffectsPushBox {
   private boolean hasNegative;

   public PotionEffectsShiftPushBox() {
      super(0, 0, 1.0F);
   }

   public int getX(int width, int height) {
      return super.getX(width, height) - this.getW(width, height);
   }

   protected int getShift() {
      return this.hasNegative ? 53 : 27;
   }

   public void update() {
      super.update();
      this.hasNegative = false;
      this.w = PotionEffectsPushBox.calculatePotionDisplayWidth(this);
   }

   public void postUpdate() {
      super.postUpdate();
      this.active = false;
   }

   public void setHasNegative(boolean b) {
      this.hasNegative = b;
   }
}
