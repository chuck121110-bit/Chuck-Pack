package xaero.hud.minimap.common.config.primary.option;

import java.util.ArrayList;
import java.util.List;
import xaero.lib.common.config.option.BooleanConfigOption;
import xaero.lib.common.config.option.ConfigOption;
import xaero.lib.common.config.option.ConfigOptionManager;
import xaero.lib.common.config.option.BooleanConfigOption.Builder;

public class MinimapPrimaryCommonConfigOptions {
   private static final List<ConfigOption<?>> ALL = new ArrayList();
   public static final BooleanConfigOption REGISTER_EFFECTS;

   public static void registerAll(ConfigOptionManager manager) {
      for(ConfigOption<?> option : ALL) {
         manager.register(option);
      }

   }

   static {
      REGISTER_EFFECTS = ((BooleanConfigOption.Builder)((BooleanConfigOption.Builder)Builder.begin().setId("register_minimap_status_effects")).setDefaultValue(true)).build(ALL);
   }
}
