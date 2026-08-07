package xaeroplus.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.render.WaypointMapRenderer;
import xaeroplus.settings.Settings;

@Mixin(
   value = {WaypointMapRenderer.class},
   remap = false
)
public class MixinWaypointMapRenderer {
   @Shadow
   private double waypointsDistance;

   @Inject(
      method = {"renderElement(Lxaero/common/minimap/waypoints/Waypoint;ZZDFDDLxaero/hud/minimap/element/render/MinimapElementRenderInfo;Lxaero/hud/minimap/element/render/MinimapElementGraphics;Lnet/minecraft/class_4597$class_4598;)Z"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/common/minimap/waypoints/Waypoint;isDestination()Z",
   ordinal = 0
)},
      cancellable = true,
      remap = true
   )
   public void limitDeathpointsRenderDistance(final CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) Waypoint w, @Local(name = {"scaledDistance2D"}) double scaledDistance2D) {
      if (Settings.REGISTRY.limitDeathpointsRenderDistance.get() && w.getPurpose() == WaypointPurpose.DEATH && this.waypointsDistance != (double)0.0F && scaledDistance2D > this.waypointsDistance) {
         cir.setReturnValue(false);
      }

   }

   @Inject(
      method = {"drawSetChange(Lxaero/hud/minimap/module/MinimapSession;Lnet/minecraft/class_332;Lnet/minecraft/class_1041;)V"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = true
   )
   public void cancelWaypointSetChangeTooltip(final CallbackInfo ci) {
      if (Settings.REGISTRY.disableWaypointSetChangeTooltip.get()) {
         ci.cancel();
      }

   }
}
