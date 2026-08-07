package xaeroplus.module.impl;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_1937;
import net.minecraft.class_3532;
import net.minecraft.class_5321;
import xaeroplus.Globals;
import xaeroplus.XaeroPlus;
import xaeroplus.event.ClientTickEvent;
import xaeroplus.event.XaeroWorldChangeEvent;
import xaeroplus.feature.drawing.DrawingCache;
import xaeroplus.feature.render.DrawFeatureFactory;
import xaeroplus.feature.render.DrawFeatureRegistry;
import xaeroplus.feature.render.ellipse.Ellipse;
import xaeroplus.feature.render.line.Line;
import xaeroplus.feature.render.text.Text;
import xaeroplus.module.Module;
import xaeroplus.util.ChunkUtils;
import xaeroplus.util.Color;
import xaeroplus.util.ColorHelper;
import xaeroplus.util.DrawingMode;
import xaeroplus.util.GuiMapHelper;

public class Drawing extends Module {
   public final DrawingCache drawingCache = new DrawingCache("XaeroPlusDrawing");
   private Line inProgressLine = null;
   private Ellipse inProgressEllipse = null;
   private Color drawingColor = new Color(255, 0, 0, 200);
   private final Deque<DrawingOperation> operationStack = new LinkedBlockingDeque();
   private DrawingOperationCollector operationCollector = null;
   private final int inProgressColorAlpha = 80;
   private static final int SNAP_THRESHOLD = 10;

   public Color getDrawingColor() {
      return this.drawingColor;
   }

   public void setDrawingColor(final Color drawingColor) {
      this.drawingColor = (Color)Objects.requireNonNull(drawingColor);
   }

   public void startOperation(class_5321<class_1937> dimension, boolean erase) {
      this.operationCollector = new DrawingOperationCollector(dimension, erase);
   }

   public void endOperation() {
      if (this.operationCollector != null) {
         DrawingOperation op = this.operationCollector.collect();
         if (op != null) {
            this.operationStack.push(op);
         }

         this.operationCollector = null;
      }

   }

   public void undoLastOperation() {
      if (!this.operationStack.isEmpty()) {
         DrawingOperation op = (DrawingOperation)this.operationStack.pop();
         op.revert(this);
      }

   }

   public void onEnable() {
      this.drawingCache.onEnable();
      Globals.drawManager.registry().register(DrawFeatureFactory.multiColorLines("Drawing-lines-saved", this::getSavedLines, (line, v) -> v, () -> 0.5F, 50));
      Globals.drawManager.registry().register(DrawFeatureFactory.lines("Drawing-lines-in-progress", this::getInProgressLines, () -> ColorHelper.getColorWithAlpha(this.drawingColor.getInt(), 80), () -> 0.5F, 1));
      Globals.drawManager.registry().register(DrawFeatureFactory.multiColorEllipses("Drawing-ellipses-saved", this::getSavedEllipses, (ellipse, color) -> color, () -> 0.25F, 50));
      Globals.drawManager.registry().register(DrawFeatureFactory.ellipses("Drawing-ellipses-in-progress", this::getInProgressEllipses, () -> this.drawingColor.getInt(), () -> 0.25F, 1));
      DrawFeatureRegistry var10000 = Globals.drawManager.registry();
      DrawingCache var10002 = this.drawingCache;
      Objects.requireNonNull(var10002);
      var10000.register(DrawFeatureFactory.multiColorChunkHighlights("Drawing-highlights", var10002::getHighlights, (pos, t) -> (int)t, 50));
      Globals.drawManager.registry().register(DrawFeatureFactory.text("Drawing-text", this::getTexts));
      this.operationStack.clear();
   }

   private Long2ObjectMap<Text> getTexts(int windowRegionX, int windowRegionZ, int windowSize, class_5321<class_1937> dim) {
      return this.drawingCache.getTexts(dim);
   }

   @EventHandler
   public void onTick(final ClientTickEvent.Post event) {
      this.drawingCache.handleTick();
      if (GuiMapHelper.getGuiMap().isEmpty()) {
         this.operationStack.clear();
         this.operationCollector = null;
      }

   }

   @EventHandler
   public void onWorldChange(final XaeroWorldChangeEvent event) {
      if (this.mc.method_22108()) {
         this.drawingCache.handleWorldChange(event);
      }
   }

   public CompletableFuture<Void> shutdown() {
      try {
         return this.drawingCache.onShutdown();
      } catch (Exception e) {
         XaeroPlus.LOGGER.error("Failed to close drawing cache", e);
         return CompletableFuture.completedFuture((Object)null);
      }
   }

   private Object2IntMap<Line> getSavedLines(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final class_5321<class_1937> dimension) {
      return this.drawingCache.getLines(dimension);
   }

   private Object2IntMap<Ellipse> getSavedEllipses(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final class_5321<class_1937> dimension) {
      return this.drawingCache.getEllipses(dimension);
   }

   public Line getInProgressLine() {
      return this.inProgressLine;
   }

   private List<Line> getInProgressLines(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final class_5321<class_1937> dimension) {
      Line l = this.inProgressLine;
      return this.inProgressLine != null ? List.of(l) : Collections.emptyList();
   }

   private List<Ellipse> getInProgressEllipses(final int windowRegionX, final int windowRegionZ, final int windowRegionSize, final class_5321<class_1937> dimension) {
      Ellipse ellipse = this.inProgressEllipse;
      return ellipse == null ? Collections.emptyList() : List.of(ellipse);
   }

   public void addLine(final Line line, int color) {
      if (!(line.length() < (double)2.0F)) {
         class_5321<class_1937> dimension = Globals.getCurrentDimensionId();
         Object2IntMap<Line> savedLines = this.drawingCache.getLines(dimension);
         Integer previousColor = savedLines.containsKey(line) ? savedLines.getInt(line) : null;
         this.drawingCache.addLine(line, color, dimension);
         if (this.operationCollector != null) {
            this.operationCollector.addLine(line, previousColor);
         }

      }
   }

   public void addLine(final Line line) {
      this.addLine(line, this.drawingColor.getInt());
   }

   public void addInfiniteLine(final Line line, int color) {
      if (!(line.length() < (double)2.0F)) {
         Line infLine = line.extrapolateToWorldBorder();
         class_5321<class_1937> dimension = Globals.getCurrentDimensionId();
         Object2IntMap<Line> savedLines = this.drawingCache.getLines(dimension);
         Integer previousColor = savedLines.containsKey(infLine) ? savedLines.getInt(infLine) : null;
         this.drawingCache.addLine(infLine, color, dimension);
         if (this.operationCollector != null) {
            this.operationCollector.addLine(infLine, previousColor);
         }

      }
   }

   public void addInfiniteLine(final Line line) {
      this.addInfiniteLine(line, this.drawingColor.getInt());
   }

   public void addEllipse(final Ellipse ellipse, final int color) {
      class_5321<class_1937> dimension = Globals.getCurrentDimensionId();
      Object2IntMap<Ellipse> savedEllipses = this.drawingCache.getEllipses(dimension);
      Integer previousColor = savedEllipses.containsKey(ellipse) ? savedEllipses.getInt(ellipse) : null;
      this.drawingCache.addEllipse(ellipse, color, dimension);
      if (this.operationCollector != null) {
         this.operationCollector.addEllipse(ellipse, previousColor);
      }

   }

   public void addEllipse(final Ellipse ellipse) {
      this.addEllipse(ellipse, this.drawingColor.getInt());
   }

   public void addHighlight(int chunkX, int chunkZ, int color) {
      class_5321<class_1937> dimension = Globals.getCurrentDimensionId();
      long key = ChunkUtils.chunkPosToLong(chunkX, chunkZ);
      Long2LongMap savedHighlights = this.drawingCache.getHighlights(dimension);
      Long previousColor = savedHighlights.containsKey(key) ? savedHighlights.get(key) : null;
      this.drawingCache.addHighlight(chunkX, chunkZ, color, dimension);
      if (this.operationCollector != null) {
         this.operationCollector.addHighlight(key, previousColor);
      }

   }

   public void addHighlight(int chunkX, int chunkZ) {
      this.addHighlight(chunkX, chunkZ, this.drawingColor.getInt());
   }

   public void removeHighlight(final int chunkX, final int chunkZ) {
      class_5321<class_1937> dimension = Globals.getCurrentDimensionId();
      long key = ChunkUtils.chunkPosToLong(chunkX, chunkZ);
      Long2LongMap highlights = this.drawingCache.getHighlights(dimension);
      if (highlights.containsKey(key)) {
         long color = highlights.get(key);
         this.drawingCache.removeHighlight(chunkX, chunkZ, dimension);
         if (this.operationCollector != null) {
            this.operationCollector.addErasedHighlight(key, color);
         }

      }
   }

   public void removeHighlights(final LongCollection toRemove) {
      Long2LongOpenHashMap matched = new Long2LongOpenHashMap();
      LongIterator it = toRemove.longIterator();
      Long2LongMap existingCache = this.drawingCache.getHighlights(Globals.getCurrentDimensionId());

      while(it.hasNext()) {
         long chunkLong = it.nextLong();
         if (existingCache.containsKey(chunkLong)) {
            matched.put(chunkLong, existingCache.get(chunkLong));
         }
      }

      this.drawingCache.removeHighlights(matched.keySet(), Globals.getCurrentDimensionId());
      if (this.operationCollector != null) {
         this.operationCollector.addErasedHighlights(matched);
      }

   }

   public void addText(final Text text) {
      if (!text.value().isBlank()) {
         class_5321<class_1937> dimension = Globals.getCurrentDimensionId();
         long key = ChunkUtils.chunkPosToLong(text.x(), text.z());
         Long2ObjectMap<Text> savedTexts = this.drawingCache.getTexts(dimension);
         Text previousText = (Text)savedTexts.get(key);
         this.drawingCache.addText(text, dimension);
         if (this.operationCollector != null) {
            this.operationCollector.addText(text, previousText);
         }

      }
   }

   public void removeText(int x, int z, float viewScale) {
      Long2ObjectMap<Text> texts = this.drawingCache.getTexts(Globals.getCurrentDimensionId());
      List<Text> toRemove = new ArrayList();
      ObjectIterator var6 = texts.values().iterator();

      while(var6.hasNext()) {
         Text text = (Text)var6.next();
         int textX = text.x();
         int textZ = text.z();
         String value = text.value();
         int valueFontWidth = this.mc.field_1772.method_1727(value);
         Objects.requireNonNull(this.mc.field_1772);
         int valueHeight = 9;
         float textScale = text.scale() * 2.0F * class_3532.method_15363(1.0F / viewScale, 1.0F, 1000.0F);
         int textMinX = class_3532.method_15375((float)textX - (float)valueFontWidth / 2.0F * textScale);
         int textMaxX = class_3532.method_15375((float)textX + (float)valueFontWidth / 2.0F * textScale);
         int textMinZ = class_3532.method_15375((float)textZ - (float)valueHeight / 2.0F * textScale);
         int textMaxZ = class_3532.method_15375((float)textZ + (float)valueHeight / 2.0F * textScale);
         if (x >= textMinX && x <= textMaxX && z >= textMinZ && z <= textMaxZ) {
            toRemove.add(text);
         }
      }

      for(Text text : toRemove) {
         this.drawingCache.removeText(text.x(), text.z(), Globals.getCurrentDimensionId());
         if (this.operationCollector != null) {
            this.operationCollector.addErasedText(text);
         }
      }

   }

   public void setInProgressLine(final Line inProgressLine, final DrawingMode drawingMode) {
      switch (drawingMode) {
         case LINE_SEGMENT:
         case MEASUREMENT:
            this.inProgressLine = inProgressLine;
            break;
         case INFINITE_LINE:
            this.inProgressLine = inProgressLine.extrapolateToWorldBorder();
      }

   }

   public void removeInProgressLine() {
      this.inProgressLine = null;
   }

   public void setInProgressEllipse(final Ellipse ellipse) {
      this.inProgressEllipse = ellipse;
   }

   public void removeInProgressEllipse() {
      this.inProgressEllipse = null;
   }

   public Ellipse ellipseFromCenterAndRadii(final int centerX, final int centerZ, final int radiusPointX, final int radiusPointZ) {
      int radiusX = Math.abs(radiusPointX - centerX);
      int radiusZ = Math.abs(radiusPointZ - centerZ);
      return radiusX != 0 && radiusZ != 0 ? new Ellipse(centerX, centerZ, radiusX, radiusZ) : null;
   }

   public Ellipse snapEllipse(final int centerX, final int centerZ, final int radiusPointX, final int radiusPointZ, final double scale) {
      Ellipse ellipse = this.ellipseFromCenterAndRadii(centerX, centerZ, radiusPointX, radiusPointZ);
      if (ellipse == null) {
         return null;
      } else {
         int threshold = this.getSnapThreshold(scale);
         int dragLength = class_3532.method_15357(Math.sqrt(Math.pow((double)(radiusPointX - centerX), (double)2.0F) + Math.pow((double)(radiusPointZ - centerZ), (double)2.0F)));
         if (dragLength <= threshold) {
            return ellipse;
         } else {
            int radiusDelta = Math.abs(ellipse.radiusX() - ellipse.radiusZ());
            if (radiusDelta != 0 && radiusDelta < threshold) {
               int radius = Math.min(ellipse.radiusX(), ellipse.radiusZ());
               return new Ellipse(centerX, centerZ, radius, radius);
            } else {
               return ellipse;
            }
         }
      }
   }

   public void removeLine(final int x, final int z) {
      Object2IntMap<Line> lines = this.drawingCache.getLines(Globals.getCurrentDimensionId());
      int maxX = x + 16;
      int maxZ = z + 16;
      Line sqLine1 = new Line(x, z, maxX, z);
      Line sqLine2 = new Line(x, z, x, maxZ);
      Line sqLine3 = new Line(maxX, z, maxX, maxZ);
      Line sqLine4 = new Line(x, maxZ, maxX, maxZ);
      Object2IntOpenHashMap<Line> toRemove = new Object2IntOpenHashMap();
      ObjectIterator<Object2IntMap.Entry<Line>> it = Object2IntMaps.fastIterator(lines);

      while(it.hasNext()) {
         Object2IntMap.Entry<Line> entry = (Object2IntMap.Entry)it.next();
         Line line = (Line)entry.getKey();
         if ((line.x1() >= x || line.x2() >= x) && (line.z1() >= z || line.z2() >= z) && (line.x1() <= maxX || line.x2() <= maxX) && (line.z1() <= maxZ || line.z2() <= maxZ) && (this.linesIntersect(line, sqLine1) || this.linesIntersect(line, sqLine2) || this.linesIntersect(line, sqLine3) || this.linesIntersect(line, sqLine4))) {
            toRemove.put(line, entry.getIntValue());
         }
      }

      ObjectIterator var15 = toRemove.object2IntEntrySet().iterator();

      while(var15.hasNext()) {
         Object2IntMap.Entry<Line> entry = (Object2IntMap.Entry)var15.next();
         Line line = (Line)entry.getKey();
         this.drawingCache.removeLine(line, Globals.getCurrentDimensionId());
         if (this.operationCollector != null) {
            this.operationCollector.addErasedLine(line, entry.getIntValue());
         }
      }

   }

   public void removeEllipse(final int x, final int z) {
      int maxX = x + 16;
      int maxZ = z + 16;
      Object2IntMap<Ellipse> ellipses = this.drawingCache.getEllipses(Globals.getCurrentDimensionId());
      Object2IntOpenHashMap<Ellipse> toRemove = new Object2IntOpenHashMap();
      ObjectIterator var7 = ellipses.object2IntEntrySet().iterator();

      while(var7.hasNext()) {
         Object2IntMap.Entry<Ellipse> entry = (Object2IntMap.Entry)var7.next();
         Ellipse ellipse = (Ellipse)entry.getKey();
         if (this.ellipseOutlineIntersectsRectangle(ellipse, x, maxX, z, maxZ)) {
            toRemove.put(ellipse, entry.getIntValue());
         }
      }

      var7 = toRemove.object2IntEntrySet().iterator();

      while(var7.hasNext()) {
         Object2IntMap.Entry<Ellipse> entry = (Object2IntMap.Entry)var7.next();
         Ellipse ellipse = (Ellipse)entry.getKey();
         this.drawingCache.removeEllipse(ellipse, Globals.getCurrentDimensionId());
         if (this.operationCollector != null) {
            this.operationCollector.addErasedEllipse(ellipse, entry.getIntValue());
         }
      }

   }

   private boolean ellipseOutlineIntersectsRectangle(final Ellipse ellipse, final int minX, final int maxX, final int minZ, final int maxZ) {
      if (!ellipse.intersects(minX, maxX, minZ, maxZ)) {
         return false;
      } else {
         int closestX = class_3532.method_15340(ellipse.centerX(), minX, maxX);
         int closestZ = class_3532.method_15340(ellipse.centerZ(), minZ, maxZ);
         long furthestX = Math.max(Math.abs((long)minX - (long)ellipse.centerX()), Math.abs((long)maxX - (long)ellipse.centerX()));
         long furthestZ = Math.max(Math.abs((long)minZ - (long)ellipse.centerZ()), Math.abs((long)maxZ - (long)ellipse.centerZ()));
         double minNormalizedDistance = this.normalizedEllipseDistance(ellipse, (long)(closestX - ellipse.centerX()), (long)(closestZ - ellipse.centerZ()));
         double maxNormalizedDistance = this.normalizedEllipseDistance(ellipse, furthestX, furthestZ);
         return minNormalizedDistance <= (double)1.0F && maxNormalizedDistance >= (double)1.0F;
      }
   }

   private double normalizedEllipseDistance(final Ellipse ellipse, final long deltaX, final long deltaZ) {
      double normalizedX = (double)deltaX / (double)ellipse.radiusX();
      double normalizedZ = (double)deltaZ / (double)ellipse.radiusZ();
      return normalizedX * normalizedX + normalizedZ * normalizedZ;
   }

   private boolean linesIntersect(Line line1, Line line2) {
      double bx = (double)(line1.x2() - line1.x1());
      double bz = (double)(line1.z2() - line1.z1());
      double dx = (double)(line2.x2() - line2.x1());
      double dz = (double)(line2.z2() - line2.z1());
      double bDotDPerp = bx * dz - bz * dx;
      if (Math.round(bDotDPerp) == 0L) {
         return false;
      } else {
         int cx = line2.x1() - line1.x1();
         int cz = line2.z1() - line1.z1();
         double t = ((double)cx * dz - (double)cz * dx) / bDotDPerp;
         if (!(t < (double)0.0F) && !(t > (double)1.0F)) {
            double u = ((double)cx * bz - (double)cz * bx) / bDotDPerp;
            return u >= (double)0.0F && u <= (double)1.0F;
         } else {
            return false;
         }
      }
   }

   public Line snap(int x1, int z1, int x2, int z2, double scale) {
      int threshold = this.getSnapThreshold(scale);
      int len = class_3532.method_15357(Math.sqrt(Math.pow((double)(x2 - x1), (double)2.0F) + Math.pow((double)(z2 - z1), (double)2.0F)));
      if (len <= threshold) {
         return new Line(x1, z1, x2, z2);
      } else {
         int xDelta = Math.abs(x2 - x1);
         int zDelta = Math.abs(z2 - z1);
         if (xDelta < threshold) {
            return new Line(x1, z1, x1, z2);
         } else if (zDelta < threshold) {
            return new Line(x1, z1, x2, z1);
         } else {
            int dDelta = Math.abs(xDelta - zDelta);
            if (dDelta != 0 && dDelta < threshold) {
               if (zDelta < xDelta) {
                  int xSignum = x2 - x1 >= 0 ? 1 : -1;
                  return new Line(x1, z1, x1 + zDelta * xSignum, z2);
               }

               if (xDelta < zDelta) {
                  int zSignum = z2 - z1 >= 0 ? 1 : -1;
                  return new Line(x1, z1, x2, z1 + xDelta * zSignum);
               }
            }

            return new Line(x1, z1, x2, z2);
         }
      }
   }

   private int getSnapThreshold(final double scale) {
      double scalar = (double)1.0F / scale;
      return class_3532.method_15340(class_3532.method_15357((double)10.0F * scalar), 10, 1000);
   }

   public void clearAll() {
      this.drawingCache.getAllHighlightCaches().forEach((c) -> c.removeAllHighlights());
      this.drawingCache.getAllLinesCaches().forEach((c) -> c.removeAllLines());
      this.drawingCache.getAllEllipseCaches().forEach((c) -> c.removeAllEllipses());
      this.drawingCache.getAllTextsCaches().forEach((c) -> c.removeAllTexts());
      this.operationCollector = null;
      this.operationStack.clear();
   }

   public static record HighlightDrawingOperation(Long2LongMap replacedHighlights, LongSet addedHighlights, class_5321<class_1937> dimension) implements DrawingOperation {
      public void revert(Drawing drawing) {
         Iterator var2 = this.addedHighlights.iterator();

         while(var2.hasNext()) {
            Long chunkLong = (Long)var2.next();
            int chunkX = ChunkUtils.longToChunkX(chunkLong);
            int chunkZ = ChunkUtils.longToChunkZ(chunkLong);
            drawing.drawingCache.removeHighlight(chunkX, chunkZ, this.dimension);
         }

         var2 = this.replacedHighlights.long2LongEntrySet().iterator();

         while(var2.hasNext()) {
            Long2LongMap.Entry entry = (Long2LongMap.Entry)var2.next();
            int chunkX = ChunkUtils.longToChunkX(entry.getLongKey());
            int chunkZ = ChunkUtils.longToChunkZ(entry.getLongKey());
            drawing.drawingCache.addHighlight(chunkX, chunkZ, (int)entry.getLongValue(), this.dimension);
         }

      }
   }

   public static record LineDrawingOperation(Line line, Integer previousColor, class_5321<class_1937> dimension) implements DrawingOperation {
      public void revert(Drawing drawing) {
         if (this.previousColor == null) {
            drawing.drawingCache.removeLine(this.line, this.dimension);
         } else {
            drawing.drawingCache.addLine(this.line, this.previousColor, this.dimension);
         }

      }
   }

   public static record EllipseDrawingOperation(Ellipse ellipse, Integer previousColor, class_5321<class_1937> dimension) implements DrawingOperation {
      public void revert(Drawing drawing) {
         if (this.previousColor == null) {
            drawing.drawingCache.removeEllipse(this.ellipse, this.dimension);
         } else {
            drawing.drawingCache.addEllipse(this.ellipse, this.previousColor, this.dimension);
         }

      }
   }

   public static record TextDrawingOperation(Text text, Text previousText, class_5321<class_1937> dimension) implements DrawingOperation {
      public void revert(Drawing drawing) {
         if (this.previousText == null) {
            drawing.drawingCache.removeText(this.text.x(), this.text.z(), this.dimension);
         } else {
            drawing.drawingCache.addText(this.previousText, this.dimension);
         }

      }
   }

   public static record EraseOperation(Long2LongMap highlights, Object2IntMap<Line> lines, Object2IntMap<Ellipse> ellipses, List<Text> texts, class_5321<class_1937> dimension) implements DrawingOperation {
      public void revert(Drawing drawing) {
         ObjectIterator var2 = this.highlights.long2LongEntrySet().iterator();

         while(var2.hasNext()) {
            Long2LongMap.Entry entry = (Long2LongMap.Entry)var2.next();
            int chunkX = ChunkUtils.longToChunkX(entry.getLongKey());
            int chunkZ = ChunkUtils.longToChunkZ(entry.getLongKey());
            drawing.drawingCache.addHighlight(chunkX, chunkZ, (int)entry.getLongValue(), this.dimension);
         }

         var2 = this.lines.object2IntEntrySet().iterator();

         while(var2.hasNext()) {
            Object2IntMap.Entry<Line> entry = (Object2IntMap.Entry)var2.next();
            drawing.drawingCache.addLine((Line)entry.getKey(), entry.getIntValue(), this.dimension);
         }

         var2 = this.ellipses.object2IntEntrySet().iterator();

         while(var2.hasNext()) {
            Object2IntMap.Entry<Ellipse> entry = (Object2IntMap.Entry)var2.next();
            drawing.drawingCache.addEllipse((Ellipse)entry.getKey(), entry.getIntValue(), this.dimension);
         }

         for(Text text : this.texts) {
            drawing.drawingCache.addText(text, this.dimension);
         }

      }
   }

   public static class DrawingOperationCollector {
      private final Long2LongMap replacedHighlights = new Long2LongOpenHashMap();
      private final LongSet addedHighlights = new LongOpenHashSet();
      private final Long2LongMap erasedHighlights = new Long2LongOpenHashMap();
      private Line line;
      private Integer replacedLineColor;
      private final Object2IntMap<Line> erasedLines = new Object2IntOpenHashMap();
      private Ellipse ellipse;
      private Integer replacedEllipseColor;
      private final Object2IntMap<Ellipse> erasedEllipses = new Object2IntOpenHashMap();
      private Text text;
      private Text replacedText;
      private final List<Text> erasedTexts = new ArrayList();
      private final class_5321<class_1937> dimension;
      public boolean erase;

      public DrawingOperationCollector(class_5321<class_1937> dimension, boolean erase) {
         this.dimension = dimension;
         this.erase = erase;
      }

      public void addHighlight(final long chunkPos, final Long previousColor) {
         if (!this.replacedHighlights.containsKey(chunkPos) && !this.addedHighlights.contains(chunkPos)) {
            if (previousColor == null) {
               this.addedHighlights.add(chunkPos);
            } else {
               this.replacedHighlights.put(chunkPos, previousColor);
            }

         }
      }

      public void addErasedHighlight(final long chunkPos, final long color) {
         this.erasedHighlights.put(chunkPos, color);
      }

      public void addErasedHighlights(final Long2LongMap highlights) {
         this.erasedHighlights.putAll(highlights);
      }

      public void addLine(final Line line, final Integer previousColor) {
         this.line = line;
         this.replacedLineColor = previousColor;
      }

      public void addErasedLine(final Line line, final int color) {
         this.erasedLines.put(line, color);
      }

      public void addEllipse(final Ellipse ellipse, final Integer previousColor) {
         this.ellipse = ellipse;
         this.replacedEllipseColor = previousColor;
      }

      public void addErasedEllipse(final Ellipse ellipse, final int color) {
         this.erasedEllipses.put(ellipse, color);
      }

      public void addText(final Text text, final Text previousText) {
         this.text = text;
         this.replacedText = previousText;
      }

      public void addErasedText(final Text text) {
         this.erasedTexts.add(text);
      }

      public DrawingOperation collect() {
         if (this.erase) {
            return this.erasedHighlights.isEmpty() && this.erasedLines.isEmpty() && this.erasedEllipses.isEmpty() && this.erasedTexts.isEmpty() ? null : new EraseOperation(this.erasedHighlights, this.erasedLines, this.erasedEllipses, this.erasedTexts, this.dimension);
         } else if (this.replacedHighlights.isEmpty() && this.addedHighlights.isEmpty()) {
            if (this.line != null) {
               return new LineDrawingOperation(this.line, this.replacedLineColor, this.dimension);
            } else if (this.ellipse != null) {
               return new EllipseDrawingOperation(this.ellipse, this.replacedEllipseColor, this.dimension);
            } else {
               return this.text != null ? new TextDrawingOperation(this.text, this.replacedText, this.dimension) : null;
            }
         } else {
            return new HighlightDrawingOperation(this.replacedHighlights, this.addedHighlights, this.dimension);
         }
      }
   }

   public interface DrawingOperation {
      void revert(Drawing drawing);
   }
}
