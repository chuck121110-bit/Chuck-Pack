package xaeroplus.module.impl;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import java.util.concurrent.atomic.AtomicInteger;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2818;
import net.minecraft.class_3532;
import net.minecraft.class_3610;
import net.minecraft.class_5321;
import net.minecraft.class_638;
import xaeroplus.Globals;
import xaeroplus.event.ChunkDataEvent;
import xaeroplus.feature.highlights.SavableHighlightCacheInstance;
import xaeroplus.feature.render.DrawFeatureFactory;
import xaeroplus.module.Module;
import xaeroplus.util.ChunkScanner;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.ColorHelper;

public class LavaColumns extends Module {
   public final SavableHighlightCacheInstance lavaColumnsCache = new SavableHighlightCacheInstance("XaeroPlusLavaColumns");
   private int minColumnHeight = 5;
   private int alphaStep = 8;
   private int alphaShift = 0;
   private int color = ColorHelper.getColor(0, 255, 0, 255);
   private static final ReferenceSet<class_2248> lavaFilter;

   public void setDiskCache(boolean disk) {
      this.lavaColumnsCache.setDiskCache(disk, this.isEnabled());
   }

   @EventHandler
   public void onChunkData(final ChunkDataEvent event) {
      class_638 level = this.mc.field_1687;
      if (level != null && this.mc.field_1769.field_4112 != null) {
         class_2818 chunk = event.chunk();
         class_5321<class_1937> dim = chunk.method_12200().method_27983();
         class_1923 chunkPos = chunk.method_12004();
         if (dim == class_1937.field_25180) {
            if (!this.lavaColumnsCache.get().isHighlighted(chunkPos.field_9181, chunkPos.field_9180, dim)) {
               AtomicInteger maxHeight = new AtomicInteger(0);
               ChunkScanner.chunkScanBlockstatePredicate(chunk, lavaFilter, (c, state, relX, y, relZ) -> {
                  int x = ChunkUtils.chunkCoordToCoord(c.method_12004().field_9181) + relX;
                  int z = ChunkUtils.chunkCoordToCoord(c.method_12004().field_9180) + relZ;
                  int maxY = level.method_31600();
                  class_3610 fluid = state.method_26227();
                  if (!fluid.method_15769() && !fluid.method_15771()) {
                     int columnHeight = 1;

                     for(int yy = y + 1; yy <= maxY; ++yy) {
                        class_3610 aboveState = chunk.method_12234(x, yy, z);
                        if (aboveState.method_15769() || aboveState.method_15771()) {
                           break;
                        }

                        ++columnHeight;
                     }

                     if (columnHeight >= maxHeight.get()) {
                        maxHeight.set(columnHeight);
                     }
                  }

                  return false;
               }, 0);
               this.lavaColumnsCache.get().addHighlight(chunk.method_12004().field_9181, chunk.method_12004().field_9180, (long)maxHeight.get(), dim);
            }
         }
      }
   }

   public void onEnable() {
      Globals.drawManager.registry().register(DrawFeatureFactory.multiColorChunkHighlights("LavaColumns", this::getLavaColumnHeightHighlights, this::colorFunction, 250));
      this.lavaColumnsCache.onEnable();
   }

   private Long2LongMap getLavaColumnHeightHighlights(class_5321<class_1937> dim) {
      return this.lavaColumnsCache.get().getCacheMap(dim);
   }

   private int colorFunction(long pos, long columnHeight) {
      if (columnHeight < (long)this.minColumnHeight) {
         return 0;
      } else {
         int alpha = class_3532.method_15340(this.alphaShift + (int)columnHeight * this.alphaStep, 0, 255);
         return ColorHelper.getColorWithAlpha(this.color, alpha);
      }
   }

   public void onDisable() {
      this.lavaColumnsCache.onDisable();
      Globals.drawManager.registry().unregister("LavaColumns");
   }

   public void setMinColumnHeight(final int minHeight) {
      this.minColumnHeight = minHeight;
   }

   public void setAlphaStep(final int alphaStep) {
      this.alphaStep = alphaStep;
   }

   public void setRgbColor(final int color) {
      this.color = color;
   }

   public void setAlphaShift(final int alphaShift) {
      this.alphaShift = alphaShift;
   }

   static {
      lavaFilter = ReferenceSet.of(class_2246.field_10164);
   }
}
