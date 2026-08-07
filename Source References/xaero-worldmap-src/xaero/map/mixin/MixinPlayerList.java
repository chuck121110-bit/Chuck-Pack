package xaero.map.mixin;

import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3324;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.server.core.XaeroWorldMapServerCore;

@Mixin({class_3324.class})
public class MixinPlayerList {
   @Inject(
      at = {@At("HEAD")},
      method = {"method_14606(Lnet/minecraft/class_3222;Lnet/minecraft/class_3218;)V"}
   )
   public void onSendWorldInfo(class_3222 player, class_3218 world, CallbackInfo info) {
      XaeroWorldMapServerCore.onServerWorldInfo(player);
   }
}
