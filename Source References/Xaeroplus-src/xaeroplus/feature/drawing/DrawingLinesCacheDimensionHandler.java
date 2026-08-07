package xaeroplus.feature.drawing;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.class_1937;
import net.minecraft.class_310;
import net.minecraft.class_5321;
import xaeroplus.XaeroPlus;
import xaeroplus.feature.drawing.db.DrawingDatabase;
import xaeroplus.feature.render.line.Line;
import xaeroplus.util.ChunkUtils;

public class DrawingLinesCacheDimensionHandler {
   private final class_5321<class_1937> dimension;
   private int windowRegionX = 0;
   private int windowRegionZ = 0;
   private int windowRegionSize = 0;
   private final DrawingDatabase database;
   private final ListeningExecutorService dbExecutor;
   private final Object2IntMap<Line> lines = new Object2IntOpenHashMap();
   public final Set<Line> staleLines = new HashSet();
   ListenableFuture<?> windowMoveFuture = Futures.immediateVoidFuture();
   class_310 mc = class_310.method_1551();

   public DrawingLinesCacheDimensionHandler(class_5321<class_1937> dimension, DrawingDatabase database, ListeningExecutorService dbExecutor) {
      this.dimension = dimension;
      this.database = database;
      this.dbExecutor = dbExecutor;
   }

   public void addLine(Line line, int color) {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("addLine must be called on the main thread!");
      } else {
         this.lines.put(line, color);
         this.staleLines.add(line);
         this.writeStaleLinesToDatabase();
      }
   }

   public void removeLine(Line line) {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("removeLine must be called on the main thread!");
      } else {
         if (this.lines.containsKey(line)) {
            this.lines.removeInt(line);
            this.staleLines.add(line);
            this.dbExecutor.execute(() -> this.database.removeLine(line.x1(), line.z1(), line.x2(), line.z2(), this.dimension));
         }

      }
   }

   public void removeAllLines() {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("removeAllLines must be called on the main thread!");
      } else {
         this.lines.clear();
         this.staleLines.clear();
         this.dbExecutor.execute(() -> this.database.removeAllLines(this.dimension));
      }
   }

   public Object2IntMap<Line> getLines() {
      return this.lines;
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
               this.windowMoveFuture = this.moveWindow0(regionX, regionZ, regionSize);
            } catch (Exception e) {
               XaeroPlus.LOGGER.error("Failed submitting move window task for {} disk cache dimension: {}", new Object[]{this.database.databaseName, this.dimension.method_29177(), e});
            }
         }

      } else {
         XaeroPlus.LOGGER.debug("Rejecting window move to: [{} {} {}] from: [{} {} {}]", new Object[]{regionX, regionZ, regionSize, this.windowRegionX, this.windowRegionZ, this.windowRegionSize});
      }
   }

   protected ListenableFuture<?> moveWindow0(final int windowRegionX, final int windowRegionZ, final int windowRegionSize) {
      ListenableFuture<Object2IntMap<Line>> loadDataFuture = this.dbExecutor.submit(() -> this.loadLinesFromDatabase(windowRegionX, windowRegionZ, windowRegionSize));
      Futures.addCallback(loadDataFuture, new LineDataLoadFutureCallback(), this.mc);
      ListenableFuture<?> removeDataFuture = this.flushLinesOutsideWindow(windowRegionX, windowRegionZ, windowRegionSize);
      return Futures.allAsList(new ListenableFuture[]{loadDataFuture, removeDataFuture});
   }

   private Object2IntMap<Line> loadLinesFromDatabase(final int windowRegionX, final int windowRegionZ, final int windowRegionSize) {
      Object2IntMap<Line> dataBuf = new Object2IntOpenHashMap();
      int windowXMin = ChunkUtils.regionCoordToCoord(windowRegionX - windowRegionSize);
      int windowZMin = ChunkUtils.regionCoordToCoord(windowRegionZ - windowRegionSize);
      int windowXMax = ChunkUtils.regionCoordToCoord(windowRegionX + windowRegionSize);
      int windowZMax = ChunkUtils.regionCoordToCoord(windowRegionZ + windowRegionSize);
      this.database.getLinesInDimension(this.dimension, (x1, z1, x2, z2, color) -> {
         Line line = new Line(x1, z1, x2, z2);
         if (line.lineClip(windowXMin, windowXMax, windowZMin, windowZMax)) {
            dataBuf.put(line, color);
         }

      });
      return dataBuf;
   }

   private ListenableFuture<?> flushLinesOutsideWindow(final int windowRegionX, final int windowRegionZ, final int windowRegionSize) {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("removeChunksOutsideWindow must be called on the main thread");
      } else {
         Object2IntMap<Line> dataBuf = new Object2IntOpenHashMap();
         int windowXMin = ChunkUtils.regionCoordToCoord(windowRegionX - windowRegionSize);
         int windowZMin = ChunkUtils.regionCoordToCoord(windowRegionZ - windowRegionSize);
         int windowXMax = ChunkUtils.regionCoordToCoord(windowRegionX + windowRegionSize);
         int windowZMax = ChunkUtils.regionCoordToCoord(windowRegionZ + windowRegionSize);
         ObjectIterator<Line> it = this.lines.keySet().iterator();

         while(it.hasNext()) {
            Line line = (Line)it.next();
            if (!line.lineClip(windowXMin, windowXMax, windowZMin, windowZMax)) {
               if (this.staleLines.contains(line)) {
                  dataBuf.put(line, this.lines.getInt(line));
               }

               it.remove();
            }
         }

         return this.dbExecutor.submit(() -> this.database.insertLinesList(dataBuf, this.dimension));
      }
   }

   public ListenableFuture<?> writeStaleLinesToDatabase() {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("writeStaleHighlightsToDatabase must be called on the main thread");
      } else {
         Object2IntMap<Line> toWrite = this.collectStaleLinesToWrite();
         return toWrite.isEmpty() ? Futures.immediateVoidFuture() : this.writeDataToDatabase(toWrite);
      }
   }

   public Object2IntMap<Line> collectStaleLinesToWrite() {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("collectStaleHighlightsToWrite must be called on the main thread");
      } else if (this.staleLines.isEmpty()) {
         return Object2IntMaps.emptyMap();
      } else {
         Object2IntMap<Line> linesToWrite = new Object2IntOpenHashMap(this.staleLines.size());

         for(Iterator<Line> it = this.staleLines.iterator(); it.hasNext(); it.remove()) {
            Line line = (Line)it.next();
            if (this.lines.containsKey(line)) {
               linesToWrite.put(line, this.lines.getInt(line));
            }
         }

         return linesToWrite;
      }
   }

   public ListenableFuture<?> writeDataToDatabase(Object2IntMap<Line> toWrite) {
      try {
         return this.dbExecutor.submit(() -> this.database.insertLinesList(toWrite, this.dimension));
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Failed to submit db write task for {} disk cache dimension: {}", new Object[]{this.database.databaseName, this.dimension.method_29177(), e});
         return Futures.immediateFailedFuture(e);
      }
   }

   private final class LineDataLoadFutureCallback implements FutureCallback<Object2IntMap<Line>> {
      public void onSuccess(Object2IntMap<Line> dataBuf) {
         if (!DrawingLinesCacheDimensionHandler.this.mc.method_18854()) {
            XaeroPlus.LOGGER.error("LineDataLoadFutureCallback must be called on the main thread");
         }

         if (!dataBuf.isEmpty()) {
            DrawingLinesCacheDimensionHandler.this.lines.putAll(dataBuf);
         }
      }

      public void onFailure(Throwable t) {
         XaeroPlus.LOGGER.error("Error loading lines {} disk cache dimension: {}", new Object[]{DrawingLinesCacheDimensionHandler.this.database.databaseName, DrawingLinesCacheDimensionHandler.this.dimension.method_29177(), t});
      }
   }
}
