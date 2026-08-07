package xaero.hud.minimap.waypoint;

import net.minecraft.class_1074;
import net.minecraft.class_124;
import net.minecraft.class_1659;
import net.minecraft.class_1937;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import net.minecraft.class_5321;
import xaero.common.HudMod;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.config.primary.option.MinimapPrimaryClientConfigOptions;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.minimap.world.container.MinimapWorldRootContainer;
import xaero.hud.minimap.world.container.config.RootConfig;
import xaero.hud.path.XaeroPath;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.common.config.Config;
import xaero.lib.common.config.single.SingleConfigManager;

public class WaypointTeleport {
   public static final String TELEPORT_ANYWAY_COMMAND = "xaero_tp_anyway";
   public static final String SLASH_TELEPORT_ANYWAY_COMMAND = "/xaero_tp_anyway";
   private final HudMod modMain;
   private final class_310 mc;
   private final WaypointSession session;
   private final MinimapSession minimapSession;
   private Waypoint teleportAnywayWP;
   private MinimapWorld teleportAnywayWorld;

   public WaypointTeleport(HudMod modMain, WaypointSession session, MinimapSession minimapSession) {
      this.modMain = modMain;
      this.session = session;
      this.minimapSession = minimapSession;
      this.mc = class_310.method_1551();
   }

   public boolean canTeleport(boolean displayingTeleportableWorld, MinimapWorld displayedWorld) {
      ClientConfigManager configManager = this.modMain.getHudConfigs().getClientConfigManager();
      SingleConfigManager<Config> primaryConfigManager = configManager.getPrimaryConfigManager();
      return ((Boolean)primaryConfigManager.getEffective(MinimapPrimaryClientConfigOptions.WRONG_WORLD_TELEPORT) || displayingTeleportableWorld) && displayedWorld.getRootConfig().isTeleportationEnabled();
   }

   public void teleportAnyway() {
      if (this.teleportAnywayWP != null) {
         class_437 dummyScreen = new class_437(class_2561.method_43470("")) {
         };
         class_310 minecraft = class_310.method_1551();
         dummyScreen.method_25423(minecraft.method_22683().method_4486(), minecraft.method_22683().method_4502());
         this.teleportToWaypoint(this.teleportAnywayWP, this.teleportAnywayWorld, dummyScreen, false);
      }
   }

   public void teleportToWaypoint(Waypoint waypoint, MinimapWorld world, class_437 screen) {
      this.teleportToWaypoint(waypoint, world, screen, true);
   }

   public void teleportToWaypoint(Waypoint waypoint, MinimapWorld world, class_437 screen, boolean respectHiddenCoords) {
      this.minimapSession.getWorldStateUpdater().update();
      boolean isTeleportableWorld = this.isWorldTeleportable(world);
      if (waypoint != null && this.canTeleport(isTeleportableWorld, world)) {
         this.mc.method_1507((class_437)null);
         if (!waypoint.isYIncluded() && this.mc.field_1761.method_2908()) {
            class_5250 messageComponent = class_2561.method_43470(class_1074.method_4662("gui.xaero_teleport_y_unknown", new Object[0]));
            messageComponent.method_10862(messageComponent.method_10866().method_10977(class_124.field_1061));
            this.mc.field_1705.method_1743().method_1812(messageComponent);
         } else {
            String fullCommand = "";
            boolean crossDimension = false;
            MinimapWorldRootContainer rootContainer = world.getContainer().getRoot();
            MinimapWorld autoWorld = this.minimapSession.getWorldManager().getAutoWorld();
            if (isTeleportableWorld && world != autoWorld) {
               if (!this.isTeleportationSafe(world)) {
                  class_5250 messageComponent = class_2561.method_43470(class_1074.method_4662("gui.xaero_teleport_not_connected", new Object[0]));
                  messageComponent.method_10862(messageComponent.method_10866().method_10977(class_124.field_1061));
                  this.mc.field_1705.method_1743().method_1812(messageComponent);
                  return;
               }

               boolean reachableDimension = true;
               if (autoWorld == null || autoWorld.getContainer() != world.getContainer()) {
                  crossDimension = true;
                  XaeroPath containerPath = world.getContainer().getPath();
                  if (containerPath.getNodeCount() > 1) {
                     String dimensionNode = containerPath.getAtIndex(1).getLastNode();
                     if (!dimensionNode.startsWith("dim%")) {
                        this.mc.field_1705.method_1743().method_1812(class_2561.method_43471("gui.xaero_visit_needed"));
                        return;
                     }

                     class_5321<class_1937> dimensionId = this.minimapSession.getDimensionHelper().getDimensionKeyForDirectoryName(dimensionNode);
                     if (dimensionId != null) {
                        this.minimapSession.getWorldState().setCustomWorldPath((XaeroPath)null);
                        fullCommand = "/execute in " + String.valueOf(dimensionId.method_29177()) + " run ";
                     } else {
                        reachableDimension = false;
                     }
                  } else {
                     reachableDimension = false;
                  }
               }

               if (!reachableDimension) {
                  this.mc.field_1705.method_1743().method_1812(class_2561.method_43470(class_1074.method_4662("gui.xaero_unreachable_dimension", new Object[0])).method_27692(class_124.field_1061));
                  return;
               }
            }

            ClientConfigManager configManager = this.modMain.getHudConfigs().getClientConfigManager();
            boolean hideWaypointCoordinatesConfig = (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.HIDE_WAYPOINT_COORDINATES);
            if (respectHiddenCoords && hideWaypointCoordinatesConfig && this.mc.field_1690.method_42539().method_41753() != class_1659.field_7536) {
               class_5250 messageComponent = class_2561.method_43470(class_1074.method_4662("gui.xaero_teleport_coordinates_hidden", new Object[0]));
               messageComponent.method_10862(messageComponent.method_10866().method_10977(class_124.field_1075));
               this.mc.field_1705.method_1743().method_1812(messageComponent);
               class_5250 clickableQuestion = class_2561.method_43470("§e[" + class_1074.method_4662("gui.xaero_teleport_anyway", new Object[0]) + "]");
               clickableQuestion.method_10862(clickableQuestion.method_10866().method_10958(new class_2558.class_10609("/xaero_tp_anyway")).method_10949(new class_2568.class_10613(class_2561.method_43470(class_1074.method_4662("gui.xaero_teleport_shows_coordinates", new Object[0])).method_27692(class_124.field_1061))));
               this.teleportAnywayWP = waypoint;
               this.teleportAnywayWorld = world;
               this.mc.field_1705.method_1743().method_1812(clickableQuestion);
            } else {
               int x = waypoint.getX();
               int z = waypoint.getZ();
               double dimDiv = this.minimapSession.getDimensionHelper().getDimensionDivision(world);
               if (!crossDimension && dimDiv != (double)1.0F) {
                  x = (int)Math.floor((double)x / dimDiv);
                  z = (int)Math.floor((double)z / dimDiv);
               }

               RootConfig config = rootContainer.getConfig();
               String serverTpCommand = waypoint.isRotation() ? config.getServerTeleportCommandRotationFormat() : config.getServerTeleportCommandFormat();
               String defaultTpCommand = (String)configManager.getEffective(waypoint.isRotation() ? MinimapProfiledConfigOptions.WAYPOINT_DEFAULT_TELEPORT_ROTATION_FORMAT : MinimapProfiledConfigOptions.WAYPOINT_DEFAULT_TELEPORT_FORMAT);
               String tpCommand = !config.isUsingDefaultTeleportCommand() && serverTpCommand != null ? serverTpCommand : defaultTpCommand;
               if (!fullCommand.isEmpty()) {
                  if (tpCommand.startsWith("/")) {
                     tpCommand = tpCommand.substring(1);
                  }

                  if (tpCommand.startsWith("minecraft:")) {
                     tpCommand = tpCommand.substring(10);
                  }
               }

               boolean partialYConfig = (Boolean)configManager.getEffective(MinimapProfiledConfigOptions.WAYPOINT_PARTIAL_Y_TELEPORT);
               String yString = !waypoint.isYIncluded() ? "~" : (partialYConfig ? "" + ((double)waypoint.getY() + (double)0.5F) : "" + waypoint.getY());
               tpCommand = tpCommand.replace("{x}", "" + x).replace("{y}", yString).replace("{z}", "" + z).replace("{name}", waypoint.getLocalizedName());
               if (waypoint.isRotation()) {
                  tpCommand = tpCommand.replace("{yaw}", "" + waypoint.getYaw());
               }

               fullCommand = fullCommand + tpCommand;
               if (fullCommand.startsWith("/")) {
                  fullCommand = fullCommand.substring(1);
                  this.mc.field_1724.field_3944.method_45730(fullCommand);
               } else {
                  this.mc.field_1724.field_3944.method_45729(fullCommand);
               }
            }
         }
      }
   }

   public boolean isWorldTeleportable(MinimapWorld displayedWorld) {
      MinimapWorld autoWorld = this.minimapSession.getWorldManager().getAutoWorld();
      MinimapWorldRootContainer rootContainer = displayedWorld.getContainer().getRoot();
      if (!rootContainer.getPath().equals(this.minimapSession.getWorldState().getAutoRootContainerPath())) {
         return false;
      } else if (autoWorld == displayedWorld) {
         return true;
      } else if (autoWorld == null) {
         return false;
      } else {
         return autoWorld.getContainer() == displayedWorld.getContainer() ? true : (Boolean)this.modMain.getHudConfigs().getClientConfigManager().getEffective(MinimapProfiledConfigOptions.WAYPOINT_TELEPORT_CROSS_DIMENSION);
      }
   }

   public boolean isTeleportationSafe(MinimapWorld displayedWorld) {
      if (!class_310.method_1551().field_1761.method_2908()) {
         return true;
      } else {
         MinimapWorld autoWorld = this.minimapSession.getWorldManager().getAutoWorld();
         MinimapWorldRootContainer rootContainer = displayedWorld.getContainer().getRoot();
         return rootContainer.getSubWorldConnections().isConnected(autoWorld, displayedWorld);
      }
   }
}
