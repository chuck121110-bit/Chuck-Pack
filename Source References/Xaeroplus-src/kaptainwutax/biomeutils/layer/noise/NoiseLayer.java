package kaptainwutax.biomeutils.layer.noise;

import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.layer.IntBiomeLayer;
import kaptainwutax.mcutils.version.MCVersion;

public class NoiseLayer extends IntBiomeLayer {
   public NoiseLayer(MCVersion version, long worldSeed, long salt, IntBiomeLayer parent) {
      super(version, worldSeed, salt, parent);
   }

   public int sample(int x, int y, int z) {
      this.setSeed(x, z);
      int i = ((IntBiomeLayer)this.getParent(IntBiomeLayer.class)).get(x, y, z);
      return Biome.isShallowOcean(i, this.getVersion()) ? i : this.nextInt(this.getVersion().isOlderOrEqualTo(MCVersion.v1_6_4) ? 2 : 299999) + 2;
   }
}
