package xaero.hud.minimap.controls.key.function;

import java.util.function.Supplier;
import xaero.common.HudMod;
import xaero.hud.controls.key.function.KeyMappingFunction;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.common.config.option.BooleanConfigOption;
import xaero.lib.common.config.profile.ConfigProfile;

public class ToggleSettingFunction extends KeyMappingFunction {
   private final Supplier<BooleanConfigOption> settingSupplier;

   protected ToggleSettingFunction(Supplier<BooleanConfigOption> settingSupplier) {
      super(false);
      this.settingSupplier = settingSupplier;
   }

   public void onPress() {
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      ConfigProfile currentProfile = configManager.getCurrentProfile();
      BooleanConfigOption option = (BooleanConfigOption)this.settingSupplier.get();
      currentProfile.set(option, !(Boolean)currentProfile.get(option));
      HudMod.INSTANCE.getHudConfigs().getClientConfigProfileIO().save(currentProfile);
   }

   public void onRelease() {
   }
}
