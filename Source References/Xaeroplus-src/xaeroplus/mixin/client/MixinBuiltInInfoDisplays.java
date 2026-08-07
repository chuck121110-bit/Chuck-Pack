package xaeroplus.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xaero.hud.minimap.info.BuiltInInfoDisplays;
import xaero.hud.minimap.info.render.compile.InfoDisplayCompiler;
import xaero.hud.minimap.world.MinimapWorld;
import xaeroplus.settings.Settings;
import xaeroplus.util.ChunkUtils;

@Mixin(
   value = {BuiltInInfoDisplays.class},
   remap = false
)
public class MixinBuiltInInfoDisplays {
   @WrapOperation(
      method = {"lambda$static$20"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/hud/minimap/info/render/compile/InfoDisplayCompiler;addWords(Ljava/lang/String;)V"
)}
   )
   private static void hideAutoSubworldInfoWhenOwAutoWaypointsEnabled(final InfoDisplayCompiler instance, final String words, final Operation<Void> original, @Local(name = {"currentWorld"}) MinimapWorld currentWorld) {
      if (Settings.REGISTRY.owAutoWaypointDimension.get()) {
         class_5321<class_1937> actualDimension = ChunkUtils.getActualDimension();
         if (actualDimension == class_1937.field_25180 && currentWorld.getDimId() == class_1937.field_25179) {
            return;
         }
      }

      original.call(new Object[]{instance, words});
   }
}
