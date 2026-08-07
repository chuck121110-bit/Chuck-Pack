package xaeroplus.feature.extensions;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Hashtable;
import xaero.map.file.RegionDetection;
import xaero.map.highlight.RegionHighlightExistenceTracker;
import xaero.map.region.MapLayer;
import xaero.map.util.linked.LinkedChain;
import xaero.map.world.MapDimension;
import xaeroplus.util.ChunkUtils;

public class OptimizedMapLayer extends MapLayer {
   private final MapDimension mapDimension;
   private final Long2ObjectOpenHashMap<RegionDetection> detectedRegions0;
   private final Long2ObjectOpenHashMap<RegionDetection> completeDetectedRegions0;
   private final LinkedChain<RegionDetection> completeDetectedRegionsLinked;

   public OptimizedMapLayer(MapDimension mapDimension, RegionHighlightExistenceTracker regionHighlightExistenceTracker) {
      super(mapDimension, regionHighlightExistenceTracker);
      this.mapDimension = mapDimension;
      this.detectedRegions0 = new Long2ObjectOpenHashMap();
      this.completeDetectedRegions0 = new Long2ObjectOpenHashMap();
      this.completeDetectedRegionsLinked = new LinkedChain();
   }

   public void addRegionDetection(RegionDetection regionDetection) {
      synchronized(this.detectedRegions0) {
         long packedPos = ChunkUtils.chunkPosToLong(regionDetection.getRegionX(), regionDetection.getRegionZ());
         this.detectedRegions0.put(packedPos, regionDetection);
         this.tryAddingToCompleteRegionDetection(regionDetection);
      }
   }

   public RegionDetection getCompleteRegionDetection(int x, int z) {
      if (this.mapDimension.isUsingWorldSave()) {
         return this.mapDimension.getWorldSaveRegionDetection(x, z);
      } else {
         synchronized(this.completeDetectedRegions0) {
            long packedPos = ChunkUtils.chunkPosToLong(x, z);
            return (RegionDetection)this.completeDetectedRegions0.get(packedPos);
         }
      }
   }

   private boolean completeRegionDetectionContains(RegionDetection regionDetection) {
      return this.getCompleteRegionDetection(regionDetection.getRegionX(), regionDetection.getRegionZ()) != null;
   }

   public void tryAddingToCompleteRegionDetection(RegionDetection regionDetection) {
      if (!this.completeRegionDetectionContains(regionDetection)) {
         if (this.mapDimension.isUsingWorldSave()) {
            this.mapDimension.addWorldSaveRegionDetection(regionDetection);
         } else {
            synchronized(this.completeDetectedRegions0) {
               long packedPos = ChunkUtils.chunkPosToLong(regionDetection.getRegionX(), regionDetection.getRegionZ());
               this.completeDetectedRegions0.put(packedPos, regionDetection);
               this.completeDetectedRegionsLinked.add(regionDetection);
            }
         }
      }

   }

   public RegionDetection getRegionDetection(int x, int z) {
      RegionDetection result = null;
      synchronized(this.detectedRegions0) {
         long packedPos = ChunkUtils.chunkPosToLong(x, z);
         result = (RegionDetection)this.detectedRegions0.get(packedPos);
      }

      if (result == null) {
         RegionDetection worldSaveDetection = this.mapDimension.getWorldSaveRegionDetection(x, z);
         if (worldSaveDetection != null) {
            result = new RegionDetection(worldSaveDetection.getWorldId(), worldSaveDetection.getDimId(), worldSaveDetection.getMwId(), worldSaveDetection.getRegionX(), worldSaveDetection.getRegionZ(), worldSaveDetection.getRegionFile(), worldSaveDetection.getInitialVersion(), worldSaveDetection.isHasHadTerrain());
            this.addRegionDetection(result);
            return result;
         }
      } else if (result.isRemoved()) {
         return null;
      }

      return result;
   }

   public void removeRegionDetection(int x, int z) {
      if (this.mapDimension.getWorldSaveRegionDetection(x, z) != null) {
         RegionDetection regionDetection = this.getRegionDetection(x, z);
         if (regionDetection != null) {
            regionDetection.setRemoved(true);
         }
      } else {
         synchronized(this.detectedRegions0) {
            long packedPos = ChunkUtils.chunkPosToLong(x, z);
            this.detectedRegions0.remove(packedPos);
         }
      }

   }

   public Hashtable<Integer, Hashtable<Integer, RegionDetection>> getDetectedRegions() {
      Hashtable<Integer, Hashtable<Integer, RegionDetection>> resultTable = new Hashtable();
      synchronized(this.detectedRegions0) {
         int z;
         RegionDetection regionDetection;
         Hashtable<Integer, RegionDetection> column;
         for(ObjectIterator var3 = Long2ObjectMaps.fastIterable(this.detectedRegions0).iterator(); var3.hasNext(); column.put(z, regionDetection)) {
            Long2ObjectMap.Entry<RegionDetection> entry = (Long2ObjectMap.Entry)var3.next();
            long packedPos = entry.getLongKey();
            int x = ChunkUtils.longToChunkX(packedPos);
            z = ChunkUtils.longToChunkZ(packedPos);
            regionDetection = (RegionDetection)entry.getValue();
            column = (Hashtable)resultTable.get(x);
            if (column == null) {
               column = new Hashtable();
               resultTable.put(x, column);
            }
         }

         return resultTable;
      }
   }

   public Iterable<RegionDetection> getLinkedCompleteWorldSaveDetectedRegions() {
      return (Iterable<RegionDetection>)(this.mapDimension.isUsingWorldSave() ? this.mapDimension.getLinkedWorldSaveDetectedRegions() : this.completeDetectedRegionsLinked);
   }

   public void preDetection() {
      this.detectedRegions0.clear();
      this.completeDetectedRegions0.clear();
      this.completeDetectedRegionsLinked.reset();
   }
}
