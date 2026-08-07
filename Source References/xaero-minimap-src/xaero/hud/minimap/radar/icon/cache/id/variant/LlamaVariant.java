package xaero.hud.minimap.radar.icon.cache.id.variant;

import java.util.Objects;
import net.minecraft.class_1792;
import net.minecraft.class_2960;

public class LlamaVariant {
   private final class_2960 texture;
   private final boolean trader;
   private final class_1792 swag;

   public LlamaVariant(class_2960 texture, boolean trader, class_1792 swag) {
      this.texture = texture;
      this.trader = trader;
      this.swag = swag;
   }

   public String toString() {
      String var10000 = String.valueOf(this.texture);
      return var10000 + "%" + this.trader + "%" + String.valueOf(this.swag);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         LlamaVariant that = (LlamaVariant)o;
         return this.trader == that.trader && Objects.equals(this.texture, that.texture) && this.swag == that.swag;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.texture, this.trader, this.swag});
   }
}
