package xaero.map.mixin;

import net.minecraft.class_10859;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xaero.map.core.IWorldMapGlBuffer;

@Mixin({class_10859.class})
public class MixinGlBuffer implements IWorldMapGlBuffer {
   @Shadow
   private int field_57842;

   public int xaero_wm_getHandle() {
      return this.field_57842;
   }
}
