package xaeroplus.feature.render.line;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.class_11285;
import org.jetbrains.annotations.Nullable;
import xaeroplus.feature.render.DrawContext;

public abstract class AbstractLineVertexBuffer<T> {
   protected boolean stale = true;
   protected @Nullable GpuBuffer vertexBuffer = null;
   protected boolean flipped = false;
   protected int indexCount = 0;
   public class_11285 uniformBuffer = null;
   protected int bufferOriginBlockX;
   protected int bufferOriginBlockZ;

   public boolean needsRefresh(final DrawContext ctx) {
      return this.vertexBuffer == null || this.vertexBuffer.isClosed() || this.stale || this.flipped != ctx.worldmap() || this.uniformBuffer == null;
   }

   public void preRender(final DrawContext ctx, final T lines) {
      if (this.needsRefresh(ctx)) {
         this.refresh(ctx, lines);
      }

      if (this.uniformBuffer == null) {
         this.uniformBuffer = new class_11285(() -> "XaeroPlus Lines Uniform Buffer", 130, (new Std140SizeCalculator()).putMat4f().putVec2().putFloat().putVec2().get());
      }

   }

   protected void setBufferOrigin(final DrawContext ctx) {
      this.bufferOriginBlockX = ctx.cameraBlockX();
      this.bufferOriginBlockZ = ctx.cameraBlockZ();
   }

   protected abstract void refresh(DrawContext ctx, T lines);

   public abstract void render(DrawContext ctx, float lineWidthScale);

   public void markStale() {
      this.stale = true;
   }

   public void close() {
      if (this.vertexBuffer != null) {
         this.vertexBuffer.close();
         this.vertexBuffer = null;
      }

      if (this.uniformBuffer != null) {
         this.uniformBuffer.close();
         this.uniformBuffer = null;
      }

   }
}
