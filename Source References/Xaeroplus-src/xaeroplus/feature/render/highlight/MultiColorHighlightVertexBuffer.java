package xaeroplus.feature.render.highlight;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
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

public class MultiColorHighlightVertexBuffer extends AbstractHighlightVertexBuffer {
   private final MultiColorHighlightColorFunction colorFunction;

   public MultiColorHighlightVertexBuffer(final MultiColorHighlightColorFunction colorFunction) {
      this.colorFunction = colorFunction;
   }

   public void preRender(final DrawContext ctx, final Long2LongMap highlights, final int color) {
      super.preRender(ctx, highlights, color);
   }

   public void refresh(DrawContext ctx, Long2LongMap highlights, int colorAlpha) {
      this.stale = false;
      this.lastRefreshed = System.currentTimeMillis();
      this.flipped = ctx.worldmap();
      if (highlights.isEmpty()) {
         this.close();
      } else {
         class_287 bufferBuilder = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1576);
         ObjectIterator<Long2LongMap.Entry> it = Long2LongMaps.fastIterator(highlights);

         while(it.hasNext()) {
            Long2LongMap.Entry entry = (Long2LongMap.Entry)it.next();
            long pos = entry.getLongKey();
            long foundTime = entry.getLongValue();
            int color = this.colorFunction.getColor(pos, foundTime);
            int alpha = ColorHelper.getIntA(color);
            if (alpha != 0) {
               int chunkPosX = ChunkUtils.longToChunkX(pos);
               int chunkPosZ = ChunkUtils.longToChunkZ(pos);
               float x1 = (float)chunkPosX;
               float x2 = (float)(chunkPosX + 1);
               float y1 = this.flipped ? (float)(chunkPosZ + 1) : (float)chunkPosZ;
               float y2 = this.flipped ? (float)chunkPosZ : (float)(chunkPosZ + 1);
               bufferBuilder.method_22912(x1, y2, 0.0F).method_39415(color);
               bufferBuilder.method_22912(x2, y2, 0.0F).method_39415(color);
               bufferBuilder.method_22912(x2, y1, 0.0F).method_39415(color);
               bufferBuilder.method_22912(x1, y1, 0.0F).method_39415(color);
            }
         }

         class_9801 meshData = bufferBuilder.method_60794();
         if (meshData == null) {
            this.close();
         } else {
            class_9801 var22 = meshData;

            try {
               this.close();
               this.vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "Chunk Highlight Buffer", 32, meshData.method_60818());
               this.indexCount = meshData.method_60822().comp_751();
            } catch (Throwable var20) {
               if (meshData != null) {
                  try {
                     var22.close();
                  } catch (Throwable var19) {
                     var20.addSuppressed(var19);
                  }
               }

               throw var20;
            }

            if (meshData != null) {
               meshData.close();
            }

         }
      }
   }

   public void render(DrawContext ctx, Long2LongMap highlights, int color) {
      if (this.vertexBuffer != null && !this.vertexBuffer.isClosed() && this.uniformBuffer != null) {
         this.uniformBuffer.method_71121();
         GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.uniformBuffer.method_71119(), false, true);

         try {
            Std140Builder.intoBuffer(mappedView.data()).putMat4f(ctx.untranslatedMapViewMatrix()).putVec2((float)Math.floorDiv(ctx.cameraBlockX(), 16), (float)Math.floorDiv(ctx.cameraBlockZ(), 16)).putVec2((float)Math.floorMod(ctx.cameraBlockX(), 16), (float)Math.floorMod(ctx.cameraBlockZ(), 16));
         } catch (Throwable var14) {
            if (mappedView != null) {
               try {
                  mappedView.close();
               } catch (Throwable var12) {
                  var14.addSuppressed(var12);
               }
            }

            throw var14;
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
            pass.setPipeline(XaeroPlusShaders.MULTI_COLOR_HIGHLIGHT_PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamic);
            pass.setUniform("MultiColorHighlightTransforms", this.uniformBuffer.method_71119());
            pass.setIndexBuffer(indexBuffer, indexType);
            pass.setVertexBuffer(0, this.vertexBuffer);
            pass.drawIndexed(0, 0, this.indexCount, 1);
         } catch (Throwable var13) {
            if (pass != null) {
               try {
                  pass.close();
               } catch (Throwable var11) {
                  var13.addSuppressed(var11);
               }
            }

            throw var13;
         }

         if (pass != null) {
            pass.close();
         }

      }
   }
}
