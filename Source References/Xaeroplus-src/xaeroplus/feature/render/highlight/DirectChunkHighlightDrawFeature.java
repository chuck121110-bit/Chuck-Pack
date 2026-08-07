package xaeroplus.feature.render.highlight;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import java.util.concurrent.ThreadLocalRandom;
import xaeroplus.Globals;
import xaeroplus.feature.render.DrawContext;

public class DirectChunkHighlightDrawFeature extends AbstractChunkHighlightDrawFeature {
   private final String id;
   private final DirectChunkHighlightProvider chunkHighlightProvider;
   private int lastRefreshedHighlightCount = 0;
   private int jitterOffset = ThreadLocalRandom.current().nextInt(0, 100);
   private final int refreshIntervalMs;

   public DirectChunkHighlightDrawFeature(String id, AbstractHighlightVertexBuffer drawBuffer, DirectChunkHighlightProvider chunkHighlightProvider, int refreshIntervalMs) {
      super(drawBuffer);
      this.id = id;
      this.chunkHighlightProvider = chunkHighlightProvider;
      this.refreshIntervalMs = refreshIntervalMs;
   }

   public int color() {
      return this.chunkHighlightProvider.colorSupplier().getAsInt();
   }

   public Long2LongMap chunkHighlights() {
      return this.chunkHighlightProvider.chunkHighlightSupplier().getHighlights(Globals.getCurrentDimensionId());
   }

   public String id() {
      return this.id;
   }

   public void preRender(final DrawContext ctx) {
      super.preRender(ctx);
      Long2LongMap highlights = this.chunkHighlights();
      if (System.currentTimeMillis() - this.drawBuffer.lastRefreshed >= (long)this.refreshIntervalMs) {
         boolean highlightCountChanged = this.lastRefreshedHighlightCount != highlights.size();
         boolean refreshRegardlessThreshold = System.currentTimeMillis() - this.drawBuffer.lastRefreshed > 500L + (long)this.jitterOffset;
         if (highlightCountChanged || refreshRegardlessThreshold) {
            this.invalidateCache();
            this.lastRefreshedHighlightCount = highlights.size();
            this.jitterOffset = ThreadLocalRandom.current().nextInt(0, 100);
         }
      }

      this.drawBuffer.preRender(ctx, highlights, this.color());
   }

   public void render(final DrawContext ctx) {
      this.preRender(ctx);
      this.drawBuffer.render(ctx, this.chunkHighlights(), this.color());
      this.postRender(ctx);
   }
}
