package xaeroplus.util;

import com.google.common.net.InternetDomainName;
import java.nio.file.Path;
import java.util.Objects;
import net.minecraft.class_124;
import net.minecraft.class_2558;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_634;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.common.HudMod;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.map.MapProcessor;
import xaero.map.WorldMapSession;
import xaero.map.core.XaeroWorldMapCore;
import xaero.map.file.MapSaveLoad;
import xaeroplus.Globals;
import xaeroplus.XaeroPlus;
import xaeroplus.settings.Settings;

public class DataFolderResolveUtil {
   public static void resolveDataFolder(final class_634 connection, final CallbackInfoReturnable<String> cir) {
      Settings.DataFolderResolutionMode dataFolderResolutionMode = Globals.dataFolderResolutionMode;
      if (dataFolderResolutionMode == Settings.DataFolderResolutionMode.SERVER_NAME) {
         if (Objects.nonNull(connection.method_45734())) {
            String serverName = connection.method_45734().field_3752;
            if (!serverName.isEmpty()) {
               cir.setReturnValue(sanitizeDataFolderName("Multiplayer_" + serverName));
               cir.cancel();
               return;
            }
         }

         if (!class_310.method_1551().method_1496()) {
            XaeroPlus.LOGGER.error("Unable to resolve valid MC Server Name. Falling back to default Xaero data folder resolution");
         }
      } else if (dataFolderResolutionMode == Settings.DataFolderResolutionMode.BASE_DOMAIN && Objects.nonNull(connection.method_45734())) {
         String id;
         try {
            id = InternetDomainName.from(connection.method_45734().field_3761).topPrivateDomain().toString();
         } catch (IllegalArgumentException ex) {
            XaeroPlus.LOGGER.error("Error resolving BASE_DOMAIN data folder. Falling back to default Xaero resolution.", ex);
            return;
         }

         while(id.endsWith(".")) {
            id = id.substring(0, id.length() - 1);
         }

         if (!id.isEmpty()) {
            id = "Multiplayer_" + id;
            cir.setReturnValue(sanitizeDataFolderName(id));
            cir.cancel();
            return;
         }

         if (!class_310.method_1551().method_1496()) {
            XaeroPlus.LOGGER.error("Unable to resolve valid Base domain. Falling back to default Xaero data folder resolution");
         }
      }

   }

   public static class_2561 getCurrentDataDirPath() {
      try {
         WorldMapSession currentSession = XaeroWorldMapCore.currentSession;
         MapProcessor mapProcessor = currentSession.getMapProcessor();
         String mainId = mapProcessor.getMapWorld().getMainId();
         Path rootFolder = MapSaveLoad.getRootFolder(mainId);
         return class_2561.method_43470(rootFolder.toString()).method_10852(class_2561.method_43470(" (").method_10852(class_2561.method_43470("Click To Open").method_27694((style) -> style.method_10958(new class_2558.class_10607(rootFolder.toString())).method_10977(class_124.field_1065))).method_10852(class_2561.method_43470(")")));
      } catch (Throwable e) {
         XaeroPlus.LOGGER.error("Failed to get data directory", e);
         return class_2561.method_43470("Failed to get data directory");
      }
   }

   public static class_2561 getCurrentWaypointDataDirPath() {
      try {
         Path minimapDataFolder = HudMod.INSTANCE.getMinimapFolder();
         MinimapSession session = (MinimapSession)BuiltInHudModules.MINIMAP.getCurrentSession();
         Path rootFolder = session.getWorldState().getCurrentRootContainerPath().applyToFilePath(minimapDataFolder);
         return class_2561.method_43470(rootFolder.toString()).method_10852(class_2561.method_43470(" (").method_10852(class_2561.method_43470("Click To Open").method_27694((style) -> style.method_10958(new class_2558.class_10607(rootFolder.toString())).method_10977(class_124.field_1065))).method_10852(class_2561.method_43470(")")));
      } catch (Throwable e) {
         XaeroPlus.LOGGER.error("Failed to get data directory", e);
         return class_2561.method_43470("Failed to get data directory");
      }
   }

   public static String sanitizeDataFolderName(final String in) {
      String invalidChars = "[<>:\"/\\\\|?*]";
      return in.replaceAll("[<>:\"/\\\\|?*]", "_").trim();
   }
}
