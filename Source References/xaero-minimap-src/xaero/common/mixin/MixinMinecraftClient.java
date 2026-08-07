package xaero.common.mixin;

import net.minecraft.class_310;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xaero.common.core.IXaeroMinimapMinecraftClient;

@Mixin({class_310.class})
public class MixinMinecraftClient implements IXaeroMinimapMinecraftClient {
   @Shadow
   private static int field_1738;

   public int getXaeroMinimap_fps() {
      return field_1738;
   }
}
