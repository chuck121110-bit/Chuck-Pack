package xaeroplus.mixin.client;

import net.minecraft.class_2561;
import net.minecraft.class_437;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.gui.GuiMinimapMain;
import xaero.common.gui.GuiMinimapSettings;
import xaero.lib.client.gui.ISettingEntry;
import xaero.lib.client.gui.config.context.IEditConfigScreenContext;
import xaeroplus.feature.extensions.IXaeroPlusSettingEntry;
import xaeroplus.settings.SettingLocation;
import xaeroplus.settings.Settings;

@Mixin(
   value = {GuiMinimapMain.class},
   remap = false
)
public abstract class MixinGuiMinimapMain extends GuiMinimapSettings {
   @Shadow
   private ISettingEntry[] mainEntries;

   public MixinGuiMinimapMain(final class_2561 title, final class_437 par1Screen, final class_437 escScreen, final IEditConfigScreenContext context) {
      super(title, par1Screen, escScreen, context);
   }

   @Inject(
      method = {"<init>(Lxaero/common/IXaeroMinimap;Lnet/minecraft/class_437;Lnet/minecraft/class_437;ZLxaero/lib/client/gui/config/context/IEditConfigScreenContext;)V"},
      at = {@At("RETURN")},
      remap = true
   )
   public void init(final CallbackInfo ci) {
      IXaeroPlusSettingEntry[] configSettingEntries = Settings.REGISTRY.getXaeroSettingEntries(SettingLocation.MINIMAP_MAIN);
      int oldLen = this.mainEntries.length;
      int newLen = configSettingEntries.length;
      int totalNewLen = oldLen + configSettingEntries.length;
      ISettingEntry[] newEntries = new ISettingEntry[totalNewLen];
      System.arraycopy(this.mainEntries, 0, newEntries, 0, 2);
      System.arraycopy(configSettingEntries, 0, newEntries, 2, newLen);
      System.arraycopy(this.mainEntries, 2, newEntries, 2 + newLen, oldLen - 2);
      this.mainEntries = newEntries;
   }
}
