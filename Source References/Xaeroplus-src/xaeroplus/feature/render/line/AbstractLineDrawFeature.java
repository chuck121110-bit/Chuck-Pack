package xaeroplus.feature.render.line;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import net.minecraft.class_1937;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_5321;
import xaeroplus.Globals;
import xaeroplus.feature.render.DrawContext;
import xaeroplus.feature.render.DrawFeature;
import xaeroplus.feature.render.MapRenderWindow;
import xaeroplus.feature.render.shaders.XaeroPlusShaders;
import xaeroplus.module.impl.TickTaskExecutor;

public abstract class AbstractLineDrawFeature<T> implements DrawFeature {
   public final AsyncLoadingCache<Long, T> lineRenderCache;

   protected AbstractLineDrawFeature(int refreshIntervalMs) {
      this.lineRenderCache = Caffeine.newBuilder().expireAfterWrite(10L, TimeUnit.SECONDS).refreshAfterWrite((long)refreshIntervalMs, TimeUnit.MILLISECONDS).executor(TickTaskExecutor.INSTANCE).removalListener((k, v, cause) -> this.markDrawBufferStale()).buildAsync((CacheLoader)((k) -> this.loadLinesInWindow()));
   }

   public void invalidateCache() {
      this.lineRenderCache.synchronous().invalidateAll();
      this.markDrawBufferStale();
   }

   public abstract float lineWidth();

   public T loadLinesInWindow() {
      MapRenderWindow window = MapRenderWindow.resolveCurrent();
      return (T)this.preProcessLines(this.provideLinesInWindow(window.windowX(), window.windowZ(), window.windowSize(), window.dimension()), window.windowX(), window.windowZ(), window.windowSize());
   }

   public abstract T provideLinesInWindow(int windowX, int windowZ, int windowSize, class_5321<class_1937> dimension);

   public abstract T preProcessLines(T lines, final int windowX, final int windowZ, final int windowSize);

   public abstract T emptyLines();

   protected abstract void markDrawBufferStale();

   protected abstract void closeDrawBuffer();

   public T getLines() {
      return (T)this.lineRenderCache.get(0L).getNow(this.emptyLines());
   }

   protected float lineWidthScale(final DrawContext ctx) {
      return 16.0F * (float)class_3532.method_15350((double)this.lineWidth() * ctx.fboScale(), (double)(0.1F * (ctx.worldmap() ? 1.0F : (float)Globals.minimapScaleMultiplier)), (double)1000.0F);
   }

   public void preRender(final DrawContext ctx) {
      if (ctx.worldmap()) {
         XaeroPlusShaders.setLinesFrameSize((float)class_310.method_1551().method_22683().method_4489(), (float)class_310.method_1551().method_22683().method_4506());
      }

   }

   public void postRender(final DrawContext ctx) {
   }

   public void close() {
      this.lineRenderCache.synchronous().invalidateAll();
      this.closeDrawBuffer();
   }
}
