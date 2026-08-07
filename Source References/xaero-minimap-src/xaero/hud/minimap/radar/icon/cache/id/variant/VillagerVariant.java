package xaero.hud.minimap.radar.icon.cache.id.variant;

import java.util.Objects;
import net.minecraft.class_2960;

public class VillagerVariant {
   private final class_2960 texture;
   private final boolean baby;
   private final class_2960 type;
   private final class_2960 profession;
   private final int level;

   public VillagerVariant(class_2960 texture, boolean baby, class_2960 type, class_2960 profession, int level) {
      this.texture = texture;
      this.baby = baby;
      this.type = type;
      this.profession = profession;
      this.level = level;
   }

   public String toString() {
      return this.texture + "%" + this.baby + "%" + this.type + "%" + this.profession + "%" + this.level;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         VillagerVariant that = (VillagerVariant)o;
         return this.baby == that.baby && this.level == that.level && Objects.equals(this.texture, that.texture) && this.type.equals(that.type) && this.profession.equals(that.profession);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.texture, this.baby, this.type, this.profession, this.level});
   }
}
