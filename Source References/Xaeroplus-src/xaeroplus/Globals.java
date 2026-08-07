package xaeroplus;

import com.google.common.base.Suppliers;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_5321;
import xaero.hud.HudSession;
import xaero.map.MapProcessor;
import xaero.map.WorldMapSession;
import xaero.map.core.XaeroWorldMapCore;
import xaero.map.world.MapWorld;
import xaeroplus.event.ClientPlaySessionFinalizedEvent;
import xaeroplus.feature.render.DrawManager;
import xaeroplus.settings.Settings;

public class Globals {
   public static final DrawManager drawManager = new DrawManager();
   public static final class_2960 guiTextures = class_2960.method_60655("xaeroplus", "gui/xpgui.png");
   public static boolean nullOverworldDimensionFolder = false;
   public static Settings.DataFolderResolutionMode dataFolderResolutionMode;
   public static int minimapScaleMultiplier;
   public static int minimapSizeMultiplier;
   public static boolean shouldResetFBO;
   public static boolean minimapSettingsInitialized;
   public static boolean switchingDimension;
   public static boolean disableDrawCullingOverride;
   public static final boolean atomicMoveAvailable;
   public static boolean transparentWmBgApplyMapBlend;
   public static boolean transparentWmBgApplyMapFrameBlend;
   public static boolean bypassVertexCountLimit;
   public static ByteArrayOutputStream zipFastByteBuffer;
   public static final Supplier<ExecutorService> cacheRefreshExecutorService;
   public static final Supplier<ExecutorService> moduleExecutorService;

   public static class_5321<class_1937> getCurrentDimensionId() {
      try {
         class_5321<class_1937> dim = XaeroWorldMapCore.currentSession.getMapProcessor().getMapWorld().getCurrentDimensionId();
         return dim == null ? class_1937.field_25179 : dim;
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Failed getting current dimension id", e);
         return class_1937.field_25179;
      }
   }

   public static void initStickySettings() {
      nullOverworldDimensionFolder = Settings.REGISTRY.nullOverworldDimensionFolder.get();
      dataFolderResolutionMode = Settings.REGISTRY.dataFolderResolutionMode.get();
      minimapScaleMultiplier = Settings.REGISTRY.minimapScaleMultiplierSetting.getAsInt();
      minimapSizeMultiplier = Settings.REGISTRY.minimapSizeMultiplierSetting.getAsInt();
      XaeroPlus.EVENT_BUS.registerConsumer((e) -> {
         nullOverworldDimensionFolder = Settings.REGISTRY.nullOverworldDimensionFolder.get();
         dataFolderResolutionMode = Settings.REGISTRY.dataFolderResolutionMode.get();
      }, ClientPlaySessionFinalizedEvent.class);
   }

   public static void switchToDimension(final class_5321<class_1937> newDimId) {
      if (newDimId != null) {
         try {
            WorldMapSession session = XaeroWorldMapCore.currentSession;
            if (session == null) {
               return;
            }

            MapProcessor mapProcessor = session.getMapProcessor();
            if (mapProcessor == null) {
               return;
            }

            MapWorld mapWorld = mapProcessor.getMapWorld();
            if (mapWorld == null) {
               return;
            }

            mapWorld.setCustomDimensionId(newDimId);
            mapProcessor.checkForWorldUpdate();
         } catch (Exception e) {
            XaeroPlus.LOGGER.error("Failed switching to dimension: {}", newDimId, e);
         }

      }
   }

   public static void setNullOverworldDimFolderIfAble(final boolean b) {
      try {
         WorldMapSession currentWMSession = XaeroWorldMapCore.currentSession;
         HudSession currentMMSession = HudSession.getCurrentSession();
         if (currentWMSession != null || currentMMSession != null) {
            return;
         }

         nullOverworldDimensionFolder = b;
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Failed setting nullOverworldDimensionFolder", e);
      }

   }

   public static void setDataFolderResolutionModeIfAble(Settings.DataFolderResolutionMode mode) {
      try {
         WorldMapSession currentWMSession = XaeroWorldMapCore.currentSession;
         HudSession currentMMSession = HudSession.getCurrentSession();
         if (currentWMSession != null || currentMMSession != null) {
            return;
         }

         dataFolderResolutionMode = mode;
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Failed setting data folder resolution mode", e);
      }

   }

   private static boolean testAtomicMoveAvailable() {
      try {
         File tempFile = File.createTempFile("xp-atomic-move-test", "test");
         tempFile.deleteOnExit();
         File tempFile2 = File.createTempFile("xp-atomic-move-test", "test");
         tempFile2.deleteOnExit();
         Files.move(tempFile.toPath(), tempFile2.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
         XaeroPlus.LOGGER.debug("Atomic move supported");
         return true;
      } catch (AtomicMoveNotSupportedException e) {
         XaeroPlus.LOGGER.debug("Atomic move not supported", e);
         return false;
      } catch (Exception e) {
         XaeroPlus.LOGGER.debug("Failed testing atomic move support", e);
         return false;
      }
   }

   static {
      dataFolderResolutionMode = Settings.DataFolderResolutionMode.IP;
      minimapScaleMultiplier = 1;
      minimapSizeMultiplier = 1;
      shouldResetFBO = false;
      minimapSettingsInitialized = false;
      switchingDimension = false;
      disableDrawCullingOverride = false;
      atomicMoveAvailable = testAtomicMoveAvailable();
      transparentWmBgApplyMapBlend = false;
      transparentWmBgApplyMapFrameBlend = false;
      bypassVertexCountLimit = false;
      zipFastByteBuffer = new ByteArrayOutputStream();
      cacheRefreshExecutorService = Suppliers.memoize(() -> Executors.newFixedThreadPool(Math.max(1, Math.min(Runtime.getRuntime().availableProcessors() / 2, 2)), (new ThreadFactoryBuilder()).setNameFormat("XaeroPlus-Cache-Refresh-%d").setUncaughtExceptionHandler((t, e) -> XaeroPlus.LOGGER.error("Caught unhandled exception in cache refresh executor", e)).setDaemon(true).build()));
      moduleExecutorService = Suppliers.memoize(() -> Executors.newFixedThreadPool(Math.max(1, Math.min(Runtime.getRuntime().availableProcessors() / 2, 2)), (new ThreadFactoryBuilder()).setNameFormat("XaeroPlus-Module-%d").setUncaughtExceptionHandler((t, e) -> XaeroPlus.LOGGER.error("Caught unhandled exception in module executor", e)).setDaemon(true).build()));
   }
}
