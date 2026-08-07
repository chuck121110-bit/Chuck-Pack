package kaptainwutax.biomeutils.source;

import java.util.function.BiFunction;
import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.biomeutils.layer.IntBiomeLayer;
import kaptainwutax.biomeutils.layer.composite.VoronoiLayer;
import kaptainwutax.biomeutils.layer.land.BambooJungleLayer;
import kaptainwutax.biomeutils.layer.land.BaseBiomesLayer;
import kaptainwutax.biomeutils.layer.land.ContinentLayer;
import kaptainwutax.biomeutils.layer.land.HillsLayer;
import kaptainwutax.biomeutils.layer.land.IslandLayer;
import kaptainwutax.biomeutils.layer.land.LandLayer;
import kaptainwutax.biomeutils.layer.land.MushroomLayer;
import kaptainwutax.biomeutils.layer.land.SunflowerPlainsLayer;
import kaptainwutax.biomeutils.layer.noise.NoiseLayer;
import kaptainwutax.biomeutils.layer.scale.ScaleLayer;
import kaptainwutax.biomeutils.layer.scale.SmoothScaleLayer;
import kaptainwutax.biomeutils.layer.shore.EaseEdgeLayer;
import kaptainwutax.biomeutils.layer.shore.EdgeBiomesLayer;
import kaptainwutax.biomeutils.layer.temperature.ClimateLayer;
import kaptainwutax.biomeutils.layer.water.DeepOceanLayer;
import kaptainwutax.biomeutils.layer.water.NoiseToRiverLayer;
import kaptainwutax.biomeutils.layer.water.OceanTemperatureLayer;
import kaptainwutax.biomeutils.layer.water.OldRiverInBiomes;
import kaptainwutax.biomeutils.layer.water.RiverLayer;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.util.pos.BPos;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.mcutils.version.UnsupportedVersion;

public class OverworldBiomeSource extends LayeredBiomeSource<IntBiomeLayer> {
   public final int biomeSize;
   public final int riverSize;
   public final boolean useDefault1_1;
   public static final int DEFAULT_BIOME_SIZE = 4;
   public static final int DEFAULT_RIVER_SIZE = 4;
   public IntBiomeLayer base;
   public IntBiomeLayer ocean;
   public IntBiomeLayer noise;
   public IntBiomeLayer variants;
   public IntBiomeLayer biomes;
   public IntBiomeLayer river;
   public IntBiomeLayer full;
   public VoronoiLayer voronoi;
   public IntBiomeLayer debug;

   public OverworldBiomeSource(MCVersion version, long worldSeed) {
      this(version, worldSeed, 4, 4);
   }

   public OverworldBiomeSource(MCVersion version, long worldSeed, int biomeSize, int riverSize) {
      this(version, worldSeed, biomeSize, riverSize, false);
   }

   public OverworldBiomeSource(MCVersion version, long worldSeed, int biomeSize, int riverSize, boolean useDefault1_1) {
      super(version, worldSeed);
      if (this.getVersion().isOlderThan(MCVersion.vb1_8_1)) {
         throw new UnsupportedVersion(this.getVersion(), "overworld biomes");
      } else {
         if (this.getVersion().isOlderThan(MCVersion.vb1_8_1)) {
            System.out.println("WARNING USING TEMPORARY BIOME STACK (NOT VERIFIED)");
         }

         this.biomeSize = biomeSize;
         this.riverSize = riverSize;
         this.useDefault1_1 = useDefault1_1;
         this.build();
      }
   }

   public Dimension getDimension() {
      return Dimension.OVERWORLD;
   }

   protected void build() {
      BiFunction<Long, IntBiomeLayer, IntBiomeLayer> NORMAL_SCALE = (salt, parent) -> new ScaleLayer(this.getVersion(), this.getWorldSeed(), salt, ScaleLayer.Type.NORMAL, parent);
      this.layers.add(this.base = new ContinentLayer(this.getVersion(), this.getWorldSeed(), 1L));
      this.layers.add(this.base = new ScaleLayer(this.getVersion(), this.getWorldSeed(), 2000L, ScaleLayer.Type.FUZZY, this.base));
      this.layers.add(this.base = new LandLayer(this.getVersion(), this.getWorldSeed(), 1L, this.base));
      this.layers.add(this.base = new ScaleLayer(this.getVersion(), this.getWorldSeed(), 2001L, ScaleLayer.Type.NORMAL, this.base));
      this.layers.add(this.base = new LandLayer(this.getVersion(), this.getWorldSeed(), 2L, this.base));
      if (this.getVersion().isNewerOrEqualTo(MCVersion.v1_7_2)) {
         this.layers.add(this.base = new LandLayer(this.getVersion(), this.getWorldSeed(), 50L, this.base));
         this.layers.add(this.base = new LandLayer(this.getVersion(), this.getWorldSeed(), 70L, this.base));
         this.layers.add(this.base = new IslandLayer(this.getVersion(), this.getWorldSeed(), 2L, this.base));
      }

      if (this.getVersion().isNewerOrEqualTo(MCVersion.v1_0)) {
         this.layers.add(this.base = new ClimateLayer.Cold(this.getVersion(), this.getWorldSeed(), 2L, this.base));
      }

      if (this.getVersion().isNewerOrEqualTo(MCVersion.v1_7_2)) {
         this.layers.add(this.base = new LandLayer(this.getVersion(), this.getWorldSeed(), 3L, this.base));
         this.layers.add(this.base = new ClimateLayer.Temperate(this.getVersion(), this.getWorldSeed(), 2L, this.base));
         this.layers.add(this.base = new ClimateLayer.Cool(this.getVersion(), this.getWorldSeed(), 2L, this.base));
         this.layers.add(this.base = new ClimateLayer.Special(this.getVersion(), this.getWorldSeed(), 3L, this.base));
      }

      this.layers.add(this.base = new ScaleLayer(this.getVersion(), this.getWorldSeed(), 2002L, ScaleLayer.Type.NORMAL, this.base));
      if (this.getVersion().isOlderOrEqualTo(MCVersion.v1_6_4)) {
         this.layers.add(this.base = new LandLayer(this.getVersion(), this.getWorldSeed(), 3L, this.base));
      }

      this.layers.add(this.base = new ScaleLayer(this.getVersion(), this.getWorldSeed(), 2003L, ScaleLayer.Type.NORMAL, this.base));
      this.layers.add(this.base = new LandLayer(this.getVersion(), this.getWorldSeed(), this.getVersion().isNewerOrEqualTo(MCVersion.v1_0) ? 4L : 3L, this.base));
      if (this.getVersion().isNewerOrEqualTo(MCVersion.v1_0)) {
         this.layers.add(this.base = new MushroomLayer(this.getVersion(), this.getWorldSeed(), 5L, this.base));
      } else {
         this.layers.add(this.base = new ScaleLayer(this.getVersion(), this.getWorldSeed(), 2004L, ScaleLayer.Type.NORMAL, this.base));
         this.layers.add(this.base = new LandLayer(this.getVersion(), this.getWorldSeed(), 3L, this.base));
      }

      if (this.getVersion().isNewerOrEqualTo(MCVersion.v1_7_2)) {
         this.layers.add(this.base = new DeepOceanLayer(this.getVersion(), this.getWorldSeed(), 4L, this.base));
      }

      this.layers.add(this.biomes = (new BaseBiomesLayer(this.getVersion(), this.getWorldSeed(), 200L, this.base)).setDefault1_1(this.useDefault1_1));
      if (this.getVersion().isNewerOrEqualTo(MCVersion.v1_14)) {
         this.layers.add(this.biomes = new BambooJungleLayer(this.getVersion(), this.getWorldSeed(), 1001L, this.biomes));
      }

      this.biomes = this.stack(1000L, NORMAL_SCALE, this.biomes, 2);
      if (this.getVersion().isNewerOrEqualTo(MCVersion.v1_7_2)) {
         this.layers.add(this.biomes = new EaseEdgeLayer(this.getVersion(), this.getWorldSeed(), 1000L, this.biomes));
      }

      this.layers.add(this.noise = new NoiseLayer(this.getVersion(), this.getWorldSeed(), 100L, this.base));
      if (this.getVersion().isOlderThan(MCVersion.v1_13)) {
         this.river = this.stack(1000L, NORMAL_SCALE, this.noise, 2);
         this.layers.add(this.noise = new ScaleLayer(this.getVersion(), 0L, 0L, ScaleLayer.Type.NORMAL, this.noise));
         this.layers.add(this.noise = new ScaleLayer(this.getVersion(), 0L, 0L, ScaleLayer.Type.NORMAL, this.noise));
      } else {
         this.layers.add(this.noise = new ScaleLayer(this.getVersion(), this.getWorldSeed(), 1000L, ScaleLayer.Type.NORMAL, this.noise));
         this.layers.add(this.noise = new ScaleLayer(this.getVersion(), this.getWorldSeed(), 1001L, ScaleLayer.Type.NORMAL, this.noise));
      }

      if (this.getVersion().isNewerOrEqualTo(MCVersion.v1_1)) {
         this.layers.add(this.variants = new HillsLayer(this.getVersion(), this.getWorldSeed(), 1000L, new IntBiomeLayer[]{this.biomes, this.noise}));
      } else {
         this.variants = this.biomes;
      }

      if (this.getVersion().isNewerOrEqualTo(MCVersion.v1_7_2)) {
         this.layers.add(this.variants = new SunflowerPlainsLayer(this.getVersion(), this.getWorldSeed(), 1001L, this.variants));
      }

      for(int i = 0; i < this.biomeSize; ++i) {
         this.layers.add(this.variants = new ScaleLayer(this.getVersion(), this.getWorldSeed(), 1000L + (long)i, ScaleLayer.Type.NORMAL, this.variants));
         if (i == 0) {
            this.layers.add(this.variants = new LandLayer(this.getVersion(), this.getWorldSeed(), 3L, this.variants));
         }

         if (this.getVersion().isNewerOrEqualTo(MCVersion.v1_1) && (i == 1 || this.biomeSize == 1 && this.getVersion().isNewerOrEqualTo(MCVersion.v1_8)) || this.getVersion().isOlderOrEqualTo(MCVersion.v1_0) && i == 0 && this.getVersion().isNewerOrEqualTo(MCVersion.v1_0)) {
            this.layers.add(this.variants = new EdgeBiomesLayer(this.getVersion(), this.getWorldSeed(), 1000L, this.variants));
         }

         if (i == 1 && this.getVersion().isBetween(MCVersion.v1_1, MCVersion.v1_6_4)) {
            this.layers.add(this.variants = new OldRiverInBiomes(this.getVersion(), this.getWorldSeed(), 1000L, this.variants));
         }
      }

      this.layers.add(this.variants = new SmoothScaleLayer(this.getVersion(), this.getWorldSeed(), 1000L, this.variants));
      this.river = this.stack(this.getVersion().isNewerOrEqualTo(MCVersion.v1_7_2) ? 1000L : 1002L, NORMAL_SCALE, this.getVersion().isOlderThan(MCVersion.v1_13) ? this.river : this.noise, this.getVersion().isNewerOrEqualTo(MCVersion.v1_8) ? this.riverSize : this.biomeSize);
      this.layers.add(this.river = new NoiseToRiverLayer(this.getVersion(), this.getWorldSeed(), 1L, this.river));
      this.layers.add(this.river = new SmoothScaleLayer(this.getVersion(), this.getWorldSeed(), 1000L, this.river));
      this.layers.add(this.full = new RiverLayer(this.getVersion(), this.getWorldSeed(), 100L, new IntBiomeLayer[]{this.variants, this.river}));
      if (this.getVersion().isNewerOrEqualTo(MCVersion.v1_13)) {
         this.layers.add(this.ocean = new OceanTemperatureLayer(this.getVersion(), this.getWorldSeed(), 2L));
         this.ocean = this.stack(2001L, NORMAL_SCALE, this.ocean, 6);
         this.layers.add(this.full = new OceanTemperatureLayer.Apply(this.getVersion(), this.getWorldSeed(), 100L, new IntBiomeLayer[]{this.full, this.ocean}));
      }

      this.layers.add(this.voronoi = new VoronoiLayer(this.getVersion(), this.getWorldSeed(), false, this.full));
      this.layers.setScales();
   }

   public IntBiomeLayer stack(long salt, BiFunction<Long, IntBiomeLayer, IntBiomeLayer> layer, IntBiomeLayer parent, int count) {
      for(int i = 0; i < count; ++i) {
         this.layers.add(parent = (IntBiomeLayer)layer.apply(salt + (long)i, parent));
      }

      return parent;
   }

   public Biome getBiome(BPos bpos) {
      return (Biome)Biomes.REGISTRY.get(this.voronoi.get(bpos.getX(), 0, bpos.getZ()));
   }

   public Biome getBiome(int x, int y, int z) {
      return (Biome)Biomes.REGISTRY.get(this.voronoi.get(x, 0, z));
   }

   public Biome getBiomeForNoiseGen(int x, int y, int z) {
      return (Biome)Biomes.REGISTRY.get(this.full.get(x, 0, z));
   }
}
