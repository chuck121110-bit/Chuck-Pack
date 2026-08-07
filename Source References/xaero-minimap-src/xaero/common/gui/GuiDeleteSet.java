package xaero.common.gui;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import net.minecraft.class_1074;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_410;
import net.minecraft.class_437;
import xaero.common.HudMod;
import xaero.common.IXaeroMinimap;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.world.MinimapWorldManager;
import xaero.hud.path.XaeroPath;

public class GuiDeleteSet extends class_410 {
   public GuiDeleteSet(String setName, XaeroPath worldPath, String name, class_437 parent, class_437 escapeScreen, IXaeroMinimap modMain, MinimapSession session) {
      BooleanConsumer var10001 = (result) -> confirmDeleteSet(result, worldPath, name, parent, escapeScreen, modMain, session);
      String var10002 = class_1074.method_4662("gui.xaero_delete_set_message", new Object[0]);
      super(var10001, class_2561.method_43470(var10002 + ": " + setName.replace("§§", ":") + "?"), class_2561.method_43471("gui.xaero_delete_set_message2"));
   }

   private static void confirmDeleteSet(boolean p_confirmResult_1_, XaeroPath worldPath, String name, class_437 parent, class_437 escapeScreen, IXaeroMinimap modMain, MinimapSession session) {
      if (p_confirmResult_1_) {
         MinimapWorldManager waypointsManager = session.getWorldManager();
         waypointsManager.getWorld(worldPath).removeWaypointSet(name);
         waypointsManager.getWorld(worldPath).setCurrentWaypointSetId("gui.xaero_default");

         try {
            session.getWorldManagerIO().saveWorld(waypointsManager.getWorld(worldPath));
         } catch (IOException e) {
            MinimapLogs.LOGGER.error("suppressed exception", e);
         }

         class_310.method_1551().method_1507(new GuiWaypoints((HudMod)modMain, session, ((GuiWaypoints)parent).parent, escapeScreen));
      } else {
         class_310.method_1551().method_1507(parent);
      }

   }
}
