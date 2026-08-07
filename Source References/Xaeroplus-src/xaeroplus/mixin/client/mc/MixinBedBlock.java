package xaeroplus.mixin.client.mc;

import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2244;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaeroplus.XaeroPlus;
import xaeroplus.event.RespawnPointSetEvent;

@Mixin({class_2244.class})
public abstract class MixinBedBlock {
   @Inject(
      method = {"method_55766"},
      at = {@At("HEAD")}
   )
   public void checkBedSpawnPointSet(final class_2680 state, final class_1937 level, final class_2338 pos, final class_1657 player, final class_3965 hitResult, final CallbackInfoReturnable<class_1269> cir) {
      if (player == class_310.method_1551().field_1724) {
         if (level.method_27983() == class_1937.field_25179) {
            if (!(Boolean)state.method_11654(class_2244.field_9968)) {
               XaeroPlus.EVENT_BUS.call(new RespawnPointSetEvent(pos));
            }
         }
      }
   }
}
