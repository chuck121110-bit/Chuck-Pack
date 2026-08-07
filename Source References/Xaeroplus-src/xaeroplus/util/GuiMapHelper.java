package xaeroplus.util;

import java.util.Optional;
import net.minecraft.class_310;
import net.minecraft.class_437;
import xaero.lib.client.gui.GuiSettings;
import xaero.map.gui.GuiMap;
import xaeroplus.mixin.client.AccessorGuiMap;

public class GuiMapHelper {
   public static Optional<GuiMap> getGuiMap() {
      class_437 currentScreen = class_310.method_1551().field_1755;
      if (currentScreen instanceof GuiMap) {
         return Optional.of((GuiMap)currentScreen);
      } else {
         if (currentScreen instanceof GuiSettings) {
            GuiSettings screen = (GuiSettings)currentScreen;
            class_437 var4 = screen.parent;
            if (var4 instanceof GuiMap) {
               GuiMap map = (GuiMap)var4;
               return Optional.of(map);
            }

            var4 = screen.escape;
            if (var4 instanceof GuiMap) {
               GuiMap map = (GuiMap)var4;
               return Optional.of(map);
            }
         }

         return Optional.empty();
      }
   }

   public static double getCameraX(final GuiMap guiMap) {
      return ((AccessorGuiMap)guiMap).getCameraX();
   }

   public static double getCameraZ(final GuiMap guiMap) {
      return ((AccessorGuiMap)guiMap).getCameraZ();
   }

   public static double getDestScale(final GuiMap guiMap) {
      return AccessorGuiMap.getDestScale();
   }

   public static int getGuiMapRegionSize(final GuiMap guiMap) {
      return (int)Math.max((double)5.0F / getDestScale(guiMap), (double)3.0F);
   }

   public static boolean isGuiMapLoaded() {
      return class_310.method_1551().field_1755 instanceof GuiMap;
   }

   public static int getGuiMapCenterRegionX(final GuiMap guiMap) {
      return ChunkUtils.coordToRegionCoord(getCameraX(guiMap));
   }

   public static int getGuiMapCenterRegionZ(final GuiMap guiMap) {
      return ChunkUtils.coordToRegionCoord(getCameraZ(guiMap));
   }
}
