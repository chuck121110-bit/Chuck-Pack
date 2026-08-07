package xaeroplus.feature.highlights;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import xaeroplus.event.XaeroWorldChangeEvent;
import xaeroplus.module.impl.TickTaskExecutor;

public interface ChunkHighlightCache {
   void addHighlight(final int x, final int z);

   void addHighlight(final int x, final int z, class_5321<class_1937> dimensionId);

   void addHighlight(final int x, final int z, long foundTime);

   void addHighlight(final int x, final int z, long foundTime, class_5321<class_1937> dimensionId);

   void removeHighlight(final int x, final int z);

   void removeHighlight(final int x, final int z, class_5321<class_1937> dimensionId);

   void removeHighlights(final LongCollection toRemove);

   void removeHighlights(final LongCollection toRemove, class_5321<class_1937> dimensionId);

   boolean isHighlighted(final int x, final int z, class_5321<class_1937> dimensionId);

   Long2LongMap getCacheMap(class_5321<class_1937> dimensionId);

   CompletableFuture<Long2LongMap> getHighlightsInCustomWindow(int windowRegionX, int windowRegionZ, int windowRegionSize, class_5321<class_1937> dimension);

   void handleWorldChange(final XaeroWorldChangeEvent event);

   void handleTick();

   void onEnable();

   void onDisable();

   default <V> CompletableFuture<V> submitTickTask(final Supplier<V> task) {
      return TickTaskExecutor.INSTANCE.submit(task);
   }

   default CompletableFuture<Void> submitTickTask(final Runnable task) {
      return TickTaskExecutor.INSTANCE.submit(task);
   }
}
