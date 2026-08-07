package xaero.hud.minimap.radar.icon.cache.id.armor;

import net.minecraft.class_12117;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1496;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_3489;
import net.minecraft.class_8053;
import net.minecraft.class_8054;
import net.minecraft.class_8056;
import net.minecraft.class_9334;

public class RadarIconArmorHandler {
   public RadarIconArmor getArmor(class_1309 livingEntity) {
      class_1304 relevantArmourSlot = !(livingEntity instanceof class_1496) && !(livingEntity instanceof class_12117) ? class_1304.field_6169 : class_1304.field_48824;
      class_1799 armorItemStack = livingEntity.method_6118(relevantArmourSlot);
      if (armorItemStack != null && armorItemStack != class_1799.field_8037) {
         class_1792 armorItem = armorItemStack.method_7909();
         if (!armorItemStack.method_31573(class_3489.field_41890)) {
            return new RadarIconArmor(armorItem, (class_8054)null, (class_8056)null);
         } else if (!armorItemStack.method_57826(class_9334.field_49607)) {
            return new RadarIconArmor(armorItem, (class_8054)null, (class_8056)null);
         } else {
            class_8053 trim = (class_8053)armorItemStack.method_58694(class_9334.field_49607);
            class_8054 trimMaterial = (class_8054)trim.comp_3179().comp_349();
            class_8056 trimPattern = (class_8056)trim.comp_3180().comp_349();
            return new RadarIconArmor(armorItem, trimMaterial, trimPattern);
         }
      } else {
         return null;
      }
   }
}
