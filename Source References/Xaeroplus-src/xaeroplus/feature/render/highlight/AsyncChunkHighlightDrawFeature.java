package xaeroplus.feature.render.highlight;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import xaeroplus.Globals;
import xaeroplus.feature.render.DrawContext;
import xaeroplus.feature.render.MapRenderWindow;

public class AsyncChunkHighlightDrawFeature extends AbstractChunkHighlightDrawFeature {
   private final String id;
   private final AsyncLoadingCache<Long, Long2LongMap> chunkRenderCache;
   private final AsyncChunkHighlightProvider chunkHighlightProvider;

   public AsyncChunkHighlightDrawFeature(String id, AbstractHighlightVertexBuffer drawBuffer, AsyncChunkHighlightProvider chunkHighlightProvider) {
      super(drawBuffer);
      this.id = id;
      this.chunkHighlightProvider = chunkHighlightProvider;
      this.chunkRenderCache = Caffeine.newBuilder().expireAfterWrite(10L, TimeUnit.SECONDS).refreshAfterWrite(500L, TimeUnit.MILLISECONDS).executor((Executor)Globals.cacheRefreshExecutorService.get()).removalListener((k, v, cause) -> drawBuffer.markStale()).buildAsync((CacheLoader)((k) -> this.loadFeatureHighlightsInWindow()));
   }

   private Long2LongMap loadFeatureHighlightsInWindow() {
      MapRenderWindow window = MapRenderWindow.resolveCurrent();
      return this.chunkHighlightProvider.chunkHighlightSupplier().getHighlights(window.windowX(), window.windowZ(), window.windowSize(), window.dimension());
   }

   public String id() {
      return this.id;
   }

   public Long2LongMap chunkHighlights() {
      return (Long2LongMap)this.chunkRenderCache.get(0L).getNow(Long2LongMaps.EMPTY_MAP);
   }

   public int color() {
      return this.chunkHighlightProvider.colorSupplier().getAsInt();
   }

   public void preRender(final DrawContext ctx) {
      super.preRender(ctx);
      this.drawBuffer.preRender(ctx, this.chunkHighlights(), this.color());
   }

   public void render(final DrawContext ctx) {
      this.preRender(ctx);
      this.drawBuffer.render(ctx, this.chunkHighlights(), this.color());
      this.postRender(ctx);
   }
}
