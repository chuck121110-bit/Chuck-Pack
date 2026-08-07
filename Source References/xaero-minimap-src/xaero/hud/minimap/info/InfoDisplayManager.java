package xaero.hud.minimap.info;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import xaero.common.HudMod;
import xaero.hud.minimap.common.config.info.config.InfoDisplayManagerConfigData;
import xaero.hud.minimap.info.config.InfoDisplayConfigClientUtils;

public final class InfoDisplayManager {
   private final Map<String, InfoDisplay<?>> displays;
   private final List<String> defaultOrder;
   private final List<String> unmodifiableDefaultOrder;
   private List<String> order;
   private final Supplier<InfoDisplayManagerConfigData> localConfigSupplier;
   private final Consumer<InfoDisplayManagerConfigData> localConfigSetter;
   private final Supplier<InfoDisplayManagerConfigData> enforcedConfigSupplier;
   private boolean configApplicationRecursion;

   private InfoDisplayManager(Map<String, InfoDisplay<?>> displays, List<String> defaultOrder, List<String> unmodifiableDefaultOrder, Supplier<InfoDisplayManagerConfigData> localConfigSupplier, Consumer<InfoDisplayManagerConfigData> localConfigSetter, Supplier<InfoDisplayManagerConfigData> enforcedConfigSupplier) {
      this.displays = displays;
      this.defaultOrder = defaultOrder;
      this.unmodifiableDefaultOrder = unmodifiableDefaultOrder;
      this.localConfigSupplier = localConfigSupplier;
      this.localConfigSetter = localConfigSetter;
      this.enforcedConfigSupplier = enforcedConfigSupplier;
   }

   public void add(InfoDisplay<?> infoDisplay) {
      if (this.displays.put(infoDisplay.getId(), infoDisplay) == null) {
         this.defaultOrder.add(infoDisplay.getId());
         infoDisplay.setManager(this);
      }

   }

   public void applyLocalConfig() {
      if (!this.configApplicationRecursion) {
         InfoDisplayManagerConfigData config = this.getLocalConfig();
         if (config == null || config == InfoDisplayManagerConfigData.EMPTY) {
            this.configApplicationRecursion = true;
            this.localConfigSetter.accept(config = InfoDisplayConfigClientUtils.createDefaultConfig(this, HudMod.INSTANCE.getSettings(), true));
            this.configApplicationRecursion = false;
         }

         this.order = this.adaptOrder(config.getOrderStream());
      }
   }

   public List<String> adaptOrder(Stream<String> originalOrder) {
      List<String> order = new ArrayList();
      Stream var10000 = originalOrder.filter((id) -> this.get(id) != null);
      Objects.requireNonNull(order);
      var10000.forEach(order::add);
      int lastDefaultOrderIdIndex = -1;

      for(int i = 0; i < this.defaultOrder.size(); ++i) {
         String defaultOrderId = (String)this.defaultOrder.get(i);
         int defaultOrderIdIndex = order.indexOf(defaultOrderId);
         if (defaultOrderIdIndex == -1) {
            if (lastDefaultOrderIdIndex != -1) {
               defaultOrderIdIndex = lastDefaultOrderIdIndex + 1;
            } else {
               defaultOrderIdIndex = 0;
            }

            order.add(defaultOrderIdIndex, defaultOrderId);
         }

         lastDefaultOrderIdIndex = defaultOrderIdIndex;
      }

      return order;
   }

   public InfoDisplayManagerConfigData getLocalConfig() {
      return (InfoDisplayManagerConfigData)this.localConfigSupplier.get();
   }

   public InfoDisplayManagerConfigData getEnforcedConfig() {
      return (InfoDisplayManagerConfigData)this.enforcedConfigSupplier.get();
   }

   public InfoDisplay<?> get(String id) {
      return (InfoDisplay)this.displays.get(id);
   }

   public Stream<InfoDisplay<?>> getOrderedStream() {
      Stream var10000 = this.order.stream();
      Map var10001 = this.displays;
      Objects.requireNonNull(var10001);
      Stream<InfoDisplay<?>> unfilteredResult = var10000.map(var10001::get);
      return unfilteredResult.filter(Objects::nonNull);
   }

   public Stream<InfoDisplay<?>> getStream() {
      return this.displays.values().stream();
   }

   public int getCount() {
      return this.displays.size();
   }

   public List<String> getDefaultOrder() {
      return this.unmodifiableDefaultOrder;
   }

   public void clearStateCache() {
      this.displays.values().forEach(InfoDisplay::clearStateCache);
   }

   public static final class Builder {
      private Supplier<InfoDisplayManagerConfigData> localConfigSupplier;
      private Consumer<InfoDisplayManagerConfigData> localConfigSetter;
      private Supplier<InfoDisplayManagerConfigData> enforcedConfigSupplier;

      private Builder() {
      }

      private Builder setDefault() {
         this.setLocalConfigSupplier((Supplier)null);
         this.setLocalConfigSetter((Consumer)null);
         this.setEnforcedConfigSupplier((Supplier)null);
         return this;
      }

      public Builder setLocalConfigSupplier(Supplier<InfoDisplayManagerConfigData> localConfigSupplier) {
         this.localConfigSupplier = localConfigSupplier;
         return this;
      }

      public Builder setLocalConfigSetter(Consumer<InfoDisplayManagerConfigData> localConfigSetter) {
         this.localConfigSetter = localConfigSetter;
         return this;
      }

      public Builder setEnforcedConfigSupplier(Supplier<InfoDisplayManagerConfigData> enforcedConfigSupplier) {
         this.enforcedConfigSupplier = enforcedConfigSupplier;
         return this;
      }

      public InfoDisplayManager build() {
         if (this.localConfigSupplier != null && this.localConfigSetter != null && this.enforcedConfigSupplier != null) {
            Map<String, InfoDisplay<?>> displays = new HashMap();
            List<String> defaultOrder = new ArrayList();
            List<String> unmodifiableDefaultOrder = Collections.unmodifiableList(defaultOrder);
            return new InfoDisplayManager(displays, defaultOrder, unmodifiableDefaultOrder, this.localConfigSupplier, this.localConfigSetter, this.enforcedConfigSupplier);
         } else {
            throw new IllegalStateException();
         }
      }

      public static Builder begin() {
         return (new Builder()).setDefault();
      }
   }
}
