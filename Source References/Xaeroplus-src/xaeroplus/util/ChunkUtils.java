package xaeroplus.util;

import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_310;
import net.minecraft.class_5321;
import xaeroplus.Globals;

public class ChunkUtils {
   public static long chunkPosToLong(final class_1923 chunkPos) {
      return chunkPos.method_8324();
   }

   public static long chunkPosToLong(final int x, final int z) {
      return class_1923.method_8331(x, z);
   }

   public static class_1923 longToChunkPos(final long l) {
      return new class_1923(l);
   }

   public static int posToChunkPos(final int i) {
      return i >> 4;
   }

   public static int longToChunkX(final long l) {
      return class_1923.method_8325(l);
   }

   public static int longToChunkZ(final long l) {
      return class_1923.method_8332(l);
   }

   public static double getPlayerX() {
      try {
         class_310 mc = class_310.method_1551();
         class_5321<class_1937> dim = mc.field_1687.method_27983();
         if ((dim == class_1937.field_25180 || Globals.getCurrentDimensionId() == class_1937.field_25180) && dim != Globals.getCurrentDimensionId()) {
            if (Globals.getCurrentDimensionId() == class_1937.field_25179) {
               return mc.method_1560().method_23317() * (double)8.0F;
            }

            if (Globals.getCurrentDimensionId() == class_1937.field_25180 && dim == class_1937.field_25179) {
               return mc.method_1560().method_23317() / (double)8.0F;
            }
         }

         return mc.method_1560().method_23317();
      } catch (Exception var2) {
         return (double)0.0F;
      }
   }

   public static double getPlayerZ() {
      try {
         class_310 mc = class_310.method_1551();
         class_5321<class_1937> dim = mc.field_1687.method_27983();
         if ((dim == class_1937.field_25180 || Globals.getCurrentDimensionId() == class_1937.field_25180) && dim != Globals.getCurrentDimensionId()) {
            if (Globals.getCurrentDimensionId() == class_1937.field_25179) {
               return mc.method_1560().method_23321() * (double)8.0F;
            }

            if (Globals.getCurrentDimensionId() == class_1937.field_25180 && dim == class_1937.field_25179) {
               return mc.method_1560().method_23321() / (double)8.0F;
            }
         }

         return mc.method_1560().method_23321();
      } catch (Exception var2) {
         return (double)0.0F;
      }
   }

   public static int actualPlayerChunkX() {
      try {
         return class_310.method_1551().method_1560().method_31476().field_9181;
      } catch (NullPointerException var1) {
         return 0;
      }
   }

   public static int getPlayerChunkX() {
      return coordToChunkCoord(getPlayerX());
   }

   public static int actualPlayerChunkZ() {
      try {
         return class_310.method_1551().method_1560().method_31476().field_9180;
      } catch (NullPointerException var1) {
         return 0;
      }
   }

   public static int getPlayerChunkZ() {
      return coordToChunkCoord(getPlayerZ());
   }

   public static int actualPlayerRegionX() {
      return actualPlayerChunkX() >> 5;
   }

   public static int getPlayerRegionX() {
      return getPlayerChunkX() >> 5;
   }

   public static int actualPlayerRegionZ() {
      return actualPlayerChunkZ() >> 5;
   }

   public static int getPlayerRegionZ() {
      return getPlayerChunkZ() >> 5;
   }

   public static class_5321<class_1937> getActualDimension() {
      try {
         return class_310.method_1551().field_1687.method_27983();
      } catch (Exception var1) {
         return class_1937.field_25179;
      }
   }

   public static int coordToChunkCoord(final double coord) {
      return (int)coord >> 4;
   }

   public static int coordToRegionCoord(final double coord) {
      return (int)coord >> 9;
   }

   public static int chunkCoordToCoord(final int chunkCoord) {
      return chunkCoord << 4;
   }

   public static int chunkCoordToRegionCoord(final int chunkCoord) {
      return chunkCoord >> 5;
   }

   public static int regionCoordToChunkCoord(final int regionCoord) {
      return regionCoord << 5;
   }

   public static int regionCoordToCoord(final int regionCoord) {
      return regionCoord << 9;
   }

   public static int coordToMapRegionCoord(final int coord) {
      return coord >> 9;
   }

   public static int mapRegionCoordToCoord(final int mapRegionCoord) {
      return mapRegionCoord << 9;
   }

   public static int mapTileChunkCoordToMapRegionCoord(final int mapTileChunkCoord) {
      return mapTileChunkCoord >> 3;
   }

   public static int mapRegionCoordToMapTileChunkCoord(final int mapRegionCoord) {
      return mapRegionCoord << 3;
   }

   public static int mapTileCoordToMapTileChunkCoord(final int mapTileCoord) {
      return mapTileCoord >> 2;
   }

   public static int mapTileChunkCoordToMapTileCoord(final int mapTileChunkCoord) {
      return mapTileChunkCoord << 2;
   }

   public static int mapTileCoordToCoord(final int mapTileCoord) {
      return mapTileCoord << 4;
   }

   public static int coordToMapTileCoord(final int coord) {
      return coord >> 4;
   }

   public static int mapTileCoordToMapRegionCoord(final int mapTileCoord) {
      return mapTileCoord >> 6;
   }

   public static int mapRegionCoordToMapTileCoord(final int mapRegionCoord) {
      return mapRegionCoord << 6;
   }

   public static int mapTileChunkCoordToCoord(final int mapTileChunkCoord) {
      return mapTileChunkCoord << 6;
   }

   public static int coordToMapTileChunkCoord(final int coord) {
      return coord >> 6;
   }

   public static int chunkCoordToMapRegionCoord(final int chunkCoord) {
      return chunkCoord >> 5;
   }

   public static int mapRegionCoordToChunkCoord(final int mapRegionCoord) {
      return mapRegionCoord << 5;
   }

   public static int chunkCoordToMapTileChunkCoord(final int chunkCoord) {
      return chunkCoord >> 2;
   }

   public static int mapTileChunkCoordToChunkCoord(final int mapTileChunkCoord) {
      return mapTileChunkCoord << 2;
   }

   public static int chunkCoordToMapTileCoord(final int chunkCoord) {
      return chunkCoord;
   }

   public static int mapTileCoordToChunkCoord(final int mapTileCoord) {
      return mapTileCoord;
   }

   public static int regionCoordToMapRegionCoord(final int regionCoord) {
      return regionCoord;
   }

   public static int mapRegionCoordToRegionCoord(final int mapRegionCoord) {
      return mapRegionCoord;
   }

   public static int regionCoordToMapTileChunkCoord(final int regionCoord) {
      return regionCoord << 3;
   }

   public static int mapTileChunkCoordToRegionCoord(final int mapTileChunkCoord) {
      return mapTileChunkCoord >> 3;
   }

   public static int regionCoordToMapTileCoord(final int regionCoord) {
      return regionCoord << 6;
   }

   public static int mapTileCoordToRegionCoord(final int mapTileCoord) {
      return mapTileCoord >> 6;
   }

   public static int chunkCoordToMapTileChunkCoordLocal(final int chunkCoord) {
      return chunkCoordToMapTileChunkCoord(chunkCoord) & 7;
   }

   public static int chunkCoordToMapTileCoordLocal(final int chunkCoord) {
      return chunkCoordToMapTileCoord(chunkCoord) & 3;
   }
}
