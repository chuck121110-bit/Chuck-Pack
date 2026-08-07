package xaeroplus.feature.render.text;

import java.util.Collection;
import java.util.Optional;
import xaero.map.gui.GuiMap;
import xaeroplus.Globals;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.GuiMapHelper;

public class DirectTextDrawFeature extends AbstractTextDrawFeature {
   private final TextSupplier textSupplier;
   private final String id;

   public DirectTextDrawFeature(String id, TextSupplier textSupplier) {
      this.id = id;
      this.textSupplier = textSupplier;
   }

   public Collection<Text> getTexts() {
      Optional<GuiMap> guiMapOptional = GuiMapHelper.getGuiMap();
      int windowX;
      int windowZ;
      int windowSize;
      if (guiMapOptional.isPresent()) {
         GuiMap guiMap = (GuiMap)guiMapOptional.get();
         windowX = GuiMapHelper.getGuiMapCenterRegionX(guiMap);
         windowZ = GuiMapHelper.getGuiMapCenterRegionZ(guiMap);
         windowSize = GuiMapHelper.getGuiMapRegionSize(guiMap);
      } else {
         windowX = ChunkUtils.getPlayerRegionX();
         windowZ = ChunkUtils.getPlayerRegionZ();
         windowSize = Math.max(3, Globals.minimapScaleMultiplier);
      }

      return this.textSupplier.getText(windowX, windowZ, windowSize, Globals.getCurrentDimensionId()).values();
   }

   public String id() {
      return this.id;
   }

   public void invalidateCache() {
   }

   public void close() {
   }
}
