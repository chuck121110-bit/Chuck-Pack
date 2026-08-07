package net.aero.aeropack.mixin;

import meteordevelopment.meteorclient.systems.modules.misc.swarm.SwarmConnection;
import meteordevelopment.meteorclient.systems.modules.misc.swarm.SwarmHost;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SwarmHost.class)
public class SwarmHostMixin {

    @Inject(method = "getConnectionCount", at = @At("HEAD"), cancellable = true)
    private void aeropack$fixWorkerCount(CallbackInfoReturnable<Integer> cir) {
        int count = aeropack$cleanAndCount();
        cir.setReturnValue(count);
    }

    @Inject(method = "sendMessage", at = @At("HEAD"))
    private void aeropack$cleanBeforeSend(String s, CallbackInfo ci) {
        aeropack$cleanAndCount();
    }

    @Unique
    private int aeropack$cleanAndCount() {
        SwarmHost self = (SwarmHost) (Object) this;
        SwarmConnection[] conns = self.getConnections();
        int count = 0;
        for (int i = 0; i < conns.length; i++) {
            SwarmConnection conn = conns[i];
            if (conn == null) continue;
            if (!conn.isAlive() || conn.socket.isClosed()) {
                conns[i] = null;
                try { conn.socket.close(); } catch (Exception ignored) {}
                continue;
            }
            count++;
        }
        return count;
    }
}
