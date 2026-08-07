package xaeroplus.feature.render.ellipse;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import xaeroplus.feature.render.DrawContext;

public class MultiColorEllipseDrawFeature extends AbstractEllipseDrawFeature<Object2IntMap<Ellipse>> {
   private final String id;
   private final MultiColorEllipseProvider ellipseProvider;
   private final MultiColorEllipseVertexBuffer drawBuffer;

   public MultiColorEllipseDrawFeature(final String id, final MultiColorEllipseProvider ellipseProvider, final int refreshIntervalMs) {
      super(refreshIntervalMs);
      this.id = id;
      this.ellipseProvider = ellipseProvider;
      this.drawBuffer = new MultiColorEllipseVertexBuffer(ellipseProvider.colorFunction());
   }

   public float thickness() {
      return this.ellipseProvider.thicknessSupplier().getFloat();
   }

   public Object2IntMap<Ellipse> provideEllipsesInWindow(final int windowX, final int windowZ, final int windowSize, final class_5321<class_1937> dimension) {
      return this.ellipseProvider.ellipseSupplier().getEllipses(windowX, windowZ, windowSize, dimension);
   }

   public Object2IntMap<Ellipse> preProcessEllipses(final Object2IntMap<Ellipse> ellipses, final int windowX, final int windowZ, final int windowSize) {
      if (ellipses.isEmpty()) {
         return Object2IntMaps.emptyMap();
      } else {
         EllipsePreProcessor.WindowBounds bounds = EllipsePreProcessor.windowBounds(windowX, windowZ, windowSize);
         Object2IntOpenHashMap<Ellipse> visibleEllipses = new Object2IntOpenHashMap(ellipses.size());
         ObjectIterator<Object2IntMap.Entry<Ellipse>> iterator = Object2IntMaps.fastIterator(ellipses);

         while(iterator.hasNext()) {
            Object2IntMap.Entry<Ellipse> entry = (Object2IntMap.Entry)iterator.next();
            if (bounds.intersects((Ellipse)entry.getKey())) {
               visibleEllipses.put((Ellipse)entry.getKey(), entry.getIntValue());
            }
         }

         return visibleEllipses;
      }
   }

   public Object2IntMap<Ellipse> emptyEllipses() {
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
      this.drawBuffer.preRender(ctx, (Object2IntMap)this.getEllipses());
      this.drawBuffer.render(ctx, this.thicknessScale(ctx));
      this.postRender(ctx);
   }
}
