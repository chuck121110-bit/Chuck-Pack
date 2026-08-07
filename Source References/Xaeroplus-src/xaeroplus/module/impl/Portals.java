package xaeroplus.module.impl;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2334;
import net.minecraft.class_2338;
import net.minecraft.class_2423;
import net.minecraft.class_2680;
import net.minecraft.class_2812;
import net.minecraft.class_2818;
import net.minecraft.class_5321;
import net.minecraft.class_638;
import xaeroplus.Globals;
import xaeroplus.event.ChunkBlockUpdateEvent;
import xaeroplus.event.ChunkBlocksUpdateEvent;
import xaeroplus.event.ChunkDataEvent;
import xaeroplus.feature.highlights.SavableHighlightCacheInstance;
import xaeroplus.feature.render.DrawFeatureFactory;
import xaeroplus.module.Module;
import xaeroplus.settings.Settings;
import xaeroplus.util.ChunkScanner;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.ColorHelper;

public class Portals extends Module {
   public final SavableHighlightCacheInstance portalsCache = new SavableHighlightCacheInstance("XaeroPlusPortals");
   private int portalsColor = ColorHelper.getColor(0, 255, 0, 100);
   private static final ReferenceSet<class_2248> PORTAL_BLOCKS;

   public void setDiskCache(final boolean disk) {
      this.portalsCache.setDiskCache(disk, this.isEnabled());
   }

   public void onEnable() {
      Globals.drawManager.registry().register(DrawFeatureFactory.chunkHighlights("Portals", this::getHighlightsState, this::getPortalsColor, 250));
      this.portalsCache.onEnable();
      this.searchAllLoadedChunks();
   }

   public void onDisable() {
      this.portalsCache.onDisable();
      Globals.drawManager.registry().unregister("Portals");
   }

   @EventHandler
   public void onChunkData(final ChunkDataEvent event) {
      this.findPortalInChunk(event.chunk());
   }

   @EventHandler
   public void onMultiBlockUpdate(final ChunkBlocksUpdateEvent event) {
      switch (event.phase()) {
         case PRE -> event.packet().method_30621(this::handleBlockChange);
         case POST -> this.handleMultiBlockChangePost(event);
      }

   }

   private void handleMultiBlockChangePost(final ChunkBlocksUpdateEvent event) {
      class_638 level = this.mc.field_1687;
      if (level != null) {
         class_5321<class_1937> dim = level.method_27983();
         event.packet().method_30621((blockPos, blockState) -> {
            if (blockState.method_26215()) {
               int chunkX = ChunkUtils.posToChunkPos(blockPos.method_10263());
               int chunkZ = ChunkUtils.posToChunkPos(blockPos.method_10260());
               if (this.portalsCache.get().isHighlighted(chunkX, chunkZ, dim)) {
                  class_2818 chunk = level.method_2935().method_12126(chunkX, chunkZ, false);
                  if (!(chunk instanceof class_2812) && chunk != null) {
                     this.findPortalInChunk(chunk);
                  }
               }
            }
         });
      }
   }

   @EventHandler
   public void onBlockUpdate(final ChunkBlockUpdateEvent event) {
      switch (event.phase()) {
         case PRE -> this.handleBlockChange(event.packet().method_11309(), event.packet().method_11308());
         case POST -> this.handleBlockChangePost(event);
      }

   }

   private void handleBlockChangePost(final ChunkBlockUpdateEvent event) {
      class_638 level = this.mc.field_1687;
      if (level != null) {
         class_5321<class_1937> dim = level.method_27983();
         class_2338 blockPos = event.packet().method_11309();
         class_2680 blockState = event.packet().method_11308();
         if (blockState.method_26215()) {
            int chunkX = ChunkUtils.posToChunkPos(blockPos.method_10263());
            int chunkZ = ChunkUtils.posToChunkPos(blockPos.method_10260());
            if (this.portalsCache.get().isHighlighted(chunkX, chunkZ, dim)) {
               class_2818 chunk = level.method_2935().method_12126(chunkX, chunkZ, false);
               if (!(chunk instanceof class_2812) && chunk != null) {
                  this.findPortalInChunk(chunk);
               }
            }
         }
      }
   }

   private void findPortalInChunk(final class_2818 chunk) {
      class_5321<class_1937> dim = chunk.method_12200().method_27983();
      boolean chunkHadPortal = this.portalsCache.get().isHighlighted(chunk.method_12004().field_9181, chunk.method_12004().field_9180, dim);
      boolean hasPortal = ChunkScanner.chunkContainsBlocks(chunk, PORTAL_BLOCKS, this.mc.field_1687.method_31607());
      if (hasPortal) {
         this.portalsCache.get().addHighlight(chunk.method_12004().field_9181, chunk.method_12004().field_9180, dim);
      } else if (chunkHadPortal) {
         this.portalsCache.get().removeHighlight(chunk.method_12004().field_9181, chunk.method_12004().field_9180, dim);
      }

   }

   private void searchAllLoadedChunks() {
      if (this.mc.field_1687 != null) {
         int renderDist = (Integer)this.mc.field_1690.method_42503().method_41753();
         int xMin = ChunkUtils.actualPlayerChunkX() - renderDist;
         int xMax = ChunkUtils.actualPlayerChunkX() + renderDist;
         int zMin = ChunkUtils.actualPlayerChunkZ() - renderDist;
         int zMax = ChunkUtils.actualPlayerChunkZ() + renderDist;

         for(int x = xMin; x <= xMax; ++x) {
            for(int z = zMin; z <= zMax; ++z) {
               class_2818 chunk = this.mc.field_1687.method_2935().method_12126(x, z, false);
               if (!(chunk instanceof class_2812) && chunk != null) {
                  this.findPortalInChunk(chunk);
               }
            }
         }

      }
   }

   private void handleBlockChange(final class_2338 pos, final class_2680 state) {
      class_5321<class_1937> dim = ChunkUtils.getActualDimension();
      int chunkX = ChunkUtils.posToChunkPos(pos.method_10263());
      int chunkZ = ChunkUtils.posToChunkPos(pos.method_10260());
      if (state.method_26204() instanceof class_2423 || state.method_26204() instanceof class_2334) {
         this.portalsCache.get().addHighlight(chunkX, chunkZ, dim);
      }
   }

   public int getPortalsColor() {
      return this.portalsColor;
   }

   public void setRgbColor(final int color) {
      this.portalsColor = ColorHelper.getColorWithAlpha(color, Settings.REGISTRY.portalsAlphaSetting.getAsInt());
   }

   public void setAlpha(final double a) {
      this.portalsColor = ColorHelper.getColorWithAlpha(this.portalsColor, (int)a);
   }

   public boolean isPortalChunk(final int chunkPosX, final int chunkPosZ, final class_5321<class_1937> dimensionId) {
      return this.portalsCache.get().isHighlighted(chunkPosX, chunkPosZ, dimensionId);
   }

   public Long2LongMap getHighlightsState(final class_5321<class_1937> dimension) {
      return this.portalsCache.get().getCacheMap(dimension);
   }

   static {
      PORTAL_BLOCKS = ReferenceOpenHashSet.of(new class_2248[]{class_2246.field_10027, class_2246.field_10613, class_2246.field_10316, class_2246.field_10398});
   }
}
