package xaeroplus.feature.highlights;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.longs.LongCollection;
import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CancellationException;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xaero.map.MapProcessor;
import xaero.map.WorldMapSession;
import xaero.map.core.XaeroWorldMapCore;
import xaero.map.gui.GuiMap;
import xaeroplus.Globals;
import xaeroplus.XaeroPlus;
import xaeroplus.event.XaeroWorldChangeEvent;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.GuiMapHelper;
import xaeroplus.util.Wait;
import xaeroplus.util.timer.Timer;
import xaeroplus.util.timer.Timers;

public class ChunkHighlightSavingCache implements ChunkHighlightCache, Closeable {
   private @Nullable ChunkHighlightDatabase database = null;
   private @Nullable String currentWorldId;
   private final AtomicBoolean cacheReady = new AtomicBoolean(false);
   private final @Nullable String databaseName;
   private @Nullable ListeningExecutorService dbExecutor;
   private final @NotNull ListeningExecutorService parentExecutor;
   private final Map<class_5321<class_1937>, ChunkHighlightCacheDimensionHandler> dimensionCacheMap = new ConcurrentHashMap(3);
   private final Queue<QueuedInitOperation> initOperationQueue = new ConcurrentLinkedQueue();
   class_310 mc = class_310.method_1551();
   private ListenableFuture<?> initializeTask = Futures.immediateVoidFuture();
   final Timer tickTimer = Timers.tickTimer();
   final Timer flushTimer = Timers.tickTimer();

   public ChunkHighlightSavingCache(final @NotNull String databaseName) {
      this.databaseName = databaseName;
      this.parentExecutor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor((new ThreadFactoryBuilder()).setNameFormat(databaseName + "-Manager").setUncaughtExceptionHandler((t, e) -> XaeroPlus.LOGGER.error("Uncaught exception in {}", t.getName(), e)).build()));
   }

   public void addHighlight(final int x, final int z) {
      this.addHighlight(x, z, ChunkUtils.getActualDimension());
   }

   public void addHighlight(final int x, final int z, final class_5321<class_1937> dimension) {
      try {
         ChunkHighlightCacheDimensionHandler cacheForActualDimension = this.getCacheForDimension(dimension, true);
         if (cacheForActualDimension == null) {
            this.addInitOperation(() -> this.addHighlight(x, z, dimension));
            return;
         }

         cacheForActualDimension.addHighlight(x, z);
      } catch (Exception e) {
         XaeroPlus.LOGGER.warn("Error adding highlight to {} disk cache: {}, {}", new Object[]{this.databaseName, x, z, e});
      }

   }

   public void addHighlight(final int x, final int z, final long foundTime) {
      this.addHighlight(x, z, foundTime, ChunkUtils.getActualDimension());
   }

   public void addHighlight(final int x, final int z, final long foundTime, final class_5321<class_1937> dimension) {
      try {
         ChunkHighlightCacheDimensionHandler cacheForActualDimension = this.getCacheForDimension(dimension, true);
         if (cacheForActualDimension == null) {
            this.addInitOperation(() -> this.addHighlight(x, z, foundTime, dimension));
            return;
         }

         cacheForActualDimension.addHighlight(x, z, foundTime);
      } catch (Exception e) {
         XaeroPlus.LOGGER.warn("Error adding highlight to {} disk cache: {}, {}", new Object[]{this.databaseName, x, z, e});
      }

   }

   public void removeHighlight(final int x, final int z) {
      this.removeHighlight(x, z, ChunkUtils.getActualDimension());
   }

   public void removeHighlight(final int x, final int z, final class_5321<class_1937> dimension) {
      try {
         ChunkHighlightCacheDimensionHandler cacheForActualDimension = this.getCacheForDimension(dimension, true);
         if (cacheForActualDimension == null) {
            this.initOperationQueue.add(new QueuedInitOperation(() -> this.removeHighlight(x, z, dimension)));
            return;
         }

         cacheForActualDimension.removeHighlight(x, z);
      } catch (Exception e) {
         XaeroPlus.LOGGER.warn("Error removing highlight from {} disk cache: {}, {}", new Object[]{this.databaseName, x, z, e});
      }

   }

   public void removeHighlights(final LongCollection toRemove) {
      this.removeHighlights(toRemove, ChunkUtils.getActualDimension());
   }

   public void removeHighlights(final LongCollection toRemove, final class_5321<class_1937> dimension) {
      try {
         ChunkHighlightCacheDimensionHandler cacheForActualDimension = this.getCacheForDimension(dimension, true);
         if (cacheForActualDimension == null) {
            this.addInitOperation(() -> this.removeHighlights(toRemove, dimension));
            return;
         }

         cacheForActualDimension.removeHighlights(toRemove);
      } catch (Exception e) {
         XaeroPlus.LOGGER.warn("Error removing highlights from {} disk cache: {}, {}", new Object[]{this.databaseName, toRemove, dimension, e});
      }

   }

   public boolean isHighlighted(final int chunkPosX, final int chunkPosZ, final class_5321<class_1937> dimensionId) {
      if (dimensionId == null) {
         return false;
      } else {
         ChunkHighlightCacheDimensionHandler cacheForDimension = this.getCacheForDimension(dimensionId, false);
         return cacheForDimension == null ? false : cacheForDimension.isHighlighted(chunkPosX, chunkPosZ, dimensionId);
      }
   }

   public Long2LongMap getCacheMap(final class_5321<class_1937> dimensionId) {
      if (dimensionId == null) {
         return Long2LongMaps.EMPTY_MAP;
      } else {
         ChunkHighlightCacheDimensionHandler cacheForDimension = this.getCacheForDimension(dimensionId, false);
         return (Long2LongMap)(cacheForDimension == null ? Long2LongMaps.EMPTY_MAP : cacheForDimension.getCacheMap(dimensionId));
      }
   }

   public CompletableFuture<Long2LongMap> getHighlightsInCustomWindow(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final class_5321<class_1937> dimension) {
      if (dimension == null) {
         return CompletableFuture.completedFuture(Long2LongMaps.EMPTY_MAP);
      } else {
         ChunkHighlightCacheDimensionHandler cacheForDimension = this.getCacheForDimension(dimension, true);
         return cacheForDimension == null ? CompletableFuture.completedFuture(Long2LongMaps.EMPTY_MAP) : cacheForDimension.getHighlightsInCustomWindow(windowRegionX, windowRegionZ, windowRegionSize, dimension);
      }
   }

   public void handleWorldChange(final XaeroWorldChangeEvent event) {
      if (XaeroWorldMapCore.currentSession != null) {
         this.parentExecutor.execute(() -> {
            switch (event.worldChangeType()) {
               case ENTER_WORLD:
                  if (!this.cacheReady.get() && this.initializeTask.isDone()) {
                     this.initializeTask = this.initializeWorld();
                     Futures.addCallback(this.initializeTask, new FutureCallback() {
                        public void onSuccess(final @Nullable Object result) {
                           ChunkHighlightSavingCache.this.cacheReady.compareAndSet(false, true);
                        }

                        public void onFailure(final @NotNull Throwable t) {
                           if (t instanceof CancellationException) {
                              XaeroPlus.LOGGER.warn("{} disk cache initialization cancelled", ChunkHighlightSavingCache.this.databaseName);
                           } else {
                              XaeroPlus.LOGGER.error("Error initializing {} disk cache", ChunkHighlightSavingCache.this.databaseName, t);
                           }

                           ChunkHighlightSavingCache.this.cacheReady.set(false);
                           ChunkHighlightSavingCache.this.reset();
                        }
                     }, this.parentExecutor);
                  } else {
                     XaeroPlus.LOGGER.warn("[{}] Entered world when cache was already initialized", this.databaseName);
                  }
                  break;
               case EXIT_WORLD:
                  if (!this.initializeTask.isDone()) {
                     this.initializeTask.cancel(true);
                  }

                  if (this.cacheReady.compareAndSet(true, false)) {
                     try {
                        CompletableFuture<Void> future = CompletableFuture.allOf((CompletableFuture[])this.flushAllChunks().toArray((x$0) -> new CompletableFuture[x$0]));
                        Wait.waitUntil(() -> !this.mc.method_22108() || future.isDone(), 30);
                     } catch (Exception e) {
                        XaeroPlus.LOGGER.error("Error saving all chunks before disabling", e);
                     }
                  } else {
                     XaeroPlus.LOGGER.warn("[{}] Exited world when cache was already uninitialized", this.databaseName);
                  }

                  this.reset();
                  break;
               case VIEWED_DIMENSION_SWITCH:
                  this.submitTickTask(this::loadChunksInViewedDimension);
                  break;
               case ACTUAL_DIMENSION_SWITCH:
                  this.submitTickTask(this::loadChunksOnActualDimensionSwitch);
            }

         });
      }
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

      this.dimensionCacheMap.clear();
      this.database = null;
      this.initOperationQueue.clear();
   }

   private List<CompletableFuture<?>> flushAllChunks() {
      return (List)this.getAllCaches().stream().map((cache) -> this.submitTickTask(() -> {
            cache.flushStaleToRemoveChunks();
            cache.writeStaleHighlightsToDatabase();
         })).collect(Collectors.toList());
   }

   public ChunkHighlightCacheDimensionHandler getCacheForActualDimension() {
      return !this.cacheReady.get() ? null : this.getCacheForDimension(ChunkUtils.getActualDimension(), true);
   }

   private ChunkHighlightCacheDimensionHandler initializeDimensionCacheHandler(final class_5321<class_1937> dimension) {
      if (dimension == null) {
         return null;
      } else {
         ChunkHighlightDatabase db = this.database;
         ListeningExecutorService executor = this.dbExecutor;
         if (db != null && executor != null) {
            ChunkHighlightCacheDimensionHandler cacheHandler = new ChunkHighlightCacheDimensionHandler(dimension, db, executor);
            db.initializeDimension(dimension);
            this.dimensionCacheMap.put(dimension, cacheHandler);
            return cacheHandler;
         } else {
            XaeroPlus.LOGGER.error("[{}] Unable to initialize {} disk cache handler for: {}, database: {} or executor: {} is null", new Object[]{Thread.currentThread().getName(), this.databaseName, dimension.method_29177(), db, executor});
            return null;
         }
      }
   }

   public ChunkHighlightCacheDimensionHandler getCacheForDimension(final class_5321<class_1937> dimension, boolean create) {
      if (!this.cacheReady.get()) {
         return null;
      } else if (dimension == null) {
         return null;
      } else {
         ChunkHighlightCacheDimensionHandler dimensionCache = (ChunkHighlightCacheDimensionHandler)this.dimensionCacheMap.get(dimension);
         if (dimensionCache == null) {
            if (!create) {
               return null;
            }

            XaeroPlus.LOGGER.info("Initializing {} disk cache for dimension: {}", this.databaseName, dimension.method_29177());
            dimensionCache = this.initializeDimensionCacheHandler(dimension);
         }

         return dimensionCache;
      }
   }

   public List<ChunkHighlightCacheDimensionHandler> getAllCaches() {
      return List.copyOf(this.dimensionCacheMap.values());
   }

   public List<ChunkHighlightCacheDimensionHandler> getCachesExceptDimension(final class_5321<class_1937> dimension) {
      ArrayList<ChunkHighlightCacheDimensionHandler> caches = new ArrayList(this.dimensionCacheMap.size());

      for(Map.Entry<class_5321<class_1937>, ChunkHighlightCacheDimensionHandler> entry : this.dimensionCacheMap.entrySet()) {
         if (!((class_5321)entry.getKey()).equals(dimension)) {
            caches.add((ChunkHighlightCacheDimensionHandler)entry.getValue());
         }
      }

      return caches;
   }

   public List<ChunkHighlightCacheDimensionHandler> getCachesExceptDimensions(final List<class_5321<class_1937>> dimensions) {
      ArrayList<ChunkHighlightCacheDimensionHandler> caches = new ArrayList(this.dimensionCacheMap.size());

      for(Map.Entry<class_5321<class_1937>, ChunkHighlightCacheDimensionHandler> entry : this.dimensionCacheMap.entrySet()) {
         if (!dimensions.contains(entry.getKey())) {
            caches.add((ChunkHighlightCacheDimensionHandler)entry.getValue());
         }
      }

      return caches;
   }

   private synchronized ListenableFuture<?> initializeWorld() {
      try {
         WorldMapSession currentSession = XaeroWorldMapCore.currentSession;
         if (currentSession == null) {
            return Futures.immediateFailedFuture(new IllegalStateException("WorldMapSession is null"));
         } else {
            MapProcessor mapProcessor = currentSession.getMapProcessor();
            if (mapProcessor == null) {
               return Futures.immediateFailedFuture(new IllegalStateException("MapProcessor is null"));
            } else {
               String worldId = mapProcessor.getCurrentWorldId();
               if (worldId == null) {
                  return Futures.immediateFailedFuture(new IllegalStateException("WorldId is null"));
               } else {
                  this.currentWorldId = worldId;
                  this.dbExecutor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor((new ThreadFactoryBuilder()).setNameFormat(this.databaseName + "-Worker").setUncaughtExceptionHandler((t, ex) -> XaeroPlus.LOGGER.error("Uncaught exception handler in {}", t.getName(), ex)).build()));
                  return this.dbExecutor.submit(() -> {
                     this.database = new ChunkHighlightDatabase(worldId, this.databaseName);
                     this.database.initializeDb();
                     this.initializeDimensionCacheHandler(class_1937.field_25179);
                     this.initializeDimensionCacheHandler(class_1937.field_25180);
                     this.initializeDimensionCacheHandler(class_1937.field_25181);
                     this.loadChunksInViewedDimension();
                     if (!this.initOperationQueue.isEmpty()) {
                        XaeroPlus.LOGGER.info("[{}] Running {} queued tasks", this.databaseName, this.initOperationQueue.size());
                     }

                     while(!this.initOperationQueue.isEmpty()) {
                        QueuedInitOperation op = (QueuedInitOperation)this.initOperationQueue.poll();
                        if (op != null && op.task() != null) {
                           this.submitTickTask(op.task());
                        }
                     }

                  });
               }
            }
         }
      } catch (Exception e) {
         this.reset();
         return Futures.immediateFailedFuture(e);
      }
   }

   private void loadChunksOnActualDimensionSwitch() {
      ChunkHighlightCacheDimensionHandler cacheForActualDimension = this.getCacheForActualDimension();
      if (cacheForActualDimension != null) {
         cacheForActualDimension.setWindow(ChunkUtils.actualPlayerRegionX(), ChunkUtils.actualPlayerRegionZ(), this.getMinimapRegionWindowSize());
      }
   }

   private void loadChunksInViewedDimension() {
      class_5321<class_1937> viewedDim = Globals.getCurrentDimensionId();
      ChunkHighlightCacheDimensionHandler cacheForCurrentDimension = this.getCacheForDimension(viewedDim, true);
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
            CompletableFuture<Void> future = CompletableFuture.allOf((CompletableFuture[])this.flushAllChunks().toArray((x$0) -> new CompletableFuture[x$0]));
            Wait.waitUntil(() -> !this.mc.method_22108() || future.isDone(), 30);
         } catch (Exception e) {
            XaeroPlus.LOGGER.error("Error saving all chunks before disabling", e);
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

         for(ChunkHighlightCacheDimensionHandler cache : this.getAllCaches()) {
            cache.flushStaleToRemoveChunks();
            cache.writeStaleHighlightsToDatabase();
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

               ChunkHighlightCacheDimensionHandler cacheForDimension = this.getCacheForDimension(mapDimension, true);
               if (cacheForDimension != null) {
                  cacheForDimension.setWindow(windowCenterX, windowCenterZ, windowSize);
               }

               if (mapDimension == actualDimension) {
                  this.getCachesExceptDimension(mapDimension).forEach((cache) -> cache.setWindow(0, 0, 0));
               } else {
                  ChunkHighlightCacheDimensionHandler actualDimCache = this.getCacheForDimension(actualDimension, true);
                  if (actualDimCache != null) {
                     actualDimCache.setWindow(actualPlayerRegionX, actualPlayerRegionZ, windowSize);
                  }

                  this.getCachesExceptDimensions(List.of(mapDimension, actualDimension)).forEach((cache) -> cache.setWindow(0, 0, 0));
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
                  throw new RuntimeException("Timed out awaiting shutdown termination");
               }
            } catch (Exception e) {
               XaeroPlus.LOGGER.error("Error waiting for {} db executor to shutdown", this.databaseName, e);
            }
         }

      });
      this.parentExecutor.shutdown();

      try {
         if (!this.parentExecutor.awaitTermination(5L, TimeUnit.SECONDS)) {
            throw new RuntimeException("Timed out awaiting shutdown termination");
         }
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Error waiting for {} executor to shutdown", this.databaseName, e);
      }

   }

   private synchronized void addInitOperation(final Runnable task) {
      if (!this.initOperationQueue.isEmpty()) {
         Instant cutoff = Instant.now().minusSeconds(10L);

         while(true) {
            QueuedInitOperation next = (QueuedInitOperation)this.initOperationQueue.peek();
            if (next == null || !next.time().isBefore(cutoff)) {
               break;
            }

            this.initOperationQueue.poll();
         }
      }

      if (this.initOperationQueue.size() <= 5000) {
         this.initOperationQueue.add(new QueuedInitOperation(task));
      }
   }

   static record QueuedInitOperation(Instant time, Runnable task) {
      public QueuedInitOperation(Runnable task) {
         this(Instant.now(), task);
      }
   }
}
