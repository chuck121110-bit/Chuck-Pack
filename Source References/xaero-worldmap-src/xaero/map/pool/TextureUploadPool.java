package xaero.map.pool;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.textures.TextureFormat;
import xaero.lib.client.graphics.GpuTextureAndView;
import xaero.map.graphics.TextureUpload;
import xaero.map.region.texture.BranchTextureRenderer;

public abstract class TextureUploadPool<T extends TextureUpload> extends MapPool<T> {
   public TextureUploadPool(int maxSize) {
      super(maxSize);
   }

   public static class Normal extends TextureUploadPool<TextureUpload.Normal> {
      public Normal(int maxSize) {
         super(maxSize);
      }

      protected TextureUpload.Normal construct(Object... args) {
         return new TextureUpload.Normal(args);
      }

      public TextureUpload.Normal get(GpuTextureAndView glTexture, GpuBuffer glPbo, int level, TextureFormat internalFormat, int width, int height, int border, long pixels_buffer_offset) {
         return (TextureUpload.Normal)super.get(new Object[]{glTexture, glPbo, level, internalFormat, width, height, border, pixels_buffer_offset});
      }
   }

   public static class SubsequentNormal extends TextureUploadPool<TextureUpload.SubsequentNormal> {
      public SubsequentNormal(int maxSize) {
         super(maxSize);
      }

      protected TextureUpload.SubsequentNormal construct(Object... args) {
         return new TextureUpload.SubsequentNormal(args);
      }

      public TextureUpload.SubsequentNormal get(GpuTextureAndView glTexture, GpuBuffer glPbo, int level, int width, int height, int border, long pixels_buffer_offset, int xOffset, int yOffset) {
         return (TextureUpload.SubsequentNormal)super.get(new Object[]{glTexture, glPbo, level, null, width, height, border, pixels_buffer_offset, xOffset, yOffset});
      }
   }

   public static class BranchUpdate extends TextureUploadPool<TextureUpload.BranchUpdate> {
      protected boolean allocate;

      public BranchUpdate(int maxSize, boolean allocate) {
         super(maxSize);
         this.allocate = allocate;
      }

      protected TextureUpload.BranchUpdate construct(Object... args) {
         return new TextureUpload.BranchUpdate(args);
      }

      public TextureUpload.BranchUpdate get(GpuTextureAndView gpuTextureView, GpuBuffer glPbo, int level, TextureFormat internalFormat, int width, int height, int border, long pixels_buffer_offset, GpuTextureAndView srcTextureTopLeft, GpuTextureAndView srcTextureTopRight, GpuTextureAndView srcTextureBottomLeft, GpuTextureAndView srcTextureBottomRight, BranchTextureRenderer renderer, GpuBuffer glPackPbo, long pboOffset) {
         return (TextureUpload.BranchUpdate)super.get(new Object[]{gpuTextureView, glPbo, level, internalFormat, width, height, border, pixels_buffer_offset, this.allocate, srcTextureTopLeft, srcTextureTopRight, srcTextureBottomLeft, srcTextureBottomRight, renderer, glPackPbo, pboOffset});
      }
   }

   public static class BranchDownload extends TextureUploadPool<TextureUpload.BranchDownload> {
      public BranchDownload(int maxSize) {
         super(maxSize);
      }

      protected TextureUpload.BranchDownload construct(Object... args) {
         return new TextureUpload.BranchDownload(args);
      }

      public TextureUpload.BranchDownload get(GpuTextureAndView glTexture, GpuBuffer glPackPbo, long pboOffset) {
         return (TextureUpload.BranchDownload)super.get(new Object[]{glTexture, null, 0, null, 0, 0, 0, 0L, glPackPbo, pboOffset});
      }
   }
}
