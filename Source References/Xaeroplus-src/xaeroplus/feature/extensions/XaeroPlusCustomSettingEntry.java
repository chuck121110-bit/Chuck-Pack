package xaeroplus.feature.extensions;

import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import net.minecraft.class_2561;
import xaero.lib.client.gui.CustomSettingEntry;
import xaero.lib.common.gui.widget.TooltipInfo;
import xaeroplus.settings.XaeroPlusSetting;

public class XaeroPlusCustomSettingEntry<T> extends CustomSettingEntry<T> implements IXaeroPlusSettingEntry {
   private final XaeroPlusSetting xaeroPlusSetting;

   public XaeroPlusCustomSettingEntry(final XaeroPlusSetting xaeroPlusSetting, final class_2561 name, final TooltipInfo tooltipInfo, final boolean slider, final Supplier<T> currentValueSupplier, final int minIndex, final int maxIndex, final IntFunction<T> indexReader, final Function<T, class_2561> valueNamer, final BiConsumer<T, T> onValueChange, final BooleanSupplier activeSupplier) {
      super(() -> false, name, tooltipInfo, slider, currentValueSupplier, minIndex, maxIndex, indexReader, valueNamer, onValueChange, activeSupplier);
      this.xaeroPlusSetting = xaeroPlusSetting;
   }

   public XaeroPlusSetting getXaeroPlusSetting() {
      return this.xaeroPlusSetting;
   }
}
