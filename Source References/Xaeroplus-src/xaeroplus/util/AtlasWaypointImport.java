package xaeroplus.util;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import org.jetbrains.annotations.Nullable;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.map.mods.SupportMods;
import xaeroplus.XaeroPlus;
import xaeroplus.feature.waypoint.WaypointAPI;
import xaeroplus.module.impl.TickTaskExecutor;

public final class AtlasWaypointImport {
   private AtlasWaypointImport() {
   }

   public static CompletableFuture<Integer> importAtlasWaypoints() {
      return CompletableFuture.supplyAsync(AtlasWaypointImport::getAtlasApiResponse, ForkJoinPool.commonPool()).thenApplyAsync((atlasWaypoints) -> {
         int addedWaypoints = importAtlasWaypoints(atlasWaypoints);
         XaeroPlus.LOGGER.info("Imported {} Atlas waypoints", addedWaypoints);
         return addedWaypoints;
      }, TickTaskExecutor.INSTANCE);
   }

   private static int importAtlasWaypoints(List<AtlasWaypoint> atlasWaypoints) {
      MinimapSession minimapSession = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
      if (minimapSession == null) {
         return 0;
      } else {
         MinimapWorld currentWorld = minimapSession.getWorldManager().getCurrentWorld();
         if (currentWorld == null) {
            return 0;
         } else {
            WaypointSet currentWpSet = currentWorld.getCurrentWaypointSet();
            if (currentWpSet == null) {
               return 0;
            } else if (atlasWaypoints.isEmpty()) {
               return 0;
            } else {
               Map<class_5321<class_1937>, ArrayList<AtlasWaypoint>> atlasByDimension = (Map)atlasWaypoints.stream().filter(AtlasWaypoint::isValid).collect(Collectors.toMap((k) -> Objects.equals(0, k.end_dimension) ? class_1937.field_25179 : class_1937.field_25181, (v) -> {
                  ArrayList<AtlasWaypoint> l = new ArrayList();
                  l.add(v);
                  return l;
               }, (v1, v2) -> {
                  v1.addAll(v2);
                  return v1;
               }));
               MinimapWorld owMinimapWorld = WaypointAPI.getMinimapWorld(class_1937.field_25179);
               MinimapWorld endMinimapWorld = WaypointAPI.getMinimapWorld(class_1937.field_25181);
               WaypointSet owAtlasSet = WaypointAPI.getOrCreateWaypointSetInWorld(owMinimapWorld, "atlas");
               WaypointSet endAtlasSet = WaypointAPI.getOrCreateWaypointSetInWorld(endMinimapWorld, "atlas");
               owAtlasSet.clear();
               endAtlasSet.clear();
               int addedWaypoints = 0;

               for(Map.Entry<class_5321<class_1937>, ArrayList<AtlasWaypoint>> atlasWp : atlasByDimension.entrySet()) {
                  class_5321<class_1937> dim = (class_5321)atlasWp.getKey();
                  List<AtlasWaypoint> waypoints = (List)atlasWp.getValue();
                  WaypointSet waypointSet = dim == class_1937.field_25179 ? owAtlasSet : endAtlasSet;

                  for(AtlasWaypoint waypoint : waypoints) {
                     int index = Math.abs(Hashing.murmur3_128().hashUnencodedChars(waypoint.name).asInt()) % WaypointColor.values().length;
                     WaypointColor color = WaypointColor.fromIndex(index);
                     Waypoint wp = new Waypoint(waypoint.x, waypoint.y == null ? 64 : waypoint.y, waypoint.z, waypoint.name, waypoint.name.substring(0, Math.min(2, waypoint.name.length())), color, WaypointPurpose.NORMAL);
                     waypointSet.add(wp);
                     ++addedWaypoints;
                  }
               }

               SupportMods.xaeroMinimap.requestWaypointsRefresh();
               return addedWaypoints;
            }
         }
      }
   }

   private static List<AtlasWaypoint> getAtlasApiResponse() {
      HttpClient client = HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).connectTimeout(Duration.ofSeconds(2L)).build();

      try {
         HttpRequest req = HttpRequest.newBuilder().uri(URI.create("https://2b2tAtlas.com/api/locations.php")).headers(new String[]{"User-Agent", "XaeroPlus/" + XaeroPlus.XP_VERSION + "+" + XaeroPlus.MC_VERSION}).timeout(Duration.ofSeconds(10L)).GET().build();
         HttpResponse<String> response = client.send(req, BodyHandlers.ofString());
         Gson gson = (new GsonBuilder()).disableHtmlEscaping().setLenient().create();
         return (List)gson.fromJson((String)response.body(), new TypeToken<List<AtlasWaypoint>>() {
         });
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Failed to get Atlas API response", e);
         return Collections.emptyList();
      }
   }

   public static class AtlasWaypoint {
      public String name;
      public Integer x;
      public @Nullable Integer y;
      public Integer z;
      public Integer end_dimension;

      public boolean isValid() {
         return this.name != null && this.x != null && this.z != null && this.end_dimension != null;
      }
   }
}
