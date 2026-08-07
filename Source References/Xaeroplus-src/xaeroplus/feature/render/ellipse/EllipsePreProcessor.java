package xaeroplus.feature.render.ellipse;

import xaeroplus.util.ChunkUtils;

public class EllipsePreProcessor {
   public static WindowBounds windowBounds(final int windowX, final int windowZ, final int windowSize) {
      int minX = ChunkUtils.regionCoordToCoord(windowX - windowSize);
      int minZ = ChunkUtils.regionCoordToCoord(windowZ - windowSize);
      int maxX = ChunkUtils.regionCoordToCoord(windowX + windowSize);
      int maxZ = ChunkUtils.regionCoordToCoord(windowZ + windowSize);
      return new WindowBounds(minX, maxX, minZ, maxZ);
   }

   public static record WindowBounds(int minX, int maxX, int minZ, int maxZ) {
      public boolean intersects(final Ellipse ellipse) {
         return ellipse.intersects(this.minX, this.maxX, this.minZ, this.maxZ);
      }
   }
}
