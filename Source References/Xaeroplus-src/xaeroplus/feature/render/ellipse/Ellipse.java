package xaeroplus.feature.render.ellipse;

public record Ellipse(int centerX, int centerZ, int radiusX, int radiusZ) {
   public Ellipse {
      if (radiusX <= 0) {
         throw new IllegalArgumentException("radiusX must be positive");
      } else if (radiusZ <= 0) {
         throw new IllegalArgumentException("radiusZ must be positive");
      }
   }

   public boolean intersects(final int minX, final int maxX, final int minZ, final int maxZ) {
      return (long)this.centerX + (long)this.radiusX >= (long)minX && (long)this.centerX - (long)this.radiusX <= (long)maxX && (long)this.centerZ + (long)this.radiusZ >= (long)minZ && (long)this.centerZ - (long)this.radiusZ <= (long)maxZ;
   }
}
