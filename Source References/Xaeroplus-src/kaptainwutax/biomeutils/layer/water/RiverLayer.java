package kaptainwutax.biomeutils.layer.water;

import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.biomeutils.layer.IntBiomeLayer;
import kaptainwutax.mcutils.version.MCVersion;

public class RiverLayer extends IntBiomeLayer {
   public RiverLayer(MCVersion version, long worldSeed, long salt, IntBiomeLayer... parents) {
      super(version, worldSeed, salt, parents);
   }

   public int sample(int x, int y, int z) {
      int landStackCenter = ((IntBiomeLayer)this.getParent(0, IntBiomeLayer.class)).get(x, y, z);
      int riverStackCenter = ((IntBiomeLayer)this.getParent(1, IntBiomeLayer.class)).get(x, y, z);
      if (this.getVersion().isOlderOrEqualTo(MCVersion.v1_6_4)) {
         if (landStackCenter == Biomes.OCEAN.getId()) {
            return landStackCenter;
         }
      } else if (Biome.isOcean(landStackCenter)) {
         return landStackCenter;
      }

      if (riverStackCenter != Biomes.RIVER.getId()) {
         return landStackCenter;
      } else if (this.getVersion().isOlderOrEqualTo(MCVersion.vb1_8_1)) {
         return riverStackCenter;
      } else if (landStackCenter == Biomes.SNOWY_TUNDRA.getId()) {
         return Biomes.FROZEN_RIVER.getId();
      } else {
         return landStackCenter != Biomes.MUSHROOM_FIELDS.getId() && landStackCenter != Biomes.MUSHROOM_FIELD_SHORE.getId() ? riverStackCenter & 255 : Biomes.MUSHROOM_FIELD_SHORE.getId();
      }
   }
}
