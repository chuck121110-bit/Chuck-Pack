package xaero.hud.xminimap.controls.key;

import java.util.function.Consumer;
import net.minecraft.class_304;
import xaero.hud.controls.key.KeyMappingControllerManager;
import xaero.hud.minimap.controls.key.MinimapKeyMappings;
import xaero.hud.xminimap.controls.key.function.XMinimapKeyMappingFunctions;

public class XMinimapKeyMappings {
   public static class_304 SETTINGS;
   public static class_304 SERVER_PROFILES;

   public static void registerAll(KeyMappingControllerManager controllerManager, Consumer<class_304> registry, Consumer<class_304.class_11900> categoryRegistry) {
      controllerManager.registerController(SETTINGS, true, registry);
      controllerManager.registerController(SERVER_PROFILES, true, registry);
      XMinimapKeyMappingFunctions.registerAll(controllerManager);
   }

   static {
      SETTINGS = new class_304("gui.xaero_minimap_settings", 89, MinimapKeyMappings.CATEGORY);
      SERVER_PROFILES = new class_304("gui.xaero_minimap_server_profiles", -1, MinimapKeyMappings.CATEGORY);
   }
}
