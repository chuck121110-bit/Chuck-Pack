package xaero.map.mixin;

import net.minecraft.class_2626;
import net.minecraft.class_2637;
import net.minecraft.class_2666;
import net.minecraft.class_2672;
import net.minecraft.class_2676;
import net.minecraft.class_2759;
import net.minecraft.class_634;
import net.minecraft.class_6603;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.WorldMapSession;
import xaero.map.core.IWorldMapClientPlayNetHandler;
import xaero.map.core.XaeroWorldMapCore;

@Mixin({class_634.class})
public class MixinClientPlayNetworkHandler implements IWorldMapClientPlayNetHandler {
   @Shadow
   private int field_19144;
   WorldMapSession xaero_worldmapSession;

   @Inject(
      at = {@At(
   value = "INVOKE",
   shift = Shift.AFTER,
   target = "Lnet/minecraft/class_2600;method_11074(Lnet/minecraft/class_2596;Lnet/minecraft/class_2547;Lnet/minecraft/class_11980;)V"
)},
      method = {"method_11100(Lnet/minecraft/class_2637;)V"}
   )
   public void onOnChunkDeltaUpdate(class_2637 packet, CallbackInfo info) {
      XaeroWorldMapCore.onMultiBlockChange(packet);
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"method_38539(IILnet/minecraft/class_6603;)V"}
   )
   public void onOnChunkData(int x, int z, class_6603 packet, CallbackInfo info) {
      XaeroWorldMapCore.onChunkData(x, z, packet);
   }

   @Inject(
      at = {@At(
   value = "INVOKE",
   shift = Shift.AFTER,
   target = "Lnet/minecraft/class_2600;method_11074(Lnet/minecraft/class_2596;Lnet/minecraft/class_2547;Lnet/minecraft/class_11980;)V"
)},
      method = {"method_11128(Lnet/minecraft/class_2672;)V"}
   )
   public void onHandleLevelChunkWithLight(class_2672 packet, CallbackInfo info) {
      XaeroWorldMapCore.onHandleLevelChunkWithLight(packet);
   }

   @Inject(
      at = {@At(
   value = "INVOKE",
   shift = Shift.AFTER,
   target = "Lnet/minecraft/class_2600;method_11074(Lnet/minecraft/class_2596;Lnet/minecraft/class_2547;Lnet/minecraft/class_11980;)V"
)},
      method = {"method_11143(Lnet/minecraft/class_2676;)V"}
   )
   public void onHandleLightUpdatePacket(class_2676 packet, CallbackInfo info) {
      XaeroWorldMapCore.onHandleLightUpdatePacket(packet);
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"method_51684(Lnet/minecraft/class_2666;)V"}
   )
   public void onQueueLightRemoval(class_2666 packet, CallbackInfo info) {
      XaeroWorldMapCore.onQueueLightRemoval(packet);
   }

   @Inject(
      at = {@At(
   value = "INVOKE",
   shift = Shift.AFTER,
   target = "Lnet/minecraft/class_2600;method_11074(Lnet/minecraft/class_2596;Lnet/minecraft/class_2547;Lnet/minecraft/class_11980;)V"
)},
      method = {"method_11136(Lnet/minecraft/class_2626;)V"}
   )
   public void onOnBlockUpdate(class_2626 packet, CallbackInfo info) {
      XaeroWorldMapCore.onBlockChange(packet);
   }

   @Inject(
      at = {@At(
   value = "INVOKE",
   shift = Shift.AFTER,
   target = "Lnet/minecraft/class_2600;method_11074(Lnet/minecraft/class_2596;Lnet/minecraft/class_2547;Lnet/minecraft/class_11980;)V"
)},
      method = {"method_11142(Lnet/minecraft/class_2759;)V"}
   )
   public void onOnPlayerSpawnPosition(class_2759 packet, CallbackInfo info) {
      XaeroWorldMapCore.handlePlayerSetSpawnPacket(packet);
   }

   public WorldMapSession getXaero_worldmapSession() {
      return this.xaero_worldmapSession;
   }

   public void setXaero_worldmapSession(WorldMapSession session) {
      this.xaero_worldmapSession = session;
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"method_47658()V"}
   )
   public void onCleanup(CallbackInfo info) {
      XaeroWorldMapCore.onPlayNetHandlerCleanup((class_634)this);
   }

   public int getXaero_serverChunkRadius() {
      return this.field_19144;
   }
}
