package xaero.map;

import xaero.map.biome.BiomeGetter;
import xaero.map.cache.BlockStateShortShapeCache;
import xaero.map.region.OverlayManager;

public class MapWriterFabric extends MapWriter {
   public MapWriterFabric(OverlayManager overlayManager, BlockStateShortShapeCache blockStateShortShapeCache, BiomeGetter biomeGetter) {
      super(overlayManager, blockStateShortShapeCache, biomeGetter);
   }
}
