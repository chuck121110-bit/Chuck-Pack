package kaptainwutax.biomeutils.layer.water;

import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.biomeutils.layer.IntBiomeLayer;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.noiseutils.perlin.PerlinNoiseSampler;
import kaptainwutax.seedutils.rand.JRand;

public class OceanTemperatureLayer extends IntBiomeLayer {
   private final PerlinNoiseSampler perlin;

   public OceanTemperatureLayer(MCVersion version, long worldSeed, long salt) {
      super(version, worldSeed, salt);
      this.perlin = new PerlinNoiseSampler(new JRand(worldSeed));
   }

   public int sample(int x, int y, int z) {
      double normalizedNoise = this.perlin.sample((double)x / (double)8.0F, (double)z / (double)8.0F, (double)0.0F, (double)0.0F, (double)0.0F);
      if (normalizedNoise > 0.4) {
         return Biomes.WARM_OCEAN.getId();
      } else if (normalizedNoise > 0.2) {
         return Biomes.LUKEWARM_OCEAN.getId();
      } else if (normalizedNoise < -0.4) {
         return Biomes.FROZEN_OCEAN.getId();
      } else {
         return normalizedNoise < -0.2 ? Biomes.COLD_OCEAN.getId() : Biomes.OCEAN.getId();
      }
   }

   public static class Apply extends IntBiomeLayer {
      public Apply(MCVersion version, long worldSeed, long salt, IntBiomeLayer... parents) {
         super(version, worldSeed, salt, parents);
      }

      public int sample(int x, int y, int z) {
         int fullStackCenter = ((IntBiomeLayer)this.getParent(0, IntBiomeLayer.class)).get(x, y, z);
         if (!Biome.isOcean(fullStackCenter)) {
            return fullStackCenter;
         } else {
            int oceanStackCenter = ((IntBiomeLayer)this.getParent(1, IntBiomeLayer.class)).get(x, y, z);

            for(int rx = -8; rx <= 8; rx += 4) {
               for(int rz = -8; rz <= 8; rz += 4) {
                  int shiftedXZ = ((IntBiomeLayer)this.getParent(0, IntBiomeLayer.class)).get(x + rx, y, z + rz);
                  if (!Biome.isOcean(shiftedXZ)) {
                     if (oceanStackCenter == Biomes.WARM_OCEAN.getId()) {
                        return Biomes.LUKEWARM_OCEAN.getId();
                     }

                     if (oceanStackCenter == Biomes.FROZEN_OCEAN.getId()) {
                        return Biomes.COLD_OCEAN.getId();
                     }
                  }
               }
            }

            if (fullStackCenter != Biomes.DEEP_OCEAN.getId()) {
               return oceanStackCenter;
            } else if (oceanStackCenter == Biomes.LUKEWARM_OCEAN.getId()) {
               return Biomes.DEEP_LUKEWARM_OCEAN.getId();
            } else if (oceanStackCenter == Biomes.OCEAN.getId()) {
               return Biomes.DEEP_OCEAN.getId();
            } else if (oceanStackCenter == Biomes.COLD_OCEAN.getId()) {
               return Biomes.DEEP_COLD_OCEAN.getId();
            } else if (oceanStackCenter == Biomes.FROZEN_OCEAN.getId()) {
               return Biomes.DEEP_FROZEN_OCEAN.getId();
            } else {
               return oceanStackCenter;
            }
         }
      }
   }
}
