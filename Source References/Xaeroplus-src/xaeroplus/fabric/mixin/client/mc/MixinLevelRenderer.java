package xaeroplus.fabric.mixin.client.mc;

import net.minecraft.class_11658;
import net.minecraft.class_11661;
import net.minecraft.class_4587;
import net.minecraft.class_761;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaeroplus.feature.render.beacon.WaypointBeaconRenderer;

@Mixin({class_761.class})
public class MixinLevelRenderer {
   @Inject(
      method = {"method_62208"},
      at = {@At("HEAD")}
   )
   public void renderBlockEntitiesInject(final class_4587 poseStack, final class_11658 levelRenderState, final class_11661 submitNodeStorage, final CallbackInfo ci) {
      WaypointBeaconRenderer.INSTANCE.renderHook(poseStack, levelRenderState, submitNodeStorage);
   }
}
