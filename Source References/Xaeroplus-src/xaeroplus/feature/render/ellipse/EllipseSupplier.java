package xaeroplus.feature.render.ellipse;

import java.util.List;
import net.minecraft.class_1937;
import net.minecraft.class_5321;

@FunctionalInterface
public interface EllipseSupplier {
   List<Ellipse> getEllipses(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final class_5321<class_1937> dimension);
}
