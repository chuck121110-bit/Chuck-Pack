package xaero.hud.minimap.world;

import java.util.function.Function;
import net.minecraft.class_151;
import net.minecraft.class_156;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_5321;
import net.minecraft.class_746;
import net.minecraft.class_7924;
import xaero.hud.minimap.world.container.MinimapWorldRootContainer;
import xaero.lib.common.util.IOUtils;

public class MinimapDimensionHelper {
   private final Function<class_5321<class_1937>, String> convertToDimensionDirectoryNameCache = class_156.method_34866(this::convertToDimensionDirectoryName);

   public double getDimensionDivision(MinimapWorld minimapWorld) {
      if (class_310.method_1551().field_1687 == null) {
         return (double)1.0F;
      } else {
         double dimCoordinateScale = this.getDimCoordinateScale(minimapWorld);
         return class_310.method_1551().field_1687.method_8597().comp_646() / dimCoordinateScale;
      }
   }

   public double getDimCoordinateScale(MinimapWorld minimapWorld) {
      if (minimapWorld == null) {
         return (double)1.0F;
      } else {
         MinimapWorldRootContainer rootContainer = minimapWorld.getContainer().getRoot();
         class_5321<class_1937> dimKey = minimapWorld.getDimId();
         return dimKey == null ? (double)1.0F : rootContainer.getDimensionScale(dimKey);
      }
   }

   public String getDimensionDirectoryName(class_5321<class_1937> dimKey) {
      if (dimKey == class_1937.field_25179) {
         return "dim%0";
      } else if (dimKey == class_1937.field_25180) {
         return "dim%-1";
      } else {
         return dimKey == class_1937.field_25181 ? "dim%1" : (String)this.convertToDimensionDirectoryNameCache.apply(dimKey);
      }
   }

   private String convertToDimensionDirectoryName(class_5321<class_1937> dimKey) {
      class_2960 identifier = dimKey.method_29177();
      String path = identifier.method_12832().replace('/', '%');
      path = IOUtils.replaceTrailingDots(path, ',');
      String var10000 = identifier.method_12836();
      return "dim%" + var10000 + "$" + path;
   }

   public class_5321<class_1937> findDimensionKeyForOldName(class_746 player, String oldName) {
      for(class_5321<class_1937> dk : player.field_3944.method_29356()) {
         if (oldName.equals(dk.method_29177().method_12832().replaceAll("[^a-zA-Z0-9_]+", ""))) {
            return dk;
         }
      }

      return null;
   }

   public class_5321<class_1937> getDimensionKeyForDirectoryName(String dirName) {
      String dimIdPart = dirName.substring(4);
      if (dimIdPart.equals("0")) {
         return class_1937.field_25179;
      } else if (dimIdPart.equals("1")) {
         return class_1937.field_25181;
      } else if (dimIdPart.equals("-1")) {
         return class_1937.field_25180;
      } else {
         String[] idArgs = dimIdPart.split("\\$");
         if (idArgs.length < 2) {
            return null;
         } else {
            String path = idArgs[1].replace('%', '/');
            path = path.replace(',', '.');

            try {
               return class_5321.method_29179(class_7924.field_41223, class_2960.method_60655(idArgs[0], path));
            } catch (class_151 var6) {
               return null;
            }
         }
      }
   }
}
