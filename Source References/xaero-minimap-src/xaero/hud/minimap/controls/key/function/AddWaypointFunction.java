package xaero.hud.minimap.controls.key.function;

import com.google.common.collect.Lists;
import net.minecraft.class_310;
import net.minecraft.class_437;
import xaero.common.HudMod;
import xaero.common.gui.GuiAddWaypoint;
import xaero.hud.controls.key.function.KeyMappingFunction;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;

public class AddWaypointFunction extends KeyMappingFunction {
   protected AddWaypointFunction() {
      super(false);
   }

   public void onPress() {
      MinimapSession session = BuiltInHudModules.MINIMAP.getCurrentSession();
      if (HudMod.INSTANCE.getSettings().waypointsGUI(session)) {
         class_310.method_1551().method_1507(new GuiAddWaypoint(HudMod.INSTANCE, session, (class_437)null, Lists.newArrayList(), session.getWorldState().getCurrentWorldPath().getRoot(), session.getWorldManager().getCurrentWorld(), true));
      }
   }

   public void onRelease() {
   }
}
