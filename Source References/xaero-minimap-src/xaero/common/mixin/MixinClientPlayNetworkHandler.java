package xaero.common.mixin;

import net.minecraft.class_2626;
import net.minecraft.class_2637;
import net.minecraft.class_2666;
import net.minecraft.class_2672;
import net.minecraft.class_2676;
import net.minecraft.class_2678;
import net.minecraft.class_2759;
import net.minecraft.class_437;
import net.minecraft.class_634;
import net.minecraft.class_6603;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.HudMod;
import xaero.common.XaeroMinimapSession;
import xaero.common.core.IXaeroMinimapClientPlayNetHandler;
import xaero.common.core.XaeroMinimapCore;

@Mixin({class_634.class})
public class MixinClientPlayNetworkHandler implements IXaeroMinimapClientPlayNetHandler {
   XaeroMinimapSession xaero_minimapSession;

   @Inject(
      at = {@At(
   value = "INVOKE",
   shift = Shift.AFTER,
   target = "Lnet/minecraft/class_2600;method_11074(Lnet/minecraft/class_2596;Lnet/minecraft/class_2547;Lnet/minecraft/class_11980;)V"
)},
      method = {"method_11100(Lnet/minecraft/class_2637;)V"}
   )
   public void onOnChunkDeltaUpdate(class_2637 packet, CallbackInfo info) {
      XaeroMinimapCore.onMultiBlockChange(packet);
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"method_38539(IILnet/minecraft/class_6603;)V"}
   )
   public void onOnChunkData(int x, int z, class_6603 packet, CallbackInfo info) {
      XaeroMinimapCore.onChunkData(x, z, packet);
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
      XaeroMinimapCore.onHandleLevelChunkWithLight(packet);
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
      XaeroMinimapCore.onHandleLightUpdatePacket(packet);
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"method_51684(Lnet/minecraft/class_2666;)V"}
   )
   public void onQueueLightRemoval(class_2666 packet, CallbackInfo info) {
      XaeroMinimapCore.onQueueLightRemoval(packet);
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
      XaeroMinimapCore.onBlockChange(packet);
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
      XaeroMinimapCore.onSpawn(packet);
   }

   public XaeroMinimapSession getXaero_minimapSession() {
      return this.xaero_minimapSession;
   }

   public void setXaero_minimapSession(XaeroMinimapSession session) {
      this.xaero_minimapSession = session;
   }

   @Inject(
      at = {@At(
   value = "INVOKE",
   shift = Shift.AFTER,
   target = "Lnet/minecraft/class_2600;method_11074(Lnet/minecraft/class_2596;Lnet/minecraft/class_2547;Lnet/minecraft/class_11980;)V"
)},
      method = {"method_11120(Lnet/minecraft/class_2678;)V"}
   )
   public void onOnGameJoin(class_2678 packet, CallbackInfo info) {
      XaeroMinimapCore.onPlayNetHandler((class_634)this, packet);
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"method_47658()V"}
   )
   public void onClose(CallbackInfo info) {
      XaeroMinimapCore.onPlayNetHandlerCleanup((class_634)this);
   }

   @Inject(
      at = {@At("HEAD")},
      method = {"method_45730(Ljava/lang/String;)V"},
      cancellable = true
   )
   public void onSendCommand(String string_1, CallbackInfo info) {
      if (XaeroMinimapCore.onLocalPlayerCommand(string_1)) {
         info.cancel();
      }

   }

   @Inject(
      at = {@At("HEAD")},
      method = {"method_71927(Ljava/lang/String;Lnet/minecraft/class_437;)V"},
      cancellable = true
   )
   public void onSendUnattendedCommand(String string_1, class_437 screen, CallbackInfo info) {
      if (XaeroMinimapCore.isModLoaded()) {
         if (HudMod.INSTANCE.getEvents().handleClientSendChatEvent(string_1)) {
            info.cancel();
         }

      }
   }
}
