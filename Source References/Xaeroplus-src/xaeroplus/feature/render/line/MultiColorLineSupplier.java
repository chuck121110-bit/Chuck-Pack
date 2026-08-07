package xaeroplus.feature.render.line;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.class_1937;
import net.minecraft.class_5321;

@FunctionalInterface
public interface MultiColorLineSupplier {
   Object2IntMap<Line> getLines(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final class_5321<class_1937> dimension);
}
