package xaero.map.block;

import net.minecraft.class_1920;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_3568;
import net.minecraft.class_3610;
import net.minecraft.class_6539;
import org.jetbrains.annotations.Nullable;
import xaero.map.MapProcessor;

public class SingleBlockAndTintGetter implements class_1920 {
   private final class_2338 pos;
   private final class_2680 state;
   private final MapProcessor mapProcessor;

   public SingleBlockAndTintGetter(class_2338 pos, class_2680 state, MapProcessor mapProcessor) {
      this.pos = pos;
      this.state = state;
      this.mapProcessor = mapProcessor;
   }

   public float method_24852(class_2350 var1, boolean var2) {
      return 1.0F;
   }

   public class_3568 method_22336() {
      return this.mapProcessor.getWorld().method_22336();
   }

   public int method_23752(class_2338 var1, class_6539 var2) {
      return -1;
   }

   public @Nullable class_2586 method_8321(class_2338 var1) {
      return null;
   }

   public class_2680 method_8320(class_2338 var1) {
      return var1.equals(this.pos) ? this.state : class_2246.field_10124.method_9564();
   }

   public class_3610 method_8316(class_2338 var1) {
      return this.method_8320(var1).method_26227();
   }

   public int method_31605() {
      return 64;
   }

   public int method_31607() {
      return this.pos.method_10264() >> 4 << 4;
   }
}
