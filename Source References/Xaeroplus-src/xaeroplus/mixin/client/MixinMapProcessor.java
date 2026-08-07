package xaeroplus.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.lang.ref.WeakReference;
import java.util.Objects;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2378;
import net.minecraft.class_310;
import net.minecraft.class_5321;
import net.minecraft.class_634;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.MapProcessor;
import xaero.map.region.MapRegion;
import xaero.map.world.MapDimension;
import xaero.map.world.MapWorld;
import xaeroplus.Globals;
import xaeroplus.XaeroPlus;
import xaeroplus.event.XaeroWorldChangeEvent;
import xaeroplus.feature.extensions.CustomMapProcessor;
import xaeroplus.settings.Settings;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.DataFolderResolveUtil;

@Mixin(
   value = {MapProcessor.class},
   remap = false
)
public abstract class MixinMapProcessor implements CustomMapProcessor {
   @Unique
   private String xaeroPlus$prevWorldId;
   @Unique
   private String xaeroPlus$prevDimId;
   @Unique
   private String xaeroPlus$prevMWId;
   @Unique
   private boolean xaeroPlus$worldChange_prevMapWorldUsable;
   @Unique
   private WeakReference<class_638> xaeroPlus$worldChange_prevWorld;
   @Unique
   private String xaeroPlus$worldChange_prevCurrentMWId;
   @Unique
   private class_5321<class_1937> xaeroPlus$worldChange_prevMapWorldCurrentDimId;
   @Unique
   private boolean xaeroPlus$nextWorldChangeIsDimSwitch = false;
   @Shadow
   private class_638 world;
   @Shadow
   private MapWorld mapWorld;
   @Shadow
   private boolean mapWorldUsable;
   @Shadow
   private String currentWorldId;
   @Shadow
   private String currentDimId;
   @Shadow
   private String currentMWId;
   @Shadow
   private long mainWorldChangedTime;
   @Unique
   private static final ThreadLocal<Boolean> xaeroPlus$getLeafRegionActualDimSignal = ThreadLocal.withInitial(() -> false);
   @Unique
   private static final ThreadLocal<Boolean> xaeroPlus$getCurrentDimensionActualDimSignal = ThreadLocal.withInitial(() -> false);
   @Unique
   private long xaeroPlus$lastWorldHashCode = 0L;

   public ThreadLocal<Boolean> xaeroPlus$getLeafRegionActualDimSignal() {
      return xaeroPlus$getLeafRegionActualDimSignal;
   }

   public ThreadLocal<Boolean> xaeroPlus$getCurrentDimensionActualDimSignal() {
      return xaeroPlus$getLeafRegionActualDimSignal;
   }

   @Shadow
   public abstract String getDimensionName(final class_5321<class_1937> id);

   @Shadow
   public abstract void updateWorldSpawn(class_2338 newSpawn, class_638 world);

   @Inject(
      method = {"getMainId(ILnet/minecraft/class_634;)Ljava/lang/String;"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = true
   )
   private void getMainId(final int version, final class_634 connection, final CallbackInfoReturnable<String> cir) {
      DataFolderResolveUtil.resolveDataFolder(connection, cir);
   }

   @Inject(
      method = {"getDimensionName"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   public void getDimensionName(final class_5321<class_1937> id, final CallbackInfoReturnable<String> cir) {
      if (!Globals.nullOverworldDimensionFolder && id == class_1937.field_25179) {
         cir.setReturnValue("DIM0");
      }

   }

   @Inject(
      method = {"onWorldUnload"},
      at = {@At("HEAD")}
   )
   public void resetCustomDimOnWorldUnload(final CallbackInfo ci) {
      if (this.mapWorld != null) {
         this.mapWorld.setCustomDimensionId((class_5321)null);
      }

   }

   @Redirect(
      method = {"run"},
      at = @At(
   value = "INVOKE",
   target = "Ljava/lang/Thread;sleep(J)V"
)
   )
   public void decreaseThreadSleepTime(final long millis) throws InterruptedException {
      Thread.sleep(5L);
   }

   @Inject(
      method = {"updateWorldSynced"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/map/MapProcessor;pushRenderPause(ZZ)V",
   ordinal = 0
)}
   )
   public void capturePrevStateForWorldChangeEvent(CallbackInfo ci) {
      this.xaeroPlus$worldChange_prevMapWorldUsable = this.mapWorldUsable;
      this.xaeroPlus$worldChange_prevWorld = new WeakReference(this.world);
      this.xaeroPlus$worldChange_prevCurrentMWId = this.currentMWId;
      this.xaeroPlus$worldChange_prevMapWorldCurrentDimId = this.mapWorld != null ? this.mapWorld.getCurrentDimensionId() : null;
   }

   @Inject(
      method = {"updateWorldSynced"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/map/MapProcessor;popRenderPause(ZZ)V",
   ordinal = 0
)}
   )
   public void fireWorldChangedEvent(final CallbackInfo ci) {
      if (Globals.switchingDimension) {
         XaeroPlus.LOGGER.info("Skipping dim switch world change event firing");
         this.xaeroPlus$nextWorldChangeIsDimSwitch = true;
      } else {
         boolean dimSwitching = this.xaeroPlus$nextWorldChangeIsDimSwitch;
         this.xaeroPlus$nextWorldChangeIsDimSwitch = false;
         class_5321<class_1937> mapWorldDim = this.mapWorld != null ? this.mapWorld.getCurrentDimensionId() : null;
         XaeroWorldChangeEvent.WorldChangeType type;
         class_5321<class_1937> from;
         class_5321<class_1937> to;
         if (!this.xaeroPlus$worldChange_prevMapWorldUsable && this.mapWorldUsable && this.xaeroPlus$worldChange_prevWorld.get() == null && !dimSwitching) {
            type = XaeroWorldChangeEvent.WorldChangeType.ENTER_WORLD;
            from = null;
            to = mapWorldDim;
         } else if (this.xaeroPlus$worldChange_prevMapWorldUsable && !this.mapWorldUsable && this.world == null) {
            type = XaeroWorldChangeEvent.WorldChangeType.EXIT_WORLD;
            from = mapWorldDim;
            to = null;
         } else if (dimSwitching) {
            type = XaeroWorldChangeEvent.WorldChangeType.ACTUAL_DIMENSION_SWITCH;
            from = null;
            to = mapWorldDim;
         } else if (this.xaeroPlus$worldChange_prevMapWorldUsable && this.mapWorldUsable && this.xaeroPlus$worldChange_prevMapWorldCurrentDimId != mapWorldDim) {
            type = XaeroWorldChangeEvent.WorldChangeType.VIEWED_DIMENSION_SWITCH;
            from = this.xaeroPlus$worldChange_prevMapWorldCurrentDimId;
            to = mapWorldDim;
         } else {
            if (Objects.equals(this.xaeroPlus$worldChange_prevCurrentMWId, this.currentMWId) || this.xaeroPlus$worldChange_prevWorld.get() != this.world) {
               XaeroPlus.LOGGER.warn("Unhandled XaeroWorldChangeEvent type :(");
               return;
            }

            type = XaeroWorldChangeEvent.WorldChangeType.MULTIWORLD_SWITCH;
            from = null;
            to = null;
         }

         XaeroPlus.LOGGER.info("Firing world change event: {} from {} to {}", new Object[]{type, from, to});
         XaeroWorldChangeEvent event = new XaeroWorldChangeEvent(type, from, to);
         XaeroPlus.EVENT_BUS.call(event);
      }
   }

   @Inject(
      method = {"getCurrentDimension"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void getActualDimIfSignalSet(final CallbackInfoReturnable<String> cir) {
      if ((Boolean)xaeroPlus$getCurrentDimensionActualDimSignal.get()) {
         cir.setReturnValue(this.getDimensionName(ChunkUtils.getActualDimension()));
      }

   }

   @WrapOperation(
      method = {"getLeafMapRegion"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/map/world/MapWorld;getCurrentDimension()Lxaero/map/world/MapDimension;",
   ordinal = 0
)}
   )
   public MapDimension getLeafMapRegionActualDimensionIfSignalled(final MapWorld instance, final Operation<MapDimension> original) {
      class_638 world = this.world;
      return (Boolean)this.xaeroPlus$getLeafRegionActualDimSignal().get() && world != null && this.xaeroPlus$prevDimId != null && this.xaeroPlus$prevDimId.equals(this.getDimensionName(world.method_27983())) ? instance.getDimension(world.method_27983()) : (MapDimension)original.call(new Object[]{instance});
   }

   @Redirect(
      method = {"getLeafMapRegion"},
      at = @At(
   value = "NEW",
   target = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lxaero/map/world/MapDimension;IIIIZLnet/minecraft/class_2378;)Lxaero/map/region/MapRegion;"
),
      remap = true
   )
   public MapRegion createMapRegionInActualDimensionIfSignalled(String worldId, String dimId, String mwId, final MapDimension dim, final int x, final int z, final int caveLayer, final int initialVersion, final boolean normalMapData, final class_2378 biomeRegistry) {
      class_638 world = this.world;
      if ((Boolean)this.xaeroPlus$getLeafRegionActualDimSignal().get() && world != null && this.xaeroPlus$prevDimId != null && this.xaeroPlus$prevDimId.equals(this.getDimensionName(world.method_27983()))) {
         worldId = this.xaeroPlus$prevWorldId;
         dimId = this.xaeroPlus$prevDimId;
         mwId = this.xaeroPlus$prevMWId;
      }

      return new MapRegion(worldId, dimId, mwId, dim, x, z, caveLayer, initialVersion, normalMapData, biomeRegistry);
   }

   @WrapOperation(
      method = {"updateWorldSynced"},
      at = {@At(
   value = "INVOKE",
   target = "Lxaero/map/world/MapWorld;getCurrentDimension()Lxaero/map/world/MapDimension;",
   ordinal = 0
)},
      slice = {@Slice(
   from = @At(
   value = "INVOKE",
   target = "Lxaero/map/MapProcessor;releaseLocksIfNeeded()V",
   ordinal = 0
)
)}
   )
   public MapDimension updateWorldSyncedGetActualDimension(final MapWorld mapWorld, final Operation<MapDimension> original) {
      class_638 world = this.world;
      return Settings.REGISTRY.writesWhileDimSwitched.get() && world != null && mapWorld.isMultiplayer() ? mapWorld.getDimension(world.method_27983()) : (MapDimension)original.call(new Object[]{mapWorld});
   }

   @WrapOperation(
      method = {"updateWorldSynced"},
      at = {@At(
   value = "FIELD",
   target = "Lxaero/map/MapProcessor;currentWorldId:Ljava/lang/String;",
   opcode = 181,
   ordinal = 0
)}
   )
   public void storePrevWorldVarStates(final MapProcessor instance, final String value, final Operation<Void> original) {
      class_638 world = this.world;
      if (world != null && this.getDimensionName(world.method_27983()).equals(this.currentDimId)) {
         this.xaeroPlus$prevWorldId = this.currentWorldId;
         this.xaeroPlus$prevDimId = this.currentDimId;
         this.xaeroPlus$prevMWId = this.currentMWId;
      }

      original.call(new Object[]{instance, value});
   }

   @Inject(
      method = {"onClientTickStart"},
      at = {@At("RETURN")}
   )
   public void fixDeadlockIfSpawnPacketNeverReceived(final CallbackInfo ci) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1687 != null && mc.field_1724 != null) {
         if (this.mapWorld != null && this.world == null && this.mainWorldChangedTime == -1L) {
            if (this.mapWorld.getCurrentMultiworldType() != 2) {
               MapDimension futureDim = this.mapWorld.getFutureDimension();
               if (futureDim == null || this.mapWorld.getFutureMultiworldType(futureDim) != 2) {
                  if (this.xaeroPlus$lastWorldHashCode != (long)mc.field_1687.hashCode()) {
                     if (mc.field_1724.field_6012 > 100) {
                        this.xaeroPlus$lastWorldHashCode = (long)mc.field_1687.hashCode();
                        XaeroPlus.LOGGER.info("Unblocking map load with fallback spawn point, xaero hooks didn't receive or process DefaultSpawnPositionPacket");
                        class_2338 spawnPointPos = mc.field_1687.method_74854().method_74897();
                        this.updateWorldSpawn(spawnPointPos, mc.field_1687);
                     }

                  }
               }
            }
         }
      }
   }
}
