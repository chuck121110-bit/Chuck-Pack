package xaero.hud.minimap.radar.icon.definition.form.model;

import java.util.Arrays;
import org.apache.logging.log4j.Logger;
import xaero.hud.minimap.MinimapLogs;
import xaero.hud.minimap.radar.icon.definition.RadarIconDefinition;
import xaero.hud.minimap.radar.icon.definition.form.RadarIconBasicForms;
import xaero.hud.minimap.radar.icon.definition.form.RadarIconForm;
import xaero.hud.minimap.radar.icon.definition.form.model.config.RadarIconModelConfig;
import xaero.hud.minimap.radar.icon.definition.form.type.RadarIconFormType;

public class RadarIconModelForm extends RadarIconForm {
   private final RadarIconModelConfig config;

   public RadarIconModelForm(RadarIconFormType type, RadarIconModelConfig config) {
      super(type);
      this.config = config;
   }

   public RadarIconModelConfig getConfig() {
      return this.config;
   }

   public static RadarIconModelForm read(RadarIconFormType type, String[] args, RadarIconDefinition iconDefinition) {
      if (args.length == 1) {
         return RadarIconBasicForms.DEFAULT_MODEL;
      } else if (args.length != 2) {
         return null;
      } else {
         int configIndex = 0;

         try {
            configIndex = Integer.parseInt(args[1]);
         } catch (NumberFormatException nfe) {
            MinimapLogs.LOGGER.error("suppressed exception", nfe);
         }

         RadarIconModelConfig referencedModelConfig = iconDefinition.getModelConfig(configIndex);
         if (referencedModelConfig == null) {
            Logger var10000 = MinimapLogs.LOGGER;
            String var10001 = Arrays.toString(args);
            var10000.info("Specified model config is not defined: " + var10001 + " for " + String.valueOf(iconDefinition.getEntityId()));
         }

         return new RadarIconModelForm(type, referencedModelConfig);
      }
   }
}
