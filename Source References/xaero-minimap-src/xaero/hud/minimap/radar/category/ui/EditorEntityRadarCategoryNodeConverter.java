package xaero.hud.minimap.radar.category.ui;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import xaero.hud.category.rule.ObjectCategoryListRuleType;
import xaero.hud.category.ui.EditorFilterCategoryNodeConverter;
import xaero.hud.minimap.radar.category.EntityRadarCategory;
import xaero.hud.minimap.radar.category.rule.EntityRadarListRuleTypes;
import xaero.hud.minimap.radar.category.ui.node.EditorEntityRadarCategoryNode;
import xaero.hud.minimap.radar.category.ui.node.EditorEntityRadarCategorySettingsNode;

public final class EditorEntityRadarCategoryNodeConverter extends EditorFilterCategoryNodeConverter<class_1297, class_1657, EntityRadarCategory, EditorEntityRadarCategoryNode, EntityRadarCategory.Builder, EditorEntityRadarCategorySettingsNode<?>, EditorEntityRadarCategorySettingsNode.Builder, EditorEntityRadarCategoryNode.Builder> {
   private EditorEntityRadarCategoryNodeConverter(@Nonnull Supplier<EntityRadarCategory.Builder> categoryBuilderFactory, @Nonnull Supplier<EditorEntityRadarCategoryNode.Builder> editorDataBuilderFactory, ObjectCategoryListRuleType<class_1297, class_1657, ?> defaultListRuleType, Function<String, ObjectCategoryListRuleType<class_1297, class_1657, ?>> listRuleTypeGetter, String listRuleTypePrefixSeparator, Predicate<String> inputRuleTypeStringValidator) {
      super(categoryBuilderFactory, editorDataBuilderFactory, defaultListRuleType, listRuleTypeGetter, listRuleTypePrefixSeparator, inputRuleTypeStringValidator);
   }

   public static final class Builder extends EditorFilterCategoryNodeConverter.Builder<class_1297, class_1657, EntityRadarCategory, EditorEntityRadarCategoryNode, EntityRadarCategory.Builder, EditorEntityRadarCategorySettingsNode<?>, EditorEntityRadarCategorySettingsNode.Builder, EditorEntityRadarCategoryNode.Builder, Builder> {
      private Builder() {
         super(EntityRadarCategory.Builder::begin, EditorEntityRadarCategoryNode.Builder::begin);
      }

      protected Builder setDefault() {
         super.setDefault();
         this.setDefaultListRuleType(EntityRadarListRuleTypes.ENTITY_TYPE);
         Map var10001 = EntityRadarListRuleTypes.TYPE_MAP;
         Objects.requireNonNull(var10001);
         this.setListRuleTypeGetter(var10001::get);
         return this;
      }

      protected EditorEntityRadarCategoryNodeConverter buildInternally() {
         return new EditorEntityRadarCategoryNodeConverter(this.categoryBuilderFactory, this.editorDataBuilderFactory, this.defaultListRuleType, this.listRuleTypeGetter, this.listRuleTypePrefixSeparator, this.inputRuleTypeStringValidator);
      }

      public static Builder begin() {
         return (new Builder()).setDefault();
      }
   }
}
