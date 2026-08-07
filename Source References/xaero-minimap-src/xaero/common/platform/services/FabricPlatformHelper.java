package xaero.common.platform.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
import net.fabricmc.loader.api.metadata.ModOrigin.Kind;

public class FabricPlatformHelper implements IPlatformHelper {
   public String getPlatformName() {
      return "Fabric";
   }

   public boolean isModLoaded(String modId) {
      return FabricLoader.getInstance().isModLoaded(modId);
   }

   public boolean isDevelopmentEnvironment() {
      return FabricLoader.getInstance().isDevelopmentEnvironment();
   }

   public boolean isDedicatedServer() {
      return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
   }

   public Path getGameDir() {
      return FabricLoader.getInstance().getGameDir().normalize();
   }

   public Path getConfigDir() {
      return FabricLoader.getInstance().getConfigDir();
   }

   public Path getModFile(String modId) {
      ModContainer modContainer = (ModContainer)FabricLoader.getInstance().getModContainer(modId).orElse((Object)null);
      ModOrigin origin = modContainer.getOrigin();
      Path modFile = origin.getKind() == Kind.PATH ? (Path)origin.getPaths().get(0) : null;
      if (modFile == null) {
         try {
            Class<?> quiltLoaderClass = Class.forName("org.quiltmc.loader.api.QuiltLoader");
            Method quiltGetModContainerMethod = quiltLoaderClass.getDeclaredMethod("getModContainer", String.class);
            Class<?> quiltModContainerAPIClass = Class.forName("org.quiltmc.loader.api.ModContainer");
            Method quiltGetSourcePathsMethod = quiltModContainerAPIClass.getDeclaredMethod("getSourcePaths");
            Object quiltModContainer = ((Optional)quiltGetModContainerMethod.invoke((Object)null, modContainer.getMetadata().getId())).orElse((Object)null);
            List<List<Path>> paths = (List)quiltGetSourcePathsMethod.invoke(quiltModContainer);
            if (!paths.isEmpty() && !((List)paths.get(0)).isEmpty()) {
               modFile = (Path)((List)paths.get(0)).get(0);
            }
         } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException var11) {
         }
      }

      return modFile;
   }
}
