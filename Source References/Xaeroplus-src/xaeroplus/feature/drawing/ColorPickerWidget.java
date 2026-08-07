package xaeroplus.feature.drawing;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.class_11909;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_339;
import net.minecraft.class_3532;
import net.minecraft.class_5250;
import net.minecraft.class_6381;
import net.minecraft.class_6382;
import xaeroplus.util.Color;
import xaeroplus.util.ColorHelper;

public class ColorPickerWidget extends class_339 {
   private static final float HEIGHT_PROPORTION = 0.8888889F;
   private static final float PADDING_PROPORTION = 0.011111111F;
   private static final float SELECTOR_GAP_PROPORTION = 0.022222223F;
   private static final float HUE_BAR_PROPORTION = 0.06666667F;
   private static final float ALPHA_BAR_PROPORTION = 0.06666667F;
   private static final float CHECKER_PROPORTION = 0.033333335F;
   private static final float COLOR_SELECTOR_OUTER_PROPORTION = 0.055555556F;
   private static final float COLOR_SELECTOR_INNER_PROPORTION = 0.033333335F;
   private static final float BAR_SELECTOR_THICKNESS_PROPORTION = 0.033333335F;
   private static final float SELECTOR_EXTENSION_PROPORTION = 0.011111111F;
   private final Consumer<Color> onColorChanged;
   private final int padding;
   private final int selectorGap;
   private final int hueBarHeight;
   private final int alphaBarWidth;
   private final int checkerSize;
   private final int colorSelectorOuterSize;
   private final int colorSelectorInnerSize;
   private final int barSelectorThickness;
   private final int selectorExtension;
   private float hue;
   private float saturation;
   private float brightness;
   private int colorAlpha;
   private Selector activeSelector;

   public ColorPickerWidget(final int x, final int y, final int size, final Color color, final Consumer<Color> onColorChanged) {
      super(x, y, size, Math.round((float)size * 0.8888889F), class_2561.method_43471("xaeroplus.gui.world_map.draw_color"));
      this.activeSelector = ColorPickerWidget.Selector.NONE;
      this.onColorChanged = (Consumer)Objects.requireNonNull(onColorChanged);
      this.padding = scaled(size, 0.011111111F);
      this.selectorGap = scaled(size, 0.022222223F);
      this.hueBarHeight = scaled(size, 0.06666667F);
      this.alphaBarWidth = scaled(size, 0.06666667F);
      this.checkerSize = scaled(size, 0.033333335F);
      this.colorSelectorOuterSize = Math.max(3, scaled(size, 0.055555556F));
      this.colorSelectorInnerSize = Math.min(this.colorSelectorOuterSize - 2, scaled(size, 0.033333335F));
      this.barSelectorThickness = Math.max(3, scaled(size, 0.033333335F));
      this.selectorExtension = scaled(size, 0.011111111F);
      this.setColor(color, false);
   }

   private static int scaled(final int size, final float proportion) {
      return Math.max(1, Math.round((float)size * proportion));
   }

   public Color getColor() {
      java.awt.Color rgb = java.awt.Color.getHSBColor(this.hue, this.saturation, this.brightness);
      return new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), this.colorAlpha);
   }

   void setColor(final Color color, final boolean notify) {
      Objects.requireNonNull(color);
      float[] hsb = java.awt.Color.RGBtoHSB(color.r(), color.g(), color.b(), (float[])null);
      this.hue = hsb[0];
      this.saturation = hsb[1];
      this.brightness = hsb[2];
      this.colorAlpha = color.a();
      if (notify) {
         this.onColorChanged.accept(this.getColor());
      }

   }

   protected void method_48579(final class_332 guiGraphics, final int mouseX, final int mouseY, final float partialTick) {
      guiGraphics.method_25294(this.method_46426(), this.method_46427(), this.method_46426() + this.field_22758, this.method_46427() + this.field_22759, ColorHelper.getColor(0, 0, 0, 255));
      this.renderColorGradient(guiGraphics);
      this.renderHueBar(guiGraphics);
      this.renderAlphaBar(guiGraphics);
      this.renderSelectors(guiGraphics);
   }

   void renderColorGradient(final class_332 guiGraphics) {
      int gradientWidth = this.gradientWidth();

      for(int xOffset = 0; xOffset < gradientWidth; ++xOffset) {
         float columnSaturation = (float)xOffset / (float)(gradientWidth - 1);
         int topColor = java.awt.Color.HSBtoRGB(this.hue, columnSaturation, 1.0F);
         guiGraphics.method_25296(this.gradientX() + xOffset, this.gradientY(), this.gradientX() + xOffset + 1, this.gradientY() + this.gradientHeight(), topColor, ColorHelper.getColor(0, 0, 0, 255));
      }

   }

   void renderHueBar(final class_332 guiGraphics) {
      int hueX = this.gradientX();
      int hueY = this.hueY();
      int hueWidth = this.gradientWidth();

      for(int xOffset = 0; xOffset < hueWidth; ++xOffset) {
         float columnHue = (float)xOffset / (float)(hueWidth - 1);
         guiGraphics.method_25294(hueX + xOffset, hueY, hueX + xOffset + 1, hueY + this.hueBarHeight, java.awt.Color.HSBtoRGB(columnHue, 1.0F, 1.0F));
      }

   }

   void renderAlphaBar(final class_332 guiGraphics) {
      int alphaX = this.alphaX();
      int alphaY = this.gradientY();
      int alphaHeight = this.gradientHeight();

      for(int yOffset = 0; yOffset < alphaHeight; yOffset += this.checkerSize) {
         for(int xOffset = 0; xOffset < this.alphaBarWidth; xOffset += this.checkerSize) {
            int checkerColor = (xOffset / this.checkerSize + yOffset / this.checkerSize) % 2 == 0 ? ColorHelper.getColor(255, 255, 255, 255) : ColorHelper.getColor(119, 119, 199, 255);
            guiGraphics.method_25294(alphaX + xOffset, alphaY + yOffset, Math.min(alphaX + xOffset + this.checkerSize, alphaX + this.alphaBarWidth), Math.min(alphaY + yOffset + this.checkerSize, alphaY + alphaHeight), checkerColor);
         }
      }

      int rgb = java.awt.Color.HSBtoRGB(this.hue, this.saturation, this.brightness) & ColorHelper.getColor(255, 255, 255, 0);
      guiGraphics.method_25296(alphaX, alphaY, alphaX + this.alphaBarWidth, alphaY + alphaHeight, ColorHelper.getColorWithAlpha(rgb, 255), rgb);
   }

   void renderSelectors(final class_332 guiGraphics) {
      int selectedX = this.gradientX() + Math.round(this.saturation * (float)(this.gradientWidth() - 1));
      int selectedY = this.gradientY() + Math.round((1.0F - this.brightness) * (float)(this.gradientHeight() - 1));
      renderOutline(guiGraphics, selectedX - this.colorSelectorOuterSize / 2, selectedY - this.colorSelectorOuterSize / 2, this.colorSelectorOuterSize, this.colorSelectorOuterSize, ColorHelper.getColor(0, 0, 0, 255));
      renderOutline(guiGraphics, selectedX - this.colorSelectorInnerSize / 2, selectedY - this.colorSelectorInnerSize / 2, this.colorSelectorInnerSize, this.colorSelectorInnerSize, ColorHelper.getColor(255, 255, 255, 255));
      int hueSelectorX = this.gradientX() + Math.round(this.hue * (float)(this.gradientWidth() - 1));
      renderOutline(guiGraphics, hueSelectorX - this.barSelectorThickness / 2, this.hueY() - this.selectorExtension, this.barSelectorThickness, this.hueBarHeight + this.selectorExtension * 2, ColorHelper.getColor(255, 255, 255, 255));
      guiGraphics.method_51742(hueSelectorX, this.hueY() - this.selectorExtension, this.hueY() + this.hueBarHeight + this.selectorExtension - 1, ColorHelper.getColor(0, 0, 0, 255));
      int alphaSelectorY = this.gradientY() + Math.round((1.0F - (float)this.colorAlpha / 255.0F) * (float)(this.gradientHeight() - 1));
      renderOutline(guiGraphics, this.alphaX() - this.selectorExtension, alphaSelectorY - this.barSelectorThickness / 2, this.alphaBarWidth + this.selectorExtension * 2, this.barSelectorThickness, ColorHelper.getColor(255, 255, 255, 255));
      guiGraphics.method_51738(this.alphaX() - this.selectorExtension, this.alphaX() + this.alphaBarWidth + this.selectorExtension - 1, alphaSelectorY, ColorHelper.getColor(0, 0, 0, 255));
   }

   private static void renderOutline(final class_332 guiGraphics, final int x, final int y, final int width, final int height, final int color) {
      guiGraphics.method_25294(x, y, x + width, y + 1, color);
      guiGraphics.method_25294(x, y + height - 1, x + width, y + height, color);
      guiGraphics.method_25294(x, y + 1, x + 1, y + height - 1, color);
      guiGraphics.method_25294(x + width - 1, y + 1, x + width, y + height - 1, color);
   }

   public void method_25348(final class_11909 event, final boolean doubleClick) {
      this.activeSelector = this.selectorAt(event.comp_4798(), event.comp_4799());
      this.updateSelection(event.comp_4798(), event.comp_4799());
   }

   protected void method_25349(final class_11909 event, final double dragX, final double dragY) {
      this.updateSelection(event.comp_4798(), event.comp_4799());
   }

   public void method_25357(final class_11909 event) {
      this.activeSelector = ColorPickerWidget.Selector.NONE;
   }

   Selector selectorAt(final double mouseX, final double mouseY) {
      if (this.contains(mouseX, mouseY, this.gradientX(), this.gradientY(), this.gradientWidth(), this.gradientHeight())) {
         return ColorPickerWidget.Selector.COLOR;
      } else if (this.contains(mouseX, mouseY, this.gradientX(), this.hueY(), this.gradientWidth(), this.hueBarHeight)) {
         return ColorPickerWidget.Selector.HUE;
      } else {
         return this.contains(mouseX, mouseY, this.alphaX(), this.gradientY(), this.alphaBarWidth, this.gradientHeight()) ? ColorPickerWidget.Selector.ALPHA : ColorPickerWidget.Selector.NONE;
      }
   }

   boolean contains(final double mouseX, final double mouseY, final int x, final int y, final int areaWidth, final int areaHeight) {
      return mouseX >= (double)x && mouseX < (double)(x + areaWidth) && mouseY >= (double)y && mouseY < (double)(y + areaHeight);
   }

   void updateSelection(final double mouseX, final double mouseY) {
      switch (this.activeSelector.ordinal()) {
         case 1:
            this.saturation = this.normalized(mouseX, this.gradientX(), this.gradientWidth());
            this.brightness = 1.0F - this.normalized(mouseY, this.gradientY(), this.gradientHeight());
            break;
         case 2:
            this.hue = this.normalized(mouseX, this.gradientX(), this.gradientWidth());
            break;
         case 3:
            this.colorAlpha = Math.round((1.0F - this.normalized(mouseY, this.gradientY(), this.gradientHeight())) * 255.0F);
      }

      if (this.activeSelector != ColorPickerWidget.Selector.NONE) {
         this.onColorChanged.accept(this.getColor());
      }

   }

   float normalized(final double position, final int start, final int length) {
      return class_3532.method_15363((float)((position - (double)start) / (double)(length - 1)), 0.0F, 1.0F);
   }

   int gradientX() {
      return this.method_46426() + this.padding;
   }

   int gradientY() {
      return this.method_46427() + this.padding;
   }

   int gradientWidth() {
      return this.field_22758 - this.padding * 2 - this.selectorGap - this.alphaBarWidth;
   }

   int gradientHeight() {
      return this.field_22759 - this.padding * 2 - this.selectorGap - this.hueBarHeight;
   }

   int hueY() {
      return this.gradientY() + this.gradientHeight() + this.selectorGap;
   }

   int alphaX() {
      return this.gradientX() + this.gradientWidth() + this.selectorGap;
   }

   protected void method_47399(final class_6382 narrationElementOutput) {
      Color color = this.getColor();
      class_6381 var10001 = class_6381.field_33788;
      class_5250 var10002 = class_2561.method_43471("xaeroplus.gui.world_map.draw_color");
      int var10003 = color.r();
      narrationElementOutput.method_37034(var10001, var10002.method_10852(class_2561.method_43470(" RGBA " + var10003 + ", " + color.g() + ", " + color.b() + ", " + color.a())));
   }

   static enum Selector {
      NONE,
      COLOR,
      HUE,
      ALPHA;

      // $FF: synthetic method
      private static Selector[] $values() {
         return new Selector[]{NONE, COLOR, HUE, ALPHA};
      }
   }
}
