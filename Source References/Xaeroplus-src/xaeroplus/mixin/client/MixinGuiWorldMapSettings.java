package xaeroplus.mixin.client;

import net.minecraft.class_2561;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.lib.client.gui.ISettingEntry;
import xaero.lib.client.gui.config.EditConfigScreen;
import xaero.lib.client.gui.config.context.IEditConfigScreenContext;
import xaero.lib.common.config.channel.ConfigChannel;
import xaero.map.gui.GuiWorldMapSettings;
import xaero.map.gui.ScreenSwitchSettingEntry;
import xaeroplus.feature.extensions.GuiXaeroPlusWorldMapSettings;

@Mixin(
   value = {GuiWorldMapSettings.class},
   remap = false
)
public abstract class MixinGuiWorldMapSettings extends EditConfigScreen {
   public MixinGuiWorldMapSettings(final class_2561 title, final class_437 backScreen, final class_437 escScreen, final IEditConfigScreenContext context, final ConfigChannel channel) {
      super(title, backScreen, escScreen, context, channel);
   }

   @Inject(
      method = {"<init>(Lnet/minecraft/class_437;Lnet/minecraft/class_437;Lxaero/lib/client/gui/config/context/IEditConfigScreenContext;)V"},
      at = {@At("RETURN")},
      remap = true
   )
   public void init(final class_437 parent, final class_437 escapeScreen, final IEditConfigScreenContext context, final CallbackInfo ci) {
      int oldLen = this.entries.length;
      int totalNewLen = oldLen + 1;
      ISettingEntry[] newEntries = new ISettingEntry[totalNewLen];
      ScreenSwitchSettingEntry xpScreenSwitchEntry = GuiXaeroPlusWorldMapSettings.getScreenSwitchSettingEntry(parent);
      System.arraycopy(this.entries, 0, newEntries, 0, 2);
      newEntries[2] = xpScreenSwitchEntry;
      System.arraycopy(this.entries, 2, newEntries, 3, oldLen - 2);
      this.entries = newEntries;
   }
}
