package xaero.common.mixin.waystones;

import net.blay09.mods.waystones.api.Waystone;
import net.blay09.mods.waystones.core.PlayerWaystoneManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xaero.common.HudMod;

@Mixin({PlayerWaystoneManager.class})
public class MixinPlayerWaystoneManager {
   @ModifyVariable(
      method = {"deactivateWaystone(Lnet/minecraft/class_1657;Lnet/blay09/mods/waystones/api/Waystone;)V"},
      remap = false,
      index = 1,
      at = @At("HEAD")
   )
   private static Waystone onDeactivation(Waystone waystone) {
      if (HudMod.INSTANCE.getSupportMods() != null) {
         HudMod.INSTANCE.getSupportMods().getSupportWaystones().onWaystoneDeactivation(waystone);
      }

      return waystone;
   }
}
