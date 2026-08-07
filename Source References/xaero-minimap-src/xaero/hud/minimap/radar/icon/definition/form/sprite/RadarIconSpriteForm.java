package xaero.hud.minimap.radar.icon.definition.form.sprite;

import net.minecraft.class_2960;
import xaero.hud.minimap.radar.icon.definition.RadarIconDefinition;
import xaero.hud.minimap.radar.icon.definition.form.RadarIconForm;
import xaero.hud.minimap.radar.icon.definition.form.type.RadarIconFormType;

public class RadarIconSpriteForm extends RadarIconForm {
   private final class_2960 spriteLocation;

   public RadarIconSpriteForm(RadarIconFormType type, class_2960 spriteLocation) {
      super(type);
      this.spriteLocation = spriteLocation;
   }

   public class_2960 getSpriteLocation() {
      return this.spriteLocation;
   }

   public static RadarIconSpriteForm read(RadarIconFormType type, String[] args, RadarIconDefinition iconDefinition) {
      if (args.length != 2) {
         return null;
      } else {
         class_2960 sprite = class_2960.method_60655("xaerominimap", "entity/icon/sprite/" + args[1]);
         return new RadarIconSpriteForm(type, sprite);
      }
   }
}
