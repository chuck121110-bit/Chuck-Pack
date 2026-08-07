package xaero.hud.minimap.world.state;

import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_310;
import net.minecraft.class_5218;
import net.minecraft.class_5321;
import net.minecraft.class_634;
import net.minecraft.class_642;
import xaero.common.HudMod;
import xaero.common.minimap.mcworld.MinimapClientWorldData;
import xaero.common.minimap.mcworld.MinimapClientWorldDataHelper;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.config.primary.option.MinimapPrimaryClientConfigOptions;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.world.container.MinimapWorldContainer;
import xaero.hud.minimap.world.container.MinimapWorldContainerUtil;
import xaero.hud.minimap.world.container.MinimapWorldRootContainer;
import xaero.hud.minimap.world.container.config.RootConfig;
import xaero.hud.path.XaeroPath;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.common.config.Config;
import xaero.lib.common.config.single.SingleConfigManager;
import xaero.lib.common.util.IOUtils;

public class MinimapWorldStateUpdater {
   public static final int ROOT_CONTAINER_FORMAT = 4;
   private final HudMod modMain;
   private final MinimapSession session;
   private final class_634 connection;
   private class_2338 currentWorldSpawn;

   public MinimapWorldStateUpdater(HudMod modMain, MinimapSession session, class_634 connection) {
      this.modMain = modMain;
      this.session = session;
      this.connection = connection;
   }

   public void init() {
      this.session.getWorldState().setAutoRootContainerPath(this.getAutoRootContainerPath(4));

      for(int i = 0; i < 4; ++i) {
         this.session.getWorldState().setOutdatedAutoRootContainerPath(i, this.getAutoRootContainerPath(i));
      }

   }

   public void update() {
      MinimapWorldState state = this.session.getWorldState();
      XaeroPath oldAutoWorldPath = state.getAutoWorldPath();
      XaeroPath potentialAutoContainerPath = this.getPotentialContainerPath();
      state.setAutoContainerPathIgnoreCaseCache(potentialAutoContainerPath);
      boolean worldmap = this.modMain.getSupportMods().worldmap();
      String potentialAutoWorldNode = this.getPotentialWorldNode(this.session.getMc().field_1687.method_27983(), worldmap);
      if (potentialAutoWorldNode != null) {
         XaeroPath autoWorldPath = potentialAutoContainerPath.resolve(potentialAutoWorldNode);
         state.setAutoWorldPath(autoWorldPath);
         if (oldAutoWorldPath == null || !potentialAutoContainerPath.equals(oldAutoWorldPath.getParent())) {
            MinimapWorldRootContainer autoRootContainer = this.session.getWorldManager().getAutoRootContainer();
            autoRootContainer.renameOldContainer(potentialAutoContainerPath);
            autoRootContainer.updateDimensionType(this.session.getMc().field_1687);
            if (oldAutoWorldPath != null) {
               MinimapWorldContainer oldContainer = this.session.getWorldManager().getWorldContainer(oldAutoWorldPath.getParent());
               oldContainer.getServerWaypointManager().clear();
            }
         }
      }
   }

   public XaeroPath getPotentialContainerPath() {
      String dimensionNode = this.session.getDimensionHelper().getDimensionDirectoryName(this.session.getMc().field_1687.method_27983());
      XaeroPath potentialContainerPath = this.session.getWorldState().getAutoRootContainerPath().resolve(dimensionNode);
      return this.ignoreContainerCase(potentialContainerPath, this.session.getWorldState().getAutoContainerPathIgnoreCaseCache());
   }

   public XaeroPath ignoreContainerCase(XaeroPath potentialContainerPath, XaeroPath currentPath) {
      if (potentialContainerPath.equals(currentPath)) {
         return currentPath;
      } else {
         for(MinimapWorldRootContainer rootContainer : this.session.getWorldManager().getRootContainers()) {
            XaeroPath containerSearch = rootContainer.fixPathCharacterCases(potentialContainerPath);
            if (containerSearch != null) {
               return containerSearch;
            }
         }

         return potentialContainerPath;
      }
   }

   public XaeroPath getAutoRootContainerPath(int version) {
      class_642 serverData = this.connection.method_45734();
      class_310 mc = class_310.method_1551();
      String potentialContainerID;
      if (mc.method_1576() != null) {
         potentialContainerID = MinimapWorldContainerUtil.convertWorldFolderToContainerNode(mc.method_1576().method_27050(class_5218.field_24188).getParent().getFileName().toString(), version);
      } else if (serverData != null && serverData.method_52811() && this.modMain.getEvents().latestRealm != null) {
         String var10000 = String.valueOf(this.modMain.getEvents().latestRealm.field_22605);
         potentialContainerID = "Realms_" + var10000 + "." + this.modMain.getEvents().latestRealm.field_22599;
      } else if (serverData != null) {
         ClientConfigManager configManager = this.modMain.getHudConfigs().getClientConfigManager();
         SingleConfigManager<Config> primaryConfigManager = configManager.getPrimaryConfigManager();
         boolean differentiateByServerAddress = (Boolean)primaryConfigManager.getEffective(MinimapPrimaryClientConfigOptions.DIFFERENTIATE_BY_SERVER_ADDRESS);
         String serverIP = differentiateByServerAddress ? serverData.field_3761 : "Any Address";
         int portDivider;
         if (version >= 1 && serverIP.indexOf(":") != serverIP.lastIndexOf(":")) {
            portDivider = serverIP.lastIndexOf("]:") + 1;
         } else {
            portDivider = serverIP.indexOf(":");
         }

         if (portDivider > 0) {
            serverIP = serverIP.substring(0, portDivider);
         }

         while(serverIP.endsWith(".")) {
            serverIP = serverIP.substring(0, serverIP.length() - 1);
         }

         if (version >= 2) {
            serverIP = serverIP.replace("[", "").replace("]", "");
         }

         serverIP = serverIP.replace(":", version < 3 ? "§" : ".").replace("_", "%us%").replace("/", "%fs%").replace("\\", "%bs%");
         if (version >= 4) {
            serverIP = serverIP.trim();
            serverIP = IOUtils.replaceTrailingDots(serverIP, ',');
         }

         if (serverIP.isEmpty()) {
            serverIP = "Empty Address";
         }

         potentialContainerID = "Multiplayer_" + serverIP;
      } else {
         potentialContainerID = "Unknown";
      }

      XaeroPath potentialContainerPath = XaeroPath.root(potentialContainerID);
      potentialContainerPath = this.ignoreContainerCase(potentialContainerPath, (XaeroPath)null);
      return potentialContainerPath;
   }

   public String getPotentialWorldNode(class_5321<class_1937> dimId, boolean useWorldmap) {
      if (this.session.getMc().method_1576() != null) {
         return "waypoints";
      } else {
         MinimapWorldState state = this.session.getWorldState();
         MinimapWorldRootContainer rootContainer = this.session.getWorldManager().getRootWorldContainer(state.getAutoRootContainerPath());
         Object autoNodeBase = this.getAutoWorldNodeBase(rootContainer);
         if (autoNodeBase == null) {
            return null;
         } else {
            String worldmapWorldNode = useWorldmap ? this.modMain.getSupportMods().worldmapSupport.tryToGetMultiworldId(dimId) : null;
            if (useWorldmap && worldmapWorldNode == null) {
               return null;
            } else {
               String actualWorldNode;
               if (autoNodeBase instanceof class_2338) {
                  class_2338 pos = (class_2338)autoNodeBase;
                  int var10000 = pos.method_10263() >> 6;
                  actualWorldNode = "mw" + var10000 + "," + (pos.method_10264() >> 6) + "," + (pos.method_10260() >> 6);
                  RootConfig config = rootContainer.getConfig();
                  if (!config.isUsingMultiworldDetection()) {
                     String defaultMultiworldId = config.getDefaultMultiworldId();
                     if (defaultMultiworldId == null) {
                        config.setDefaultMultiworldId(actualWorldNode);
                        this.session.getWorldManagerIO().getRootConfigIO().save(rootContainer);
                     } else {
                        actualWorldNode = defaultMultiworldId;
                     }
                  }
               } else {
                  actualWorldNode = "mw$" + String.valueOf(autoNodeBase);
               }

               if (useWorldmap && worldmapWorldNode != "minimap") {
                  actualWorldNode = worldmapWorldNode;
               }

               return actualWorldNode;
            }
         }
      }
   }

   public boolean hasServerLevelId(MinimapWorldRootContainer rootContainer) {
      MinimapClientWorldData worldData = MinimapClientWorldDataHelper.getCurrentWorldData();
      return worldData.serverLevelId != null && !rootContainer.getConfig().isIgnoreServerLevelId();
   }

   public Object getAutoWorldNodeBase(MinimapWorldRootContainer rootContainer) {
      MinimapClientWorldData worldData = MinimapClientWorldDataHelper.getCurrentWorldData();
      return this.hasServerLevelId(rootContainer) ? worldData.serverLevelId : this.currentWorldSpawn;
   }

   public void onServerLevelId(int id) {
      MinimapClientWorldData worldData = MinimapClientWorldDataHelper.getCurrentWorldData();
      worldData.serverLevelId = id;
      MinimapLogs.LOGGER.info("Minimap updated server level id: " + id + " for world " + String.valueOf(class_310.method_1551().field_1687.method_27983()));
   }

   public void setCurrentWorldSpawn(class_2338 currentWorldSpawn) {
      this.currentWorldSpawn = currentWorldSpawn;
   }
}
