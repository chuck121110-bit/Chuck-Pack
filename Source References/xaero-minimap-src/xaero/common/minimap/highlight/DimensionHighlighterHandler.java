package xaero.common.minimap.highlight;

import net.minecraft.class_1937;
import net.minecraft.class_5321;
import xaero.common.minimap.write.MinimapWriter;
import xaero.hud.minimap.info.render.compile.InfoDisplayCompiler;

public class DimensionHighlighterHandler {
   private final MinimapWriter writer;
   private final class_5321<class_1937> dimension;
   private final HighlighterRegistry registry;
   private int version;

   public DimensionHighlighterHandler(class_5321<class_1937> dimension, HighlighterRegistry registry, MinimapWriter writer) {
      this.dimension = dimension;
      this.registry = registry;
      this.writer = writer;
   }

   public boolean shouldApplyRegionHighlights(int regionX, int regionZ, boolean discovered) {
      class_5321<class_1937> dimension = this.dimension;

      for(AbstractHighlighter hl : this.registry.getHighlighters()) {
         if ((discovered || hl.isCoveringOutsideDiscovered()) && hl.regionHasHighlights(dimension, regionX, regionZ)) {
            return true;
         }
      }

      return false;
   }

   public boolean shouldApplyTileChunkHighlights(int regionX, int regionZ, int insideTileChunkX, int insideTileChunkZ, boolean discovered) {
      int startChunkX = regionX << 5 | insideTileChunkX << 2;
      int startChunkZ = regionZ << 5 | insideTileChunkZ << 2;

      for(AbstractHighlighter hl : this.registry.getHighlighters()) {
         if (this.shouldApplyTileChunkHighlightsHelp(hl, regionX, regionZ, startChunkX, startChunkZ, discovered)) {
            return true;
         }
      }

      return false;
   }

   private boolean shouldApplyTileChunkHighlights(AbstractHighlighter hl, int regionX, int regionZ, int insideTileChunkX, int insideTileChunkZ, boolean discovered) {
      int startChunkX = regionX << 5 | insideTileChunkX << 2;
      int startChunkZ = regionZ << 5 | insideTileChunkZ << 2;
      return this.shouldApplyTileChunkHighlightsHelp(hl, regionX, regionZ, startChunkX, startChunkZ, discovered);
   }

   private boolean shouldApplyTileChunkHighlightsHelp(AbstractHighlighter hl, int regionX, int regionZ, int startChunkX, int startChunkZ, boolean discovered) {
      if (!discovered && !hl.isCoveringOutsideDiscovered()) {
         return false;
      } else {
         class_5321<class_1937> dimension = this.dimension;
         if (!hl.regionHasHighlights(dimension, regionX, regionZ)) {
            return false;
         } else {
            for(int i = 0; i < 4; ++i) {
               for(int j = 0; j < 4; ++j) {
                  if (hl.chunkIsHighlit(dimension, startChunkX | i, startChunkZ | j)) {
                     return true;
                  }
               }
            }

            return false;
         }
      }
   }

   public int[] applyChunkHighlightColors(int tileX, int tileZ) {
      int[] highlights = new int[256];
      class_5321<class_1937> dimension = this.dimension;

      for(AbstractHighlighter hl : this.registry.getHighlighters()) {
         int[] highlightColors = hl.getChunkHighlitColor(dimension, tileX, tileZ);
         if (highlightColors != null) {
            for(int i = 0; i < highlightColors.length; ++i) {
               int highlightColor = highlightColors[i];
               int hlAlpha = highlightColor & 255;
               float hlAlphaFloat = (float)hlAlpha / 255.0F;
               float oneMinusHlAlpha = 1.0F - hlAlphaFloat;
               int hlRed = highlightColor >> 8 & 255;
               int hlGreen = highlightColor >> 16 & 255;
               int hlBlue = highlightColor >> 24 & 255;
               int destColor = highlights[i];
               int red = destColor >> 8 & 255;
               int green = destColor >> 16 & 255;
               int blue = destColor >> 24 & 255;
               int alpha = destColor & 255;
               red = (int)((float)red * oneMinusHlAlpha + (float)hlRed * hlAlphaFloat);
               green = (int)((float)green * oneMinusHlAlpha + (float)hlGreen * hlAlphaFloat);
               blue = (int)((float)blue * oneMinusHlAlpha + (float)hlBlue * hlAlphaFloat);
               alpha = (int)((float)alpha * oneMinusHlAlpha + (float)hlAlpha);
               if (red > 255) {
                  red = 255;
               }

               if (green > 255) {
                  green = 255;
               }

               if (blue > 255) {
                  blue = 255;
               }

               if (alpha > 255) {
                  alpha = 255;
               }

               highlights[i] = blue << 24 | green << 16 | red << 8 | alpha;
            }
         }
      }

      return highlights;
   }

   public void requestRefresh(int regionX, int regionZ) {
      int loadingCanvasLeft = this.writer.getLoadingMapChunkX();
      int loadingCanvasTop = this.writer.getLoadingMapChunkZ();
      int loadingCanvasRight = loadingCanvasLeft + this.writer.getLoadingSideInChunks();
      int loadingCanvasBottom = loadingCanvasTop + this.writer.getLoadingSideInChunks();
      int regionLeft = regionX << 3;
      int regionRight = regionX + 1 << 3;
      int regionTop = regionZ << 3;
      int regionBottom = regionZ + 1 << 3;
      if (regionRight > loadingCanvasLeft && regionLeft < loadingCanvasRight && regionBottom > loadingCanvasTop && regionTop < loadingCanvasBottom) {
         ++this.version;
      }

   }

   public void requestRefresh() {
      ++this.version;
   }

   public void addBlockHighlightTooltips(InfoDisplayCompiler compiler, int blockX, int blockZ, int width, boolean discovered) {
      class_5321<class_1937> dimension = this.dimension;
      int tileChunkX = blockX >> 6;
      int tileChunkZ = blockZ >> 6;
      int regionX = tileChunkX >> 3;
      int regionZ = tileChunkZ >> 3;
      if (this.shouldApplyRegionHighlights(regionX, regionZ, discovered)) {
         int localTileChunkX = tileChunkX & 7;
         int localTileChunkZ = tileChunkZ & 7;

         for(AbstractHighlighter hl : this.registry.getHighlighters()) {
            if (this.shouldApplyTileChunkHighlights(hl, regionX, regionZ, localTileChunkX, localTileChunkZ, discovered)) {
               hl.addBlockHighlightTooltips(compiler, dimension, blockX, blockZ, width);
            }
         }

      }
   }

   public int getVersion() {
      return this.version;
   }
}
