package xaero.common.mixin;

import net.minecraft.class_11228;
import net.minecraft.class_11246;
import net.minecraft.class_11256;
import net.minecraft.class_310;
import net.minecraft.class_4597;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.hud.minimap.render.MinimapPipRenderState;
import xaero.hud.minimap.render.MinimapPipRenderer;

@Mixin({class_11228.class})
public class MixinOptionalGuiRenderer {
   @Shadow
   private class_4597.class_4598 field_59917;
   @Shadow
   class_11246 field_59914;
   private MinimapPipRenderer<MinimapPipRenderState> xaeroMinimapRenderer;
   private MinimapPipRenderer<MinimapPipRenderState.Enlarged> xaeroMinimapEnlargedRenderer;

   @ModifyVariable(
      method = {"method_70888(Lnet/minecraft/class_11256;I)V"},
      at = @At("HEAD"),
      index = 1
   )
   public <T extends class_11256> T onPreparePictureInPictureState(T state) {
      if (this.xaeroMinimapRenderer == null) {
         this.xaeroMinimapRenderer = new MinimapPipRenderer<MinimapPipRenderState>(MinimapPipRenderState.class, this.field_59917);
      }

      if (state.getClass() == this.xaeroMinimapRenderer.method_70903()) {
         this.xaeroMinimapRenderer.prepare((MinimapPipRenderState)state, this.field_59914, class_310.method_1551().method_22683().method_4495());
         return state;
      } else {
         if (this.xaeroMinimapEnlargedRenderer == null) {
            this.xaeroMinimapEnlargedRenderer = new MinimapPipRenderer<MinimapPipRenderState.Enlarged>(MinimapPipRenderState.Enlarged.class, this.field_59917);
         }

         if (state.getClass() == this.xaeroMinimapEnlargedRenderer.method_70903()) {
            this.xaeroMinimapEnlargedRenderer.prepare((MinimapPipRenderState.Enlarged)state, this.field_59914, class_310.method_1551().method_22683().method_4495());
         }

         return state;
      }
   }

   @Inject(
      method = {"close()V"},
      at = {@At("HEAD")}
   )
   public void onClose(CallbackInfo ci) {
      if (this.xaeroMinimapRenderer != null) {
         this.xaeroMinimapRenderer.close();
      }

      if (this.xaeroMinimapEnlargedRenderer != null) {
         this.xaeroMinimapEnlargedRenderer.close();
      }

   }
}
