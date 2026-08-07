package kaptainwutax.mcutils.rand.seed;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Predicate;
import kaptainwutax.mcutils.util.data.StringUnhasher;

public final class WorldSeed {
   public static boolean isString(long worldSeed) {
      return (long)((int)worldSeed) == worldSeed;
   }

   public static void toString(long worldSeed, StringUnhasher.Config config, Predicate<String> continueSearching) {
      if (isString(worldSeed)) {
         StringUnhasher.unhash((int)worldSeed, config, continueSearching);
      }

   }

   public static long toHash(long worldSeed) {
      MessageDigest digest;
      try {
         digest = MessageDigest.getInstance("SHA-256");
      } catch (NoSuchAlgorithmException e) {
         e.printStackTrace();
         return worldSeed;
      }

      byte[] bytes = new byte[8];

      for(int i = 0; i < 8; ++i) {
         bytes[i] = (byte)((int)(worldSeed & 255L));
         worldSeed >>>= 8;
      }

      bytes = digest.digest(bytes);
      long hashedWorldSeed = (long)bytes[0] & 255L;

      for(int i = 1; i < 8; ++i) {
         hashedWorldSeed |= ((long)bytes[i] & 255L) << (i << 3);
      }

      return hashedWorldSeed;
   }
}
