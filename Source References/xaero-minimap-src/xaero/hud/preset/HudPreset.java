package xaero.hud.preset;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.class_2561;
import net.minecraft.class_2960;

public final class HudPreset {
   private final class_2960 id;
   private final class_2561 name;
   private final Set<ModulePreset<?>> modulePresets;
   private boolean applied;

   private HudPreset(class_2960 id, class_2561 name, Set<ModulePreset<?>> modulePresets) {
      this.id = id;
      this.name = name;
      this.modulePresets = modulePresets;
   }

   public class_2960 getId() {
      return this.id;
   }

   public class_2561 getName() {
      return this.name;
   }

   public void apply() {
      this.applied = true;

      for(ModulePreset<?> modulePreset : this.modulePresets) {
         modulePreset.apply();
      }

   }

   public void confirm() {
      if (this.applied) {
         this.applied = false;

         for(ModulePreset<?> modulePreset : this.modulePresets) {
            modulePreset.confirm();
         }

      }
   }

   public void cancel() {
      if (this.applied) {
         this.applied = false;

         for(ModulePreset<?> modulePreset : this.modulePresets) {
            modulePreset.cancel();
         }

      }
   }

   public void applyAndConfirm() {
      this.apply();
      this.confirm();

      for(ModulePreset<?> modulePreset : this.modulePresets) {
         modulePreset.getModule().confirmTransform();
      }

   }

   public static final class Builder {
      private class_2960 id;
      private class_2561 name;
      private final Set<ModulePreset<?>> modulePresets = new HashSet();

      private Builder() {
      }

      public Builder setDefault() {
         this.modulePresets.clear();
         return this;
      }

      public Builder setId(class_2960 id) {
         this.id = id;
         return this;
      }

      public Builder setName(class_2561 name) {
         this.name = name;
         return this;
      }

      public Builder addModulePreset(ModulePreset<?> modulePreset) {
         this.modulePresets.add(modulePreset);
         return this;
      }

      public HudPreset build() {
         if (this.id != null && this.name != null) {
            return new HudPreset(this.id, this.name, this.modulePresets);
         } else {
            throw new IllegalStateException();
         }
      }

      public static Builder begin() {
         return (new Builder()).setDefault();
      }
   }
}
