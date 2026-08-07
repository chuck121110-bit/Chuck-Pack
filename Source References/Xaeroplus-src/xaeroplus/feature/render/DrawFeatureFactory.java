package xaeroplus.feature.render;

import java.util.function.IntSupplier;
import xaeroplus.feature.render.ellipse.EllipseDrawFeature;
import xaeroplus.feature.render.ellipse.EllipseProvider;
import xaeroplus.feature.render.ellipse.EllipseSupplier;
import xaeroplus.feature.render.ellipse.MultiColorEllipseColorFunction;
import xaeroplus.feature.render.ellipse.MultiColorEllipseDrawFeature;
import xaeroplus.feature.render.ellipse.MultiColorEllipseProvider;
import xaeroplus.feature.render.ellipse.MultiColorEllipseSupplier;
import xaeroplus.feature.render.highlight.AsyncChunkHighlightDrawFeature;
import xaeroplus.feature.render.highlight.AsyncChunkHighlightProvider;
import xaeroplus.feature.render.highlight.AsyncChunkHighlightSupplier;
import xaeroplus.feature.render.highlight.DirectChunkHighlightDrawFeature;
import xaeroplus.feature.render.highlight.DirectChunkHighlightProvider;
import xaeroplus.feature.render.highlight.DirectChunkHighlightSupplier;
import xaeroplus.feature.render.highlight.HighlightVertexBuffer;
import xaeroplus.feature.render.highlight.MultiColorHighlightColorFunction;
import xaeroplus.feature.render.highlight.MultiColorHighlightVertexBuffer;
import xaeroplus.feature.render.line.LineDrawFeature;
import xaeroplus.feature.render.line.LineProvider;
import xaeroplus.feature.render.line.LineSupplier;
import xaeroplus.feature.render.line.MultiColorLineColorFunction;
import xaeroplus.feature.render.line.MultiColorLineDrawFeature;
import xaeroplus.feature.render.line.MultiColorLineProvider;
import xaeroplus.feature.render.line.MultiColorLineSupplier;
import xaeroplus.feature.render.text.AsyncTextDrawFeature;
import xaeroplus.feature.render.text.DirectTextDrawFeature;
import xaeroplus.feature.render.text.TextSupplier;
import xaeroplus.util.FloatSupplier;

public interface DrawFeatureFactory {
   static DrawFeature ellipses(String id, EllipseSupplier ellipseSupplier, IntSupplier colorSupplier, FloatSupplier thicknessSupplier, int refreshIntervalMs) {
      return new EllipseDrawFeature(id, new EllipseProvider(ellipseSupplier, colorSupplier, thicknessSupplier), refreshIntervalMs);
   }

   static DrawFeature multiColorEllipses(String id, MultiColorEllipseSupplier ellipseSupplier, MultiColorEllipseColorFunction colorFunction, FloatSupplier thicknessSupplier, int refreshIntervalMs) {
      return new MultiColorEllipseDrawFeature(id, new MultiColorEllipseProvider(ellipseSupplier, colorFunction, thicknessSupplier), refreshIntervalMs);
   }

   static DrawFeature chunkHighlights(String id, DirectChunkHighlightSupplier chunkHighlightSupplier, IntSupplier colorSupplier, int refreshIntervalMs) {
      return new DirectChunkHighlightDrawFeature(id, new HighlightVertexBuffer(), new DirectChunkHighlightProvider(chunkHighlightSupplier, colorSupplier), refreshIntervalMs);
   }

   static DrawFeature multiColorChunkHighlights(String id, DirectChunkHighlightSupplier chunkHighlightSupplier, MultiColorHighlightColorFunction colorFunction, int refreshIntervalMs) {
      return new DirectChunkHighlightDrawFeature(id, new MultiColorHighlightVertexBuffer(colorFunction), new DirectChunkHighlightProvider(chunkHighlightSupplier, () -> 0), refreshIntervalMs);
   }

   static DrawFeature asyncChunkHighlights(String id, AsyncChunkHighlightSupplier chunkHighlightSupplier, IntSupplier colorSupplier) {
      return new AsyncChunkHighlightDrawFeature(id, new HighlightVertexBuffer(), new AsyncChunkHighlightProvider(chunkHighlightSupplier, colorSupplier));
   }

   static DrawFeature multiColorAsyncChunkHighlights(String id, AsyncChunkHighlightSupplier chunkHighlightSupplier, MultiColorHighlightColorFunction colorFunction) {
      return new AsyncChunkHighlightDrawFeature(id, new MultiColorHighlightVertexBuffer(colorFunction), new AsyncChunkHighlightProvider(chunkHighlightSupplier, () -> 0));
   }

   static DrawFeature lines(String id, LineSupplier lineSupplier, IntSupplier colorSupplier, FloatSupplier lineWidthSupplier, int refreshIntervalMs) {
      return new LineDrawFeature(id, new LineProvider(lineSupplier, colorSupplier, lineWidthSupplier), refreshIntervalMs);
   }

   static DrawFeature multiColorLines(String id, MultiColorLineSupplier lineSupplier, MultiColorLineColorFunction colorFunction, FloatSupplier lineWidthSupplier, int refreshIntervalMs) {
      return new MultiColorLineDrawFeature(id, new MultiColorLineProvider(lineSupplier, colorFunction, lineWidthSupplier), refreshIntervalMs);
   }

   static DrawFeature text(String id, TextSupplier textSupplier) {
      return new DirectTextDrawFeature(id, textSupplier);
   }

   static DrawFeature asyncText(String id, TextSupplier textSupplier, int refreshIntervalMs) {
      return new AsyncTextDrawFeature(id, textSupplier, refreshIntervalMs);
   }
}
