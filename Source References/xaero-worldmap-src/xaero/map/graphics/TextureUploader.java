package xaero.map.graphics;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.textures.TextureFormat;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.GL11;
import xaero.lib.client.graphics.GpuTextureAndView;
import xaero.map.exception.OpenGLException;
import xaero.map.pool.TextureUploadPool;
import xaero.map.region.texture.BranchTextureRenderer;

public class TextureUploader {
   public static final int NORMAL = 0;
   public static final int NORMALDOWNLOAD = 1;
   public static final int BRANCHUPDATE = 3;
   public static final int BRANCHUPDATE_ALLOCATE = 4;
   public static final int BRANCHDOWNLOAD = 5;
   public static final int SUBSEQUENT_NORMAL = 6;
   private static final int DEFAULT_NORMAL_TIME = 1000000;
   private static final int DEFAULT_COMPRESSED_TIME = 1000000;
   private static final int DEFAULT_BRANCHUPDATED_TIME = 3000000;
   private static final int DEFAULT_BRANCHUPDATE_ALLOCATE_TIME = 4000000;
   private static final int DEFAULT_BRANCHDOWNLOAD_TIME = 1000000;
   private static final int DEFAULT_SUBSEQUENT_NORMAL_TIME = 1000000;
   private List<TextureUpload> textureUploadRequests = new ArrayList();
   private TextureUploadBenchmark textureUploadBenchmark;
   private final TextureUploadPool.Normal normalTextureUploadPool;
   private final TextureUploadPool.BranchUpdate branchUpdatePool;
   private final TextureUploadPool.BranchUpdate branchUpdateAllocatePool;
   private final TextureUploadPool.BranchDownload branchDownloadPool;
   private final TextureUploadPool.SubsequentNormal subsequentNormalTextureUploadPool;

   public TextureUploader(TextureUploadPool.Normal normalTextureUploadPool, TextureUploadPool.BranchUpdate branchUpdatePool, TextureUploadPool.BranchUpdate branchUpdateAllocatePool, TextureUploadPool.BranchDownload branchDownloadPool, TextureUploadPool.SubsequentNormal subsequentNormalTextureUploadPool, TextureUploadBenchmark textureUploadBenchmark) {
      this.normalTextureUploadPool = normalTextureUploadPool;
      this.textureUploadBenchmark = textureUploadBenchmark;
      this.branchUpdatePool = branchUpdatePool;
      this.branchUpdateAllocatePool = branchUpdateAllocatePool;
      this.branchDownloadPool = branchDownloadPool;
      this.subsequentNormalTextureUploadPool = subsequentNormalTextureUploadPool;
   }

   public long requestUpload(TextureUpload upload) {
      this.textureUploadRequests.add(upload);
      if (upload.getUploadType() == 0) {
         return this.textureUploadBenchmark.isFinished(0) ? Math.min(this.textureUploadBenchmark.getAverage(0), 1000000L) : 1000000L;
      } else if (upload.getUploadType() == 3) {
         return this.textureUploadBenchmark.isFinished(3) ? Math.min(this.textureUploadBenchmark.getAverage(3), 3000000L) : 3000000L;
      } else if (upload.getUploadType() == 4) {
         return this.textureUploadBenchmark.isFinished(4) ? Math.min(this.textureUploadBenchmark.getAverage(4), 4000000L) : 4000000L;
      } else if (upload.getUploadType() == 5) {
         return this.textureUploadBenchmark.isFinished(5) ? Math.min(this.textureUploadBenchmark.getAverage(5), 1000000L) : 1000000L;
      } else if (upload.getUploadType() == 6) {
         return this.textureUploadBenchmark.isFinished(6) ? Math.min(this.textureUploadBenchmark.getAverage(6), 1000000L) : 1000000L;
      } else {
         return 0L;
      }
   }

   public long requestNormal(GpuTextureAndView glTexture, GpuBuffer glPbo, int level, TextureFormat internalFormat, int width, int height, int border, long pixels_buffer_offset) {
      TextureUpload upload = this.normalTextureUploadPool.get(glTexture, glPbo, level, internalFormat, width, height, border, pixels_buffer_offset);
      return this.requestUpload(upload);
   }

   public long requestSubsequentNormal(GpuTextureAndView glTexture, GpuBuffer glPbo, int level, int width, int height, int border, long pixels_buffer_offset, int xOffset, int yOffset) {
      TextureUpload upload = this.subsequentNormalTextureUploadPool.get(glTexture, glPbo, level, width, height, border, pixels_buffer_offset, xOffset, yOffset);
      return this.requestUpload(upload);
   }

   public long requestBranchUpdate(boolean allocate, GpuTextureAndView gpuTextureView, GpuBuffer glPbo, int level, TextureFormat internalFormat, int width, int height, int border, long pixels_buffer_offset, GpuTextureAndView srcTextureTopLeft, GpuTextureAndView srcTextureTopRight, GpuTextureAndView srcTextureBottomLeft, GpuTextureAndView srcTextureBottomRight, BranchTextureRenderer renderer, GpuBuffer glPackPbo, long pboOffset) {
      TextureUpload upload;
      if (!allocate) {
         upload = this.branchUpdatePool.get(gpuTextureView, glPbo, level, internalFormat, width, height, border, pixels_buffer_offset, srcTextureTopLeft, srcTextureTopRight, srcTextureBottomLeft, srcTextureBottomRight, renderer, glPackPbo, pboOffset);
      } else {
         upload = this.branchUpdateAllocatePool.get(gpuTextureView, glPbo, level, internalFormat, width, height, border, pixels_buffer_offset, srcTextureTopLeft, srcTextureTopRight, srcTextureBottomLeft, srcTextureBottomRight, renderer, glPackPbo, pboOffset);
      }

      return this.requestUpload(upload);
   }

   public long requestBranchDownload(GpuTextureAndView glTexture, GpuBuffer glPackPbo, long pboOffset) {
      TextureUpload upload = this.branchDownloadPool.get(glTexture, glPackPbo, pboOffset);
      return this.requestUpload(upload);
   }

   public void finishNewestRequestImmediately() {
      TextureUpload newestRequest = (TextureUpload)this.textureUploadRequests.remove(this.textureUploadRequests.size() - 1);
      newestRequest.run();
      this.addToPool(newestRequest);
   }

   public void uploadTextures() throws OpenGLException {
      if (!this.textureUploadRequests.isEmpty()) {
         boolean prepared = false;

         for(int i = 0; i < this.textureUploadRequests.size(); ++i) {
            TextureUpload tu = (TextureUpload)this.textureUploadRequests.get(i);
            int type = tu.getUploadType();
            if (!this.textureUploadBenchmark.isFinished(type)) {
               if (!prepared) {
                  GL11.glFinish();
                  prepared = true;
               }

               this.textureUploadBenchmark.pre();
            }

            tu.run();
            if (!this.textureUploadBenchmark.isFinished(type)) {
               this.textureUploadBenchmark.post(type);
               prepared = true;
            }

            this.addToPool(tu);
         }

         this.textureUploadRequests.clear();
      }

   }

   private void addToPool(TextureUpload tu) {
      switch (tu.getUploadType()) {
         case 0:
            this.normalTextureUploadPool.addToPool((TextureUpload.Normal)tu);
         case 1:
         case 2:
         default:
            break;
         case 3:
            this.branchUpdatePool.addToPool((TextureUpload.BranchUpdate)tu);
            break;
         case 4:
            this.branchUpdateAllocatePool.addToPool((TextureUpload.BranchUpdate)tu);
            break;
         case 5:
            this.branchDownloadPool.addToPool((TextureUpload.BranchDownload)tu);
            break;
         case 6:
            this.subsequentNormalTextureUploadPool.addToPool((TextureUpload.SubsequentNormal)tu);
      }

   }
}
