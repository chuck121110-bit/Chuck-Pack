package xaero.hud.category.serialization.data;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import xaero.common.misc.ListFactory;
import xaero.common.misc.MapFactory;

public abstract class ObjectCategoryData<D extends ObjectCategoryData<D>> {
   private final String name;
   private final boolean protection;
   private final Map<String, Object> settingOverrides;
   private final List<D> subCategories;

   protected ObjectCategoryData(@Nonnull String name, @Nonnull Map<String, Object> settingOverrides, @Nonnull List<D> subCategories, boolean protection) {
      this.name = name;
      this.settingOverrides = settingOverrides;
      this.subCategories = subCategories;
      this.protection = protection;
   }

   public String getName() {
      return this.name;
   }

   public Iterator<Map.Entry<String, Object>> getSettingOverrideIterator() {
      return this.settingOverrides.entrySet().iterator();
   }

   public Iterator<D> getSubCategoryIterator() {
      return this.subCategories.iterator();
   }

   public boolean getProtection() {
      return this.protection;
   }

   public abstract static class Builder<D extends ObjectCategoryData<D>, B extends Builder<D, B>> {
      protected final B self = (B)this;
      protected String name;
      protected final Map<String, Object> settingOverrides;
      private final ListFactory listFactory;
      protected final List<B> subCategoryBuilders;
      protected boolean protection;

      public Builder(@Nonnull ListFactory listFactory, @Nonnull MapFactory mapFactory) {
         this.settingOverrides = mapFactory.<String, Object>get();
         this.subCategoryBuilders = listFactory.<B>get();
         this.listFactory = listFactory;
      }

      public B setDefault() {
         this.setName((String)null);
         this.setProtection(false);
         this.settingOverrides.clear();
         return this.self;
      }

      public B setName(String name) {
         this.name = name;
         return this.self;
      }

      public B setSettingOverride(String key, Object value) {
         this.settingOverrides.put(key, value);
         return this.self;
      }

      public B addSubCategoryBuilder(B builder) {
         this.subCategoryBuilders.add(builder);
         return this.self;
      }

      public B setProtection(boolean protection) {
         this.protection = protection;
         return this.self;
      }

      private List<D> buildSubCategories() {
         Stream var10000 = this.subCategoryBuilders.stream().map(Builder::build);
         ListFactory var10001 = this.listFactory;
         Objects.requireNonNull(var10001);
         return (List)var10000.collect(var10001::get, List::add, List::addAll);
      }

      public D build() {
         if (this.name == null) {
            throw new IllegalStateException("required fields not set!");
         } else {
            return (D)this.buildInternally(this.buildSubCategories());
         }
      }

      protected abstract D buildInternally(List<D> var1);
   }
}
