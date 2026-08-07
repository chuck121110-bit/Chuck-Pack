package xaero.hud.controls;

import java.util.function.Consumer;
import net.minecraft.class_304;
import xaero.hud.controls.key.KeyMappingControllerManager;
import xaero.hud.minimap.controls.key.MinimapKeyMappings;

public abstract class ControlsRegister {
   protected final KeyMappingControllerManager keyMappingControllers = new KeyMappingControllerManager();

   protected ControlsRegister() {
   }

   public void registerKeybindings(Consumer<class_304> registry, Consumer<class_304.class_11900> categoryRegistry) {
      MinimapKeyMappings.registerAll(this.keyMappingControllers, registry, categoryRegistry);
   }

   public void onStage2() {
   }

   public KeyMappingControllerManager getKeyMappingControllers() {
      return this.keyMappingControllers;
   }
}
