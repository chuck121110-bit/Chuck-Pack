package xaeroplus;

import com.mojang.brigadier.CommandDispatcher;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import net.lenni0451.lambdaevents.LambdaManager;
import net.lenni0451.lambdaevents.generator.LambdaMetaFactoryGenerator;
import net.minecraft.class_3797;
import net.minecraft.class_7157;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xaero.map.platform.Services;
import xaeroplus.commands.XPClientCommandSource;
import xaeroplus.commands.XPCommandManager;
import xaeroplus.event.ClientStoppingEvent;
import xaeroplus.event.MinimapInitCompletedEvent;
import xaeroplus.feature.highlights.SavableHighlightCacheInstance;
import xaeroplus.feature.keybind.KeybindListener;
import xaeroplus.module.ModuleManager;
import xaeroplus.module.impl.Drawing;
import xaeroplus.settings.SettingHooks;
import xaeroplus.settings.Settings;
import xaeroplus.settings.XaeroPlusSetting;

public class XaeroPlus {
   public static final Logger LOGGER = LoggerFactory.getLogger("XaeroPlus");
   public static final LambdaManager EVENT_BUS = LambdaManager.threadSafe(new LambdaMetaFactoryGenerator());
   public static final AtomicBoolean initialized = new AtomicBoolean(false);
   public static final File configFile;
   public static String XP_VERSION;
   public static final String MC_VERSION;
   public static final KeybindListener KEYBIND_LISTENER;

   public static void initializeSettings() {
      SettingHooks.loadXPSettings();
      Settings.REGISTRY.getAllSettings().forEach(XaeroPlusSetting::init);
      Globals.initStickySettings();
      ((Drawing)ModuleManager.getModule(Drawing.class)).enable();
      EVENT_BUS.registerConsumer((o) -> {
         if (!Globals.minimapSettingsInitialized) {
            Globals.minimapSettingsInitialized = true;
         }
      }, MinimapInitCompletedEvent.class);
      EVENT_BUS.register(KEYBIND_LISTENER);
      EVENT_BUS.registerConsumer((o) -> {
         try {
            ArrayList<CompletableFuture<Void>> futures = new ArrayList();
            Stream var10000 = SavableHighlightCacheInstance.instances().stream().map(SavableHighlightCacheInstance::shutdown);
            Objects.requireNonNull(futures);
            var10000.forEach(futures::add);
            futures.add(((Drawing)ModuleManager.getModule(Drawing.class)).shutdown());
            CompletableFuture.allOf((CompletableFuture[])futures.toArray((x$0) -> new CompletableFuture[x$0])).get(5L, TimeUnit.SECONDS);
         } catch (Exception var2) {
            LOGGER.warn("Timed out awaiting XaeroPlus cache shutdowns");
         }

      }, ClientStoppingEvent.class);
   }

   public static void registerCommands(CommandDispatcher<XPClientCommandSource> dispatcher, class_7157 context) {
      XPCommandManager.registerCommands(dispatcher, context);
   }

   static {
      configFile = Services.PLATFORM.getConfigDir().resolve("xaeroplus.txt").toFile();
      XP_VERSION = "2";
      MC_VERSION = class_3797.field_25319.comp_4025();
      KEYBIND_LISTENER = new KeybindListener();
   }
}
