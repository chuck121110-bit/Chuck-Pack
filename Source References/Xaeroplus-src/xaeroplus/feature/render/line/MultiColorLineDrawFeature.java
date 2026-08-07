package xaeroplus.feature.render.line;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.List;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import xaeroplus.feature.render.DrawContext;

public class MultiColorLineDrawFeature extends AbstractLineDrawFeature<Object2IntMap<Line>> {
   private final String id;
   private final MultiColorLineProvider lineProvider;
   private final MultiColorLineVertexBuffer drawBuffer;

   public MultiColorLineDrawFeature(final String id, final MultiColorLineProvider lineProvider, int refreshIntervalMs) {
      super(refreshIntervalMs);
      this.id = id;
      this.lineProvider = lineProvider;
      this.drawBuffer = new MultiColorLineVertexBuffer(lineProvider.colorFunction());
   }

   public float lineWidth() {
      return this.lineProvider.lineWidthSupplier().getFloat();
   }

   public Object2IntMap<Line> provideLinesInWindow(final int windowX, final int windowZ, final int windowSize, final class_5321<class_1937> dimension) {
      return this.lineProvider.lineSupplier().getLines(windowX, windowZ, windowSize, dimension);
   }

   public Object2IntMap<Line> preProcessLines(final Object2IntMap<Line> lines, final int windowX, final int windowZ, final int windowSize) {
      if (lines.isEmpty()) {
         return Object2IntMaps.emptyMap();
      } else {
         Object2IntMap<Line> out = new Object2IntOpenHashMap(lines.size());
         LinePreProcessor.WindowBounds bounds = LinePreProcessor.windowBounds(windowX, windowZ, windowSize);
         ObjectIterator<Object2IntMap.Entry<Line>> it = Object2IntMaps.fastIterator(lines);

         while(it.hasNext()) {
            Object2IntMap.Entry<Line> entry = (Object2IntMap.Entry)it.next();
            List<Line> processedLines = LinePreProcessor.clippedSplitOriented((Line)entry.getKey(), bounds);

            for(int i = 0; i < processedLines.size(); ++i) {
               out.put((Line)processedLines.get(i), entry.getIntValue());
            }
         }

         return out;
      }
   }

   public Object2IntMap<Line> emptyLines() {
      return Object2IntMaps.emptyMap();
   }

   public String id() {
      return this.id;
   }

   protected void markDrawBufferStale() {
      this.drawBuffer.markStale();
   }

   protected void closeDrawBuffer() {
      this.drawBuffer.close();
   }

   public void render(final DrawContext ctx) {
      this.preRender(ctx);
      this.drawBuffer.preRender(ctx, (Object2IntMap)this.getLines());
      this.drawBuffer.render(ctx, this.lineWidthScale(ctx));
      this.postRender(ctx);
   }
}
