package xaeroplus.feature.drawing;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.class_1937;
import net.minecraft.class_310;
import net.minecraft.class_5321;
import xaeroplus.XaeroPlus;
import xaeroplus.feature.drawing.db.DrawingDatabase;
import xaeroplus.feature.render.text.Text;
import xaeroplus.util.ChunkUtils;

public class DrawingTextCacheDimensionHandler {
   private final class_5321<class_1937> dimension;
   private int windowRegionX = 0;
   private int windowRegionZ = 0;
   private int windowRegionSize = 0;
   private final DrawingDatabase database;
   private final ListeningExecutorService dbExecutor;
   private final Long2ObjectMap<Text> texts = new Long2ObjectOpenHashMap();
   public final LongSet staleTexts = new LongOpenHashSet();
   class_310 mc = class_310.method_1551();
   ListenableFuture<?> windowMoveFuture = Futures.immediateVoidFuture();

   public DrawingTextCacheDimensionHandler(class_5321<class_1937> dimension, DrawingDatabase database, ListeningExecutorService dbExecutor) {
      this.dimension = dimension;
      this.database = database;
      this.dbExecutor = dbExecutor;
   }

   public void addText(Text text) {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("addText must be called on the main thread");
      } else {
         long key = ChunkUtils.chunkPosToLong(text.x(), text.z());
         this.texts.put(key, text);
         this.staleTexts.add(key);
         this.writeStaleTextsToDatabase();
      }
   }

   public void removeText(int x, int z) {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("removeText must be called on the main thread!");
      } else {
         long key = ChunkUtils.chunkPosToLong(x, z);
         if (this.texts.containsKey(key)) {
            this.texts.remove(key);
            this.staleTexts.add(key);
            this.dbExecutor.execute(() -> this.database.removeText(x, z, this.dimension));
         }

      }
   }

   public void removeAllTexts() {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("removeAllTexts must be called on the main thread!");
      } else {
         this.texts.clear();
         this.staleTexts.clear();
         this.dbExecutor.execute(() -> this.database.removeAllTexts(this.dimension));
      }
   }

   public Long2ObjectMap<Text> getTexts() {
      return this.texts;
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
      ListenableFuture<Long2ObjectMap<Text>> loadDataFuture = this.dbExecutor.submit(() -> this.loadTextsFromDatabase(windowRegionX, windowRegionZ, windowRegionSize, prevWindowRegionX, prevWindowRegionZ, prevWindowRegionSize));
      Futures.addCallback(loadDataFuture, new WindowDataLoadFutureCallback(), this.mc);
      ListenableFuture<?> removeDataFuture = this.flushTextsOutsideWindow(windowRegionX, windowRegionZ, windowRegionSize);
      return Futures.allAsList(new ListenableFuture[]{loadDataFuture, removeDataFuture});
   }

   public Long2ObjectMap<Text> loadTextsFromDatabase(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final int prevWindowRegionX, final int prevWindowRegionZ, final int prevWindowRegionSize) {
      Long2ObjectMap<Text> texts = new Long2ObjectOpenHashMap();
      this.database.getTextsInWindow(this.dimension, windowRegionX - windowRegionSize, windowRegionX + windowRegionSize, windowRegionZ - windowRegionSize, windowRegionZ + windowRegionSize, (text) -> texts.put(ChunkUtils.chunkPosToLong(text.x(), text.z()), text));
      return texts;
   }

   private ListenableFuture<?> flushTextsOutsideWindow(final int windowRegionX, final int windowRegionZ, final int windowRegionSize) {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("flushTextsOutsideWindow must be called on the main thread");
      } else {
         Long2ObjectMap<Text> dataBuf = new Long2ObjectOpenHashMap();
         int blockXMin = ChunkUtils.regionCoordToCoord(windowRegionX - windowRegionSize);
         int blockXMax = ChunkUtils.regionCoordToCoord(windowRegionX + windowRegionSize);
         int blockZMin = ChunkUtils.regionCoordToCoord(windowRegionZ - windowRegionSize);
         int blockZMax = ChunkUtils.regionCoordToCoord(windowRegionZ + windowRegionSize);
         LongIterator it = this.texts.keySet().longIterator();

         while(it.hasNext()) {
            long key = it.nextLong();
            int blockX = ChunkUtils.longToChunkX(key);
            int blockZ = ChunkUtils.longToChunkZ(key);
            if (blockX < blockXMin || blockX > blockXMax || blockZ < blockZMin || blockZ > blockZMax) {
               if (this.staleTexts.contains(key)) {
                  dataBuf.put(key, (Text)this.texts.get(key));
               }

               it.remove();
            }
         }

         return this.dbExecutor.submit(() -> this.database.insertTextsList(dataBuf, this.dimension));
      }
   }

   public Long2ObjectMap<Text> collectStaleTextsToWrite() {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("collectStaleTextsToWrite must be called on the main thread");
      } else if (this.staleTexts.isEmpty()) {
         return Long2ObjectMaps.emptyMap();
      } else {
         Long2ObjectMap<Text> textsToWrite = new Long2ObjectOpenHashMap(this.staleTexts.size());

         for(LongIterator it = this.staleTexts.longIterator(); it.hasNext(); it.remove()) {
            long key = it.nextLong();
            Text text = (Text)this.texts.get(key);
            if (text != null) {
               textsToWrite.put(key, text);
            }
         }

         return textsToWrite;
      }
   }

   public ListenableFuture<?> writeDataToDatabase(Long2ObjectMap<Text> toWrite) {
      try {
         return this.dbExecutor.submit(() -> this.database.insertTextsList(toWrite, this.dimension));
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Failed to submit db write task for {} disk cache dimension: {}", new Object[]{this.database.databaseName, this.dimension.method_29177(), e});
         return Futures.immediateFailedFuture(e);
      }
   }

   public ListenableFuture<?> writeStaleTextsToDatabase() {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("writeStaleHighlightsToDatabase must be called on the main thread");
      } else {
         Long2ObjectMap<Text> toWrite = this.collectStaleTextsToWrite();
         return toWrite.isEmpty() ? Futures.immediateVoidFuture() : this.writeDataToDatabase(toWrite);
      }
   }

   private final class WindowDataLoadFutureCallback implements FutureCallback<Long2ObjectMap<Text>> {
      public void onSuccess(Long2ObjectMap<Text> dataBuf) {
         if (!DrawingTextCacheDimensionHandler.this.mc.method_18854()) {
            XaeroPlus.LOGGER.error("WindowDataLoadFutureCallback must be called on the main thread");
         }

         if (!dataBuf.isEmpty()) {
            DrawingTextCacheDimensionHandler.this.texts.putAll(dataBuf);
         }
      }

      public void onFailure(Throwable t) {
         XaeroPlus.LOGGER.error("Error while moving window for {} disk cache dimension: {}", new Object[]{DrawingTextCacheDimensionHandler.this.database.databaseName, DrawingTextCacheDimensionHandler.this.dimension.method_29177(), t});
      }
   }
}
