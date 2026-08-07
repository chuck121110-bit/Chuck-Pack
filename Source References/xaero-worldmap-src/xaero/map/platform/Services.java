package xaero.map.platform;

import java.util.ServiceLoader;
import xaero.map.WorldMap;
import xaero.map.platform.services.IPlatformHelper;

public class Services {
   public static final IPlatformHelper PLATFORM = (IPlatformHelper)load(IPlatformHelper.class);

   public static <T> T load(Class<T> clazz) {
      T loadedService = (T)ServiceLoader.load(clazz).findFirst().orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
      WorldMap.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
      return loadedService;
   }
}
