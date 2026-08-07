package xaero.hud.minimap.radar.icon.definition;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_2960;

public class RadarIconDefinitionManager {
   private final Map<class_2960, RadarIconDefinition> definitions = new HashMap();
   private final RadarIconDefinitionReloader reloader = new RadarIconDefinitionReloader();

   public RadarIconDefinition get(class_2960 key) {
      return (RadarIconDefinition)this.definitions.get(key);
   }

   public void reloadResources() {
      this.reloader.reloadResources(this.definitions);
   }
}
