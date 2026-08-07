package xaeroplus.fabric.util.compat;

import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class XaeroPlusCompatibleMinimapMixinPlugin implements IMixinConfigPlugin {
   public void onLoad(final String mixinPackage) {
   }

   public String getRefMapperConfig() {
      return null;
   }

   public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
      if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
         return true;
      } else if (XaeroPlusMinimapCompatibilityChecker.versionCheckResult.minimapCompatible()) {
         return true;
      } else {
         return mixinClassName.startsWith("xaeroplus") ? mixinClassName.contains("MixinMinecraftClientFabric") : true;
      }
   }

   public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) {
   }

   public List<String> getMixins() {
      return null;
   }

   public void preApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
   }

   public void postApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
   }
}
