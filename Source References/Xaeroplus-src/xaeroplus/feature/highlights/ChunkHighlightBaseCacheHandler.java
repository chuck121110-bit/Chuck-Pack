package xaeroplus.feature.highlights;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import java.util.Objects;
import net.minecraft.class_1937;
import net.minecraft.class_310;
import net.minecraft.class_5321;
import xaeroplus.util.ChunkUtils;

public abstract class ChunkHighlightBaseCacheHandler implements ChunkHighlightCache {
   public final Long2LongMap chunks = new Long2LongOpenHashMap();
   public class_310 mc = class_310.method_1551();

   public ChunkHighlightBaseCacheHandler() {
      this.chunks.defaultReturnValue(-1L);
   }

   public void addHighlight(final int x, final int z) {
      this.addHighlight(x, z, System.currentTimeMillis());
   }

   public void addHighlight(final int x, final int z, final class_5321<class_1937> dimensionId) {
      this.addHighlight(x, z);
   }

   public void addHighlight(final int x, final int z, final long foundTime) {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("addHighlight must be called on the main thread!");
      } else {
         this.chunks.put(ChunkUtils.chunkPosToLong(x, z), foundTime);
      }
   }

   public void addHighlight(final int x, final int z, final long foundTime, final class_5321<class_1937> dimensionId) {
      this.addHighlight(x, z, foundTime);
   }

   public void removeHighlight(final int x, final int z) {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("removeHighlight must be called on the main thread!");
      } else {
         this.chunks.remove(ChunkUtils.chunkPosToLong(x, z));
      }
   }

   public void removeHighlight(final int x, final int z, final class_5321<class_1937> dimensionId) {
      this.removeHighlight(x, z);
   }

   public void removeHighlights(final LongCollection toRemove) {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("removeHighlights must be called on the main thread!");
      } else {
         Long2LongMap var10001 = this.chunks;
         Objects.requireNonNull(var10001);
         toRemove.forEach(var10001::remove);
      }
   }

   public void removeHighlights(final LongCollection toRemove, class_5321<class_1937> dimensionId) {
      this.removeHighlights(toRemove);
   }

   public boolean isHighlighted(final int x, final int z, class_5321<class_1937> dimensionId) {
      return this.isHighlighted(ChunkUtils.chunkPosToLong(x, z));
   }

   public Long2LongMap getCacheMap(final class_5321<class_1937> dimension) {
      return this.chunks;
   }

   public boolean isHighlighted(final long chunkPos) {
      return this.chunks.containsKey(chunkPos);
   }

   public void replaceState(final Long2LongOpenHashMap state) {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("replaceState must be called on the main thread!");
      } else {
         this.chunks.clear();
         this.chunks.putAll(state);
      }
   }

   public void reset() {
      if (!this.mc.method_18854()) {
         throw new RuntimeException("reset must be called on the main thread!");
      } else {
         this.chunks.clear();
      }
   }
}
