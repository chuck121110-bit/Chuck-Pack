package xaeroplus.util;

import org.waste.of.time.manager.CaptureManager;
import org.waste.of.time.storage.cache.HotCache;
import xaeroplus.XaeroPlus;

public class WorldToolsHelper {
   private static boolean isWorldToolsPresent = false;
   private static boolean checked = false;
   private static String minVersion = "1.2.0";

   public static boolean isWorldToolsPresent() {
      if (!checked) {
         try {
            boolean a = CaptureManager.INSTANCE.getCapturing();
            boolean b = HotCache.INSTANCE.isChunkSaved(0, 0);
            XaeroPlus.LOGGER.info("Found WorldTools. Enabling WorldTools support.");
            isWorldToolsPresent = true;
         } catch (Throwable var2) {
            XaeroPlus.LOGGER.info("WorldTools not found. Disabling WorldTools support.");
            isWorldToolsPresent = false;
         }

         checked = true;
      }

      return isWorldToolsPresent;
   }

   public static boolean isDownloading() {
      return CaptureManager.INSTANCE.getCapturing();
   }
}
