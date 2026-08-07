package xaero.map.config.primary.option;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2561;
import xaero.lib.client.config.option.ClientConfigOptionManager;
import xaero.lib.common.config.option.BooleanConfigOption;
import xaero.lib.common.config.option.ConfigOption;
import xaero.lib.common.config.option.RangeConfigOption;
import xaero.lib.common.config.option.BooleanConfigOption.Builder;
import xaero.lib.common.config.option.ConfigOption.FinalBuilder;
import xaero.lib.common.config.option.value.type.BuiltInConfigValueTypes;
import xaero.lib.common.config.util.ConfigUtils;

public class WorldMapPrimaryClientConfigOptions {
   private static final List<ConfigOption<?>> ALL = new ArrayList();
   public static final ConfigOption<Integer> IGNORED_UPDATE;
   public static final BooleanConfigOption UPDATE_NOTIFICATIONS;
   public static final BooleanConfigOption RELOAD_VIEWED;
   public static final ConfigOption<Integer> RELOAD_VIEWED_VERSION;
   public static final BooleanConfigOption DEBUG;
   public static final ConfigOption<Integer> CAVE_MODE_START;
   public static final BooleanConfigOption EXPORT_MULTIPLE_IMAGES;
   public static final BooleanConfigOption NIGHT_EXPORT;
   public static final RangeConfigOption EXPORT_SCALE_DOWN_SQUARE;
   public static final BooleanConfigOption EXPORT_HIGHLIGHTS;
   public static final BooleanConfigOption DIFFERENTIATE_BY_SERVER_ADDRESS;
   public static final BooleanConfigOption DISPLAY_DISABLED_WAYPOINTS;
   public static final BooleanConfigOption CLOSE_WAYPOINTS_AFTER_HOP;
   public static final BooleanConfigOption ONLY_CURRENT_MAP_WAYPOINTS;
   public static final ConfigOption<Integer> GLOBAL_VERSION;
   public static final RangeConfigOption MAX_LOADED_REGIONS;
   public static final BooleanConfigOption IGNORE_MAP_FILE_LOCKS;

   public static void registerAll(ClientConfigOptionManager manager) {
      for(ConfigOption<?> option : ALL) {
         manager.register(option);
      }

   }

   static {
      IGNORED_UPDATE = ((ConfigOption.FinalBuilder)((ConfigOption.FinalBuilder)((ConfigOption.FinalBuilder)((ConfigOption.FinalBuilder)FinalBuilder.begin().setId("ignored_update")).setDefaultValue(0)).setValueType(BuiltInConfigValueTypes.INTEGER)).setDisplayGetter(ConfigUtils::getDisplayForSimpleNumber)).build(ALL);
      UPDATE_NOTIFICATIONS = ((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)Builder.begin().setId("update_notifications")).setDefaultValue(true)).setDisplayName(class_2561.method_43471("gui.xaero_wm_update_notification"))).build(ALL);
      RELOAD_VIEWED = ((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)Builder.begin().setId("reload_viewed")).setDefaultValue(false)).setDisplayName(class_2561.method_43471("gui.xaero_reload_viewed_regions"))).setTooltip(class_2561.method_43471("gui.xaero_box_reload_viewed_regions"))).build(ALL);
      RELOAD_VIEWED_VERSION = ((ConfigOption.FinalBuilder)((ConfigOption.FinalBuilder)((ConfigOption.FinalBuilder)FinalBuilder.begin().setId("reload_viewed_version")).setValueType(BuiltInConfigValueTypes.INTEGER)).setDefaultValue(0)).build(ALL);
      DEBUG = ((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)Builder.begin().setId("debug")).setDefaultValue(false)).setDisplayName(class_2561.method_43471("gui.xaero_debug"))).build(ALL);
      CAVE_MODE_START = ((ConfigOption.FinalBuilder)((ConfigOption.FinalBuilder)((ConfigOption.FinalBuilder)((ConfigOption.FinalBuilder)FinalBuilder.begin().setId("cave_mode_start_y")).setValueType(BuiltInConfigValueTypes.INTEGER)).setDefaultValue(Integer.MAX_VALUE)).setDisplayName(class_2561.method_43471("gui.xaero_wm_cave_mode_start"))).build(ALL);
      EXPORT_MULTIPLE_IMAGES = ((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)Builder.begin().setId("png_export_in_multiple_images")).setDefaultValue(false)).setDisplayName(class_2561.method_43471("gui.xaero_export_option_multiple_images"))).setTooltip(class_2561.method_43471("gui.xaero_box_export_option_multiple_images"))).build(ALL);
      NIGHT_EXPORT = ((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)Builder.begin().setId("png_export_night")).setDefaultValue(false)).setDisplayName(class_2561.method_43471("gui.xaero_export_option_nighttime"))).setTooltip(class_2561.method_43471("gui.xaero_box_export_option_nighttime"))).build(ALL);
      EXPORT_SCALE_DOWN_SQUARE = ((RangeConfigOption.Builder)((RangeConfigOption.Builder)((RangeConfigOption.Builder)((RangeConfigOption.Builder)((RangeConfigOption.Builder)((RangeConfigOption.Builder)((RangeConfigOption.Builder)xaero.lib.common.config.option.RangeConfigOption.Builder.begin().setId("png_export_scale_down_square")).setDefaultValue(20)).setMinIndex(0)).setMaxIndex(90)).setDisplayGetter((o, v) -> v <= 0 ? class_2561.method_43471("gui.xaero_export_option_scale_down_square_unscaled") : class_2561.method_43469("gui.xaero_export_option_scale_down_square_value", new Object[]{v}))).setDisplayName(class_2561.method_43471("gui.xaero_export_option_scale_down_square"))).setTooltip(class_2561.method_43471("gui.xaero_box_export_option_scale_down_square"))).build(ALL);
      EXPORT_HIGHLIGHTS = ((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)Builder.begin().setId("png_export_highlights")).setDefaultValue(false)).setDisplayName(class_2561.method_43471("gui.xaero_export_option_highlights"))).setTooltip(class_2561.method_43471("gui.xaero_box_export_option_highlights"))).build(ALL);
      DIFFERENTIATE_BY_SERVER_ADDRESS = ((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)Builder.begin().setId("differentiate_by_server_address")).setDefaultValue(true)).build(ALL);
      DISPLAY_DISABLED_WAYPOINTS = ((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)Builder.begin().setId("display_disabled_waypoints")).setDefaultValue(false)).build(ALL);
      CLOSE_WAYPOINTS_AFTER_HOP = ((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)Builder.begin().setId("close_waypoints_after_hopping")).setDefaultValue(true)).build(ALL);
      ONLY_CURRENT_MAP_WAYPOINTS = ((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)Builder.begin().setId("only_display_current_map_waypoints")).setDefaultValue(false)).build(ALL);
      GLOBAL_VERSION = ((ConfigOption.FinalBuilder)((ConfigOption.FinalBuilder)((ConfigOption.FinalBuilder)FinalBuilder.begin().setId("global_version")).setValueType(BuiltInConfigValueTypes.INTEGER)).setDefaultValue(1)).build(ALL);
      MAX_LOADED_REGIONS = ((RangeConfigOption.Builder)((RangeConfigOption.Builder)((RangeConfigOption.Builder)((RangeConfigOption.Builder)((RangeConfigOption.Builder)((RangeConfigOption.Builder)xaero.lib.common.config.option.RangeConfigOption.Builder.begin().setId("max_loaded_regions")).setDefaultValue(300)).setMinIndex(50)).setMaxIndex(1000)).setDisplayName(class_2561.method_43471("gui.xaero_wm_max_loaded_regions"))).setTooltip(class_2561.method_43471("gui.xaero_wm_box_max_loaded_regions"))).build(ALL);
      IGNORE_MAP_FILE_LOCKS = ((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)Builder.begin().setId("ignore_map_file_locks")).setDefaultValue(false)).setDisplayName(class_2561.method_43471("gui.xaero_wm_ignore_map_file_locks"))).setTooltip(class_2561.method_43471("gui.xaero_wm_box_ignore_map_file_locks"))).build(ALL);
   }
}
