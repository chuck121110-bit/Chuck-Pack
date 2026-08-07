package kaptainwutax.noiseutils.noise;

import kaptainwutax.seedutils.rand.JRand;

public class Noise {
   public final double originX;
   public final double originY;
   public final double originZ;
   protected final byte[] permutations = new byte[256];

   public Noise(JRand rand) {
      this.originX = rand.nextDouble() * (double)256.0F;
      this.originY = rand.nextDouble() * (double)256.0F;
      this.originZ = rand.nextDouble() * (double)256.0F;

      for(int j = 0; j < 256; ++j) {
         this.permutations[j] = (byte)j;
      }

      for(int index = 0; index < 256; ++index) {
         int randomIndex = rand.nextInt(256 - index) + index;
         byte temp = this.permutations[index];
         this.permutations[index] = this.permutations[randomIndex];
         this.permutations[randomIndex] = temp;
      }

   }

   protected int lookup(int hash) {
      return this.permutations[hash & 255] & 255;
   }
}
