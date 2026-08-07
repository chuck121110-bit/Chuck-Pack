package xaeroplus.module.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import java.time.Duration;
import java.util.concurrent.Executor;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_3610;
import net.minecraft.class_5321;
import net.minecraft.class_638;
import xaeroplus.Globals;
import xaeroplus.event.ChunkBlockUpdateEvent;
import xaeroplus.event.ChunkBlocksUpdateEvent;
import xaeroplus.event.ChunkDataEvent;
import xaeroplus.event.Phase;
import xaeroplus.event.XaeroWorldChangeEvent;
import xaeroplus.feature.highlights.SavableHighlightCacheInstance;
import xaeroplus.feature.render.DrawFeatureFactory;
import xaeroplus.module.Module;
import xaeroplus.settings.Settings;
import xaeroplus.util.ChunkScanner;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.ColorHelper;

public class LiquidNewChunks extends Module {
   public final SavableHighlightCacheInstance newChunksCache = new SavableHighlightCacheInstance("XaeroPlusNewChunks");
   public final SavableHighlightCacheInstance inverseNewChunksCache = new SavableHighlightCacheInstance("XaeroPlusNewChunksLiquidInverse");
   private final Cache<Long, Byte> seenChunksCache;
   private boolean renderInverse;
   private int newChunksColor;
   private int inverseColor;
   private static final class_2350[] searchDirs;
   private static final ReferenceSet<class_2248> liquidBlockTypeFilter;
   private static final String inverseDrawFeatureId = "LiquidNewChunksInverse";

   public LiquidNewChunks() {
      this.seenChunksCache = Caffeine.newBuilder().maximumSize(1000L).executor((Executor)Globals.cacheRefreshExecutorService.get()).expireAfterAccess(Duration.ofMinutes(5L)).<Long, Byte>build();
      this.renderInverse = false;
      this.newChunksColor = ColorHelper.getColor(255, 0, 0, 100);
      this.inverseColor = ColorHelper.getColor(0, 255, 0, 100);
   }

   public void setDiskCache(boolean disk) {
      this.newChunksCache.setDiskCache(disk, this.isEnabled());
      this.inverseNewChunksCache.setDiskCache(disk, this.isEnabled());
   }

   @EventHandler
   public void onMultiBlockUpdate(final ChunkBlocksUpdateEvent event) {
      if (event.phase() == Phase.PRE) {
         class_638 level = this.mc.field_1687;
         if (level != null && this.mc.field_1769.field_4112 != null) {
            event.packet().method_30621((pos, state) -> this.handleBlockUpdate(level, pos, state));
         }
      }
   }

   @EventHandler
   public void onBlockUpdate(final ChunkBlockUpdateEvent event) {
      if (event.phase() == Phase.PRE) {
         class_638 level = this.mc.field_1687;
         if (level != null && this.mc.field_1769.field_4112 != null) {
            this.handleBlockUpdate(level, event.packet().method_11309(), event.packet().method_11308());
         }
      }
   }

   private void handleBlockUpdate(class_1937 level, class_2338 pos, class_2680 state) {
      if (!state.method_26227().method_15769() && !state.method_26227().method_15771()) {
         int chunkX = ChunkUtils.posToChunkPos(pos.method_10263());
         int chunkZ = ChunkUtils.posToChunkPos(pos.method_10260());
         class_5321<class_1937> dim = level.method_27983();
         if (this.inverseNewChunksCache.get().isHighlighted(chunkX, chunkZ, dim)) {
            return;
         }

         if (this.newChunksCache.get().isHighlighted(chunkX, chunkZ, dim)) {
            return;
         }

         int srcX = pos.method_10263();
         int srcY = pos.method_10264();
         if (Settings.REGISTRY.liquidNewChunksOnlyAboveY0Setting.get() && srcY <= 0) {
            return;
         }

         int srcZ = pos.method_10260();
         class_2338.class_2339 bp = new class_2338.class_2339(srcX, srcY, srcZ);

         for(int i = 0; i < searchDirs.length; ++i) {
            class_2350 dir = searchDirs[i];
            bp.method_10103(srcX + dir.method_10148(), srcY + dir.method_10164(), srcZ + dir.method_10165());
            if (level.method_8320(bp).method_26227().method_15771()) {
               this.newChunksCache.get().addHighlight(chunkX, chunkZ, dim);
               return;
            }
         }
      }

   }

   @EventHandler
   public void onChunkData(final ChunkDataEvent event) {
      class_638 level = this.mc.field_1687;
      if (level != null && this.mc.field_1769.field_4112 != null) {
         class_2818 chunk = event.chunk();
         class_5321<class_1937> dim = chunk.method_12200().method_27983();
         class_1923 chunkPos = chunk.method_12004();
         long chunkLong = ChunkUtils.chunkPosToLong(chunkPos);
         if (this.seenChunksCache.getIfPresent(chunkLong) == null) {
            this.seenChunksCache.put(chunkLong, (byte)127);
            if (!this.newChunksCache.get().isHighlighted(chunkPos.field_9181, chunkPos.field_9180, dim)) {
               if (!this.inverseNewChunksCache.get().isHighlighted(chunkPos.field_9181, chunkPos.field_9180, dim)) {
                  ChunkScanner.chunkScanBlockstatePredicate(chunk, liquidBlockTypeFilter, (c, state, relX, y, relZ) -> {
                     int x = ChunkUtils.chunkCoordToCoord(c.method_12004().field_9181) + relX;
                     int z = ChunkUtils.chunkCoordToCoord(c.method_12004().field_9180) + relZ;
                     class_3610 fluid = state.method_26227();
                     if (!fluid.method_15769() && !fluid.method_15771()) {
                        if (fluid.method_15761() < 2) {
                           this.inverseNewChunksCache.get().addHighlight(c.method_12004().field_9181, chunk.method_12004().field_9180, dim);
                           return true;
                        }

                        boolean foundColumn = true;

                        for(int i = 1; i <= 5; ++i) {
                           class_3610 aboveState = chunk.method_12234(x, y + i, z);
                           if (aboveState.method_15769() || aboveState.method_15771()) {
                              foundColumn = false;
                              break;
                           }
                        }

                        if (foundColumn) {
                           this.inverseNewChunksCache.get().addHighlight(c.method_12004().field_9181, c.method_12004().field_9180, dim);
                           return true;
                        }
                     }

                     return false;
                  }, Settings.REGISTRY.liquidNewChunksOnlyAboveY0Setting.get() ? Math.max(1, level.method_31607()) : level.method_31607());
               }
            }
         }
      }
   }

   @EventHandler
   public void onXaeroWorldChangeEvent(final XaeroWorldChangeEvent event) {
      this.seenChunksCache.invalidateAll();
   }

   public synchronized void setInverseRenderEnabled(final boolean b) {
      this.renderInverse = b;
      if (this.renderInverse && this.isEnabled()) {
         this.registerInverseChunkHighlightProvider();
      } else {
         Globals.drawManager.registry().unregister("LiquidNewChunksInverse");
      }

   }

   public void onEnable() {
      Globals.drawManager.registry().register(DrawFeatureFactory.chunkHighlights("LiquidNewChunks", this::getNewChunkHighlightsState, this::getNewChunksColor, 250));
      if (this.renderInverse) {
         this.registerInverseChunkHighlightProvider();
      }

      this.newChunksCache.onEnable();
      this.inverseNewChunksCache.onEnable();
   }

   private void registerInverseChunkHighlightProvider() {
      Globals.drawManager.registry().register(DrawFeatureFactory.chunkHighlights("LiquidNewChunksInverse", this::getInverseNewChunkHighlightsState, this::getInverseColor, 250));
   }

   public void onDisable() {
      this.newChunksCache.onDisable();
      this.inverseNewChunksCache.onDisable();
      Globals.drawManager.registry().unregister("LiquidNewChunks");
      Globals.drawManager.registry().unregister("LiquidNewChunksInverse");
   }

   public int getNewChunksColor() {
      return this.newChunksColor;
   }

   private int getInverseColor() {
      return this.inverseColor;
   }

   public void setRgbColor(final int color) {
      this.newChunksColor = ColorHelper.getColorWithAlpha(color, Settings.REGISTRY.liquidNewChunksAlphaSetting.getAsInt());
   }

   public void setInverseRgbColor(final int color) {
      this.inverseColor = ColorHelper.getColorWithAlpha(color, Settings.REGISTRY.liquidNewChunksAlphaSetting.getAsInt());
   }

   public void setAlpha(final double a) {
      this.newChunksColor = ColorHelper.getColorWithAlpha(this.newChunksColor, (int)a);
      this.inverseColor = ColorHelper.getColorWithAlpha(this.inverseColor, (int)a);
   }

   public Long2LongMap getNewChunkHighlightsState(final class_5321<class_1937> dimension) {
      return this.newChunksCache.get().getCacheMap(dimension);
   }

   public Long2LongMap getInverseNewChunkHighlightsState(final class_5321<class_1937> dimension) {
      return this.inverseNewChunksCache.get().getCacheMap(dimension);
   }

   public boolean isNewChunk(final int chunkPosX, final int chunkPosZ, final class_5321<class_1937> dimensionId) {
      return this.newChunksCache.get().isHighlighted(chunkPosX, chunkPosZ, dimensionId);
   }

   public boolean isInverseNewChunk(final int chunkPosX, final int chunkPosZ, final class_5321<class_1937> dimensionId) {
      return this.inverseNewChunksCache.get().isHighlighted(chunkPosX, chunkPosZ, dimensionId);
   }

   static {
      searchDirs = new class_2350[]{class_2350.field_11034, class_2350.field_11043, class_2350.field_11039, class_2350.field_11035, class_2350.field_11036};
      liquidBlockTypeFilter = ReferenceOpenHashSet.of(class_2246.field_10382, class_2246.field_10164);
   }
}
