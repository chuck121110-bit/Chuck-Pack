package xaero.hud.category.serialization;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import xaero.hud.category.FilterObjectCategory;
import xaero.hud.category.rule.ObjectCategoryExcludeList;
import xaero.hud.category.rule.ObjectCategoryHardRule;
import xaero.hud.category.rule.ObjectCategoryIncludeList;
import xaero.hud.category.rule.ObjectCategoryListRule;
import xaero.hud.category.rule.ObjectCategoryListRuleType;
import xaero.hud.category.rule.ObjectCategoryRule;
import xaero.hud.category.serialization.data.FilterObjectCategoryData;
import xaero.hud.category.serialization.data.ObjectCategoryDataSerializer;
import xaero.hud.category.setting.ObjectCategorySetting;

public abstract class FilterObjectCategorySerializationHandler<E, P, D extends FilterObjectCategoryData<D>, C extends FilterObjectCategory<E, P, D, C>, B extends FilterObjectCategory.Builder<E, P, C, B>, DB extends FilterObjectCategoryData.Builder<D, DB>> extends ObjectCategorySerializationHandler<D, C, B, DB> {
   private final Function<String, ObjectCategoryHardRule<E, P>> hardRuleGetter;
   private final ObjectCategoryListRuleType<E, P, ?> defaultListRuleType;
   private final Function<String, ObjectCategoryListRuleType<E, P, ?>> listRuleTypeGetter;
   private final String listRuleTypePrefixSeparator;

   protected FilterObjectCategorySerializationHandler(ObjectCategoryDataSerializer<D, String> serializer, Supplier<DB> dataBuilderFactory, Supplier<B> objectCategoryBuilderFactory, Function<String, ObjectCategorySetting<?>> settingTypeGetter, Function<String, ObjectCategoryHardRule<E, P>> hardRuleGetter, ObjectCategoryListRuleType<E, P, ?> defaultListRuleType, Function<String, ObjectCategoryListRuleType<E, P, ?>> listRuleTypeGetter, String listRuleTypePrefixSeparator) {
      super(serializer, dataBuilderFactory, objectCategoryBuilderFactory, settingTypeGetter);
      this.hardRuleGetter = hardRuleGetter;
      this.defaultListRuleType = defaultListRuleType;
      this.listRuleTypeGetter = listRuleTypeGetter;
      this.listRuleTypePrefixSeparator = listRuleTypePrefixSeparator;
   }

   protected DB getConfiguredDataBuilderForCategory(C category) {
      DB dataBuilder = (DB)(super.getConfiguredDataBuilderForCategory(category));
      ObjectCategoryRule<E, P> baseRule = ((FilterObjectCategory)category).getBaseRule();
      dataBuilder.setHardInclude(baseRule == null ? "nothing" : baseRule.getName());
      dataBuilder.setExcludeMode(category.getExcludeMode());
      ((FilterObjectCategoryData.Builder)dataBuilder).setIncludeListInSuperCategory(category.getIncludeInSuperCategory());

      for(ObjectCategoryIncludeList<E, P, ?> includeList : ((FilterObjectCategory)category).getIncludeLists()) {
         String prefix = this.getListRulePrefix(includeList);
         includeList.forEach((el) -> dataBuilder.addToIncludeList(prefix + el));
      }

      for(ObjectCategoryExcludeList<E, P, ?> excludeList : ((FilterObjectCategory)category).getExcludeLists()) {
         String prefix = this.getListRulePrefix(excludeList);
         excludeList.forEach((el) -> dataBuilder.addToExcludeList(prefix + el));
      }

      return dataBuilder;
   }

   private String getListRulePrefix(ObjectCategoryListRule<E, P, ?> listRule) {
      if (listRule.getType() == this.defaultListRuleType) {
         return "";
      } else {
         String var10000 = listRule.getType().getId();
         return var10000 + this.listRuleTypePrefixSeparator;
      }
   }

   protected B getConfiguredCategoryBuilderForData(D data) {
      B objectCategoryBuilder = (B)(super.getConfiguredCategoryBuilderForData(data));
      String hardInclude = data.getHardInclude();
      ObjectCategoryHardRule<E, P> serializedHardRule = this.hardRuleGetter == null ? null : (ObjectCategoryHardRule)this.hardRuleGetter.apply(hardInclude);
      if (serializedHardRule != null) {
         ((FilterObjectCategory.Builder)objectCategoryBuilder).setBaseRule(serializedHardRule);
      }

      ((FilterObjectCategory.Builder)objectCategoryBuilder).setExcludeMode(data.getExcludeMode());
      ((FilterObjectCategory.Builder)objectCategoryBuilder).setIncludeInSuperCategory(data.getIncludeListInSuperCategory());
      data.getIncludeListIterator().forEachRemaining((s) -> {
         Objects.requireNonNull(objectCategoryBuilder);
         this.handleListRuleSerializedElement(s, objectCategoryBuilder::getIncludeListBuilder);
      });
      data.getExcludeListIterator().forEachRemaining((s) -> {
         Objects.requireNonNull(objectCategoryBuilder);
         this.handleListRuleSerializedElement(s, objectCategoryBuilder::getExcludeListBuilder);
      });
      return objectCategoryBuilder;
   }

   public void handleListRuleSerializedElement(String s, Function<ObjectCategoryListRuleType<E, P, ?>, ObjectCategoryListRule.Builder<E, P, ?, ?>> listBuilderGetter) {
      handleListRuleSerializedElement(s, listBuilderGetter, this.defaultListRuleType, this.listRuleTypeGetter, this.listRuleTypePrefixSeparator);
   }

   public static <E, P> void handleListRuleSerializedElement(String s, Function<ObjectCategoryListRuleType<E, P, ?>, ObjectCategoryListRule.Builder<E, P, ?, ?>> listBuilderGetter, ObjectCategoryListRuleType<E, P, ?> defaultListRuleType, Function<String, ObjectCategoryListRuleType<E, P, ?>> listRuleTypeGetter, String listRuleTypePrefixSeparator) {
      ObjectCategoryListRuleType<E, P, ?> entryListRuleType = defaultListRuleType;
      if (s.contains(listRuleTypePrefixSeparator)) {
         ObjectCategoryListRuleType<E, P, ?> specifiedListRuleType = (ObjectCategoryListRuleType)listRuleTypeGetter.apply(s.substring(0, s.indexOf(listRuleTypePrefixSeparator)));
         if (specifiedListRuleType != null) {
            entryListRuleType = specifiedListRuleType;
         }

         s = s.substring(s.indexOf(listRuleTypePrefixSeparator) + 1);
      }

      ((ObjectCategoryListRule.Builder)listBuilderGetter.apply(entryListRuleType)).addListElement(s);
   }

   public abstract static class Builder<E, P, D extends FilterObjectCategoryData<D>, C extends FilterObjectCategory<E, P, D, C>, B extends FilterObjectCategory.Builder<E, P, C, B>, DB extends FilterObjectCategoryData.Builder<D, DB>, SH extends FilterObjectCategorySerializationHandler<E, P, D, C, B, DB>, SHB extends Builder<E, P, D, C, B, DB, SH, SHB>> extends ObjectCategorySerializationHandler.Builder<D, C, B, DB, SH, SHB> {
      protected Function<String, ObjectCategoryHardRule<E, P>> hardRuleGetter;
      protected ObjectCategoryListRuleType<E, P, ?> defaultListRuleType;
      protected Function<String, ObjectCategoryListRuleType<E, P, ?>> listRuleTypeGetter;
      protected String listRuleTypePrefixSeparator;

      public Builder(ObjectCategoryDataSerializer<D, String> serializer) {
         super(serializer);
      }

      public SHB setDefault() {
         super.setDefault();
         this.setHardRuleGetter((Function)null);
         this.setDefaultListRuleType((ObjectCategoryListRuleType)null);
         this.setListRuleTypeGetter((Function)null);
         this.setListRuleTypePrefixSeparator(";");
         return this.self;
      }

      public SHB setDefaultListRuleType(ObjectCategoryListRuleType<E, P, ?> defaultListRuleType) {
         this.defaultListRuleType = defaultListRuleType;
         return this.self;
      }

      public SHB setHardRuleGetter(Function<String, ObjectCategoryHardRule<E, P>> hardRuleGetter) {
         this.hardRuleGetter = hardRuleGetter;
         return this.self;
      }

      public SHB setListRuleTypeGetter(Function<String, ObjectCategoryListRuleType<E, P, ?>> listRuleTypeGetter) {
         this.listRuleTypeGetter = listRuleTypeGetter;
         return this.self;
      }

      public SHB setListRuleTypePrefixSeparator(String listRuleTypePrefixSeparator) {
         this.listRuleTypePrefixSeparator = listRuleTypePrefixSeparator;
         return this.self;
      }

      public SH build() {
         if (this.hardRuleGetter != null && this.defaultListRuleType != null && this.listRuleTypeGetter != null) {
            return (SH)(super.build());
         } else {
            throw new IllegalStateException("required fields not set!");
         }
      }
   }
}
