package xaero.hud.minimap.radar.icon.definition.form;

import net.minecraft.class_2960;
import xaero.hud.minimap.radar.icon.definition.form.item.RadarIconItemForm;
import xaero.hud.minimap.radar.icon.definition.form.model.RadarIconModelForm;
import xaero.hud.minimap.radar.icon.definition.form.model.config.RadarIconModelConfig;
import xaero.hud.minimap.radar.icon.definition.form.type.RadarIconFormTypes;

public class RadarIconBasicForms {
   public static final RadarIconModelForm DEFAULT_MODEL;
   public static final RadarIconItemForm SELF_ITEM;
   public static final RadarIconForm DOT;

   static {
      DEFAULT_MODEL = new RadarIconModelForm(RadarIconFormTypes.MODEL, (RadarIconModelConfig)null);
      SELF_ITEM = new RadarIconItemForm(RadarIconFormTypes.ITEM, (class_2960)null);
      DOT = new RadarIconForm(RadarIconFormTypes.DOT);
   }
}
