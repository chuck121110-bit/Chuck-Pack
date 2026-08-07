package xaero.map.graphics;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.class_310;
import xaero.lib.client.graphics.GpuTextureAndView;
import xaero.map.exception.OpenGLException;
import xaero.map.pool.PoolUnit;
import xaero.map.region.texture.BranchTextureRenderer;

public abstract class TextureUpload implements PoolUnit {
   protected GpuTextureAndView glTexture;
   private GpuBuffer glUnpackPbo;
   private int level;
   private TextureFormat internalFormat;
   private int width;
   private int height;
   private int border;
   private long pixels_buffer_offset;
   private int uploadType;

   public void create(Object... args) {
      this.glTexture = (GpuTextureAndView)args[0];
      this.glUnpackPbo = (GpuBuffer)args[1];
      this.level = (Integer)args[2];
      this.internalFormat = (TextureFormat)args[3];
      this.width = (Integer)args[4];
      this.height = (Integer)args[5];
      this.border = (Integer)args[6];
      this.pixels_buffer_offset = (Long)args[7];
   }

   public void run() throws OpenGLException {
      OpenGLException.checkGLError();
      this.upload();
      OpenGLException.checkGLError();
   }

   abstract void upload() throws OpenGLException;

   public int getUploadType() {
      return this.uploadType;
   }

   public static class Normal extends TextureUpload {
      public Normal(int uploadType) {
         super.uploadType = uploadType;
      }

      public Normal(Object... args) {
         this(0);
         this.create(args);
      }

      void upload() throws OpenGLException {
         OpenGlHelper.copyBGRAUnpackBufferToMapTexture(super.glUnpackPbo, super.glTexture.texture, super.level, super.internalFormat, super.width, super.height, 0, super.pixels_buffer_offset);
         OpenGLException.checkGLError(false, "uploading a map texture");
      }

      public void create(Object... args) {
         super.create(args);
      }
   }

   public static class SubsequentNormal extends TextureUpload {
      private int xOffset;
      private int yOffset;

      public SubsequentNormal(int uploadType) {
         super.uploadType = uploadType;
      }

      public SubsequentNormal(Object... args) {
         this(6);
         this.create(args);
      }

      void upload() throws OpenGLException {
         OpenGlHelper.copyBGRAUnpackBufferToSubMapTexture(super.glUnpackPbo, super.glTexture.texture, super.level, this.xOffset, this.yOffset, super.width, super.height, super.pixels_buffer_offset);
         OpenGLException.checkGLError();
      }

      public void create(Object... args) {
         super.create(args);
         this.xOffset = (Integer)args[8];
         this.yOffset = (Integer)args[9];
      }
   }

   public static class BranchUpdate extends TextureUpload {
      private boolean allocate;
      private GpuTextureAndView srcTextureTopLeft;
      private GpuTextureAndView srcTextureTopRight;
      private GpuTextureAndView srcTextureBottomLeft;
      private GpuTextureAndView srcTextureBottomRight;
      private BranchTextureRenderer renderer;
      private GpuBuffer glPackPbo;
      private long pboOffset;

      public BranchUpdate(int uploadType) {
         super.uploadType = uploadType;
      }

      public BranchUpdate(Object... args) {
         this((Boolean)args[8] ? 4 : 3);
         this.create(args);
      }

      void upload() throws OpenGLException {
         this.renderer.render(this.glTexture, this.srcTextureTopLeft, this.srcTextureTopRight, this.srcTextureBottomLeft, this.srcTextureBottomRight, class_310.method_1551().method_1522(), this.allocate);
         if (this.glPackPbo != null) {
            OpenGlHelper.copyTextureToBGRAPackBuffer(this.glTexture.texture, this.glPackPbo, this.pboOffset);
         }
      }

      public void create(Object... args) {
         super.create(args);
         this.allocate = (Boolean)args[8];
         this.srcTextureTopLeft = (GpuTextureAndView)args[9];
         this.srcTextureTopRight = (GpuTextureAndView)args[10];
         this.srcTextureBottomLeft = (GpuTextureAndView)args[11];
         this.srcTextureBottomRight = (GpuTextureAndView)args[12];
         this.renderer = (BranchTextureRenderer)args[13];
         this.glPackPbo = (GpuBuffer)args[14];
         this.pboOffset = (Long)args[15];
      }
   }

   public static class BranchDownload extends TextureUpload {
      private GpuBuffer glPackPbo;
      private long pboOffset;

      public BranchDownload(int uploadType) {
         super.uploadType = uploadType;
      }

      public BranchDownload(Object... args) {
         this(5);
         this.create(args);
      }

      void upload() throws OpenGLException {
         if (this.glPackPbo != null) {
            OpenGlHelper.copyTextureToBGRAPackBuffer(this.glTexture.texture, this.glPackPbo, this.pboOffset);
         }
      }

      public void create(Object... args) {
         super.create(args);
         this.glPackPbo = (GpuBuffer)args[8];
         this.pboOffset = (Long)args[9];
      }
   }
}
