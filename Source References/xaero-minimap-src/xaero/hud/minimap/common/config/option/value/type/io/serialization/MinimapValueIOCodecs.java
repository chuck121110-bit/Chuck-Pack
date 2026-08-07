package xaero.hud.minimap.common.config.option.value.type.io.serialization;

import xaero.common.HudMod;
import xaero.hud.minimap.common.config.info.config.InfoDisplayManagerConfigData;
import xaero.hud.minimap.radar.category.serialization.data.EntityRadarCategoryData;
import xaero.lib.common.config.option.value.io.serialization.LargeConfigValueIOCodec;

public class MinimapValueIOCodecs {
   public static final LargeConfigValueIOCodec<EntityRadarCategoryData> ENTITY_RADAR_CATEGORIES = new LargeConfigValueIOCodec((rootCategory) -> HudMod.INSTANCE.getEntityRadarCategorySerializers().getGson().serialize(rootCategory), (serializedData) -> serializedData == null ? null : (EntityRadarCategoryData)HudMod.INSTANCE.getEntityRadarCategorySerializers().getGson().deserialize(serializedData), Integer.MAX_VALUE, ".json");
   public static final LargeConfigValueIOCodec<InfoDisplayManagerConfigData> INFO_DISPLAY_CONFIG = new LargeConfigValueIOCodec((managerConfigData) -> HudMod.INSTANCE.getInfoDisplaysIO().encode(managerConfigData), (encodedData) -> encodedData == null ? null : HudMod.INSTANCE.getInfoDisplaysIO().decode(encodedData), Integer.MAX_VALUE, ".txt");
}
