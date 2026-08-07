package xaero.hud.minimap.radar.icon.definition.form.type;

import xaero.hud.minimap.radar.icon.definition.RadarIconDefinition;
import xaero.hud.minimap.radar.icon.definition.form.RadarIconForm;

public interface IRadarIconFormReader {
   RadarIconForm read(RadarIconFormType var1, String[] var2, RadarIconDefinition var3);
}
