package xaero.hud.xminimap.controls.key.function;

import net.minecraft.class_310;
import net.minecraft.class_437;
import xaero.common.HudMod;
import xaero.common.gui.GuiMinimapMain;
import xaero.hud.controls.key.function.KeyMappingFunction;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.config.context.BuiltInEditConfigScreenContexts;

public class MinimapSettingsFunction extends KeyMappingFunction {
   private final boolean server;

   protected MinimapSettingsFunction(boolean server) {
      super(false);
      this.server = server;
   }

   public void onPress() {
      class_437 current = class_310.method_1551().field_1755;
      class_437 currentEscScreen = current instanceof ScreenBase ? ((ScreenBase)current).escape : null;
      class_310.method_1551().method_1507(new GuiMinimapMain(HudMod.INSTANCE, current, currentEscScreen, true, this.server ? BuiltInEditConfigScreenContexts.SERVER : BuiltInEditConfigScreenContexts.CLIENT));
   }

   public void onRelease() {
   }
}
