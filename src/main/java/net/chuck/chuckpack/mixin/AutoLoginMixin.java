package net.chuck.chuckpack.mixin;

import net.chuck.chuckpack.modules.misc.AutoLogin;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class AutoLoginMixin {

    @Inject(method = "sendChat", at = @At("HEAD"))
    private void onSendChatMessage(String message, CallbackInfo ci) {
        AutoLogin.onChatSent(message);
    }

    @Inject(method = "sendCommand", at = @At("HEAD"))
    private void onSendChatCommand(String command, CallbackInfo ci) {
        AutoLogin.onChatSent("/" + command);
    }
}
