package xaeroplus.mixin.client.mc;

import net.minecraft.class_310;
import net.minecraft.class_638;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaeroplus.Globals;
import xaeroplus.XaeroPlus;
import xaeroplus.event.ClientStoppingEvent;
import xaeroplus.event.ClientTickEvent;

@Mixin(
   value = {class_310.class},
   priority = 999
)
public class MixinMinecraft {
   @Shadow
   public class_638 field_1687;

   @Inject(
      method = {"method_1523"},
      at = {@At("HEAD")}
   )
   public void renderTickHead(final CallbackInfo ci) {
      XaeroPlus.EVENT_BUS.call(ClientTickEvent.RenderPre.INSTANCE);
   }

   @Inject(
      method = {"method_1574"},
      at = {@At("HEAD")}
   )
   public void tickHead(final CallbackInfo ci) {
      XaeroPlus.EVENT_BUS.call(ClientTickEvent.Pre.INSTANCE);
   }

   @Inject(
      method = {"method_1574"},
      at = {@At("RETURN")}
   )
   public void tickReturn(final CallbackInfo ci) {
      XaeroPlus.EVENT_BUS.call(ClientTickEvent.Post.INSTANCE);
   }

   @Inject(
      method = {"method_1481"},
      at = {@At("HEAD")}
   )
   public void onLevelChangePre(final class_638 newWorld, final CallbackInfo ci) {
      class_638 prev = this.field_1687;
      if (prev != null && newWorld != null) {
         Globals.switchingDimension = true;
      }

   }

   @Inject(
      method = {"method_1481"},
      at = {@At("RETURN")}
   )
   public void onLevelChangePost(CallbackInfo info) {
      Globals.switchingDimension = false;
   }

   @Inject(
      method = {"method_1490"},
      at = {@At(
   value = "INVOKE",
   target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V",
   shift = Shift.AFTER
)}
   )
   public void onDestroy(CallbackInfo info) {
      XaeroPlus.EVENT_BUS.call(ClientStoppingEvent.INSTANCE);
   }
}
