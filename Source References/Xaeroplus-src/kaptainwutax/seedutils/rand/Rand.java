package kaptainwutax.seedutils.rand;

import kaptainwutax.mathutils.util.Mth;
import kaptainwutax.seedutils.lcg.LCG;

public class Rand {
   private final LCG lcg;
   private long seed;

   protected Rand(LCG lcg) {
      this.lcg = lcg;
   }

   public Rand(LCG lcg, long seed) {
      this(lcg);
      this.setSeed(seed);
   }

   public long getSeed() {
      return this.seed;
   }

   public void setSeed(long seed) {
      this.seed = seed;
   }

   public LCG getLcg() {
      return this.lcg;
   }

   public long nextSeed() {
      return this.seed = this.lcg.nextSeed(this.seed);
   }

   public long nextBits(int bits) {
      this.seed = this.nextSeed();
      return this.lcg.isModPowerOf2() ? this.seed >>> this.lcg.getModTrailingZeroes() - bits : this.seed / Mth.getPow2(bits);
   }

   public void advance(long calls) {
      this.advance(this.lcg.combine(calls));
   }

   public void advance(LCG skip) {
      this.seed = skip.nextSeed(this.seed);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Rand)) {
         return false;
      } else {
         Rand rand = (Rand)o;
         return this.getSeed() == rand.getSeed() && this.lcg.equals(rand.lcg);
      }
   }

   public int hashCode() {
      return (int)((long)this.lcg.hashCode() + this.seed);
   }

   public String toString() {
      String var10000 = String.valueOf(this.lcg);
      return "Rand{lcg=" + var10000 + ", seed=" + this.seed + "}";
   }
}
