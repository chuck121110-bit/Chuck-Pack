package xaero.common.mods;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_1937;
import net.minecraft.class_2561;
import net.minecraft.class_5321;
import xaero.common.minimap.highlight.AbstractHighlighter;
import xaero.hud.minimap.info.render.compile.InfoDisplayCompiler;

public class WorldMapHighlighter extends AbstractHighlighter {
   private final xaero.map.highlight.AbstractHighlighter highlighter;
   private List<class_2561> tooltips;

   public WorldMapHighlighter(xaero.map.highlight.AbstractHighlighter highlighter) {
      super(highlighter.isCoveringOutsideDiscovered());
      this.highlighter = highlighter;
      this.tooltips = new ArrayList();
   }

   public boolean regionHasHighlights(class_5321<class_1937> dimension, int regionX, int regionZ) {
      return this.highlighter.regionHasHighlights(dimension, regionX, regionZ);
   }

   public boolean chunkIsHighlit(class_5321<class_1937> dimension, int chunkX, int chunkZ) {
      return this.highlighter.chunkIsHighlit(dimension, chunkX, chunkZ);
   }

   public int[] getChunkHighlitColor(class_5321<class_1937> dimension, int chunkX, int chunkZ) {
      return this.highlighter.getChunkHighlitColor(dimension, chunkX, chunkZ);
   }

   public void addBlockHighlightTooltips(InfoDisplayCompiler compiler, class_5321<class_1937> dimension, int blockX, int blockZ, int width) {
      this.tooltips.clear();
      this.highlighter.addMinimapBlockHighlightTooltips(this.tooltips, dimension, blockX, blockZ, width);

      for(class_2561 tooltipLine : this.tooltips) {
         compiler.addLine(tooltipLine);
      }

   }

   public boolean isCoveringOutsideDiscovered() {
      return this.highlighter.isCoveringOutsideDiscovered();
   }
}
