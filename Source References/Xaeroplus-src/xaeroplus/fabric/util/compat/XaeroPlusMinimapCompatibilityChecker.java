package xaeroplus.fabric.util.compat;

import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import xaeroplus.XaeroPlus;

public class XaeroPlusMinimapCompatibilityChecker {
   public static VersionCheckResult versionCheckResult = versionCheck();

   private static VersionCheckResult versionCheck() {
      try {
         SemanticVersion compatibleMinimapVersion = SemanticVersion.parse(getCompatibleMinimapVersion());
         Optional<Version> minimapVersion = getVersion("xaerominimap");
         Optional<Version> betterPvpVersion = getVersion("xaerobetterpvp");
         return new VersionCheckResult(minimapVersion, betterPvpVersion, compatibleMinimapVersion);
      } catch (VersionParsingException e) {
         throw new RuntimeException(e);
      }
   }

   private static Optional<Version> getVersion(final String modId) {
      try {
         return FabricLoader.getInstance().getAllMods().stream().filter((modContainer) -> modContainer.getMetadata().getId().equals(modId)).map((modContainer) -> modContainer.getMetadata().getVersion()).map((ver) -> {
            try {
               return Version.parse(ver.getFriendlyString());
            } catch (VersionParsingException var2) {
               return null;
            }
         }).findFirst();
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Failed to check version for {}", modId, e);
         return Optional.empty();
      }
   }

   private static String getCompatibleMinimapVersion() {
      return ((ModContainer)FabricLoader.getInstance().getModContainer("xaeroplus").get()).getMetadata().getCustomValue("minimap_version").getAsString();
   }
}
