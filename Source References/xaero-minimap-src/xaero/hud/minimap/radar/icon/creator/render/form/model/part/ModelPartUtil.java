package xaero.hud.minimap.radar.icon.creator.render.form.model.part;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import net.minecraft.class_630;
import xaero.lib.common.reflection.util.ReflectionUtils;

public class ModelPartUtil {
   private static final Field CUBES_FIELD = ReflectionUtils.getFieldReflection(class_630.class, "cubes", "field_3663", "Ljava/util/List;", "f_104212_");
   private static final Field CHILDREN_FIELD = ReflectionUtils.getFieldReflection(class_630.class, "children", "field_3661", "Ljava/util/Map;", "f_104213_");

   public static List<class_630.class_628> getCubes(class_630 modelRenderer) {
      return (List)ReflectionUtils.getReflectFieldValue(modelRenderer, CUBES_FIELD);
   }

   public static Map<String, class_630> getChildren(class_630 modelRenderer) {
      return (Map)ReflectionUtils.getReflectFieldValue(modelRenderer, CHILDREN_FIELD);
   }

   public static boolean hasDirectCubes(class_630 part) {
      List<class_630.class_628> cubes = getCubes(part);
      return cubes != null && !cubes.isEmpty();
   }

   public static boolean hasCubes(class_630 part) {
      if (hasDirectCubes(part)) {
         return true;
      } else {
         Map<String, class_630> children = getChildren(part);

         for(class_630 child : children.values()) {
            if (hasCubes(child)) {
               return true;
            }
         }

         return false;
      }
   }

   public static class_630.class_628 getBiggestCuboid(class_630 part) {
      List<class_630.class_628> mainCubeList = getCubes(part);
      if (mainCubeList == null) {
         return null;
      } else if (mainCubeList.isEmpty()) {
         return null;
      } else {
         float biggestSize = 0.0F;
         class_630.class_628 biggestCuboid = null;

         for(class_630.class_628 cuboid : mainCubeList) {
            float size = Math.abs((cuboid.field_3648 - cuboid.field_3645) * (cuboid.field_3647 - cuboid.field_3644) * (cuboid.field_3646 - cuboid.field_3643));
            if (size >= biggestSize) {
               biggestCuboid = cuboid;
               biggestSize = size;
            }
         }

         return biggestCuboid;
      }
   }
}
