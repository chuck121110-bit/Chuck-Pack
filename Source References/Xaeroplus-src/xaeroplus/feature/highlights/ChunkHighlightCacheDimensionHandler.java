package xaeroplus.feature.highlights;

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
import it.unimi.dsi.fastutil.longs.LongSets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import org.jetbrains.annotations.NotNull;
import xaeroplus.XaeroPlus;
import xaeroplus.event.XaeroWorldChangeEvent;
import xaeroplus.util.ChunkUtils;

public class ChunkHighlightCacheDimensionHandler extends ChunkHighlightBaseCacheHandler {
   private final @NotNull class_5321<class_1937> dimension;
   private int windowRegionX = 0;
   private int windowRegionZ = 0;
   private int windowRegionSize = 0;
   private final @NotNull ChunkHighlightDatabase database;
   private final @NotNull ListeningExecutorService dbExecutor;
   public final LongSet staleChunks = new LongOpenHashSet();
   public final LongSet staleToRemoveChunks = new LongOpenHashSet();
   ListenableFuture<?> windowMoveFuture = Futures.immediateVoidFuture();

   public ChunkHighlightCacheDimensionHandler(@NotNull class_5321<class_1937> dimension, @NotNull ChunkHighlightDatabase database, @NotNull ListeningExecutorService dbExecutor) {
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
      ListenableFuture<?> flushToDeleteChunksFuture = this.flushStaleToRemoveChunks();
      ListenableFuture<?> flushOutsideWindowFuture = this.flushChunksOutsideWindow(windowRegionX, windowRegionZ, windowRegionSize);
      return Futures.allAsList(new ListenableFuture[]{loadDataFuture, flushToDeleteChunksFuture, flushOutsideWindowFuture});
   }

   private Long2LongMap loadUpdatedWindowFromDatabase(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final int prevWindowRegionX, final int prevWindowRegionZ, final int prevWindowRegionSize) {
      Long2LongMap dataBuf = new Long2LongOpenHashMap();
      this.database.getHighlightsInWindowAndOutsidePrevWindow(this.dimension, windowRegionX - windowRegionSize, windowRegionX + windowRegionSize, windowRegionZ - windowRegionSize, windowRegionZ + windowRegionSize, prevWindowRegionX - prevWindowRegionSize, prevWindowRegionX + prevWindowRegionSize, prevWindowRegionZ - prevWindowRegionSize, prevWindowRegionZ + prevWindowRegionSize, (chunkX, chunkZ, foundTime) -> dataBuf.put(ChunkUtils.chunkPosToLong(chunkX, chunkZ), foundTime));
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

         if (dataBuf.isEmpty()) {
            return Futures.immediateVoidFuture();
         } else {
            return this.dbExecutor.submit(() -> this.database.insertHighlightList(dataBuf, this.dimension));
         }
      }
   }

   public ListenableFuture<?> flushStaleToRemoveChunks() {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("flushStaleToRemoveChunks must be called on the main thread");
      } else {
         LongCollection dataBuf = this.collectStaleToRemoveChunks();
         if (dataBuf.isEmpty()) {
            return Futures.immediateVoidFuture();
         } else {
            try {
               return this.dbExecutor.submit(() -> this.database.removeHighlights(dataBuf, this.dimension));
            } catch (Exception e) {
               XaeroPlus.LOGGER.error("Failed to execute flush stale to remove chunks task for {} disk cache dimension: {}", new Object[]{this.database.databaseName, this.dimension.method_29177(), e});
               return Futures.immediateFailedFuture(e);
            }
         }
      }
   }

   private LongCollection collectStaleToRemoveChunks() {
      if (this.staleToRemoveChunks.isEmpty()) {
         return LongSets.emptySet();
      } else {
         LongOpenHashSet chunksToRemove = new LongOpenHashSet(this.staleToRemoveChunks.size());

         for(LongIterator it = this.staleToRemoveChunks.longIterator(); it.hasNext(); it.remove()) {
            long chunkPos = it.nextLong();
            if (!this.chunks.containsKey(chunkPos)) {
               chunksToRemove.add(chunkPos);
            }
         }

         return chunksToRemove;
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
            long foundTime = this.chunks.get(chunkPos);
            if (foundTime != this.chunks.defaultReturnValue()) {
               chunksToWrite.put(chunkPos, foundTime);
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
      long pos = ChunkUtils.chunkPosToLong(x, z);
      this.staleChunks.add(pos);
      this.staleToRemoveChunks.remove(pos);
   }

   public void addHighlight(final int x, final int z, final long foundTime, final class_5321<class_1937> dimensionId) {
      super.addHighlight(x, z, foundTime, dimensionId);
      long pos = ChunkUtils.chunkPosToLong(x, z);
      this.staleChunks.add(pos);
      this.staleToRemoveChunks.remove(pos);
   }

   public void addHighlight(final int x, final int z, final class_5321<class_1937> dimensionId) {
      super.addHighlight(x, z, dimensionId);
      long pos = ChunkUtils.chunkPosToLong(x, z);
      this.staleChunks.add(pos);
      this.staleToRemoveChunks.remove(pos);
   }

   public void removeHighlight(final int x, final int z) {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("removeHighlight must be called on the main thread!");
      } else {
         long pos = ChunkUtils.chunkPosToLong(x, z);
         super.removeHighlight(x, z);
         this.staleToRemoveChunks.add(pos);
      }
   }

   public void removeHighlights(final LongCollection toRemove) {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("removeHighlights must be called on the main thread!");
      } else {
         Long2LongMap var10001 = this.chunks;
         Objects.requireNonNull(var10001);
         toRemove.forEach(var10001::remove);
         this.staleToRemoveChunks.addAll(toRemove);
      }
   }

   public CompletableFuture<Long2LongMap> getHighlightsInCustomWindow(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final class_5321<class_1937> dimension) {
      return this.submitTickTask(this::writeStaleHighlightsToDatabase).thenApplyAsync((v) -> {
         int regionXMin = windowRegionX - windowRegionSize;
         int regionZMin = windowRegionZ - windowRegionSize;
         int regionXMax = windowRegionX + windowRegionSize;
         int regionZMax = windowRegionZ + windowRegionSize;
         Long2LongOpenHashMap resultMap = new Long2LongOpenHashMap();
         ListenableFuture<?> dbLoadFuture = this.dbExecutor.submit(() -> this.database.getHighlightsInWindow(dimension, regionXMin, regionXMax, regionZMin, regionZMax, (chunkX, chunkZ, foundTime) -> resultMap.put(ChunkUtils.chunkPosToLong(chunkX, chunkZ), foundTime)));

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
         if (!ChunkHighlightCacheDimensionHandler.this.mc.method_18854()) {
            XaeroPlus.LOGGER.error("WindowDataLoadFutureCallback must be called on the main thread");
         }

         if (!dataBuf.isEmpty()) {
            ChunkHighlightCacheDimensionHandler.this.chunks.putAll(dataBuf);
         }
      }

      public void onFailure(Throwable t) {
         XaeroPlus.LOGGER.error("Error while moving window for {} disk cache dimension: {}", new Object[]{ChunkHighlightCacheDimensionHandler.this.database.databaseName, ChunkHighlightCacheDimensionHandler.this.dimension.method_29177(), t});
      }
   }
}
