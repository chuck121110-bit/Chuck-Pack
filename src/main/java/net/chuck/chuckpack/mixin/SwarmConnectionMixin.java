package net.chuck.chuckpack.mixin;

import meteordevelopment.meteorclient.systems.modules.misc.swarm.SwarmConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.DataInputStream;
import java.net.Socket;

// Host side: meteor's SwarmConnection only ever WRITES to workers, so the
// worker->host direction is wide open. Read it here and route playertab
// reports into the shared registry. One reader per socket; the writer thread
// is untouched (separate stream direction, no interference).
@Mixin(SwarmConnection.class)
public class SwarmConnectionMixin {
    @Inject(method = "<init>(Ljava/net/Socket;)V", at = @At("TAIL"))
    private void chuckpack$startReader(Socket socket, CallbackInfo ci) {
        Thread reader = new Thread(() -> {
            try {
                DataInputStream in = new DataInputStream(socket.getInputStream());
                while (!Thread.currentThread().isInterrupted()) {
                    String msg;
                    try {
                        msg = in.readUTF();
                    } catch (java.io.EOFException | java.net.SocketException e) {
                        break;
                    }
                    if (msg != null && msg.startsWith("swarm ChuckPack-playertab ")) {
                        net.chuck.chuckpack.util.SwarmPlayerList.handlePayload(msg);
                    }
                }
            } catch (Throwable ignored) {}
        }, "ChuckPack-SwarmReader");
        reader.setDaemon(true);
        reader.start();
    }
}
