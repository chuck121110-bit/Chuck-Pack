package xaero.map.graphics;

import com.mojang.blaze3d.buffers.GpuBuffer;
import java.util.ArrayList;
import xaero.lib.client.graphics.GpuTextureAndView;

public class GpuObjectDeleter {
   private static final int DELETE_PER_FRAME = 5;
   private ArrayList<GpuTextureAndView> texturesToDelete = new ArrayList();
   private ArrayList<GpuBuffer> buffersToDelete = new ArrayList();

   public void work() {
      if (!this.texturesToDelete.isEmpty()) {
         do {
            synchronized(this.texturesToDelete) {
               OpenGlHelper.deleteTextures(this.texturesToDelete, 5);
            }
         } while(this.texturesToDelete.size() > 640);
      }

      if (!this.buffersToDelete.isEmpty()) {
         do {
            synchronized(this.buffersToDelete) {
               OpenGlHelper.deleteBuffers(this.buffersToDelete, 5);
            }
         } while(this.buffersToDelete.size() > 640);
      }

   }

   public void requestTextureDeletion(GpuTextureAndView texture) {
      synchronized(this.texturesToDelete) {
         this.texturesToDelete.add(texture);
      }
   }

   public void requestBufferToDelete(GpuBuffer bufferId) {
      synchronized(this.buffersToDelete) {
         this.buffersToDelete.add(bufferId);
      }
   }
}
