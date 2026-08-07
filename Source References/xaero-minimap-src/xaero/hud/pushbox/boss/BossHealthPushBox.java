package xaero.hud.pushbox.boss;

import xaero.hud.pushbox.PushBox;

public class BossHealthPushBox extends PushBox implements IBossHealthPushBox {
   public BossHealthPushBox() {
      super(-92, 0, 184, 0, 0.5F, 0.0F, 0);
   }

   public void postUpdate() {
      super.postUpdate();
      this.h = 0;
      this.active = false;
   }

   public void setLastBossHealthHeight(int h) {
      this.h = h;
   }
}
