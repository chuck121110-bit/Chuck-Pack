package xaero.map.gui;

import net.minecraft.class_2561;
import net.minecraft.class_2583;
import xaero.lib.client.gui.widget.Tooltip;

/** @deprecated */
@Deprecated
public class CursorBox extends Tooltip {
   public CursorBox(String code) {
      super(code);
   }

   public CursorBox(String code, class_2583 codeStyle) {
      super(code, codeStyle);
   }

   public CursorBox(String code, class_2583 codeStyle, boolean flippedByDefault) {
      super(code, codeStyle, flippedByDefault);
   }

   public CursorBox(class_2561 directText) {
      super(directText);
   }

   public CursorBox(class_2561 directText, boolean flippedByDefault) {
      super(directText, flippedByDefault);
   }

   public CursorBox(int size) {
      super(size);
   }
}
