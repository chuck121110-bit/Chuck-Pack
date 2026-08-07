package xaeroplus.feature.render.ellipse;

import java.util.function.IntSupplier;
import xaeroplus.util.FloatSupplier;

public record EllipseProvider(EllipseSupplier ellipseSupplier, IntSupplier colorSupplier, FloatSupplier thicknessSupplier) {
}
