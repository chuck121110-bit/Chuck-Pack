package net.chuck.chuckpack.mixin;

import meteordevelopment.meteorclient.systems.modules.misc.swarm.SwarmConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.DataOutputStream;
import java.io.IOException;

@Mixin(SwarmConnection.class)
public abstract class SwarmConnectionMixin extends Thread {

    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void chuckpack\$silentRun(CallbackInfo ci) {
        ci.cancel();

        SwarmConnection self = (SwarmConnection) (Object) this;
        try {
            DataOutputStream out = new DataOutputStream(self.socket.getOutputStream());

            while (!self.isInterrupted()) {
                if (self.messageToSend != null) {
                    String msg = self.messageToSend;
                    self.messageToSend = null;
                    if (self.socket.isConnected() && !self.socket.isClosed()) {
                        try {
                            out.writeUTF(msg);
                            out.flush();
                        } catch (Exception ignored) {
                        }
                    }
                }
            }

            out.close();
        } catch (IOException ignored) {
        }
    }
}
