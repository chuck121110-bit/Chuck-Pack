package xaero.hud.minimap.common.config.option.value.type.sync.serialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_2487;
import net.minecraft.class_2499;
import net.minecraft.class_2519;
import xaero.common.HudMod;
import xaero.hud.minimap.common.config.info.config.InfoDisplayConfigData;
import xaero.hud.minimap.common.config.info.config.InfoDisplayManagerConfigData;
import xaero.hud.minimap.radar.category.serialization.data.EntityRadarCategoryData;
import xaero.lib.common.config.option.value.sync.serialization.ConfigValueSyncCodec;

public class MinimapValueSyncCodecs {
   public static final ConfigValueSyncCodec<EntityRadarCategoryData, class_2487> ENTITY_RADAR_CATEGORIES = new ConfigValueSyncCodec((rootCategory) -> HudMod.INSTANCE.getEntityRadarCategorySerializers().getNbt().serialize(rootCategory), (serializedData) -> (EntityRadarCategoryData)HudMod.INSTANCE.getEntityRadarCategorySerializers().getNbt().deserialize(serializedData));
   public static final ConfigValueSyncCodec<InfoDisplayManagerConfigData, class_2487> INFO_DISPLAY_CONFIG = new ConfigValueSyncCodec((config) -> {
      class_2487 encodedData = new class_2487();
      class_2499 orderTag = new class_2499();
      config.getOrderStream().forEach((s) -> orderTag.add(class_2519.method_23256(s)));
      encodedData.method_10566("o", orderTag);
      class_2487 configsTag = new class_2487();
      config.getOrderStream().forEach((id) -> {
         InfoDisplayConfigData infoDisplayConfig = config.get(id);
         if (infoDisplayConfig != null) {
            class_2487 infoDisplayTag = new class_2487();
            if (infoDisplayConfig.getState() != null) {
               infoDisplayTag.method_10582("s", infoDisplayConfig.getState());
            }

            if (infoDisplayConfig.getBackgroundColor() != null) {
               infoDisplayTag.method_10569("b", infoDisplayConfig.getBackgroundColor());
            }

            if (infoDisplayConfig.getTextColor() != null) {
               infoDisplayTag.method_10569("t", infoDisplayConfig.getTextColor());
            }

            if (!infoDisplayTag.method_33133()) {
               configsTag.method_10566(id, infoDisplayTag);
            }
         }
      });
      encodedData.method_10566("c", configsTag);
      return encodedData;
   }, (encodedData) -> {
      class_2499 orderTag = encodedData.method_68569("o");
      List<String> order = new ArrayList();
      if (!orderTag.isEmpty()) {
         for(int i = 0; i < orderTag.size(); ++i) {
            order.add(orderTag.method_68577(i, ""));
         }
      }

      class_2487 configsTag = encodedData.method_68568("c");
      Map<String, InfoDisplayConfigData> configs = new HashMap();
      order.forEach((id) -> {
         class_2487 infoDisplayTag = configsTag.method_68568(id);
         if (infoDisplayTag.method_33133()) {
            configs.put(id, new InfoDisplayConfigData((Integer)null, (Integer)null, (String)null));
         } else {
            String state = infoDisplayTag.method_10545("s") ? infoDisplayTag.method_68564("s", "") : null;
            Integer backgroundColor = infoDisplayTag.method_10545("b") ? infoDisplayTag.method_68083("b", 0) : null;
            Integer textColor = infoDisplayTag.method_10545("t") ? infoDisplayTag.method_68083("t", 0) : null;
            configs.put(id, new InfoDisplayConfigData(backgroundColor, textColor, state));
         }
      });
      return new InfoDisplayManagerConfigData(order, configs);
   });
}
