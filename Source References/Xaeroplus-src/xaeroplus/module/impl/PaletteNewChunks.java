package xaeroplus.module.impl;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_1937;
import net.minecraft.class_1959;
import net.minecraft.class_1972;
import net.minecraft.class_2248;
import net.minecraft.class_2680;
import net.minecraft.class_2814;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_2834;
import net.minecraft.class_2837;
import net.minecraft.class_2841;
import net.minecraft.class_5321;
import net.minecraft.class_6490;
import net.minecraft.class_6880;
import net.minecraft.class_7522;
import xaeroplus.Globals;
import xaeroplus.XaeroPlus;
import xaeroplus.event.ChunkDataEvent;
import xaeroplus.feature.highlights.SavableHighlightCacheInstance;
import xaeroplus.feature.render.DrawFeatureFactory;
import xaeroplus.module.Module;
import xaeroplus.settings.Settings;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.ColorHelper;

public class PaletteNewChunks extends Module {
   public final SavableHighlightCacheInstance newChunksCache = new SavableHighlightCacheInstance("XaeroPlusPaletteNewChunks");
   public final SavableHighlightCacheInstance newChunksInverseCache = new SavableHighlightCacheInstance("XaeroPlusPaletteNewChunksInverse");
   private int newChunksColor = ColorHelper.getColor(255, 0, 0, 100);
   private final IntSet presentStateIdsBuf = new IntOpenHashSet();
   private final IntList presentStateIdsOrderedBuf = new IntArrayList();
   private boolean renderInverse = false;
   private Duration minRescanAge = Duration.ofDays(7L);
   private boolean rescan = false;

   public void setDiskCache(final boolean disk) {
      this.newChunksCache.setDiskCache(disk, this.isEnabled());
      this.newChunksInverseCache.setDiskCache(disk, this.isEnabled());
   }

   @EventHandler
   public void onChunkData(ChunkDataEvent event) {
      if (!event.seenChunk()) {
         class_2818 chunk = event.chunk();
         class_5321<class_1937> dim = chunk.method_12200().method_27983();
         int x = chunk.method_12004().field_9181;
         int z = chunk.method_12004().field_9180;

         try {
            if (this.shouldSkipScan(x, z, dim, this.newChunksCache)) {
               return;
            }

            if (this.shouldSkipScan(x, z, dim, this.newChunksInverseCache)) {
               return;
            }

            if (this.scanIsNewChunk(dim, chunk)) {
               this.newChunksCache.get().addHighlight(x, z, dim);
            } else {
               this.newChunksInverseCache.get().addHighlight(x, z, dim);
            }
         } catch (Exception e) {
            XaeroPlus.LOGGER.error("Error checking palette NewChunk at [{} {}]", new Object[]{x, z, e});
         }

      }
   }

   private boolean shouldSkipScan(final int x, final int z, final class_5321<class_1937> dim, final SavableHighlightCacheInstance cache) {
      if (cache.get().isHighlighted(x, z, dim)) {
         if (!this.rescan) {
            return true;
         }

         Long2LongMap chunks = cache.get().getCacheMap(dim);
         long foundTime = chunks.get(ChunkUtils.chunkPosToLong(x, z));
         Duration age = Duration.ofMillis(Math.abs(System.currentTimeMillis() - foundTime));
         if (age.compareTo(this.minRescanAge) < 0) {
            return true;
         }

         cache.get().removeHighlight(x, z, dim);
      }

      return false;
   }

   public boolean scanIsNewChunk(final class_5321<class_1937> dim, final class_2818 chunk) {
      if (dim == class_1937.field_25179) {
         boolean var10000;
         if (Settings.REGISTRY.paletteNewChunksVersionUpgradedChunks.get()) {
            var10000 = this.scanNewChunkBlockStatePalette(chunk);
         } else {
            switch (this.scanNewChunkBiomePalette(chunk, true).ordinal()) {
               case 0 -> var10000 = false;
               case 1 -> var10000 = true;
               case 2 -> var10000 = this.scanNewChunkBlockStatePalette(chunk);
               default -> throw new MatchException((String)null, (Throwable)null);
            }
         }

         return var10000;
      } else if (dim == class_1937.field_25180) {
         return Settings.REGISTRY.paletteNewChunksVersionUpgradedChunks.get() ? this.scanNewChunkBlockStatePalette(chunk) : this.scanNewChunkBiomePalette(chunk, false) == PaletteNewChunks.BiomeCheckResult.PLAINS_IN_PALETTE;
      } else if (dim == class_1937.field_25181) {
         return this.scanNewChunkBiomePalette(chunk, false) == PaletteNewChunks.BiomeCheckResult.PLAINS_IN_PALETTE;
      } else {
         return false;
      }
   }

   public boolean scanNewChunkBlockStatePalette(class_2818 chunk) {
      class_2826[] sections = chunk.method_12006();
      if (sections.length == 0) {
         return false;
      } else {
         int threshold = 3;
         int positiveCount = 0;

         for(int i = 0; i < sections.length; ++i) {
            class_2826 section = sections[i];
            class_2841.class_6561<class_2680> paletteContainerData = section.method_12265().field_34560;
            class_2837<class_2680> palette = paletteContainerData.comp_119();
            if (palette instanceof class_2834) {
               if (this.scanLinearPaletteOrder(palette, section)) {
                  ++positiveCount;
               }
            } else if (palette instanceof class_2814 && this.checkForExtraPaletteEntries(paletteContainerData)) {
               ++positiveCount;
            }

            if (positiveCount >= threshold) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean scanLinearPaletteOrder(final class_2837<class_2680> palette, final class_2826 section) {
      this.presentStateIdsOrderedBuf.clear();

      for(int id = 0; id < palette.method_12197(); ++id) {
         int blockStateId = class_2248.method_9507((class_2680)palette.method_12288(id));
         this.presentStateIdsOrderedBuf.add(blockStateId);
      }

      this.presentStateIdsBuf.clear();
      AtomicInteger searchIndex = new AtomicInteger(0);
      AtomicBoolean isNewChunk = new AtomicBoolean(false);
      section.method_12265().field_34560.comp_118().method_21739((dataId) -> {
         if (!isNewChunk.get()) {
            if (searchIndex.get() != this.presentStateIdsOrderedBuf.size()) {
               int blockStateId = class_2248.method_9507((class_2680)palette.method_12288(dataId));
               if (!this.presentStateIdsBuf.contains(blockStateId)) {
                  int nextExpectedId = this.presentStateIdsOrderedBuf.getInt(searchIndex.get());
                  if (blockStateId == nextExpectedId) {
                     this.presentStateIdsBuf.add(blockStateId);
                     searchIndex.incrementAndGet();
                  } else {
                     isNewChunk.set(true);
                  }

               }
            }
         }
      });
      return isNewChunk.get();
   }

   public synchronized BiomeCheckResult scanNewChunkBiomePalette(class_2818 chunk, boolean checkData) {
      class_2826[] sections = chunk.method_12006();
      if (sections.length == 0) {
         return PaletteNewChunks.BiomeCheckResult.NO_PLAINS;
      } else {
         class_2826 firstSection = sections[0];
         class_7522<class_6880<class_1959>> biomes = firstSection.method_38294();
         if (biomes instanceof class_2841) {
            class_2841<class_6880<class_1959>> biomesPaletteContainer = (class_2841)biomes;
            class_2837<class_6880<class_1959>> palette = biomesPaletteContainer.field_34560.comp_119();
            boolean paletteContainsPlains = palette.method_19525(PaletteNewChunks::isPlainsBiome);
            if (paletteContainsPlains && checkData) {
               if (palette.method_12197() == 1) {
                  return PaletteNewChunks.BiomeCheckResult.PLAINS_PRESENT;
               }

               class_6490 storage = biomesPaletteContainer.field_34560.comp_118();
               this.presentStateIdsBuf.clear();
               IntSet var10001 = this.presentStateIdsBuf;
               Objects.requireNonNull(var10001);
               storage.method_21739(var10001::add);
               IntIterator var10 = this.presentStateIdsBuf.iterator();

               while(var10.hasNext()) {
                  int id = (Integer)var10.next();
                  if (isPlainsBiome((class_6880)palette.method_12288(id))) {
                     return PaletteNewChunks.BiomeCheckResult.PLAINS_PRESENT;
                  }
               }
            }

            if (paletteContainsPlains) {
               return PaletteNewChunks.BiomeCheckResult.PLAINS_IN_PALETTE;
            }
         }

         return PaletteNewChunks.BiomeCheckResult.NO_PLAINS;
      }
   }

   public synchronized boolean checkForExtraPaletteEntries(class_2841.class_6561<class_2680> paletteContainer) {
      this.presentStateIdsBuf.clear();
      class_2837<class_2680> palette = paletteContainer.comp_119();
      class_6490 storage = paletteContainer.comp_118();
      IntSet var10001 = this.presentStateIdsBuf;
      Objects.requireNonNull(var10001);
      storage.method_21739(var10001::add);
      return palette.method_12197() > this.presentStateIdsBuf.size();
   }

   private static boolean isPlainsBiome(class_6880<class_1959> holder) {
      return holder.method_40225(class_1972.field_9451);
   }

   public void onEnable() {
      Globals.drawManager.registry().register(DrawFeatureFactory.chunkHighlights("PaletteNewChunks", this::getHighlightsState, this::getNewChunksColor, 250));
      this.newChunksCache.onEnable();
      this.newChunksInverseCache.onEnable();
   }

   public void onDisable() {
      this.newChunksCache.onDisable();
      this.newChunksInverseCache.onDisable();
      Globals.drawManager.registry().unregister("PaletteNewChunks");
   }

   public int getNewChunksColor() {
      return this.newChunksColor;
   }

   public void setRgbColor(final int color) {
      this.newChunksColor = ColorHelper.getColorWithAlpha(color, Settings.REGISTRY.paletteNewChunksAlphaSetting.getAsInt());
   }

   public void setAlpha(final double a) {
      this.newChunksColor = ColorHelper.getColorWithAlpha(this.newChunksColor, (int)a);
   }

   public void setInverse(final boolean b) {
      this.renderInverse = b;
   }

   public void setRescan(final boolean b) {
      this.rescan = b;
   }

   public void setMinRescanAge(final Duration duration) {
      this.minRescanAge = duration;
   }

   public boolean isHighlighted(final int chunkPosX, final int chunkPosZ, final class_5321<class_1937> dimensionId) {
      return this.renderInverse ? this.isInverseNewChunk(chunkPosX, chunkPosZ, dimensionId) : this.isNewChunk(chunkPosX, chunkPosZ, dimensionId);
   }

   public Long2LongMap getHighlightsState(final class_5321<class_1937> dimension) {
      return this.renderInverse ? this.newChunksInverseCache.get().getCacheMap(dimension) : this.newChunksCache.get().getCacheMap(dimension);
   }

   public boolean isNewChunk(final int chunkPosX, final int chunkPosZ, final class_5321<class_1937> dimensionId) {
      return this.newChunksCache.get().isHighlighted(chunkPosX, chunkPosZ, dimensionId);
   }

   public boolean isInverseNewChunk(final int chunkPosX, final int chunkPosZ, final class_5321<class_1937> dimensionId) {
      return this.newChunksInverseCache.get().isHighlighted(chunkPosX, chunkPosZ, dimensionId);
   }

   public static enum BiomeCheckResult {
      NO_PLAINS,
      PLAINS_IN_PALETTE,
      PLAINS_PRESENT;

      // $FF: synthetic method
      private static BiomeCheckResult[] $values() {
         return new BiomeCheckResult[]{NO_PLAINS, PLAINS_IN_PALETTE, PLAINS_PRESENT};
      }
   }
}
