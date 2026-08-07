package xaero.common.mixin;

import net.minecraft.class_2535;
import net.minecraft.class_3222;
import net.minecraft.class_3324;
import net.minecraft.class_8792;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.HudMod;
import xaero.common.server.core.XaeroMinimapServerCore;

@Mixin({class_3324.class})
public class MixinFabricPlayerList {
   @Inject(
      at = {@At("TAIL")},
      method = {"method_14570(Lnet/minecraft/class_2535;Lnet/minecraft/class_3222;Lnet/minecraft/class_8792;)V"}
   )
   public void onPlaceNewPlayer(class_2535 connection, class_3222 serverPlayer, class_8792 commonListenerCookie, CallbackInfo info) {
      if (XaeroMinimapServerCore.isModLoaded()) {
         HudMod.INSTANCE.getCommonEvents().onPlayerLogIn(serverPlayer);
      }
   }
}
