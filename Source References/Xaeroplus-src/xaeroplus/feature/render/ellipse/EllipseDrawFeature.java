package xaeroplus.feature.render.ellipse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import xaeroplus.feature.render.DrawContext;
import xaeroplus.util.ColorHelper;

public class EllipseDrawFeature extends AbstractEllipseDrawFeature<List<Ellipse>> {
   private final String id;
   private final EllipseProvider ellipseProvider;
   private final EllipseVertexBuffer drawBuffer = new EllipseVertexBuffer();

   public EllipseDrawFeature(final String id, final EllipseProvider ellipseProvider, final int refreshIntervalMs) {
      super(refreshIntervalMs);
      this.id = id;
      this.ellipseProvider = ellipseProvider;
   }

   public float thickness() {
      return this.ellipseProvider.thicknessSupplier().getFloat();
   }

   public List<Ellipse> provideEllipsesInWindow(final int windowX, final int windowZ, final int windowSize, final class_5321<class_1937> dimension) {
      return this.ellipseProvider.ellipseSupplier().getEllipses(windowX, windowZ, windowSize, dimension);
   }

   public List<Ellipse> preProcessEllipses(final List<Ellipse> ellipses, final int windowX, final int windowZ, final int windowSize) {
      if (ellipses.isEmpty()) {
         return ellipses;
      } else {
         EllipsePreProcessor.WindowBounds bounds = EllipsePreProcessor.windowBounds(windowX, windowZ, windowSize);
         ArrayList<Ellipse> visibleEllipses = new ArrayList(ellipses.size());

         for(Ellipse ellipse : ellipses) {
            if (bounds.intersects(ellipse)) {
               visibleEllipses.add(ellipse);
            }
         }

         return visibleEllipses;
      }
   }

   public List<Ellipse> emptyEllipses() {
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
      int color = this.ellipseProvider.colorSupplier().getAsInt();
      if (ColorHelper.getA(color) != 0.0F) {
         this.drawBuffer.setColor(color);
         this.preRender(ctx);
         this.drawBuffer.preRender(ctx, (List)this.getEllipses());
         this.drawBuffer.render(ctx, this.thicknessScale(ctx));
         this.postRender(ctx);
      }
   }
}
