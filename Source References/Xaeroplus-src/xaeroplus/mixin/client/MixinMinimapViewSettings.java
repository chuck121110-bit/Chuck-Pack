package xaeroplus.mixin.client;

import net.minecraft.class_2561;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.gui.GuiMinimapSettings;
import xaero.common.gui.GuiMinimapViewSettings;
import xaero.lib.client.gui.ISettingEntry;
import xaero.lib.client.gui.config.context.IEditConfigScreenContext;
import xaeroplus.feature.extensions.IXaeroPlusSettingEntry;
import xaeroplus.settings.SettingLocation;
import xaeroplus.settings.Settings;

@Mixin(
   value = {GuiMinimapViewSettings.class},
   remap = false
)
public abstract class MixinMinimapViewSettings extends GuiMinimapSettings {
   public MixinMinimapViewSettings(final class_2561 title, final class_437 par1Screen, final class_437 escScreen, final IEditConfigScreenContext context) {
      super(title, par1Screen, escScreen, context);
   }

   @Inject(
      method = {"<init>"},
      at = {@At("RETURN")}
   )
   public void init(final CallbackInfo ci) {
      IXaeroPlusSettingEntry[] configSettingEntries = Settings.REGISTRY.getXaeroSettingEntries(SettingLocation.MINIMAP_VIEW);
      int oldLen = this.entries.length;
      int newLen = configSettingEntries.length;
      int totalNewLen = oldLen + configSettingEntries.length;
      ISettingEntry[] newEntries = new ISettingEntry[totalNewLen];
      System.arraycopy(this.entries, 0, newEntries, newLen, oldLen);
      System.arraycopy(configSettingEntries, 0, newEntries, 0, newLen);
      this.entries = newEntries;
   }
}
