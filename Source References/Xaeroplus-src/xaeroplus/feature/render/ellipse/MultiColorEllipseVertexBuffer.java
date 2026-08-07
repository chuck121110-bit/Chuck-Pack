package xaeroplus.feature.render.ellipse;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
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
import xaeroplus.feature.render.DrawHelper;
import xaeroplus.feature.render.shaders.XaeroPlusShaders;
import xaeroplus.util.ColorHelper;

public class MultiColorEllipseVertexBuffer extends AbstractEllipseVertexBuffer<Object2IntMap<Ellipse>> {
   private final MultiColorEllipseColorFunction colorFunction;

   public MultiColorEllipseVertexBuffer(final MultiColorEllipseColorFunction colorFunction) {
      this.colorFunction = colorFunction;
   }

   protected void refresh(final DrawContext ctx, final Object2IntMap<Ellipse> ellipses) {
      this.stale = false;
      this.flipped = ctx.worldmap();
      if (ellipses.isEmpty()) {
         this.close();
      } else {
         this.setBufferOrigin(ctx);
         class_287 bufferBuilder = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);
         boolean hasVertices = false;
         ObjectIterator<Object2IntMap.Entry<Ellipse>> iterator = Object2IntMaps.fastIterator(ellipses);

         while(iterator.hasNext()) {
            Object2IntMap.Entry<Ellipse> entry = (Object2IntMap.Entry)iterator.next();
            Ellipse ellipse = (Ellipse)entry.getKey();
            int color = this.colorFunction.getColor(ellipse, entry.getIntValue());
            float alpha = ColorHelper.getA(color);
            if (alpha != 0.0F) {
               DrawHelper.addColoredEllipseQuadToExistingBuffer(bufferBuilder, (float)(ellipse.centerX() - this.bufferOriginBlockX), (float)(ellipse.centerZ() - this.bufferOriginBlockZ), (float)ellipse.radiusX(), (float)ellipse.radiusZ(), ColorHelper.getR(color), ColorHelper.getG(color), ColorHelper.getB(color), alpha);
               hasVertices = true;
            }
         }

         if (!hasVertices) {
            this.close();
         } else {
            class_9801 meshData = bufferBuilder.method_60794();
            if (meshData == null) {
               this.close();
            } else {
               class_9801 var13 = meshData;

               try {
                  this.close();
                  this.vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "Multi Color Ellipse Buffer", 32, meshData.method_60818());
                  this.indexCount = meshData.method_60822().comp_751();
               } catch (Throwable var11) {
                  if (meshData != null) {
                     try {
                        var13.close();
                     } catch (Throwable var10) {
                        var11.addSuppressed(var10);
                     }
                  }

                  throw var11;
               }

               if (meshData != null) {
                  meshData.close();
               }

            }
         }
      }
   }

   public void render(final DrawContext ctx, final float thicknessScale) {
      if (this.vertexBuffer != null && !this.vertexBuffer.isClosed() && this.uniformBuffer != null) {
         this.uniformBuffer.method_71121();
         GpuBuffer.MappedView mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.uniformBuffer.method_71119(), false, true);

         try {
            Std140Builder.intoBuffer(mappedView.data()).putMat4f(ctx.untranslatedMapViewMatrix()).putVec2(XaeroPlusShaders.ELLIPSES_FRAME_SIZE[0], XaeroPlusShaders.ELLIPSES_FRAME_SIZE[1]).putFloat(thicknessScale).putVec2((float)((long)this.bufferOriginBlockX - (long)ctx.cameraBlockX()), (float)((long)this.bufferOriginBlockZ - (long)ctx.cameraBlockZ()));
         } catch (Throwable var13) {
            if (mappedView != null) {
               try {
                  mappedView.close();
               } catch (Throwable var11) {
                  var13.addSuppressed(var11);
               }
            }

            throw var13;
         }

         if (mappedView != null) {
            mappedView.close();
         }

         GpuBufferSlice dynamic = RenderSystem.getDynamicUniforms().method_71106(RenderSystem.getModelViewMatrix(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());
         RenderSystem.class_5590 autoIndexBuffer = RenderSystem.getSequentialBuffer(class_5596.field_27382);
         VertexFormat.class_5595 indexType = autoIndexBuffer.method_31924();
         GpuBuffer indexBuffer = autoIndexBuffer.method_68274(this.indexCount);
         RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "XaeroPlus Ellipses", class_310.method_1551().method_1522().method_71639(), OptionalInt.empty());

         try {
            pass.setPipeline(XaeroPlusShaders.ELLIPSES_PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamic);
            pass.setUniform("EllipsesTransforms", this.uniformBuffer.method_71119());
            pass.setIndexBuffer(indexBuffer, indexType);
            pass.setVertexBuffer(0, this.vertexBuffer);
            pass.drawIndexed(0, 0, this.indexCount, 1);
         } catch (Throwable var12) {
            if (pass != null) {
               try {
                  pass.close();
               } catch (Throwable var10) {
                  var12.addSuppressed(var10);
               }
            }

            throw var12;
         }

         if (pass != null) {
            pass.close();
         }

      }
   }
}
