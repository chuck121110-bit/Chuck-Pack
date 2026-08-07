package xaero.hud.controls.key;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.class_304;
import xaero.hud.controls.key.function.KeyMappingFunction;

public class KeyMappingController implements Iterable<KeyMappingFunction> {
   private final class_304 keyMapping;
   private final boolean xaeroKey;
   private final Set<KeyMappingFunction> functions;
   private boolean pressed;

   public KeyMappingController(class_304 keyMapping, boolean xaeroKey) {
      this.keyMapping = keyMapping;
      this.xaeroKey = xaeroKey;
      this.functions = new HashSet();
   }

   public void add(KeyMappingFunction function) {
      this.functions.add(function);
   }

   public class_304 getKeyMapping() {
      return this.keyMapping;
   }

   public Iterable<KeyMappingFunction> getFunctions() {
      return this.functions;
   }

   public boolean isXaeroKey() {
      return this.xaeroKey;
   }

   public boolean isPressed() {
      return this.pressed;
   }

   public void setPressed(boolean pressed) {
      this.pressed = pressed;
   }

   public Iterator<KeyMappingFunction> iterator() {
      return this.functions.iterator();
   }
}
