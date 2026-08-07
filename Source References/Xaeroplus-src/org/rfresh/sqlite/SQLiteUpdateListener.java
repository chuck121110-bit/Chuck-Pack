package org.rfresh.sqlite;

public interface SQLiteUpdateListener {
   void onUpdate(Type var1, String var2, String var3, long var4);

   public static enum Type {
      INSERT,
      DELETE,
      UPDATE;

      // $FF: synthetic method
      private static Type[] $values() {
         return new Type[]{INSERT, DELETE, UPDATE};
      }
   }
}
