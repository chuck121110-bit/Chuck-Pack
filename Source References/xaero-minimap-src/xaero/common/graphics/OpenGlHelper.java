package xaero.common.graphics;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.class_10865;
import net.minecraft.class_10868;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import xaero.lib.platform.Services;

public class OpenGlHelper {
   public static boolean isUsingOpenGL() {
      return Services.PLATFORM.getRenderDeviceHelper().getRealDevice() instanceof class_10865;
   }

   public static void resetPixelStore() {
      if (isUsingOpenGL()) {
         GlStateManager._pixelStore(3317, 4);
         GlStateManager._pixelStore(3316, 0);
         GlStateManager._pixelStore(3315, 0);
         GlStateManager._pixelStore(3314, 0);
      }
   }

   public static void bindTexture(int index, GpuTexture texture) {
      if (isUsingOpenGL()) {
         texture = Services.PLATFORM.getRenderDeviceHelper().getRealTexture(texture);
         class_10868 glTexture = (class_10868)texture;
         GlStateManager._activeTexture('蓀' + index);
         GlStateManager._bindTexture(glTexture == null ? 0 : glTexture.method_68427());
      }
   }

   public static void generateMipmaps(GpuTexture texture) {
      if (isUsingOpenGL()) {
         bindTexture(0, texture);
         GL30.glGenerateMipmap(3553);
      }
   }

   public static void fixOtherMods() {
      if (isUsingOpenGL()) {
         GlStateManager._blendFuncSeparate(1, 771, 1, 771);
         GlStateManager._blendFuncSeparate(770, 771, 1, 771);
         GlStateManager._disableDepthTest();
         GlStateManager._enableDepthTest();
         GlStateManager._depthFunc(516);
         GlStateManager._depthFunc(515);
         GlStateManager._colorMask(false, false, false, false);
         GlStateManager._colorMask(true, true, true, true);
         GlStateManager._depthMask(false);
         GlStateManager._depthMask(true);
      }
   }

   public static void clearErrors() {
      if (isUsingOpenGL()) {
         while(GL11.glGetError() != 0) {
         }

      }
   }
}
