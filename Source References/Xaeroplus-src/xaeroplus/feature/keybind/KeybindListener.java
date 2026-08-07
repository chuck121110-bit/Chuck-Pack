package xaeroplus.feature.keybind;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.concurrent.ForkJoinPool;
import net.lenni0451.lambdaevents.EventHandler;
import net.minecraft.class_304;
import net.minecraft.class_310;
import xaeroplus.event.ClientTickEvent;
import xaeroplus.settings.BooleanSetting;
import xaeroplus.settings.SettingHooks;
import xaeroplus.settings.Settings;

public class KeybindListener {
   private final Object2BooleanMap<class_304> prevKeybindState = new Object2BooleanOpenHashMap();

   @EventHandler
   public void onTick(ClientTickEvent.Post event) {
      if (class_310.method_1551().field_1755 == null) {
         if (class_310.method_1551().field_1724 != null) {
            for(class_304 keybind : Settings.REGISTRY.getKeybindings()) {
               if (keybind.method_1434()) {
                  boolean wasPrevDown = this.prevKeybindState.getOrDefault(keybind, false);
                  this.prevKeybindState.put(keybind, true);
                  if (!wasPrevDown) {
                     BooleanSetting setting = Settings.REGISTRY.getKeybindingSetting(keybind);
                     if (setting != null) {
                        setting.setValue(!setting.get());
                        ForkJoinPool.commonPool().execute(SettingHooks::saveSettings);
                     }
                  }
               } else {
                  this.prevKeybindState.put(keybind, false);
               }
            }

         }
      }
   }
}
