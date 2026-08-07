package xaeroplus.feature.render.ellipse;

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

public abstract class AbstractEllipseDrawFeature<T> implements DrawFeature {
   public final AsyncLoadingCache<Long, T> ellipseRenderCache;

   protected AbstractEllipseDrawFeature(final int refreshIntervalMs) {
      this.ellipseRenderCache = Caffeine.newBuilder().expireAfterWrite(10L, TimeUnit.SECONDS).refreshAfterWrite((long)refreshIntervalMs, TimeUnit.MILLISECONDS).executor(TickTaskExecutor.INSTANCE).removalListener((key, value, cause) -> this.markDrawBufferStale()).buildAsync((CacheLoader)((key) -> this.loadEllipsesInWindow()));
   }

   public void invalidateCache() {
      this.ellipseRenderCache.synchronous().invalidateAll();
      this.markDrawBufferStale();
   }

   public abstract float thickness();

   public T loadEllipsesInWindow() {
      MapRenderWindow window = MapRenderWindow.resolveCurrent();
      return (T)this.preProcessEllipses(this.provideEllipsesInWindow(window.windowX(), window.windowZ(), window.windowSize(), window.dimension()), window.windowX(), window.windowZ(), window.windowSize());
   }

   public abstract T provideEllipsesInWindow(int windowX, int windowZ, int windowSize, class_5321<class_1937> dimension);

   public abstract T preProcessEllipses(T ellipses, int windowX, int windowZ, int windowSize);

   public abstract T emptyEllipses();

   protected abstract void markDrawBufferStale();

   protected abstract void closeDrawBuffer();

   public T getEllipses() {
      return (T)this.ellipseRenderCache.get(0L).getNow(this.emptyEllipses());
   }

   protected float thicknessScale(final DrawContext ctx) {
      return 16.0F * (float)class_3532.method_15350((double)this.thickness() * ctx.fboScale(), (double)(0.1F * (ctx.worldmap() ? 1.0F : (float)Globals.minimapScaleMultiplier)), (double)1000.0F);
   }

   public void preRender(final DrawContext ctx) {
      if (ctx.worldmap()) {
         XaeroPlusShaders.setEllipsesFrameSize((float)class_310.method_1551().method_22683().method_4489(), (float)class_310.method_1551().method_22683().method_4506());
      }

   }

   public void postRender(final DrawContext ctx) {
   }

   public void close() {
      this.ellipseRenderCache.synchronous().invalidateAll();
      this.closeDrawBuffer();
   }
}
