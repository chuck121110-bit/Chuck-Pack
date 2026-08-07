package xaeroplus.feature.drawing;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import net.minecraft.class_1937;
import net.minecraft.class_310;
import net.minecraft.class_5321;
import xaero.map.MapProcessor;
import xaero.map.core.XaeroWorldMapCore;
import xaero.map.gui.GuiMap;
import xaeroplus.Globals;
import xaeroplus.XaeroPlus;
import xaeroplus.event.XaeroWorldChangeEvent;
import xaeroplus.feature.drawing.db.DrawingDatabase;
import xaeroplus.feature.render.ellipse.Ellipse;
import xaeroplus.feature.render.line.Line;
import xaeroplus.feature.render.text.Text;
import xaeroplus.module.impl.TickTaskExecutor;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.GuiMapHelper;
import xaeroplus.util.Wait;
import xaeroplus.util.timer.Timer;
import xaeroplus.util.timer.Timers;

public class DrawingCache implements Closeable {
   private DrawingDatabase database;
   private String currentWorldId;
   private final AtomicBoolean cacheReady = new AtomicBoolean(false);
   private final String databaseName;
   private ListeningExecutorService dbExecutor;
   private final ListeningExecutorService parentExecutor;
   private final Map<class_5321<class_1937>, DrawingHighlightCacheDimensionHandler> highlightsCacheMap = new ConcurrentHashMap(3);
   private final Map<class_5321<class_1937>, DrawingLinesCacheDimensionHandler> linesCacheMap = new ConcurrentHashMap(3);
   private final Map<class_5321<class_1937>, DrawingEllipseCacheDimensionHandler> ellipsesCacheMap = new ConcurrentHashMap(3);
   private final Map<class_5321<class_1937>, DrawingTextCacheDimensionHandler> textsCacheMap = new ConcurrentHashMap(3);
   private final Queue<Runnable> initializeTaskQueue = new ConcurrentLinkedQueue();
   class_310 mc = class_310.method_1551();
   final Timer tickTimer = Timers.tickTimer();
   final Timer flushTimer = Timers.tickTimer();

   public DrawingCache(final String databaseName) {
      this.databaseName = databaseName;
      this.parentExecutor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor((new ThreadFactoryBuilder()).setNameFormat(databaseName + "-Manager").setUncaughtExceptionHandler((t, e) -> XaeroPlus.LOGGER.error("Uncaught exception in {}", t.getName(), e)).build()));
   }

   public void addHighlight(final int x, final int z, final int color, final class_5321<class_1937> dimension) {
      try {
         DrawingHighlightCacheDimensionHandler cacheForActualDimension = this.getHighlightCacheForDimension(dimension, true);
         if (cacheForActualDimension == null) {
            this.initializeTaskQueue.add((Runnable)() -> this.addHighlight(x, z, color, dimension));
            return;
         }

         cacheForActualDimension.addHighlight(x, z, (long)color);
      } catch (Exception e) {
         XaeroPlus.LOGGER.warn("Error adding highlight to {} disk cache: {}, {}", new Object[]{this.databaseName, x, z, e});
      }

   }

   public void addLine(Line line, int color, class_5321<class_1937> dimension) {
      try {
         DrawingLinesCacheDimensionHandler cacheForActualDimension = this.getLinesCacheForDimension(dimension, true);
         if (cacheForActualDimension == null) {
            this.initializeTaskQueue.add((Runnable)() -> this.addLine(line, color, dimension));
            return;
         }

         cacheForActualDimension.addLine(line, color);
      } catch (Exception e) {
         XaeroPlus.LOGGER.warn("Error adding line to {} disk cache: {}, {}", new Object[]{this.databaseName, line, e});
      }

   }

   public void addEllipse(final Ellipse ellipse, final int color, final class_5321<class_1937> dimension) {
      try {
         DrawingEllipseCacheDimensionHandler cache = this.getEllipseCacheForDimension(dimension, true);
         if (cache == null) {
            this.initializeTaskQueue.add((Runnable)() -> this.addEllipse(ellipse, color, dimension));
            return;
         }

         cache.addEllipse(ellipse, color);
      } catch (Exception e) {
         XaeroPlus.LOGGER.warn("Error adding ellipse to {} disk cache: {}, {}", new Object[]{this.databaseName, ellipse, e});
      }

   }

   public void addText(Text text, class_5321<class_1937> dimension) {
      if (!text.value().isBlank()) {
         try {
            DrawingTextCacheDimensionHandler cacheForActualDimension = this.getTextCacheForDimension(dimension, true);
            if (cacheForActualDimension == null) {
               this.initializeTaskQueue.add((Runnable)() -> this.addText(text, dimension));
               return;
            }

            cacheForActualDimension.addText(text);
         } catch (Exception e) {
            XaeroPlus.LOGGER.warn("Error adding text to {} disk cache: {}, {}", new Object[]{this.databaseName, text, e});
         }

      }
   }

   public void removeHighlight(final int x, final int z, final class_5321<class_1937> dimension) {
      try {
         DrawingHighlightCacheDimensionHandler cacheForActualDimension = this.getHighlightCacheForDimension(dimension, true);
         if (cacheForActualDimension == null) {
            this.initializeTaskQueue.add((Runnable)() -> this.removeHighlight(x, z, dimension));
            return;
         }

         cacheForActualDimension.removeHighlight(x, z);
      } catch (Exception e) {
         XaeroPlus.LOGGER.warn("Error removing highlight from {} disk cache: {}, {}", new Object[]{this.databaseName, x, z, e});
      }

   }

   public void removeHighlights(final LongCollection toRemove, final class_5321<class_1937> dimension) {
      try {
         DrawingHighlightCacheDimensionHandler cacheForActualDimension = this.getHighlightCacheForDimension(dimension, true);
         if (cacheForActualDimension == null) {
            this.initializeTaskQueue.add((Runnable)() -> this.removeHighlights(toRemove, dimension));
            return;
         }

         cacheForActualDimension.removeHighlights(toRemove);
      } catch (Exception e) {
         XaeroPlus.LOGGER.warn("Error removing {} highlights from {} disk cache: {}", new Object[]{toRemove.size(), this.databaseName, toRemove, e});
      }

   }

   public void removeLine(Line line, class_5321<class_1937> dimension) {
      try {
         DrawingLinesCacheDimensionHandler cacheForActualDimension = this.getLinesCacheForDimension(dimension, true);
         if (cacheForActualDimension == null) {
            this.initializeTaskQueue.add((Runnable)() -> this.removeLine(line, dimension));
            return;
         }

         cacheForActualDimension.removeLine(line);
      } catch (Exception e) {
         XaeroPlus.LOGGER.warn("Error removing line from {} disk cache: {}, {}", new Object[]{this.databaseName, line, e});
      }

   }

   public void removeEllipse(final Ellipse ellipse, final class_5321<class_1937> dimension) {
      try {
         DrawingEllipseCacheDimensionHandler cache = this.getEllipseCacheForDimension(dimension, true);
         if (cache == null) {
            this.initializeTaskQueue.add((Runnable)() -> this.removeEllipse(ellipse, dimension));
            return;
         }

         cache.removeEllipse(ellipse);
      } catch (Exception e) {
         XaeroPlus.LOGGER.warn("Error removing ellipse from {} disk cache: {}, {}", new Object[]{this.databaseName, ellipse, e});
      }

   }

   public void removeText(final int x, final int z, class_5321<class_1937> dimension) {
      try {
         DrawingTextCacheDimensionHandler cacheForActualDimension = this.getTextCacheForDimension(dimension, true);
         if (cacheForActualDimension == null) {
            this.initializeTaskQueue.add((Runnable)() -> this.removeText(x, z, dimension));
            return;
         }

         cacheForActualDimension.removeText(x, z);
      } catch (Exception e) {
         XaeroPlus.LOGGER.warn("Error removing text from {} disk cache: {}, {}", new Object[]{this.databaseName, x, z, e});
      }

   }

   public Long2LongMap getHighlights(final class_5321<class_1937> dimensionId) {
      if (dimensionId == null) {
         return Long2LongMaps.EMPTY_MAP;
      } else {
         DrawingHighlightCacheDimensionHandler cacheForDimension = this.getHighlightCacheForDimension(dimensionId, false);
         return (Long2LongMap)(cacheForDimension == null ? Long2LongMaps.EMPTY_MAP : cacheForDimension.getCacheMap(dimensionId));
      }
   }

   public Object2IntMap<Line> getLines(final class_5321<class_1937> dimension) {
      if (dimension == null) {
         return Object2IntMaps.emptyMap();
      } else {
         DrawingLinesCacheDimensionHandler cacheForDimension = this.getLinesCacheForDimension(dimension, false);
         return cacheForDimension == null ? Object2IntMaps.emptyMap() : cacheForDimension.getLines();
      }
   }

   public Object2IntMap<Ellipse> getEllipses(final class_5321<class_1937> dimension) {
      if (dimension == null) {
         return Object2IntMaps.emptyMap();
      } else {
         DrawingEllipseCacheDimensionHandler cacheForDimension = this.getEllipseCacheForDimension(dimension, false);
         return cacheForDimension == null ? Object2IntMaps.emptyMap() : cacheForDimension.getEllipses();
      }
   }

   public Long2ObjectMap<Text> getTexts(final class_5321<class_1937> dimensionId) {
      if (dimensionId == null) {
         return Long2ObjectMaps.emptyMap();
      } else {
         DrawingTextCacheDimensionHandler cacheForDimension = this.getTextCacheForDimension(dimensionId, false);
         return cacheForDimension == null ? Long2ObjectMaps.emptyMap() : cacheForDimension.getTexts();
      }
   }

   public void handleWorldChange(final XaeroWorldChangeEvent event) {
      this.parentExecutor.execute(() -> {
         switch (event.worldChangeType()) {
            case ENTER_WORLD:
               if (!this.cacheReady.get()) {
                  if (this.initializeWorld()) {
                     this.cacheReady.set(true);
                     this.submitTickTask(() -> {
                        this.loadHighlightsInViewedDimension();
                        this.loadLinesInViewedDimension();
                        this.loadEllipsesInViewedDimension();
                        this.loadTextsInViewedDimension();
                     });
                  }
               } else {
                  XaeroPlus.LOGGER.warn("[{}] Entered world when cache was already initialized", this.databaseName);
               }
               break;
            case EXIT_WORLD:
               if (this.cacheReady.compareAndSet(true, false)) {
                  try {
                     List<CompletableFuture<?>> tasks = new ArrayList();
                     tasks.addAll(this.flushAllChunks());
                     tasks.addAll(this.flushAllLines());
                     tasks.addAll(this.flushAllEllipses());
                     tasks.addAll(this.flushAllTexts());
                     CompletableFuture<Void> future = CompletableFuture.allOf((CompletableFuture[])tasks.toArray((x$0) -> new CompletableFuture[x$0]));
                     Wait.waitUntil(() -> !this.mc.method_22108() || future.isDone(), 30);
                  } catch (Exception e) {
                     XaeroPlus.LOGGER.error("Error saving all chunks before world change", e);
                  }
               } else {
                  XaeroPlus.LOGGER.warn("[{}] Exited world when cache was already uninitialized", this.databaseName);
               }

               this.reset();
               break;
            case VIEWED_DIMENSION_SWITCH:
               this.submitTickTask(this::loadHighlightsInViewedDimension);
               this.submitTickTask(this::loadLinesInViewedDimension);
               this.submitTickTask(this::loadEllipsesInViewedDimension);
               this.submitTickTask(this::loadTextsInViewedDimension);
               break;
            case ACTUAL_DIMENSION_SWITCH:
               this.submitTickTask(this::loadChunksOnActualDimensionSwitch);
               this.submitTickTask(this::loadLinesOnActualDimensionSwitch);
               this.submitTickTask(this::loadEllipsesOnActualDimensionSwitch);
               this.submitTickTask(this::loadTextsOnActualDimensionSwitch);
         }

      });
   }

   private CompletableFuture<?> submitTickTask(final Runnable runnable) {
      return TickTaskExecutor.INSTANCE.submit(runnable);
   }

   private synchronized void reset() {
      this.currentWorldId = null;
      if (this.dbExecutor != null) {
         try {
            this.dbExecutor.shutdown();
            if (!this.dbExecutor.awaitTermination(6L, TimeUnit.SECONDS)) {
               throw new RuntimeException("Timed out awaiting shutdown termination");
            }
         } catch (Throwable e) {
            XaeroPlus.LOGGER.error("Timed out waiting for {} executor to shutdown", this.databaseName, e);

            try {
               List<Runnable> droppedTasks = this.dbExecutor.shutdownNow();
               if (!this.dbExecutor.awaitTermination(4L, TimeUnit.SECONDS)) {
                  throw new RuntimeException("Timed out awaiting force shutdown termination");
               }

               XaeroPlus.LOGGER.error("Forcibly shut down {} executor with {} tasks remaining", this.databaseName, droppedTasks.size());
            } catch (Throwable e2) {
               XaeroPlus.LOGGER.error("Error force shutting down {} executor", this.databaseName, e2);
            }
         }
      }

      if (this.database != null) {
         this.database.close();
      }

      this.highlightsCacheMap.clear();
      this.linesCacheMap.clear();
      this.ellipsesCacheMap.clear();
      this.textsCacheMap.clear();
      this.database = null;
      this.initializeTaskQueue.clear();
   }

   private List<CompletableFuture<?>> flushAllChunks() {
      return (List)this.getAllHighlightCaches().stream().map((cache) -> {
         Objects.requireNonNull(cache);
         return this.submitTickTask(cache::writeStaleHighlightsToDatabase);
      }).collect(Collectors.toList());
   }

   private List<CompletableFuture<?>> flushAllLines() {
      return (List)this.getAllLinesCaches().stream().map((cache) -> {
         Objects.requireNonNull(cache);
         return this.submitTickTask(cache::writeStaleLinesToDatabase);
      }).collect(Collectors.toList());
   }

   private List<CompletableFuture<?>> flushAllEllipses() {
      return (List)this.getAllEllipseCaches().stream().map((cache) -> {
         Objects.requireNonNull(cache);
         return this.submitTickTask(cache::writeStaleEllipsesToDatabase);
      }).collect(Collectors.toList());
   }

   private List<CompletableFuture<?>> flushAllTexts() {
      return (List)this.getAllTextsCaches().stream().map((cache) -> {
         Objects.requireNonNull(cache);
         return this.submitTickTask(cache::writeStaleTextsToDatabase);
      }).collect(Collectors.toList());
   }

   public DrawingHighlightCacheDimensionHandler getHighlightCacheForActualDimension() {
      return !this.cacheReady.get() ? null : this.getHighlightCacheForDimension(ChunkUtils.getActualDimension(), true);
   }

   public DrawingTextCacheDimensionHandler getTextCacheForActualDimension() {
      return !this.cacheReady.get() ? null : this.getTextCacheForDimension(ChunkUtils.getActualDimension(), true);
   }

   private DrawingHighlightCacheDimensionHandler initializeHighlightDimensionCacheHandler(final class_5321<class_1937> dimension) {
      if (dimension == null) {
         return null;
      } else {
         DrawingDatabase db = this.database;
         ListeningExecutorService executor = this.dbExecutor;
         if (db != null && executor != null) {
            DrawingHighlightCacheDimensionHandler cacheHandler = new DrawingHighlightCacheDimensionHandler(dimension, db, executor);
            db.initializeDimension(dimension);
            this.highlightsCacheMap.put(dimension, cacheHandler);
            return cacheHandler;
         } else {
            XaeroPlus.LOGGER.error("[{}] Unable to initialize {} disk cache handler for: {}, database: {} or executor: {} is null", new Object[]{Thread.currentThread().getName(), this.databaseName, dimension.method_29177(), db, executor});
            return null;
         }
      }
   }

   private DrawingLinesCacheDimensionHandler initializeLinesCacheHandler(final class_5321<class_1937> dimension) {
      if (dimension == null) {
         return null;
      } else {
         DrawingDatabase db = this.database;
         ListeningExecutorService executor = this.dbExecutor;
         if (db != null && executor != null) {
            DrawingLinesCacheDimensionHandler linesCacheHandler = new DrawingLinesCacheDimensionHandler(dimension, db, executor);
            db.initializeDimension(dimension);
            this.linesCacheMap.put(dimension, linesCacheHandler);
            return linesCacheHandler;
         } else {
            XaeroPlus.LOGGER.error("[{}] Unable to initialize {} disk lines cache handler for: {}, database: {} or executor: {} is null", new Object[]{Thread.currentThread().getName(), this.databaseName, dimension.method_29177(), db, executor});
            return null;
         }
      }
   }

   private DrawingEllipseCacheDimensionHandler initializeEllipseCacheHandler(final class_5321<class_1937> dimension) {
      if (dimension == null) {
         return null;
      } else {
         DrawingDatabase db = this.database;
         ListeningExecutorService executor = this.dbExecutor;
         if (db != null && executor != null) {
            DrawingEllipseCacheDimensionHandler ellipsesCacheHandler = new DrawingEllipseCacheDimensionHandler(dimension, db, executor);
            db.initializeDimension(dimension);
            this.ellipsesCacheMap.put(dimension, ellipsesCacheHandler);
            return ellipsesCacheHandler;
         } else {
            XaeroPlus.LOGGER.error("[{}] Unable to initialize {} ellipse disk cache handler for: {}", new Object[]{Thread.currentThread().getName(), this.databaseName, dimension.method_29177()});
            return null;
         }
      }
   }

   private DrawingTextCacheDimensionHandler initializeTextDimensionCacheHandler(final class_5321<class_1937> dimension) {
      if (dimension == null) {
         return null;
      } else {
         DrawingDatabase db = this.database;
         ListeningExecutorService executor = this.dbExecutor;
         if (db != null && executor != null) {
            DrawingTextCacheDimensionHandler cacheHandler = new DrawingTextCacheDimensionHandler(dimension, db, executor);
            db.initializeDimension(dimension);
            this.textsCacheMap.put(dimension, cacheHandler);
            return cacheHandler;
         } else {
            XaeroPlus.LOGGER.error("[{}] Unable to initialize {} disk cache handler for: {}, database: {} or executor: {} is null", new Object[]{Thread.currentThread().getName(), this.databaseName, dimension.method_29177(), db, executor});
            return null;
         }
      }
   }

   public DrawingHighlightCacheDimensionHandler getHighlightCacheForDimension(final class_5321<class_1937> dimension, boolean create) {
      if (!this.cacheReady.get()) {
         return null;
      } else if (dimension == null) {
         return null;
      } else {
         DrawingHighlightCacheDimensionHandler dimensionCache = (DrawingHighlightCacheDimensionHandler)this.highlightsCacheMap.get(dimension);
         if (dimensionCache == null) {
            if (!create) {
               return null;
            }

            XaeroPlus.LOGGER.info("Initializing {} disk cache for dimension: {}", this.databaseName, dimension.method_29177());
            dimensionCache = this.initializeHighlightDimensionCacheHandler(dimension);
         }

         return dimensionCache;
      }
   }

   public DrawingTextCacheDimensionHandler getTextCacheForDimension(final class_5321<class_1937> dimension, boolean create) {
      if (!this.cacheReady.get()) {
         return null;
      } else if (dimension == null) {
         return null;
      } else {
         DrawingTextCacheDimensionHandler dimensionCache = (DrawingTextCacheDimensionHandler)this.textsCacheMap.get(dimension);
         if (dimensionCache == null) {
            if (!create) {
               return null;
            }

            XaeroPlus.LOGGER.info("Initializing {} disk cache for dimension: {}", this.databaseName, dimension.method_29177());
            dimensionCache = this.initializeTextDimensionCacheHandler(dimension);
         }

         return dimensionCache;
      }
   }

   public DrawingLinesCacheDimensionHandler getLinesCacheForDimension(final class_5321<class_1937> dimension, boolean create) {
      if (!this.cacheReady.get()) {
         return null;
      } else if (dimension == null) {
         return null;
      } else {
         DrawingLinesCacheDimensionHandler linesCache = (DrawingLinesCacheDimensionHandler)this.linesCacheMap.get(dimension);
         if (linesCache == null) {
            if (!create) {
               return null;
            }

            XaeroPlus.LOGGER.info("Initializing {} disk lines cache for dimension: {}", this.databaseName, dimension.method_29177());
            linesCache = this.initializeLinesCacheHandler(dimension);
         }

         return linesCache;
      }
   }

   public DrawingEllipseCacheDimensionHandler getEllipseCacheForDimension(final class_5321<class_1937> dimension, final boolean create) {
      if (this.cacheReady.get() && dimension != null) {
         DrawingEllipseCacheDimensionHandler cache = (DrawingEllipseCacheDimensionHandler)this.ellipsesCacheMap.get(dimension);
         if (cache == null && create) {
            XaeroPlus.LOGGER.info("Initializing {} disk ellipse cache for dimension: {}", this.databaseName, dimension.method_29177());
            cache = this.initializeEllipseCacheHandler(dimension);
         }

         return cache;
      } else {
         return null;
      }
   }

   public List<DrawingHighlightCacheDimensionHandler> getAllHighlightCaches() {
      return List.copyOf(this.highlightsCacheMap.values());
   }

   public List<DrawingLinesCacheDimensionHandler> getAllLinesCaches() {
      return List.copyOf(this.linesCacheMap.values());
   }

   public List<DrawingEllipseCacheDimensionHandler> getAllEllipseCaches() {
      return List.copyOf(this.ellipsesCacheMap.values());
   }

   public List<DrawingTextCacheDimensionHandler> getAllTextsCaches() {
      return List.copyOf(this.textsCacheMap.values());
   }

   public List<DrawingHighlightCacheDimensionHandler> getHighlightCachesExceptDimension(final class_5321<class_1937> dimension) {
      ArrayList<DrawingHighlightCacheDimensionHandler> caches = new ArrayList(this.highlightsCacheMap.size());

      for(Map.Entry<class_5321<class_1937>, DrawingHighlightCacheDimensionHandler> entry : this.highlightsCacheMap.entrySet()) {
         if (!((class_5321)entry.getKey()).equals(dimension)) {
            caches.add((DrawingHighlightCacheDimensionHandler)entry.getValue());
         }
      }

      return caches;
   }

   public List<DrawingHighlightCacheDimensionHandler> getHighlightCachesExceptDimensions(final List<class_5321<class_1937>> dimensions) {
      ArrayList<DrawingHighlightCacheDimensionHandler> caches = new ArrayList(this.highlightsCacheMap.size());

      for(Map.Entry<class_5321<class_1937>, DrawingHighlightCacheDimensionHandler> entry : this.highlightsCacheMap.entrySet()) {
         if (!dimensions.contains(entry.getKey())) {
            caches.add((DrawingHighlightCacheDimensionHandler)entry.getValue());
         }
      }

      return caches;
   }

   public List<DrawingTextCacheDimensionHandler> getTextCachesExceptDimension(final class_5321<class_1937> dimension) {
      ArrayList<DrawingTextCacheDimensionHandler> caches = new ArrayList(this.textsCacheMap.size());

      for(Map.Entry<class_5321<class_1937>, DrawingTextCacheDimensionHandler> entry : this.textsCacheMap.entrySet()) {
         if (!((class_5321)entry.getKey()).equals(dimension)) {
            caches.add((DrawingTextCacheDimensionHandler)entry.getValue());
         }
      }

      return caches;
   }

   public List<DrawingTextCacheDimensionHandler> getTextCachesExceptDimensions(final List<class_5321<class_1937>> dimensions) {
      ArrayList<DrawingTextCacheDimensionHandler> caches = new ArrayList(this.textsCacheMap.size());

      for(Map.Entry<class_5321<class_1937>, DrawingTextCacheDimensionHandler> entry : this.textsCacheMap.entrySet()) {
         if (!dimensions.contains(entry.getKey())) {
            caches.add((DrawingTextCacheDimensionHandler)entry.getValue());
         }
      }

      return caches;
   }

   public List<DrawingLinesCacheDimensionHandler> getLineCachesExceptDimension(final class_5321<class_1937> dimension) {
      ArrayList<DrawingLinesCacheDimensionHandler> caches = new ArrayList(this.linesCacheMap.size());

      for(Map.Entry<class_5321<class_1937>, DrawingLinesCacheDimensionHandler> entry : this.linesCacheMap.entrySet()) {
         if (!((class_5321)entry.getKey()).equals(dimension)) {
            caches.add((DrawingLinesCacheDimensionHandler)entry.getValue());
         }
      }

      return caches;
   }

   public List<DrawingLinesCacheDimensionHandler> getLineCachesExceptDimensions(final List<class_5321<class_1937>> dimensions) {
      ArrayList<DrawingLinesCacheDimensionHandler> caches = new ArrayList(this.linesCacheMap.size());

      for(Map.Entry<class_5321<class_1937>, DrawingLinesCacheDimensionHandler> entry : this.linesCacheMap.entrySet()) {
         if (!dimensions.contains(entry.getKey())) {
            caches.add((DrawingLinesCacheDimensionHandler)entry.getValue());
         }
      }

      return caches;
   }

   public List<DrawingEllipseCacheDimensionHandler> getEllipseCachesExceptDimension(final class_5321<class_1937> dimension) {
      ArrayList<DrawingEllipseCacheDimensionHandler> caches = new ArrayList(this.ellipsesCacheMap.size());

      for(Map.Entry<class_5321<class_1937>, DrawingEllipseCacheDimensionHandler> entry : this.ellipsesCacheMap.entrySet()) {
         if (!((class_5321)entry.getKey()).equals(dimension)) {
            caches.add((DrawingEllipseCacheDimensionHandler)entry.getValue());
         }
      }

      return caches;
   }

   public List<DrawingEllipseCacheDimensionHandler> getEllipseCachesExceptDimensions(final List<class_5321<class_1937>> dimensions) {
      ArrayList<DrawingEllipseCacheDimensionHandler> caches = new ArrayList(this.ellipsesCacheMap.size());

      for(Map.Entry<class_5321<class_1937>, DrawingEllipseCacheDimensionHandler> entry : this.ellipsesCacheMap.entrySet()) {
         if (!dimensions.contains(entry.getKey())) {
            caches.add((DrawingEllipseCacheDimensionHandler)entry.getValue());
         }
      }

      return caches;
   }

   private synchronized boolean initializeWorld() {
      try {
         MapProcessor mapProcessor = XaeroWorldMapCore.currentSession.getMapProcessor();
         if (mapProcessor == null) {
            return false;
         } else {
            String worldId = mapProcessor.getCurrentWorldId();
            if (worldId == null) {
               return false;
            } else {
               this.currentWorldId = worldId;
               this.dbExecutor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor((new ThreadFactoryBuilder()).setNameFormat(this.databaseName + "-Worker").setUncaughtExceptionHandler((t, e) -> XaeroPlus.LOGGER.error("Uncaught exception handler in {}", t.getName(), e)).build()));
               this.database = new DrawingDatabase(worldId, this.databaseName);
               this.initializeHighlightDimensionCacheHandler(class_1937.field_25179);
               this.initializeHighlightDimensionCacheHandler(class_1937.field_25180);
               this.initializeHighlightDimensionCacheHandler(class_1937.field_25181);
               this.initializeLinesCacheHandler(class_1937.field_25179);
               this.initializeLinesCacheHandler(class_1937.field_25180);
               this.initializeLinesCacheHandler(class_1937.field_25181);
               this.initializeEllipseCacheHandler(class_1937.field_25179);
               this.initializeEllipseCacheHandler(class_1937.field_25180);
               this.initializeEllipseCacheHandler(class_1937.field_25181);
               this.initializeTextDimensionCacheHandler(class_1937.field_25179);
               this.initializeTextDimensionCacheHandler(class_1937.field_25180);
               this.initializeTextDimensionCacheHandler(class_1937.field_25181);
               if (!this.initializeTaskQueue.isEmpty()) {
                  XaeroPlus.LOGGER.info("[{}] Running {} queued tasks", this.databaseName, this.initializeTaskQueue.size());
               }

               while(!this.initializeTaskQueue.isEmpty()) {
                  this.submitTickTask((Runnable)this.initializeTaskQueue.poll());
               }

               return true;
            }
         }
      } catch (Exception var3) {
         this.reset();
         return false;
      }
   }

   private void loadChunksOnActualDimensionSwitch() {
      DrawingHighlightCacheDimensionHandler cacheForActualDimension = this.getHighlightCacheForActualDimension();
      if (cacheForActualDimension != null) {
         cacheForActualDimension.setWindow(ChunkUtils.actualPlayerRegionX(), ChunkUtils.actualPlayerRegionZ(), this.getMinimapRegionWindowSize());
      }
   }

   private void loadLinesOnActualDimensionSwitch() {
      DrawingLinesCacheDimensionHandler linesCacheForActualDimension = this.getLinesCacheForDimension(ChunkUtils.getActualDimension(), true);
      if (linesCacheForActualDimension != null) {
         linesCacheForActualDimension.setWindow(ChunkUtils.actualPlayerRegionX(), ChunkUtils.actualPlayerRegionZ(), this.getMinimapRegionWindowSize());
      }
   }

   private void loadEllipsesOnActualDimensionSwitch() {
      DrawingEllipseCacheDimensionHandler cache = this.getEllipseCacheForDimension(ChunkUtils.getActualDimension(), true);
      if (cache != null) {
         cache.setWindow(ChunkUtils.actualPlayerRegionX(), ChunkUtils.actualPlayerRegionZ(), this.getMinimapRegionWindowSize());
      }

   }

   private void loadTextsOnActualDimensionSwitch() {
      DrawingTextCacheDimensionHandler cacheForActualDimension = this.getTextCacheForActualDimension();
      if (cacheForActualDimension != null) {
         cacheForActualDimension.setWindow(ChunkUtils.actualPlayerRegionX(), ChunkUtils.actualPlayerRegionZ(), this.getMinimapRegionWindowSize());
      }
   }

   private void loadHighlightsInViewedDimension() {
      class_5321<class_1937> viewedDim = Globals.getCurrentDimensionId();
      DrawingHighlightCacheDimensionHandler cacheForCurrentDimension = this.getHighlightCacheForDimension(viewedDim, true);
      if (cacheForCurrentDimension != null) {
         Optional<GuiMap> guiMapOptional = GuiMapHelper.getGuiMap();
         int windowSize;
         int windowCenterX;
         int windowCenterZ;
         if (guiMapOptional.isPresent()) {
            GuiMap guiMap = (GuiMap)guiMapOptional.get();
            windowSize = GuiMapHelper.getGuiMapRegionSize(guiMap);
            windowCenterX = GuiMapHelper.getGuiMapCenterRegionX(guiMap);
            windowCenterZ = GuiMapHelper.getGuiMapCenterRegionZ(guiMap);
         } else {
            windowSize = this.getMinimapRegionWindowSize();
            windowCenterX = ChunkUtils.getPlayerRegionX();
            windowCenterZ = ChunkUtils.getPlayerRegionZ();
         }

         cacheForCurrentDimension.setWindow(windowCenterX, windowCenterZ, windowSize);
      }
   }

   private void loadLinesInViewedDimension() {
      class_5321<class_1937> viewedDim = Globals.getCurrentDimensionId();
      DrawingLinesCacheDimensionHandler linesCacheForCurrentDimension = this.getLinesCacheForDimension(viewedDim, true);
      if (linesCacheForCurrentDimension != null) {
         Optional<GuiMap> guiMapOptional = GuiMapHelper.getGuiMap();
         int windowSize;
         int windowCenterX;
         int windowCenterZ;
         if (guiMapOptional.isPresent()) {
            GuiMap guiMap = (GuiMap)guiMapOptional.get();
            windowSize = GuiMapHelper.getGuiMapRegionSize(guiMap);
            windowCenterX = GuiMapHelper.getGuiMapCenterRegionX(guiMap);
            windowCenterZ = GuiMapHelper.getGuiMapCenterRegionZ(guiMap);
         } else {
            windowSize = this.getMinimapRegionWindowSize();
            windowCenterX = ChunkUtils.getPlayerRegionX();
            windowCenterZ = ChunkUtils.getPlayerRegionZ();
         }

         linesCacheForCurrentDimension.setWindow(windowCenterX, windowCenterZ, windowSize);
      }
   }

   private void loadEllipsesInViewedDimension() {
      class_5321<class_1937> viewedDimension = Globals.getCurrentDimensionId();
      DrawingEllipseCacheDimensionHandler cache = this.getEllipseCacheForDimension(viewedDimension, true);
      if (cache != null) {
         Optional<GuiMap> guiMap = GuiMapHelper.getGuiMap();
         int windowSize;
         int windowCenterX;
         int windowCenterZ;
         if (guiMap.isPresent()) {
            windowSize = GuiMapHelper.getGuiMapRegionSize((GuiMap)guiMap.get());
            windowCenterX = GuiMapHelper.getGuiMapCenterRegionX((GuiMap)guiMap.get());
            windowCenterZ = GuiMapHelper.getGuiMapCenterRegionZ((GuiMap)guiMap.get());
         } else {
            windowSize = this.getMinimapRegionWindowSize();
            windowCenterX = ChunkUtils.getPlayerRegionX();
            windowCenterZ = ChunkUtils.getPlayerRegionZ();
         }

         cache.setWindow(windowCenterX, windowCenterZ, windowSize);
      }
   }

   private void loadTextsInViewedDimension() {
      class_5321<class_1937> viewedDim = Globals.getCurrentDimensionId();
      DrawingTextCacheDimensionHandler cacheForCurrentDimension = this.getTextCacheForDimension(viewedDim, true);
      if (cacheForCurrentDimension != null) {
         Optional<GuiMap> guiMapOptional = GuiMapHelper.getGuiMap();
         int windowSize;
         int windowCenterX;
         int windowCenterZ;
         if (guiMapOptional.isPresent()) {
            GuiMap guiMap = (GuiMap)guiMapOptional.get();
            windowSize = GuiMapHelper.getGuiMapRegionSize(guiMap);
            windowCenterX = GuiMapHelper.getGuiMapCenterRegionX(guiMap);
            windowCenterZ = GuiMapHelper.getGuiMapCenterRegionZ(guiMap);
         } else {
            windowSize = this.getMinimapRegionWindowSize();
            windowCenterX = ChunkUtils.getPlayerRegionX();
            windowCenterZ = ChunkUtils.getPlayerRegionZ();
         }

         cacheForCurrentDimension.setWindow(windowCenterX, windowCenterZ, windowSize);
      }
   }

   public void onEnable() {
      this.handleWorldChange(new XaeroWorldChangeEvent(XaeroWorldChangeEvent.WorldChangeType.ENTER_WORLD, (class_5321)null, ChunkUtils.getActualDimension()));
   }

   public void onDisable() {
      this.parentExecutor.execute(() -> {
         this.cacheReady.set(false);

         try {
            List<CompletableFuture<?>> tasks = new ArrayList();
            tasks.addAll(this.flushAllChunks());
            tasks.addAll(this.flushAllLines());
            tasks.addAll(this.flushAllEllipses());
            tasks.addAll(this.flushAllTexts());
            CompletableFuture<Void> future = CompletableFuture.allOf((CompletableFuture[])tasks.toArray((x$0) -> new CompletableFuture[x$0]));
            Wait.waitUntil(() -> !this.mc.method_22108() || future.isDone(), 30);
         } catch (Exception e) {
            XaeroPlus.LOGGER.error("Error saving all drawing data before disabling", e);
         }

         this.reset();
      });
   }

   public CompletableFuture<Void> onShutdown() {
      if (!this.mc.method_18854()) {
         this.onDisable();
         return CompletableFuture.completedFuture((Object)null);
      } else {
         this.cacheReady.set(false);

         for(DrawingHighlightCacheDimensionHandler cache : this.getAllHighlightCaches()) {
            cache.writeStaleHighlightsToDatabase();
         }

         for(DrawingLinesCacheDimensionHandler cache : this.getAllLinesCaches()) {
            cache.writeStaleLinesToDatabase();
         }

         for(DrawingEllipseCacheDimensionHandler cache : this.getAllEllipseCaches()) {
            cache.writeStaleEllipsesToDatabase();
         }

         for(DrawingTextCacheDimensionHandler cache : this.getAllTextsCaches()) {
            cache.writeStaleTextsToDatabase();
         }

         return CompletableFuture.runAsync(this::closeAndAwaitTermination);
      }
   }

   public int getMinimapRegionWindowSize() {
      return Math.max(3, Globals.minimapScaleMultiplier);
   }

   public void handleTick() {
      if (this.cacheReady.get()) {
         if (XaeroWorldMapCore.currentSession != null) {
            int jitter = ThreadLocalRandom.current().nextInt(0, 10);
            if (this.flushTimer.tick((long)(600 + jitter))) {
               this.flushAllChunks();
               this.flushAllLines();
               this.flushAllEllipses();
               this.flushAllTexts();
            }

            if (this.tickTimer.tick((long)(10 + jitter))) {
               class_5321<class_1937> mapDimension = Globals.getCurrentDimensionId();
               class_5321<class_1937> actualDimension = ChunkUtils.getActualDimension();
               int actualPlayerRegionX = ChunkUtils.actualPlayerRegionX();
               int actualPlayerRegionZ = ChunkUtils.actualPlayerRegionZ();
               Optional<GuiMap> guiMapOptional = GuiMapHelper.getGuiMap();
               int windowSize;
               int windowCenterX;
               int windowCenterZ;
               if (guiMapOptional.isPresent()) {
                  GuiMap guiMap = (GuiMap)guiMapOptional.get();
                  windowSize = GuiMapHelper.getGuiMapRegionSize(guiMap);
                  windowCenterX = GuiMapHelper.getGuiMapCenterRegionX(guiMap);
                  windowCenterZ = GuiMapHelper.getGuiMapCenterRegionZ(guiMap);
               } else {
                  windowSize = this.getMinimapRegionWindowSize();
                  windowCenterX = ChunkUtils.getPlayerRegionX();
                  windowCenterZ = ChunkUtils.getPlayerRegionZ();
               }

               DrawingHighlightCacheDimensionHandler highlightCacheForDimension = this.getHighlightCacheForDimension(mapDimension, true);
               if (highlightCacheForDimension != null) {
                  highlightCacheForDimension.setWindow(windowCenterX, windowCenterZ, windowSize);
               }

               DrawingTextCacheDimensionHandler textCacheForDimension = this.getTextCacheForDimension(mapDimension, true);
               if (textCacheForDimension != null) {
                  textCacheForDimension.setWindow(windowCenterX, windowCenterZ, windowSize);
               }

               DrawingLinesCacheDimensionHandler lineCacheForDimension = this.getLinesCacheForDimension(mapDimension, true);
               if (lineCacheForDimension != null) {
                  lineCacheForDimension.setWindow(windowCenterX, windowCenterZ, windowSize);
               }

               DrawingEllipseCacheDimensionHandler ellipseCacheForDimension = this.getEllipseCacheForDimension(mapDimension, true);
               if (ellipseCacheForDimension != null) {
                  ellipseCacheForDimension.setWindow(windowCenterX, windowCenterZ, windowSize);
               }

               if (mapDimension == actualDimension) {
                  this.getHighlightCachesExceptDimension(mapDimension).forEach((cache) -> cache.setWindow(0, 0, 0));
                  this.getTextCachesExceptDimension(mapDimension).forEach((cache) -> cache.setWindow(0, 0, 0));
                  this.getLineCachesExceptDimension(mapDimension).forEach((cache) -> cache.setWindow(0, 0, 0));
                  this.getEllipseCachesExceptDimension(mapDimension).forEach((cache) -> cache.setWindow(0, 0, 0));
               } else {
                  DrawingHighlightCacheDimensionHandler actualDimHighlightCache = this.getHighlightCacheForDimension(actualDimension, true);
                  if (actualDimHighlightCache != null) {
                     actualDimHighlightCache.setWindow(actualPlayerRegionX, actualPlayerRegionZ, windowSize);
                  }

                  DrawingTextCacheDimensionHandler actualDimTextCache = this.getTextCacheForDimension(actualDimension, true);
                  if (actualDimTextCache != null) {
                     actualDimTextCache.setWindow(actualPlayerRegionX, actualPlayerRegionZ, windowSize);
                  }

                  DrawingLinesCacheDimensionHandler actualDimLinesCache = this.getLinesCacheForDimension(actualDimension, true);
                  if (actualDimLinesCache != null) {
                     actualDimLinesCache.setWindow(actualPlayerRegionX, actualPlayerRegionZ, windowSize);
                  }

                  DrawingEllipseCacheDimensionHandler actualDimEllipseCache = this.getEllipseCacheForDimension(actualDimension, true);
                  if (actualDimEllipseCache != null) {
                     actualDimEllipseCache.setWindow(actualPlayerRegionX, actualPlayerRegionZ, windowSize);
                  }

                  this.getHighlightCachesExceptDimensions(List.of(mapDimension, actualDimension)).forEach((cache) -> cache.setWindow(0, 0, 0));
                  this.getTextCachesExceptDimensions(List.of(mapDimension, actualDimension)).forEach((cache) -> cache.setWindow(0, 0, 0));
                  this.getLineCachesExceptDimensions(List.of(mapDimension, actualDimension)).forEach((cache) -> cache.setWindow(0, 0, 0));
                  this.getEllipseCachesExceptDimensions(List.of(mapDimension, actualDimension)).forEach((cache) -> cache.setWindow(0, 0, 0));
               }

            }
         }
      }
   }

   public void close() throws IOException {
      this.parentExecutor.execute(() -> {
         ListeningExecutorService dbExec = this.dbExecutor;
         if (dbExec != null && !dbExec.isShutdown()) {
            dbExec.execute(() -> {
               if (this.database != null) {
                  this.database.close();
               }

            });
            dbExec.shutdown();
         }

      });
      this.parentExecutor.shutdown();
   }

   public void closeAndAwaitTermination() {
      this.parentExecutor.execute(() -> {
         ListeningExecutorService dbExec = this.dbExecutor;
         if (dbExec != null && !dbExec.isShutdown()) {
            dbExec.execute(() -> {
               if (this.database != null) {
                  this.database.close();
               }

            });
            dbExec.shutdown();

            try {
               if (!dbExec.awaitTermination(5L, TimeUnit.SECONDS)) {
                  throw new RuntimeException("Failed to shutdown drawing executor");
               }
            } catch (Exception e) {
               XaeroPlus.LOGGER.error("Error waiting for drawing db executor to shutdown", e);
            }
         }

      });
      this.parentExecutor.shutdown();

      try {
         if (!this.parentExecutor.awaitTermination(5L, TimeUnit.SECONDS)) {
            throw new RuntimeException("Failed to shutdown drawing executor");
         }
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Error waiting for executor to shutdown", e);
      }

   }
}
