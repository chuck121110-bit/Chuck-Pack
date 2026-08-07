package xaero.hud.controls.key;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.class_304;
import xaero.hud.controls.key.function.KeyMappingFunction;

public class KeyMappingControllerManager implements Iterable<KeyMappingController> {
   private final Map<class_304, KeyMappingController> controllers = new HashMap();

   public KeyMappingController getController(class_304 keyMapping) {
      return (KeyMappingController)this.controllers.get(keyMapping);
   }

   public void registerController(class_304 keyMapping, boolean xaeroKey) {
      this.registerController(keyMapping, xaeroKey, (Consumer)null);
   }

   public void registerController(class_304 keyMapping, boolean xaeroKey, Consumer<class_304> then) {
      if (this.controllers.containsKey(keyMapping)) {
         throw new IllegalArgumentException("The key mapping is already registered!");
      } else {
         this.controllers.put(keyMapping, new KeyMappingController(keyMapping, xaeroKey));
         if (then != null) {
            then.accept(keyMapping);
         }

      }
   }

   public void registerFunction(class_304 keyMapping, KeyMappingFunction function) {
      KeyMappingController functionSet = this.getController(keyMapping);
      if (functionSet == null) {
         throw new IllegalArgumentException("The key mapping needs to be registered with registerController first!");
      } else {
         functionSet.add(function);
      }
   }

   public Iterator<KeyMappingController> iterator() {
      return this.controllers.values().iterator();
   }
}
