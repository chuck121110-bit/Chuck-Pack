package xaero.hud.category.ui;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import xaero.hud.category.FilterObjectCategory;
import xaero.hud.category.rule.ObjectCategoryExcludeList;
import xaero.hud.category.rule.ObjectCategoryIncludeList;
import xaero.hud.category.rule.ObjectCategoryListRule;
import xaero.hud.category.rule.ObjectCategoryListRuleType;
import xaero.hud.category.serialization.FilterObjectCategorySerializationHandler;
import xaero.hud.category.ui.node.EditorFilterCategoryNode;
import xaero.hud.category.ui.node.EditorFilterSettingsNode;
import xaero.hud.category.ui.node.rule.EditorExcludeListNode;
import xaero.hud.category.ui.node.rule.EditorIncludeListNode;

public abstract class EditorFilterCategoryNodeConverter<E, P, C extends FilterObjectCategory<E, P, ?, C>, ED extends EditorFilterCategoryNode<C, SD, ED>, CB extends FilterObjectCategory.Builder<E, P, C, CB>, SD extends EditorFilterSettingsNode<E, P, ?>, SDB extends EditorFilterSettingsNode.Builder<E, P, SD, SDB>, EDB extends EditorFilterCategoryNode.Builder<C, ED, SD, SDB, EDB>> extends EditorCategoryNodeConverter<C, ED, CB, SD, SDB, EDB> {
   private final ObjectCategoryListRuleType<E, P, ?> defaultListRuleType;
   private final Function<String, ObjectCategoryListRuleType<E, P, ?>> listRuleTypeGetter;
   private final String listRuleTypePrefixSeparator;
   private final Predicate<String> inputRuleTypeStringValidator;

   public EditorFilterCategoryNodeConverter(@Nonnull Supplier<CB> categoryBuilderFactory, @Nonnull Supplier<EDB> editorDataBuilderFactory, ObjectCategoryListRuleType<E, P, ?> defaultListRuleType, Function<String, ObjectCategoryListRuleType<E, P, ?>> listRuleTypeGetter, String listRuleTypePrefixSeparator, Predicate<String> inputRuleTypeStringValidator) {
      super(categoryBuilderFactory, editorDataBuilderFactory);
      this.defaultListRuleType = defaultListRuleType;
      this.listRuleTypeGetter = listRuleTypeGetter;
      this.listRuleTypePrefixSeparator = listRuleTypePrefixSeparator;
      this.inputRuleTypeStringValidator = inputRuleTypeStringValidator;
   }

   protected EDB getConfiguredBuilder(C category, boolean canBeRoot) {
      EDB editorNodeBuilder = (EDB)(super.getConfiguredBuilder(category, canBeRoot));
      SDB settingNodeBuilder = (SDB)(editorNodeBuilder.getSettingDataBuilder());
      ((EditorFilterSettingsNode.Builder)settingNodeBuilder).setBaseRule(((FilterObjectCategory)category).getBaseRule());
      EditorIncludeListNode.Builder<E, P> includeListBuilder = ((EditorFilterSettingsNode.Builder)settingNodeBuilder).getIncludeListBuilder();
      EditorExcludeListNode.Builder<E, P> excludeListBuilder = ((EditorFilterSettingsNode.Builder)settingNodeBuilder).getExcludeListBuilder();

      for(ObjectCategoryIncludeList<E, P, ?> includeList : ((FilterObjectCategory)category).getIncludeLists()) {
         String prefix = this.getListRulePrefix(includeList);
         includeList.forEach((el) -> includeListBuilder.addListElement(prefix + el));
      }

      for(ObjectCategoryExcludeList<E, P, ?> excludeList : ((FilterObjectCategory)category).getExcludeLists()) {
         String prefix = this.getListRulePrefix(excludeList);
         excludeList.forEach((el) -> excludeListBuilder.addListElement(prefix + el));
      }

      ((EditorFilterCategoryNode.Builder)editorNodeBuilder).setListRuleTypePrefixSeparator(this.listRuleTypePrefixSeparator).setInputRuleTypeStringValidator(this.inputRuleTypeStringValidator);
      includeListBuilder.getIncludeInSuperToggleDataBuilder().setCurrentValue(category.getIncludeInSuperCategory());
      excludeListBuilder.setExcludeMode(category.getExcludeMode());
      return editorNodeBuilder;
   }

   private String getListRulePrefix(ObjectCategoryListRule<E, P, ?> list) {
      if (list.getType() == this.defaultListRuleType) {
         return "";
      } else {
         String var10000 = list.getType().getId();
         return var10000 + this.listRuleTypePrefixSeparator;
      }
   }

   protected CB getConfiguredBuilder(ED editorNode) {
      CB categoryBuilder = (CB)(super.getConfiguredBuilder(editorNode));
      SD settingsNode = (SD)(editorNode.getSettingsNode());
      ((FilterObjectCategory.Builder)categoryBuilder).setBaseRule(((EditorFilterSettingsNode)settingsNode).getBaseRule());
      ((FilterObjectCategory.Builder)categoryBuilder).setIncludeInSuperCategory(settingsNode.getIncludeList().getIncludeInSuper());
      ((FilterObjectCategory.Builder)categoryBuilder).setExcludeMode(settingsNode.getExcludeList().getExcludeMode());
      settingsNode.getIncludeList().getList().forEach((led) -> {
         String var10000 = (String)led.getElement();
         Objects.requireNonNull(categoryBuilder);
         FilterObjectCategorySerializationHandler.handleListRuleSerializedElement(var10000, categoryBuilder::getIncludeListBuilder, this.defaultListRuleType, this.listRuleTypeGetter, this.listRuleTypePrefixSeparator);
      });
      settingsNode.getExcludeList().getList().forEach((led) -> {
         String var10000 = (String)led.getElement();
         Objects.requireNonNull(categoryBuilder);
         FilterObjectCategorySerializationHandler.handleListRuleSerializedElement(var10000, categoryBuilder::getExcludeListBuilder, this.defaultListRuleType, this.listRuleTypeGetter, this.listRuleTypePrefixSeparator);
      });
      return categoryBuilder;
   }

   public abstract static class Builder<E, P, C extends FilterObjectCategory<E, P, ?, C>, ED extends EditorFilterCategoryNode<C, SD, ED>, CB extends FilterObjectCategory.Builder<E, P, C, CB>, SD extends EditorFilterSettingsNode<E, P, ?>, SDB extends EditorFilterSettingsNode.Builder<E, P, SD, SDB>, EDB extends EditorFilterCategoryNode.Builder<C, ED, SD, SDB, EDB>, B extends Builder<E, P, C, ED, CB, SD, SDB, EDB, B>> extends EditorCategoryNodeConverter.Builder<C, ED, CB, SD, SDB, EDB, B> {
      protected ObjectCategoryListRuleType<E, P, ?> defaultListRuleType;
      protected Function<String, ObjectCategoryListRuleType<E, P, ?>> listRuleTypeGetter;
      protected String listRuleTypePrefixSeparator;
      protected Predicate<String> inputRuleTypeStringValidator;

      protected Builder(Supplier<CB> categoryBuilderFactory, Supplier<EDB> editorDataBuilderFactory) {
         super(categoryBuilderFactory, editorDataBuilderFactory);
      }

      protected B setDefault() {
         this.setDefaultListRuleType((ObjectCategoryListRuleType)null);
         this.setListRuleTypeGetter((Function)null);
         this.setListRuleTypePrefixSeparator(";");
         this.setInputRuleTypeStringValidator((s) -> s.matches("[a-z_0-9\\-]+"));
         return (B)(super.setDefault());
      }

      public B setDefaultListRuleType(ObjectCategoryListRuleType<E, P, ?> defaultListRuleType) {
         this.defaultListRuleType = defaultListRuleType;
         return this.self;
      }

      public B setListRuleTypeGetter(Function<String, ObjectCategoryListRuleType<E, P, ?>> listRuleTypeGetter) {
         this.listRuleTypeGetter = listRuleTypeGetter;
         return this.self;
      }

      public B setListRuleTypePrefixSeparator(String listRuleTypePrefixSeparator) {
         this.listRuleTypePrefixSeparator = listRuleTypePrefixSeparator;
         return this.self;
      }

      public B setInputRuleTypeStringValidator(Predicate<String> inputRuleTypeStringValidator) {
         this.inputRuleTypeStringValidator = inputRuleTypeStringValidator;
         return this.self;
      }

      public EditorCategoryNodeConverter<C, ED, CB, SD, SDB, EDB> build() {
         if (this.defaultListRuleType != null && this.listRuleTypeGetter != null) {
            return super.build();
         } else {
            throw new IllegalStateException();
         }
      }

      protected abstract EditorFilterCategoryNodeConverter<E, P, C, ED, CB, SD, SDB, EDB> buildInternally();
   }
}
