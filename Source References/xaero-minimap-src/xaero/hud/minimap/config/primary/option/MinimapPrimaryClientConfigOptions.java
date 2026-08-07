package xaero.hud.minimap.config.primary.option;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2561;
import xaero.lib.common.config.option.BooleanConfigOption;
import xaero.lib.common.config.option.ConfigOption;
import xaero.lib.common.config.option.ConfigOptionManager;
import xaero.lib.common.config.option.BooleanConfigOption.Builder;
import xaero.lib.common.config.option.ConfigOption.FinalBuilder;
import xaero.lib.common.config.option.value.type.BuiltInConfigValueTypes;
import xaero.lib.common.config.util.ConfigUtils;

public class MinimapPrimaryClientConfigOptions {
   private static final List<ConfigOption<?>> ALL = new ArrayList();
   public static final BooleanConfigOption UPDATE_NOTIFICATIONS;
   public static final ConfigOption<Integer> IGNORED_UPDATE;
   public static final BooleanConfigOption WRONG_WORLD_TELEPORT;
   public static final BooleanConfigOption DIFFERENTIATE_BY_SERVER_ADDRESS;
   public static final BooleanConfigOption DEBUG_ENTITY_ICONS;
   public static final BooleanConfigOption DEBUG_ENTITY_VARIANT_IDS;
   public static final BooleanConfigOption WAYPOINT_MUTUAL_EDIT;

   public static void registerAll(ConfigOptionManager manager) {
      for(ConfigOption<?> option : ALL) {
         manager.register(option);
      }

   }

   static {
      UPDATE_NOTIFICATIONS = ((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)Builder.begin().setId("update_notifications")).setDefaultValue(true)).setDisplayName(class_2561.method_43471("gui.xaero_update_notification"))).build(ALL);
      IGNORED_UPDATE = ((ConfigOption.FinalBuilder)((ConfigOption.FinalBuilder)((ConfigOption.FinalBuilder)((ConfigOption.FinalBuilder)FinalBuilder.begin().setId("ignored_update")).setDefaultValue(0)).setValueType(BuiltInConfigValueTypes.INTEGER)).setDisplayGetter(ConfigUtils::getDisplayForSimpleNumber)).build(ALL);
      WRONG_WORLD_TELEPORT = ((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)Builder.begin().setId("allow_wrong_world_teleport")).setDefaultValue(false)).build(ALL);
      DIFFERENTIATE_BY_SERVER_ADDRESS = ((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)Builder.begin().setId("differentiate_by_server_address")).setDefaultValue(true)).build(ALL);
      DEBUG_ENTITY_ICONS = ((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)Builder.begin().setId("debug_entity_icons")).setDefaultValue(false)).build(ALL);
      DEBUG_ENTITY_VARIANT_IDS = ((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)Builder.begin().setId("debug_entity_variant_ids")).setDefaultValue(false)).build(ALL);
      WAYPOINT_MUTUAL_EDIT = ((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)Builder.begin().setId("waypoint_mutual_edit")).setDefaultValue(true)).build(ALL);
   }
}
