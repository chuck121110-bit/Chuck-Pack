package xaero.common.mixin;

import net.minecraft.class_2637;
import net.minecraft.class_4076;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xaero.common.core.IXaeroMinimapSMultiBlockChangePacket;

@Mixin({class_2637.class})
public class MixinChunkDeltaUpdateS2CPacket implements IXaeroMinimapSMultiBlockChangePacket {
   @Shadow
   class_4076 field_26345;

   public class_4076 xaero_mm_getSectionPos() {
      return this.field_26345;
   }
}
