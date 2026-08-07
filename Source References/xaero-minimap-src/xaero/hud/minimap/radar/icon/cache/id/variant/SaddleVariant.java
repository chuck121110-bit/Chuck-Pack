package xaero.hud.minimap.radar.icon.cache.id.variant;

import java.util.Objects;
import net.minecraft.class_2960;

public class SaddleVariant {
   private final class_2960 texture;
   private final boolean saddled;

   public SaddleVariant(class_2960 texture, boolean saddled) {
      this.texture = texture;
      this.saddled = saddled;
   }

   public String toString() {
      String var10000 = String.valueOf(this.texture);
      return var10000 + "%" + this.saddled;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         SaddleVariant that = (SaddleVariant)o;
         return this.saddled == that.saddled && Objects.equals(this.texture, that.texture);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.texture, this.saddled});
   }
}
