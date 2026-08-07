package xaero.map.region.texture;

import xaero.map.highlight.DimensionHighlighterHandler;
import xaero.map.pool.buffer.PoolTextureDirectBufferUnit;
import xaero.map.region.MapTileChunk;

public class ExportLeafRegionTexture extends LeafRegionTexture {
   public static int MIP_MAP_LEVELS = 10;

   public ExportLeafRegionTexture(MapTileChunk tileChunk) {
      super(tileChunk);
   }

   public void applyHighlights(DimensionHighlighterHandler highlighterHandler, PoolTextureDirectBufferUnit colorBuffer) {
      super.applyHighlights(highlighterHandler, colorBuffer, false);
   }

   protected int getMipMapLevels() {
      return MIP_MAP_LEVELS;
   }
}
