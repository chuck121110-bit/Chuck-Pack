package xaero.hud.minimap.radar.icon.cache.id;

import java.util.Objects;
import xaero.hud.minimap.radar.icon.cache.id.armor.RadarIconArmor;

public class RadarIconKey {
   private final Object variant;
   private final RadarIconArmor armor;

   public RadarIconKey(Object variant, RadarIconArmor armor) {
      this.variant = variant;
      this.armor = armor;
   }

   public Object getVariant() {
      return this.variant;
   }

   public String toString() {
      String var10000 = String.valueOf(this.variant);
      return "RadarIconKey{" + var10000 + ", " + String.valueOf(this.armor) + "}";
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         RadarIconKey that = (RadarIconKey)o;
         return this.variant.equals(that.variant) && Objects.equals(this.armor, that.armor);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.variant, this.armor});
   }
}
