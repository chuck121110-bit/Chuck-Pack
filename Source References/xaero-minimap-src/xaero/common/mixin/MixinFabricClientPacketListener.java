package xaero.common.mixin;

import net.minecraft.class_634;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.HudMod;
import xaero.common.core.XaeroMinimapCore;

@Mixin({class_634.class})
public class MixinFabricClientPacketListener {
   @Inject(
      at = {@At("HEAD")},
      method = {"method_45729(Ljava/lang/String;)V"},
      cancellable = true
   )
   public void onSendChat(String string_1, CallbackInfo info) {
      if (XaeroMinimapCore.isModLoaded()) {
         if (HudMod.INSTANCE.getEvents().handleClientSendChatEvent(string_1)) {
            info.cancel();
         }

      }
   }
}
