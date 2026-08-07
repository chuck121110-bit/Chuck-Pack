package xaeroplus.feature.render.line;

import java.util.List;
import net.minecraft.class_1937;
import net.minecraft.class_5321;

@FunctionalInterface
public interface LineSupplier {
   List<Line> getLines(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final class_5321<class_1937> dimension);
}
