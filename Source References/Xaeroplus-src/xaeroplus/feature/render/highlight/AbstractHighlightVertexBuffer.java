package xaeroplus.feature.render.highlight;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import net.minecraft.class_11285;
import org.jetbrains.annotations.Nullable;
import xaeroplus.Globals;
import xaeroplus.feature.render.DrawContext;

public abstract class AbstractHighlightVertexBuffer {
   protected boolean stale = true;
   protected @Nullable GpuBuffer vertexBuffer = null;
   protected boolean flipped = false;
   public long lastRefreshed = 0L;
   public int indexCount = 0;
   public class_11285 uniformBuffer = null;

   public boolean needsRefresh(DrawContext ctx) {
      return this.vertexBuffer == null || this.vertexBuffer.isClosed() || this.stale || this.flipped != ctx.worldmap() || this.uniformBuffer == null;
   }

   public void preRender(final DrawContext ctx, final Long2LongMap highlights, final int color) {
      if (this.needsRefresh(ctx)) {
         Globals.bypassVertexCountLimit = true;
         this.refresh(ctx, highlights, color);
         Globals.bypassVertexCountLimit = false;
      }

      if (this.uniformBuffer == null) {
         this.uniformBuffer = new class_11285(() -> "XaeroPlus Highlight Uniform Buffer", 130, (new Std140SizeCalculator()).putMat4f().putMat4f().putMat4f().putVec4().putVec2().putVec2().get());
      }

   }

   public abstract void refresh(DrawContext ctx, Long2LongMap highlights, int color);

   public abstract void render(DrawContext ctx, Long2LongMap highlights, int color);

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
