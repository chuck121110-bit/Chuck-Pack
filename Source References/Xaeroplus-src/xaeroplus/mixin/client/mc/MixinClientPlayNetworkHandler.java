package xaeroplus.mixin.client.mc;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import java.util.function.BiConsumer;
import net.minecraft.class_2338;
import net.minecraft.class_2622;
import net.minecraft.class_2626;
import net.minecraft.class_2637;
import net.minecraft.class_2668;
import net.minecraft.class_2672;
import net.minecraft.class_2680;
import net.minecraft.class_2806;
import net.minecraft.class_634;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaeroplus.XaeroPlus;
import xaeroplus.event.ChunkBlockEntityUpdateEvent;
import xaeroplus.event.ChunkBlockUpdateEvent;
import xaeroplus.event.ChunkBlocksUpdateEvent;
import xaeroplus.event.ChunkDataEvent;
import xaeroplus.event.ClientPlaySessionFinalizedEvent;
import xaeroplus.event.ClientTeleportEvent;
import xaeroplus.event.Phase;
import xaeroplus.event.RespawnObstructedEvent;

@Mixin({class_634.class})
public class MixinClientPlayNetworkHandler {
   @Shadow
   private class_638 field_3699;

   @Inject(
      method = {"method_11128"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_634;method_38539(IILnet/minecraft/class_6603;)V"
)}
   )
   public void onChunkDataPacket(final class_2672 packet, final CallbackInfo ci, @Share("seenChunk") LocalBooleanRef seenChunkRef) {
      seenChunkRef.set(this.field_3699.method_8402(packet.method_11523(), packet.method_11524(), class_2806.field_12803, false) != null);
   }

   @Inject(
      method = {"method_11128"},
      at = {@At("RETURN")}
   )
   public void onChunkData(final class_2672 packet, final CallbackInfo ci, @Share("seenChunk") LocalBooleanRef seenChunkRef) {
      XaeroPlus.EVENT_BUS.call(new ChunkDataEvent(this.field_3699.method_8497(packet.method_11523(), packet.method_11524()), seenChunkRef.get()));
   }

   @WrapOperation(
      method = {"method_11100"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_2637;method_30621(Ljava/util/function/BiConsumer;)V"
)}
   )
   public void onChunkBlocksUpdate(final class_2637 instance, final BiConsumer<class_2338, class_2680> mutableBlockPos, final Operation<Void> original) {
      ChunkBlocksUpdateEvent event = new ChunkBlocksUpdateEvent(instance);
      event.setPhase(Phase.PRE);
      XaeroPlus.EVENT_BUS.call(event);
      original.call(new Object[]{instance, mutableBlockPos});
      event.setPhase(Phase.POST);
      XaeroPlus.EVENT_BUS.call(event);
   }

   @WrapOperation(
      method = {"method_11136"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/class_638;method_41928(Lnet/minecraft/class_2338;Lnet/minecraft/class_2680;I)V"
)}
   )
   public void onBlockUpdate(final class_638 instance, final class_2338 pos, final class_2680 state, final int flags, final Operation<Void> original, @Local(argsOnly = true) final class_2626 packet) {
      ChunkBlockUpdateEvent event = new ChunkBlockUpdateEvent(packet);
      event.setPhase(Phase.PRE);
      XaeroPlus.EVENT_BUS.call(event);
      original.call(new Object[]{instance, pos, state, flags});
      event.setPhase(Phase.POST);
      XaeroPlus.EVENT_BUS.call(event);
   }

   @Inject(
      method = {"method_11094"},
      at = {@At("RETURN")}
   )
   public void onBlockEntityData(final class_2622 packet, final CallbackInfo ci) {
      XaeroPlus.EVENT_BUS.call(new ChunkBlockEntityUpdateEvent(packet));
   }

   @Inject(
      method = {"method_47658"},
      at = {@At("RETURN")}
   )
   public void onClientSessionClose(final CallbackInfo ci) {
      XaeroPlus.EVENT_BUS.call(ClientPlaySessionFinalizedEvent.INSTANCE);
   }

   @Inject(
      method = {"method_11157"},
      at = {@At("RETURN")}
   )
   public void onTeleport(CallbackInfo ci) {
      XaeroPlus.EVENT_BUS.call(ClientTeleportEvent.INSTANCE);
   }

   @Inject(
      method = {"method_11085"},
      at = {@At("RETURN")}
   )
   private void onGameEvent(class_2668 packet, CallbackInfo ci) {
      if (packet.method_11491() == class_2668.field_25645) {
         XaeroPlus.EVENT_BUS.call(RespawnObstructedEvent.INSTANCE);
      }

   }
}
