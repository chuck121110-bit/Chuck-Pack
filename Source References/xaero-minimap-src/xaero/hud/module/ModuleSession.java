package xaero.hud.module;

import xaero.common.HudMod;

public abstract class ModuleSession<MS extends ModuleSession<MS>> {
   protected final HudMod modMain;
   protected final HudModule<MS> module;

   public ModuleSession(HudMod modMain, HudModule<MS> module) {
      this.modMain = modMain;
      this.module = module;
   }

   public HudModule<MS> getModule() {
      return this.module;
   }

   public HudMod getModMain() {
      return this.modMain;
   }

   public boolean isActive() {
      return this.module.isActive(this.modMain.getHudConfigs().getClientConfigManager());
   }

   public int getEffectiveX(int screenWidth, double screenScale) {
      ModuleTransform transform = this.module.getUsedTransform();
      if (!transform.centered && !transform.fromRight) {
         return transform.x;
      } else {
         int width = this.getWidth(screenScale);
         return transform.centered ? screenWidth / 2 - width / 2 : screenWidth - transform.x - width;
      }
   }

   public int getEffectiveY(int screenHeight, double screenScale) {
      ModuleTransform transform = this.module.getUsedTransform();
      if (!transform.fromBottom) {
         return transform.y;
      } else {
         int height = this.getHeight(screenScale);
         return screenHeight - transform.y - height;
      }
   }

   public boolean isFlippedHor() {
      return this.module.getUsedTransform().flippedHor;
   }

   public boolean isFlippedVer() {
      return this.module.getUsedTransform().flippedVer;
   }

   public boolean isCentered() {
      return this.module.getUsedTransform().centered;
   }

   public boolean shouldFlipHorizontally(int screenWidth, double screenScale) {
      boolean flipped = this.isFlippedHor();
      int x = this.getEffectiveX(screenWidth, screenScale);
      int w = this.getWidth(screenScale);
      if (this.isCentered()) {
         return flipped;
      } else if (flipped) {
         return x + w / 2 < screenWidth / 2;
      } else {
         return x + w / 2 > screenWidth / 2;
      }
   }

   public boolean shouldFlipVertically(int screenHeight, double screenScale) {
      boolean flipped = this.isFlippedVer();
      int y = this.getEffectiveY(screenHeight, screenScale);
      int h = this.getHeight(screenScale);
      if (flipped) {
         return y + h / 2 < screenHeight / 2;
      } else {
         return y + h / 2 > screenHeight / 2;
      }
   }

   public void prePotentialRender() {
   }

   public void onPostGameOverlay() {
   }

   public abstract int getWidth(double var1);

   public abstract int getHeight(double var1);

   public abstract void close();
}
