package xaeroplus.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xaero.common.minimap.waypoints.Waypoint;
import xaeroplus.feature.extensions.SyncedWaypoint;
import xaeroplus.settings.Settings;

@Mixin(
   targets = {"xaero/common/gui/GuiWaypoints$List"},
   remap = false
)
public abstract class MixinGuiWaypointsList {
   @ModifyExpressionValue(
      method = {"drawWaypointSlot"},
      at = {@At(
   value = "CONSTANT",
   args = {"stringValue=gui.xaero_temporary"}
)}
   )
   public String syncedWaypointTranslationKey(final String original, @Local(argsOnly = true) Waypoint wp) {
      if (!Settings.REGISTRY.waypointsListUIAdditions.get()) {
         return original;
      } else {
         return wp instanceof SyncedWaypoint ? "xaeroplus.gui.waypoints.synced_waypoint" : original;
      }
   }
}
