package xaero.hud.minimap;

import net.minecraft.class_2561;
import net.minecraft.class_2960;
import xaero.common.gui.GuiMinimapMain;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.module.MinimapRenderer;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.module.HudModule;
import xaero.hud.module.ModuleManager;

public class BuiltInHudModules {
   public static final HudModule<MinimapSession> MINIMAP;

   public static void addAll(ModuleManager manager) {
      manager.register(MINIMAP);
   }

   static {
      MINIMAP = new HudModule<MinimapSession>(class_2960.method_60655("xaerominimap", "minimap"), class_2561.method_43471("gui.xaero_minimap"), MinimapSession::new, MinimapRenderer::new, GuiMinimapMain::new, MinimapProfiledConfigOptions.DISPLAY_MINIMAP);
   }
}
