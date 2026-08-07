package xaeroplus.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.render.world.WaypointWorldRenderer;
import xaeroplus.feature.waypoint.eta.WaypointEtaManager;
import xaeroplus.settings.Settings;
import xaeroplus.util.ChunkUtils;

@Mixin(
   value = {WaypointWorldRenderer.class},
   remap = false
)
public class MixinWaypointWorldRenderer {
   @Shadow
   private String subWorldName;
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
      WaypointPurpose purpose = w.getPurpose();
      if (purpose == WaypointPurpose.DEATH && Settings.REGISTRY.limitDeathpointsRenderDistance.get() && this.waypointsDistance != (double)0.0F && scaledDistance2D > this.waypointsDistance) {
         cir.setReturnValue(false);
      }

   }

   @ModifyArg(
      method = {"renderElement(Lxaero/common/minimap/waypoints/Waypoint;ZZDFDDLxaero/hud/minimap/element/render/MinimapElementRenderInfo;Lxaero/hud/minimap/element/render/MinimapElementGraphics;Lnet/minecraft/class_4597$class_4598;)Z"},
      at = @At(
   value = "INVOKE",
   target = "Lxaero/hud/minimap/waypoint/render/world/WaypointWorldRenderer;renderIconWithLabels(Lxaero/common/minimap/waypoints/Waypoint;ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;FIILnet/minecraft/class_327;ILnet/minecraft/class_4587;Lxaero/lib/client/graphics/XaeroBufferProvider;)V"
),
      index = 4,
      remap = true
   )
   public String preferOwWaypointsRemoveSubworldText(final String name) {
      if (!Settings.REGISTRY.owAutoWaypointDimension.get()) {
         return name;
      } else if (this.subWorldName == null) {
         return name;
      } else {
         class_5321<class_1937> actualDimension = ChunkUtils.getActualDimension();
         class_5321<class_1937> currentWpWorldDim = ((MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession()).getWorldManager().getCurrentWorld().getDimId();
         return actualDimension == class_1937.field_25180 && currentWpWorldDim == class_1937.field_25179 ? null : name;
      }
   }

   @ModifyArg(
      method = {"renderElement(Lxaero/common/minimap/waypoints/Waypoint;ZZDFDDLxaero/hud/minimap/element/render/MinimapElementRenderInfo;Lxaero/hud/minimap/element/render/MinimapElementGraphics;Lnet/minecraft/class_4597$class_4598;)Z"},
      at = @At(
   value = "INVOKE",
   target = "Lxaero/hud/minimap/waypoint/render/world/WaypointWorldRenderer;renderIconWithLabels(Lxaero/common/minimap/waypoints/Waypoint;ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;FIILnet/minecraft/class_327;ILnet/minecraft/class_4587;Lxaero/lib/client/graphics/XaeroBufferProvider;)V"
),
      index = 3,
      remap = true
   )
   public String modifyDistanceText(final String text, @Local(argsOnly = true) Waypoint waypoint) {
      if (!Settings.REGISTRY.waypointEta.get()) {
         return text;
      } else if (text != null && !text.isBlank()) {
         String etaText = WaypointEtaManager.INSTANCE.getEtaTextSuffix(waypoint);
         return text + etaText;
      } else {
         return text;
      }
   }
}
