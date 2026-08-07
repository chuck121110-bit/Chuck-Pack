package xaeroplus.feature.extensions;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_339;
import net.minecraft.class_437;
import xaero.common.gui.TooltipButton;
import xaero.lib.client.gui.ScreenBase;
import xaero.lib.client.gui.widget.Tooltip;
import xaeroplus.settings.StringSetting;
import xaeroplus.settings.XaeroPlusSetting;

public class XaeroPlusScreenSwitchSettingEntry implements IXaeroPlusSettingEntry {
   private final StringSetting xaeroPlusSetting;

   public XaeroPlusScreenSwitchSettingEntry(final StringSetting xaeroPlusSetting) {
      this.xaeroPlusSetting = xaeroPlusSetting;
   }

   public XaeroPlusSetting getXaeroPlusSetting() {
      return this.xaeroPlusSetting;
   }

   public String getStringForSearch() {
      return this.xaeroPlusSetting.getTranslatedName();
   }

   public class_339 createWidget(final int x, final int y, final int w) {
      TooltipButton button = new TooltipButton(x, y, w, 20, class_2561.method_43471(this.xaeroPlusSetting.getSettingNameTranslationKey()), (b) -> {
         class_310 mc = class_310.method_1551();
         class_437 current = mc.field_1755;
         class_437 currentEscScreen = current instanceof ScreenBase ? ((ScreenBase)current).escape : null;
         class_437 targetScreen = this.xaeroPlusSetting.getScreenSupplier().getScreen(current, currentEscScreen, this.xaeroPlusSetting);
         mc.method_1507(targetScreen);
      }, () -> new Tooltip(this.xaeroPlusSetting.getTooltipTranslationKey()));
      button.field_22763 = this.xaeroPlusSetting.isVisible();
      return button;
   }
}
