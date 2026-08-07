package xaero.hud.minimap.radar.icon.cache.id.armor;

import java.util.Objects;
import net.minecraft.class_1792;
import net.minecraft.class_8054;
import net.minecraft.class_8056;

public class RadarIconArmor {
   private final class_1792 armor;
   private final class_8054 trimMaterial;
   private final class_8056 trimPattern;

   public RadarIconArmor(class_1792 armor, class_8054 trimMaterial, class_8056 trimPattern) {
      this.armor = armor;
      this.trimMaterial = trimMaterial;
      this.trimPattern = trimPattern;
   }

   public String toString() {
      String var10000 = String.valueOf(this.armor);
      return "RadarIconArmor{" + var10000 + ", " + String.valueOf(this.trimMaterial) + ", " + String.valueOf(this.trimPattern) + "}";
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         RadarIconArmor that = (RadarIconArmor)o;
         return this.armor.equals(that.armor) && Objects.equals(this.trimMaterial, that.trimMaterial) && Objects.equals(this.trimPattern, that.trimPattern);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.armor, this.trimMaterial, this.trimPattern});
   }
}
