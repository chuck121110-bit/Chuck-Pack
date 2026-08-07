package xaero.hud.minimap.info.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import xaero.common.settings.ModSettings;
import xaero.hud.minimap.common.config.info.config.InfoDisplayConfigData;
import xaero.hud.minimap.common.config.info.config.InfoDisplayManagerConfigData;
import xaero.hud.minimap.info.InfoDisplay;
import xaero.hud.minimap.info.InfoDisplayManager;
import xaero.lib.common.config.option.ConfigOption;

public class InfoDisplayConfigClientUtils {
   public static <T> InfoDisplayConfigData createDefaultConfig(InfoDisplay<T> infoDisplay, ModSettings legacySettings, boolean clientSide) {
      T legacyValue = (T)(clientSide ? infoDisplay.getLegacyValue(legacySettings) : null);
      return new InfoDisplayConfigData(!clientSide ? null : -1, !clientSide ? null : 15, !clientSide ? null : infoDisplay.getCodec().encode(legacyValue == null ? infoDisplay.getDefaultState() : legacyValue, (Path)null, (ConfigOption)null));
   }

   public static InfoDisplayManagerConfigData createDefaultConfig(InfoDisplayManager manager, ModSettings legacySettings, boolean clientSide) {
      List<String> order = new ArrayList(manager.getDefaultOrder());
      Map<String, InfoDisplayConfigData> configs = new HashMap();
      manager.getStream().forEach((infoDisplay) -> configs.put(infoDisplay.getId(), createDefaultConfig(infoDisplay, legacySettings, clientSide)));
      return new InfoDisplayManagerConfigData(order, configs);
   }
}
