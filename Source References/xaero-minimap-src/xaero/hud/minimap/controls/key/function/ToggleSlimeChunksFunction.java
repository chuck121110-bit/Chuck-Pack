package xaero.hud.minimap.controls.key.function;

import net.minecraft.class_310;
import net.minecraft.class_437;
import xaero.common.HudMod;
import xaero.common.gui.GuiSlimeSeed;
import xaero.hud.controls.key.function.KeyMappingFunction;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.module.MinimapSession;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.config.context.BuiltInEditConfigScreenContexts;
import xaero.lib.common.config.profile.ConfigProfile;

public class ToggleSlimeChunksFunction extends KeyMappingFunction {
   protected ToggleSlimeChunksFunction() {
      super(false);
   }

   public void onPress() {
      HudMod modMain = HudMod.INSTANCE;
      MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
      ClientConfigManager configManager = modMain.getHudConfigs().getClientConfigManager();
      if (class_310.method_1551().method_1576() == null && (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.OPEN_SLIME_CHUNKS_SCREEN)) {
         class_437 current = class_310.method_1551().field_1755;
         class_437 currentEscScreen = current instanceof ScreenBase ? ((ScreenBase)current).escape : null;
         class_310.method_1551().method_1507(new GuiSlimeSeed(modMain, session, current, currentEscScreen, BuiltInEditConfigScreenContexts.CLIENT));
      } else {
         ConfigProfile currentProfile = configManager.getCurrentProfile();
         currentProfile.set(MinimapProfiledConfigOptions.SLIME_CHUNKS, !(Boolean)currentProfile.get(MinimapProfiledConfigOptions.SLIME_CHUNKS));
         modMain.getHudConfigs().getClientConfigProfileIO().save(currentProfile);
      }
   }

   public void onRelease() {
   }
}
