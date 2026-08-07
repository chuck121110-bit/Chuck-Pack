package xaero.common.mixin;

import net.minecraft.class_310;
import net.minecraft.class_757;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.HudMod;
import xaero.common.core.XaeroMinimapCore;
import xaero.common.events.ClientEvents;

@Mixin({class_757.class})
public class MixinFabricGameRenderer {
   @Shadow
   private class_310 field_4015;

   @Inject(
      at = {@At("HEAD")},
      method = {"method_3192(Lnet/minecraft/class_9779;Z)V"}
   )
   public void onRenderStart(class_9779 deltaTracker, boolean boolean_1, CallbackInfo info) {
      if (XaeroMinimapCore.isModLoaded()) {
         ClientEvents fmlEvents = HudMod.INSTANCE.getEvents();
         if (fmlEvents != null) {
            fmlEvents.handleRenderTickStart();
         }

      }
   }

   @Inject(
      at = {@At("TAIL")},
      method = {"method_3192(Lnet/minecraft/class_9779;Z)V"}
   )
   public void onRenderEnd(class_9779 deltaTracker, boolean boolean_1, CallbackInfo info) {
      if (XaeroMinimapCore.isModLoaded()) {
         if (!this.field_4015.field_1743 && this.field_4015.method_18506() == null && this.field_4015.field_1755 != null) {
            ClientEvents events = HudMod.INSTANCE.getEvents();
            if (events != null) {
               events.handleDrawScreenEventPost(this.field_4015.field_1755);
            }
         }

      }
   }
}
