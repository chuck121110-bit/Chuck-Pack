package xaeroplus.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.class_2556;
import net.minecraft.class_2561;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.common.events.ClientEvents;
import xaeroplus.settings.Settings;

@Mixin(
   value = {ClientEvents.class},
   remap = false
)
public abstract class MixinClientEvents {
   @Inject(
      method = {"handleClientSystemChatReceivedEvent"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onSystemChatReceived(final class_2561 component, final CallbackInfoReturnable<Boolean> cir) {
      if (component != null) {
         if (Settings.REGISTRY.disableReceivingWaypoints.get()) {
            cir.setReturnValue(false);
         }

      }
   }

   @Inject(
      method = {"handleClientPlayerChatReceivedEvent"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onPlayerChatReceived(final class_2556.class_7602 chatType, final class_2561 component, final GameProfile gameProfile, final CallbackInfoReturnable<Boolean> cir) {
      if (component != null) {
         if (Settings.REGISTRY.disableReceivingWaypoints.get()) {
            cir.setReturnValue(false);
         }

      }
   }
}
