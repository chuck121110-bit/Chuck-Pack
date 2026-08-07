package xaero.common.gui;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.class_1074;
import net.minecraft.class_2561;
import net.minecraft.class_437;
import xaero.common.HudMod;
import xaero.common.IXaeroMinimap;
import xaero.common.settings.ModSettings;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.module.MinimapSession;
import xaero.lib.client.gui.GuiConstants;
import xaero.lib.client.gui.GuiSettings;
import xaero.lib.client.gui.ISettingEntry;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.config.context.BuiltInEditConfigScreenContexts;
import xaero.lib.client.gui.config.context.IEditConfigScreenContext;
import xaero.lib.client.gui.widget.Tooltip;
import xaero.lib.common.config.option.BuiltInProfiledConfigOptions;

public class GuiMinimapMain extends GuiMinimapSettings {
   private ISettingEntry[] mainEntries;
   private ISettingEntry[] searchableEntries;

   public GuiMinimapMain(class_437 current) {
      this(HudMod.INSTANCE, current, ScreenBase.tryToGetEscape(current), true, BuiltInEditConfigScreenContexts.CLIENT);
   }

   public GuiMinimapMain(IXaeroMinimap modMain, class_437 par1GuiScreen, class_437 escScreen, boolean profileOptions, IEditConfigScreenContext context) {
      super(class_2561.method_43471("gui.xaero_minimap_settings"), par1GuiScreen, escScreen, context);
      ScreenSwitchSettingEntry changePositionEntry = new ScreenSwitchSettingEntry("gui.xaero_change_position", (current, escape) -> (class_437)(par1GuiScreen instanceof GuiEditMode ? par1GuiScreen : new GuiEditMode(modMain, current, escape, false, class_2561.method_43471("gui.xaero_minimap_guide"))), context.isClientSide() ? null : new Tooltip(GuiConstants.SETTING_ENTRY_WRONG_CONTEXT_COMPONENT), context.isClientSide());
      ScreenSwitchSettingEntry viewSettingsEntry = new ScreenSwitchSettingEntry("gui.xaero_minimap_view_settings", (current, escape) -> new GuiMinimapViewSettings(modMain, current, escape, context), (Tooltip)null, true);
      ScreenSwitchSettingEntry entityRadarSettingsEntry = new ScreenSwitchSettingEntry("gui.xaero_entity_radar_settings", (current, escape) -> new GuiEntityRadarSettings(modMain, current, escape, context), (Tooltip)null, true);
      ScreenSwitchSettingEntry blockMapSettingsEntry = new ScreenSwitchSettingEntry("gui.xaero_minimap_block_map_settings", (current, escape) -> new GuiMinimapBlockMapSettings(modMain, current, escape, context), (Tooltip)null, true);
      ScreenSwitchSettingEntry overlaySettingsEntry = new ScreenSwitchSettingEntry("gui.xaero_overlay_settings", (current, escape) -> new GuiMinimapOverlaysSettings(modMain, current, escape, context), (Tooltip)null, true);
      ScreenSwitchSettingEntry infoSettingsEntry = new ScreenSwitchSettingEntry("gui.xaero_minimap_info_settings", (current, escape) -> new GuiMinimapInfoSettings(modMain, current, escape, context), (Tooltip)null, true);
      ScreenSwitchSettingEntry waypointSettingsEntry = new ScreenSwitchSettingEntry("gui.xaero_waypoint_settings", (current, escape) -> new GuiWaypointSettings(modMain, current, escape, context), (Tooltip)null, true);
      ScreenSwitchSettingEntry miscSettingsEntry = new ScreenSwitchSettingEntry("gui.xaero_minimap_misc_settings", (current, escape) -> new GuiMinimapMiscSettings(current, escape, context), (Tooltip)null, true);
      ScreenSwitchSettingEntry waypointsEntry = new ScreenSwitchSettingEntry("gui.xaero_waypoints", (current, escape) -> {
         MinimapSession minimapSession2 = BuiltInHudModules.MINIMAP.getCurrentSession();
         return minimapSession2 != null && modMain.getSettings().waypointsGUI(minimapSession2) ? new GuiWaypoints((HudMod)modMain, minimapSession2, this, escape) : null;
      }, context.isClientSide() ? null : new Tooltip(GuiConstants.SETTING_ENTRY_WRONG_CONTEXT_COMPONENT), () -> {
         if (!context.isClientSide()) {
            return false;
         } else {
            MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
            return minimapSession != null && modMain.getSettings().waypointsGUI(minimapSession);
         }
      });
      List<ISettingEntry> mainEntriesBuilder = new ArrayList();
      if (profileOptions) {
         mainEntriesBuilder.add(this.createProfileIDEntry());
         mainEntriesBuilder.add(this.optionEntry(BuiltInProfiledConfigOptions.PROFILE_NAME));
      }

      mainEntriesBuilder.addAll(Lists.newArrayList(new ISettingEntry[]{this.optionEntry(MinimapProfiledConfigOptions.DISPLAY_MINIMAP), changePositionEntry, viewSettingsEntry, blockMapSettingsEntry, entityRadarSettingsEntry, overlaySettingsEntry, infoSettingsEntry, waypointSettingsEntry, miscSettingsEntry, waypointsEntry, this.optionEntry(BuiltInProfiledConfigOptions.IGNORE_ENFORCEMENT_IF_EDITOR)}));
      if (modMain.isStandalone()) {
         mainEntriesBuilder.add(new ScreenSwitchSettingEntry("gui.xaero_reset_config_profile_default", (current, escape) -> new GuiReset(this::resetConfirmResult, par1GuiScreen, escape), (Tooltip)null, true, false));
      }

      this.mainEntries = (ISettingEntry[])mainEntriesBuilder.toArray(new ISettingEntry[0]);
      this.updateSearchableEntries();
   }

   private void resetConfirmResult(boolean b) {
      if (b) {
         HudMod.INSTANCE.getEntityRadarCategoryManager().resetRootCategorySettings(this.context);
         this.resetProfileToDefaults();
      }

      this.field_22787.method_1507(this);
   }

   private void updateSearchableEntries() {
      if (this.mainEntries != null) {
         LinkedHashSet<ISettingEntry> searchableEntriesBuilder = new LinkedHashSet();

         for(ISettingEntry entry : this.mainEntries) {
            if (entry instanceof ScreenSwitchSettingEntry) {
               ScreenSwitchSettingEntry screenSwitchEntry = (ScreenSwitchSettingEntry)entry;
               class_437 tempScreen = (class_437)screenSwitchEntry.getScreenFactory().apply(this, this);
               if (tempScreen instanceof GuiSettings) {
                  GuiSettings tempSettingsScreen = (GuiSettings)tempScreen;
                  ISettingEntry[] settingsScreenEntries = tempSettingsScreen.getEntriesCopy();
                  if (settingsScreenEntries != null) {
                     searchableEntriesBuilder.addAll(Arrays.asList(settingsScreenEntries));
                  }
               } else {
                  searchableEntriesBuilder.add(entry);
               }
            } else {
               searchableEntriesBuilder.add(entry);
            }
         }

         this.searchableEntries = (ISettingEntry[])searchableEntriesBuilder.toArray(new ISettingEntry[0]);
      }
   }

   public void method_25426() {
      if (this.entryFilter.isEmpty()) {
         this.entries = this.mainEntries;
      } else {
         this.entries = this.searchableEntries;
      }

      super.method_25426();
      if (ModSettings.serverSettings != ModSettings.defaultSettings) {
         this.screenTitle = class_2561.method_43470("§e" + class_1074.method_4662("gui.xaero_server_disabled", new Object[0]));
      }

   }
}
