package xaeroplus.feature.highlights;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import xaeroplus.XaeroPlus;
import xaeroplus.event.XaeroWorldChangeEvent;

public class ChunkHighlightLocalCache extends ChunkHighlightBaseCacheHandler {
   private static final int maxNumber = 5000;

   public void addHighlight(final int x, final int z) {
      super.addHighlight(x, z);
      this.limitChunksSize();
   }

   public void addHighlight(final int x, final int z, final long foundTime) {
      super.addHighlight(x, z, foundTime);
      this.limitChunksSize();
   }

   private void limitChunksSize() {
      try {
         if (this.chunks.size() > 5000) {
            long[] toRemove = this.chunks.long2LongEntrySet().stream().sorted(Entry.comparingByValue()).limit(500L).mapToLong(Long2LongMap.Entry::getLongKey).toArray();

            for(int i = 0; i < toRemove.length; ++i) {
               this.chunks.remove(toRemove[i]);
            }
         }
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Error limiting local cache size", e);
      }

   }

   public CompletableFuture<Long2LongMap> getHighlightsInCustomWindow(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final class_5321<class_1937> dimension) {
      return this.submitTickTask(() -> new Long2LongOpenHashMap(this.chunks));
   }

   public void handleWorldChange(final XaeroWorldChangeEvent event) {
      this.chunks.clear();
   }

   public void handleTick() {
   }

   public void onEnable() {
   }

   public void onDisable() {
   }
}
