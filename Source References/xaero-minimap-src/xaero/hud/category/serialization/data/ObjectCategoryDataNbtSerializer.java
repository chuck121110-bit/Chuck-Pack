package xaero.hud.category.serialization.data;

import java.util.function.Supplier;
import net.minecraft.class_2481;
import net.minecraft.class_2487;
import net.minecraft.class_2489;
import net.minecraft.class_2497;
import net.minecraft.class_2499;
import net.minecraft.class_2520;

public class ObjectCategoryDataNbtSerializer<D extends ObjectCategoryData<D>, DB extends ObjectCategoryData.Builder<D, DB>> extends ObjectCategoryDataSerializer<D, class_2487> {
   private final Supplier<DB> builderSupplier;

   protected ObjectCategoryDataNbtSerializer(Supplier<DB> builderSupplier) {
      this.builderSupplier = builderSupplier;
   }

   public class_2487 serialize(D data) {
      class_2487 resultTag = new class_2487();
      resultTag.method_10582("n", data.getName());
      resultTag.method_10556("p", data.getProtection());
      class_2487 settingOverrides = new class_2487();
      data.getSettingOverrideIterator().forEachRemaining((entry) -> {
         String key = (String)entry.getKey();
         Object value = entry.getValue();
         if (value != null) {
            if (value instanceof Boolean) {
               settingOverrides.method_10556(key, (Boolean)value);
            } else if (value instanceof Double) {
               settingOverrides.method_10549(key, (Double)value);
            } else if (value instanceof Integer) {
               settingOverrides.method_10569(key, (Integer)value);
            } else {
               throw new IllegalArgumentException("Unsupported category setting type: " + String.valueOf(value.getClass()));
            }
         }
      });
      resultTag.method_10566("v", settingOverrides);
      class_2499 subCategoriesTag = new class_2499();
      data.getSubCategoryIterator().forEachRemaining((sub) -> {
         class_2487 subCategoryTag = this.serialize(sub);
         subCategoriesTag.add(subCategoryTag);
      });
      resultTag.method_10566("s", subCategoriesTag);
      return resultTag;
   }

   public final D deserialize(class_2487 serializedData) {
      return (D)this.getConfiguredBuilder(serializedData).build();
   }

   protected DB getConfiguredBuilder(class_2487 serializedData) {
      DB builder = (DB)(this.builderSupplier.get());
      ((ObjectCategoryData.Builder)builder).setName(serializedData.method_68564("n", ""));
      ((ObjectCategoryData.Builder)builder).setProtection(serializedData.method_68566("p", false));
      class_2487 settingOverrides = serializedData.method_68568("v");

      for(String key : settingOverrides.method_10541()) {
         class_2520 valueTag = settingOverrides.method_10580(key);
         if (valueTag instanceof class_2481) {
            ((ObjectCategoryData.Builder)builder).setSettingOverride(key, ((class_2481)valueTag).method_10698() == 1);
         } else if (valueTag instanceof class_2489) {
            ((ObjectCategoryData.Builder)builder).setSettingOverride(key, ((class_2489)valueTag).method_10697());
         } else {
            if (!(valueTag instanceof class_2497)) {
               throw new IllegalArgumentException("Unsupported category setting NBT tag type: " + String.valueOf(valueTag.getClass()));
            }

            ((ObjectCategoryData.Builder)builder).setSettingOverride(key, ((class_2497)valueTag).method_10701());
         }
      }

      for(class_2520 subCategoryTag : serializedData.method_68569("s")) {
         ((ObjectCategoryData.Builder)builder).addSubCategoryBuilder(this.getConfiguredBuilder((class_2487)subCategoryTag));
      }

      return builder;
   }

   public abstract static class Builder<D extends ObjectCategoryData<D>, DB extends ObjectCategoryData.Builder<D, DB>, B extends Builder<D, DB, B>> {
      protected final B self = (B)this;
      protected Supplier<DB> builderSupplier;

      protected Builder() {
      }

      public B setDefault() {
         this.setBuilderSupplier((Supplier)null);
         return this.self;
      }

      public B setBuilderSupplier(Supplier<DB> builderSupplier) {
         this.builderSupplier = builderSupplier;
         return this.self;
      }

      public ObjectCategoryDataNbtSerializer<D, DB> build() {
         if (this.builderSupplier == null) {
            throw new IllegalStateException();
         } else {
            return this.buildInternally();
         }
      }

      protected abstract ObjectCategoryDataNbtSerializer<D, DB> buildInternally();
   }

   public static final class FinalBuilder<D extends ObjectCategoryData<D>, DB extends ObjectCategoryData.Builder<D, DB>> extends Builder<D, DB, FinalBuilder<D, DB>> {
      private FinalBuilder() {
      }

      protected ObjectCategoryDataNbtSerializer<D, DB> buildInternally() {
         return new ObjectCategoryDataNbtSerializer<D, DB>(this.builderSupplier);
      }

      public static <D extends ObjectCategoryData<D>, DB extends ObjectCategoryData.Builder<D, DB>> FinalBuilder<D, DB> begin() {
         return (FinalBuilder)(new FinalBuilder()).setDefault();
      }
   }
}
