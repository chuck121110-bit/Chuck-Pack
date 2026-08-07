package kaptainwutax.noiseutils.perlin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import kaptainwutax.mcutils.util.data.Pair;
import kaptainwutax.mcutils.util.data.Quad;
import kaptainwutax.noiseutils.noise.NoiseSampler;
import kaptainwutax.noiseutils.utils.MathHelper;
import kaptainwutax.seedutils.rand.JRand;

public class OctavePerlinNoiseSampler implements NoiseSampler {
   public final double lacunarity;
   public final double persistence;
   private final PerlinNoiseSampler[] octaveSamplers;
   private final List<Double> amplitudes;

   public OctavePerlinNoiseSampler(JRand random, int octaveCount) {
      this.amplitudes = null;
      this.octaveSamplers = new PerlinNoiseSampler[octaveCount];

      for(int i = 0; i < octaveCount; ++i) {
         this.octaveSamplers[i] = new PerlinNoiseSampler(random);
      }

      this.lacunarity = (double)1.0F;
      this.persistence = (double)1.0F;
   }

   public int getCount() {
      return this.octaveSamplers.length;
   }

   public OctavePerlinNoiseSampler(JRand rand, IntStream octaves) {
      this(rand, (List)octaves.boxed().collect(Collectors.toList()));
   }

   public static Pair<Integer, List<Double>> makeAmplitudes(List<Integer> octaves) {
      Quad<Integer, Integer, Integer, List<Integer>> processedOctaves = processOctaves(octaves);
      int start = (Integer)processedOctaves.getFirst();
      List<Double> octavePlaces = new ArrayList();

      for(int octave : processedOctaves.getFourth()) {
         octavePlaces.set(octave + start, (double)1.0F);
      }

      return new Pair<Integer, List<Double>>(start, octavePlaces);
   }

   public OctavePerlinNoiseSampler(JRand rand, Pair<Integer, List<Double>> octaveParams) {
      this.amplitudes = octaveParams.getSecond();
      PerlinNoiseSampler perlin = new PerlinNoiseSampler(rand);
      int length = this.amplitudes.size();
      int start = (Integer)octaveParams.getFirst();
      this.octaveSamplers = new PerlinNoiseSampler[length];
      if (start >= 0 && start < length) {
         double d0 = (Double)this.amplitudes.get(start);
         if (d0 != (double)0.0F) {
            this.octaveSamplers[start] = perlin;
         }
      }

      for(int idx = start - 1; idx >= 0; --idx) {
         if (idx < length) {
            double d1 = (Double)this.amplitudes.get(idx);
            if (d1 != (double)0.0F) {
               this.octaveSamplers[idx] = new PerlinNoiseSampler(rand);
            } else {
               rand.advance(SKIP_262);
            }
         } else {
            rand.advance(SKIP_262);
         }
      }

      if (start < length - 1) {
         long noiseSeed = (long)(perlin.sample((double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F) * (double)Long.MAX_VALUE);
         rand.setSeed(noiseSeed);

         for(int l = start + 1; l < length; ++l) {
            if (l >= 0) {
               double d2 = (Double)this.amplitudes.get(l);
               if (d2 != (double)0.0F) {
                  this.octaveSamplers[l] = new PerlinNoiseSampler(rand);
               } else {
                  rand.advance(SKIP_262);
               }
            } else {
               rand.advance(SKIP_262);
            }
         }
      }

      this.persistence = Math.pow((double)2.0F, (double)(-start));
      this.lacunarity = Math.pow((double)2.0F, (double)(length - 1)) / (Math.pow((double)2.0F, (double)length) - (double)1.0F);
   }

   private static Quad<Integer, Integer, Integer, List<Integer>> processOctaves(List<Integer> octaves) {
      octaves = (List)octaves.stream().sorted(Integer::compareTo).collect(Collectors.toList());
      if (octaves.isEmpty()) {
         throw new IllegalArgumentException("Need some octaves!");
      } else {
         int start = -(Integer)octaves.get(0);
         int end = (Integer)octaves.get(octaves.size() - 1);
         int length = start + end + 1;
         if (length < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
         } else {
            return new Quad<Integer, Integer, Integer, List<Integer>>(start, end, length, octaves);
         }
      }
   }

   public OctavePerlinNoiseSampler(JRand rand, List<Integer> octaves) {
      this.amplitudes = null;
      Quad<Integer, Integer, Integer, List<Integer>> processedOctaves = processOctaves(octaves);
      int end = (Integer)processedOctaves.getSecond();
      int length = (Integer)processedOctaves.getThird();
      PerlinNoiseSampler perlin = new PerlinNoiseSampler(rand);
      this.octaveSamplers = new PerlinNoiseSampler[length];
      if (end >= 0 && end < length && octaves.contains(0)) {
         this.octaveSamplers[end] = perlin;
      }

      for(int idx = end + 1; idx < length; ++idx) {
         if (idx >= 0 && octaves.contains(end - idx)) {
            this.octaveSamplers[idx] = new PerlinNoiseSampler(rand);
         } else {
            rand.advance(SKIP_262);
         }
      }

      if (end > 0) {
         long noiseSeed = (long)(perlin.sample((double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F) * (double)Long.MAX_VALUE);
         rand.setSeed(noiseSeed);

         for(int index = end - 1; index >= 0; --index) {
            if (index < length && octaves.contains(end - index)) {
               this.octaveSamplers[index] = new PerlinNoiseSampler(rand);
            } else {
               rand.advance(SKIP_262);
            }
         }
      }

      this.persistence = Math.pow((double)2.0F, (double)end);
      this.lacunarity = (double)1.0F / (Math.pow((double)2.0F, (double)length) - (double)1.0F);
   }

   public double sample(double x, double y, double z) {
      return this.sample(x, y, z, (double)0.0F, (double)0.0F, false);
   }

   public double sample(double x, double y, double z, double yAmplification, double minY, boolean useDefaultY) {
      double noise = (double)0.0F;
      double persistence = this.persistence;
      double lacunarity = this.lacunarity;

      for(int idx = 0; idx < this.octaveSamplers.length; ++idx) {
         PerlinNoiseSampler sampler = this.octaveSamplers[idx];
         if (sampler != null) {
            double sample = sampler.sample(MathHelper.maintainPrecision(x * persistence), useDefaultY ? -sampler.originY : MathHelper.maintainPrecision(y * persistence), MathHelper.maintainPrecision(z * persistence), yAmplification * persistence, minY * persistence) * lacunarity;
            noise += (this.amplitudes != null ? (Double)this.amplitudes.get(idx) : (double)1.0F) * sample;
         }

         persistence /= (double)2.0F;
         lacunarity *= (double)2.0F;
      }

      return noise;
   }

   public PerlinNoiseSampler getOctave(int octave) {
      return this.octaveSamplers[octave];
   }

   public double sample(double x, double y, double yAmplification, double minY) {
      return this.sample(x, y, (double)0.0F, yAmplification, minY, false);
   }
}
