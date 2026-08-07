package xaeroplus.fabric;

import java.util.Objects;
import java.util.Optional;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.class_310;
import xaeroplus.XaeroPlus;
import xaeroplus.fabric.util.compat.IncompatibleMinimapWarningScreen;
import xaeroplus.fabric.util.compat.VersionCheckResult;
import xaeroplus.fabric.util.compat.XaeroPlusMinimapCompatibilityChecker;
import xaeroplus.settings.Settings;
import xaeroplus.util.XaeroPlusGameTest;

public class XaeroPlusFabric implements ClientModInitializer {
   public static void initialize() {
      if (XaeroPlus.initialized.compareAndSet(false, true)) {
         XaeroPlus.LOGGER.info("Initializing XaeroPlus");
         if (!XaeroPlusMinimapCompatibilityChecker.versionCheckResult.minimapCompatible()) {
            XaeroPlus.LOGGER.error("Incompatible Xaero Minimap version detected! Expected: {} Actual: {}", XaeroPlusMinimapCompatibilityChecker.versionCheckResult.expectedVersion().getFriendlyString(), XaeroPlusMinimapCompatibilityChecker.versionCheckResult.anyPresentMinimapVersion().map(Version::getFriendlyString).orElse("None!"));
            return;
         }

         XaeroPlus.XP_VERSION = (String)FabricLoader.getInstance().getModContainer("xaeroplus").map(ModContainer::getMetadata).map(ModMetadata::getVersion).map(Version::getFriendlyString).orElse("2.x");
         XaeroPlus.initializeSettings();
         Settings.REGISTRY.getKeybindings().forEach(KeyBindingHelper::registerKeyBinding);
      }

   }

   public void onInitializeClient() {
      initialize();
      ClientLifecycleEvents.CLIENT_STARTED.register((ClientLifecycleEvents.ClientStarted)(client) -> {
         if (System.getenv("XP_CI_TEST") != null || System.getProperty("XP_CI_TEST") != null) {
            class_310.method_1551().execute(XaeroPlusGameTest::applyMixinsTest);
         }

      });
      ClientLifecycleEvents.CLIENT_STARTED.register((ClientLifecycleEvents.ClientStarted)(client) -> {
         VersionCheckResult versionCheckResult = XaeroPlusMinimapCompatibilityChecker.versionCheckResult;
         if (!versionCheckResult.minimapCompatible()) {
            Optional var10000 = versionCheckResult.minimapVersion();
            Objects.requireNonNull(versionCheckResult);
            Optional<Version> anyPresentVersion = var10000.or(versionCheckResult::betterPvpVersion);
            class_310.method_1551().method_1507(new IncompatibleMinimapWarningScreen(anyPresentVersion, versionCheckResult.expectedVersion()));
         }
      });
      ClientCommandRegistrationCallback.EVENT.register((ClientCommandRegistrationCallback)(dispatcher, registryAccess) -> XaeroPlus.registerCommands(dispatcher, registryAccess));
   }
}
