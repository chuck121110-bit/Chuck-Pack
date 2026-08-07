package kaptainwutax.mcutils.rand.seed;

import kaptainwutax.mathutils.util.Mth;
import kaptainwutax.seedutils.lcg.LCG;

public class SeedMixer {
   public static final long A;
   public static final long B;
   public static final long MAGIC;
   public final long salt;
   public final int steps;

   public SeedMixer(long salt) {
      this(salt, 1);
   }

   protected SeedMixer(long salt, int steps) {
      this.salt = salt;
      this.steps = steps;
   }

   public static long getOtherSolution(long seed) {
      return MAGIC - seed;
   }

   public static long mixSeed(long seed, long salt) {
      seed *= seed * A + B;
      seed += salt;
      return seed;
   }

   public static long unmixSeed(long seed, long salt, Solution solution) {
      if ((seed - salt & 1L) == 1L) {
         throw new UnsupportedOperationException("Seed " + seed + " is unreachable with salt " + salt);
      } else {
         long r = (long)solution.ordinal();

         for(int j = 1; j < 64; j <<= 1) {
            r -= (A * r * r + B * r + salt - seed) * Mth.modInverse(2L * A * r + B, 64);
         }

         return r;
      }
   }

   public long nextSeed(long seed) {
      if (this.steps >= 0) {
         for(int i = 0; i < this.steps; ++i) {
            seed = mixSeed(seed, this.salt);
         }
      } else {
         for(int i = 0; i < -this.steps; ++i) {
            seed = unmixSeed(seed, this.salt, SeedMixer.Solution.EVEN);
         }
      }

      return seed;
   }

   public SeedMixer combine(int steps) {
      return new SeedMixer(this.salt, steps);
   }

   static {
      A = LCG.MMIX.multiplier;
      B = LCG.MMIX.addend;
      MAGIC = -B * Mth.modInverse(A);
   }

   public static enum Solution {
      EVEN,
      ODD;

      public static Solution of(long n) {
         return values()[(int)(n & 1L)];
      }

      // $FF: synthetic method
      private static Solution[] $values() {
         return new Solution[]{EVEN, ODD};
      }
   }
}
