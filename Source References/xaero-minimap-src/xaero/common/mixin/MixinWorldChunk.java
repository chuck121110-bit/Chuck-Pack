package xaero.common.mixin;

import net.minecraft.class_2818;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({class_2818.class})
public class MixinWorldChunk {
   public boolean xaero_chunkClean = false;
}
