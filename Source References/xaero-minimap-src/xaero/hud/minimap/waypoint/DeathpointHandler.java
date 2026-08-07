package xaero.hud.minimap.waypoint;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import net.minecraft.class_1657;
import xaero.common.HudMod;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.misc.OptimizedMath;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.path.XaeroPath;
import xaero.lib.client.config.ClientConfigManager;

public class DeathpointHandler {
   private final HudMod modMain;
   private final MinimapSession session;

   public DeathpointHandler(HudMod modMain, MinimapSession session) {
      this.modMain = modMain;
      this.session = session;
   }

   public void createDeathpoint(class_1657 player) {
      this.session.getWorldStateUpdater().update();
      ClientConfigManager configManager = this.modMain.getHudConfigs().getClientConfigManager();
      if ((Boolean)configManager.getEffective(MinimapProfiledConfigOptions.AUTO_WAYPOINTS_ON_DEATH)) {
         this.session.getWorldState().setCustomWorldPath((XaeroPath)null);
      }

      boolean worldmap = this.modMain.getSupportMods().worldmap();
      MinimapWorld potentialAutoWorld = null;
      XaeroPath usedAutoWorldPath = this.session.getWorldState().getAutoWorldPath();
      XaeroPath usedAutoContainerPath = usedAutoWorldPath == null ? null : usedAutoWorldPath.getParent();
      XaeroPath potentialAutoContainerPath = this.session.getWorldStateUpdater().getPotentialContainerPath();
      if (!potentialAutoContainerPath.equals(usedAutoContainerPath)) {
         String potentialAutoWorldNode = this.session.getWorldStateUpdater().getPotentialWorldNode(this.session.getMc().field_1687.method_27983(), worldmap);
         if (potentialAutoWorldNode != null) {
            XaeroPath potentialAutoWorldPath = potentialAutoContainerPath.resolve(potentialAutoWorldNode);
            potentialAutoWorld = this.session.getWorldManager().getWorld(potentialAutoWorldPath);
            this.createDeathpoint(player, potentialAutoWorld, false);
         }
      }

      MinimapWorld autoWorld = this.session.getWorldManager().getAutoWorld();
      if (potentialAutoWorld == null && autoWorld != null) {
         this.createDeathpoint(player, autoWorld, false);
      }

      if (worldmap) {
         List<String> allPotentialMWIds = this.modMain.getSupportMods().worldmapSupport.getPotentialMultiworldIds(player.method_73183().method_27983());
         if (allPotentialMWIds != null) {
            for(String mwId : allPotentialMWIds) {
               MinimapWorld potentialWorld = this.session.getWorldManager().getWorld(potentialAutoContainerPath.resolve(mwId));
               if (potentialWorld != autoWorld && potentialWorld != potentialAutoWorld) {
                  this.createDeathpoint(player, potentialWorld, false);
               }
            }

         }
      }
   }

   public void createDeathpoint(class_1657 player, MinimapWorld world, boolean temporary) {
      WaypointSet currentSet = world.getCurrentWaypointSet();
      if (currentSet != null) {
         ClientConfigManager configManager = this.modMain.getHudConfigs().getClientConfigManager();
         boolean disabled = false;
         boolean oldDeathpoints = (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.OLD_DEATHPOINTS);

         for(WaypointSet set : world.getIterableWaypointSets()) {
            Iterator<Waypoint> waypoints = set.getWaypoints().iterator();

            while(waypoints.hasNext()) {
               Waypoint w = (Waypoint)waypoints.next();
               if (w.getPurpose() == WaypointPurpose.DEATH) {
                  if (set == currentSet) {
                     disabled = w.isDisabled();
                  }

                  if (!oldDeathpoints) {
                     waypoints.remove();
                  } else {
                     w.setPurpose(WaypointPurpose.OLD_DEATH);
                     w.setName("gui.xaero_deathpoint_old");
                  }
                  break;
               }
            }
         }

         double dimDiv = this.session.getDimensionHelper().getDimensionDivision(world);
         if ((Boolean)configManager.getEffective(MinimapProfiledConfigOptions.DEATHPOINTS)) {
            Waypoint deathpoint = new Waypoint(OptimizedMath.myFloor((double)OptimizedMath.myFloor(player.method_23317()) * dimDiv), OptimizedMath.myFloor(player.method_23318()), OptimizedMath.myFloor((double)OptimizedMath.myFloor(player.method_23321()) * dimDiv), "gui.xaero_deathpoint", "D", WaypointColor.BLACK, WaypointPurpose.DEATH);
            deathpoint.setTemporary(temporary);
            deathpoint.setDisabled(disabled);
            currentSet.add(deathpoint, true);
         }

         try {
            this.session.getWorldManagerIO().saveWorld(world);
         } catch (IOException e) {
            MinimapLogs.LOGGER.error("suppressed exception", e);
         }

      }
   }
}
