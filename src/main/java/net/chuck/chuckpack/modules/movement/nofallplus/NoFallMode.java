package net.chuck.chuckpack.modules.movement.nofallplus;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import net.minecraft.client.Minecraft;

public class NoFallMode {
    protected final Minecraft mc = Minecraft.getInstance();
    protected final NoFallPlus settings;

    public NoFallMode(NoFallPlus settings) {
        this.settings = settings;
    }

    public void onActivate() {}
    public void onDeactivate() {}
    public void onTickEventPre(TickEvent.Pre event) {}
    public void onTickEventPost(TickEvent.Post event) {}
    public void onSendPacket(PacketEvent.Send event) {}
    public void onSentPacket(PacketEvent.Sent event) {}
}
