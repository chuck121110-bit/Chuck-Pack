package xaeroplus.feature.render.line;

import java.util.function.IntSupplier;
import xaeroplus.util.FloatSupplier;

public record LineProvider(LineSupplier lineSupplier, IntSupplier colorSupplier, FloatSupplier lineWidthSupplier) {
}
