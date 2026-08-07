package xaero.common.minimap.write;

import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import xaero.common.IXaeroMinimap;
import xaero.common.cache.BlockStateShortShapeCache;
import xaero.common.minimap.highlight.HighlighterRegistry;
import xaero.hud.minimap.module.MinimapSession;

public class MinimapWriterFabric extends MinimapWriter {
   public MinimapWriterFabric(IXaeroMinimap modMain, MinimapSession minimapSession, BlockStateShortShapeCache blockStateShortShapeCache, HighlighterRegistry highlighterRegistry) {
      super(modMain, minimapSession, blockStateShortShapeCache, highlighterRegistry);
   }

   protected int getBlockStateLightEmission(class_2680 state, class_1937 world, class_2338 pos) {
      return state.method_26213();
   }
}
