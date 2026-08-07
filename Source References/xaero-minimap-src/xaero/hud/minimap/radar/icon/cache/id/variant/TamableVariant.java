package xaero.hud.minimap.radar.icon.cache.id.variant;

import java.util.Objects;
import net.minecraft.class_2960;

public class TamableVariant {
   private final class_2960 texture;
   private final boolean tame;

   public TamableVariant(class_2960 texture, boolean tame) {
      this.texture = texture;
      this.tame = tame;
   }

   public String toString() {
      String var10000 = String.valueOf(this.texture);
      return var10000 + "%" + this.tame;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         TamableVariant that = (TamableVariant)o;
         return this.tame == that.tame && Objects.equals(this.texture, that.texture);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.texture, this.tame});
   }
}
