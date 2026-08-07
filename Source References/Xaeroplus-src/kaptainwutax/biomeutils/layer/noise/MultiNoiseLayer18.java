package kaptainwutax.biomeutils.layer.noise;

import java.util.Arrays;
import java.util.List;
import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.biomeutils.layer.IntBiomeLayer;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.util.data.Pair;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.noiseutils.noise.DoublePerlinNoiseSampler;

public class MultiNoiseLayer18 extends IntBiomeLayer {
   private final TargetPoint[] biomePoints;
   private final boolean is3D;
   private DoublePerlinNoiseSampler temperature;
   private DoublePerlinNoiseSampler humidity;
   private DoublePerlinNoiseSampler altitude;
   private DoublePerlinNoiseSampler erosion;
   private DoublePerlinNoiseSampler weirdness;
   private DoublePerlinNoiseSampler offset;
   public static final TerrainShaper shaper = new TerrainShaper();

   public MultiNoiseLayer18(MCVersion version, long worldSeed, boolean is3D, TargetPoint[] biomePoints) {
      super(version, (IntBiomeLayer)null);
      this.is3D = is3D;
      if (biomePoints != null) {
         this.temperature = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed), new Pair(-9, Arrays.asList((double)1.0F, (double)1.0F, (double)0.0F, (double)1.0F, (double)1.0F, (double)0.0F)));
         this.humidity = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed + 1L), new Pair(-7, Arrays.asList((double)1.0F, (double)0.0F, (double)1.0F, (double)0.0F, (double)1.0F, (double)0.0F)));
         this.altitude = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed + 2L), new Pair(-9, Arrays.asList((double)1.0F, (double)1.0F, (double)2.0F, (double)2.0F, (double)2.0F, (double)1.0F, (double)1.0F, (double)1.0F, (double)1.0F)));
         this.erosion = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed + 3L), new Pair(-9, Arrays.asList((double)1.0F, (double)1.0F, (double)0.0F, (double)1.0F, (double)1.0F)));
         this.weirdness = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed + 4L), new Pair(-7, Arrays.asList((double)1.0F, (double)2.0F, (double)1.0F, (double)0.0F, (double)0.0F, (double)0.0F)));
         this.offset = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed + 5L), new Pair(-3, Arrays.asList((double)1.0F, (double)1.0F, (double)1.0F, (double)0.0F)));
      }

      this.biomePoints = biomePoints;
   }

   public MultiNoiseLayer18(MCVersion version, long worldSeed, boolean is3D, TargetPoint[] biomePoints, NoiseSettings noiseSettings) {
      super(version, (IntBiomeLayer)null);
      this.is3D = is3D;
      if (biomePoints != null) {
         this.temperature = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed), noiseSettings.temperatureNoise);
         this.humidity = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed + 1L), noiseSettings.humidityNoise);
         this.altitude = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed + 2L), noiseSettings.altitudeNoise);
         this.erosion = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed + 3L), noiseSettings.erosionNoise);
         this.altitude = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed + 4L), noiseSettings.altitudeNoise);
         this.offset = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed + 5L), noiseSettings.offsetNoise);
      }

      this.biomePoints = biomePoints;
   }

   public int sample(int x, int y, int z) {
      if (this.biomePoints == null) {
         return Biomes.THE_VOID.getId();
      } else {
         double X = (double)x + this.offset.sample((double)x, (double)0.0F, (double)z) * (double)4.0F;
         double Y = (double)y + this.offset.sample((double)y, (double)z, (double)x) * (double)4.0F;
         double Z = (double)z + this.offset.sample((double)z, (double)x, (double)0.0F) * (double)4.0F;
         float altitude = (float)this.altitude.sample(X, (double)0.0F, Z);
         float erosion = (float)this.erosion.sample(X, (double)0.0F, Z);
         float weirdness = (float)this.weirdness.sample(X, (double)0.0F, Z);
         double shape = shaper.compute(altitude, erosion, weirdness);
         float depth = (float)(computeDimensionDensity((double)1.0F, -0.51875, (double)(y * 4)) + shape);
         float temperature = (float)this.temperature.sample(X, Y, Z);
         float humidity = (float)this.humidity.sample(X, Y, Z);
         TargetPoint target = new TargetPoint(temperature, humidity, altitude, erosion, depth, weirdness);
         return this.bruteforceFinder(target, Arrays.asList()).getId();
      }
   }

   private Biome bruteforceFinder(TargetPoint target, List<BiomePoint> biomes) {
      float current = Float.MAX_VALUE;
      Biome biome = Biomes.THE_VOID;

      for(BiomePoint biomePoint : biomes) {
         float fitness = biomePoint.fitness(target);
         if (fitness < current) {
            current = fitness;
            biome = biomePoint.getBiome();
         }
      }

      return biome;
   }

   public static double computeDimensionDensity(double x, double y, double z) {
      return computeDimensionDensity(x, y, z, (double)0.0F);
   }

   public static double computeDimensionDensity(double x, double y, double z, double offset) {
      double d5 = (double)1.0F - z / (double)128.0F + offset;
      return d5 * x + y;
   }

   public static class BiomePoint {
      public final Biome biome;
      public final Pair<Float, Float> temperature;
      public final Pair<Float, Float> humidity;
      public final Pair<Float, Float> altitude;
      public final Pair<Float, Float> erosion;
      public final Pair<Float, Float> depth;
      public final Pair<Float, Float> weirdness;
      public final float offset;

      public BiomePoint(Biome biome, Pair<Float, Float> temperature, Pair<Float, Float> humidity, Pair<Float, Float> altitude, Pair<Float, Float> erosion, Pair<Float, Float> depth, Pair<Float, Float> weirdness, float offset) {
         this.biome = biome;
         this.temperature = temperature;
         this.humidity = humidity;
         this.altitude = altitude;
         this.erosion = erosion;
         this.depth = depth;
         this.weirdness = weirdness;
         this.offset = offset;
      }

      public Biome getBiome() {
         return this.biome;
      }

      float fitness(TargetPoint targetPoint) {
         return square(distance(this.temperature, targetPoint.temperature)) + square(distance(this.humidity, targetPoint.humidity)) + square(distance(this.altitude, targetPoint.altitude)) + square(distance(this.erosion, targetPoint.erosion)) + square(distance(this.depth, targetPoint.depth)) + square(distance(this.weirdness, targetPoint.weirdness)) + square(this.offset);
      }

      public static float distance(Pair<Float, Float> minMax, float f) {
         float f2 = f - (Float)minMax.getSecond();
         float f3 = (Float)minMax.getFirst() - f;
         return f2 > 0.0F ? f2 : Math.max(f3, 0.0F);
      }

      public static float square(float f) {
         return f * f;
      }
   }

   public static class TargetPoint {
      public final float temperature;
      public final float humidity;
      public final float altitude;
      public final float erosion;
      public final float depth;
      public final float weirdness;

      public TargetPoint(float temperature, float humidity, float altitude, float erosion, float depth, float weirdness) {
         this.temperature = temperature;
         this.humidity = humidity;
         this.altitude = altitude;
         this.erosion = erosion;
         this.depth = depth;
         this.weirdness = weirdness;
      }
   }

   public static class TerrainShaper {
      public double compute(float x, float y, float z) {
         z = peaksAndValleys(z);
         return (double)0.015F;
      }

      public static float peaksAndValleys(float f) {
         return -(Math.abs(Math.abs(f) - 0.6666667F) - 0.33333334F) * 3.0F;
      }
   }

   public static class NoiseSettings {
      public Pair<Integer, List<Double>> temperatureNoise;
      public Pair<Integer, List<Double>> humidityNoise;
      public Pair<Integer, List<Double>> altitudeNoise;
      public Pair<Integer, List<Double>> erosionNoise;
      public Pair<Integer, List<Double>> weirdnessNoise;
      public Pair<Integer, List<Double>> offsetNoise;

      public NoiseSettings() {
         this.setTemperature(-9, Arrays.asList((double)1.0F, (double)1.0F, (double)0.0F, (double)1.0F, (double)1.0F, (double)0.0F)).setHumidity(-7, Arrays.asList((double)1.0F, (double)0.0F, (double)1.0F, (double)0.0F, (double)1.0F, (double)0.0F)).setAltitude(-9, Arrays.asList((double)1.0F, (double)1.0F, (double)2.0F, (double)2.0F, (double)2.0F, (double)1.0F, (double)1.0F, (double)1.0F, (double)1.0F)).setErosion(-9, Arrays.asList((double)1.0F, (double)1.0F, (double)0.0F, (double)1.0F, (double)1.0F)).setWeirdness(-7, Arrays.asList((double)1.0F, (double)2.0F, (double)1.0F, (double)0.0F, (double)0.0F, (double)0.0F)).setOffset(-3, Arrays.asList((double)1.0F, (double)1.0F, (double)1.0F, (double)0.0F));
      }

      public NoiseSettings setTemperature(int firstOctaves, List<Double> amplitudes) {
         this.temperatureNoise = new Pair<Integer, List<Double>>(firstOctaves, amplitudes);
         return this;
      }

      public NoiseSettings setHumidity(int firstOctaves, List<Double> amplitudes) {
         this.humidityNoise = new Pair<Integer, List<Double>>(firstOctaves, amplitudes);
         return this;
      }

      public NoiseSettings setAltitude(int firstOctaves, List<Double> amplitudes) {
         this.altitudeNoise = new Pair<Integer, List<Double>>(firstOctaves, amplitudes);
         return this;
      }

      public NoiseSettings setErosion(int firstOctaves, List<Double> amplitudes) {
         this.erosionNoise = new Pair<Integer, List<Double>>(firstOctaves, amplitudes);
         return this;
      }

      public NoiseSettings setWeirdness(int firstOctaves, List<Double> amplitudes) {
         this.weirdnessNoise = new Pair<Integer, List<Double>>(firstOctaves, amplitudes);
         return this;
      }

      public NoiseSettings setOffset(int firstOctaves, List<Double> amplitudes) {
         this.offsetNoise = new Pair<Integer, List<Double>>(firstOctaves, amplitudes);
         return this;
      }
   }
}
