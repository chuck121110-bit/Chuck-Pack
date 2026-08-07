package xaero.hud.minimap.radar.icon.cache.id.variant;

import java.util.Objects;
import net.minecraft.class_2960;
import net.minecraft.class_9273;

public class IronGolemVariant {
   private final class_2960 texture;
   private final class_9273.class_4621 cracks;

   public IronGolemVariant(class_2960 texture, class_9273.class_4621 cracks) {
      this.texture = texture;
      this.cracks = cracks;
   }

   public String toString() {
      String var10000 = String.valueOf(this.texture);
      return var10000 + "%" + String.valueOf(this.cracks);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         IronGolemVariant that = (IronGolemVariant)o;
         return Objects.equals(this.texture, that.texture) && this.cracks == that.cracks;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.texture, this.cracks});
   }
}
