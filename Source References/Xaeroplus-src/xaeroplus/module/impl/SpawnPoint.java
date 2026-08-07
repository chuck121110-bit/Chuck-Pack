package xaeroplus.module.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_5321;
import net.minecraft.class_634;
import net.minecraft.class_7924;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.map.MapProcessor;
import xaero.map.WorldMap;
import xaero.map.WorldMapSession;
import xaero.map.core.XaeroWorldMapCore;
import xaeroplus.XaeroPlus;
import xaeroplus.event.ClientTickEvent;
import xaeroplus.event.RespawnObstructedEvent;
import xaeroplus.event.RespawnPointSetEvent;
import xaeroplus.event.XaeroWorldChangeEvent;
import xaeroplus.feature.extensions.SyncedWaypoint;
import xaeroplus.feature.waypoint.WaypointAPI;
import xaeroplus.module.Module;
import xaeroplus.settings.Settings;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.FileUtil;

public class SpawnPoint extends Module {
   private final Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
   private final Map<UUID, SpawnPosition> respawnPoints = new ConcurrentHashMap();
   private static final WeakReference nullRef = new WeakReference((Object)null);
   private WeakReference<Waypoint> wpRef;
   private WeakReference<WaypointSet> wpSetRef;
   private SpawnPosition wpSpawnPoint;

   public SpawnPoint() {
      this.wpRef = nullRef;
      this.wpSetRef = nullRef;
      this.wpSpawnPoint = null;
   }

   public void onEnable() {
      if (this.mc.field_1687 != null) {
         this.respawnPoints.clear();
         this.loadRespawnPoints();
      }
   }

   public void onDisable() {
      if (this.mc.field_1687 != null) {
         this.saveRespawnPoints();
         this.respawnPoints.clear();
         this.clearWpAndState();
      }
   }

   @EventHandler
   public void onRespawnPointSet(RespawnPointSetEvent event) {
      class_634 con = this.mc.method_1562();
      if (con != null) {
         UUID activeUUID = con.method_2879().id();
         this.respawnPoints.put(activeUUID, new SpawnPosition(ChunkUtils.getActualDimension().method_29177().toString(), event.pos().method_10263(), event.pos().method_10264(), event.pos().method_10260()));
         this.saveRespawnPointsAsync();
      }
   }

   @EventHandler
   public void onRespawnObstructed(RespawnObstructedEvent event) {
      class_634 con = this.mc.method_1562();
      if (con != null) {
         UUID uuid = con.method_2879().id();
         if (this.respawnPoints.containsKey(uuid)) {
            this.removeSpawnPoint(uuid);
            XaeroPlus.LOGGER.info("[SpawnPoint] Respawn obstructed or bed/anchor missing. Waypoint removed.");
         }

      }
   }

   @EventHandler
   public void onXaeroWorldChange(XaeroWorldChangeEvent event) {
      switch (event.worldChangeType()) {
         case EXIT_WORLD:
            this.saveRespawnPoints();
            this.clearWpAndState();
            this.respawnPoints.clear();
            break;
         case ENTER_WORLD:
            this.clearWpAndState();
            this.respawnPoints.clear();
            this.loadRespawnPoints();
      }

   }

   @EventHandler
   public void onClientTick(ClientTickEvent.Post event) {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      if (minimapSession != null) {
         if (minimapSession.getWorldManager().getCurrentWorld() != null) {
            class_634 con = this.mc.method_1562();
            if (con != null) {
               UUID uuid = con.method_2879().id();
               SpawnPosition spawnPoint = (SpawnPosition)this.respawnPoints.get(uuid);
               if (spawnPoint == null) {
                  this.clearWpAndState();
               } else {
                  class_5321<class_1937> spawnPointDimension = spawnPoint.dimension();
                  if (spawnPointDimension == null) {
                     this.clearWpAndState();
                  } else {
                     if (Settings.REGISTRY.owAutoWaypointDimension.get() && spawnPointDimension == class_1937.field_25180) {
                        spawnPointDimension = class_1937.field_25179;
                        spawnPoint = new SpawnPosition(class_1937.field_25179.method_29177().toString(), spawnPoint.x() * 8, spawnPoint.y(), spawnPoint.z() * 8);
                     }

                     MinimapWorld minimapWorld = WaypointAPI.getMinimapWorld(spawnPointDimension);
                     if (minimapWorld == null) {
                        this.clearWpAndState();
                     } else {
                        WaypointSet waypointSet = WaypointAPI.getOrCreateWaypointSetInWorld(minimapWorld, "gui.xaero_default");
                        if (!this.isWaypointStateValid(spawnPoint)) {
                           this.clearWpAndState();
                           this.wpSetRef = new WeakReference(waypointSet);
                           Waypoint wp = SyncedWaypoint.create(spawnPoint.x(), spawnPoint.y(), spawnPoint.z(), "Spawn Point", "SP", WaypointColor.AQUA);
                           waypointSet.add(wp);
                           this.wpRef = new WeakReference(wp);
                           this.wpSpawnPoint = spawnPoint;
                           XaeroPlus.LOGGER.info("[SpawnPoint] Spawn Point Waypoint Updated: {} {} {}", new Object[]{spawnPoint.x(), spawnPoint.y(), spawnPoint.z()});
                        }

                     }
                  }
               }
            }
         }
      }
   }

   private synchronized void clearWpAndState() {
      WaypointSet set = (WaypointSet)this.wpSetRef.get();
      Waypoint wp = (Waypoint)this.wpRef.get();
      if (set != null && wp != null) {
         set.remove(wp);
      }

      this.wpRef = nullRef;
      this.wpSetRef = nullRef;
      this.wpSpawnPoint = null;
   }

   private void removeSpawnPoint(UUID uuid) {
      this.respawnPoints.remove(uuid);
      this.clearWpAndState();
      this.saveRespawnPointsAsync();
   }

   private boolean isWaypointStateValid(SpawnPosition spawnPoint) {
      Waypoint wp = (Waypoint)this.wpRef.get();
      WaypointSet set = (WaypointSet)this.wpSetRef.get();
      return Objects.equals(this.wpSpawnPoint, spawnPoint) && wp != null && set != null;
   }

   private File getSaveFile() {
      WorldMapSession currentSession = XaeroWorldMapCore.currentSession;
      if (currentSession == null) {
         return null;
      } else {
         MapProcessor mapProcessor = currentSession.getMapProcessor();
         if (mapProcessor == null) {
            return null;
         } else {
            String worldId = mapProcessor.getCurrentWorldId();
            if (worldId == null) {
               return null;
            } else {
               return WorldMap.saveFolder == null ? null : WorldMap.saveFolder.toPath().resolve(worldId).resolve("xaeroplus-respawn-points.json").toFile();
            }
         }
      }
   }

   public synchronized void loadRespawnPoints() {
      try {
         File saveFile = this.getSaveFile();
         if (saveFile == null || !saveFile.exists()) {
            return;
         }

         BufferedReader reader = Files.newBufferedReader(saveFile.toPath());

         try {
            Map<UUID, SpawnPosition> map = (Map)this.gson.fromJson(reader, (new TypeToken<Map<UUID, SpawnPosition>>() {
            }).getType());
            if (map != null) {
               this.respawnPoints.clear();
               this.respawnPoints.putAll(map);
            }
         } catch (Throwable var6) {
            if (reader != null) {
               try {
                  reader.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (reader != null) {
            reader.close();
         }
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("[SpawnPoint] Failed to read respawn points file", e);
      }

   }

   public void saveRespawnPointsAsync() {
      ForkJoinPool.commonPool().execute(this::saveRespawnPoints);
   }

   public synchronized void saveRespawnPoints() {
      try {
         File saveFile = this.getSaveFile();
         if (saveFile == null) {
            return;
         }

         FileUtil.safeSave(saveFile, (writer) -> this.gson.toJson(this.respawnPoints, (new TypeToken<Map<UUID, SpawnPosition>>() {
            }).getType(), writer));
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("[SpawnPoint] Failed to write respawn points file", e);
      }

   }

   public Map<UUID, SpawnPosition> getLoadedSpawnPositions() {
      return this.respawnPoints;
   }

   public static record SpawnPosition(String dimensionKey, int x, int y, int z) {
      private static final Map<String, class_5321<class_1937>> DIMENSION_CACHE = new ConcurrentHashMap();

      public class_5321<class_1937> dimension() {
         return (class_5321)DIMENSION_CACHE.computeIfAbsent(this.dimensionKey, (s) -> this.parseDimension(this.dimensionKey));
      }

      private class_5321<class_1937> parseDimension(String key) {
         if (key == null) {
            return null;
         } else {
            class_2960 location = class_2960.method_12829(key);
            if (location == null) {
               return null;
            } else if (location.equals(class_1937.field_25179.method_29177())) {
               return class_1937.field_25179;
            } else if (location.equals(class_1937.field_25180.method_29177())) {
               return class_1937.field_25180;
            } else {
               return location.equals(class_1937.field_25181.method_29177()) ? class_1937.field_25181 : class_5321.method_29179(class_7924.field_41223, location);
            }
         }
      }
   }
}
