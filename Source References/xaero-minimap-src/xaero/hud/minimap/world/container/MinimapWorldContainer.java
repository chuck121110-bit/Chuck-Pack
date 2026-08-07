package xaero.hud.minimap.world.container;

import com.google.common.collect.Iterables;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_5321;
import xaero.common.HudMod;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.server.ServerWaypointManager;
import xaero.hud.minimap.waypoint.thirdparty.ThirdPartyWaypointManager;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.hud.minimap.world.container.config.RootConfig;
import xaero.hud.path.XaeroPath;

public class MinimapWorldContainer {
   public static final class_2960 UNKNOWN_DIM_ID = class_2960.method_60655("xaerominimap", "unknown");
   private final HudMod modMain;
   protected final MinimapSession session;
   protected final Map<String, MinimapWorldContainer> subContainers;
   protected final Map<String, MinimapWorld> worlds;
   private final Map<String, String> worldNames;
   private final MinimapWorldRootContainer rootContainer;
   /** @deprecated */
   @Deprecated
   private final ServerWaypointManager serverWaypointManager;
   private final class_5321<class_1937> equivalentDimId;
   private final ThirdPartyWaypointManager thirdPartyWaypointManager;
   protected XaeroPath path;

   protected MinimapWorldContainer(HudMod modMain, MinimapSession session, Map<String, MinimapWorldContainer> subContainers, Map<String, MinimapWorld> worlds, Map<String, String> worldNames, ServerWaypointManager serverWaypointManager, XaeroPath path, MinimapWorldRootContainer rootContainer, class_5321<class_1937> equivalentDimId, ThirdPartyWaypointManager thirdPartyWaypointManager) {
      this.subContainers = subContainers;
      this.worlds = worlds;
      this.worldNames = worldNames;
      this.serverWaypointManager = serverWaypointManager;
      if (path.getLastNode().contains(":")) {
         throw new IllegalArgumentException();
      } else {
         this.modMain = modMain;
         this.session = session;
         this.path = path;
         this.rootContainer = rootContainer;
         this.equivalentDimId = equivalentDimId;
         this.thirdPartyWaypointManager = thirdPartyWaypointManager;
      }
   }

   public class_5321<class_1937> getEquivalentDimKey() {
      return this.equivalentDimId;
   }

   public class_2960 getEquivalentDimId() {
      return this.equivalentDimId == null ? UNKNOWN_DIM_ID : this.equivalentDimId.method_29177();
   }

   public void setPath(XaeroPath path) {
      if (path.getLastNode().contains(":")) {
         throw new IllegalArgumentException();
      } else {
         this.path = path;

         for(MinimapWorldContainer s : this.subContainers.values()) {
            s.setPath(path.resolve(s.getLastNode()));
         }

      }
   }

   public MinimapWorldContainer addSubContainer(XaeroPath containerPath) {
      if (containerPath.getNodeCount() <= this.path.getNodeCount()) {
         throw new IllegalArgumentException();
      } else {
         String nextNode = containerPath.getAtIndex(this.path.getNodeCount()).getLastNode();
         MinimapWorldContainer sub = (MinimapWorldContainer)this.subContainers.get(nextNode);
         if (sub == null) {
            this.subContainers.put(nextNode, sub = ((FinalBuilder)((FinalBuilder)((FinalBuilder)((FinalBuilder)MinimapWorldContainer.FinalBuilder.begin().setModMain(this.modMain)).setSession(this.session)).setPath(this.path.resolve(nextNode))).setRootContainer(this.getRoot())).build());
         }

         return containerPath.getNodeCount() > this.path.getNodeCount() + 1 ? sub.addSubContainer(containerPath) : sub;
      }
   }

   public boolean containsSubContainer(XaeroPath containerPath) {
      if (containerPath.getNodeCount() <= this.path.getNodeCount()) {
         throw new IllegalArgumentException();
      } else {
         String nextNode = containerPath.getAtIndex(this.path.getNodeCount()).getLastNode();
         MinimapWorldContainer sub = (MinimapWorldContainer)this.subContainers.get(nextNode);
         if (sub == null) {
            return false;
         } else {
            return containerPath.getNodeCount() == this.path.getNodeCount() + 1 ? true : sub.containsSubContainer(containerPath);
         }
      }
   }

   public boolean deleteSubContainer(XaeroPath containerPath) {
      if (containerPath.getNodeCount() <= this.path.getNodeCount()) {
         throw new IllegalArgumentException();
      } else if (containerPath.getNodeCount() == this.path.getNodeCount() + 1) {
         return this.subContainers.remove(containerPath.getLastNode()) != null;
      } else {
         MinimapWorldContainer sub = (MinimapWorldContainer)this.subContainers.get(containerPath.getAtIndex(this.path.getNodeCount()).getLastNode());
         return sub == null ? false : sub.deleteSubContainer(containerPath);
      }
   }

   public boolean isEmpty() {
      return this.subContainers.isEmpty() && this.worlds.isEmpty();
   }

   public MinimapWorld addWorld(String worldNode) {
      MinimapWorld world = (MinimapWorld)this.worlds.get(worldNode);
      if (world != null) {
         return world;
      } else {
         MinimapWorld defaultWorld = (MinimapWorld)this.worlds.get("waypoints");
         if (defaultWorld == null) {
            world = MinimapWorld.Builder.begin().setContainer(this).setNode(worldNode).setDimId(this.equivalentDimId).build();
            this.worlds.put(worldNode, world);
            return world;
         } else {
            this.worlds.put(worldNode, defaultWorld);

            try {
               Path defaultFile = this.session.getWorldManagerIO().getWorldFile(defaultWorld);
               defaultWorld.setNode(worldNode);
               Path fixedFile = this.session.getWorldManagerIO().getWorldFile(defaultWorld);
               if (Files.exists(defaultFile, new LinkOption[0])) {
                  Files.move(defaultFile, fixedFile);
               }
            } catch (IOException e) {
               MinimapLogs.LOGGER.error("suppressed exception", e);
            }

            this.worlds.remove("waypoints");
            return defaultWorld;
         }
      }
   }

   public void addWorld(MinimapWorld world) {
      if (this.worlds.containsKey(world.getNode())) {
         throw new IllegalArgumentException();
      } else {
         this.worlds.put(world.getNode(), world);
      }
   }

   public void removeWorld(String worldNode) {
      this.worlds.remove(worldNode);
   }

   public void setName(String worldNode, String name) {
      String current = (String)this.worldNames.get(worldNode);
      if (current != null && !current.equals(name)) {
         ((MinimapWorld)this.worlds.get(worldNode)).requestRemovalOnSave(current);
      }

      this.worldNames.put(worldNode, name);
   }

   public String getName(String worldNode) {
      if (worldNode.equals("waypoints")) {
         return null;
      } else {
         String name = (String)this.worldNames.get(worldNode);
         if (name != null) {
            return name;
         } else {
            int numericName = this.worldNames.size() + 1;

            do {
               int var10000 = numericName++;
               name = "" + var10000;
            } while(this.worldNames.containsValue(name));

            this.setName(worldNode, name);
            return name;
         }
      }
   }

   public void removeName(String worldNode) {
      this.worldNames.remove(worldNode);
   }

   public String getLastNode() {
      return this.path.getLastNode();
   }

   public String getSubName() {
      String subName = this.getLastNode();
      if (!subName.startsWith("dim%")) {
         return subName;
      } else {
         class_5321<class_1937> dimensionKey = this.equivalentDimId;
         if (dimensionKey == null) {
            return "Dim. " + subName.substring(4);
         } else {
            return dimensionKey.method_29177().method_12836().equals("minecraft") ? dimensionKey.method_29177().method_12832() : dimensionKey.method_29177().toString();
         }
      }
   }

   public String getFullWorldName(String worldNode, String containerName) {
      if (this.worlds.size() < 2 && !containerName.isEmpty()) {
         return containerName;
      } else {
         String worldName = this.getName(worldNode);
         if (this.equivalentDimId != null && this.modMain.getSupportMods().worldmap() && this.getRoot().getPath().equals(this.session.getWorldState().getAutoRootContainerPath())) {
            String worldMapMWName = this.modMain.getSupportMods().worldmapSupport.tryToGetMultiworldName(this.equivalentDimId, worldNode);
            if (worldMapMWName != null && !worldMapMWName.equals(worldNode)) {
               worldName = worldMapMWName;
            }
         }

         if (worldName == null) {
            return containerName;
         } else {
            return !containerName.isEmpty() ? worldName + " - " + containerName : worldName;
         }
      }
   }

   public XaeroPath getPath() {
      return this.path;
   }

   public MinimapWorld getFirstWorld() {
      if (!this.worlds.isEmpty()) {
         return (MinimapWorld)this.worlds.values().stream().findFirst().orElse((Object)null);
      } else {
         for(MinimapWorldContainer sub : this.subContainers.values()) {
            MinimapWorld subFirst = sub.getFirstWorld();
            if (subFirst != null) {
               return subFirst;
            }
         }

         return null;
      }
   }

   public MinimapWorld getFirstWorldConnectedTo(MinimapWorld refWorld) {
      if (!this.worlds.isEmpty()) {
         MinimapWorldRootContainer rootContainer = this.getRoot();

         for(MinimapWorld world : this.worlds.values()) {
            if (rootContainer.getSubWorldConnections().isConnected(refWorld, world)) {
               return world;
            }
         }
      }

      for(MinimapWorldContainer sub : this.subContainers.values()) {
         MinimapWorld subFirst = sub.getFirstWorldConnectedTo(refWorld);
         if (subFirst != null) {
            return subFirst;
         }
      }

      return null;
   }

   public String toString() {
      String var10000 = String.valueOf(this.path);
      return var10000 + " sc:" + this.subContainers.size() + " w:" + this.worlds.size();
   }

   public Iterable<MinimapWorld> getWorlds() {
      return this.worlds.values();
   }

   public List<MinimapWorld> getWorldsCopy() {
      return new ArrayList(this.worlds.values());
   }

   public Iterable<MinimapWorldContainer> getSubContainers() {
      return this.subContainers.values();
   }

   public Iterable<MinimapWorld> getAllWorldsIterable() {
      Iterable<MinimapWorld> allWorlds = this.worlds.values();

      for(MinimapWorldContainer sub : this.subContainers.values()) {
         allWorlds = Iterables.concat(allWorlds, sub.getAllWorldsIterable());
      }

      return allWorlds;
   }

   public XaeroPath fixPathCharacterCases(XaeroPath containerPath) {
      if (containerPath.equals(this.path)) {
         return this.path;
      } else if (!containerPath.isSubOf(this.path)) {
         return null;
      } else {
         for(Map.Entry<String, MinimapWorldContainer> entry : this.subContainers.entrySet()) {
            XaeroPath subSearch = ((MinimapWorldContainer)entry.getValue()).fixPathCharacterCases(containerPath);
            if (subSearch != null) {
               return subSearch;
            }
         }

         XaeroPath fixedContainerPath = this.path;

         for(int i = this.path.getNodeCount(); i < containerPath.getNodeCount(); ++i) {
            fixedContainerPath = fixedContainerPath.resolve(containerPath.getAtIndex(i).getLastNode());
         }

         return fixedContainerPath;
      }
   }

   public MinimapWorldRootContainer getRoot() {
      return this.rootContainer;
   }

   public RootConfig getRootConfig() {
      return this.getRoot().getConfig();
   }

   public Path getDirectoryPath() {
      Path worldFolder = this.modMain.getMinimapFolder();
      return this.path.applyToFilePath(worldFolder);
   }

   public MinimapSession getSession() {
      return this.session;
   }

   /** @deprecated */
   @Deprecated
   public ServerWaypointManager getServerWaypointManager() {
      return this.serverWaypointManager;
   }

   public ThirdPartyWaypointManager getThirdPartyWaypointManager() {
      return this.thirdPartyWaypointManager;
   }

   public abstract static class Builder<B extends Builder> {
      protected final B self = (B)this;
      protected HudMod modMain;
      protected MinimapSession session;
      protected XaeroPath path;
      protected MinimapWorldRootContainer rootContainer;

      protected Builder() {
      }

      public B setDefault() {
         this.setModMain((HudMod)null);
         this.setSession((MinimapSession)null);
         this.setPath((XaeroPath)null);
         this.setRootContainer((MinimapWorldRootContainer)null);
         return this.self;
      }

      public B setModMain(HudMod modMain) {
         this.modMain = modMain;
         return this.self;
      }

      public B setSession(MinimapSession session) {
         this.session = session;
         return this.self;
      }

      public B setPath(XaeroPath path) {
         this.path = path;
         return this.self;
      }

      public B setRootContainer(MinimapWorldRootContainer rootContainer) {
         this.rootContainer = rootContainer;
         return this.self;
      }

      private class_5321<class_1937> determineDimensionEquivalent() {
         if (this.path.getNodeCount() < 2) {
            return null;
         } else {
            String secondNode = this.path.getAtIndex(1).getLastNode();
            if (secondNode.startsWith("dim%")) {
               class_5321<class_1937> dimKey = this.session.getDimensionHelper().getDimensionKeyForDirectoryName(secondNode);
               if (dimKey != null) {
                  return dimKey;
               }
            }

            return null;
         }
      }

      public MinimapWorldContainer build() {
         if (this.modMain != null && this.session != null && this.path != null) {
            class_5321<class_1937> equivalentDimId = this.determineDimensionEquivalent();
            ThirdPartyWaypointManager thirdPartyWaypointManager = this.path.getNodeCount() != 2 ? null : new ThirdPartyWaypointManager(this.session);
            ServerWaypointManager serverWaypointManager = new ServerWaypointManager(thirdPartyWaypointManager);
            return this.buildInternally(new HashMap(), new HashMap(), new HashMap(), serverWaypointManager, equivalentDimId, thirdPartyWaypointManager);
         } else {
            throw new IllegalStateException();
         }
      }

      protected abstract MinimapWorldContainer buildInternally(Map<String, MinimapWorldContainer> var1, Map<String, MinimapWorld> var2, Map<String, String> var3, ServerWaypointManager var4, class_5321<class_1937> var5, ThirdPartyWaypointManager var6);
   }

   public static final class FinalBuilder extends Builder<FinalBuilder> {
      private FinalBuilder() {
      }

      public MinimapWorldContainer build() {
         if (this.rootContainer == null) {
            throw new IllegalStateException();
         } else {
            return super.build();
         }
      }

      protected MinimapWorldContainer buildInternally(Map<String, MinimapWorldContainer> subContainers, Map<String, MinimapWorld> worlds, Map<String, String> worldNames, ServerWaypointManager serverWaypointManager, class_5321<class_1937> equivalentDimId, ThirdPartyWaypointManager thirdPartyWaypointManager) {
         return new MinimapWorldContainer(this.modMain, this.session, subContainers, worlds, worldNames, serverWaypointManager, this.path, this.rootContainer, equivalentDimId, thirdPartyWaypointManager);
      }

      public static FinalBuilder begin() {
         return (FinalBuilder)(new FinalBuilder()).setDefault();
      }
   }
}
