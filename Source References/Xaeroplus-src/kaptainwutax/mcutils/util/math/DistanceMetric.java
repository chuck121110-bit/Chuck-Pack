package kaptainwutax.mcutils.util.math;

import kaptainwutax.mathutils.util.Mth;

@FunctionalInterface
public interface DistanceMetric {
   DistanceMetric EUCLIDEAN_SQ = (x, y, z) -> (double)(x * x + y * y + z * z);
   DistanceMetric EUCLIDEAN = (x, y, z) -> Math.sqrt(EUCLIDEAN_SQ.getDistance(x, y, z));
   DistanceMetric MANHATTAN = (x, y, z) -> (double)(Math.abs(x) + Math.abs(y) + Math.abs(z));
   DistanceMetric CHEBYSHEV = (x, y, z) -> (double)Mth.max(Math.abs(x), Math.abs(y), Math.abs(z));

   double getDistance(int var1, int var2, int var3);
}
