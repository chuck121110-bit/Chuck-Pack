package xaero.hud.minimap.config.listener.handler;

import xaero.common.HudMod;
import xaero.common.minimap.highlight.DimensionHighlighterHandler;
import xaero.hud.gui.util.GuiUtils;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.Minimap;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.module.MinimapSession;
import xaero.lib.client.config.listener.ClientConfigChangeListener;
import xaero.lib.common.config.Config;

public class MinimapConfigOptionClientHandlers {
   private static void resetImage() {
      MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
      if (minimapSession != null) {
         minimapSession.getProcessor().setToResetImage(true);
      }

   }

   private static void handleSafeMode(Config config) {
      resetImage();
      GuiUtils.refreshScreenBase();
   }

   private static void handleMinimapItem(Config config) {
      MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
      if (minimapSession != null) {
         minimapSession.getProcessor().updateMinimapItem();
      }

   }

   private static void handleEntityRadarCategories(Config config) {
      HudMod.INSTANCE.getEntityRadarCategoryManager().updateFromConfigChange(config);
   }

   private static void requestClaimsRefresh() {
      MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
      if (minimapSession != null) {
         DimensionHighlighterHandler hh = minimapSession.getProcessor().getMinimapWriter().getDimensionHighlightHandler();
         if (hh != null) {
            hh.requestRefresh();
         }
      }
   }

   private static void handleOpacClaims(Config config) {
      requestClaimsRefresh();
   }

   private static void handleOpacClaimsOpacity(Config config) {
      if ((Boolean)HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getEffective(MinimapProfiledConfigOptions.OPAC_CLAIMS)) {
         requestClaimsRefresh();
      }
   }

   private static void handleLegibleCaveMaps(Config config) {
      resetImage();
   }

   private static void handleDisplayWorldMapChunks(Config config) {
      GuiUtils.refreshScreenBase();
   }

   private static void handleBlockTransparency(Config config) {
      resetImage();
   }

   private static void handleDisplayStainedGlass(Config config) {
      resetImage();
   }

   private static void handleInfoDisplayConfig(Config config) {
      Minimap minimap = HudMod.INSTANCE.getMinimap();
      if (minimap != null) {
         if (config != HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getServerSynced().getConfig()) {
            minimap.getInfoDisplays().getManager().applyLocalConfig();
         }

         minimap.getInfoDisplays().clearStateCache();
      }
   }

   public static void registerAll(ClientConfigChangeListener listener) {
      listener.register(MinimapProfiledConfigOptions.SAFE_MODE, MinimapConfigOptionClientHandlers::handleSafeMode);
      listener.register(MinimapProfiledConfigOptions.MINIMAP_ITEM, MinimapConfigOptionClientHandlers::handleMinimapItem);
      listener.register(MinimapProfiledConfigOptions.RADAR_CATEGORIES, MinimapConfigOptionClientHandlers::handleEntityRadarCategories);
      listener.register(MinimapProfiledConfigOptions.LEGIBLE_CAVE_MAPS, MinimapConfigOptionClientHandlers::handleLegibleCaveMaps);
      listener.register(MinimapProfiledConfigOptions.DISPLAY_WORLD_MAP_CHUNKS, MinimapConfigOptionClientHandlers::handleDisplayWorldMapChunks);
      listener.register(MinimapProfiledConfigOptions.BLOCK_TRANSPARENCY, MinimapConfigOptionClientHandlers::handleBlockTransparency);
      listener.register(MinimapProfiledConfigOptions.DISPLAY_STAINED_GLASS, MinimapConfigOptionClientHandlers::handleDisplayStainedGlass);
      listener.register(MinimapProfiledConfigOptions.OPAC_CLAIMS, MinimapConfigOptionClientHandlers::handleOpacClaims);
      listener.register(MinimapProfiledConfigOptions.OPAC_CLAIMS_FILL_OPACITY, MinimapConfigOptionClientHandlers::handleOpacClaimsOpacity);
      listener.register(MinimapProfiledConfigOptions.OPAC_CLAIMS_BORDER_OPACITY, MinimapConfigOptionClientHandlers::handleOpacClaimsOpacity);
      listener.register(MinimapProfiledConfigOptions.INFO_DISPLAY_CONFIG, MinimapConfigOptionClientHandlers::handleInfoDisplayConfig);
   }
}
