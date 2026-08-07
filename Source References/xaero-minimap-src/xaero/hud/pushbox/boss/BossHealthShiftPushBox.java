package xaero.hud.pushbox.boss;

import xaero.hud.pushbox.FullHeightShiftPushBox;

public class BossHealthShiftPushBox extends FullHeightShiftPushBox implements IBossHealthPushBox {
   public int lastBossHealthHeight;

   public BossHealthShiftPushBox() {
      super(-92, 184, 0.5F);
   }

   protected int getShift() {
      return this.lastBossHealthHeight;
   }

   public void postUpdate() {
      super.postUpdate();
      this.lastBossHealthHeight = 0;
      this.active = false;
   }

   public void setLastBossHealthHeight(int h) {
      this.lastBossHealthHeight = h;
   }
}
