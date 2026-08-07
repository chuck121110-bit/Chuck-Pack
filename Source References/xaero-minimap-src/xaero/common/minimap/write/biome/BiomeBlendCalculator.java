package xaero.common.minimap.write.biome;

import net.minecraft.class_1920;
import net.minecraft.class_1937;
import net.minecraft.class_1959;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_3568;
import net.minecraft.class_3610;
import net.minecraft.class_6539;
import net.minecraft.class_6880;

public class BiomeBlendCalculator implements class_1920 {
   private class_1937 original;
   private int startI;
   private int endI;
   private int startJ;
   private int endJ;

   public void prepare(class_1937 original, boolean biomeBlending) {
      this.original = original;
      this.startI = this.endI = this.startJ = this.endJ = 0;
      if (biomeBlending) {
         this.startI = -1;
         this.endI = 1;
         this.startJ = -1;
         this.endJ = 1;
      }

   }

   public class_2586 method_8321(class_2338 blockPos) {
      return this.original.method_8321(blockPos);
   }

   public class_2680 method_8320(class_2338 blockPos) {
      return this.original.method_8320(blockPos);
   }

   public class_3610 method_8316(class_2338 blockPos) {
      return this.original.method_8316(blockPos);
   }

   public float method_24852(class_2350 direction, boolean bl) {
      return this.original.method_24852(direction, bl);
   }

   public class_3568 method_22336() {
      return this.original.method_22336();
   }

   public int method_31605() {
      return this.original.method_31605();
   }

   public int method_31607() {
      return this.original.method_31607();
   }

   public int method_23752(class_2338 blockPos, class_6539 colorResolver) {
      class_2338.class_2339 mutableBlockPos = new class_2338.class_2339();
      int x = blockPos.method_10263();
      int y = blockPos.method_10264();
      int z = blockPos.method_10260();
      int redAccumulator = 0;
      int greenAccumulator = 0;
      int blueAccumulator = 0;
      class_1937 original = this.original;
      int total = 0;

      for(int i = this.startI; i <= this.endI; ++i) {
         for(int j = this.startJ; j <= this.endJ; ++j) {
            if (i == 0 || j == 0) {
               mutableBlockPos.method_10103(x + i, y, z + j);
               class_6880<class_1959> biomeHolder = original.method_23753(mutableBlockPos);
               class_1959 biome = biomeHolder == null ? null : (class_1959)biomeHolder.comp_349();
               if (biome != null) {
                  int colorSample = colorResolver.getColor(biome, (double)mutableBlockPos.method_10263(), (double)mutableBlockPos.method_10260());
                  redAccumulator += colorSample & 16711680;
                  greenAccumulator += colorSample & '\uff00';
                  blueAccumulator += colorSample & 255;
                  ++total;
               }
            }
         }
      }

      int red = redAccumulator / total;
      int green = greenAccumulator / total;
      int blue = blueAccumulator / total;
      return -16777216 | red & 16711680 | green & '\uff00' | blue;
   }
}
