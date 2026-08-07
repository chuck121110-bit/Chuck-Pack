package xaeroplus.feature.render;

import java.util.Optional;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import xaero.map.gui.GuiMap;
import xaeroplus.Globals;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.GuiMapHelper;

public record MapRenderWindow(int windowX, int windowZ, int windowSize, class_5321<class_1937> dimension) {
   public static MapRenderWindow resolveCurrent() {
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

      return new MapRenderWindow(windowX, windowZ, windowSize, Globals.getCurrentDimensionId());
   }
}
