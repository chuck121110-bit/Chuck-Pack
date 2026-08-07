package xaeroplus.module.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_1297;
import net.minecraft.class_1684;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_5321;
import net.minecraft.class_634;
import net.minecraft.class_638;
import net.minecraft.class_7924;
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
import xaeroplus.event.XaeroWorldChangeEvent;
import xaeroplus.feature.extensions.SyncedWaypoint;
import xaeroplus.feature.waypoint.WaypointAPI;
import xaeroplus.module.Module;
import xaeroplus.util.FileUtil;
import xaeroplus.util.timer.Timer;
import xaeroplus.util.timer.Timers;

public class Pearls extends Module {
   private final Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
   private final Map<UUID, Map<UUID, Pearl>> pearlsMap = new ConcurrentHashMap();
   private boolean stale = false;
   public static final String PEARL_WP_SET_ID = "xaeroplus.gui.pearl_waypoints_set";
   private final Timer updateTracksPearlsTimer = Timers.tickTimer();
   private final Timer trackedPearlRemovalTimer = Timers.tickTimer();

   public void onEnable() {
      this.pearlsMap.clear();
      this.loadPearls();
   }

   public void onDisable() {
      this.savePearls();
      this.deleteAllPearlWaypointSets();
      this.pearlsMap.clear();
   }

   @EventHandler
   public void updateTrackedPearls(ClientTickEvent.Post event) {
      if (this.updateTracksPearlsTimer.tick(10L)) {
         class_638 level = this.mc.field_1687;
         if (level != null) {
            class_634 con = this.mc.method_1562();
            if (con != null) {
               UUID selfUuid = con.method_2879().id();
               if (selfUuid != null) {
                  boolean updated = false;

                  for(class_1297 entity : level.method_18112()) {
                     if (entity instanceof class_1684) {
                        class_1684 thrownPearlEntity = (class_1684)entity;
                        UUID pearlUUID = thrownPearlEntity.method_5667();
                        class_1297 pearlOwner = thrownPearlEntity.method_24921();
                        if (pearlOwner != null && pearlOwner.method_5667().equals(selfUuid) && thrownPearlEntity.field_6012 >= 20 && !(Math.abs(thrownPearlEntity.method_18798().method_10216()) > 0.01) && !(Math.abs(thrownPearlEntity.method_18798().method_10214()) > (double)1.0F) && !(Math.abs(thrownPearlEntity.method_18798().method_10215()) > 0.01)) {
                           Map<UUID, Pearl> pearls = (Map)this.pearlsMap.computeIfAbsent(pearlOwner.method_5667(), (e) -> new ConcurrentHashMap());
                           Pearl existingPearl = (Pearl)pearls.get(pearlUUID);
                           if (existingPearl == null || thrownPearlEntity.method_5649((double)existingPearl.x(), (double)existingPearl.y(), (double)existingPearl.z()) > (double)1.0F) {
                              pearls.put(pearlUUID, new Pearl(pearlUUID, level.method_27983().method_29177().toString(), thrownPearlEntity.method_24515().method_10263(), thrownPearlEntity.method_24515().method_10264(), thrownPearlEntity.method_24515().method_10260()));
                              updated = true;
                           }
                        }
                     }
                  }

                  if (updated) {
                     this.savePearlsAsync();
                  }

               }
            }
         }
      }
   }

   @EventHandler
   public void trackedPearlRemoval(ClientTickEvent.Post event) {
      if (this.trackedPearlRemovalTimer.tick(11L)) {
         class_638 level = this.mc.field_1687;
         if (level != null) {
            class_634 con = this.mc.method_1562();
            if (con != null) {
               UUID selfUuid = con.method_2879().id();
               if (selfUuid != null) {
                  class_1297 player = this.mc.method_1560();
                  if (player != null) {
                     int effectiveRenderDistance = this.mc.field_1690.method_38521();
                     int trackedEntityRadius = Math.min(3, effectiveRenderDistance + 1) * 16;
                     int trackedEntityRadiusSq = trackedEntityRadius * trackedEntityRadius;
                     Map<UUID, Pearl> savedPearls = (Map)this.pearlsMap.getOrDefault(selfUuid, Collections.emptyMap());
                     boolean updated = false;
                     Iterator<Pearl> it = savedPearls.values().iterator();

                     while(it.hasNext()) {
                        Pearl savedPearl = (Pearl)it.next();
                        if (savedPearl.dimension() == level.method_27983() && player.method_5649((double)savedPearl.x(), player.method_23318(), (double)savedPearl.z()) <= (double)trackedEntityRadiusSq && level.field_27734.method_31866().method_31808(savedPearl.uuid) == null) {
                           it.remove();
                           updated = true;
                        }
                     }

                     if (updated) {
                        this.savePearlsAsync();
                     }

                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onXaeroWorldChange(XaeroWorldChangeEvent event) {
      switch (event.worldChangeType()) {
         case EXIT_WORLD:
            this.savePearls();
            this.pearlsMap.clear();
            break;
         case ENTER_WORLD:
            this.pearlsMap.clear();
            this.loadPearls();
      }

   }

   @EventHandler
   public void syncPearlsToWaypoints(ClientTickEvent.Post event) {
      if (this.stale) {
         MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
         if (minimapSession != null) {
            MinimapWorld currentWorld = minimapSession.getWorldManager().getCurrentWorld();
            if (currentWorld != null) {
               class_634 con = this.mc.method_1562();
               if (con != null) {
                  UUID selfUuid = con.method_2879().id();
                  if (selfUuid != null) {
                     this.clearAllPearlWaypoints();
                     Map<UUID, Pearl> pearls = (Map)this.pearlsMap.get(selfUuid);
                     if (pearls != null) {
                        for(Pearl pearl : pearls.values()) {
                           class_5321<class_1937> pearlDim = pearl.dimension();
                           MinimapWorld minimapWorld = WaypointAPI.getMinimapWorld(pearlDim);
                           if (minimapWorld == null) {
                              return;
                           }

                           WaypointSet wpSet = WaypointAPI.getOrCreateWaypointSetInWorld(minimapWorld, "xaeroplus.gui.pearl_waypoints_set");
                           SyncedWaypoint wp = SyncedWaypoint.create(pearl.x(), pearl.y(), pearl.z(), "Pearl", "P", this.getPearlWaypointColor(pearl));
                           wpSet.add(wp);
                        }

                        this.stale = false;
                     }
                  }
               }
            }
         }
      }
   }

   private WaypointColor getPearlWaypointColor(Pearl pearl) {
      int index = Math.abs(pearl.uuid().hashCode() % WaypointColor.values().length);
      return WaypointColor.fromIndex(index);
   }

   private void clearAllPearlWaypoints() {
      WaypointAPI.forEachWaypointSetInAllMinimapWorlds((wpSet) -> {
         if ("xaeroplus.gui.pearl_waypoints_set".equals(wpSet.getName())) {
            wpSet.clear();
         }

      });
   }

   private void deleteAllPearlWaypointSets() {
      WaypointAPI.forEachMinimapWorld((world) -> {
         String currentWpSet = world.getCurrentWaypointSetId();
         if (currentWpSet != null && currentWpSet.equals("xaeroplus.gui.pearl_waypoints_set")) {
            world.setCurrentWaypointSetId("gui.xaero_default");
         }

         world.removeWaypointSet("xaeroplus.gui.pearl_waypoints_set");
      });
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
               return WorldMap.saveFolder == null ? null : WorldMap.saveFolder.toPath().resolve(worldId).resolve("xaeroplus-pearls.json").toFile();
            }
         }
      }
   }

   public synchronized void loadPearls() {
      try {
         File saveFile = this.getSaveFile();
         if (saveFile == null) {
            return;
         }

         if (!saveFile.exists()) {
            return;
         }

         BufferedReader reader = Files.newBufferedReader(saveFile.toPath());

         try {
            Map<UUID, Map<UUID, Pearl>> map = (Map)this.gson.fromJson(reader, (new TypeToken<Map<UUID, Map<UUID, Pearl>>>() {
            }).getType());
            if (map != null) {
               this.pearlsMap.clear();
               this.pearlsMap.putAll(map);
            }

            this.stale = true;
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
         XaeroPlus.LOGGER.error("[Pearls] Failed to read pearls file", e);
      }

   }

   public void savePearlsAsync() {
      this.stale = true;
      ForkJoinPool.commonPool().execute(this::savePearls);
   }

   public synchronized void savePearls() {
      try {
         File saveFile = this.getSaveFile();
         if (saveFile == null) {
            return;
         }

         FileUtil.safeSave(saveFile, (writer) -> this.gson.toJson(this.pearlsMap, (new TypeToken<Map<UUID, Map<UUID, Pearl>>>() {
            }).getType(), writer));
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("[Pearls] Failed to write pearls file", e);
      }

   }

   public Map<UUID, Map<UUID, Pearl>> getLoadedPearls() {
      return this.pearlsMap;
   }

   public static record Pearl(UUID uuid, String dimensionKey, int x, int y, int z) {
      public class_2960 dimensionLocation() {
         return class_2960.method_12829(this.dimensionKey);
      }

      public class_5321<class_1937> dimension() {
         class_2960 dimensionLocation = this.dimensionLocation();
         if (dimensionLocation == null) {
            return null;
         } else {
            class_2960 ow = class_1937.field_25179.method_29177();
            class_2960 nether = class_1937.field_25180.method_29177();
            class_2960 end = class_1937.field_25181.method_29177();
            if (dimensionLocation.equals(ow)) {
               return class_1937.field_25179;
            } else if (dimensionLocation.equals(nether)) {
               return class_1937.field_25180;
            } else if (dimensionLocation.equals(end)) {
               return class_1937.field_25181;
            } else {
               class_638 level = class_310.method_1551().field_1687;
               return level != null && level.method_27983().method_29177().equals(dimensionLocation) ? level.method_27983() : class_5321.method_29179(class_7924.field_41223, dimensionLocation);
            }
         }
      }
   }
}
