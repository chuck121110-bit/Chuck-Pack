package xaero.hud.minimap.radar.icon.definition.form.item;

import javax.annotation.Nullable;
import net.minecraft.class_2960;
import xaero.hud.minimap.radar.icon.definition.RadarIconDefinition;
import xaero.hud.minimap.radar.icon.definition.form.RadarIconBasicForms;
import xaero.hud.minimap.radar.icon.definition.form.RadarIconForm;
import xaero.hud.minimap.radar.icon.definition.form.type.RadarIconFormType;

public class RadarIconItemForm extends RadarIconForm {
   private final class_2960 itemKey;

   public RadarIconItemForm(RadarIconFormType type, @Nullable class_2960 itemKey) {
      super(type);
      this.itemKey = itemKey;
   }

   public class_2960 getItemKey() {
      return this.itemKey;
   }

   public static RadarIconItemForm read(RadarIconFormType type, String[] args, RadarIconDefinition iconDefinition) {
      if (args.length == 1) {
         return RadarIconBasicForms.SELF_ITEM;
      } else if (args.length > 3) {
         return null;
      } else {
         class_2960 itemKey = args.length == 2 ? class_2960.method_60654(args[1]) : class_2960.method_60655(args[1], args[2]);
         return new RadarIconItemForm(type, itemKey);
      }
   }
}
