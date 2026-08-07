package xaeroplus.feature.render.ellipse;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.class_1937;
import net.minecraft.class_5321;

@FunctionalInterface
public interface MultiColorEllipseSupplier {
   Object2IntMap<Ellipse> getEllipses(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final class_5321<class_1937> dimension);
}
