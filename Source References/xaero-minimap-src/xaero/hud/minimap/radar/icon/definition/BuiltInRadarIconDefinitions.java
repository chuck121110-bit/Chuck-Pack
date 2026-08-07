package xaero.hud.minimap.radar.icon.definition;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.class_10017;
import net.minecraft.class_10067;
import net.minecraft.class_10332;
import net.minecraft.class_11260;
import net.minecraft.class_12150;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1321;
import net.minecraft.class_1439;
import net.minecraft.class_1451;
import net.minecraft.class_1452;
import net.minecraft.class_1474;
import net.minecraft.class_1493;
import net.minecraft.class_1498;
import net.minecraft.class_1501;
import net.minecraft.class_1533;
import net.minecraft.class_1542;
import net.minecraft.class_1560;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2960;
import net.minecraft.class_3850;
import net.minecraft.class_3851;
import net.minecraft.class_3852;
import net.minecraft.class_3854;
import net.minecraft.class_3879;
import net.minecraft.class_4495;
import net.minecraft.class_4587;
import net.minecraft.class_4791;
import net.minecraft.class_4985;
import net.minecraft.class_4997;
import net.minecraft.class_549;
import net.minecraft.class_553;
import net.minecraft.class_555;
import net.minecraft.class_561;
import net.minecraft.class_562;
import net.minecraft.class_565;
import net.minecraft.class_567;
import net.minecraft.class_570;
import net.minecraft.class_571;
import net.minecraft.class_574;
import net.minecraft.class_576;
import net.minecraft.class_5772;
import net.minecraft.class_578;
import net.minecraft.class_583;
import net.minecraft.class_584;
import net.minecraft.class_586;
import net.minecraft.class_588;
import net.minecraft.class_592;
import net.minecraft.class_594;
import net.minecraft.class_595;
import net.minecraft.class_596;
import net.minecraft.class_599;
import net.minecraft.class_602;
import net.minecraft.class_604;
import net.minecraft.class_608;
import net.minecraft.class_609;
import net.minecraft.class_610;
import net.minecraft.class_611;
import net.minecraft.class_612;
import net.minecraft.class_615;
import net.minecraft.class_621;
import net.minecraft.class_6227;
import net.minecraft.class_624;
import net.minecraft.class_625;
import net.minecraft.class_630;
import net.minecraft.class_7198;
import net.minecraft.class_7280;
import net.minecraft.class_7751;
import net.minecraft.class_7923;
import net.minecraft.class_8185;
import net.minecraft.class_889;
import net.minecraft.class_897;
import net.minecraft.class_8973;
import net.minecraft.class_9082;
import xaero.common.minimap.render.radar.EntityIconDefinitions;
import xaero.common.misc.OptimizedMath;
import xaero.hud.minimap.radar.icon.cache.id.variant.EndermanVariant;
import xaero.hud.minimap.radar.icon.cache.id.variant.HorseVariant;
import xaero.hud.minimap.radar.icon.cache.id.variant.IronGolemVariant;
import xaero.hud.minimap.radar.icon.cache.id.variant.LlamaVariant;
import xaero.hud.minimap.radar.icon.cache.id.variant.SaddleVariant;
import xaero.hud.minimap.radar.icon.cache.id.variant.TamableVariant;
import xaero.hud.minimap.radar.icon.cache.id.variant.TropicalFishVariant;
import xaero.hud.minimap.radar.icon.cache.id.variant.VillagerVariant;
import xaero.hud.minimap.radar.icon.creator.render.form.model.custom.RadarIconCustomPrerenderer;

public class BuiltInRadarIconDefinitions {
   public static final Method BUILD_VARIANT_ID_STRING_METHOD;
   public static final Method GET_VARIANT_ID_STRING_METHOD;

   public static List<String> getMainModelPartFields(class_897<?, ?> renderer, class_583<?> model, class_1297 entity) {
      List<String> result = new ArrayList();
      if (model instanceof class_553) {
         String modelClassPath = class_553.class.getName();
         result.add(String.format("%s;%s", modelClassPath, "head"));
         result.add(String.format("%s;%s", modelClassPath, "field_3321"));
         result.add(String.format("%s;%s", modelClassPath, "f_102184_"));
      } else if (model instanceof class_555) {
         String modelClassPath = class_555.class.getName();
         result.add(String.format("%s;%s", modelClassPath, "head"));
         result.add(String.format("%s;%s", modelClassPath, "field_3329"));
         result.add(String.format("%s;%s", modelClassPath, "f_102245_"));
      } else if (model instanceof class_611) {
         String modelClassPath = class_611.class.getName();
         result.add(String.format("%s;%s", modelClassPath, "head"));
         result.add(String.format("%s;%s", modelClassPath, "field_3583"));
         result.add(String.format("%s;%s", modelClassPath, "f_103852_"));
      } else if (model instanceof class_562) {
         String modelClassPath = class_562.class.getName();
         result.add(String.format("%s;%s", modelClassPath, "head"));
         result.add(String.format("%s;%s", modelClassPath, "field_3360"));
         result.add(String.format("%s;%s", modelClassPath, "f_102451_"));
      } else if (model instanceof class_578) {
         String modelClassPath = class_578.class.getName();
         result.add(String.format("%s;%s", modelClassPath, "head"));
         result.add(String.format("%s;%s", modelClassPath, "field_27443"));
         result.add(String.format("%s;%s", modelClassPath, "f_103031_"));
      } else if (model instanceof class_584) {
         String modelClassPath = class_584.class.getName();
         result.add(String.format("%s;%s", modelClassPath, "head"));
         result.add(String.format("%s;%s", modelClassPath, "field_3452"));
         result.add(String.format("%s;%s", modelClassPath, "f_103188_"));
      } else if (model instanceof class_596) {
         String modelClassPath = class_596.class.getName();
         result.add(String.format("%s;%s", modelClassPath, "head"));
         result.add(String.format("%s;%s", modelClassPath, "field_27486"));
         result.add(String.format("%s;%s", modelClassPath, "f_103523_"));
      } else if (model instanceof class_571) {
         String modelClassPath = class_571.class.getName();
         result.add(String.format("%s;%s", modelClassPath, "head"));
         result.add(String.format("%s;%s", modelClassPath, "field_3386"));
         result.add(String.format("%s;%s", modelClassPath, "f_103598_"));
      } else if (model instanceof class_574) {
         String modelClassPath = class_574.class.getName();
         result.add(String.format("%s;%s", modelClassPath, "head"));
         result.add(String.format("%s;%s", modelClassPath, "field_3415"));
         result.add(String.format("%s;%s", modelClassPath, "f_102936_"));
      } else if (model instanceof class_608) {
         String modelClassPath = class_608.class.getName();
         result.add(String.format("%s;%s", modelClassPath, "head"));
         result.add(String.format("%s;%s", modelClassPath, "field_3568"));
         result.add(String.format("%s;%s", modelClassPath, "f_103839_"));
      } else if (model instanceof class_625) {
         String modelClassPath = class_625.class.getName();
         result.add(String.format("%s;%s", modelClassPath, "head"));
         result.add(String.format("%s;%s", modelClassPath, "field_3630"));
         result.add(String.format("%s;%s", modelClassPath, "f_114235_"));
      } else if (model instanceof class_602) {
         String modelClassPath = class_602.class.getName();
         result.add(String.format("%s;%s", modelClassPath, "head"));
         result.add(String.format("%s;%s", modelClassPath, "field_3554"));
         result.add(String.format("%s;%s", modelClassPath, "f_103724_"));
      } else if (model instanceof class_609) {
         result.add(String.format("%s;%s", class_630.class.getName(), String.format("children['%s']", "cube")));
         result.add(String.format("%s;%s", class_630.class.getName(), String.format("field_3661['%s']", "cube")));
         result.add(String.format("%s;%s", class_630.class.getName(), String.format("f_104213_['%s']", "cube")));
      } else if (model instanceof class_5772) {
         String modelClassPath = class_5772.class.getName();
         result.add(String.format("%s;%s", modelClassPath, "head"));
         result.add(String.format("%s;%s", modelClassPath, "field_28379"));
         result.add(String.format("%s;%s", modelClassPath, "f_170365_"));
      } else if (model instanceof class_576) {
         result.add(String.format("%s;%s", class_630.class.getName(), "children['inside_cube']"));
         result.add(String.format("%s;%s", class_630.class.getName(), "field_3661['inside_cube']"));
         result.add(String.format("%s;%s", class_630.class.getName(), "f_104213_['inside_cube']"));
      } else if (!(model instanceof class_610) && !(model instanceof class_567) && !(model instanceof class_11260) && !(model instanceof class_4997) && !(model instanceof class_588)) {
         if (!(model instanceof class_7280) && !(model instanceof class_7198)) {
            if (model instanceof class_8185) {
               String modelClassPath = class_8185.class.getName();
               result.add(String.format("%s;%s", modelClassPath, "head"));
               result.add(String.format("%s;%s", modelClassPath, "field_43085"));
               result.add(String.format("%s;%s", modelClassPath, "f_273862_"));
            } else if (model instanceof class_7751) {
               String modelClassPath = class_7751.class.getName();
               result.add(String.format("%s;%s", modelClassPath, "head"));
               result.add(String.format("%s;%s", modelClassPath, "field_40464"));
               result.add(String.format("%s;%s", modelClassPath, "f_243837_"));
            } else if (model instanceof class_8973) {
               String modelClassPath = class_8973.class.getName();
               result.add(String.format("%s;%s", modelClassPath, "head"));
               result.add(String.format("%s;%s", modelClassPath, "field_47435"));
               result.add(String.format("%s;%s", modelClassPath, "f_302678_"));
            } else if (model instanceof class_9082) {
               String modelClassPath = class_9082.class.getName();
               result.add(String.format("%s;%s", modelClassPath, "head"));
               result.add(String.format("%s;%s", modelClassPath, "field_47872"));
            } else if (model instanceof class_10332) {
               String modelClassPath = class_10332.class.getName();
               result.add(String.format("%s;%s", modelClassPath, "head"));
               result.add(String.format("%s;%s", modelClassPath, "field_54844"));
            } else if (model instanceof class_624) {
               String modelClassPath = class_624.class.getName();
               result.add(String.format("%s;%s", modelClassPath, "realHead"));
               result.add(String.format("%s;%s", modelClassPath, "field_20788"));
            } else if (model instanceof class_12150) {
               result.add(String.format("%s;%s", class_630.class.getName(), String.format("children['%s']", "shell")));
               result.add(String.format("%s;%s", class_630.class.getName(), String.format("field_3661['%s']", "shell")));
               result.add(String.format("%s;%s", class_630.class.getName(), String.format("f_104213_['%s']", "shell")));
            }
         } else {
            result.add(String.format("%s;%s", class_630.class.getName(), String.format("children['%s']", "head")));
            result.add(String.format("%s;%s", class_630.class.getName(), String.format("field_3661['%s']", "head")));
            result.add(String.format("%s;%s", class_630.class.getName(), String.format("f_104213_['%s']", "head")));
         }
      } else {
         result.add(String.format("%s;%s", class_630.class.getName(), String.format("children['%s']", "body")));
         result.add(String.format("%s;%s", class_630.class.getName(), String.format("field_3661['%s']", "body")));
         result.add(String.format("%s;%s", class_630.class.getName(), String.format("f_104213_['%s']", "body")));
      }

      return result;
   }

   public static List<String> getSecondaryModelPartsFields(class_897<?, ?> renderer, class_583<?> model, class_1297 entity) {
      List<String> result = new ArrayList();
      if (model instanceof class_596) {
         String modelClassPath = class_596.class.getName();
         result.add(String.format("%s;%s", modelClassPath, "rightEar"));
         result.add(String.format("%s;%s", modelClassPath, "field_27487"));
         result.add(String.format("%s;%s", modelClassPath, "f_170877_"));
         result.add(String.format("%s;%s", modelClassPath, "leftEar"));
         result.add(String.format("%s;%s", modelClassPath, "field_27488"));
         result.add(String.format("%s;%s", modelClassPath, "f_170878_"));
         result.add(String.format("%s;%s", modelClassPath, "nose"));
         result.add(String.format("%s;%s", modelClassPath, "field_3530"));
         result.add(String.format("%s;%s", modelClassPath, "f_103527_"));
      } else if (model instanceof class_12150) {
         result.add(String.format("%s;%s", class_630.class.getName(), String.format("children['%s']", "body")));
         result.add(String.format("%s;%s", class_630.class.getName(), String.format("field_3661['%s']", "body")));
         result.add(String.format("%s;%s", class_630.class.getName(), String.format("f_104213_['%s']", "body")));
      }

      return result;
   }

   public static Object getModelRoot(class_3879 entityModel) {
      if (!(entityModel instanceof class_610) && !(entityModel instanceof class_567) && !(entityModel instanceof class_11260) && !(entityModel instanceof class_609) && !(entityModel instanceof class_588) && !(entityModel instanceof class_4997) && !(entityModel instanceof class_576)) {
         if (entityModel instanceof class_12150) {
            return entityModel.method_63512().method_32086("root");
         } else if (entityModel instanceof class_7280) {
            return entityModel.method_63512().method_32086("bone").method_32086("body");
         } else {
            return entityModel instanceof class_7198 ? entityModel.method_63512().method_32086("body") : entityModel;
         }
      } else {
         return entityModel.method_63512();
      }
   }

   public static boolean forceFieldCheck(class_583<?> entityModel) {
      return entityModel instanceof class_5772 || entityModel instanceof class_624;
   }

   public static void prepareEntityRenderState(class_10017 renderState) {
      if (renderState instanceof class_10067 cubeState) {
         cubeState.field_53574 = 0.0F;
      }

   }

   public static void defaultTransformation(class_4587 matrixStack, class_583 em, class_1297 entity) {
      if (!(em instanceof class_561) && !(em instanceof class_599)) {
         if (!(em instanceof class_615) && !(em instanceof class_612)) {
            if (em instanceof class_553) {
               matrixStack.method_22905(0.5F, 0.5F, 0.5F);
            } else if (em instanceof class_549) {
               OptimizedMath.rotatePose(matrixStack, 65.0F, OptimizedMath.XP);
               matrixStack.method_22905(0.7F, 0.7F, 0.7F);
            } else if (!(em instanceof class_889) && !(em instanceof class_6227)) {
               if (!(em instanceof class_570) && !(em instanceof class_610)) {
                  if (em instanceof class_4791) {
                     OptimizedMath.rotatePose(matrixStack, 45.0F, OptimizedMath.XP);
                     matrixStack.method_22905(0.5F, 0.5F, 0.5F);
                  } else if (!(em instanceof class_578) && !(em instanceof class_7751) && !(em instanceof class_8185)) {
                     if (!(em instanceof class_567) && !(em instanceof class_11260) && !(em instanceof class_571) && !(em instanceof class_4997) && !(em instanceof class_625)) {
                        if (em instanceof class_621) {
                           matrixStack.method_22905(0.35F, 0.35F, 0.35F);
                        } else if (em instanceof class_588) {
                           matrixStack.method_22905(0.3F, 0.3F, 0.3F);
                           OptimizedMath.rotatePose(matrixStack, 90.0F, OptimizedMath.XP);
                        } else if (em instanceof class_586) {
                           matrixStack.method_22905(0.7F, 0.7F, 0.7F);
                        } else if (em instanceof class_7280) {
                           matrixStack.method_22905(0.7F, 0.7F, 0.7F);
                        } else if (em instanceof class_12150) {
                           matrixStack.method_22905(0.5F, 0.5F, 0.5F);
                           OptimizedMath.rotatePose(matrixStack, 90.0F, OptimizedMath.YP);
                        }
                     } else {
                        matrixStack.method_22905(0.5F, 0.5F, 0.5F);
                     }
                  } else {
                     matrixStack.method_22905(0.5F, 0.5F, 0.5F);
                  }
               } else {
                  matrixStack.method_22905(0.5F, 0.5F, 0.5F);
               }
            } else {
               matrixStack.method_22905(0.7F, 0.7F, 0.7F);
            }
         } else {
            OptimizedMath.rotatePose(matrixStack, 90.0F, OptimizedMath.YP);
         }
      } else {
         OptimizedMath.rotatePose(matrixStack, 90.0F, OptimizedMath.YP);
         matrixStack.method_22905(0.5F, 0.5F, 0.5F);
      }

   }

   public static void defaultPostIconModelRender(class_4587 matrixStack, class_583 entityModel, class_1297 entity) {
   }

   public static boolean fullModelIcon(class_583 em) {
      return em instanceof class_561 || em instanceof class_599 || em instanceof class_615 || em instanceof class_612 || em instanceof class_4495 || em instanceof class_889 || em instanceof class_570 || em instanceof class_565 || em instanceof class_576 || em instanceof class_609 || em instanceof class_592 || em instanceof class_595 || em instanceof class_594 || em instanceof class_604 || em instanceof class_621;
   }

   public static <S extends class_10017> RadarIconCustomPrerenderer getCustomLayer(class_897<?, ? super S> entityRenderer, class_1297 entity) {
      return null;
   }

   public static <E extends class_1297> Object getVariant(class_2960 entityTexture, class_897<? super E, ?> entityRenderer, E entity) {
      if (entity instanceof class_1498) {
         return new HorseVariant(entityTexture, ((class_1498)entity).method_27078());
      } else if (entity instanceof class_3851) {
         class_3850 villagerdata = ((class_3851)entity).method_7231();
         class_3854 villagertype = (class_3854)villagerdata.comp_3520().comp_349();
         class_2960 villagerTypeKey = class_7923.field_41194.method_10221(villagertype);
         class_3852 villagerprofession = (class_3852)villagerdata.comp_3521().comp_349();
         class_2960 villagerProfessionKey = class_7923.field_41195.method_10221(villagerprofession);
         int villagerprofessionlevel = villagerdata.comp_3522();
         return new VillagerVariant(entityTexture, ((class_1309)entity).method_6109(), villagerTypeKey, villagerProfessionKey, villagerprofessionlevel);
      } else if (!(entity instanceof class_1451) && !(entity instanceof class_1493)) {
         if (entity instanceof class_1439) {
            return new IronGolemVariant(entityTexture, ((class_1439)entity).method_23347());
         } else if (entity instanceof class_1501) {
            class_1501 llama = (class_1501)entity;
            return new LlamaVariant(entityTexture, llama.method_6807(), llama.method_56676().method_7909());
         } else if (entity instanceof class_1452) {
            return new SaddleVariant(entityTexture, ((class_1452)entity).method_66672());
         } else if (entity instanceof class_4985) {
            return new SaddleVariant(entityTexture, ((class_4985)entity).method_66672());
         } else if (entity instanceof class_1474) {
            class_1474 fish = (class_1474)entity;
            return new TropicalFishVariant(entityTexture, fish.method_66681(), fish.method_6658(), fish.method_6655());
         } else if (entity instanceof class_1560) {
            class_1560 enderman = (class_1560)entity;
            return new EndermanVariant(entityTexture, enderman.method_7028());
         } else if (entity instanceof class_1542) {
            class_1542 itemEntity = (class_1542)entity;
            return class_7923.field_41178.method_10221(itemEntity.method_6983().method_7909());
         } else if (entity instanceof class_1533) {
            class_1533 itemFrame = (class_1533)entity;
            class_1799 itemFrameStack = itemFrame.method_6940();
            class_1792 item = itemFrameStack == null ? class_1802.field_8162 : itemFrameStack.method_7909();
            return class_7923.field_41178.method_10221(item);
         } else {
            return entityTexture == null ? "default" : entityTexture;
         }
      } else {
         return new TamableVariant(entityTexture, ((class_1321)entity).method_6181());
      }
   }

   static {
      try {
         BUILD_VARIANT_ID_STRING_METHOD = EntityIconDefinitions.class.getDeclaredMethod("buildVariantIdString", StringBuilder.class, class_897.class, class_1297.class);
         GET_VARIANT_ID_STRING_METHOD = EntityIconDefinitions.class.getDeclaredMethod("getVariantString", class_897.class, class_1297.class);
      } catch (NoSuchMethodException e) {
         throw new RuntimeException(e);
      }
   }
}
