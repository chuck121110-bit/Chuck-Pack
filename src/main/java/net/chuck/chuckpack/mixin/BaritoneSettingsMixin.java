package net.chuck.chuckpack.mixin;

import baritone.api.Settings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Settings.class, remap = false)
public class BaritoneSettingsMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void chuckpack$enableNotifications(CallbackInfo ci) {
        Settings self = (Settings) (Object) this;
        self.logAsToast.value = true;
        self.chatDebug.value = true;
        self.desktopNotifications.value = true;
        self.notificationOnPathComplete.value = true;
        self.notificationOnMineFail.value = true;
        self.notificationOnBuildFinished.value = true;
        self.notificationOnExploreFinished.value = true;
        self.notificationOnFarmFail.value = true;
    }
}
