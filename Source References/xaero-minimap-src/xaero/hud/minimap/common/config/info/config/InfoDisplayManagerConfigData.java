package xaero.hud.minimap.common.config.info.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class InfoDisplayManagerConfigData {
   public static final InfoDisplayManagerConfigData EMPTY = new InfoDisplayManagerConfigData(new ArrayList(), new HashMap());
   private final List<String> order;
   private final Map<String, InfoDisplayConfigData> configs;

   public InfoDisplayManagerConfigData(List<String> order, Map<String, InfoDisplayConfigData> configs) {
      this.order = order;
      this.configs = configs;
   }

   public String getFromOrder(int index) {
      return (String)this.order.get(index);
   }

   public Stream<String> getOrderStream() {
      return this.order.stream();
   }

   public List<String> copyOrder() {
      return new ArrayList(this.order);
   }

   public InfoDisplayConfigData get(String id) {
      return (InfoDisplayConfigData)this.configs.get(id);
   }

   public static final class Builder {
      private final List<String> order = new ArrayList();
      private final Map<String, InfoDisplayConfigData> configs = new HashMap();

      private Builder() {
      }

      public Builder setDefault() {
         this.order.clear();
         this.configs.clear();
         return this;
      }

      public Builder add(String id, InfoDisplayConfigData infoDisplayConfig) {
         this.order.add(id);
         this.configs.put(id, infoDisplayConfig);
         return this;
      }

      public InfoDisplayManagerConfigData build() {
         return new InfoDisplayManagerConfigData(this.order, this.configs);
      }

      public static Builder begin() {
         return (new Builder()).setDefault();
      }
   }
}
