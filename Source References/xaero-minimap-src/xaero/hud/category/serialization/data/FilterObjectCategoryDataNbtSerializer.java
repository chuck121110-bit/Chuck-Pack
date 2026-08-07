package xaero.hud.category.serialization.data;

import java.util.function.Supplier;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2519;
import net.minecraft.class_2520;
import xaero.hud.category.rule.ExcludeListMode;

public class FilterObjectCategoryDataNbtSerializer<D extends FilterObjectCategoryData<D>, DB extends FilterObjectCategoryData.Builder<D, DB>> extends ObjectCategoryDataNbtSerializer<D, DB> {
   protected FilterObjectCategoryDataNbtSerializer(Supplier<DB> builderSupplier) {
      super(builderSupplier);
   }

   public class_2487 serialize(D data) {
      class_2487 resultTag = super.serialize(data);
      resultTag.method_10582("h", data.getHardInclude());
      resultTag.method_10556("i", data.getIncludeListInSuperCategory());
      resultTag.method_10582("m", data.getExcludeMode().name());
      class_2499 includeListTag = new class_2499();
      data.getIncludeListIterator().forEachRemaining((includeEntry) -> includeListTag.add(class_2519.method_23256(includeEntry)));
      resultTag.method_10566("l", includeListTag);
      class_2499 excludeListTag = new class_2499();
      data.getExcludeListIterator().forEachRemaining((excludeEntry) -> excludeListTag.add(class_2519.method_23256(excludeEntry)));
      resultTag.method_10566("e", excludeListTag);
      return resultTag;
   }

   protected DB getConfiguredBuilder(class_2487 serializedData) {
      DB builder = (DB)(super.getConfiguredBuilder(serializedData));
      builder.setHardInclude(serializedData.method_68564("h", ""));
      ((FilterObjectCategoryData.Builder)builder).setIncludeListInSuperCategory(serializedData.method_68566("i", false));
      builder.setExcludeMode(ExcludeListMode.valueOf(serializedData.method_68564("m", "")));

      for(class_2520 includeEntryTag : serializedData.method_68569("l")) {
         ((FilterObjectCategoryData.Builder)builder).addToIncludeList((String)includeEntryTag.method_68658().get());
      }

      for(class_2520 excludeEntryTag : serializedData.method_68569("e")) {
         ((FilterObjectCategoryData.Builder)builder).addToExcludeList((String)excludeEntryTag.method_68658().get());
      }

      return builder;
   }

   public abstract static class Builder<D extends FilterObjectCategoryData<D>, DB extends FilterObjectCategoryData.Builder<D, DB>, B extends Builder<D, DB, B>> extends ObjectCategoryDataNbtSerializer.Builder<D, DB, B> {
      protected Builder() {
      }

      public FilterObjectCategoryDataNbtSerializer<D, DB> build() {
         return (FilterObjectCategoryDataNbtSerializer)super.build();
      }
   }

   public static final class FinalBuilder<D extends FilterObjectCategoryData<D>, DB extends FilterObjectCategoryData.Builder<D, DB>> extends Builder<D, DB, FinalBuilder<D, DB>> {
      private FinalBuilder() {
      }

      protected FilterObjectCategoryDataNbtSerializer<D, DB> buildInternally() {
         return new FilterObjectCategoryDataNbtSerializer<D, DB>(this.builderSupplier);
      }

      public static <D extends FilterObjectCategoryData<D>, DB extends FilterObjectCategoryData.Builder<D, DB>> FinalBuilder<D, DB> begin() {
         return (FinalBuilder)(new FinalBuilder()).setDefault();
      }
   }
}
