package xaero.hud.category;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import xaero.common.misc.ListFactory;
import xaero.common.misc.MapFactory;
import xaero.hud.category.serialization.data.ObjectCategoryData;
import xaero.hud.category.setting.ObjectCategorySetting;

public abstract class ObjectCategory<D extends ObjectCategoryData<D>, C extends ObjectCategory<D, C>> {
   private final C self = (C)this;
   private C superCategory;
   private final String name;
   private final boolean protection;
   private final Map<ObjectCategorySetting<?>, Object> settingOverrides;
   private final List<C> subCategories;

   protected ObjectCategory(@Nonnull String name, C superCategory, @Nonnull Map<ObjectCategorySetting<?>, Object> settingOverrides, @Nonnull List<C> subCategories, boolean protection) {
      this.name = name;
      this.superCategory = superCategory;
      this.settingOverrides = settingOverrides;
      this.subCategories = subCategories;
      this.protection = protection;
   }

   public String getName() {
      return this.name;
   }

   public C getSuperCategory() {
      return this.superCategory;
   }

   void setSuperCategory(C superCategory) {
      this.superCategory = superCategory;
   }

   public Iterator<C> getDirectSubCategoryIterator() {
      return this.subCategories.iterator();
   }

   public <T> T getSettingValue(ObjectCategorySetting<T> setting) {
      T current = (T)this.settingOverrides.get(setting);
      if (current != null) {
         return current;
      } else {
         return (T)(this.superCategory == null ? null : this.superCategory.getSettingValue(setting));
      }
   }

   public <T> void setSettingValue(ObjectCategorySetting<T> setting, T value) {
      this.settingOverrides.put(setting, value);
   }

   public Iterator<Map.Entry<ObjectCategorySetting<?>, Object>> getSettingOverridesIterator() {
      return this.settingOverrides.entrySet().iterator();
   }

   public boolean getProtection() {
      return this.protection;
   }

   public abstract static class Builder<C extends ObjectCategory<?, C>, B extends Builder<C, B>> {
      protected final B self = (B)this;
      protected String name;
      protected C superCategory;
      protected final List<B> subCategories;
      protected boolean protection;
      protected final Map<ObjectCategorySetting<?>, Object> settingOverrides;
      protected final ListFactory listFactory;
      protected final MapFactory mapFactory;

      public Builder(@Nonnull ListFactory listFactory, @Nonnull MapFactory mapFactory) {
         this.subCategories = listFactory.<B>get();
         this.settingOverrides = mapFactory.<ObjectCategorySetting<?>, Object>get();
         this.listFactory = listFactory;
         this.mapFactory = mapFactory;
      }

      public B setDefault() {
         this.setName((String)null);
         this.subCategories.clear();
         this.settingOverrides.clear();
         this.setSuperCategory((ObjectCategory)null);
         this.setProtection(false);
         return this.self;
      }

      public B setName(String name) {
         this.name = name;
         return this.self;
      }

      public B setSuperCategory(C superCategory) {
         this.superCategory = superCategory;
         return this.self;
      }

      public B addSubCategoryBuilder(B subCategoryBuilder) {
         this.subCategories.add(subCategoryBuilder);
         return this.self;
      }

      public <T> B setSettingValue(ObjectCategorySetting<T> setting, T value) {
         this.settingOverrides.put(setting, value);
         return this.self;
      }

      public B setProtection(boolean protection) {
         this.protection = protection;
         return this.self;
      }

      public C build() {
         if (this.name == null) {
            throw new IllegalStateException("required fields not set!");
         } else {
            C result = this.buildUnchecked(this.buildSubCategories());
            result.getDirectSubCategoryIterator().forEachRemaining((c) -> c.setSuperCategory(result));
            return result;
         }
      }

      private List<C> buildSubCategories() {
         Stream var10000 = this.subCategories.stream().map(Builder::build);
         ListFactory var10001 = this.listFactory;
         Objects.requireNonNull(var10001);
         return (List)var10000.collect(var10001::get, List::add, List::addAll);
      }

      protected abstract C buildUnchecked(List<C> var1);
   }
}
