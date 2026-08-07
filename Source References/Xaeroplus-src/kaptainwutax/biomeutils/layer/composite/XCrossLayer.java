package kaptainwutax.biomeutils.layer.composite;

import kaptainwutax.biomeutils.layer.IntBiomeLayer;
import kaptainwutax.mcutils.version.MCVersion;

public abstract class XCrossLayer extends IntBiomeLayer {
   public XCrossLayer(MCVersion version, long worldSeed, long salt, IntBiomeLayer parent) {
      super(version, worldSeed, salt, parent);
   }

   public int sample(int x, int y, int z) {
      this.setSeed(x, z);
      return this.sample(((IntBiomeLayer)this.getParent(IntBiomeLayer.class)).get(x - 1, y, z + 1), ((IntBiomeLayer)this.getParent(IntBiomeLayer.class)).get(x + 1, y, z + 1), ((IntBiomeLayer)this.getParent(IntBiomeLayer.class)).get(x + 1, y, z - 1), ((IntBiomeLayer)this.getParent(IntBiomeLayer.class)).get(x - 1, y, z - 1), ((IntBiomeLayer)this.getParent(IntBiomeLayer.class)).get(x, y, z));
   }

   public abstract int sample(int var1, int var2, int var3, int var4, int var5);
}
