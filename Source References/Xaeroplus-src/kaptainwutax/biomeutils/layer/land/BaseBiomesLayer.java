package kaptainwutax.biomeutils.layer.land;

import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.biomeutils.layer.IntBiomeLayer;
import kaptainwutax.mcutils.version.MCVersion;

public class BaseBiomesLayer extends IntBiomeLayer {
   public boolean useDefault1_1 = false;
   public static final Biome[] DRY_BIOMES;
   public static final Biome[] TEMPERATE_BIOMES;
   public static final Biome[] COOL_BIOMES;
   public static final Biome[] SNOWY_BIOMES;
   public static final Biome[] OLD_BIOMES;
   public static final Biome[] OLD_BIOMES_DEFAULT_1_1;

   public BaseBiomesLayer(MCVersion version, long worldSeed, long salt, IntBiomeLayer parent) {
      super(version, worldSeed, salt, parent);
   }

   public BaseBiomesLayer setDefault1_1(boolean useDefault1_1) {
      this.useDefault1_1 = useDefault1_1;
      return this;
   }

   public int sample(int x, int y, int z) {
      this.setSeed(x, z);
      int center = ((IntBiomeLayer)this.getParent(IntBiomeLayer.class)).get(x, y, z);
      int specialBits = center >> 8 & 15;
      center &= -3841;
      if (this.getVersion().isOlderOrEqualTo(MCVersion.v1_6_4)) {
         return this.sampleOld(center);
      } else if (!Biome.isOcean(center) && center != Biomes.MUSHROOM_FIELDS.getId()) {
         if (center == Biomes.PLAINS.getId()) {
            if (specialBits > 0) {
               return this.nextInt(3) == 0 ? Biomes.BADLANDS_PLATEAU.getId() : Biomes.WOODED_BADLANDS_PLATEAU.getId();
            } else {
               return this.useDefault1_1 ? OLD_BIOMES_DEFAULT_1_1[this.nextInt(OLD_BIOMES_DEFAULT_1_1.length)].getId() : DRY_BIOMES[this.nextInt(DRY_BIOMES.length)].getId();
            }
         } else if (center == Biomes.DESERT.getId()) {
            return specialBits > 0 ? Biomes.JUNGLE.getId() : TEMPERATE_BIOMES[this.nextInt(TEMPERATE_BIOMES.length)].getId();
         } else if (center == Biomes.MOUNTAINS.getId()) {
            return specialBits > 0 ? Biomes.GIANT_TREE_TAIGA.getId() : COOL_BIOMES[this.nextInt(COOL_BIOMES.length)].getId();
         } else {
            return center == Biomes.FOREST.getId() ? SNOWY_BIOMES[this.nextInt(SNOWY_BIOMES.length)].getId() : Biomes.MUSHROOM_FIELDS.getId();
         }
      } else {
         return center;
      }
   }

   private int sampleOld(int center) {
      if (Biome.isShallowOcean(center, this.getVersion())) {
         return Biomes.OCEAN.getId();
      } else if (center == Biomes.MUSHROOM_FIELDS.getId()) {
         return Biomes.MUSHROOM_FIELDS.getId();
      } else {
         Biome[] biomeList = this.getVersion().isOlderOrEqualTo(MCVersion.v1_1) ? OLD_BIOMES_DEFAULT_1_1 : OLD_BIOMES;
         Biome oldBiome = biomeList[this.nextInt(biomeList.length)];
         if (this.getVersion().isOlderOrEqualTo(MCVersion.vb1_8_1)) {
            return oldBiome.getId();
         } else if (center == Biomes.PLAINS.getId()) {
            return oldBiome.getId();
         } else if (this.getVersion().isOlderOrEqualTo(MCVersion.v1_2_5)) {
            return Biomes.SNOWY_TUNDRA.getId();
         } else {
            return oldBiome == Biomes.TAIGA ? Biomes.TAIGA.getId() : Biomes.SNOWY_TUNDRA.getId();
         }
      }
   }

   static {
      DRY_BIOMES = new Biome[]{Biomes.DESERT, Biomes.DESERT, Biomes.DESERT, Biomes.SAVANNA, Biomes.SAVANNA, Biomes.PLAINS};
      TEMPERATE_BIOMES = new Biome[]{Biomes.FOREST, Biomes.DARK_FOREST, Biomes.MOUNTAINS, Biomes.PLAINS, Biomes.BIRCH_FOREST, Biomes.SWAMP};
      COOL_BIOMES = new Biome[]{Biomes.FOREST, Biomes.MOUNTAINS, Biomes.TAIGA, Biomes.PLAINS};
      SNOWY_BIOMES = new Biome[]{Biomes.SNOWY_TUNDRA, Biomes.SNOWY_TUNDRA, Biomes.SNOWY_TUNDRA, Biomes.SNOWY_TAIGA};
      OLD_BIOMES = new Biome[]{Biomes.DESERT, Biomes.FOREST, Biomes.MOUNTAINS, Biomes.SWAMP, Biomes.PLAINS, Biomes.TAIGA, Biomes.JUNGLE};
      OLD_BIOMES_DEFAULT_1_1 = new Biome[]{Biomes.DESERT, Biomes.FOREST, Biomes.MOUNTAINS, Biomes.SWAMP, Biomes.PLAINS, Biomes.TAIGA};
   }
}
