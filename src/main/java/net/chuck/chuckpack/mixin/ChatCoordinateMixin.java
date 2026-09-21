package net.chuck.chuckpack.mixin;

import net.chuck.chuckpack.modules.misc.ChatWaypoints;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// Thin hook: all coordinate logic lives in MapIntegration (chat-coords-click
// setting) so the feature is owned by that module, not the client core.
@Mixin(ChatComponent.class)
public class ChatCoordinateMixin {

    @ModifyVariable(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V", at = @At("HEAD"), argsOnly = true)
    private Component highlightCoords(Component message) {
        if (!ChatWaypoints.chatCoordsEnabled()) return message;
        return ChatWaypoints.highlightCoords(message);
    }
}
