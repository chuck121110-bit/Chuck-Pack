package xaero.hud.minimap.world.container.config.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_5321;
import net.minecraft.class_7924;
import xaero.common.HudMod;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointRenderInfo;
import xaero.hud.minimap.waypoint.WaypointVisibilityType;
import xaero.hud.minimap.waypoint.WaypointsSort;
import xaero.hud.minimap.waypoint.thirdparty.ThirdPartyWaypointManager;
import xaero.hud.minimap.waypoint.thirdparty.ThirdPartyWaypoints;
import xaero.hud.minimap.world.container.MinimapWorldContainer;
import xaero.hud.minimap.world.container.MinimapWorldRootContainer;
import xaero.hud.minimap.world.container.config.RootConfig;
import xaero.hud.path.XaeroPath;
import xaero.hud.path.XaeroPathReader;

public class RootConfigIO {
   private final HudMod modMain;

   public RootConfigIO(HudMod modMain) {
      this.modMain = modMain;
   }

   private File getFile(MinimapWorldRootContainer rootContainer) {
      Path directoryPath = rootContainer.getDirectoryPath();

      try {
         if (!Files.exists(directoryPath, new LinkOption[0])) {
            Files.createDirectories(directoryPath);
         }
      } catch (IOException e) {
         MinimapLogs.LOGGER.error("suppressed exception", e);
      }

      return directoryPath.resolve("config.txt").toFile();
   }

   public void save(MinimapWorldRootContainer rootContainer) {
      File configFile = this.getFile(rootContainer);
      PrintWriter writer = null;
      RootConfig config = rootContainer.getConfig();

      try {
         writer = new PrintWriter(new FileWriter(configFile, StandardCharsets.UTF_8));
         writer.println("//waypoints config options");
         writer.println("usingMultiworldDetection:" + config.isUsingMultiworldDetection());
         writer.println("ignoreServerLevelId:" + config.isIgnoreServerLevelId());
         if (config.getDefaultMultiworldId() != null) {
            writer.println("defaultMultiworldId:" + config.getDefaultMultiworldId());
         }

         writer.println("teleportationEnabled:" + config.isTeleportationEnabled());
         writer.println("usingDefaultTeleportCommand:" + config.isUsingDefaultTeleportCommand());
         if (config.getServerTeleportCommandFormat() != null) {
            String var10001 = config.getServerTeleportCommandFormat();
            writer.println("serverTeleportCommandFormat:" + var10001.replace(":", "^col^"));
         }

         if (config.getServerTeleportCommandRotationFormat() != null) {
            String var17 = config.getServerTeleportCommandRotationFormat();
            writer.println("serverTeleportCommandRotationFormat:" + var17.replace(":", "^col^"));
         }

         writer.println("sortType:" + config.getSortType().name());
         writer.println("sortReversed:" + config.isSortReversed());
         writer.println("");
         writer.println("//other config options");
         writer.println("ignoreHeightmaps:" + config.isIgnoreHeightmaps());
         rootContainer.getSubWorldConnections().save(writer);
         writer.println("");
         writer.println("//dimension types (DO NOT EDIT)");

         for(Map.Entry<class_5321<class_1937>, class_2960> entry : rootContainer.getDimensionTypeIds()) {
            String var18 = ((class_5321)entry.getKey()).method_29177().toString().replace(':', '$');
            writer.println("dimensionType:" + var18 + ":" + ((class_2960)entry.getValue()).toString().replace(':', '$'));
         }

         writer.println("//third-party waypoints");

         for(MinimapWorldContainer dimContainer : rootContainer.getSubContainers()) {
            ThirdPartyWaypointManager thirdPartyWaypointManager = dimContainer.getThirdPartyWaypointManager();
            if (thirdPartyWaypointManager != null) {
               for(ThirdPartyWaypoints thirdPartyWaypoints : thirdPartyWaypointManager.getAll()) {
                  for(Map.Entry<String, WaypointRenderInfo> entry : thirdPartyWaypoints.getRenderInfoOverrides().entrySet()) {
                     String waypointId = (String)entry.getKey();
                     WaypointRenderInfo renderInfoOverride = (WaypointRenderInfo)entry.getValue();
                     writer.print("third-party-waypoint");
                     writer.print(":");
                     writer.print(dimContainer.getLastNode());
                     writer.print(":");
                     writer.print(thirdPartyWaypoints.getOriginId().method_12836());
                     writer.print(":");
                     writer.print(thirdPartyWaypoints.getOriginId().method_12832());
                     writer.print(":");
                     writer.print(waypointId.replace(":", "§§"));
                     writer.print(":");
                     if (renderInfoOverride.getDisabled() != null) {
                        writer.print(renderInfoOverride.getDisabled());
                     }

                     writer.print(":");
                     writer.print(renderInfoOverride.isThirdPartyDeleted());
                     writer.print(":");
                     if (renderInfoOverride.getInitials() != null) {
                        writer.print(renderInfoOverride.getInitialsSafe("§§"));
                     }

                     writer.print(":");
                     if (renderInfoOverride.getWaypointColor() != null) {
                        writer.print(renderInfoOverride.getWaypointColor());
                     }

                     writer.print(":");
                     if (renderInfoOverride.getVisibility() != null) {
                        writer.print(renderInfoOverride.getVisibility());
                     }

                     writer.println(":;");
                  }
               }
            }
         }
      } catch (IOException e) {
         MinimapLogs.LOGGER.error("suppressed exception", e);
      }

      if (writer != null) {
         writer.close();
      }

   }

   public void load(MinimapWorldRootContainer rootContainer) {
      RootConfig config = rootContainer.getConfig();
      config.setLoaded(true);
      File configFile = this.getFile(rootContainer);
      if (!configFile.exists()) {
         this.save(rootContainer);
      } else {
         BufferedReader reader = null;

         try {
            reader = new BufferedReader(new FileReader(configFile, StandardCharsets.UTF_8));

            String line;
            while((line = reader.readLine()) != null) {
               String[] args = line.split(":");
               String valueString = args.length < 2 ? "" : args[1];
               if (args[0].equals("usingMultiworldDetection")) {
                  config.setUsingMultiworldDetection(valueString.equals("true"));
               } else if (args[0].equals("ignoreServerLevelId")) {
                  config.setIgnoreServerLevelId(valueString.equals("true"));
               } else if (args[0].equals("defaultMultiworldId")) {
                  if (valueString.matches("[a-zA-Z,$0-9-]+")) {
                     config.setDefaultMultiworldId(valueString);
                  } else {
                     MinimapLogs.LOGGER.warn("Ignoring invalid defaultMultiworldId in {}", configFile);
                  }
               } else if (args[0].equals("teleportationEnabled")) {
                  config.setTeleportationEnabled(valueString.equals("true"));
               } else if (args[0].equals("usingDefaultTeleportCommand")) {
                  config.setUsingDefaultTeleportCommand(valueString.equals("true"));
               } else if (args[0].equals("teleportCommand")) {
                  config.setServerTeleportCommandFormat("/" + valueString.replace("^col^", ":") + " {x} {y} {z}");
                  config.setServerTeleportCommandRotationFormat("/" + valueString.replace("^col^", ":") + " {x} {y} {z} {yaw} ~");
               } else if (args[0].equals("serverTeleportCommand")) {
                  config.setServerTeleportCommandFormat(valueString.replace("^col^", ":") + " {x} {y} {z}");
                  config.setServerTeleportCommandRotationFormat(valueString.replace("^col^", ":") + " {x} {y} {z} {yaw} ~");
               } else if (args[0].equals("serverTeleportCommandFormat")) {
                  config.setServerTeleportCommandFormat(valueString.replace("^col^", ":"));
               } else if (args[0].equals("serverTeleportCommandRotationFormat")) {
                  config.setServerTeleportCommandRotationFormat(valueString.replace("^col^", ":"));
               } else if (args[0].equals("sortType")) {
                  config.setSortType(WaypointsSort.valueOf(valueString));
               } else if (args[0].equals("sortReversed")) {
                  config.setSortReversed(valueString.equals("true"));
               } else if (args[0].equals("ignoreHeightmaps")) {
                  config.setIgnoreHeightmaps(valueString.equals("true"));
               } else if (args[0].equals("connection")) {
                  XaeroPath worldKey1 = (new XaeroPathReader()).read(valueString);
                  if (args.length > 2) {
                     XaeroPath worldKey2 = (new XaeroPathReader()).read(args[2]);
                     config.getSubWorldConnections().addConnection(worldKey1, worldKey2);
                  }
               } else if (args[0].equals("dimensionType")) {
                  try {
                     rootContainer.setDimensionTypeId(class_5321.method_29179(class_7924.field_41223, class_2960.method_60654(args[1].replace('$', ':'))), class_2960.method_60654(args[2].replace('$', ':')));
                  } catch (Throwable var16) {
                  }
               } else if (args[0].equals("third-party-waypoint") && args.length >= 11) {
                  String dimensionNode = args[1];
                  String originNamespace = args[2];
                  String originPath = args[3];
                  class_2960 originId = class_2960.method_60655(originNamespace, originPath);
                  MinimapWorldContainer dimContainer = rootContainer.addSubContainer(rootContainer.getPath().resolve(dimensionNode));
                  String id = Waypoint.getStringFromStringSafe(args[4], "§§");
                  WaypointRenderInfo defaultRenderInfoOverride = new WaypointRenderInfo();
                  defaultRenderInfoOverride.setDisabled(args[5].isEmpty() ? null : args[5].equals("true"));
                  defaultRenderInfoOverride.setThirdPartyDeleted(args[6].equals("true"));
                  defaultRenderInfoOverride.setInitials(args[7].isEmpty() ? null : Waypoint.getStringFromStringSafe(args[7], "§§"));
                  defaultRenderInfoOverride.setWaypointColor(args[8].isEmpty() ? null : WaypointColor.valueOf(args[8]));
                  defaultRenderInfoOverride.setVisibility(args[9].isEmpty() ? null : WaypointVisibilityType.valueOf(args[9]));
                  dimContainer.getThirdPartyWaypointManager().get(originId).addRenderInfoOverride(id, defaultRenderInfoOverride);
               }
            }
         } catch (IOException e) {
            MinimapLogs.LOGGER.error("suppressed exception", e);
         }

         if (reader != null) {
            try {
               reader.close();
            } catch (IOException e) {
               MinimapLogs.LOGGER.error("suppressed exception", e);
            }
         }

      }
   }
}
