package xaeroplus.mixin.client.mc;

import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_4969;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaeroplus.XaeroPlus;
import xaeroplus.event.RespawnPointSetEvent;

@Mixin({class_4969.class})
public abstract class MixinRespawnAnchor {
   @Inject(
      method = {"method_55766"},
      at = {@At(
   value = "FIELD",
   opcode = 178,
   target = "Lnet/minecraft/class_1269;field_21466:Lnet/minecraft/class_1269$class_9860;"
)}
   )
   public void checkRespawnAnchorRespawnPointSet(final class_2680 state, final class_1937 level, final class_2338 pos, final class_1657 player, final class_3965 hitResult, final CallbackInfoReturnable<class_1269> cir) {
      if (player == class_310.method_1551().field_1724) {
         XaeroPlus.EVENT_BUS.call(new RespawnPointSetEvent(pos));
      }
   }
}
