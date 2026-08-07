package kaptainwutax.biomeutils.source;

import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.version.MCVersion;

public abstract class BiomeSource {
   private final MCVersion version;
   private final long worldSeed;

   public BiomeSource(MCVersion version, long worldSeed) {
      this.version = version;
      this.worldSeed = worldSeed;
   }

   public static Factory factory(Dimension dimension) {
      if (dimension == Dimension.OVERWORLD) {
         return OverworldBiomeSource::new;
      } else if (dimension == Dimension.NETHER) {
         return NetherBiomeSource::new;
      } else {
         return dimension == Dimension.END ? EndBiomeSource::new : null;
      }
   }

   public static BiomeSource of(Dimension dimension, MCVersion version, long worldSeed) {
      Factory factory = factory(dimension);
      return factory == null ? null : factory.create(version, worldSeed);
   }

   public MCVersion getVersion() {
      return this.version;
   }

   public long getWorldSeed() {
      return this.worldSeed;
   }

   public abstract Dimension getDimension();

   public abstract Biome getBiome(BPos var1);

   public abstract Biome getBiome(int var1, int var2, int var3);

   public abstract Biome getBiomeForNoiseGen(int var1, int var2, int var3);

   @FunctionalInterface
   public interface Factory {
      BiomeSource create(MCVersion var1, long var2);
   }
}
