package xaero.common.platform;

import java.util.ServiceLoader;
import xaero.common.platform.services.IPlatformHelper;
import xaero.hud.minimap.MinimapLogs;

public class Services {
   public static final IPlatformHelper PLATFORM = (IPlatformHelper)load(IPlatformHelper.class);

   public static <T> T load(Class<T> clazz) {
      T loadedService = (T)ServiceLoader.load(clazz).findFirst().orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
      MinimapLogs.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
      return loadedService;
   }
}
