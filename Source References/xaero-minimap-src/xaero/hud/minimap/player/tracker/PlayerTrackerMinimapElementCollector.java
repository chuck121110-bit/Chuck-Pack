package xaero.hud.minimap.player.tracker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import xaero.hud.minimap.player.tracker.system.IRenderedPlayerTracker;
import xaero.hud.minimap.player.tracker.system.ITrackedPlayerReader;
import xaero.hud.minimap.player.tracker.system.RenderedPlayerTrackerManager;

public class PlayerTrackerMinimapElementCollector {
   private Map<UUID, PlayerTrackerMinimapElement<?>> elements = new HashMap();
   private final RenderedPlayerTrackerManager systemManager;

   public PlayerTrackerMinimapElementCollector(RenderedPlayerTrackerManager systemManager) {
      this.systemManager = systemManager;
   }

   public void update(class_310 mc) {
      if (this.elements == null) {
         this.elements = new HashMap();
      }

      Map<UUID, PlayerTrackerMinimapElement<?>> updatedMap = new HashMap();
      boolean hasNewPlayer = false;

      for(IRenderedPlayerTracker<?> system : this.systemManager.getAllSystems()) {
         hasNewPlayer = this.updateForSystem(system, updatedMap, this.elements) || hasNewPlayer;
      }

      if (hasNewPlayer || updatedMap.size() != this.elements.size()) {
         this.elements = updatedMap;
      }

   }

   private <P> boolean updateForSystem(IRenderedPlayerTracker<P> system, Map<UUID, PlayerTrackerMinimapElement<?>> destination, Map<UUID, PlayerTrackerMinimapElement<?>> current) {
      Iterator<P> playerIterator = system.getTrackedPlayerIterator();
      if (playerIterator == null) {
         return false;
      } else {
         ITrackedPlayerReader<P> reader = system.getReader();
         boolean hasNewPlayer = false;

         while(playerIterator.hasNext()) {
            P player = (P)playerIterator.next();
            UUID playerId = reader.getId(player);
            PlayerTrackerMinimapElement<?> element = (PlayerTrackerMinimapElement)current.get(playerId);
            if (!destination.containsKey(playerId)) {
               if (element == null || element.getSystem() != system || element.getPlayer() != player) {
                  element = new PlayerTrackerMinimapElement(player, system);
                  hasNewPlayer = true;
               }

               destination.put(element.getPlayerId(), element);
            }
         }

         return hasNewPlayer;
      }
   }

   public boolean playerExists(UUID id) {
      return this.elements != null && this.elements.containsKey(id);
   }

   public Iterable<PlayerTrackerMinimapElement<?>> getElements() {
      return this.elements.values();
   }

   public void resetRenderedOnRadarFlags() {
      for(PlayerTrackerMinimapElement<?> e : this.elements.values()) {
         e.setRenderedOnRadar(false);
      }

   }

   public void confirmPlayerRadarRender(class_1657 p) {
      ((PlayerTrackerMinimapElement)this.elements.get(p.method_5667())).setRenderedOnRadar(true);
   }
}
