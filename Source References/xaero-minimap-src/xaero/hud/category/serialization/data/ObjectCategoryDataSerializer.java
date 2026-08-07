package xaero.hud.category.serialization.data;

public abstract class ObjectCategoryDataSerializer<D extends ObjectCategoryData<D>, S> {
   protected ObjectCategoryDataSerializer() {
   }

   public abstract S serialize(D var1);

   public abstract D deserialize(S var1);

   public abstract static class Builder<D extends ObjectCategoryData<D>, S> {
      public Builder<D, S> setDefault() {
         return this;
      }

      public abstract ObjectCategoryDataSerializer<D, S> build();
   }
}
