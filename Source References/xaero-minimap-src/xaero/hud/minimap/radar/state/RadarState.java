package xaero.hud.minimap.radar.state;

import java.util.ArrayList;
import java.util.List;
import xaero.hud.minimap.radar.category.EntityRadarCategory;

public class RadarState {
   private EntityRadarCategory listsGeneratedForConfig;
   private EntityRadarCategory listsGeneratedForSyncedConfig;
   private boolean listsReversedOrder;
   private final List<RadarList> radarLists = new ArrayList();

   public boolean getListsReversedOrder() {
      return this.listsReversedOrder;
   }

   public Iterable<RadarList> getRadarLists() {
      return this.radarLists;
   }

   public EntityRadarCategory getListsGeneratedForConfig() {
      return this.listsGeneratedForConfig;
   }

   public EntityRadarCategory getListsGeneratedForSyncedConfig() {
      return this.listsGeneratedForSyncedConfig;
   }

   List<RadarList> getUpdatableLists() {
      return this.radarLists;
   }

   void setListsReversedOrder(boolean listsReversedOrder) {
      this.listsReversedOrder = listsReversedOrder;
   }

   void setListsGeneratedForConfig(EntityRadarCategory listsGeneratedForConfig) {
      this.listsGeneratedForConfig = listsGeneratedForConfig;
   }

   public void setListsGeneratedForSyncedConfig(EntityRadarCategory listsGeneratedForSyncedConfig) {
      this.listsGeneratedForSyncedConfig = listsGeneratedForSyncedConfig;
   }
}
