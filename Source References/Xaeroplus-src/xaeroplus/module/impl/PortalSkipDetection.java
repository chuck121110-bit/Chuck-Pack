package xaeroplus.module.impl;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_1937;
import net.minecraft.class_310;
import net.minecraft.class_5321;
import xaero.map.MapProcessor;
import xaero.map.WorldMapSession;
import xaero.map.core.XaeroWorldMapCore;
import xaero.map.gui.GuiMap;
import xaero.map.region.MapRegion;
import xaero.map.region.MapTileChunk;
import xaeroplus.Globals;
import xaeroplus.XaeroPlus;
import xaeroplus.event.ClientTickEvent;
import xaeroplus.event.XaeroWorldChangeEvent;
import xaeroplus.feature.extensions.SeenChunksTrackingMapTileChunk;
import xaeroplus.feature.highlights.ChunkHighlightLocalCache;
import xaeroplus.feature.render.DrawFeatureFactory;
import xaeroplus.module.Module;
import xaeroplus.module.ModuleManager;
import xaeroplus.settings.Settings;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.ColorHelper;
import xaeroplus.util.GuiMapHelper;
import xaeroplus.util.timer.Timer;
import xaeroplus.util.timer.Timers;

public class PortalSkipDetection extends Module {
   private Future<?> portalSkipDetectionSearchFuture = null;
   private int portalSkipChunksColor = ColorHelper.getColor(255, 255, 255, 100);
   public final ChunkHighlightLocalCache cache = new ChunkHighlightLocalCache();
   private int windowRegionX = 0;
   private int windowRegionZ = 0;
   private int windowRegionSize = 0;
   private static final int defaultRegionWindowSize = 2;
   private boolean worldCacheInitialized = false;
   private int searchDelayTicks = 0;
   private int portalRadius = 15;
   private boolean oldChunksInverse = false;
   private boolean newChunks = false;
   private OldChunks oldChunksModule;
   private PaletteNewChunks newChunksModule;
   private final Timer tickTimer = Timers.tickTimer();
   private final LongOpenHashSet portalDetectionSearchChunksBuf = new LongOpenHashSet();
   private final Long2LongOpenHashMap portalAreaChunksBuf = new Long2LongOpenHashMap();
   private final LongOpenHashSet portalChunkTempSetBuf = new LongOpenHashSet();

   @EventHandler
   public void onClientTickEvent(final ClientTickEvent.Post event) {
      if (this.worldCacheInitialized && (this.portalSkipDetectionSearchFuture == null || this.portalSkipDetectionSearchFuture.isDone())) {
         if (this.tickTimer.tick((long)this.searchDelayTicks)) {
            Optional<GuiMap> guiMapOptional = GuiMapHelper.getGuiMap();
            if (guiMapOptional.isPresent()) {
               GuiMap guiMap = (GuiMap)guiMapOptional.get();
               int mapCenterX = GuiMapHelper.getGuiMapCenterRegionX(guiMap);
               int mapCenterZ = GuiMapHelper.getGuiMapCenterRegionZ(guiMap);
               int mapSize = GuiMapHelper.getGuiMapRegionSize(guiMap);
               this.setWindow(mapCenterX, mapCenterZ, mapSize);
            } else {
               this.setWindow(ChunkUtils.getPlayerRegionX(), ChunkUtils.getPlayerRegionZ(), 2);
            }
         }

      }
   }

   @EventHandler
   public void onXaeroWorldChangeEvent(final XaeroWorldChangeEvent event) {
      TickTaskExecutor.INSTANCE.execute(() -> {
         this.reset();
         this.initializeWorld();
      });
   }

   public void onEnable() {
      Globals.drawManager.registry().register(DrawFeatureFactory.chunkHighlights("PortalSkips", this::getHighlightsState, this::getPortalSkipChunksColor, 250));
      TickTaskExecutor.INSTANCE.execute(() -> {
         this.reset();
         this.initializeWorld();
      });
      this.newChunksModule = (PaletteNewChunks)ModuleManager.getModule(PaletteNewChunks.class);
      this.oldChunksModule = (OldChunks)ModuleManager.getModule(OldChunks.class);
   }

   public void onDisable() {
      TickTaskExecutor.INSTANCE.execute(() -> this.reset());
      Globals.drawManager.registry().unregister("PortalSkips");
   }

   private void initializeWorld() {
      try {
         String worldId = XaeroWorldMapCore.currentSession.getMapProcessor().getCurrentWorldId();
         if (worldId == null) {
            return;
         }

         this.worldCacheInitialized = true;
      } catch (Exception var2) {
      }

   }

   private void reset() {
      this.cache.reset();
      Future<?> future = this.portalSkipDetectionSearchFuture;
      if (future != null && !future.isDone()) {
         future.cancel(true);
      }

   }

   public void setWindow(int regionX, int regionZ, int regionSize) {
      this.windowRegionX = regionX;
      this.windowRegionZ = regionZ;
      this.windowRegionSize = regionSize;
      if (class_310.method_1551().field_1687 != null) {
         this.portalSkipDetectionSearchFuture = ((ExecutorService)Globals.moduleExecutorService.get()).submit(this::searchForPortalSkipChunks);
      }
   }

   private void searchForPortalSkipChunks() {
      try {
         int windowRegionX = this.windowRegionX;
         int windowRegionZ = this.windowRegionZ;
         int windowRegionSize = this.windowRegionSize;
         class_5321<class_1937> currentlyViewedDimension = Globals.getCurrentDimensionId();
         this.portalDetectionSearchChunksBuf.clear();

         for(int regionX = windowRegionX - windowRegionSize; regionX <= windowRegionX + windowRegionSize; ++regionX) {
            int baseChunkCoordX = ChunkUtils.regionCoordToChunkCoord(regionX);

            for(int regionZ = windowRegionZ - windowRegionSize; regionZ <= windowRegionZ + windowRegionSize; ++regionZ) {
               int baseChunkCoordZ = ChunkUtils.regionCoordToChunkCoord(regionZ);

               for(int chunkX = 0; chunkX < 32; ++chunkX) {
                  for(int chunkZ = 0; chunkZ < 32; ++chunkZ) {
                     int chunkPosX = baseChunkCoordX + chunkX;
                     int chunkPosZ = baseChunkCoordZ + chunkZ;
                     if (this.isChunkSeen(chunkPosX, chunkPosZ, currentlyViewedDimension) && !this.isNewishChunk(chunkPosX, chunkPosZ, currentlyViewedDimension)) {
                        this.portalDetectionSearchChunksBuf.add(ChunkUtils.chunkPosToLong(chunkPosX, chunkPosZ));
                     }
                  }
               }
            }
         }

         this.portalAreaChunksBuf.clear();
         LongIterator var14 = this.portalDetectionSearchChunksBuf.iterator();

         while(var14.hasNext()) {
            long chunkPos = (Long)var14.next();
            boolean allSeen = true;
            this.portalChunkTempSetBuf.clear();

            for(int xOffset = 0; xOffset < this.portalRadius; ++xOffset) {
               for(int zOffset = 0; zOffset < this.portalRadius; ++zOffset) {
                  long currentChunkPos = ChunkUtils.chunkPosToLong(ChunkUtils.longToChunkX(chunkPos) + xOffset, ChunkUtils.longToChunkZ(chunkPos) + zOffset);
                  this.portalChunkTempSetBuf.add(currentChunkPos);
                  if (!this.portalDetectionSearchChunksBuf.contains(currentChunkPos)) {
                     allSeen = false;
                     this.portalChunkTempSetBuf.clear();
                     break;
                  }
               }

               if (!allSeen) {
                  break;
               }
            }

            if (allSeen) {
               this.portalChunkTempSetBuf.forEach((c) -> this.portalAreaChunksBuf.put(c, 0L));
            }
         }

         this.cache.submitTickTask(() -> this.cache.replaceState(this.portalAreaChunksBuf));
      } catch (Exception e) {
         XaeroPlus.LOGGER.debug("Error searching for portal skip chunks", e);
      }

   }

   private boolean isNewishChunk(final int chunkPosX, final int chunkPosZ, final class_5321<class_1937> currentlyViewedDimension) {
      if (this.newChunks && this.oldChunksInverse) {
         return this.isNewChunk(chunkPosX, chunkPosZ, currentlyViewedDimension) || this.isOldChunksInverse(chunkPosX, chunkPosZ, currentlyViewedDimension);
      } else if (this.newChunks) {
         return this.isNewChunk(chunkPosX, chunkPosZ, currentlyViewedDimension);
      } else {
         return this.oldChunksInverse ? this.isOldChunksInverse(chunkPosX, chunkPosZ, currentlyViewedDimension) : false;
      }
   }

   private boolean isNewChunk(final int chunkPosX, final int chunkPosZ, final class_5321<class_1937> currentlyViewedDimension) {
      return Settings.REGISTRY.paletteNewChunksSaveLoadToDisk.get() && this.newChunksModule != null ? this.newChunksModule.isNewChunk(chunkPosX, chunkPosZ, currentlyViewedDimension) : false;
   }

   private boolean isOldChunksInverse(final int chunkPosX, final int chunkPosZ, final class_5321<class_1937> currentlyViewedDimension) {
      return Settings.REGISTRY.oldChunksEnabledSetting.get() && this.oldChunksModule != null ? this.oldChunksModule.isOldChunkInverse(chunkPosX, chunkPosZ, currentlyViewedDimension) : false;
   }

   private boolean isChunkSeen(final int chunkPosX, final int chunkPosZ, final class_5321<class_1937> currentlyViewedDimension) {
      WorldMapSession currentSession = XaeroWorldMapCore.currentSession;
      if (currentSession == null) {
         return false;
      } else {
         MapProcessor mapProcessor = currentSession.getMapProcessor();
         if (mapProcessor == null) {
            return false;
         } else {
            MapRegion mapRegion = mapProcessor.getLeafMapRegion(mapProcessor.getCurrentCaveLayer(), ChunkUtils.chunkCoordToMapRegionCoord(chunkPosX), ChunkUtils.chunkCoordToMapRegionCoord(chunkPosZ), false);
            if (mapRegion == null) {
               return false;
            } else {
               MapTileChunk mapChunk = mapRegion.getChunk(ChunkUtils.chunkCoordToMapTileChunkCoordLocal(chunkPosX), ChunkUtils.chunkCoordToMapTileChunkCoordLocal(chunkPosZ));
               return mapChunk == null ? false : ((SeenChunksTrackingMapTileChunk)mapChunk).getSeenTiles()[ChunkUtils.chunkCoordToMapTileCoordLocal(chunkPosX)][ChunkUtils.chunkCoordToMapTileCoordLocal(chunkPosZ)];
            }
         }
      }
   }

   public int getPortalSkipChunksColor() {
      return this.portalSkipChunksColor;
   }

   public void setRgbColor(final int color) {
      this.portalSkipChunksColor = ColorHelper.getColorWithAlpha(color, Settings.REGISTRY.portalSkipDetectionAlphaSetting.getAsInt());
   }

   public void setAlpha(final double a) {
      this.portalSkipChunksColor = ColorHelper.getColorWithAlpha(this.portalSkipChunksColor, (int)a);
   }

   public boolean isPortalSkipChunk(final int chunkPosX, final int chunkPosZ, final class_5321<class_1937> dimension) {
      return this.isPortalSkipChunk(ChunkUtils.chunkPosToLong(chunkPosX, chunkPosZ));
   }

   public Long2LongMap getHighlightsState(final class_5321<class_1937> dimension) {
      return this.cache.getCacheMap(dimension);
   }

   public boolean isPortalSkipChunk(final long chunkPos) {
      return this.cache.isHighlighted(chunkPos);
   }

   public void setSearchDelayTicks(final double delay) {
      this.searchDelayTicks = (int)delay;
   }

   public void setOldChunksInverse(final boolean b) {
      this.oldChunksInverse = b;
   }

   public void setNewChunks(final boolean b) {
      this.newChunks = b;
   }

   public void setPortalRadius(final double b) {
      this.portalRadius = (int)b;
   }
}
