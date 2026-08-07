package xaeroplus.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_5321;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.MapProcessor;
import xaero.map.MapWriter;
import xaero.map.region.MapRegion;
import xaero.map.region.OverlayBuilder;
import xaero.map.world.MapWorld;
import xaeroplus.feature.extensions.CustomMapProcessor;
import xaeroplus.settings.Settings;

@Mixin(
   value = {MapWriter.class},
   remap = false
)
public abstract class MixinMapWriter {
   @Shadow
   private MapProcessor mapProcessor;
   @Shadow
   @Final
   private class_2338.class_2339 mutableLocalPos;
   @Shadow
   public long writeFreeSinceLastWrite;

   @Inject(
      method = {"loadPixel"},
      at = {@At("HEAD")},
      remap = false
   )
   public void setObsidianColumnLocalVar(final CallbackInfo ci, @Share("columnRoofObsidian") LocalBooleanRef columnRoofObsidianRef) {
      if (Settings.REGISTRY.transparentObsidianRoofSetting.get()) {
         columnRoofObsidianRef.set(false);
      }
   }

   @Inject(
      method = {"loadPixel"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_2680;method_26227()Lnet/minecraft/class_3610;",
   ordinal = 0
)},
      remap = true
   )
   public void obsidianRoofHeadInject(final CallbackInfo ci, @Local(argsOnly = true) final class_2818 bchunk, @Local(name = {"state"}) LocalRef<class_2680> stateRef, @Local(name = {"h"}) LocalIntRef hRef, @Local(name = {"transparentSkipY"}) LocalIntRef transparentSkipYRef, @Share("columnRoofObsidian") LocalBooleanRef columnRoofObsidianRef) {
      if (Settings.REGISTRY.transparentObsidianRoofSetting.get()) {
         class_2248 b = ((class_2680)stateRef.get()).method_26204();
         boolean blockHeightAboveYLimit = (double)hRef.get() >= Settings.REGISTRY.transparentObsidianRoofYSetting.get();
         if (blockHeightAboveYLimit) {
            boolean shouldMakeTransparent = b == class_2246.field_10540 || b == class_2246.field_22423;
            if (b == class_2246.field_10477) {
               this.mutableLocalPos.method_33098(hRef.get() - 1);
               class_2680 belowState = bchunk.method_8320(this.mutableLocalPos);
               this.mutableLocalPos.method_33098(hRef.get());
               shouldMakeTransparent = belowState.method_26204() == class_2246.field_10540 || belowState.method_26204() == class_2246.field_22423;
            }

            if (shouldMakeTransparent) {
               if (Settings.REGISTRY.transparentObsidianRoofDarkeningSetting.get() == (double)0.0F) {
                  stateRef.set(class_2246.field_10124.method_9564());
                  transparentSkipYRef.set(transparentSkipYRef.get() - 1);
               }

               if (!columnRoofObsidianRef.get()) {
                  columnRoofObsidianRef.set(true);
               }
            }
         }

      }
   }

   @WrapOperation(
      method = {"loadPixel"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/map/region/OverlayBuilder;isEmpty()Z"
)},
      remap = false
   )
   public boolean checkObsidianRoofColumn(final OverlayBuilder instance, final Operation<Boolean> original, @Share("columnRoofObsidian") final LocalBooleanRef columnRoofObsidianRef) {
      if (!Settings.REGISTRY.transparentObsidianRoofSetting.get()) {
         return (Boolean)original.call(new Object[]{instance});
      } else {
         return (Boolean)original.call(new Object[]{instance}) || columnRoofObsidianRef.get();
      }
   }

   @ModifyExpressionValue(
      method = {"loadPixelHelp"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/map/MapWriter;shouldOverlayCached(Lnet/minecraft/class_2688;)Z",
   ordinal = 0
)},
      remap = true
   )
   public boolean obsidianRoofOverlayMod(final boolean original, @Local(argsOnly = true) class_2818 bChunk, @Local(name = {"b"}) class_2248 b, @Local(name = {"h"}) int h) {
      if (Settings.REGISTRY.transparentObsidianRoofSetting.get() && (double)h > Settings.REGISTRY.transparentObsidianRoofYSetting.get()) {
         if (b == class_2246.field_10540 || b == class_2246.field_22423) {
            return true;
         }

         if (b == class_2246.field_10477) {
            this.mutableLocalPos.method_33098(h - 1);
            class_2680 belowState = bChunk.method_8320(this.mutableLocalPos);
            this.mutableLocalPos.method_33098(h);
            return belowState.method_26204() == class_2246.field_10540 || belowState.method_26204() == class_2246.field_22423;
         }
      }

      return original;
   }

   @WrapOperation(
      method = {"loadPixelHelp"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_2680;method_26193()I",
   ordinal = 1
)},
      remap = true
   )
   public int getOpacityForObsidianRoof(final class_2680 instance, final Operation<Integer> original, @Local(argsOnly = true) class_1937 world, @Local(name = {"h"}) int h) {
      if (Settings.REGISTRY.transparentObsidianRoofSetting.get() && (double)h > Settings.REGISTRY.transparentObsidianRoofYSetting.get()) {
         boolean shouldMakeTransparent = instance.method_26204() == class_2246.field_10540 || instance.method_26204() == class_2246.field_22423;
         if (instance.method_26204() == class_2246.field_10477) {
            this.mutableLocalPos.method_33098(h - 1);
            class_2680 belowState = world.method_8320(this.mutableLocalPos);
            this.mutableLocalPos.method_33098(h);
            if (belowState.method_26204() == class_2246.field_10540 || belowState.method_26204() == class_2246.field_22423) {
               shouldMakeTransparent = true;
            }
         }

         if (shouldMakeTransparent) {
            return 5;
         }
      }

      return (Integer)original.call(new Object[]{instance});
   }

   @Inject(
      method = {"loadPixel"},
      at = {@At("HEAD")},
      remap = false
   )
   public void netherCaveFixInject(final CallbackInfo ci, @Local(argsOnly = true) class_1937 world, @Local(index = 10,argsOnly = true) LocalBooleanRef caveRef, @Local(index = 11,argsOnly = true) LocalBooleanRef fullCaveRef) {
      if (Settings.REGISTRY.netherCaveFix.get()) {
         boolean nether = world.method_27983() == class_1937.field_25180;
         boolean shouldForceFullInNether = !caveRef.get() && nether;
         caveRef.set(shouldForceFullInNether || caveRef.get());
         fullCaveRef.set(shouldForceFullInNether || fullCaveRef.get());
      }

   }

   @WrapOperation(
      method = {"onRender"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/map/world/MapWorld;getCurrentDimensionId()Lnet/minecraft/class_5321;"
)},
      remap = true
   )
   public class_5321<class_1937> removeCustomDimSwitchWriterPrevention(final MapWorld mapWorld, final Operation<class_5321<class_1937>> original) {
      class_638 world = this.mapProcessor.getWorld();
      return Settings.REGISTRY.writesWhileDimSwitched.get() && world != null && mapWorld.isMultiplayer() ? world.method_27983() : (class_5321)original.call(new Object[]{mapWorld});
   }

   @Inject(
      method = {"onRender"},
      at = {@At("HEAD")}
   )
   public void setCrossDimWriteSignals(final CallbackInfo ci) {
      boolean signal = Settings.REGISTRY.writesWhileDimSwitched.get() && this.mapProcessor.getWorld() != null && this.mapProcessor.getMapWorld().isMultiplayer();
      ((CustomMapProcessor)this.mapProcessor).xaeroPlus$getLeafRegionActualDimSignal().set(signal);
      ((CustomMapProcessor)this.mapProcessor).xaeroPlus$getCurrentDimensionActualDimSignal().set(signal);
   }

   @Inject(
      method = {"onRender"},
      at = {@At("RETURN")}
   )
   public void resetSignals(final CallbackInfo ci) {
      ((CustomMapProcessor)this.mapProcessor).xaeroPlus$getLeafRegionActualDimSignal().set(false);
      ((CustomMapProcessor)this.mapProcessor).xaeroPlus$getCurrentDimensionActualDimSignal().set(false);
   }

   @WrapOperation(
      method = {"onRender"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/map/MapProcessor;getLeafMapRegion(IIIZ)Lxaero/map/region/MapRegion;"
)}
   )
   public MapRegion getActualMapRegionInOnRender(final MapProcessor mapProcessor, int caveLayer, int regX, int regZ, boolean create, final Operation<MapRegion> original) {
      if (Settings.REGISTRY.writesWhileDimSwitched.get() && mapProcessor.getMapWorld().isMultiplayer()) {
         ((CustomMapProcessor)mapProcessor).xaeroPlus$getLeafRegionActualDimSignal().set(true);
      }

      MapRegion var7;
      try {
         var7 = (MapRegion)original.call(new Object[]{mapProcessor, caveLayer, regX, regZ, create});
      } finally {
         ((CustomMapProcessor)mapProcessor).xaeroPlus$getLeafRegionActualDimSignal().set(false);
      }

      return var7;
   }
}
