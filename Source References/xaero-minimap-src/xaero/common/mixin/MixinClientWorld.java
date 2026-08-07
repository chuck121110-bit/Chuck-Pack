package xaero.common.mixin;

import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import xaero.common.minimap.mcworld.IXaeroMinimapClientWorld;
import xaero.common.minimap.mcworld.MinimapClientWorldData;

@Mixin({class_638.class})
public class MixinClientWorld implements IXaeroMinimapClientWorld {
   private MinimapClientWorldData xaero_minimapData;

   public MinimapClientWorldData getXaero_minimapData() {
      return this.xaero_minimapData;
   }

   public void setXaero_minimapData(MinimapClientWorldData xaero_minimapData) {
      this.xaero_minimapData = xaero_minimapData;
   }
}
