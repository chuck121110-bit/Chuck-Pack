package net.aero.aeropack.mixin;

import net.aero.aeropack.modules.misc.AutoLogin;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class AutoLoginMixin {

    @Inject(method = "sendChatMessage", at = @At("HEAD"))
    private void onSendChatMessage(String message, CallbackInfo ci) {
        AutoLogin.onChatSent(message);
    }

    @Inject(method = "sendChatCommand", at = @At("HEAD"))
    private void onSendChatCommand(String command, CallbackInfo ci) {
        AutoLogin.onChatSent("/" + command);
    }
}
