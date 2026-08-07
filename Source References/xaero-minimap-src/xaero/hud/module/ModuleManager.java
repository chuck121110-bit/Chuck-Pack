package xaero.hud.module;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.class_2960;

public final class ModuleManager {
   private final Map<class_2960, HudModule<?>> modules = new LinkedHashMap();

   public void register(HudModule<?> hudModule) {
      this.modules.put(hudModule.getId(), hudModule);
   }

   public HudModule<?> get(class_2960 id) {
      return (HudModule)this.modules.get(id);
   }

   public Iterable<HudModule<?>> getModules() {
      return this.modules.values();
   }
}
