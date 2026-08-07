package xaero.hud.minimap.radar.category.setting;

import java.util.List;
import java.util.Map;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import xaero.hud.category.setting.ObjectCategorySetting;
import xaero.hud.category.ui.setting.EditorSettingType;
import xaero.hud.category.util.CategoryConstants;
import xaero.hud.minimap.radar.category.EntityRadarCategoryConstants;
import xaero.hud.minimap.radar.color.RadarColor;
import xaero.lib.common.gui.widget.TooltipInfo;

public class EntityRadarCategorySettings {
   public static final String[] ENTITY_ICONS_OPTIONS = new String[]{"gui.xaero_icons_off", "gui.xaero_icons_list", "gui.xaero_icons_always", "-"};
   public static final String[] ENTITY_NAMES_OPTIONS = new String[]{"gui.xaero_names_off", "gui.xaero_names_list", "gui.xaero_names_always", "-"};
   public static final String[] RADAR_OVER_MAP_OPTIONS = new String[]{"gui.xaero_radar_over_map_never", "gui.xaero_radar_over_map_list", "gui.xaero_radar_over_map_always", "-"};
   public static final Map<String, ObjectCategorySetting<?>> SETTINGS;
   public static final List<ObjectCategorySetting<?>> SETTINGS_LIST;
   public static final ObjectCategorySetting<Boolean> DISPLAYED;
   public static final ObjectCategorySetting<Double> COLOR;
   public static final ObjectCategorySetting<Double> ICONS;
   public static final ObjectCategorySetting<Double> NAMES;
   public static final ObjectCategorySetting<Double> DOT_SIZE;
   public static final ObjectCategorySetting<Double> ICON_SCALE;
   public static final ObjectCategorySetting<Double> HEIGHT_LIMIT;
   public static final ObjectCategorySetting<Boolean> HEIGHT_FADE;
   public static final ObjectCategorySetting<Double> DISPLAY_Y;
   public static final ObjectCategorySetting<Double> START_FADING_AT;
   public static final ObjectCategorySetting<Double> ENTITY_NUMBER;
   public static final ObjectCategorySetting<Boolean> ALWAYS_NAMETAGS;
   public static final ObjectCategorySetting<Boolean> ICON_NAME_FALLBACK;
   public static final ObjectCategorySetting<Double> RENDER_OVER_MINIMAP;
   public static final ObjectCategorySetting<Double> RENDER_ORDER;

   static {
      SETTINGS = EntityRadarCategoryConstants.MAP_FACTORY.<String, ObjectCategorySetting<?>>get();
      SETTINGS_LIST = EntityRadarCategoryConstants.LIST_FACTORY.<ObjectCategorySetting<?>>get();
      DISPLAYED = ObjectCategorySetting.Builder.<Boolean>begin().setId("displayed").setDefaultValue(true).setDisplayName(class_2561.method_43471("gui.xaero_radar_displayed")).setSettingUIType(EditorSettingType.ITERATION_BUTTON).setUiFirstOption(0).setUiLastOption(1).setIndexReader((x) -> x == 1).setIndexWriter((x) -> x ? 1 : 0).setUiValueNameProvider(CategoryConstants::getBooleanComponent).build(SETTINGS, SETTINGS_LIST);
      COLOR = ObjectCategorySetting.Builder.<Double>begin().setId("color").setDefaultValue((double)13.0F).setDisplayName(class_2561.method_43471("gui.xaero_radar_dots_color")).setSettingUIType(EditorSettingType.EXPANDING).setUiFirstOption(-1).setUiLastOption(15).setIndexReader((x) -> (double)x).setIndexWriter((x) -> x.intValue()).setUiValueNameProvider((x) -> {
         RadarColor color = RadarColor.fromIndex(x.intValue());
         if (color == null) {
            class_5250 teamColorComponent = class_2561.method_43470("# ");
            teamColorComponent.method_10855().add(class_2561.method_43471("gui.xaero_radar_dots_color_team_colour"));
            return teamColorComponent;
         } else {
            class_5250 colorComponent = class_2561.method_43470("§" + color.getFormat() + "#§r ");
            colorComponent.method_10855().add(color.getName());
            return colorComponent;
         }
      }).build(SETTINGS, SETTINGS_LIST);
      ICONS = ObjectCategorySetting.Builder.<Double>begin().setId("icons").setDefaultValue((double)1.0F).setDisplayName(class_2561.method_43471("gui.xaero_radar_icons_displayed")).setSettingUIType(EditorSettingType.ITERATION_BUTTON).setUiFirstOption(0).setUiLastOption(2).setIndexReader((x) -> (double)x).setIndexWriter((x) -> x.intValue()).setUiValueNameProvider((x) -> class_2561.method_43471(ENTITY_ICONS_OPTIONS[x.intValue()])).setTooltip(new TooltipInfo("gui.xaero_box_entity_radar_icons")).build(SETTINGS, SETTINGS_LIST);
      NAMES = ObjectCategorySetting.Builder.<Double>begin().setId("names").setDefaultValue((double)0.0F).setDisplayName(class_2561.method_43471("gui.xaero_radar_names_displayed")).setSettingUIType(EditorSettingType.ITERATION_BUTTON).setUiFirstOption(0).setUiLastOption(2).setIndexReader((x) -> (double)x).setIndexWriter((x) -> x.intValue()).setUiValueNameProvider((x) -> class_2561.method_43471(ENTITY_NAMES_OPTIONS[x.intValue()])).setTooltip(new TooltipInfo("gui.xaero_box_entity_radar_names")).build(SETTINGS, SETTINGS_LIST);
      DOT_SIZE = ObjectCategorySetting.Builder.<Double>begin().setId("dotSize").setDefaultValue((double)2.0F).setDisplayName(class_2561.method_43471("gui.xaero_dots_size")).setSettingUIType(EditorSettingType.SLIDER).setUiFirstOption(1).setUiLastOption(4).setIndexReader((x) -> (double)x).setIndexWriter((x) -> x.intValue()).setUiValueNameProvider((x) -> class_2561.method_43470("" + x.intValue())).build(SETTINGS, SETTINGS_LIST);
      ICON_SCALE = ObjectCategorySetting.Builder.<Double>begin().setId("iconScale").setDefaultValue((double)1.0F).setDisplayName(class_2561.method_43471("gui.xaero_entity_heads_scale")).setSettingUIType(EditorSettingType.SLIDER).setUiFirstOption(5).setUiLastOption(40).setIndexReader((x) -> (double)x * 0.05).setIndexWriter((x) -> (int)(x / 0.05)).setUiValueNameProvider((x) -> class_2561.method_43470(String.format("%.2f", x))).build(SETTINGS, SETTINGS_LIST);
      HEIGHT_LIMIT = ObjectCategorySetting.Builder.<Double>begin().setId("heightLimit").setDefaultValue((double)20.0F).setDisplayName(class_2561.method_43471("gui.xaero_height_limit")).setSettingUIType(EditorSettingType.SLIDER).setUiFirstOption(2).setUiLastOption(410).setIndexReader((x) -> (double)x * (double)5.0F).setIndexWriter((x) -> (int)(x / (double)5.0F)).setUiValueNameProvider((x) -> class_2561.method_43470("" + x.intValue())).setTooltip(new TooltipInfo("gui.xaero_box_height_limit")).build(SETTINGS, SETTINGS_LIST);
      HEIGHT_FADE = ObjectCategorySetting.Builder.<Boolean>begin().setId("heightBasedFade").setDefaultValue(true).setDisplayName(class_2561.method_43471("gui.xaero_entity_depth")).setSettingUIType(EditorSettingType.ITERATION_BUTTON).setUiFirstOption(0).setUiLastOption(1).setIndexReader((x) -> x == 1).setIndexWriter((x) -> x ? 1 : 0).setUiValueNameProvider(CategoryConstants::getBooleanComponent).setTooltip(new TooltipInfo("gui.xaero_box_entity_depth")).build(SETTINGS, SETTINGS_LIST);
      DISPLAY_Y = ObjectCategorySetting.Builder.<Double>begin().setId("displayHeight").setDefaultValue((double)0.0F).setDisplayName(class_2561.method_43471("gui.xaero_entity_display_height")).setSettingUIType(EditorSettingType.ITERATION_BUTTON).setUiFirstOption(0).setUiLastOption(3).setIndexReader((x) -> (double)x).setIndexWriter((x) -> x.intValue()).setUiValueNameProvider((x) -> {
         if (x.intValue() == 0) {
            return class_2561.method_43471("gui.xaero_off");
         } else if (x.intValue() == 1) {
            return class_2561.method_43471("gui.xaero_entity_display_height_actual");
         } else {
            return x.intValue() == 2 ? class_2561.method_43471("gui.xaero_entity_display_height_relative") : class_2561.method_43471("gui.xaero_entity_display_height_direction");
         }
      }).build(SETTINGS, SETTINGS_LIST);
      START_FADING_AT = ObjectCategorySetting.Builder.<Double>begin().setId("startFadingAt").setDefaultValue((double)0.0F).setDisplayName(class_2561.method_43471("gui.xaero_start_fading_at")).setSettingUIType(EditorSettingType.SLIDER).setUiFirstOption(0).setUiLastOption(256).setIndexReader((x) -> (double)x).setIndexWriter((x) -> (int)x).setUiValueNameProvider((x) -> x.intValue() == 0 ? class_2561.method_43471("gui.xaero_start_fading_at_auto") : class_2561.method_43470("" + x.intValue())).setTooltip(new TooltipInfo("gui.xaero_box_start_fading_at")).build(SETTINGS, SETTINGS_LIST);
      ENTITY_NUMBER = ObjectCategorySetting.Builder.<Double>begin().setId("entityNumber").setDefaultValue((double)1000.0F).setDisplayName(class_2561.method_43471("gui.xaero_entity_amount")).setSettingUIType(EditorSettingType.SLIDER).setUiFirstOption(0).setUiLastOption(10).setIndexReader((x) -> (double)x * (double)100.0F).setIndexWriter((x) -> (int)(x / (double)100.0F)).setUiValueNameProvider((x) -> x.intValue() == 0 ? class_2561.method_43471("gui.xaero_unlimited") : class_2561.method_43470("" + x.intValue())).setTooltip(new TooltipInfo("gui.xaero_box_entity_amount")).build(SETTINGS, SETTINGS_LIST);
      ALWAYS_NAMETAGS = ObjectCategorySetting.Builder.<Boolean>begin().setId("alwaysDisplayNametags").setDefaultValue(false).setDisplayName(class_2561.method_43471("gui.xaero_always_entity_nametags")).setSettingUIType(EditorSettingType.ITERATION_BUTTON).setUiFirstOption(0).setUiLastOption(1).setIndexReader((x) -> x == 1).setIndexWriter((x) -> x ? 1 : 0).setUiValueNameProvider(CategoryConstants::getBooleanComponent).setTooltip(new TooltipInfo("gui.xaero_box_always_entity_nametags2")).build(SETTINGS, SETTINGS_LIST);
      ICON_NAME_FALLBACK = ObjectCategorySetting.Builder.<Boolean>begin().setId("displayNameWhenIconFails").setDefaultValue(true).setDisplayName(class_2561.method_43471("gui.xaero_entity_icon_name_fallback")).setSettingUIType(EditorSettingType.ITERATION_BUTTON).setUiFirstOption(0).setUiLastOption(1).setIndexReader((x) -> x == 1).setIndexWriter((x) -> x ? 1 : 0).setUiValueNameProvider(CategoryConstants::getBooleanComponent).build(SETTINGS, SETTINGS_LIST);
      RENDER_OVER_MINIMAP = ObjectCategorySetting.Builder.<Double>begin().setId("renderOverMinimapFrame").setDefaultValue((double)1.0F).setDisplayName(class_2561.method_43471("gui.xaero_radar_render_over_minimap")).setSettingUIType(EditorSettingType.ITERATION_BUTTON).setUiFirstOption(0).setUiLastOption(2).setIndexReader((x) -> (double)x).setIndexWriter((x) -> x.intValue()).setUiValueNameProvider((x) -> class_2561.method_43471(RADAR_OVER_MAP_OPTIONS[x.intValue()])).setTooltip(new TooltipInfo("gui.xaero_box_radar_render_over_minimap")).build(SETTINGS, SETTINGS_LIST);
      RENDER_ORDER = ObjectCategorySetting.Builder.<Double>begin().setId("renderOrder").setDefaultValue((double)0.0F).setDisplayName(class_2561.method_43471("gui.xaero_radar_render_order")).setSettingUIType(EditorSettingType.SLIDER).setUiFirstOption(0).setUiLastOption(1000).setIndexReader((x) -> (double)x).setIndexWriter((x) -> x.intValue()).setUiValueNameProvider((x) -> class_2561.method_43470("" + x.intValue())).setTooltip(new TooltipInfo("gui.xaero_box_radar_render_order")).build(SETTINGS, SETTINGS_LIST);
   }
}
