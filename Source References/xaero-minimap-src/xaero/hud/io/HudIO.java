package xaero.hud.io;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_2960;
import xaero.common.HudMod;
import xaero.hud.Hud;
import xaero.hud.module.HudModule;
import xaero.hud.module.ModuleManager;
import xaero.hud.module.ModuleTransform;

public class HudIO {
   public static final String SEPARATOR = ";";
   public static final String MODULE_LINE_PREFIX = "module;";
   private final Hud hud;
   private final List<String> unloadedModuleLines;

   private HudIO(Hud hud, List<String> unloadedModuleLines) {
      this.hud = hud;
      this.unloadedModuleLines = unloadedModuleLines;
   }

   public void save(PrintWriter writer) {
      ModuleManager moduleManager = this.hud.getModuleManager();

      for(HudModule<?> module : moduleManager.getModules()) {
         ModuleTransform transform = module.getConfirmedTransform();
         writer.print("module;");
         writer.print("id=");
         writer.print(module.getId());
         writer.print(";");
         writer.print("x=");
         writer.print(transform.x);
         writer.print(";");
         writer.print("y=");
         writer.print(transform.y);
         writer.print(";");
         writer.print("centered=");
         writer.print(transform.centered);
         writer.print(";");
         writer.print("fromRight=");
         writer.print(transform.fromRight);
         writer.print(";");
         writer.print("fromBottom=");
         writer.print(transform.fromBottom);
         writer.print(";");
         writer.print("flippedVer=");
         writer.print(transform.flippedVer);
         writer.print(";");
         writer.print("flippedHor=");
         writer.print(transform.flippedHor);
         writer.print(";");
         if (transform.fromOldSystem) {
            writer.print("fromOldSystem=");
            writer.print(transform.fromOldSystem);
            writer.print(";");
         }

         writer.println();
      }

      for(String unloadedModuleLine : this.unloadedModuleLines) {
         writer.println(unloadedModuleLine);
      }

   }

   public boolean load(String line, boolean shouldLoadLegacySettings) {
      if (!line.startsWith("module;")) {
         return false;
      } else {
         try {
            String[] entryStrings = line.substring("module;".length()).split(";");
            HudModule<?> destinationModule = null;
            Boolean active = null;
            ModuleTransform loadedTransform = new ModuleTransform();

            for(String entryString : entryStrings) {
               String[] entryStringSplit = entryString.split("=");
               if (entryStringSplit.length >= 2) {
                  String key = entryStringSplit[0];
                  String valueString = entryStringSplit[1];
                  if (key.equals("id")) {
                     destinationModule = this.hud.getModuleManager().get(class_2960.method_60654(valueString));
                     if (destinationModule == null) {
                        HudMod.LOGGER.warn("A saved hud module is no longer registered! Line:");
                        HudMod.LOGGER.warn(line);
                        break;
                     }
                  } else if (key.equals("active")) {
                     active = valueString.equals("true");
                  } else if (key.equals("x")) {
                     loadedTransform.x = Integer.parseInt(valueString);
                  } else if (key.equals("y")) {
                     loadedTransform.y = Integer.parseInt(valueString);
                  } else if (key.equals("centered")) {
                     loadedTransform.centered = valueString.equals("true");
                  } else if (key.equals("fromRight")) {
                     loadedTransform.fromRight = valueString.equals("true");
                  } else if (key.equals("fromBottom")) {
                     loadedTransform.fromBottom = valueString.equals("true");
                  } else if (key.equals("flippedVer")) {
                     loadedTransform.flippedVer = valueString.equals("true");
                  } else if (key.equals("flippedHor")) {
                     loadedTransform.flippedHor = valueString.equals("true");
                  } else if (key.equals("fromOldSystem")) {
                     loadedTransform.fromOldSystem = valueString.equals("true");
                  }
               }
            }

            if (destinationModule == null) {
               this.unloadedModuleLines.add(line);
               return true;
            }

            if (active != null && shouldLoadLegacySettings) {
               destinationModule.setActive(HudMod.INSTANCE.getHudConfigs().getClientConfigManager(), active);
            }

            destinationModule.setTransform(loadedTransform);
         } catch (Throwable t) {
            HudMod.LOGGER.error("Error loading module state from line {}", line, t);
         }

         return true;
      }
   }

   public static final class Builder {
      private Hud hud;

      private Builder() {
      }

      public Builder setDefault() {
         this.setHud((Hud)null);
         return this;
      }

      public Builder setHud(Hud hud) {
         this.hud = hud;
         return this;
      }

      public HudIO build() {
         if (this.hud == null) {
            throw new IllegalStateException();
         } else {
            return new HudIO(this.hud, new ArrayList());
         }
      }

      public static Builder begin() {
         return (new Builder()).setDefault();
      }
   }
}
