package xaeroplus.feature.render.highlight;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import net.minecraft.class_1937;
import net.minecraft.class_5321;

@FunctionalInterface
public interface AsyncChunkHighlightSupplier {
   Long2LongMap getHighlights(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final class_5321<class_1937> dimension);
}
