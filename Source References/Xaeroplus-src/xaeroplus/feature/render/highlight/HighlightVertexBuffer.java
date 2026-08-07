package xaeroplus.feature.render.highlight;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.util.OptionalInt;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_290;
import net.minecraft.class_310;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import xaeroplus.feature.render.DrawContext;
import xaeroplus.feature.render.shaders.XaeroPlusShaders;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.ColorHelper;

public class HighlightVertexBuffer extends AbstractHighlightVertexBuffer {
   public void preRender(final DrawContext ctx, final Long2LongMap highlights, final int color) {
      super.preRender(ctx, highlights, color);
   }

   public void refresh(final DrawContext ctx, final Long2LongMap highlights, final int color) {
      this.stale = false;
      this.lastRefreshed = System.currentTimeMillis();
      this.flipped = ctx.worldmap();
      if (!highlights.isEmpty() && ColorHelper.getA(color) != 0.0F) {
         class_287 bufferBuilder = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1592);
         LongIterator it = highlights.keySet().iterator();

         while(it.hasNext()) {
            long highlight = it.nextLong();
            int chunkPosX = ChunkUtils.longToChunkX(highlight);
            int chunkPosZ = ChunkUtils.longToChunkZ(highlight);
            float x1 = (float)chunkPosX;
            float x2 = (float)(chunkPosX + 1);
            float y1 = this.flipped ? (float)(chunkPosZ + 1) : (float)chunkPosZ;
            float y2 = this.flipped ? (float)chunkPosZ : (float)(chunkPosZ + 1);
            bufferBuilder.method_22912(x1, y2, 0.0F);
            bufferBuilder.method_22912(x2, y2, 0.0F);
            bufferBuilder.method_22912(x2, y1, 0.0F);
            bufferBuilder.method_22912(x1, y1, 0.0F);
         }

         class_9801 meshData = bufferBuilder.method_60800();

         try {
            this.close();
            this.vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "Chunk Highlight Buffer", 32, meshData.method_60818());
            this.indexCount = meshData.method_60822().comp_751();
         } catch (Throwable var15) {
            if (meshData != null) {
               try {
                  meshData.close();
               } catch (Throwable var14) {
                  var15.addSuppressed(var14);
               }
            }

            throw var15;
         }

         if (meshData != null) {
            meshData.close();
         }

      } else {
         this.close();
      }
   }

   public void render(DrawContext ctx, Long2LongMap highlights, int color) {
      if (this.vertexBuffer != null && !this.vertexBuffer.isClosed() && this.uniformBuffer != null) {
         float a = ColorHelper.getA(color);
         float r = ColorHelper.getR(color);
         float g = ColorHelper.getG(color);
         float b = ColorHelper.getB(color);
         this.uniformBuffer.method_71121();
         GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.uniformBuffer.method_71119(), false, true);

         try {
            Std140Builder.intoBuffer(mappedView.data()).putMat4f(ctx.untranslatedMapViewMatrix()).putVec4(new Vector4f(r, g, b, a)).putVec2((float)Math.floorDiv(ctx.cameraBlockX(), 16), (float)Math.floorDiv(ctx.cameraBlockZ(), 16)).putVec2((float)Math.floorMod(ctx.cameraBlockX(), 16), (float)Math.floorMod(ctx.cameraBlockZ(), 16));
         } catch (Throwable var18) {
            if (mappedView != null) {
               try {
                  mappedView.close();
               } catch (Throwable var16) {
                  var18.addSuppressed(var16);
               }
            }

            throw var18;
         }

         if (mappedView != null) {
            mappedView.close();
         }

         GpuBufferSlice dynamic = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), new Vector4f(), new Vector3f(), new Matrix4f());
         RenderSystem.class_5590 autoIndexBuffer = RenderSystem.getSequentialBuffer(class_5596.field_27382);
         VertexFormat.class_5595 indexType = autoIndexBuffer.method_31924();
         GpuBuffer indexBuffer = autoIndexBuffer.method_68274(this.indexCount);
         RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "XaeroPlus Highlight Vertex Buffer", class_310.method_1551().method_1522().method_71639(), OptionalInt.empty());

         try {
            pass.setPipeline(XaeroPlusShaders.HIGHLIGHT_PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamic);
            pass.setUniform("HighlightTransforms", this.uniformBuffer.method_71119());
            pass.setIndexBuffer(indexBuffer, indexType);
            pass.setVertexBuffer(0, this.vertexBuffer);
            pass.drawIndexed(0, 0, this.indexCount, 1);
         } catch (Throwable var17) {
            if (pass != null) {
               try {
                  pass.close();
               } catch (Throwable var15) {
                  var17.addSuppressed(var15);
               }
            }

            throw var17;
         }

         if (pass != null) {
            pass.close();
         }

      }
   }
}
