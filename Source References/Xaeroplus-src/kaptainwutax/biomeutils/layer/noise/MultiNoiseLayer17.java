package kaptainwutax.biomeutils.layer.noise;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import kaptainwutax.biomeutils.biome.Biome;
import kaptainwutax.biomeutils.biome.BiomePoint;
import kaptainwutax.biomeutils.biome.Biomes;
import kaptainwutax.biomeutils.layer.IntBiomeLayer;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.util.data.Pair;
import kaptainwutax.mcutils.version.MCVersion;
import kaptainwutax.noiseutils.noise.DoublePerlinNoiseSampler;

public class MultiNoiseLayer17 extends IntBiomeLayer {
   private final BiomePoint[] biomePoints;
   private final boolean is3D;
   private DoublePerlinNoiseSampler temperature;
   private DoublePerlinNoiseSampler humidity;
   private DoublePerlinNoiseSampler altitude;
   private DoublePerlinNoiseSampler weirdness;

   public MultiNoiseLayer17(MCVersion version, long worldSeed, boolean is3D, BiomePoint[] biomePoints) {
      super(version, (IntBiomeLayer)null);
      this.is3D = is3D;
      if (biomePoints != null) {
         this.temperature = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed), IntStream.rangeClosed(-7, -6));
         this.humidity = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed + 1L), IntStream.rangeClosed(-7, -6));
         this.altitude = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed + 2L), IntStream.rangeClosed(-7, -6));
         this.weirdness = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed + 3L), IntStream.rangeClosed(-7, -6));
      }

      this.biomePoints = biomePoints;
   }

   public MultiNoiseLayer17(MCVersion version, long worldSeed, boolean is3D, BiomePoint[] biomePoints, NoiseSettings noiseSettings) {
      super(version, (IntBiomeLayer)null);
      this.is3D = is3D;
      if (biomePoints != null) {
         this.temperature = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed), noiseSettings.temperatureNoise);
         this.humidity = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed + 1L), noiseSettings.humidityNoise);
         this.altitude = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed + 2L), noiseSettings.altitudeNoise);
         this.weirdness = new DoublePerlinNoiseSampler(new ChunkRand(worldSeed + 3L), noiseSettings.weirdnessNoise);
      }

      this.biomePoints = biomePoints;
   }

   public int sample(int x, int y, int z) {
      if (this.biomePoints == null) {
         return Biomes.THE_VOID.getId();
      } else {
         y = this.is3D ? y : 0;
         BiomePoint point = new BiomePoint((Biome)null, (float)this.temperature.sample((double)x, (double)y, (double)z), (float)this.humidity.sample((double)x, (double)y, (double)z), (float)this.altitude.sample((double)x, (double)y, (double)z), (float)this.weirdness.sample((double)x, (double)y, (double)z), 0.0F);
         return ((Biome)Stream.of(this.biomePoints).min(Comparator.comparing((m) -> m.distanceTo(point))).map(BiomePoint::getBiome).orElse(Biomes.THE_VOID)).getId();
      }
   }

   public static class NoiseSettings {
      public Pair<Integer, List<Double>> temperatureNoise;
      public Pair<Integer, List<Double>> humidityNoise;
      public Pair<Integer, List<Double>> altitudeNoise;
      public Pair<Integer, List<Double>> weirdnessNoise;

      public NoiseSettings() {
         this.setTemperature(-7, Arrays.asList((double)1.0F, (double)1.0F)).setHumidity(-7, Arrays.asList((double)1.0F, (double)1.0F)).setAltitude(-7, Arrays.asList((double)1.0F, (double)1.0F)).setWeirdness(-7, Arrays.asList((double)1.0F, (double)1.0F));
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

      public NoiseSettings setWeirdness(int firstOctaves, List<Double> amplitudes) {
         this.weirdnessNoise = new Pair<Integer, List<Double>>(firstOctaves, amplitudes);
         return this;
      }
   }
}
