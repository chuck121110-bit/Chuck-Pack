package xaero.map.world;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_2960;
import net.minecraft.class_5321;
import net.minecraft.class_7924;

public class MapConnectionManager {
   private Map<MapConnectionNode, Set<MapConnectionNode>> allConnections = new HashMap();

   public void addConnection(MapConnectionNode mapKey1, MapConnectionNode mapKey2) {
      this.addOneWayConnection(mapKey1, mapKey2);
      this.addOneWayConnection(mapKey2, mapKey1);
   }

   private void addOneWayConnection(MapConnectionNode mapKey1, MapConnectionNode mapKey2) {
      Set<MapConnectionNode> connections = (Set)this.allConnections.get(mapKey1);
      if (connections == null) {
         this.allConnections.put(mapKey1, connections = new HashSet());
      }

      connections.add(mapKey2);
   }

   public void removeConnection(MapConnectionNode mapKey1, MapConnectionNode mapKey2) {
      this.removeOneWayConnection(mapKey1, mapKey2);
      this.removeOneWayConnection(mapKey2, mapKey1);
   }

   private void removeOneWayConnection(MapConnectionNode mapKey1, MapConnectionNode mapKey2) {
      Set<MapConnectionNode> connections = (Set)this.allConnections.get(mapKey1);
      if (connections != null) {
         connections.remove(mapKey2);
      }
   }

   public boolean isConnected(MapConnectionNode mapKey1, MapConnectionNode mapKey2) {
      if (mapKey1 != null && mapKey2 != null) {
         if (mapKey1.equals(mapKey2)) {
            return true;
         } else {
            Set<MapConnectionNode> connections = (Set)this.allConnections.get(mapKey1);
            return connections == null ? false : connections.contains(mapKey2);
         }
      } else {
         return false;
      }
   }

   public boolean isEmpty() {
      return this.allConnections.isEmpty();
   }

   public void save(PrintWriter writer) {
      if (!this.allConnections.isEmpty()) {
         Set<String> redundantConnections = new HashSet();

         for(Map.Entry<MapConnectionNode, Set<MapConnectionNode>> entry : this.allConnections.entrySet()) {
            MapConnectionNode mapKey = (MapConnectionNode)entry.getKey();

            for(MapConnectionNode c : (Set)entry.getValue()) {
               String var10000 = String.valueOf(mapKey);
               String fullConnection = var10000 + ":" + String.valueOf(c);
               if (!redundantConnections.contains(fullConnection)) {
                  writer.println("connection:" + fullConnection);
                  String var10001 = String.valueOf(c);
                  redundantConnections.add(var10001 + ":" + String.valueOf(mapKey));
               }
            }
         }
      }

   }

   private void swapConnections(MapConnectionNode mapKey1, MapConnectionNode mapKey2) {
      Set<MapConnectionNode> connections1 = new HashSet((Collection)this.allConnections.getOrDefault(mapKey1, new HashSet()));
      Set<MapConnectionNode> connections2 = new HashSet((Collection)this.allConnections.getOrDefault(mapKey2, new HashSet()));

      for(MapConnectionNode c : connections1) {
         this.removeConnection(mapKey1, c);
      }

      for(MapConnectionNode c : connections2) {
         this.addConnection(mapKey1, c);
      }

      for(MapConnectionNode c : connections2) {
         this.removeConnection(mapKey2, c);
      }

      for(MapConnectionNode c : connections1) {
         this.addConnection(mapKey2, c);
      }

   }

   public void renameDimension(String oldName, String newName) {
      for(MapConnectionNode mapKey : new HashSet(this.allConnections.keySet())) {
         if (mapKey.getDimId().method_29177().toString().equals(oldName)) {
            String mwPart = mapKey.getMw();
            this.swapConnections(mapKey, new MapConnectionNode(class_5321.method_29179(class_7924.field_41223, class_2960.method_60654(newName)), mwPart));
         }
      }

   }
}
