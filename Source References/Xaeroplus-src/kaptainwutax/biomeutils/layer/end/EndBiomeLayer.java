package kaptainwutax.biomeutils.layer.end;

import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.biomeutils.layer.FloatBiomeLayer;
import kaptainwutax.biomeutils.layer.IntBiomeLayer;
import kaptainwutax.mcutils.version.MCVersion;

public class EndBiomeLayer extends IntBiomeLayer {
   public EndBiomeLayer(MCVersion version, FloatBiomeLayer parent) {
      super(version, parent);
   }

   public int sample(int x, int y, int z) {
      x >>= 2;
      z >>= 2;
      if ((long)x * (long)x + (long)z * (long)z <= 4096L) {
         return Biomes.THE_END.getId();
      } else {
         float height = ((FloatBiomeLayer)this.getParent(FloatBiomeLayer.class)).get(x * 2 + 1, 0, z * 2 + 1);
         if (height > 40.0F) {
            return Biomes.END_HIGHLANDS.getId();
         } else if (height >= 0.0F) {
            return Biomes.END_MIDLANDS.getId();
         } else {
            return height >= -20.0F ? Biomes.END_BARRENS.getId() : Biomes.SMALL_END_ISLANDS.getId();
         }
      }
   }
}
