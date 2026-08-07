package xaero.common.gui;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.class_2561;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import xaero.common.HudMod;
import xaero.common.IXaeroMinimap;
import xaero.hud.category.ObjectCategory;
import xaero.hud.category.setting.ObjectCategorySetting;
import xaero.hud.category.ui.RootCategorySettingEntry;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.radar.category.setting.EntityRadarCategorySettings;
import xaero.lib.client.gui.ISettingEntry;
import xaero.lib.client.gui.config.context.IEditConfigScreenContext;

public class GuiEntityRadarSettings extends GuiMinimapSettings {
   public GuiEntityRadarSettings(IXaeroMinimap modMain, class_437 backScreen, class_437 escScreen, IEditConfigScreenContext context) {
      super(class_2561.method_43471("gui.xaero_entity_radar_settings"), backScreen, escScreen, context);
      BooleanSupplier allowNullSupplier = () -> HudMod.INSTANCE.getEntityRadarCategoryManager().getEditedCategoryConfig().isAllowNullValues();
      Supplier<ObjectCategory<?, ?>> categorySupplier = () -> HudMod.INSTANCE.getEntityRadarCategoryManager().getEditedCategory();
      BiConsumer<Object, Object> defaultValueChangeListener = (o, n) -> HudMod.INSTANCE.getEntityRadarCategoryManager().setEditedCategoryNeedsSaving(true);
      ObjectCategorySetting var10004 = EntityRadarCategorySettings.ENTITY_NUMBER;
      class_5250 var10005 = class_2561.method_43471("gui.xaero_entity_amount");
      Objects.requireNonNull(defaultValueChangeListener);
      RootCategorySettingEntry<?> entityNumberEntry = new RootCategorySettingEntry(categorySupplier, allowNullSupplier, var10004, var10005, defaultValueChangeListener::accept);
      var10004 = EntityRadarCategorySettings.HEIGHT_FADE;
      var10005 = class_2561.method_43471("gui.xaero_entity_depth");
      Objects.requireNonNull(defaultValueChangeListener);
      RootCategorySettingEntry<?> entityFadeEntry = new RootCategorySettingEntry(categorySupplier, allowNullSupplier, var10004, var10005, defaultValueChangeListener::accept);
      var10004 = EntityRadarCategorySettings.ALWAYS_NAMETAGS;
      var10005 = class_2561.method_43471("gui.xaero_always_entity_nametags");
      Objects.requireNonNull(defaultValueChangeListener);
      RootCategorySettingEntry<?> alwaysNametagsEntry = new RootCategorySettingEntry(categorySupplier, allowNullSupplier, var10004, var10005, defaultValueChangeListener::accept);
      var10004 = EntityRadarCategorySettings.DISPLAY_Y;
      var10005 = class_2561.method_43471("gui.xaero_entity_display_height_full");
      Objects.requireNonNull(defaultValueChangeListener);
      RootCategorySettingEntry<?> displayYEntry = new RootCategorySettingEntry(categorySupplier, allowNullSupplier, var10004, var10005, defaultValueChangeListener::accept);
      var10004 = EntityRadarCategorySettings.ICON_NAME_FALLBACK;
      var10005 = class_2561.method_43471("gui.xaero_entity_icon_name_fallback");
      Objects.requireNonNull(defaultValueChangeListener);
      RootCategorySettingEntry<?> iconNameFallbackEntry = new RootCategorySettingEntry(categorySupplier, allowNullSupplier, var10004, var10005, defaultValueChangeListener::accept);
      var10004 = EntityRadarCategorySettings.ICONS;
      var10005 = class_2561.method_43471("gui.xaero_radar_setting_icons");
      Objects.requireNonNull(defaultValueChangeListener);
      RootCategorySettingEntry<?> iconsEntry = new RootCategorySettingEntry(categorySupplier, allowNullSupplier, var10004, var10005, defaultValueChangeListener::accept);
      var10004 = EntityRadarCategorySettings.NAMES;
      var10005 = class_2561.method_43471("gui.xaero_radar_setting_names");
      Objects.requireNonNull(defaultValueChangeListener);
      RootCategorySettingEntry<?> namesEntry = new RootCategorySettingEntry(categorySupplier, allowNullSupplier, var10004, var10005, defaultValueChangeListener::accept);
      var10004 = EntityRadarCategorySettings.RENDER_OVER_MINIMAP;
      var10005 = class_2561.method_43471("gui.xaero_radar_render_radar_over_frame");
      Objects.requireNonNull(defaultValueChangeListener);
      RootCategorySettingEntry<?> renderOverMinimapEntry = new RootCategorySettingEntry(categorySupplier, allowNullSupplier, var10004, var10005, defaultValueChangeListener::accept);
      var10004 = EntityRadarCategorySettings.DOT_SIZE;
      var10005 = class_2561.method_43471("gui.xaero_dots_size");
      Objects.requireNonNull(defaultValueChangeListener);
      RootCategorySettingEntry<?> dotSizeEntry = new RootCategorySettingEntry(categorySupplier, allowNullSupplier, var10004, var10005, defaultValueChangeListener::accept);
      RootCategorySettingEntry<?> iconScaleEntry = new RootCategorySettingEntry(categorySupplier, allowNullSupplier, EntityRadarCategorySettings.ICON_SCALE, class_2561.method_43471("gui.xaero_entity_heads_scale"), (currentScale, newScale) -> {
         defaultValueChangeListener.accept(currentScale, newScale);
         if (context.isClientSide() && (newScale < (double)1.0F || newScale < (double)1.0F != currentScale < (double)1.0F)) {
            modMain.getMinimap().getMinimapFBORenderer().resetEntityIcons();
         }

      });
      var10004 = EntityRadarCategorySettings.HEIGHT_LIMIT;
      var10005 = class_2561.method_43471("gui.xaero_height_limit");
      Objects.requireNonNull(defaultValueChangeListener);
      RootCategorySettingEntry<?> heightLimitEntry = new RootCategorySettingEntry(categorySupplier, allowNullSupplier, var10004, var10005, defaultValueChangeListener::accept);
      var10004 = EntityRadarCategorySettings.START_FADING_AT;
      var10005 = class_2561.method_43471("gui.xaero_start_fading_at");
      Objects.requireNonNull(defaultValueChangeListener);
      RootCategorySettingEntry<?> startFadingEntry = new RootCategorySettingEntry(categorySupplier, allowNullSupplier, var10004, var10005, defaultValueChangeListener::accept);
      this.entries = new ISettingEntry[]{this.optionEntry(MinimapProfiledConfigOptions.RADAR_CATEGORIES), this.optionEntry(MinimapProfiledConfigOptions.DISPLAY_RADAR), dotSizeEntry, this.optionEntry(MinimapProfiledConfigOptions.RADAR_DOTS_STYLE), this.optionEntry(MinimapProfiledConfigOptions.RADAR_SMOOTH_DOTS), iconsEntry, namesEntry, iconScaleEntry, this.optionEntry(MinimapProfiledConfigOptions.RADAR_NAME_SCALE), alwaysNametagsEntry, iconNameFallbackEntry, entityNumberEntry, heightLimitEntry, entityFadeEntry, startFadingEntry, displayYEntry, this.optionEntry(MinimapProfiledConfigOptions.RADAR_MAIN_ENTITY), this.optionEntry(MinimapProfiledConfigOptions.RADAR_MAIN_DOT_SIZE), this.optionEntry(MinimapProfiledConfigOptions.ARROW_SCALE), this.optionEntry(MinimapProfiledConfigOptions.ARROW_COLOR), this.optionEntry(MinimapProfiledConfigOptions.ARROW_OPACITY), renderOverMinimapEntry, this.optionEntry(MinimapProfiledConfigOptions.RADAR_HIDE_INVISIBLE), this.optionEntry(MinimapProfiledConfigOptions.TRACKED_PLAYERS_ON_MINIMAP), this.optionEntry(MinimapProfiledConfigOptions.TRACKED_PLAYERS_IN_WORLD), this.optionEntry(MinimapProfiledConfigOptions.TRACKED_PLAYER_MINIMAP_ICON_SCALE), this.optionEntry(MinimapProfiledConfigOptions.TRACKED_PLAYER_WORLD_ICON_SCALE), this.optionEntry(MinimapProfiledConfigOptions.TRACKED_PLAYER_WORLD_NAME_SCALE)};
   }
}
