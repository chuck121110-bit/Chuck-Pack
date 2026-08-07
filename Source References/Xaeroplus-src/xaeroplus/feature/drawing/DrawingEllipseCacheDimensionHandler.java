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
import xaeroplus.feature.render.ellipse.Ellipse;
import xaeroplus.util.ChunkUtils;

public class DrawingEllipseCacheDimensionHandler {
   private final class_5321<class_1937> dimension;
   private int windowRegionX;
   private int windowRegionZ;
   private int windowRegionSize;
   private final DrawingDatabase database;
   private final ListeningExecutorService dbExecutor;
   private final Object2IntMap<Ellipse> ellipses = new Object2IntOpenHashMap();
   private final Set<Ellipse> staleEllipses = new HashSet();
   private ListenableFuture<?> windowMoveFuture = Futures.immediateVoidFuture();
   private final class_310 mc = class_310.method_1551();

   public DrawingEllipseCacheDimensionHandler(final class_5321<class_1937> dimension, final DrawingDatabase database, final ListeningExecutorService dbExecutor) {
      this.dimension = dimension;
      this.database = database;
      this.dbExecutor = dbExecutor;
   }

   public void addEllipse(final Ellipse ellipse, final int color) {
      this.checkMainThread("addEllipse");
      this.ellipses.put(ellipse, color);
      this.staleEllipses.add(ellipse);
      this.writeStaleEllipsesToDatabase();
   }

   public void removeEllipse(final Ellipse ellipse) {
      this.checkMainThread("removeEllipse");
      if (this.ellipses.containsKey(ellipse)) {
         this.ellipses.removeInt(ellipse);
         this.staleEllipses.remove(ellipse);
         this.dbExecutor.execute(() -> this.database.removeEllipse(ellipse, this.dimension));
      }

   }

   public void removeAllEllipses() {
      this.checkMainThread("removeAllEllipses");
      this.ellipses.clear();
      this.staleEllipses.clear();
      this.dbExecutor.execute(() -> this.database.removeAllEllipses(this.dimension));
   }

   public Object2IntMap<Ellipse> getEllipses() {
      return this.ellipses;
   }

   public synchronized void setWindow(final int regionX, final int regionZ, final int regionSize) {
      boolean windowChanged = regionX != this.windowRegionX || regionZ != this.windowRegionZ || regionSize != this.windowRegionSize;
      if (!windowChanged || this.windowMoveFuture.isDone() || regionX == 0 && regionZ == 0 && regionSize == 0) {
         this.windowRegionX = regionX;
         this.windowRegionZ = regionZ;
         this.windowRegionSize = regionSize;
         if (windowChanged) {
            try {
               this.windowMoveFuture = this.moveWindow(regionX, regionZ, regionSize);
            } catch (Exception e) {
               XaeroPlus.LOGGER.error("Failed submitting ellipse window move task for {} disk cache dimension: {}", new Object[]{this.database.databaseName, this.dimension.method_29177(), e});
            }
         }

      } else {
         XaeroPlus.LOGGER.debug("Rejecting ellipse window move to: [{} {} {}] from: [{} {} {}]", new Object[]{regionX, regionZ, regionSize, this.windowRegionX, this.windowRegionZ, this.windowRegionSize});
      }
   }

   private ListenableFuture<?> moveWindow(final int regionX, final int regionZ, final int regionSize) {
      ListenableFuture<Object2IntMap<Ellipse>> loadDataFuture = this.dbExecutor.submit(() -> this.loadEllipsesFromDatabase(regionX, regionZ, regionSize));
      Futures.addCallback(loadDataFuture, new EllipseDataLoadFutureCallback(), this.mc);
      ListenableFuture<?> removeDataFuture = this.flushEllipsesOutsideWindow(regionX, regionZ, regionSize);
      return Futures.allAsList(new ListenableFuture[]{loadDataFuture, removeDataFuture});
   }

   private Object2IntMap<Ellipse> loadEllipsesFromDatabase(final int regionX, final int regionZ, final int regionSize) {
      Object2IntOpenHashMap<Ellipse> data = new Object2IntOpenHashMap();
      int minX = ChunkUtils.regionCoordToCoord(regionX - regionSize);
      int minZ = ChunkUtils.regionCoordToCoord(regionZ - regionSize);
      int maxX = ChunkUtils.regionCoordToCoord(regionX + regionSize);
      int maxZ = ChunkUtils.regionCoordToCoord(regionZ + regionSize);
      this.database.getEllipsesInDimension(this.dimension, (centerX, centerZ, radiusX, radiusZ, color) -> {
         Ellipse ellipse = new Ellipse(centerX, centerZ, radiusX, radiusZ);
         if (ellipse.intersects(minX, maxX, minZ, maxZ)) {
            data.put(ellipse, color);
         }

      });
      return data;
   }

   private ListenableFuture<?> flushEllipsesOutsideWindow(final int regionX, final int regionZ, final int regionSize) {
      this.checkMainThread("flushEllipsesOutsideWindow");
      Object2IntOpenHashMap<Ellipse> toWrite = new Object2IntOpenHashMap();
      int minX = ChunkUtils.regionCoordToCoord(regionX - regionSize);
      int minZ = ChunkUtils.regionCoordToCoord(regionZ - regionSize);
      int maxX = ChunkUtils.regionCoordToCoord(regionX + regionSize);
      int maxZ = ChunkUtils.regionCoordToCoord(regionZ + regionSize);
      ObjectIterator<Ellipse> iterator = this.ellipses.keySet().iterator();

      while(iterator.hasNext()) {
         Ellipse ellipse = (Ellipse)iterator.next();
         if (!ellipse.intersects(minX, maxX, minZ, maxZ)) {
            if (this.staleEllipses.remove(ellipse)) {
               toWrite.put(ellipse, this.ellipses.getInt(ellipse));
            }

            iterator.remove();
         }
      }

      return this.dbExecutor.submit(() -> this.database.insertEllipsesList(toWrite, this.dimension));
   }

   public ListenableFuture<?> writeStaleEllipsesToDatabase() {
      this.checkMainThread("writeStaleEllipsesToDatabase");
      Object2IntMap<Ellipse> toWrite = this.collectStaleEllipsesToWrite();
      return toWrite.isEmpty() ? Futures.immediateVoidFuture() : this.dbExecutor.submit(() -> this.database.insertEllipsesList(toWrite, this.dimension));
   }

   private Object2IntMap<Ellipse> collectStaleEllipsesToWrite() {
      if (this.staleEllipses.isEmpty()) {
         return Object2IntMaps.emptyMap();
      } else {
         Object2IntOpenHashMap<Ellipse> toWrite = new Object2IntOpenHashMap(this.staleEllipses.size());

         for(Iterator<Ellipse> iterator = this.staleEllipses.iterator(); iterator.hasNext(); iterator.remove()) {
            Ellipse ellipse = (Ellipse)iterator.next();
            if (this.ellipses.containsKey(ellipse)) {
               toWrite.put(ellipse, this.ellipses.getInt(ellipse));
            }
         }

         return toWrite;
      }
   }

   private void checkMainThread(final String operation) {
      if (!this.mc.method_18854()) {
         throw new RuntimeException(operation + " must be called on the main thread!");
      }
   }

   private final class EllipseDataLoadFutureCallback implements FutureCallback<Object2IntMap<Ellipse>> {
      public void onSuccess(final Object2IntMap<Ellipse> data) {
         if (!DrawingEllipseCacheDimensionHandler.this.mc.method_18854()) {
            XaeroPlus.LOGGER.error("EllipseDataLoadFutureCallback must be called on the main thread");
         }

         DrawingEllipseCacheDimensionHandler.this.ellipses.putAll(data);
      }

      public void onFailure(final Throwable throwable) {
         XaeroPlus.LOGGER.error("Error loading ellipses {} disk cache dimension: {}", new Object[]{DrawingEllipseCacheDimensionHandler.this.database.databaseName, DrawingEllipseCacheDimensionHandler.this.dimension.method_29177(), throwable});
      }
   }
}
