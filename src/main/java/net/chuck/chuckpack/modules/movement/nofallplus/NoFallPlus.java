package net.chuck.chuckpack.modules.movement.nofallplus;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.chuck.chuckpack.modules.movement.nofallplus.modes.*;
import net.chuck.chuckpack.ChuckPack;

public class NoFallPlus extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<NoFallModes> mode = sgGeneral.add(new EnumSetting.Builder<NoFallModes>()
        .name("mode")
        .description("The method of applying nofall.")
        .defaultValue(NoFallModes.No_Ground)
        .onChanged(this::onModeChanged)
        .build()
    );

    private NoFallMode currentMode;

    public NoFallPlus() {
        super(ChuckPack.CHUCK_CATEGORY, "no-fall+", "Bypass fall damage or reduce fall damage");
        currentMode = new No_Ground(this);
    }

    private void onModeChanged(NoFallModes mode) {
        switch (mode) {
            case Matrix_New -> currentMode = new MatrixNew(this);
            case Vulcan -> currentMode = new Vulcan(this);
            case Vulcan_2dot7dot7 -> currentMode = new Vulcan277(this);
            case Verus -> currentMode = new Verus(this);
            case Elytra_Clip -> currentMode = new Eclip(this);
            case Elytra_Fly -> currentMode = new ElytraFly(this);
            case No_Ground -> currentMode = new No_Ground(this);
            case No_Ground_Elytra -> currentMode = new No_Ground_Elytra(this);
        }
    }

    @Override
    public void onActivate() {
        onModeChanged(mode.get());
        currentMode.onActivate();
    }

    @Override
    public void onDeactivate() {
        currentMode.onDeactivate();
    }

    private void onPreTick(TickEvent.Pre event) {
        currentMode.onTickEventPre(event);
    }

    private void onPostTick(TickEvent.Post event) {
        currentMode.onTickEventPost(event);
    }

    private void onSendPacket(PacketEvent.Send event) {
        currentMode.onSendPacket(event);
    }

    private void onSentPacket(PacketEvent.Sent event) {
        currentMode.onSentPacket(event);
    }
}
