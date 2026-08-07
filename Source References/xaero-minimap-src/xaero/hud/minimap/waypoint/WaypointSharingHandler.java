package xaero.hud.minimap.waypoint;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.class_124;
import net.minecraft.class_1937;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_2568;
import net.minecraft.class_310;
import net.minecraft.class_410;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import net.minecraft.class_5321;
import xaero.common.HudMod;
import xaero.common.gui.GuiAddWaypoint;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.minimap.world.container.MinimapWorldContainer;
import xaero.hud.minimap.world.container.MinimapWorldContainerUtil;
import xaero.hud.minimap.world.container.MinimapWorldRootContainer;
import xaero.hud.path.XaeroPath;

public final class WaypointSharingHandler {
   public static final String WAYPOINT_OLD_SHARE_PREFIX = "xaero_waypoint:";
   public static final String WAYPOINT_ADD_PREFIX = "xaero_waypoint_add:";
   public static final String WAYPOINT_SHARE_PREFIX = "xaero-waypoint:";
   private static final String DESTINATION_PREFIX_INTERNAL = "Internal";
   private static final String DESTINATION_PREFIX_INTERNAL_HYPHEN = "Internal-";
   private static final String DESTINATION_PREFIX_EXTERNAL = "External";
   private HudMod modMain;
   private MinimapSession session;
   private class_437 confirmScreenParent;
   private Waypoint sharedWaypoint;
   private MinimapWorld minimapWorld;

   private WaypointSharingHandler(HudMod modMain, MinimapSession session) {
      this.modMain = modMain;
      this.session = session;
   }

   public void shareWaypoint(class_437 currentScreen, Waypoint waypoint, MinimapWorld minimapWorld) {
      this.confirmScreenParent = currentScreen;
      this.sharedWaypoint = waypoint;
      this.minimapWorld = minimapWorld;
      class_310.method_1551().method_1507(new class_410(this::onShareConfirmationResult, class_2561.method_43471("gui.xaero_share_msg1"), class_2561.method_43471("gui.xaero_share_msg2")));
   }

   public void onShareConfirmationResult(boolean confirmed) {
      if (!confirmed) {
         class_310.method_1551().method_1507(this.confirmScreenParent);
      } else {
         String destinationDetails = this.getSharedDestinationDetails(this.minimapWorld.getContainer());
         String var10000 = this.removeFormatting(this.sharedWaypoint.getNameSafe("^col^"));
         String message = "xaero-waypoint:" + var10000 + ":" + this.removeFormatting(this.sharedWaypoint.getInitialsSafe("^col^")) + ":" + this.sharedWaypoint.getX() + ":" + String.valueOf(this.sharedWaypoint.isYIncluded() ? this.sharedWaypoint.getY() : "~") + ":" + this.sharedWaypoint.getZ() + ":" + this.sharedWaypoint.getWaypointColor().ordinal() + ":" + this.sharedWaypoint.isRotation() + ":" + this.sharedWaypoint.getYaw() + ":" + destinationDetails;
         class_310.method_1551().field_1705.method_1743().method_1803(message);
         class_310.method_1551().field_1724.field_3944.method_45729(message);
         class_310.method_1551().method_1507((class_437)null);
      }
   }

   private String getSharedDestinationDetails(MinimapWorldContainer minimapWorldContainer) {
      MinimapWorldContainer rootContainer = minimapWorldContainer.getRoot();
      MinimapWorldContainer autoRootContainer = this.session.getWorldManager().getAutoWorld().getContainer().getRoot();
      if (rootContainer != autoRootContainer) {
         return "External";
      } else {
         XaeroPath containerPath = minimapWorldContainer.getPath();
         if (containerPath.getNodeCount() <= 1) {
            return "Internal";
         } else {
            XaeroPath containerSubPath = containerPath.getSubPath(1);
            String dimKey = containerSubPath.getRoot().getLastNode();
            if (dimKey.equals("dim%0")) {
               dimKey = "overworld";
            } else if (dimKey.equals("dim%-1")) {
               dimKey = "the_nether";
            } else if (dimKey.equals("dim%1")) {
               dimKey = "the_end";
            }

            containerSubPath = XaeroPath.root(dimKey).resolve(containerSubPath.getSubPath(1));
            String subContainersString = containerSubPath.toString().replace(":", "^col^");
            String var10000 = this.removeFormatting(subContainersString);
            return "Internal-" + var10000;
         }
      }
   }

   public void onWaypointReceived(String playerName, String text) {
      text = text.replaceAll("§.", "");
      boolean newFormat = text.contains("xaero-waypoint:");
      String sharePrefix = newFormat ? "xaero-waypoint:" : "xaero_waypoint:";
      String[] args = text.substring(text.indexOf(sharePrefix)).split(":");
      if (args.length < 9) {
         MinimapLogs.LOGGER.info("Incorrect format of the shared waypoint! Error: 0");
      } else {
         if (newFormat) {
            args[1] = this.restoreFormatting(args[1]);
            args[2] = this.restoreFormatting(args[2]);
         }

         class_2561 waypointName = class_2561.method_43471(Waypoint.getStringFromStringSafe(args[1], "^col^"));
         class_2561 dimensionName = null;
         if (args.length > 9) {
            if (args[9].equals("Internal")) {
               XaeroPath potentialContainerPath = this.session.getWorldStateUpdater().getPotentialContainerPath();
               args[9] = this.getSharedDestinationDetails(this.session.getWorldManager().getWorldContainer(potentialContainerPath));
            }

            if (args[9].startsWith("Internal-")) {
               dimensionName = this.getReceivedDimensionName(args[9]);
            }
         }

         class_5250 mainComponent = class_2561.method_43469(dimensionName != null ? "gui.xaero_waypoint_shared_dimension2" : "gui.xaero_waypoint_shared2", new Object[]{playerName, waypointName, dimensionName});
         StringBuilder addCommandBuilder = new StringBuilder();
         addCommandBuilder.append("xaero_waypoint_add:");
         addCommandBuilder.append(args[1]);

         for(int i = 2; i < args.length; ++i) {
            addCommandBuilder.append(':').append(args[i]);
         }

         String addCommand = addCommandBuilder.toString();
         class_5250 hoverComponent = class_2561.method_43470(args[3] + ", " + args[4] + ", " + args[5]);
         class_2558 clickEvent = new class_2558.class_10609("/" + addCommand);
         class_2568 hoverEvent = new class_2568.class_10613(hoverComponent);
         class_5250 addComponent = class_2561.method_43471("gui.xaero_waypoint_shared_add").method_27692(class_124.field_1077).method_27692(class_124.field_1073);
         mainComponent.method_10855().add(addComponent);
         mainComponent.method_10862(mainComponent.method_10866().method_27706(class_124.field_1080).method_10958(clickEvent).method_10949(hoverEvent));
         class_310.method_1551().field_1705.method_1743().method_1812(mainComponent);
      }
   }

   private class_2561 getReceivedDimensionName(String destinationDetails) {
      int lastMinus = destinationDetails.lastIndexOf("-");
      if (lastMinus == -1) {
         return null;
      } else {
         String containerPathRaw = lastMinus == "Internal".length() ? destinationDetails.substring("Internal-".length()) : destinationDetails.substring("Internal-".length(), lastMinus);
         String containerPathString = this.restoreFormatting(containerPathRaw.replace("^col^", ":"));
         if (containerPathString.contains("/")) {
            return class_2561.method_43470(containerPathString);
         } else if (!containerPathString.startsWith("dim%")) {
            return class_2561.method_43470(containerPathString);
         } else if (containerPathString.length() == 4) {
            return class_2561.method_43471("gui.xaero_waypoint_unknown_dimension");
         } else {
            class_5321<class_1937> dimId = this.session.getDimensionHelper().getDimensionKeyForDirectoryName(containerPathString);
            return dimId == null ? class_2561.method_43471("gui.xaero_waypoint_unknown_dimension") : class_2561.method_43470(dimId.method_29177().method_12832());
         }
      }
   }

   public void onWaypointAdd(String[] args) {
      String waypointName = Waypoint.getStringFromStringSafe(args[1], "^col^");
      if (waypointName.length() >= 1 && waypointName.length() <= 32) {
         String waypointSymbol = Waypoint.getStringFromStringSafe(args[2], "^col^");
         if (waypointSymbol.length() >= 1 && waypointSymbol.length() <= 3) {
            if (this.session.getWorldState().getAutoWorldPath() == null) {
               MinimapLogs.LOGGER.info("Can't add a waypoint at this time!");
            } else {
               boolean yIsIncluded = !args[4].equals("~");

               int x;
               int y;
               int z;
               WaypointColor color;
               int yaw;
               try {
                  x = Integer.parseInt(args[3]);
                  y = yIsIncluded ? Integer.parseInt(args[4]) : 0;
                  z = Integer.parseInt(args[5]);
                  int colorIndex = Integer.parseInt(args[6]);
                  if (colorIndex < 0) {
                     colorIndex = 0;
                  }

                  colorIndex %= WaypointColor.values().length;
                  color = WaypointColor.fromIndex(colorIndex);
                  String yawString = args[8];
                  if (yawString.length() > 4) {
                     MinimapLogs.LOGGER.info("Incorrect format of the shared waypoint! Error: 4");
                     return;
                  }

                  yaw = Integer.parseInt(yawString);
               } catch (NumberFormatException var14) {
                  MinimapLogs.LOGGER.info("Incorrect format of the shared waypoint! Error: 3");
                  return;
               }

               boolean rotation = args[7].equals("true");
               Waypoint waypoint = new Waypoint(x, y, z, waypointName, waypointSymbol, color, WaypointPurpose.NORMAL, false, yIsIncluded);
               waypoint.setRotation(rotation);
               waypoint.setYaw(yaw);
               MinimapWorld externalWorld = this.session.getWorldManager().getCurrentWorld();
               MinimapWorld destinationWorld = externalWorld;
               if (args.length > 9) {
                  destinationWorld = this.getReceivedDestinationWorld(args[9], externalWorld);
                  if (destinationWorld == null) {
                     return;
                  }
               }

               class_310.method_1551().method_1507(new GuiAddWaypoint(this.modMain, this.session, (class_437)null, (class_437)null, Lists.newArrayList(new Waypoint[]{waypoint}), destinationWorld.getContainer().getRoot().getPath(), destinationWorld, destinationWorld.getCurrentWaypointSetId(), true));
            }
         } else {
            MinimapLogs.LOGGER.info("Incorrect format of the shared waypoint! Error: 2");
         }
      } else {
         MinimapLogs.LOGGER.info("Incorrect format of the shared waypoint! Error: 1");
      }
   }

   private MinimapWorld getReceivedDestinationWorld(String destinationDetails, MinimapWorld externalWorld) {
      if (destinationDetails.equals("External")) {
         return externalWorld;
      } else if (destinationDetails.startsWith("Internal-") && !destinationDetails.equals("Internal-")) {
         int divider = destinationDetails.lastIndexOf(45);
         if (divider == "Internal".length()) {
            divider = destinationDetails.length();
         }

         String containerPathString = destinationDetails.substring("Internal-".length(), divider);
         containerPathString = this.restoreFormatting(containerPathString.replace("^col^", ":"));
         if (containerPathString.contains("\\")) {
            MinimapLogs.LOGGER.info("Incorrect format of the shared waypoint! Error: 13");
            return null;
         } else {
            String[] containerPathNodes = containerPathString.split("/");
            if (containerPathNodes.length != 1) {
               MinimapLogs.LOGGER.info("Incorrect format of the shared waypoint! Error: 8");
               return null;
            } else {
               for(int i = 0; i < containerPathNodes.length; ++i) {
                  String s = containerPathNodes[i];
                  if (s.isEmpty()) {
                     MinimapLogs.LOGGER.info("Incorrect format of the shared waypoint! Error: 11");
                     return null;
                  }
               }

               Optional<class_5321<class_1937>> receivedDimId = this.getReceivedDimId(containerPathNodes);
               if (receivedDimId == null) {
                  return null;
               } else if (receivedDimId.isEmpty()) {
                  return externalWorld;
               } else {
                  class_5321<class_1937> dimId = (class_5321)receivedDimId.get();
                  containerPathNodes[0] = this.session.getDimensionHelper().getDimensionDirectoryName(dimId);
                  XaeroPath containerPath = this.session.getWorldState().getAutoRootContainerPath();

                  for(String node : containerPathNodes) {
                     containerPath = containerPath.resolve(node);
                  }

                  try {
                     XaeroPath subContainerPath = containerPath.getSubPath(1);
                     Path rootContainerFilePath = this.modMain.getMinimapFolder().resolve(containerPath.getRoot().getLastNode());
                     Path rootContainerCanonicalPath = rootContainerFilePath.toFile().getCanonicalFile().toPath();
                     Path securityTest = subContainerPath.applyToFilePath(rootContainerCanonicalPath).resolve("test_1.txt");
                     if (!securityTest.equals(securityTest.toFile().getCanonicalFile().toPath())) {
                        MinimapLogs.LOGGER.info("Dangerously incorrect format of the shared waypoint! Error: 10");
                        return null;
                     }
                  } catch (IOException e) {
                     MinimapLogs.LOGGER.error("IO exception during file path check when adding a shared waypoint!", e);
                     return null;
                  } catch (InvalidPathException ipe) {
                     MinimapLogs.LOGGER.error("Invalid path exception during file path check when adding a shared waypoint!", ipe);
                     return null;
                  }

                  MinimapWorldRootContainer rootContainer = this.session.getWorldManager().getAutoRootContainer();
                  rootContainer.renameOldContainer(containerPath);
                  MinimapWorldContainer worldContainer = this.session.getWorldManager().getWorldContainer(containerPath);
                  MinimapWorld autoWorld = this.session.getWorldManager().getAutoWorld();
                  MinimapWorld destinationWorld;
                  if (worldContainer == autoWorld.getContainer()) {
                     destinationWorld = autoWorld;
                  } else {
                     destinationWorld = worldContainer.getFirstWorldConnectedTo(autoWorld);
                     if (destinationWorld == null) {
                        destinationWorld = worldContainer.getFirstWorld();
                     }

                     if (destinationWorld == null) {
                        destinationWorld = worldContainer.addWorld(this.session.getWorldStateUpdater().getPotentialWorldNode(dimId, false));
                     }
                  }

                  if (!this.modMain.getSupportMods().worldmap()) {
                     return destinationWorld;
                  } else if (!MinimapWorldContainerUtil.isMultiplayer(containerPath)) {
                     return destinationWorld;
                  } else {
                     for(String mw : this.modMain.getSupportMods().worldmapSupport.getMultiworldIds(dimId)) {
                        this.session.getWorldManager().addWorld(containerPath.resolve(mw));
                     }

                     return destinationWorld;
                  }
               }
            }
         }
      } else {
         MinimapLogs.LOGGER.info("Incorrect format of the shared waypoint! Error: 12");
         return null;
      }
   }

   private Optional<class_5321<class_1937>> getReceivedDimId(String[] containerPathNodes) {
      String dimensionNode = containerPathNodes[0];
      class_5321<class_1937> dimId;
      if (!dimensionNode.startsWith("dim%")) {
         if (!dimensionNode.replaceAll("[^a-zA-Z0-9_]+", "").equals(dimensionNode)) {
            MinimapLogs.LOGGER.info("Incorrect format of the shared waypoint! Error: 18");
            return null;
         }

         dimId = this.session.getDimensionHelper().findDimensionKeyForOldName(class_310.method_1551().field_1724, dimensionNode);
      } else {
         dimId = this.session.getDimensionHelper().getDimensionKeyForDirectoryName(dimensionNode);
      }

      if (dimId == null) {
         MinimapLogs.LOGGER.info("Destination dimension doesn't exist! Handling waypoint as external.");
         return Optional.empty();
      } else {
         return Optional.of(dimId);
      }
   }

   private String removeFormatting(String s) {
      return s.replace("-", "^min^").replace("_", "-").replace("*", "^ast^");
   }

   private String restoreFormatting(String s) {
      return s.replace("^ast^", "*").replace("-", "_").replace("^min^", "-");
   }

   public static final class Builder {
      private HudMod modMain;
      private MinimapSession session;

      private Builder() {
      }

      public Builder setDefault() {
         this.setModMain((HudMod)null);
         this.setSession((MinimapSession)null);
         return this;
      }

      public Builder setModMain(HudMod modMain) {
         this.modMain = modMain;
         return this;
      }

      public Builder setSession(MinimapSession session) {
         this.session = session;
         return this;
      }

      public WaypointSharingHandler build() {
         if (this.modMain != null && this.session != null) {
            return new WaypointSharingHandler(this.modMain, this.session);
         } else {
            throw new IllegalStateException();
         }
      }

      public static Builder begin() {
         return (new Builder()).setDefault();
      }
   }
}
