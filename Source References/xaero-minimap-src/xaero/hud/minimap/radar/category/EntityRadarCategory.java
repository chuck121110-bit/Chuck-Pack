package xaero.hud.minimap.radar.category;

import java.util.List;
import java.util.Map;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import xaero.hud.category.FilterObjectCategory;
import xaero.hud.category.rule.ExcludeListMode;
import xaero.hud.category.rule.ObjectCategoryExcludeList;
import xaero.hud.category.rule.ObjectCategoryIncludeList;
import xaero.hud.category.rule.ObjectCategoryListRuleType;
import xaero.hud.category.rule.ObjectCategoryRule;
import xaero.hud.category.setting.ObjectCategorySetting;
import xaero.hud.minimap.radar.category.rule.EntityRadarCategoryHardRules;
import xaero.hud.minimap.radar.category.rule.EntityRadarListRuleTypes;
import xaero.hud.minimap.radar.category.serialization.data.EntityRadarCategoryData;

public final class EntityRadarCategory extends FilterObjectCategory<class_1297, class_1657, EntityRadarCategoryData, EntityRadarCategory> {
   private EntityRadarCategory(String name, EntityRadarCategory parent, ObjectCategoryRule<class_1297, class_1657> baseRule, Map<ObjectCategoryListRuleType<class_1297, class_1657, ?>, ObjectCategoryIncludeList<class_1297, class_1657, ?>> includeLists, Map<ObjectCategoryListRuleType<class_1297, class_1657, ?>, ObjectCategoryExcludeList<class_1297, class_1657, ?>> excludeLists, List<ObjectCategoryIncludeList<class_1297, class_1657, ?>> includeListsIndexed, List<ObjectCategoryExcludeList<class_1297, class_1657, ?>> excludeListsIndexed, Map<ObjectCategorySetting<?>, Object> settingOverrides, List<EntityRadarCategory> subCategories, boolean protection, ExcludeListMode excludeMode, boolean includeInSuperCategory) {
      super(name, parent, baseRule, includeLists, excludeLists, includeListsIndexed, excludeListsIndexed, settingOverrides, subCategories, protection, excludeMode, includeInSuperCategory);
   }

   public static final class Builder extends FilterObjectCategory.Builder<class_1297, class_1657, EntityRadarCategory, Builder> {
      private Builder() {
         super(EntityRadarCategoryConstants.LIST_FACTORY, EntityRadarCategoryConstants.MAP_FACTORY, EntityRadarListRuleTypes.TYPE_LIST);
      }

      public Builder setDefault() {
         super.setDefault();
         this.setBaseRule(EntityRadarCategoryHardRules.IS_NOTHING);
         return this;
      }

      protected EntityRadarCategory buildUncheckedFilter(List<EntityRadarCategory> subCategories, Map<ObjectCategoryListRuleType<class_1297, class_1657, ?>, ObjectCategoryIncludeList<class_1297, class_1657, ?>> includeLists, Map<ObjectCategoryListRuleType<class_1297, class_1657, ?>, ObjectCategoryExcludeList<class_1297, class_1657, ?>> excludeLists, List<ObjectCategoryIncludeList<class_1297, class_1657, ?>> includeListsIndexed, List<ObjectCategoryExcludeList<class_1297, class_1657, ?>> excludeListsIndexed) {
         return new EntityRadarCategory(this.name, this.superCategory, this.baseRule, includeLists, excludeLists, includeListsIndexed, excludeListsIndexed, this.settingOverrides, subCategories, this.protection, this.excludeMode, this.includeInSuperCategory);
      }

      public static Builder begin() {
         return (new Builder()).setDefault();
      }
   }
}
