package xaeroplus.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.lang.ref.WeakReference;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xaero.map.world.MapDimension;
import xaero.map.world.MapWorld;

@Mixin(
   value = {MapWorld.class},
   remap = false
)
public abstract class MixinMapWorld {
   @Unique
   WeakReference<MapDimension> xaeroPlus$currentDimensionRef = new WeakReference((Object)null);
   @Shadow
   private class_5321<class_1937> currentDimensionId;

   @Shadow
   public abstract MapDimension getDimension(final class_5321<class_1937> dimId);

   @WrapOperation(
      method = {"switchToFutureUnsynced"},
      at = {@At(
   value = "FIELD",
   opcode = 181,
   target = "Lxaero/map/world/MapWorld;currentDimensionId:Lnet/minecraft/class_5321;"
)},
      remap = true
   )
   public void setCurrentDimensionRef(final MapWorld instance, final class_5321<class_1937> value, final Operation<Void> original) {
      original.call(new Object[]{instance, value});
      this.xaeroPlus$currentDimensionRef = new WeakReference(this.getDimension(value));
   }

   @Overwrite
   public MapDimension getCurrentDimension() {
      class_5321<class_1937> dimId = this.currentDimensionId;
      MapDimension ref = (MapDimension)this.xaeroPlus$currentDimensionRef.get();
      if (dimId == null) {
         return null;
      } else {
         return ref != null ? ref : this.getDimension(dimId);
      }
   }
}
