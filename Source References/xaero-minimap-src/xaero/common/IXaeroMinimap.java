package xaero.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import xaero.common.config.LegacyCommonConfigIO;
import xaero.common.events.ClientEvents;
import xaero.common.events.ClientEventsListener;
import xaero.common.events.CommonEvents;
import xaero.common.events.ModClientEvents;
import xaero.common.events.ModCommonEvents;
import xaero.common.gui.GuiHelper;
import xaero.common.mods.SupportMods;
import xaero.common.platform.Services;
import xaero.common.server.mods.SupportServerMods;
import xaero.common.server.player.ServerPlayerTickHandler;
import xaero.common.settings.ModSettings;
import xaero.common.validator.FieldValidatorHolder;
import xaero.hud.Hud;
import xaero.hud.controls.ControlsRegister;
import xaero.hud.io.HudIO;
import xaero.hud.minimap.Minimap;
import xaero.hud.minimap.player.tracker.PlayerTrackerMinimapElementRenderer;
import xaero.hud.minimap.player.tracker.system.RenderedPlayerTrackerManager;
import xaero.hud.minimap.radar.category.EntityRadarCategoryManager;
import xaero.hud.render.HudRenderer;
import xaero.lib.common.packet.IPacketHandler;
import xaero.lib.patreon.PatreonMod;

public interface IXaeroMinimap {
   Path old_waypointsFile = Services.PLATFORM.getGameDir().resolve("xaerowaypoints.txt");
   File wrongWaypointsFile = Services.PLATFORM.getGameDir().resolve("config").resolve("xaerowaypoints.txt").toFile();
   File wrongWaypointsFolder = Services.PLATFORM.getGameDir().resolve("mods").resolve("XaeroWaypoints").toFile();

   String getVersionID();

   String getFileLayoutID();

   Path getConfigFile();

   File getModJAR();

   ModSettings getSettings();

   void setSettings(ModSettings var1);

   boolean isOutdated();

   void setOutdated(boolean var1);

   String getMessage();

   void setMessage(String var1);

   String getLatestVersion();

   String getLatestVersionMD5();

   void setLatestVersion(String var1);

   void setLatestVersionMD5(String var1);

   int getNewestUpdateID();

   void setNewestUpdateID(int var1);

   PatreonMod getPatreon();

   String getVersionsURL();

   void resetSettings() throws IOException;

   String getUpdateLink();

   Object getSettingsKey();

   Object getServerSettingsKey();

   Path getWaypointsFile();

   Path getMinimapFolder();

   XaeroMinimapSession createSession();

   SupportMods getSupportMods();

   SupportServerMods getSupportServerMods();

   GuiHelper getGuiHelper();

   FieldValidatorHolder getFieldValidators();

   ControlsRegister getControlsRegister();

   ClientEvents getEvents();

   void tryLoadLater();

   void tryLoadLaterServer();

   ModClientEvents getModEvents();

   boolean isStandalone();

   EntityRadarCategoryManager getEntityRadarCategoryManager();

   boolean isFairPlay();

   PlayerTrackerMinimapElementRenderer getTrackedPlayerRenderer();

   RenderedPlayerTrackerManager getRenderedPlayerTrackerManager();

   ServerPlayerTickHandler getServerPlayerTickHandler();

   void setServerPlayerTickHandler(ServerPlayerTickHandler var1);

   IPacketHandler getMessageHandler();

   CommonEvents getCommonEvents();

   void setCommonConfigIO(LegacyCommonConfigIO var1);

   LegacyCommonConfigIO getCommonConfigIO();

   ClientEventsListener getClientEventsListener();

   PlatformContext getPlatformContext();

   void ensureControlsRegister();

   ModClientEvents getModClientEvents();

   ModCommonEvents getModCommonEvents();

   String getModId();

   boolean isLoadedClient();

   boolean isLoadedServer();

   Hud getHud();

   HudRenderer getHudRenderer();

   HudIO getHudIO();

   Minimap getMinimap();

   boolean isFirstStageLoaded();
}
