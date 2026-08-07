package xaeroplus.feature.render.line;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import xaeroplus.feature.render.DrawContext;
import xaeroplus.util.ColorHelper;

public class LineDrawFeature extends AbstractLineDrawFeature<List<Line>> {
   private final String id;
   private final LineProvider lineProvider;
   private final LineVertexBuffer drawBuffer = new LineVertexBuffer();

   public LineDrawFeature(final String id, final LineProvider lineProvider, int refreshIntervalMs) {
      super(refreshIntervalMs);
      this.id = id;
      this.lineProvider = lineProvider;
   }

   public float lineWidth() {
      return this.lineProvider.lineWidthSupplier().getFloat();
   }

   public List<Line> provideLinesInWindow(final int windowX, final int windowZ, final int windowSize, final class_5321<class_1937> dimension) {
      return this.lineProvider.lineSupplier().getLines(windowX, windowZ, windowSize, dimension);
   }

   public List<Line> preProcessLines(final List<Line> lines, final int windowX, final int windowZ, final int windowSize) {
      if (lines.isEmpty()) {
         return lines;
      } else {
         LinePreProcessor.WindowBounds bounds = LinePreProcessor.windowBounds(windowX, windowZ, windowSize);
         List<Line> out = new ArrayList(lines.size());

         for(int i = 0; i < lines.size(); ++i) {
            List<Line> processedLines = LinePreProcessor.clippedSplitOriented((Line)lines.get(i), bounds);
            if (!processedLines.isEmpty()) {
               out.addAll(processedLines);
            }
         }

         return out;
      }
   }

   public List<Line> emptyLines() {
      return Collections.emptyList();
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
      int color = this.lineProvider.colorSupplier().getAsInt();
      if (ColorHelper.getA(color) != 0.0F) {
         this.drawBuffer.setColor(color);
         this.preRender(ctx);
         this.drawBuffer.preRender(ctx, (List)this.getLines());
         this.drawBuffer.render(ctx, this.lineWidthScale(ctx));
         this.postRender(ctx);
      }
   }
}
