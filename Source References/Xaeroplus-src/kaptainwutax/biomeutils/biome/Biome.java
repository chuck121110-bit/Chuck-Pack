package kaptainwutax.biomeutils.biome;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import kaptainwutax.biomeutils.source.OverworldBiomeSource;
import kaptainwutax.mcutils.state.Dimension;
import kaptainwutax.mcutils.version.MCVersion;

public class Biome {
   private final MCVersion version;
   private final Dimension dimension;
   private final int id;
   private final String name;
   private final Category category;
   private final Precipitation precipitation;
   private final float temperature;
   private final float scale;
   private final float depth;
   private final Biome parent;
   private Biome child;

   public Biome(MCVersion version, Dimension dimension, int id, String name, Category category, Precipitation precipitation, float temperature, float scale, float depth, Biome parent) {
      this.version = version;
      this.dimension = dimension;
      this.id = id;
      this.name = name;
      this.category = category;
      this.precipitation = precipitation;
      this.temperature = temperature;
      this.scale = scale;
      this.depth = depth;
      this.parent = parent;
      if (this.parent != null) {
         this.parent.child = this;
      }

   }

   public MCVersion getVersion() {
      return this.version;
   }

   public Dimension getDimension() {
      return this.dimension;
   }

   public int getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public Category getCategory() {
      return this.category;
   }

   public Precipitation getPrecipitation() {
      return this.precipitation;
   }

   public float getTemperature() {
      return this.temperature;
   }

   public Temperature getTemperatureGroup() {
      if (this.category == Biome.Category.OCEAN) {
         return Biome.Temperature.OCEAN;
      } else if (this.getTemperature() < 0.2F) {
         return Biome.Temperature.COLD;
      } else {
         return this.getTemperature() < 1.0F ? Biome.Temperature.MEDIUM : Biome.Temperature.WARM;
      }
   }

   public float getScale() {
      return this.scale;
   }

   public float getDepth() {
      return this.depth;
   }

   public boolean hasParent() {
      return this.parent != null;
   }

   public Biome getParent() {
      return this.parent;
   }

   public boolean hasChild() {
      return this.child != null;
   }

   public Biome getChild() {
      return this.child;
   }

   public static boolean isShallowOcean(int id, MCVersion version) {
      if (version.isOlderThan(MCVersion.v1_13)) {
         return id == Biomes.OCEAN.getId();
      } else {
         return id == Biomes.OCEAN.getId() || id == Biomes.WARM_OCEAN.getId() || id == Biomes.LUKEWARM_OCEAN.getId() || id == Biomes.COLD_OCEAN.getId() || id == Biomes.FROZEN_OCEAN.getId();
      }
   }

   public static boolean isOcean(int id) {
      return id == Biomes.WARM_OCEAN.getId() || id == Biomes.LUKEWARM_OCEAN.getId() || id == Biomes.OCEAN.getId() || id == Biomes.COLD_OCEAN.getId() || id == Biomes.FROZEN_OCEAN.getId() || id == Biomes.DEEP_WARM_OCEAN.getId() || id == Biomes.DEEP_LUKEWARM_OCEAN.getId() || id == Biomes.DEEP_OCEAN.getId() || id == Biomes.DEEP_COLD_OCEAN.getId() || id == Biomes.DEEP_FROZEN_OCEAN.getId();
   }

   public static boolean isRiver(int id) {
      return id == Biomes.RIVER.getId() || id == Biomes.FROZEN_RIVER.getId();
   }

   public static boolean areSimilar(int id, Biome b2, MCVersion version) {
      if (b2 == null) {
         return false;
      } else if (id == b2.getId()) {
         return true;
      } else {
         Biome b = (Biome)Biomes.REGISTRY.get(id);
         if (b == null) {
            return false;
         } else if (version.isNewerOrEqualTo(MCVersion.v1_16_2)) {
            if (b != Biomes.WOODED_BADLANDS_PLATEAU && b != Biomes.BADLANDS_PLATEAU) {
               if (b2 != Biomes.WOODED_BADLANDS_PLATEAU && b2 != Biomes.BADLANDS_PLATEAU) {
                  return b.getCategory() == b2.getCategory();
               } else {
                  return false;
               }
            } else {
               return b2 == Biomes.WOODED_BADLANDS_PLATEAU || b2 == Biomes.BADLANDS_PLATEAU;
            }
         } else if (id != Biomes.WOODED_BADLANDS_PLATEAU.getId() && id != Biomes.BADLANDS_PLATEAU.getId()) {
            if (b.getCategory() != Biome.Category.NONE && b2.getCategory() != Biome.Category.NONE && b.getCategory() == b2.getCategory()) {
               return true;
            } else {
               return b == b2;
            }
         } else {
            return b2 == Biomes.WOODED_BADLANDS_PLATEAU || b2 == Biomes.BADLANDS_PLATEAU;
         }
      }
   }

   public static boolean applyAll(Function<Integer, Boolean> function, int... ints) {
      for(int i : ints) {
         if (!(Boolean)function.apply(i)) {
            return false;
         }
      }

      return true;
   }

   public static int equalsOrDefault(int comparator, int comparable, int fallback) {
      return comparator == comparable ? comparable : fallback;
   }

   public Data at(int x, int z) {
      return new Data(this, x, z);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Biome)) {
         return false;
      } else {
         Biome biome = (Biome)o;
         return this.id == biome.id;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.id});
   }

   public String toString() {
      int var10000 = this.id;
      return "Biome{id=" + var10000 + ", name='" + this.name + "', category=" + String.valueOf(this.category) + ", precipitation=" + String.valueOf(this.precipitation) + ", temperature=" + this.temperature + ", scale=" + this.scale + ", depth=" + this.depth + ", parent=" + (this.parent == null ? null : this.parent.name) + ", dimension=" + String.valueOf(this.dimension) + ", child=" + (this.child == null ? null : this.child.name) + "}";
   }

   public static enum Category {
      NONE("none"),
      TAIGA("taiga"),
      EXTREME_HILLS("extreme_hills"),
      JUNGLE("jungle"),
      MESA("mesa"),
      BADLANDS_PLATEAU("badlands_plateau"),
      PLAINS("plains"),
      SAVANNA("savanna"),
      ICY("icy"),
      THE_END("the_end"),
      BEACH("beach"),
      FOREST("forest"),
      OCEAN("ocean"),
      DESERT("desert"),
      RIVER("river"),
      SWAMP("swamp"),
      MUSHROOM("mushroom"),
      NETHER("nether");

      private final String name;

      private Category(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      // $FF: synthetic method
      private static Category[] $values() {
         return new Category[]{NONE, TAIGA, EXTREME_HILLS, JUNGLE, MESA, BADLANDS_PLATEAU, PLAINS, SAVANNA, ICY, THE_END, BEACH, FOREST, OCEAN, DESERT, RIVER, SWAMP, MUSHROOM, NETHER};
      }
   }

   public static enum Temperature {
      OCEAN("ocean"),
      COLD("cold"),
      MEDIUM("medium"),
      WARM("warm");

      private final String name;

      private Temperature(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      // $FF: synthetic method
      private static Temperature[] $values() {
         return new Temperature[]{OCEAN, COLD, MEDIUM, WARM};
      }
   }

   public static enum Precipitation {
      NONE("none"),
      RAIN("rain"),
      SNOW("snow");

      private final String name;

      private Precipitation(String name) {
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      // $FF: synthetic method
      private static Precipitation[] $values() {
         return new Precipitation[]{NONE, RAIN, SNOW};
      }
   }

   public static class Data {
      public final Predicate<Biome> predicate;
      public final Biome biome;
      public final int x;
      public final int z;

      public Data(Biome biome, int x, int z) {
         this((b) -> b == biome, biome, x, z);
      }

      public Data(Predicate<Biome> predicate, int x, int z) {
         this(predicate, (Biome)null, x, z);
      }

      protected Data(Predicate<Biome> predicate, Biome biome, int x, int z) {
         this.predicate = predicate;
         this.biome = biome;
         this.x = x;
         this.z = z;
      }

      public boolean test(OverworldBiomeSource source) {
         return this.predicate.test(source.getBiome(this.x, 0, this.z));
      }
   }
}
