package xaero.map.graphics;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.lang.reflect.Field;
import net.minecraft.class_12137;
import net.minecraft.class_12247;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_6367;
import xaero.lib.client.graphics.XaeroRenderType;
import xaero.lib.common.reflection.util.ReflectionUtils;
import xaero.map.icon.XaeroIconAtlas;

public class ImprovedFramebuffer extends class_6367 {
   private static Field MAIN_RENDER_TARGET_FIELD = ReflectionUtils.getFieldReflection(class_310.class, "mainRenderTarget", "field_1689", "Lnet/minecraft/class_276;", "f_91042_");
   private static class_276 mainRenderTargetBackup;
   private static GpuTextureView outputColorTextureOverrideBU;
   private static GpuTextureView outputDepthTextureOverrideBU;
   private class_12137 sampler;
   private class_12247.class_12337 tas;

   public ImprovedFramebuffer(int width, int height, boolean useDepthIn) {
      super((String)null, width, height, useDepthIn);
      this.sampler = XaeroRenderType.getSimpleSampler(FilterMode.LINEAR);
      GpuTextureView textureView = this.method_71639();
      if (textureView != null) {
         this.tas = new class_12247.class_12337(this.method_71639(), this.sampler);
      }
   }

   public void bindDefaultFramebuffer(class_310 mc) {
      restoreMainRenderTarget();
   }

   public void generateMipmaps() {
      OpenGlHelper.generateMipmaps(this.field_1475);
   }

   private void forceAsMainRenderTarget() {
      if (mainRenderTargetBackup == null) {
         mainRenderTargetBackup = (class_276)ReflectionUtils.getReflectFieldValue(class_310.method_1551(), MAIN_RENDER_TARGET_FIELD);
         outputColorTextureOverrideBU = RenderSystem.outputColorTextureOverride;
         outputDepthTextureOverrideBU = RenderSystem.outputDepthTextureOverride;
         RenderSystem.outputColorTextureOverride = null;
         RenderSystem.outputDepthTextureOverride = null;
      }

      ReflectionUtils.setReflectFieldValue(class_310.method_1551(), MAIN_RENDER_TARGET_FIELD, this);
   }

   public static void restoreMainRenderTarget() {
      if (mainRenderTargetBackup != null) {
         ReflectionUtils.setReflectFieldValue(class_310.method_1551(), MAIN_RENDER_TARGET_FIELD, mainRenderTargetBackup);
         if (RenderSystem.outputColorTextureOverride == null && RenderSystem.outputDepthTextureOverride == null) {
            RenderSystem.outputColorTextureOverride = outputColorTextureOverrideBU;
            RenderSystem.outputDepthTextureOverride = outputDepthTextureOverrideBU;
         }

         mainRenderTargetBackup = null;
         outputColorTextureOverrideBU = null;
         outputDepthTextureOverrideBU = null;
      }

   }

   public void bindAsMainTarget(boolean viewport) {
      this.forceAsMainRenderTarget();
   }

   public void setColorTexture(GpuTexture texture, GpuTextureView textureView) {
      if (this.field_1475 != texture || this.field_60567 != textureView) {
         this.field_1475 = texture;
         this.field_60567 = textureView;
         if (textureView == null) {
            this.tas = null;
         } else {
            this.tas = new class_12247.class_12337(textureView, this.sampler);
         }
      }
   }

   public void setColorTexture(XaeroIconAtlas atlas) {
      this.setColorTexture(atlas.getTextureId(), atlas.getTextureView());
   }

   public void setDepthTexture(GpuTexture depthTexture, GpuTextureView textureView) {
      this.field_56739 = depthTexture;
      this.field_60568 = textureView;
   }

   public class_12137 getSampler() {
      return this.sampler;
   }

   public void setSampler(class_12137 sampler) {
      if (this.sampler != sampler) {
         this.sampler = sampler;
         if (this.field_60567 != null) {
            this.tas = new class_12247.class_12337(this.field_60567, sampler);
         }

      }
   }

   public void closeColorTexture() {
      this.field_60567.close();
      this.field_1475.close();
   }

   public void closeDepthTexture() {
      this.field_60568.close();
      this.field_56739.close();
   }

   public class_12247.class_12337 getTas() {
      return this.tas;
   }
}
