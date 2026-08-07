package xaeroplus.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import net.minecraft.class_634;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.world.state.MinimapWorldState;
import xaero.hud.minimap.world.state.MinimapWorldStateUpdater;
import xaero.hud.path.XaeroPath;
import xaeroplus.settings.Settings;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.DataFolderResolveUtil;

@Mixin(
   value = {MinimapWorldStateUpdater.class},
   remap = false
)
public abstract class MixinMinimapWorldStateUpdater {
   @Final
   @Shadow
   private MinimapSession session;
   @Final
   @Shadow
   private class_634 connection;
   @Unique
   private class_5321<class_1937> currentDim;

   public MixinMinimapWorldStateUpdater() {
      this.currentDim = class_1937.field_25179;
   }

   @WrapOperation(
      method = {"update()V"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/hud/minimap/world/state/MinimapWorldState;setAutoWorldPath(Lxaero/hud/path/XaeroPath;)V"
)}
   )
   public void preferOverworldWpSetCustomPathOnDimUpdate(final MinimapWorldState instance, final XaeroPath autoWorldPath, final Operation<Void> original, @Local(name = {"oldAutoWorldPath"}) XaeroPath oldAutoWorldPath, @Local(name = {"potentialAutoWorldNode"}) String potentialAutoWorldNode) {
      original.call(new Object[]{instance, autoWorldPath});
      if (Settings.REGISTRY.owAutoWaypointDimension.get()) {
         class_5321<class_1937> actualDimension = ChunkUtils.getActualDimension();
         if (actualDimension == class_1937.field_25180 && this.currentDim != actualDimension) {
            XaeroPath overworldWpXaeroPath = this.session.getWorldState().getAutoRootContainerPath().resolve(this.session.getDimensionHelper().getDimensionDirectoryName(class_1937.field_25179)).resolve(potentialAutoWorldNode);
            this.session.getWorldState().setCustomWorldPath(overworldWpXaeroPath);
         }
      }

      this.currentDim = ChunkUtils.getActualDimension();
   }

   @Inject(
      method = {"getAutoRootContainerPath(I)Lxaero/hud/path/XaeroPath;"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void customDataFolderResolve(final CallbackInfoReturnable<XaeroPath> cir) {
      CallbackInfoReturnable<String> customCir = new CallbackInfoReturnable("a", true);
      DataFolderResolveUtil.resolveDataFolder(this.connection, customCir);
      if (customCir.isCancelled()) {
         cir.setReturnValue(XaeroPath.root((String)customCir.getReturnValue()));
      }

   }
}
