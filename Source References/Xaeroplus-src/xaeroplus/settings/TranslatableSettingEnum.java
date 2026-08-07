package xaeroplus.settings;

import net.minecraft.class_1074;

public interface TranslatableSettingEnum {
   String getTranslationKey();

   default String getTranslatedName() {
      return class_1074.method_4662(this.getTranslationKey(), new Object[0]);
   }
}
