package xaero.hud.minimap.render;

import net.minecraft.class_11256;
import net.minecraft.class_8030;
import org.jetbrains.annotations.Nullable;
import xaero.lib.client.graphics.XaeroBufferProvider;

public class MinimapPipRenderState implements class_11256 {
   public static final int HORIZONTAL_PADDING = 20;
   public static final int VERTICAL_PADDING = 5;
   private boolean shouldRender;
   private int x;
   private int y;
   private int width;
   private int height;
   private double scale;
   private float minimapScale;
   private int size;
   private int boxSize;
   private float partial;
   private class_8030 rectangle;
   private XaeroBufferProvider cvc;
   public boolean prepared;

   public MinimapPipRenderState update(int x, int y, int width, int height, double scale, float minimapScale, int size, int boxSize, float partial, XaeroBufferProvider cvc) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.scale = scale;
      this.minimapScale = minimapScale;
      this.size = size;
      this.boxSize = boxSize;
      this.partial = partial;
      this.cvc = cvc;
      this.rectangle = new class_8030(this.comp_4122(), this.comp_4123(), this.comp_4124() - this.comp_4122(), this.comp_4125() - this.comp_4123());
      this.prepared = false;
      return this;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public double getScale() {
      return this.scale;
   }

   public float getMinimapScale() {
      return this.minimapScale;
   }

   public int getSize() {
      return this.size;
   }

   public int getBoxSize() {
      return this.boxSize;
   }

   public float getPartial() {
      return this.partial;
   }

   public XaeroBufferProvider getCvc() {
      return this.cvc;
   }

   public int getScaledHorizontalPadding() {
      return (int)(20.0F * this.minimapScale);
   }

   public int getScaledVerticalPadding() {
      return (int)(5.0F * this.minimapScale);
   }

   public int comp_4122() {
      return this.x - this.getScaledHorizontalPadding();
   }

   public int comp_4123() {
      return this.y - this.getScaledVerticalPadding();
   }

   public int comp_4124() {
      return this.x + this.boxSize + this.getScaledHorizontalPadding();
   }

   public int comp_4125() {
      return this.y + this.boxSize + this.getScaledVerticalPadding();
   }

   public float comp_4133() {
      return 1.0F;
   }

   public @Nullable class_8030 comp_4128() {
      return null;
   }

   public @Nullable class_8030 comp_4274() {
      return this.rectangle;
   }

   public static class Enlarged extends MinimapPipRenderState {
   }
}
