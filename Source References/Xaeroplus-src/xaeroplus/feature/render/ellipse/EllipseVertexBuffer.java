package xaeroplus.feature.render.ellipse;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.util.List;
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

public class EllipseVertexBuffer extends AbstractEllipseVertexBuffer<List<Ellipse>> {
   private int color = -1;

   public void setColor(final int color) {
      if (this.color != color) {
         this.color = color;
         this.markStale();
      }

   }

   protected void refresh(final DrawContext ctx, final List<Ellipse> ellipses) {
      this.stale = false;
      this.flipped = ctx.worldmap();
      if (!ellipses.isEmpty() && ColorHelper.getA(this.color) != 0.0F) {
         this.setBufferOrigin(ctx);
         float r = ColorHelper.getR(this.color);
         float g = ColorHelper.getG(this.color);
         float b = ColorHelper.getB(this.color);
         float a = ColorHelper.getA(this.color);
         class_287 bufferBuilder = class_289.method_1348().method_60827(class_5596.field_27382, class_290.field_1575);

         for(Ellipse ellipse : ellipses) {
            DrawHelper.addColoredEllipseQuadToExistingBuffer(bufferBuilder, (float)(ellipse.centerX() - this.bufferOriginBlockX), (float)(ellipse.centerZ() - this.bufferOriginBlockZ), (float)ellipse.radiusX(), (float)ellipse.radiusZ(), r, g, b, a);
         }

         class_9801 meshData = bufferBuilder.method_60800();

         try {
            this.close();
            this.vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "Ellipse Buffer", 32, meshData.method_60818());
            this.indexCount = meshData.method_60822().comp_751();
         } catch (Throwable var12) {
            if (meshData != null) {
               try {
                  meshData.close();
               } catch (Throwable var11) {
                  var12.addSuppressed(var11);
               }
            }

            throw var12;
         }

         if (meshData != null) {
            meshData.close();
         }

      } else {
         this.close();
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
