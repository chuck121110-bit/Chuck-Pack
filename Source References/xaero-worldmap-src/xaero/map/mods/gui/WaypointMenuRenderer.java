package xaero.map.mods.gui;

import java.io.IOException;
import net.minecraft.class_10799;
import net.minecraft.class_124;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_5250;
import org.joml.Matrix3x2fStack;
import xaero.common.HudMod;
import xaero.common.IXaeroMinimap;
import xaero.common.gui.GuiNewSet;
import xaero.common.gui.GuiWaypointSets;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.controls.key.MinimapKeyMappings;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.client.controls.util.KeyMappingUtils;
import xaero.lib.client.gui.widget.Tooltip;
import xaero.lib.client.gui.widget.dropdown.DropDownWidget;
import xaero.lib.client.gui.widget.dropdown.IDropDownWidgetCallback;
import xaero.lib.client.gui.widget.dropdown.DropDownWidget.Builder;
import xaero.lib.common.config.Config;
import xaero.lib.common.config.single.SingleConfigManager;
import xaero.map.WorldMap;
import xaero.map.config.primary.option.WorldMapPrimaryClientConfigOptions;
import xaero.map.config.util.WorldMapClientConfigUtils;
import xaero.map.element.MapElementMenuRenderer;
import xaero.map.element.render.ElementRenderer;
import xaero.map.gui.GuiMap;
import xaero.map.gui.GuiTexturedButton;
import xaero.map.mods.SupportMods;

public class WaypointMenuRenderer extends MapElementMenuRenderer<Waypoint, WaypointMenuRenderContext> {
   private final WaypointRenderer renderer;
   private class_4185 showDisabledButton;
   private class_4185 closeMenuWhenHoppingButton;
   private class_4185 currentMapWaypointsButton;
   private class_4185 renderAllSetsButton;

   public WaypointMenuRenderer(WaypointMenuRenderContext context, WaypointMenuRenderProvider provider, WaypointRenderer renderer) {
      super(context, provider);
      this.renderer = renderer;
   }

   public void onMapInit(GuiMap screen, class_310 mc, int width, int height, MinimapWorld waypointWorld, IXaeroMinimap modMain, MinimapSession minimapSession) {
      super.onMapInit(screen, mc, width, height);
      GuiWaypointSets sets = waypointWorld != null ? new GuiWaypointSets(true, waypointWorld) : null;
      IDropDownWidgetCallback setsDropdownCallback = null;
      if (sets != null) {
         setsDropdownCallback = (menu, selected) -> {
            if (selected == menu.size() - 1) {
               GuiNewSet guiNewSet = new GuiNewSet(modMain, minimapSession, screen, screen, waypointWorld);
               class_310.method_1551().method_1507(guiNewSet);
               return false;
            } else {
               sets.setCurrentSet(selected);
               waypointWorld.setCurrentWaypointSetId(sets.getCurrentSetKey());

               try {
                  minimapSession.getWorldManagerIO().saveWorld(waypointWorld);
               } catch (IOException e) {
                  WorldMap.LOGGER.error("suppressed exception", e);
               }

               return true;
            }
         };
      }

      DropDownWidget setsDropdown = sets == null ? null : Builder.begin().setOptions(sets.getOptions()).setX(width - 173).setY(height - 56).setW(151).setSelected(sets.getCurrentSet()).setCallback(setsDropdownCallback).setContainer(screen).setOpeningUp(true).setNarrationTitle(class_2561.method_43471("gui.xaero_dropdown_waypoint_set")).build();
      if (setsDropdown != null) {
         screen.method_25429(setsDropdown);
      }

      class_5250 fullWaypointMenuTooltipText = class_2561.method_43469("gui.xaero_box_full_waypoints_menu", new Object[]{class_2561.method_43470(KeyMappingUtils.getKeyName(MinimapKeyMappings.WAYPOINT_MENU)).method_27692(class_124.field_1077)});
      ClientConfigManager configManager = WorldMap.INSTANCE.getConfigs().getClientConfigManager();
      SingleConfigManager<Config> primaryConfigManager = configManager.getPrimaryConfigManager();
      boolean onlyCurrentMapWaypoints = (Boolean)primaryConfigManager.getEffective(WorldMapPrimaryClientConfigOptions.ONLY_CURRENT_MAP_WAYPOINTS);
      boolean showDisabledWaypoints = (Boolean)primaryConfigManager.getEffective(WorldMapPrimaryClientConfigOptions.DISPLAY_DISABLED_WAYPOINTS);
      boolean closeWaypointsWhenHopping = (Boolean)primaryConfigManager.getEffective(WorldMapPrimaryClientConfigOptions.CLOSE_WAYPOINTS_AFTER_HOP);
      Tooltip fullWaypointMenuTooltip = new Tooltip(fullWaypointMenuTooltipText, true);
      Tooltip onlyCurrentMapWaypointsTooltip = new Tooltip(onlyCurrentMapWaypoints ? "gui.xaero_box_only_current_map_waypoints" : "gui.xaero_box_waypoints_selected_by_minimap", class_2583.field_24360, true);
      ClientConfigManager minimapConfigManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      boolean renderAllSetsConfig = (Boolean)minimapConfigManager.getEffective(MinimapProfiledConfigOptions.WAYPOINTS_ALL_SETS);
      Tooltip renderAllSetsTooltip = new Tooltip(class_2561.method_43469(!renderAllSetsConfig ? "gui.xaero_box_rendering_current_set" : "gui.xaero_box_rendering_all_sets", new Object[]{class_2561.method_43470(KeyMappingUtils.getKeyName(MinimapKeyMappings.RENDER_ALL_SETS)).method_27692(class_124.field_1077)}), true);
      Tooltip showingDisabledTooltip = new Tooltip(showDisabledWaypoints ? "gui.xaero_box_showing_disabled" : "gui.xaero_box_hiding_disabled", class_2583.field_24360, true);
      Tooltip closeWhenHoppingTooltip = new Tooltip(closeWaypointsWhenHopping ? "gui.xaero_box_closing_menu_when_hopping" : "gui.xaero_box_not_closing_menu_when_hopping", class_2583.field_24360, true);
      screen.addButton(new GuiTexturedButton(width - 173, height - 20, 20, 20, 229, 0, 16, 16, WorldMap.guiTextures, (b) -> this.onFullMenuButton(b, screen), () -> fullWaypointMenuTooltip, 256, 256));
      screen.addButton(this.currentMapWaypointsButton = new GuiTexturedButton(width - 153, height - 20, 20, 20, onlyCurrentMapWaypoints ? 213 : 229, 16, 16, 16, WorldMap.guiTextures, (b) -> this.onCurrentMapWaypointsButton(b, screen, width, height), () -> onlyCurrentMapWaypointsTooltip, 256, 256));
      screen.addButton(this.renderAllSetsButton = new GuiTexturedButton(width - 133, height - 20, 20, 20, !renderAllSetsConfig ? 81 : 97, 16, 16, 16, WorldMap.guiTextures, (b) -> this.onRenderAllSetsButton(b, screen, width, height), () -> renderAllSetsTooltip, 256, 256));
      screen.addButton(this.showDisabledButton = new GuiTexturedButton(width - 113, height - 20, 20, 20, showDisabledWaypoints ? 133 : 149, 16, 16, 16, WorldMap.guiTextures, (b) -> this.onShowDisabledButton(b, screen, width, height), () -> showingDisabledTooltip, 256, 256));
      screen.addButton(this.closeMenuWhenHoppingButton = new GuiTexturedButton(width - 93, height - 20, 20, 20, closeWaypointsWhenHopping ? 181 : 197, 16, 16, 16, WorldMap.guiTextures, (b) -> this.onCloseMenuWhenHoppingButton(b, screen, width, height), () -> closeWhenHoppingTooltip, 256, 256));
   }

   private void onFullMenuButton(class_4185 b, GuiMap screen) {
      SupportMods.xaeroMinimap.openWaypointsMenu(this.mc, screen);
   }

   private void onRenderAllSetsButton(class_4185 b, GuiMap screen, int width, int height) {
      SupportMods.xaeroMinimap.handleMinimapKeyBinding(MinimapKeyMappings.RENDER_ALL_SETS, screen);
      screen.method_25395(this.renderAllSetsButton);
   }

   private void onShowDisabledButton(class_4185 b, GuiMap screen, int width, int height) {
      WorldMapClientConfigUtils.togglePrimaryOption(WorldMapPrimaryClientConfigOptions.DISPLAY_DISABLED_WAYPOINTS);
      screen.method_25423(width, height);
      screen.method_25395(this.showDisabledButton);
   }

   private void onCloseMenuWhenHoppingButton(class_4185 b, GuiMap screen, int width, int height) {
      WorldMapClientConfigUtils.togglePrimaryOption(WorldMapPrimaryClientConfigOptions.CLOSE_WAYPOINTS_AFTER_HOP);
      screen.method_25423(width, height);
      screen.method_25395(this.closeMenuWhenHoppingButton);
   }

   private void onCurrentMapWaypointsButton(class_4185 b, GuiMap screen, int width, int height) {
      WorldMapClientConfigUtils.togglePrimaryOption(WorldMapPrimaryClientConfigOptions.ONLY_CURRENT_MAP_WAYPOINTS);
      screen.method_25423(width, height);
      screen.method_25395(this.currentMapWaypointsButton);
   }

   public void renderInMenu(Waypoint element, class_332 guiGraphics, class_437 gui, int mouseX, int mouseY, double scale, boolean enabled, boolean hovered, class_310 mc, boolean pressed, int textX) {
      Matrix3x2fStack matrixStack = guiGraphics.method_51448();
      boolean disabled = element.isDisabled();
      boolean temporary = element.isTemporary();
      WaypointPurpose purpose = element.getPurpose();
      int color = element.getColor();
      String symbol = element.getSymbol();
      matrixStack.translate(-4.0F, -4.0F);
      if (purpose == WaypointPurpose.DEATH) {
         guiGraphics.method_25294(0, 0, 9, 9, color);
         guiGraphics.method_25291(class_10799.field_56883, Waypoint.minimapTextures, 1, 1, 0.0F, 78.0F, 9, 9, 256, 256, -16119286);
         guiGraphics.method_25291(class_10799.field_56883, Waypoint.minimapTextures, 0, 0, 0.0F, 78.0F, 9, 9, 256, 256, -197380);
      } else {
         guiGraphics.method_25294(0, 0, 9, 9, color);
      }

      if (purpose != WaypointPurpose.DEATH) {
         guiGraphics.method_25303(mc.field_1772, symbol, 5 - mc.field_1772.method_1727(symbol) / 2, 1, -1);
      }

      int infoIconOffset = 10;
      if (disabled) {
         guiGraphics.method_25291(class_10799.field_56883, WorldMap.guiTextures, textX - 1 - infoIconOffset, 0, 173.0F, 16.0F, 8, 8, 256, 256, -256);
         infoIconOffset += 10;
      }

      if (temporary) {
         guiGraphics.method_25291(class_10799.field_56883, WorldMap.guiTextures, textX - 1 - infoIconOffset, 0, 165.0F, 16.0F, 8, 8, 256, 256, -65536);
         infoIconOffset += 10;
      }

   }

   public int menuStartPos(int height) {
      return height - 59;
   }

   public int menuSearchPadding() {
      return 14;
   }

   protected String getFilterPlaceholder() {
      return "gui.xaero_filter_waypoints_by_name";
   }

   protected ElementRenderer<? super Waypoint, ?, ?> getRenderer(Waypoint element) {
      return this.renderer;
   }

   protected void beforeFiltering() {
   }

   protected void beforeMenuRender() {
   }

   protected void afterMenuRender() {
   }
}
