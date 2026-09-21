package net.chuck.chuckpack.mixin;

import net.chuck.chuckpack.modules.misc.MapIntegration;
import net.chuck.chuckpack.modules.world.BaseFinder;
import net.chuck.chuckpack.util.XaeroWaypointHelper;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Inject(method = "handleComponentClicked", at = @At("HEAD"), cancellable = true)
    private void chuckpack$onChatClick(Style clicked, boolean allowInsertions, CallbackInfoReturnable<Boolean> cir) {
        if (clicked == null || clicked.getClickEvent() == null) return;

        ClickEvent clickEvent = clicked.getClickEvent();
        if (clickEvent instanceof ClickEvent.RunCommand cmd && cmd.command().equals(".openflaggedchunks")) {
            BaseFinder baseFinder = Modules.get().get(BaseFinder.class);
            if (baseFinder != null) {
                baseFinder.openFlaggedChunksScreen();
            }
            cir.setReturnValue(true);
        }

        if (clickEvent instanceof ClickEvent.RunCommand cmd && cmd.command().startsWith(".chatwaypoint-gui ")) {
            if (!MapIntegration.chatCoordsEnabled()) return;
            String[] parts = cmd.command().split(" ");
            if (parts.length == 4) {
                try {
                    int x = Integer.parseInt(parts[1]);
                    int z = Integer.parseInt(parts[3]);
                    int y;
                    if (parts[2].equals("~")) {
                        if (mc.player == null) {
                            cir.setReturnValue(true);
                            return;
                        }
                        y = mc.player.blockPosition().getY();
                    } else {
                        y = Integer.parseInt(parts[2]);
                    }
                    XaeroWaypointHelper.openChatWaypointGui(x, y, z);
                } catch (NumberFormatException ignored) {}
            }
            cir.setReturnValue(true);
        }
    }
}
