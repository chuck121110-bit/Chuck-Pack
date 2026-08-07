package xaeroplus.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.Supplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xaero.map.cache.BlockStateShortShapeCache;

@Mixin(
   value = {BlockStateShortShapeCache.class},
   remap = false
)
public class MixinBlockStateShortShapeCache {
   @WrapOperation(
      method = {"supplyForIOThread"},
      at = {@At(
   value = "FIELD",
   opcode = 180,
   target = "Lxaero/map/cache/BlockStateShortShapeCache;ioThreadWaitingForSupplier:Ljava/util/function/Supplier;"
)}
   )
   public Supplier<Boolean> patchConcurrencyCrash(final BlockStateShortShapeCache instance, final Operation<Supplier<Boolean>> original) {
      Supplier<Boolean> supplier = (Supplier)original.call(new Object[]{instance});
      return supplier != null ? supplier : () -> false;
   }
}
