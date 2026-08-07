package xaero.hud.nbt.util;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.class_2487;
import net.minecraft.class_2495;
import net.minecraft.class_4844;

public class XaeroNbtUtils {
   public static Optional<UUID> getUUID(class_2487 tag, String id) {
      int[] intArray = (int[])tag.method_10561(id).orElse((Object)null);
      if (intArray == null) {
         long[] longArray = (long[])tag.method_10565(id).orElse((Object)null);
         return longArray != null && longArray.length == 2 ? Optional.of(new UUID(longArray[0], longArray[1])) : Optional.empty();
      } else {
         return intArray.length != 4 ? Optional.empty() : Optional.of(class_4844.method_26276(intArray));
      }
   }

   public static void putUUID(class_2487 tag, String id, UUID uuid) {
      tag.method_10539(id, class_4844.method_26275(uuid));
   }

   public static void putUUIDAsLongArray(class_2487 tag, String id, UUID uuid) {
      tag.method_10564(id, new long[]{uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()});
   }

   public static class_2495 createUUIDTag(UUID partyId) {
      return new class_2495(class_4844.method_26275(partyId));
   }
}
