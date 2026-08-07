package xaero.common.cache;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_2189;
import net.minecraft.class_2338;
import net.minecraft.class_2404;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_2350.class_2351;
import xaero.common.IXaeroMinimap;
import xaero.common.cache.placeholder.PlaceholderBlockGetter;
import xaero.hud.minimap.MinimapLogs;

public class BlockStateShortShapeCache {
   private IXaeroMinimap modMain;
   private Map<class_2680, Boolean> shortBlockStates;
   private class_2680 lastShortChecked = null;
   private boolean lastShortCheckedResult = false;
   private PlaceholderBlockGetter placeholderBlockGetter;

   public BlockStateShortShapeCache(IXaeroMinimap modMain) {
      this.modMain = modMain;
      this.shortBlockStates = new HashMap();
      this.placeholderBlockGetter = new PlaceholderBlockGetter();
   }

   public boolean isShort(class_2680 state) {
      if (state != null && !(state.method_26204() instanceof class_2189) && !(state.method_26204() instanceof class_2404)) {
         Boolean cached;
         synchronized(this.shortBlockStates) {
            if (state == this.lastShortChecked) {
               return this.lastShortCheckedResult;
            }

            cached = (Boolean)this.shortBlockStates.get(state);
         }

         if (cached == null) {
            if (!class_310.method_1551().method_18854()) {
               return (Boolean)class_310.method_1551().method_5385(() -> this.isShort(state)).join();
            }

            try {
               this.placeholderBlockGetter.setPlaceholderState(state);
               class_265 shape = state.method_26218(this.placeholderBlockGetter, class_2338.field_10980);
               cached = shape.method_1105(class_2351.field_11052) < (double)0.25F;
            } catch (Throwable var7) {
               MinimapLogs.LOGGER.info("Defaulting world-dependent block state shape to not short: " + String.valueOf(state));
               cached = false;
            }

            synchronized(this.shortBlockStates) {
               this.shortBlockStates.put(state, cached);
               this.lastShortChecked = state;
               this.lastShortCheckedResult = cached;
            }
         }

         return cached;
      } else {
         return false;
      }
   }

   public void reset() {
      synchronized(this.shortBlockStates) {
         this.shortBlockStates.clear();
         this.lastShortChecked = null;
         this.lastShortCheckedResult = false;
      }
   }
}
