package xaero.hud.xminimap.controls;

import java.util.function.Consumer;
import net.minecraft.class_304;
import xaero.hud.controls.ControlsRegister;
import xaero.hud.xminimap.controls.key.XMinimapKeyMappings;

public class XMinimapControlsRegister extends ControlsRegister {
   public void registerKeybindings(Consumer<class_304> registry, Consumer<class_304.class_11900> categoryRegistry) {
      super.registerKeybindings(registry, categoryRegistry);
      XMinimapKeyMappings.registerAll(this.keyMappingControllers, registry, categoryRegistry);
   }
}
