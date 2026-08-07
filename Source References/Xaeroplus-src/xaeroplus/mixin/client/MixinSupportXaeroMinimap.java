package xaeroplus.mixin.client;

import java.util.ArrayList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.mods.SupportXaeroMinimap;
import xaero.map.mods.gui.Waypoint;
import xaeroplus.settings.Settings;

@Mixin(
   value = {SupportXaeroMinimap.class},
   remap = false
)
public class MixinSupportXaeroMinimap {
   @Redirect(
      method = {"waypointExists"},
      at = @At(
   value = "INVOKE",
   target = "Ljava/util/ArrayList;contains(Ljava/lang/Object;)Z"
)
   )
   public boolean waypointEqualityRedirect(final ArrayList waypoints, final Object w) {
      try {
         Waypoint waypoint = (Waypoint)w;

         for(Waypoint w2 : waypoints) {
            if (w2.compareTo(waypoint) == 0) {
               return true;
            }
         }

         return false;
      } catch (Exception var7) {
         return waypoints.contains(w);
      }
   }

   @Inject(
      method = {"getSubWorldNameToRender"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void getSubworldNameToRenderInject(final CallbackInfoReturnable<String> cir) {
      if (Settings.REGISTRY.owAutoWaypointDimension.get()) {
         cir.setReturnValue((Object)null);
      }

   }
}
