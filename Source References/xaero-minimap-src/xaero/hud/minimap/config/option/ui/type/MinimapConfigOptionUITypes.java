package xaero.hud.minimap.config.option.ui.type;

import xaero.common.HudMod;
import xaero.common.gui.GuiEntityRadarCategoryEditor;
import xaero.common.gui.GuiInfoDisplayEdit;
import xaero.hud.minimap.common.config.info.config.InfoDisplayManagerConfigData;
import xaero.hud.minimap.radar.category.serialization.data.EntityRadarCategoryData;
import xaero.lib.client.config.option.ui.factory.StandardConfigWidgetFactories;
import xaero.lib.client.config.option.ui.factory.StandardViewEnforcedConditions;
import xaero.lib.client.config.option.ui.type.ConfigOptionUIType;
import xaero.lib.client.config.option.ui.type.ConfigOptionUIType.Builder;
import xaero.lib.common.config.option.ConfigOption;

public class MinimapConfigOptionUITypes {
   public static final ConfigOptionUIType<ConfigOption<EntityRadarCategoryData>> RADAR_CATEGORIES_EDITOR;
   public static final ConfigOptionUIType<ConfigOption<InfoDisplayManagerConfigData>> INFO_DISPLAY_CONFIG_EDITOR;

   static {
      RADAR_CATEGORIES_EDITOR = Builder.begin().setWidgetFactory(StandardConfigWidgetFactories.getOpenScreenFactory((parent, escape, config, enforced, option, onChange, readOnly, includeNullValue) -> HudMod.INSTANCE.getEntityRadarCategoryManager().getEditedCategory() == null ? null : new GuiEntityRadarCategoryEditor(HudMod.INSTANCE, parent, parent, onChange, readOnly), StandardViewEnforcedConditions.SHIFT_PRESSED)).build();
      INFO_DISPLAY_CONFIG_EDITOR = Builder.begin().setWidgetFactory(StandardConfigWidgetFactories.getOpenScreenFactory((parent, escape, config, enforced, option, onChange, readOnly, includeNullValue) -> new GuiInfoDisplayEdit(parent, parent, config, onChange, readOnly), StandardViewEnforcedConditions.SHIFT_PRESSED)).build();
   }
}
