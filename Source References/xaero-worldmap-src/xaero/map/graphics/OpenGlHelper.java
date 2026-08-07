package xaero.map.graphics;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import java.nio.ByteBuffer;
import java.util.List;
import net.minecraft.class_10859;
import net.minecraft.class_10865;
import net.minecraft.class_10868;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import xaero.lib.client.graphics.GpuTextureAndView;
import xaero.lib.platform.Services;
import xaero.map.WorldMap;
import xaero.map.core.IWorldMapGlBuffer;
import xaero.map.exception.OpenGLException;

public class OpenGlHelper {
   public static boolean isUsingOpenGL() {
      return Services.PLATFORM.getRenderDeviceHelper().getRealDevice() instanceof class_10865;
   }

   public static void resetPixelStore() {
      if (isUsingOpenGL()) {
         GlStateManager._pixelStore(3333, 4);
         GlStateManager._pixelStore(3330, 0);
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
         texture = Services.PLATFORM.getRenderDeviceHelper().getRealTexture(texture);
         class_10868 glTexture = (class_10868)texture;
         GlStateManager._activeTexture(33984);
         GlStateManager._bindTexture(glTexture.method_68427());
         GL30.glGenerateMipmap(3553);
      }
   }

   public static void clearErrors(boolean loud, String where) {
      if (isUsingOpenGL()) {
         int error;
         while((error = GL11.glGetError()) != 0) {
            if (loud) {
               WorldMap.LOGGER.warn("OpenGL error ({}): {}", where, error);
            }
         }

      }
   }

   public static void deleteTextures(List<GpuTextureAndView> textures, int count) {
      if (isUsingOpenGL()) {
         if (textures != null && !textures.isEmpty()) {
            for(int i = 0; i < count && !textures.isEmpty(); ++i) {
               GpuTextureAndView glTexture = (GpuTextureAndView)textures.remove(textures.size() - 1);
               glTexture.close();
            }

         }
      }
   }

   public static void deleteBuffers(List<GpuBuffer> buffers, int count) {
      if (isUsingOpenGL()) {
         if (buffers != null && !buffers.isEmpty()) {
            for(int i = 0; i < count && !buffers.isEmpty(); ++i) {
               class_10859 glBuffer = (class_10859)buffers.remove(buffers.size() - 1);
               glBuffer.close();
            }

         }
      }
   }

   public static void unbindUnpackBuffer() {
      PixelBuffers.glBindBuffer(35052, 0);
   }

   public static void unbindPackBuffer() {
      PixelBuffers.glBindBuffer(35051, 0);
   }

   public static void uploadBGRABufferToMapTexture(ByteBuffer colorBuffer, GpuTexture texture, TextureFormat internalFormat, int width, int height) {
      texture = Services.PLATFORM.getRenderDeviceHelper().getRealTexture(texture);
      if (texture instanceof class_10868 glTexture) {
         GlStateManager._activeTexture(33984);
         GlStateManager._bindTexture(glTexture.method_68427());
         GL11.glTexImage2D(3553, 0, GlConst.toGlInternalId(internalFormat), width, height, 0, 32993, 32821, colorBuffer);
      }
   }

   public static void downloadMapTextureToBGRABuffer(GpuTexture texture, ByteBuffer colorBuffer) {
      texture = Services.PLATFORM.getRenderDeviceHelper().getRealTexture(texture);
      if (texture instanceof class_10868 glTexture) {
         GlStateManager._activeTexture(33984);
         GlStateManager._bindTexture(glTexture.method_68427());
         GL11.glGetTexImage(3553, 0, 32993, 33639, colorBuffer);
      }
   }

   public static void copyTextureToBGRAPackBuffer(GpuTexture texture, GpuBuffer packBuffer, long packBufferOffset) {
      if (packBuffer instanceof class_10859 glBuffer) {
         texture = Services.PLATFORM.getRenderDeviceHelper().getRealTexture(texture);
         class_10868 glTexture = (class_10868)texture;
         OpenGLException.checkGLError();
         GlStateManager._activeTexture(33984);
         GlStateManager._bindTexture(glTexture.method_68427());
         PixelBuffers.glBindBuffer(35051, ((IWorldMapGlBuffer)glBuffer).xaero_wm_getHandle());
         GL11.glGetTexImage(3553, 0, 32993, 32821, packBufferOffset);
         PixelBuffers.glBindBuffer(35051, 0);
         OpenGLException.checkGLError();
      }
   }

   public static void copyBGRAUnpackBufferToMapTexture(GpuBuffer unpackBuffer, GpuTexture texture, int level, TextureFormat internalFormat, int width, int height, int border, long pixels_buffer_offset) {
      if (unpackBuffer instanceof class_10859 glBuffer) {
         texture = Services.PLATFORM.getRenderDeviceHelper().getRealTexture(texture);
         class_10868 glTexture = (class_10868)texture;
         GlStateManager._activeTexture(33984);
         GlStateManager._bindTexture(glTexture.method_68427());
         PixelBuffers.glBindBuffer(35052, ((IWorldMapGlBuffer)glBuffer).xaero_wm_getHandle());
         GL11.glTexImage2D(3553, level, GlConst.toGlInternalId(internalFormat), width, height, border, 32993, 32821, pixels_buffer_offset);
         PixelBuffers.glBindBuffer(35052, 0);
      }
   }

   public static void copyBGRAUnpackBufferToSubMapTexture(GpuBuffer unpackBuffer, GpuTexture texture, int level, int xOffset, int yOffset, int width, int height, long pixels_buffer_offset) {
      if (unpackBuffer instanceof class_10859 glBuffer) {
         texture = Services.PLATFORM.getRenderDeviceHelper().getRealTexture(texture);
         class_10868 glTexture = (class_10868)texture;
         GlStateManager._activeTexture(33984);
         GlStateManager._bindTexture(glTexture.method_68427());
         PixelBuffers.glBindBuffer(35052, ((IWorldMapGlBuffer)glBuffer).xaero_wm_getHandle());
         GL11.glTexSubImage2D(3553, level, xOffset, yOffset, width, height, 32993, 32821, pixels_buffer_offset);
         PixelBuffers.glBindBuffer(35052, 0);
      }
   }

   public static void fixMaxLod(GpuTexture glColorTexture, int levels) {
      bindTexture(0, glColorTexture);
      GL11.glTexParameterf(3553, 33083, (float)levels);
   }
}
