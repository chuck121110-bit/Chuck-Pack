package xaeroplus.fabric.util.compat;

import java.util.Optional;
import net.fabricmc.loader.api.Version;

public record VersionCheckResult(Optional<Version> minimapVersion, Optional<Version> betterPvpVersion, Version expectedVersion) {
   public boolean minimapCompatible() {
      Optional<Version> anyVersion = this.anyPresentMinimapVersion();
      return anyVersion.isPresent() && ((Version)anyVersion.get()).compareTo(this.expectedVersion()) == 0;
   }

   public Optional<Version> anyPresentMinimapVersion() {
      return this.minimapVersion().or(this::betterPvpVersion);
   }
}
