package xaeroplus.util;

public enum DrawingMode {
   LINE_SEGMENT,
   INFINITE_LINE,
   HIGHLIGHT,
   ELLIPSE,
   TEXT,
   MEASUREMENT;

   // $FF: synthetic method
   private static DrawingMode[] $values() {
      return new DrawingMode[]{LINE_SEGMENT, INFINITE_LINE, HIGHLIGHT, ELLIPSE, TEXT, MEASUREMENT};
   }
}
