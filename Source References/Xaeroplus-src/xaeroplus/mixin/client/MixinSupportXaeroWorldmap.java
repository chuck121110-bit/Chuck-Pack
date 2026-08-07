package xaeroplus.mixin.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.common.minimap.render.MinimapRendererHelper;
import xaero.common.mods.SupportXaeroWorldmap;
import xaero.map.WorldMapSession;
import xaero.map.region.MapTileChunk;
import xaeroplus.Globals;

@Mixin(
   value = {SupportXaeroWorldmap.class},
   remap = false
)
public abstract class MixinSupportXaeroWorldmap {
   @Inject(
      method = {"drawMinimap"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/hud/minimap/config/util/MinimapConfigClientUtils;getEffectiveSlimeChunks(Lxaero/hud/minimap/module/MinimapSession;)Z"
)},
      remap = false
   )
   public void overrideRegionRange(final CallbackInfo ci, @Local(name = {"minViewX"}) int minViewX, @Local(name = {"minViewZ"}) int minViewZ, @Local(name = {"maxViewX"}) int maxViewX, @Local(name = {"maxViewZ"}) int maxViewZ, @Local(name = {"mapX"}) int mapX, @Local(name = {"mapZ"}) int mapZ, @Local(name = {"minX"}) LocalIntRef minXRef, @Local(name = {"maxX"}) LocalIntRef maxXRef, @Local(name = {"minZ"}) LocalIntRef minZRef, @Local(name = {"maxZ"}) LocalIntRef maxZRef) {
      int scaledSize = Globals.minimapScaleMultiplier * 4;
      minXRef.set(Math.min(minViewX, (mapX >> 2) - scaledSize));
      maxXRef.set(Math.max(maxViewX, (mapX >> 2) + scaledSize));
      minZRef.set(Math.min(minViewZ, (mapZ >> 2) - scaledSize));
      maxZRef.set(Math.max(maxViewZ, (mapZ >> 2) + scaledSize));
   }

   @WrapWithCondition(
      method = {"renderChunks"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/common/mods/SupportXaeroWorldmap;renderSlimeChunks(Lxaero/map/region/MapTileChunk;Ljava/lang/Long;IILnet/minecraft/class_4587;Lxaero/common/minimap/render/MinimapRendererHelper;Lnet/minecraft/class_4588;)V"
)},
      remap = true
   )
   public boolean hideSlimeChunksWhileDimSwitched(SupportXaeroWorldmap instance, MapTileChunk chunk, Long seed, int drawX, int drawZ, class_4587 matrixStack, MinimapRendererHelper helper, class_4588 overlayBufferBuilder) {
      return Globals.getCurrentDimensionId() == class_310.method_1551().field_1687.method_27983();
   }

   @Inject(
      method = {"tryToGetMultiworldId"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/map/WorldMapSession;getMapProcessor()Lxaero/map/MapProcessor;"
)},
      cancellable = true,
      remap = false
   )
   public void preventPossibleNPE(final CallbackInfoReturnable<String> cir, @Local WorldMapSession session) {
      if (session == null) {
         cir.setReturnValue((Object)null);
      }

   }
}
