package xaero.common.mixin;

import com.mojang.authlib.GameProfile;
import java.time.Instant;
import net.minecraft.class_2556;
import net.minecraft.class_2561;
import net.minecraft.class_7471;
import net.minecraft.class_7594;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.common.HudMod;
import xaero.common.core.XaeroMinimapCore;

@Mixin({class_7594.class})
public class MixinFabricChatListener {
   @Inject(
      method = {"method_44943(Lnet/minecraft/class_2556$class_7602;Lnet/minecraft/class_7471;Lnet/minecraft/class_2561;Lcom/mojang/authlib/GameProfile;ZLjava/time/Instant;)Z"},
      cancellable = true,
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_338;method_44811(Lnet/minecraft/class_2561;Lnet/minecraft/class_7469;Lnet/minecraft/class_7591;)V"
)}
   )
   public void onShowMessageToPlayer(class_2556.class_7602 bound, class_7471 playerChatMessage, class_2561 component, GameProfile gameProfile, boolean bl, Instant instant, CallbackInfoReturnable<Boolean> info) {
      if (XaeroMinimapCore.isModLoaded()) {
         if (HudMod.INSTANCE.getEvents().handleClientPlayerChatReceivedEvent(bound, component, gameProfile)) {
            info.setReturnValue(false);
         }

      }
   }
}
