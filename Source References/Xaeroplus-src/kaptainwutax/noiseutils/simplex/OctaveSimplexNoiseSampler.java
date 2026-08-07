package kaptainwutax.noiseutils.simplex;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import kaptainwutax.noiseutils.noise.NoiseSampler;
import kaptainwutax.seedutils.rand.JRand;

public class OctaveSimplexNoiseSampler implements NoiseSampler {
   public final double lacunarity;
   public final double persistence;
   private final SimplexNoiseSampler[] octaveSamplers;

   public OctaveSimplexNoiseSampler(JRand random, int octaveCount) {
      this.octaveSamplers = new SimplexNoiseSampler[octaveCount];

      for(int i = 0; i < octaveCount; ++i) {
         this.octaveSamplers[i] = new SimplexNoiseSampler(random);
      }

      this.lacunarity = (double)1.0F;
      this.persistence = (double)1.0F;
   }

   public OctaveSimplexNoiseSampler(JRand rand, IntStream octaves) {
      this(rand, (List)octaves.boxed().collect(Collectors.toList()));
   }

   public OctaveSimplexNoiseSampler(JRand rand, List<Integer> octaves) {
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
            SimplexNoiseSampler simplex = new SimplexNoiseSampler(rand);
            this.octaveSamplers = new SimplexNoiseSampler[length];
            if (end >= 0 && end < length && octaves.contains(0)) {
               this.octaveSamplers[end] = simplex;
            }

            for(int idx = end + 1; idx < length; ++idx) {
               if (idx >= 0 && octaves.contains(end - idx)) {
                  this.octaveSamplers[idx] = new SimplexNoiseSampler(rand);
               } else {
                  rand.advance(SKIP_262);
               }
            }

            if (end > 0) {
               long noiseSeed = (long)(simplex.sample3D(simplex.originX, simplex.originY, simplex.originZ) * (double)Long.MAX_VALUE);
               rand.setSeed(noiseSeed);

               for(int index = end - 1; index >= 0; --index) {
                  if (index < length && octaves.contains(end - index)) {
                     this.octaveSamplers[index] = new SimplexNoiseSampler(rand);
                  } else {
                     rand.advance(SKIP_262);
                  }
               }
            }

            this.persistence = Math.pow((double)2.0F, (double)end);
            this.lacunarity = (double)1.0F / (Math.pow((double)2.0F, (double)length) - (double)1.0F);
         }
      }
   }

   public double sample(double x, double y) {
      return this.sample(x, y, false);
   }

   public double sample(double x, double y, boolean useRandomOffset) {
      double noise = (double)0.0F;
      double persistence = this.persistence;
      double lacunarity = this.lacunarity;

      for(SimplexNoiseSampler sampler : this.octaveSamplers) {
         if (sampler != null) {
            noise += sampler.sample2D(x * persistence + (useRandomOffset ? sampler.originX : (double)0.0F), y * persistence + (useRandomOffset ? sampler.originY : (double)0.0F)) * lacunarity;
         }

         persistence /= (double)2.0F;
         lacunarity *= (double)2.0F;
      }

      return noise;
   }

   public double sample(double x, double y, double notUsed, double notUsed2) {
      return this.sample(x, y, true) * 0.55;
   }
}
