package xaero.hud.xminimap.preset;

import net.minecraft.class_2561;
import net.minecraft.class_2960;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.preset.HudPreset;
import xaero.hud.preset.HudPresetManager;
import xaero.hud.preset.ModulePreset;

public class BuiltInHudPresets {
   public static final HudPreset TOP_LEFT;
   public static final HudPreset TOP_RIGHT;
   public static final HudPreset BOTTOM_LEFT;
   public static final HudPreset BOTTOM_RIGHT;

   public static void addAll(HudPresetManager manager) {
      manager.register(TOP_LEFT);
      manager.register(TOP_RIGHT);
      manager.register(BOTTOM_LEFT);
      manager.register(BOTTOM_RIGHT);
   }

   static {
      TOP_LEFT = HudPreset.Builder.begin().setId(class_2960.method_60655("xaerominimap", "top_left")).setName(class_2561.method_43471("gui.xaero_preset_topleft")).addModulePreset(ModulePreset.Builder.begin(BuiltInHudModules.MINIMAP).setPlacement(0, 0, false, false, false, false, false).build()).build();
      TOP_RIGHT = HudPreset.Builder.begin().setId(class_2960.method_60655("xaerominimap", "top_right")).setName(class_2561.method_43471("gui.xaero_preset_topright")).addModulePreset(ModulePreset.Builder.begin(BuiltInHudModules.MINIMAP).setPlacement(0, 0, false, true, false, false, false).build()).build();
      BOTTOM_LEFT = HudPreset.Builder.begin().setId(class_2960.method_60655("xaerominimap", "bottom_left")).setName(class_2561.method_43471("gui.xaero_preset_bottom_left")).addModulePreset(ModulePreset.Builder.begin(BuiltInHudModules.MINIMAP).setPlacement(0, 0, false, false, true, false, false).build()).build();
      BOTTOM_RIGHT = HudPreset.Builder.begin().setId(class_2960.method_60655("xaerominimap", "bottom_right")).setName(class_2561.method_43471("gui.xaero_preset_bottom_right")).addModulePreset(ModulePreset.Builder.begin(BuiltInHudModules.MINIMAP).setPlacement(0, 0, false, true, true, false, false).build()).build();
   }
}
