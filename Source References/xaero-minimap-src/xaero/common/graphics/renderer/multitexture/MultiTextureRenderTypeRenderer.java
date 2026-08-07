package xaero.common.graphics.renderer.multitexture;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.class_12247;
import net.minecraft.class_1921;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import xaero.lib.client.graphics.DynamicImmediateGpuBuffer;
import xaero.lib.client.graphics.IRenderSetup;
import xaero.lib.client.graphics.IRenderType;
import xaero.lib.client.graphics.util.ImmediateRenderUtil;

public class MultiTextureRenderTypeRenderer {
   private static final String RENDER_PASS_NAME = "xaero multitexture render pass";
   private boolean used;
   private class_9799 sharedBuffer = new class_9799(16384);
   private class_287 currentBufferBuilder;
   private List<class_9801> buffersForDrawCalls = new ArrayList();
   private List<RenderPass.class_10884<MultiTextureRenderTypeRenderer>> drawCallBuilder = new ArrayList();
   private DynamicImmediateGpuBuffer immediateVertexBuffer;
   private ArrayList<GpuTexture> texturesForDrawCalls = new ArrayList();
   private Consumer<GpuTexture> textureBinder;
   private Consumer<GpuTexture> textureFinalizer;
   private GpuTexture prevTexture;
   private class_1921 renderType;

   MultiTextureRenderTypeRenderer() {
   }

   void init(Consumer<GpuTexture> textureBinder, Consumer<GpuTexture> textureFinalizer, class_1921 renderType) {
      if (this.used) {
         throw new IllegalStateException("Multi-texture renderer already in use!");
      } else {
         this.used = true;
         this.textureBinder = textureBinder;
         this.textureFinalizer = textureFinalizer;
         this.prevTexture = null;
         this.renderType = renderType;
      }
   }

   void draw() {
      if (!this.used) {
         throw new IllegalStateException("Multi-texture renderer is not in use!");
      } else {
         if (!this.texturesForDrawCalls.isEmpty()) {
            Consumer<GpuTexture> textureBinder = this.textureBinder;
            Consumer<GpuTexture> textureFinalizer = this.textureFinalizer;
            boolean hasTextureFinalizer = textureFinalizer != null;
            this.endBuffer(this.currentBufferBuilder);
            RenderPipeline renderPipeline = this.renderType.method_73243();
            class_12247 renderSetup = ((IRenderType)this.renderType).xaero_lib_getSetup();
            IRenderSetup iRenderSetup = (IRenderSetup)renderSetup;
            class_276 target = iRenderSetup.xaero_lib_getOutputTarget().method_75921();

            for(int i = 0; i < this.texturesForDrawCalls.size(); ++i) {
               GpuTexture texture = (GpuTexture)this.texturesForDrawCalls.get(i);
               class_9801 meshData = (class_9801)this.buffersForDrawCalls.get(i);
               this.drawCallBuilder.add(this.createDrawCall(meshData, texture, renderPipeline));
               meshData.close();
            }

            GpuTextureView colorTarget = RenderSystem.outputColorTextureOverride != null ? RenderSystem.outputColorTextureOverride : target.method_71639();
            GpuTextureView depthTarget = target.field_1478 ? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : target.method_71640()) : null;
            Map<String, class_12247.class_12337> textures = renderSetup.method_75926();
            RenderPass renderPass = ImmediateRenderUtil.createRenderPass("xaero multitexture render pass", renderPipeline, colorTarget, depthTarget, textures, iRenderSetup.xaero_lib_getTextureTransform());

            try {
               renderPass.drawMultipleIndexed(this.drawCallBuilder, (GpuBuffer)null, (VertexFormat.class_5595)null, Collections.emptyList(), this);
            } catch (Throwable var15) {
               if (renderPass != null) {
                  try {
                     renderPass.close();
                  } catch (Throwable var14) {
                     var15.addSuppressed(var14);
                  }
               }

               throw var15;
            }

            if (renderPass != null) {
               renderPass.close();
            }

            if (hasTextureFinalizer) {
               for(int i = 0; i < this.texturesForDrawCalls.size(); ++i) {
                  GpuTexture texture = (GpuTexture)this.texturesForDrawCalls.get(i);
                  textureFinalizer.accept(texture);
               }
            }

            textureBinder.accept((Object)null);
         }

         this.drawCallBuilder.clear();
         this.texturesForDrawCalls.clear();
         this.buffersForDrawCalls.clear();
         if (this.immediateVertexBuffer != null) {
            this.immediateVertexBuffer.clear();
         }

         this.used = false;
         this.renderType = null;
      }
   }

   private RenderPass.class_10884<MultiTextureRenderTypeRenderer> createDrawCall(class_9801 meshData, GpuTexture texture, RenderPipeline renderPipeline) {
      ByteBuffer indexBuffer = meshData.method_60821();
      if (indexBuffer != null) {
         throw new IllegalArgumentException();
      } else {
         GpuBufferSlice gpuVertexBufferSlice = this.uploadImmediateVertexBuffer(meshData.method_60818());
         long sliceOffset = gpuVertexBufferSlice.offset();
         int sliceOffsetInVertices = (int)(sliceOffset / (long)this.renderType.method_23031().getVertexSize());
         int firstIndex = this.renderType.method_23033().method_31973(sliceOffsetInVertices);
         RenderSystem.class_5590 sequentialBuffer = RenderSystem.getSequentialBuffer(this.renderType.method_23033());
         GpuBuffer gpuIndexBuffer = sequentialBuffer.method_68274(firstIndex + meshData.method_60822().comp_751());
         VertexFormat.class_5595 gpuIndexType = sequentialBuffer.method_31924();
         return new RenderPass.class_10884(0, gpuVertexBufferSlice.buffer(), gpuIndexBuffer, gpuIndexType, firstIndex, meshData.method_60822().comp_751(), (context, uu) -> this.textureBinder.accept(texture));
      }
   }

   private GpuBufferSlice uploadImmediateVertexBuffer(ByteBuffer vertexBuffer) {
      GpuDevice gpuDevice = RenderSystem.getDevice();
      if (this.immediateVertexBuffer == null) {
         this.immediateVertexBuffer = new DynamicImmediateGpuBuffer(vertexBuffer.remaining(), 32);
      }

      GpuBufferSlice slice = this.immediateVertexBuffer.allocateSlice((long)vertexBuffer.remaining());
      gpuDevice.createCommandEncoder().writeToBuffer(slice, vertexBuffer);
      return slice;
   }

   private void endBuffer(class_287 builder) {
      this.buffersForDrawCalls.add(builder.method_60794());
   }

   public class_287 begin(GpuTexture texture) {
      if (!this.used) {
         throw new IllegalStateException("Multi-texture renderer is not in use!");
      } else if (texture == null) {
         throw new IllegalStateException("Attempted to use the multi-texture renderer with texture null!");
      } else {
         if (texture != this.prevTexture) {
            if (this.prevTexture != null) {
               this.endBuffer(this.currentBufferBuilder);
            }

            this.currentBufferBuilder = new class_287(this.sharedBuffer, this.renderType.method_23033(), this.renderType.method_23031());
            this.prevTexture = texture;
            this.texturesForDrawCalls.add(texture);
         }

         return this.currentBufferBuilder;
      }
   }
}
