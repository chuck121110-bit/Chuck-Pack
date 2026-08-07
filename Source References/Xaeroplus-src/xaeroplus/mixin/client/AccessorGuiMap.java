package xaeroplus.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import xaero.map.gui.GuiMap;

@Mixin(
   value = {GuiMap.class},
   remap = false
)
public interface AccessorGuiMap {
   @Accessor("cameraX")
   double getCameraX();

   @Accessor("cameraZ")
   double getCameraZ();

   @Accessor("destScale")
   static double getDestScale() {
      throw new AssertionError();
   }
}
