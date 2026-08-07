package xaero.common.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.class_4184;
import net.minecraft.class_761;
import net.minecraft.class_9779;
import net.minecraft.class_9922;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.core.XaeroMinimapCore;

@Mixin({class_761.class})
public class MixinOptionalLevelRenderer {
   @Inject(
      at = {@At("HEAD")},
      method = {"method_22710(Lnet/minecraft/class_9922;Lnet/minecraft/class_9779;ZLnet/minecraft/class_4184;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V"}
   )
   public void onRenderLevel(class_9922 graphicsResourceAllocator, class_9779 deltaTracker, boolean bl, class_4184 camera, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, Matrix4f cullingProjectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogVector, boolean skyPass, CallbackInfo info) {
      XaeroMinimapCore.onRenderLevelMatrices(modelViewMatrix, projectionMatrix);
   }
}
