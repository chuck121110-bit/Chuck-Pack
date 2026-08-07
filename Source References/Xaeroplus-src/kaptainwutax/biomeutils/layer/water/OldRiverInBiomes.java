package kaptainwutax.biomeutils.layer.water;

import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.biomeutils.layer.IntBiomeLayer;
import kaptainwutax.mcutils.version.MCVersion;

public class OldRiverInBiomes extends IntBiomeLayer {
   public OldRiverInBiomes(MCVersion version, long worldSeed, long salt, IntBiomeLayer parent) {
      super(version, worldSeed, salt, parent);
   }

   public int sample(int x, int y, int z) {
      this.setSeed(x, z);
      int center = ((IntBiomeLayer)this.getParent(IntBiomeLayer.class)).get(x, y, z);
      return center == Biomes.SWAMP.getId() && this.nextInt(6) == 0 || (center == Biomes.JUNGLE.getId() || center == Biomes.JUNGLE_HILLS.getId()) && this.nextInt(8) == 0 ? Biomes.RIVER.getId() : center;
   }
}
