package xaero.common.minimap.mcworld;

import net.minecraft.class_310;
import net.minecraft.class_638;

public class MinimapClientWorldDataHelper {
   public static MinimapClientWorldData getCurrentWorldData() {
      return getWorldData(class_310.method_1551().field_1687);
   }

   public static MinimapClientWorldData getWorldData(class_638 clientWorld) {
      IXaeroMinimapClientWorld inter = (IXaeroMinimapClientWorld)clientWorld;
      MinimapClientWorldData minimapWorldData = inter.getXaero_minimapData();
      if (minimapWorldData == null) {
         inter.setXaero_minimapData(minimapWorldData = new MinimapClientWorldData(clientWorld));
      }

      return minimapWorldData;
   }
}
