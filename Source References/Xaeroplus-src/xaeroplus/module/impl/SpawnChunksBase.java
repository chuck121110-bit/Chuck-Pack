package xaeroplus.module.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import net.minecraft.class_638;
import xaeroplus.Globals;
import xaeroplus.feature.render.DrawFeatureFactory;
import xaeroplus.feature.render.line.Line;
import xaeroplus.module.Module;
import xaeroplus.settings.Settings;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.ColorHelper;

public abstract class SpawnChunksBase extends Module {
   final String entityProcessingId = this.getClass().getSimpleName() + "-EntityProcessing";
   final String redstoneProcessingId = this.getClass().getSimpleName() + "-RedstoneProcessing";
   final String lazyChunkId = this.getClass().getSimpleName() + "-LazyChunk";
   final String outerChunksId = this.getClass().getSimpleName() + "-OuterChunks";
   final List<Line> entityProcessingCache = new ArrayList();
   final List<Line> redstoneProcessingCache = new ArrayList();
   final List<Line> lazyChunksCache = new ArrayList();
   final List<Line> outerChunksCache = new ArrayList();
   int alpha = 204;
   int entityProcessingColor;
   int redstoneProcessingColor;
   int lazyChunksColor;
   int outerChunksColor;
   float lineWidth;

   public SpawnChunksBase() {
      this.entityProcessingColor = ColorHelper.getColor(0, 255, 0, this.alpha);
      this.redstoneProcessingColor = ColorHelper.getColor(255, 0, 0, this.alpha);
      this.lazyChunksColor = ColorHelper.getColor(0, 0, 255, this.alpha);
      this.outerChunksColor = ColorHelper.getColor(255, 255, 0, this.alpha);
      this.lineWidth = 0.1F;
   }

   public abstract class_5321<class_1937> dimension();

   abstract int getSpawnRadius();

   abstract long getSpawnChunkPos();

   void onClientTick() {
      this.updateCaches();
   }

   public void onEnable() {
      Globals.drawManager.registry().register(DrawFeatureFactory.lines(this.entityProcessingId, this::entityProcessing, this::entityProcessingColor, this::getLineWidth, 50));
      Globals.drawManager.registry().register(DrawFeatureFactory.lines(this.redstoneProcessingId, this::redstoneProcessing, this::redstoneProcessingColor, this::getLineWidth, 50));
      Globals.drawManager.registry().register(DrawFeatureFactory.lines(this.lazyChunkId, this::lazyChunks, this::lazyChunksColor, this::getLineWidth, 50));
      Globals.drawManager.registry().register(DrawFeatureFactory.lines(this.outerChunksId, this::outerChunks, this::outerChunksColor, this::getLineWidth, 50));
   }

   public void onDisable() {
      Globals.drawManager.registry().unregister(this.entityProcessingId);
      Globals.drawManager.registry().unregister(this.redstoneProcessingId);
      Globals.drawManager.registry().unregister(this.lazyChunkId);
      Globals.drawManager.registry().unregister(this.outerChunksId);
   }

   public List<Line> entityProcessing(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, class_5321<class_1937> dimension) {
      return dimension != this.dimension() ? Collections.emptyList() : this.entityProcessingCache;
   }

   public List<Line> redstoneProcessing(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, class_5321<class_1937> dimension) {
      return dimension != this.dimension() ? Collections.emptyList() : this.redstoneProcessingCache;
   }

   public List<Line> lazyChunks(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, class_5321<class_1937> dimension) {
      return dimension != this.dimension() ? Collections.emptyList() : this.lazyChunksCache;
   }

   public List<Line> outerChunks(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, class_5321<class_1937> dimension) {
      return dimension != this.dimension() ? Collections.emptyList() : this.outerChunksCache;
   }

   int entityProcessingColor() {
      return this.entityProcessingColor;
   }

   int redstoneProcessingColor() {
      return this.redstoneProcessingColor;
   }

   int lazyChunksColor() {
      return this.lazyChunksColor;
   }

   int outerChunksColor() {
      return this.outerChunksColor;
   }

   public void setEntityProcessingColor(final int color) {
      this.entityProcessingColor = ColorHelper.getColorWithAlpha(color, this.alpha);
   }

   public void setRedstoneProcessingColor(final int color) {
      this.redstoneProcessingColor = ColorHelper.getColorWithAlpha(color, this.alpha);
   }

   public void setLazyChunksColor(final int color) {
      this.lazyChunksColor = ColorHelper.getColorWithAlpha(color, this.alpha);
   }

   public void setOuterChunksColor(final int color) {
      this.outerChunksColor = ColorHelper.getColorWithAlpha(color, this.alpha);
   }

   public void setAlpha(final int alpha) {
      this.alpha = alpha;
      this.setEntityProcessingColor(this.entityProcessingColor);
      this.setRedstoneProcessingColor(this.redstoneProcessingColor);
      this.setLazyChunksColor(this.lazyChunksColor);
      this.setOuterChunksColor(this.outerChunksColor);
   }

   public float getLineWidth() {
      return this.lineWidth;
   }

   public void setLineWidth(final float lineWidth) {
      this.lineWidth = lineWidth;
   }

   void updateCaches() {
      int spawnChunkRadius = this.getSpawnRadius();
      this.clearCaches();
      class_638 level = this.mc.field_1687;
      if (level != null) {
         long spawnChunkPosLong = this.getSpawnChunkPos();
         int spawnChunkX = ChunkUtils.longToChunkX(spawnChunkPosLong);
         int spawnChunkZ = ChunkUtils.longToChunkZ(spawnChunkPosLong);
         int lazyRadius = spawnChunkRadius + 1;
         int entityProcessingRadius = spawnChunkRadius - 1;
         int worldGenRadius = lazyRadius + 11;
         this.populateCache(this.entityProcessingCache, spawnChunkX, spawnChunkZ, entityProcessingRadius);
         if (Settings.REGISTRY.spawnChunksRedstoneProcessingEnabled.get()) {
            this.populateCache(this.redstoneProcessingCache, spawnChunkX, spawnChunkZ, spawnChunkRadius);
         }

         this.populateCache(this.lazyChunksCache, spawnChunkX, spawnChunkZ, lazyRadius);
         if (Settings.REGISTRY.spawnChunksOuterChunksEnabled.get()) {
            this.populateCache(this.outerChunksCache, spawnChunkX, spawnChunkZ, worldGenRadius);
         }

      }
   }

   void populateCache(List<Line> cache, int centerX, int centerZ, int radius) {
      int minChunkX = centerX - radius;
      int maxChunkX = centerX + radius;
      int minChunkZ = centerZ - radius;
      int maxChunkZ = centerZ + radius;
      int minBlockX = ChunkUtils.chunkCoordToCoord(minChunkX);
      int maxBlockX = ChunkUtils.chunkCoordToCoord(maxChunkX + 1);
      int minBlockZ = ChunkUtils.chunkCoordToCoord(minChunkZ);
      int maxBlockZ = ChunkUtils.chunkCoordToCoord(maxChunkZ + 1);
      cache.add(new Line(minBlockX, minBlockZ, maxBlockX, minBlockZ));
      cache.add(new Line(minBlockX, minBlockZ, minBlockX, maxBlockZ));
      cache.add(new Line(maxBlockX, minBlockZ, maxBlockX, maxBlockZ));
      cache.add(new Line(minBlockX, maxBlockZ, maxBlockX, maxBlockZ));
   }

   void clearCaches() {
      this.entityProcessingCache.clear();
      this.redstoneProcessingCache.clear();
      this.lazyChunksCache.clear();
      this.outerChunksCache.clear();
   }
}
