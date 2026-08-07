package xaeroplus.feature.render.line;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.class_3532;
import xaeroplus.util.ChunkUtils;

public class LinePreProcessor {
   public static final int MAX_LINE_LENGTH = 500000;

   public static WindowBounds windowBounds(final int windowX, final int windowZ, final int windowSize) {
      int minX = ChunkUtils.regionCoordToCoord(windowX - windowSize);
      int minZ = ChunkUtils.regionCoordToCoord(windowZ - windowSize);
      int maxX = ChunkUtils.regionCoordToCoord(windowX + windowSize);
      int maxZ = ChunkUtils.regionCoordToCoord(windowZ + windowSize);
      return new WindowBounds(minX, maxX, minZ, maxZ);
   }

   public static List<Line> clippedSplitOriented(final Line line, final WindowBounds windowBounds) {
      if (!windowBounds.contains(line)) {
         return Collections.emptyList();
      } else {
         List<Line> splitLines = ensureLength(line);
         if (splitLines.isEmpty()) {
            return List.of(ensureOrientation(line));
         } else {
            splitLines.replaceAll(LinePreProcessor::ensureOrientation);
            return splitLines;
         }
      }
   }

   public static Line ensureOrientation(Line line) {
      return line.z1() > line.z2() ? new Line(line.x2(), line.z2(), line.x1(), line.z1()) : line;
   }

   public static List<Line> ensureLength(Line line) {
      double len = line.length();
      if (len <= (double)500000.0F) {
         return Collections.emptyList();
      } else {
         List<Line> lines = new ArrayList((int)(len / (double)500000.0F) + 1);
         int dx = line.x2() - line.x1();
         int dz = line.z2() - line.z1();
         if (dx == 0) {
            int x = line.x1();
            int minZ = Math.min(line.z1(), line.z2());
            int maxZ = Math.max(line.z1(), line.z2());

            int z2;
            for(int z1 = minZ; z1 < maxZ; z1 = z2) {
               z2 = class_3532.method_15340(z1 + 500000, minZ, maxZ);
               Line l = new Line(x, z1, x, z2);
               lines.add(l);
            }
         } else if (dz == 0) {
            int z = line.z1();
            int minX = Math.min(line.x1(), line.x2());
            int maxX = Math.max(line.x1(), line.x2());

            int x2;
            for(int x1 = minX; x1 < maxX; x1 = x2) {
               x2 = class_3532.method_15340(x1 + 500000, minX, maxX);
               Line l = new Line(x1, z, x2, z);
               lines.add(l);
            }
         } else {
            double slope = (double)dz / (double)dx;
            double intercept = (double)line.z1() - slope * (double)line.x1();
            boolean positiveSlope = Math.abs(slope) > (double)1.0F;
            if (positiveSlope) {
               int minZ = Math.min(line.z1(), line.z2());
               int maxZ = Math.max(line.z1(), line.z2());

               int z2;
               for(int z1 = minZ; z1 < maxZ; z1 = z2) {
                  double x1 = ((double)z1 - intercept) / slope;
                  z2 = class_3532.method_15340(z1 + 500000, minZ, maxZ);
                  double x2 = ((double)z2 - intercept) / slope;
                  int xx1 = (int)Math.round(x1);
                  int xx2 = (int)Math.round(x2);
                  Line l = new Line(xx1, z1, xx2, z2);
                  lines.add(l);
               }
            } else {
               int minX = Math.min(line.x1(), line.x2());
               int maxX = Math.max(line.x1(), line.x2());

               int x2;
               for(int x1 = minX; x1 < maxX; x1 = x2) {
                  double z1 = slope * (double)x1 + intercept;
                  x2 = class_3532.method_15340(x1 + 500000, minX, maxX);
                  double z2 = slope * (double)x2 + intercept;
                  int zz1 = (int)Math.round(z1);
                  int zz2 = (int)Math.round(z2);
                  Line l = new Line(x1, zz1, x2, zz2);
                  lines.add(l);
               }
            }
         }

         return lines;
      }
   }

   public static record WindowBounds(int minX, int maxX, int minZ, int maxZ) {
      public boolean contains(final Line line) {
         return line.lineClip(this.minX, this.maxX, this.minZ, this.maxZ);
      }
   }
}
