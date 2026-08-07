package xaero.common.server.core;

import net.minecraft.class_1657;
import net.minecraft.class_3222;
import xaero.common.IXaeroMinimap;

public class XaeroMinimapServerCore {
   public static IXaeroMinimap modMain;

   public static void onServerWorldInfo(class_1657 player) {
      if (isModLoaded()) {
         modMain.getCommonEvents().onPlayerWorldJoin((class_3222)player);
      }
   }

   public static boolean isModLoaded() {
      return modMain != null && (modMain.isLoadedServer() || modMain.isLoadedClient());
   }
}
