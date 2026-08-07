package xaeroplus.module;

import net.minecraft.class_310;
import xaeroplus.XaeroPlus;

public abstract class Module {
   private boolean enabled = false;
   public final class_310 mc = class_310.method_1551();

   protected void onEnable() {
   }

   protected void onDisable() {
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setEnabled(boolean enabled) {
      if (enabled) {
         this.enable();
      } else {
         this.disable();
      }

   }

   public void enable() {
      if (!this.isEnabled()) {
         this.enabled = true;
         XaeroPlus.EVENT_BUS.register(this);

         try {
            this.onEnable();
         } catch (Exception e) {
            XaeroPlus.LOGGER.error("Error enabling module: " + this.getClass().getSimpleName(), e);
         }

      }
   }

   public void disable() {
      if (this.isEnabled()) {
         this.enabled = false;
         XaeroPlus.EVENT_BUS.unregister(this);

         try {
            this.onDisable();
         } catch (Exception e) {
            XaeroPlus.LOGGER.error("Error enabling module: " + this.getClass().getSimpleName(), e);
         }

      }
   }

   public void toggle() {
      if (this.isEnabled()) {
         this.disable();
      } else {
         this.enable();
      }

   }
}
