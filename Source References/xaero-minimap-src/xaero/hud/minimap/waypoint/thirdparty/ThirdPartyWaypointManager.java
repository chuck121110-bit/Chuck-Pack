package xaero.hud.minimap.waypoint.thirdparty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_2960;
import xaero.hud.minimap.module.MinimapSession;

public class ThirdPartyWaypointManager {
   private final MinimapSession session;
   private final Map<class_2960, ThirdPartyWaypoints> storage;
   private final List<ThirdPartyWaypoints> orderedStorage;
   private final List<ThirdPartyWaypoints> orderedStorageUnmodifiable;

   public ThirdPartyWaypointManager(MinimapSession session) {
      this.session = session;
      this.storage = new HashMap();
      this.orderedStorage = new ArrayList();
      this.orderedStorageUnmodifiable = Collections.unmodifiableList(this.orderedStorage);
   }

   public void clear() {
      this.storage.values().forEach(ThirdPartyWaypoints::clear);
   }

   public void clearOrigin(class_2960 originId) {
      ThirdPartyWaypoints waypoints = (ThirdPartyWaypoints)this.storage.get(originId);
      if (waypoints != null) {
         waypoints.clear();
      }
   }

   public int getOriginCount() {
      return this.storage.size();
   }

   public ThirdPartyWaypoints get(class_2960 originId) {
      return (ThirdPartyWaypoints)this.storage.computeIfAbsent(originId, (id) -> {
         ThirdPartyWaypoints created = new ThirdPartyWaypoints(this.session, id);
         this.orderedStorage.add(created);
         return created;
      });
   }

   public ThirdPartyWaypoints getByIndex(int originIndex) {
      return (ThirdPartyWaypoints)this.orderedStorage.get(originIndex);
   }

   public int getCount() {
      return (Integer)this.orderedStorage.stream().map(ThirdPartyWaypoints::getCount).reduce(Integer::sum).orElse(0);
   }

   public List<ThirdPartyWaypoints> getAll() {
      return this.orderedStorageUnmodifiable;
   }
}
