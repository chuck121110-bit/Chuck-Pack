package xaero.minimap;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xaero.common.HudClientOnlyBase;
import xaero.common.HudMod;
import xaero.common.XaeroMinimapSession;
import xaero.common.events.ClientEventsListener;
import xaero.common.gui.GuiHelper;
import xaero.common.mods.SupportMods;
import xaero.common.platform.Services;
import xaero.common.settings.ModSettings;
import xaero.hud.common.config.channel.register.HudModConfigCommonRegistryHandler;
import xaero.hud.config.channel.register.HudModConfigClientRegistryHandler;
import xaero.hud.controls.ControlsRegister;
import xaero.hud.xminimap.controls.XMinimapControlsRegister;
import xaero.hud.xminimap.controls.key.XMinimapKeyMappings;
import xaero.lib.client.config.channel.register.handler.IConfigChannelClientRegistryHandler;
import xaero.lib.patreon.Patreon;
import xaero.minimap.gui.MinimapGuiHelper;

public abstract class XaeroMinimap extends HudMod {
   public static final Logger LOGGER = LogManager.getLogger();
   public static XaeroMinimap instance;
   public static final String MOD_ID = "xaerominimap";

   public XaeroMinimap() {
      instance = this;
   }

   protected void loadClient() throws IOException {
      super.loadClient();
   }

   protected void loadCommon() {
      SupportMods.checkForMinimapDuplicates("xaero.pvp.BetterPVP");
      super.loadCommon();
   }

   protected String getCommonConfigFileName() {
      return "xaerominimap-common.txt";
   }

   public String getModId() {
      return "xaerominimap";
   }

   protected ModSettings createModSettings() {
      return new ModSettings(this);
   }

   protected GuiHelper createGuiHelper() {
      return new MinimapGuiHelper(this);
   }

   public String getOldConfigFileName() {
      return "xaerominimap.txt";
   }

   protected HudClientOnlyBase createClientOnly() {
      return new MinimapClientOnly();
   }

   protected String getModName() {
      return "Xaero's Minimap";
   }

   protected Logger getLogger() {
      return LOGGER;
   }

   protected ClientEventsListener createForgeEventHandlerListener() {
      return new ClientEventsListener();
   }

   public String getVersionsURL() {
      return "http://data.chocolateminecraft.com/Versions_" + Patreon.getKEY_VERSION2() + "/Minimap.dat";
   }

   public String getUpdateLink() {
      return "http://chocolateminecraft.com/update/minimap.html";
   }

   protected ControlsRegister createControlsRegister() {
      return new XMinimapControlsRegister();
   }

   public XaeroMinimapSession createSession() {
      return new XaeroMinimapStandaloneSession(this);
   }

   public Object getSettingsKey() {
      return XMinimapKeyMappings.SETTINGS;
   }

   public Object getServerSettingsKey() {
      return XMinimapKeyMappings.SERVER_PROFILES;
   }

   public Path getConfigSubFolder() {
      return Services.PLATFORM.getConfigDir().resolve("xaero").resolve("minimap");
   }

   public Path getDefaultConfigsSubFolder() {
      return Services.PLATFORM.getConfigDir().resolveSibling("defaultconfigs").resolve("xaero").resolve("minimap");
   }

   protected HudModConfigCommonRegistryHandler createConfigCommonRegistryHandler() {
      return new HudModConfigCommonRegistryHandler();
   }

   protected Supplier<IConfigChannelClientRegistryHandler> createConfigClientRegistryHandlerSupplier() {
      return HudModConfigClientRegistryHandler::new;
   }

   public boolean isStandalone() {
      return true;
   }
}
