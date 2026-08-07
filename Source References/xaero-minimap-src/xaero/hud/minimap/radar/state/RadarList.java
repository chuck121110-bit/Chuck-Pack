package xaero.hud.minimap.radar.state;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1297;
import xaero.hud.category.setting.ObjectCategorySetting;
import xaero.hud.minimap.radar.category.EntityRadarCategory;
import xaero.hud.minimap.radar.category.setting.EntityRadarCategorySettings;

public final class RadarList implements Comparable<RadarList> {
   private EntityRadarCategory clientCategory;
   private EntityRadarCategory syncedCategory;
   private final List<class_1297> entities;

   private RadarList(List<class_1297> entities) {
      this.entities = entities;
   }

   public EntityRadarCategory getClientCategory() {
      return this.clientCategory;
   }

   public EntityRadarCategory getSyncedCategory() {
      return this.syncedCategory;
   }

   public RadarList setClientCategory(EntityRadarCategory clientCategory) {
      this.clientCategory = clientCategory;
      return this;
   }

   public RadarList setSyncedCategory(EntityRadarCategory syncedCategory) {
      this.syncedCategory = syncedCategory;
      return this;
   }

   public <T> T getEffective(ObjectCategorySetting<T> setting) {
      T syncedValue = (T)(this.syncedCategory == null ? null : this.syncedCategory.getSettingValue(setting));
      return (T)(syncedValue != null ? syncedValue : this.clientCategory.getSettingValue(setting));
   }

   public void clearEntities() {
      this.entities.clear();
   }

   public boolean add(class_1297 entity) {
      return this.entities.add(entity);
   }

   public class_1297 get(int index) {
      return (class_1297)this.entities.get(index);
   }

   public int size() {
      return this.entities.size();
   }

   public Iterable<class_1297> getEntities() {
      return this.entities;
   }

   public int compareTo(RadarList o) {
      return ((Double)this.clientCategory.getSettingValue(EntityRadarCategorySettings.RENDER_ORDER)).compareTo((Double)o.clientCategory.getSettingValue(EntityRadarCategorySettings.RENDER_ORDER));
   }

   public static final class Builder {
      private Builder() {
      }

      public Builder setDefault() {
         return this;
      }

      public RadarList build() {
         return new RadarList(new ArrayList());
      }

      public static Builder getDefault() {
         return (new Builder()).setDefault();
      }
   }
}
