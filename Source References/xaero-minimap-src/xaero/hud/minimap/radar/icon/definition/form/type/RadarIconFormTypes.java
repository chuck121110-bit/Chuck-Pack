package xaero.hud.minimap.radar.icon.definition.form.type;

import java.util.HashMap;
import java.util.Map;
import xaero.hud.minimap.radar.icon.RadarIconManager;
import xaero.hud.minimap.radar.icon.creator.render.form.IRadarIconFormPrerenderer;
import xaero.hud.minimap.radar.icon.creator.render.form.item.RadarIconItemFormPrerenderer;
import xaero.hud.minimap.radar.icon.creator.render.form.model.RadarIconModelFormPrerenderer;
import xaero.hud.minimap.radar.icon.creator.render.form.sprite.RadarIconSpriteFormPrerenderer;
import xaero.hud.minimap.radar.icon.definition.form.RadarIconBasicForms;
import xaero.hud.minimap.radar.icon.definition.form.item.RadarIconItemForm;
import xaero.hud.minimap.radar.icon.definition.form.model.RadarIconModelForm;
import xaero.hud.minimap.radar.icon.definition.form.sprite.RadarIconSpriteForm;

public class RadarIconFormTypes {
   private static final Map<String, RadarIconFormType> ALL = new HashMap();
   public static final RadarIconFormType MODEL;
   public static final RadarIconFormType NORMAL_SPRITE;
   public static final RadarIconFormType OUTLINED_SPRITE;
   public static final RadarIconFormType OLD_SPRITE;
   public static final RadarIconFormType ITEM;
   public static final RadarIconFormType DOT;

   public static RadarIconFormType readType(String typeString) {
      return (RadarIconFormType)ALL.get(typeString);
   }

   static {
      MODEL = (new RadarIconFormType("model", RadarIconModelForm::read, new RadarIconModelFormPrerenderer(), RadarIconManager.FAILED)).addTo(ALL);
      NORMAL_SPRITE = (new RadarIconFormType("normal_sprite", RadarIconSpriteForm::read, new RadarIconSpriteFormPrerenderer(false, false), RadarIconManager.FAILED)).addTo(ALL);
      OUTLINED_SPRITE = (new RadarIconFormType("outlined_sprite", RadarIconSpriteForm::read, new RadarIconSpriteFormPrerenderer(false, true), RadarIconManager.FAILED)).addTo(ALL);
      OLD_SPRITE = (new RadarIconFormType("sprite", RadarIconSpriteForm::read, new RadarIconSpriteFormPrerenderer(true, false), RadarIconManager.FAILED)).addTo(ALL);
      ITEM = (new RadarIconFormType("item", RadarIconItemForm::read, new RadarIconItemFormPrerenderer(), RadarIconManager.DOT)).addTo(ALL);
      DOT = (new RadarIconFormType("dot", (rid, type, args) -> RadarIconBasicForms.DOT, (IRadarIconFormPrerenderer)null, RadarIconManager.FAILED)).addTo(ALL);
   }
}
