package xaeroplus.feature.drawing;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import xaeroplus.XaeroPlus;
import xaeroplus.event.XaeroWorldChangeEvent;
import xaeroplus.feature.drawing.db.DrawingDatabase;
import xaeroplus.feature.highlights.ChunkHighlightBaseCacheHandler;
import xaeroplus.util.ChunkUtils;

public class DrawingHighlightCacheDimensionHandler extends ChunkHighlightBaseCacheHandler {
   private final class_5321<class_1937> dimension;
   private int windowRegionX = 0;
   private int windowRegionZ = 0;
   private int windowRegionSize = 0;
   private final DrawingDatabase database;
   private final ListeningExecutorService dbExecutor;
   public final LongSet staleChunks = new LongOpenHashSet();
   ListenableFuture<?> windowMoveFuture = Futures.immediateVoidFuture();

   public DrawingHighlightCacheDimensionHandler(class_5321<class_1937> dimension, DrawingDatabase database, ListeningExecutorService dbExecutor) {
      this.dimension = dimension;
      this.database = database;
      this.dbExecutor = dbExecutor;
   }

   public synchronized void setWindow(int regionX, int regionZ, int regionSize) {
      boolean windowChanged = regionX != this.windowRegionX || regionZ != this.windowRegionZ || regionSize != this.windowRegionSize;
      if (!windowChanged || this.windowMoveFuture.isDone() || regionX == 0 && regionZ == 0 && regionSize == 0) {
         int prevWindowRegionX = this.windowRegionX;
         int prevWindowRegionZ = this.windowRegionZ;
         int prevWindowRegionSize = this.windowRegionSize;
         this.windowRegionX = regionX;
         this.windowRegionZ = regionZ;
         this.windowRegionSize = regionSize;
         if (windowChanged) {
            try {
               this.windowMoveFuture = this.moveWindow0(regionX, regionZ, regionSize, prevWindowRegionX, prevWindowRegionZ, prevWindowRegionSize);
            } catch (Exception e) {
               XaeroPlus.LOGGER.error("Failed submitting move window task for {} disk cache dimension: {}", new Object[]{this.database.databaseName, this.dimension.method_29177(), e});
            }
         }

      } else {
         XaeroPlus.LOGGER.debug("Rejecting window move to: [{} {} {}] from: [{} {} {}]", new Object[]{regionX, regionZ, regionSize, this.windowRegionX, this.windowRegionZ, this.windowRegionSize});
      }
   }

   private ListenableFuture<?> moveWindow0(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final int prevWindowRegionX, final int prevWindowRegionZ, final int prevWindowRegionSize) {
      ListenableFuture<Long2LongMap> loadDataFuture = this.dbExecutor.submit(() -> this.loadUpdatedWindowFromDatabase(windowRegionX, windowRegionZ, windowRegionSize, prevWindowRegionX, prevWindowRegionZ, prevWindowRegionSize));
      Futures.addCallback(loadDataFuture, new WindowDataLoadFutureCallback(), this.mc);
      ListenableFuture<?> removeDataFuture = this.flushChunksOutsideWindow(windowRegionX, windowRegionZ, windowRegionSize);
      return Futures.allAsList(new ListenableFuture[]{loadDataFuture, removeDataFuture});
   }

   private Long2LongMap loadUpdatedWindowFromDatabase(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final int prevWindowRegionX, final int prevWindowRegionZ, final int prevWindowRegionSize) {
      Long2LongMap dataBuf = new Long2LongOpenHashMap();
      this.database.getHighlightsInWindow(this.dimension, windowRegionX - windowRegionSize, windowRegionX + windowRegionSize, windowRegionZ - windowRegionSize, windowRegionZ + windowRegionSize, (chunkX, chunkZ, foundTime) -> dataBuf.put(ChunkUtils.chunkPosToLong(chunkX, chunkZ), (long)foundTime));
      return dataBuf;
   }

   private ListenableFuture<?> flushChunksOutsideWindow(final int windowRegionX, final int windowRegionZ, final int windowRegionSize) {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("removeChunksOutsideWindow must be called on the main thread");
      } else {
         Long2LongMap dataBuf = new Long2LongOpenHashMap();
         int chunkXMin = ChunkUtils.regionCoordToChunkCoord(windowRegionX - windowRegionSize);
         int chunkXMax = ChunkUtils.regionCoordToChunkCoord(windowRegionX + windowRegionSize);
         int chunkZMin = ChunkUtils.regionCoordToChunkCoord(windowRegionZ - windowRegionSize);
         int chunkZMax = ChunkUtils.regionCoordToChunkCoord(windowRegionZ + windowRegionSize);
         LongIterator it = this.chunks.keySet().longIterator();

         while(it.hasNext()) {
            long chunkPos = it.nextLong();
            int chunkX = ChunkUtils.longToChunkX(chunkPos);
            int chunkZ = ChunkUtils.longToChunkZ(chunkPos);
            if (chunkX < chunkXMin || chunkX > chunkXMax || chunkZ < chunkZMin || chunkZ > chunkZMax) {
               if (this.staleChunks.contains(chunkPos)) {
                  dataBuf.put(chunkPos, this.chunks.get(chunkPos));
               }

               it.remove();
            }
         }

         return this.dbExecutor.submit(() -> this.database.insertHighlightList(dataBuf, this.dimension));
      }
   }

   public Long2LongMap collectStaleHighlightsToWrite() {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("collectStaleHighlightsToWrite must be called on the main thread");
      } else if (this.staleChunks.isEmpty()) {
         return Long2LongMaps.EMPTY_MAP;
      } else {
         Long2LongMap chunksToWrite = new Long2LongOpenHashMap(this.staleChunks.size());

         for(LongIterator it = this.staleChunks.longIterator(); it.hasNext(); it.remove()) {
            long chunkPos = it.nextLong();
            if (this.chunks.containsKey(chunkPos)) {
               chunksToWrite.put(chunkPos, this.chunks.get(chunkPos));
            }
         }

         return chunksToWrite;
      }
   }

   public ListenableFuture<?> writeDataToDatabase(Long2LongMap toWrite) {
      try {
         return this.dbExecutor.submit(() -> this.database.insertHighlightList(toWrite, this.dimension));
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Failed to submit db write task for {} disk cache dimension: {}", new Object[]{this.database.databaseName, this.dimension.method_29177(), e});
         return Futures.immediateFailedFuture(e);
      }
   }

   public ListenableFuture<?> writeStaleHighlightsToDatabase() {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("writeStaleHighlightsToDatabase must be called on the main thread");
      } else {
         Long2LongMap toWrite = this.collectStaleHighlightsToWrite();
         return toWrite.isEmpty() ? Futures.immediateVoidFuture() : this.writeDataToDatabase(toWrite);
      }
   }

   public void addHighlight(final int x, final int z, final long foundTime) {
      super.addHighlight(x, z, foundTime);
      this.staleChunks.add(ChunkUtils.chunkPosToLong(x, z));
   }

   public void addHighlight(final int x, final int z, final long foundTime, final class_5321<class_1937> dimensionId) {
      super.addHighlight(x, z, foundTime, dimensionId);
      this.staleChunks.add(ChunkUtils.chunkPosToLong(x, z));
   }

   public void addHighlight(final int x, final int z, final class_5321<class_1937> dimensionId) {
      super.addHighlight(x, z, dimensionId);
      this.staleChunks.add(ChunkUtils.chunkPosToLong(x, z));
   }

   public void removeHighlight(final int x, final int z) {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("removeHighlight must be called on the main thread!");
      } else {
         long key = ChunkUtils.chunkPosToLong(x, z);
         if (this.chunks.containsKey(key)) {
            super.removeHighlight(x, z);
            this.dbExecutor.execute(() -> this.database.removeHighlight(x, z, this.dimension));
         }

      }
   }

   public void removeHighlights(final LongCollection toRemove) {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("removeHighlights must be called on the main thread!");
      } else {
         toRemove.forEach((pos) -> {
            int x = ChunkUtils.longToChunkX(pos);
            int z = ChunkUtils.longToChunkZ(pos);
            super.removeHighlight(x, z);
         });
         this.dbExecutor.execute(() -> this.database.removeHighlights(toRemove, this.dimension));
      }
   }

   public void removeAllHighlights() {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("removeAllHighlights must be called on the main thread!");
      } else {
         this.staleChunks.clear();
         this.chunks.clear();
         this.dbExecutor.execute(() -> this.database.removeAllHighlights(this.dimension));
      }
   }

   public CompletableFuture<Long2LongMap> getHighlightsInCustomWindow(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final class_5321<class_1937> dimension) {
      return this.submitTickTask(this::writeStaleHighlightsToDatabase).thenApplyAsync((v) -> {
         int regionXMin = windowRegionX - windowRegionSize;
         int regionZMin = windowRegionZ - windowRegionSize;
         int regionXMax = windowRegionX + windowRegionSize;
         int regionZMax = windowRegionZ + windowRegionSize;
         Long2LongOpenHashMap resultMap = new Long2LongOpenHashMap();
         ListenableFuture<?> dbLoadFuture = this.dbExecutor.submit(() -> this.database.getHighlightsInWindow(dimension, regionXMin, regionXMax, regionZMin, regionZMax, (chunkX, chunkZ, foundTime) -> resultMap.put(ChunkUtils.chunkPosToLong(chunkX, chunkZ), (long)foundTime)));

         try {
            dbLoadFuture.get();
            return resultMap;
         } catch (Exception e) {
            XaeroPlus.LOGGER.error("Failed to load highlights in custom window for {} disk cache dimension: {}", new Object[]{this.database.databaseName, this.dimension.method_29177(), e});
            return Long2LongMaps.EMPTY_MAP;
         }
      });
   }

   public void handleWorldChange(final XaeroWorldChangeEvent event) {
   }

   public void handleTick() {
   }

   public void onEnable() {
   }

   public void onDisable() {
   }

   private final class WindowDataLoadFutureCallback implements FutureCallback<Long2LongMap> {
      public void onSuccess(Long2LongMap dataBuf) {
         if (!DrawingHighlightCacheDimensionHandler.this.mc.method_18854()) {
            XaeroPlus.LOGGER.error("WindowDataLoadFutureCallback must be called on the main thread");
         }

         if (!dataBuf.isEmpty()) {
            DrawingHighlightCacheDimensionHandler.this.chunks.putAll(dataBuf);
         }
      }

      public void onFailure(Throwable t) {
         XaeroPlus.LOGGER.error("Error while moving window for {} disk cache dimension: {}", new Object[]{DrawingHighlightCacheDimensionHandler.this.database.databaseName, DrawingHighlightCacheDimensionHandler.this.dimension.method_29177(), t});
      }
   }
}
