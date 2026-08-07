package xaero.hud.minimap.radar.category.serialization;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import xaero.hud.category.rule.ObjectCategoryHardRule;
import xaero.hud.category.rule.ObjectCategoryListRuleType;
import xaero.hud.category.serialization.FilterObjectCategorySerializationHandler;
import xaero.hud.category.serialization.data.ObjectCategoryDataSerializer;
import xaero.hud.category.setting.ObjectCategorySetting;
import xaero.hud.minimap.radar.category.EntityRadarCategory;
import xaero.hud.minimap.radar.category.EntityRadarCategoryConstants;
import xaero.hud.minimap.radar.category.rule.EntityRadarCategoryHardRules;
import xaero.hud.minimap.radar.category.rule.EntityRadarListRuleTypes;
import xaero.hud.minimap.radar.category.serialization.data.EntityRadarCategoryData;
import xaero.hud.minimap.radar.category.setting.EntityRadarCategorySettings;

public final class EntityRadarCategorySerializationHandler extends FilterObjectCategorySerializationHandler<class_1297, class_1657, EntityRadarCategoryData, EntityRadarCategory, EntityRadarCategory.Builder, EntityRadarCategoryData.Builder> {
   private EntityRadarCategorySerializationHandler(ObjectCategoryDataSerializer<EntityRadarCategoryData, String> serializer, Supplier<EntityRadarCategoryData.Builder> dataBuilderFactory, Supplier<EntityRadarCategory.Builder> objectCategoryBuilderFactory, Function<String, ObjectCategorySetting<?>> settingTypeGetter, Function<String, ObjectCategoryHardRule<class_1297, class_1657>> hardRuleGetter, ObjectCategoryListRuleType<class_1297, class_1657, ?> defaultListRuleType, Function<String, ObjectCategoryListRuleType<class_1297, class_1657, ?>> listRuleTypeGetter, String listRuleTypePrefixSeparator) {
      super(serializer, dataBuilderFactory, objectCategoryBuilderFactory, settingTypeGetter, hardRuleGetter, defaultListRuleType, listRuleTypeGetter, listRuleTypePrefixSeparator);
   }

   public static final class Builder extends FilterObjectCategorySerializationHandler.Builder<class_1297, class_1657, EntityRadarCategoryData, EntityRadarCategory, EntityRadarCategory.Builder, EntityRadarCategoryData.Builder, EntityRadarCategorySerializationHandler, Builder> {
      private Builder(ObjectCategoryDataSerializer<EntityRadarCategoryData, String> serializer) {
         super(serializer);
      }

      public Builder setDefault() {
         super.setDefault();
         Map var10001 = EntityRadarCategoryHardRules.HARD_RULES;
         Objects.requireNonNull(var10001);
         this.setHardRuleGetter(var10001::get);
         this.setDataBuilderFactory(EntityRadarCategoryConstants.DATA_BUILDER_FACTORY);
         this.setObjectCategoryBuilderFactory(EntityRadarCategoryConstants.CATEGORY_BUILDER_FACTORY);
         var10001 = EntityRadarCategorySettings.SETTINGS;
         Objects.requireNonNull(var10001);
         this.setSettingTypeGetter(var10001::get);
         this.setDefaultListRuleType(EntityRadarListRuleTypes.ENTITY_TYPE);
         var10001 = EntityRadarListRuleTypes.TYPE_MAP;
         Objects.requireNonNull(var10001);
         this.setListRuleTypeGetter(var10001::get);
         return this;
      }

      protected EntityRadarCategorySerializationHandler buildInternally() {
         return new EntityRadarCategorySerializationHandler(this.serializer, this.dataBuilderFactory, this.objectCategoryBuilderFactory, this.settingTypeGetter, this.hardRuleGetter, this.defaultListRuleType, this.listRuleTypeGetter, this.listRuleTypePrefixSeparator);
      }

      public static Builder begin(ObjectCategoryDataSerializer<EntityRadarCategoryData, String> serializer) {
         return (new Builder(serializer)).setDefault();
      }
   }
}
