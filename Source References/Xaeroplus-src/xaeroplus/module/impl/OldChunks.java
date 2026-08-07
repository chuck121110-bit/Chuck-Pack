package xaeroplus.module.impl;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_1937;
import net.minecraft.class_1959;
import net.minecraft.class_1972;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2812;
import net.minecraft.class_2818;
import net.minecraft.class_5321;
import net.minecraft.class_6880;
import xaeroplus.Globals;
import xaeroplus.event.ChunkDataEvent;
import xaeroplus.feature.highlights.SavableHighlightCacheInstance;
import xaeroplus.feature.render.DrawFeatureFactory;
import xaeroplus.module.Module;
import xaeroplus.settings.Settings;
import xaeroplus.util.ChunkScanner;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.ColorHelper;

public class OldChunks extends Module {
   public final SavableHighlightCacheInstance oldChunksCache = new SavableHighlightCacheInstance("XaeroPlusOldChunks");
   public final SavableHighlightCacheInstance modernChunksCache = new SavableHighlightCacheInstance("XaeroPlusModernChunks");
   private int oldChunksColor = ColorHelper.getColor(0, 0, 255, 100);
   private boolean inverse = false;
   private static final ReferenceSet<class_2248> OVERWORLD_BLOCKS;
   private static final ReferenceSet<class_2248> NETHER_BLOCKS;

   public void setDiskCache(boolean disk) {
      this.oldChunksCache.setDiskCache(disk, this.isEnabled());
      this.modernChunksCache.setDiskCache(disk, this.isEnabled());
   }

   @EventHandler
   public void onChunkData(final ChunkDataEvent event) {
      if (!event.seenChunk()) {
         this.searchChunk(event.chunk());
      }
   }

   private void searchChunk(final class_2818 chunk) {
      class_5321<class_1937> dim = chunk.method_12200().method_27983();
      int x = chunk.method_12004().field_9181;
      int z = chunk.method_12004().field_9180;
      if (!this.modernChunksCache.get().isHighlighted(x, z, dim)) {
         if (!this.oldChunksCache.get().isHighlighted(x, z, dim)) {
            if (dim != class_1937.field_25179 && dim != class_1937.field_25180) {
               if (dim == class_1937.field_25181) {
                  class_6880<class_1959> biomeHolder = this.mc.field_1687.method_23753(new class_2338(ChunkUtils.chunkCoordToCoord(x) + 8, 64, ChunkUtils.chunkCoordToCoord(z) + 8));
                  if (biomeHolder.method_40225(class_1972.field_9411)) {
                     this.oldChunksCache.get().addHighlight(x, z, dim);
                  } else {
                     this.modernChunksCache.get().addHighlight(x, z, dim);
                  }
               }
            } else if (ChunkScanner.chunkContainsBlocks(chunk, dim == class_1937.field_25179 ? OVERWORLD_BLOCKS : NETHER_BLOCKS, 5)) {
               this.modernChunksCache.get().addHighlight(x, z, dim);
            } else {
               this.oldChunksCache.get().addHighlight(x, z, dim);
            }

         }
      }
   }

   public boolean inUnknownDimension(final class_5321<class_1937> dim) {
      return dim != class_1937.field_25179 && dim != class_1937.field_25180 && dim != class_1937.field_25181;
   }

   public void onEnable() {
      Globals.drawManager.registry().register(DrawFeatureFactory.chunkHighlights("OldChunks", this::getHighlightsState, this::getOldChunksColor, 250));
      this.oldChunksCache.onEnable();
      this.modernChunksCache.onEnable();
      this.searchAllLoadedChunks();
   }

   private void searchAllLoadedChunks() {
      if (this.mc.field_1687 != null && !this.inUnknownDimension(this.mc.field_1687.method_27983())) {
         int renderDist = (Integer)this.mc.field_1690.method_42503().method_41753();
         int xMin = ChunkUtils.actualPlayerChunkX() - renderDist;
         int xMax = ChunkUtils.actualPlayerChunkX() + renderDist;
         int zMin = ChunkUtils.actualPlayerChunkZ() - renderDist;
         int zMax = ChunkUtils.actualPlayerChunkZ() + renderDist;

         for(int x = xMin; x <= xMax; ++x) {
            for(int z = zMin; z <= zMax; ++z) {
               class_2818 chunk = this.mc.field_1687.method_2935().method_12126(x, z, false);
               if (!(chunk instanceof class_2812) && chunk != null) {
                  this.searchChunk(chunk);
               }
            }
         }

      }
   }

   public void onDisable() {
      this.oldChunksCache.onDisable();
      this.modernChunksCache.onDisable();
      Globals.drawManager.registry().unregister("OldChunks");
   }

   public int getOldChunksColor() {
      return this.oldChunksColor;
   }

   public void setRgbColor(final int color) {
      this.oldChunksColor = ColorHelper.getColorWithAlpha(color, Settings.REGISTRY.oldChunksAlphaSetting.getAsInt());
   }

   public void setAlpha(final double a) {
      this.oldChunksColor = ColorHelper.getColorWithAlpha(this.oldChunksColor, (int)a);
   }

   public boolean isHighlighted(final int chunkPosX, final int chunkPosZ, final class_5321<class_1937> dimensionId) {
      return this.inverse ? this.isOldChunkInverse(chunkPosX, chunkPosZ, dimensionId) : this.isOldChunk(chunkPosX, chunkPosZ, dimensionId);
   }

   public Long2LongMap getHighlightsState(final class_5321<class_1937> dimension) {
      return this.inverse ? this.modernChunksCache.get().getCacheMap(dimension) : this.oldChunksCache.get().getCacheMap(dimension);
   }

   public boolean isOldChunk(final int chunkPosX, final int chunkPosZ, final class_5321<class_1937> dimensionId) {
      return this.oldChunksCache.get().isHighlighted(chunkPosX, chunkPosZ, dimensionId);
   }

   public boolean isOldChunkInverse(final int chunkPosX, final int chunkPosZ, final class_5321<class_1937> dimensionId) {
      return this.modernChunksCache.get().isHighlighted(chunkPosX, chunkPosZ, dimensionId);
   }

   public void setInverse(final boolean b) {
      this.inverse = b;
   }

   static {
      OVERWORLD_BLOCKS = ReferenceOpenHashSet.of(new class_2248[]{class_2246.field_27120, class_2246.field_29221, class_2246.field_27159, class_2246.field_29032, class_2246.field_27165, class_2246.field_9993, class_2246.field_10463, class_2246.field_28048, class_2246.field_28049, class_2246.field_28888, class_2246.field_28678, class_2246.field_28682, class_2246.field_28683, class_2246.field_28684, class_2246.field_28681, class_2246.field_28675, class_2246.field_28676});
      NETHER_BLOCKS = ReferenceOpenHashSet.of(new class_2248[]{class_2246.field_22109, class_2246.field_23869, class_2246.field_22091, class_2246.field_22120, class_2246.field_22113, class_2246.field_23077, class_2246.field_23985});
   }
}
