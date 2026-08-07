package xaero.hud.minimap.radar.category;

import javax.annotation.Nonnull;
import xaero.hud.minimap.radar.category.serialization.EntityRadarCategorySerializationHandler;
import xaero.hud.minimap.radar.category.serialization.data.EntityRadarCategoryData;
import xaero.lib.common.config.Config;
import xaero.lib.common.config.option.ConfigOption;

public final class EntityRadarCategoryConfigIO {
   private final ConfigOption<EntityRadarCategoryData> option;
   private final EntityRadarCategorySerializationHandler serializationHandler;

   private EntityRadarCategoryConfigIO(@Nonnull ConfigOption<EntityRadarCategoryData> option, @Nonnull EntityRadarCategorySerializationHandler serializationHandler) {
      this.option = option;
      this.serializationHandler = serializationHandler;
   }

   public void storeRootCategory(EntityRadarCategory category, Config config) {
      EntityRadarCategoryData data = (EntityRadarCategoryData)this.serializationHandler.convertToData(category);
      config.set(this.option, data);
   }

   public EntityRadarCategory loadRootCategory(Config config) {
      EntityRadarCategoryData data = (EntityRadarCategoryData)config.get(this.option);
      return data != null && data != EntityRadarCategoryConstants.NULL_DATA ? (EntityRadarCategory)this.serializationHandler.convertFromData(data) : null;
   }

   public static final class Builder {
      private ConfigOption<EntityRadarCategoryData> option;
      private EntityRadarCategorySerializationHandler serializationHandler;

      private Builder() {
      }

      private Builder setDefault() {
         this.setOption((ConfigOption)null);
         return this;
      }

      public Builder setOption(ConfigOption<EntityRadarCategoryData> option) {
         this.option = option;
         return this;
      }

      public Builder setSerializationHandler(EntityRadarCategorySerializationHandler serializationHandler) {
         this.serializationHandler = serializationHandler;
         return this;
      }

      public EntityRadarCategoryConfigIO build() {
         if (this.option != null && this.serializationHandler != null) {
            return new EntityRadarCategoryConfigIO(this.option, this.serializationHandler);
         } else {
            throw new IllegalStateException("required fields not set!");
         }
      }

      public static Builder begin() {
         return (new Builder()).setDefault();
      }
   }
}
