package net.chuck.chuckpack.modules.movement;

import meteordevelopment.meteorclient.events.meteor.MouseScrollEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.orbit.EventHandler;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class FlightScrollHandler {
    @EventHandler
    private void onMouseScroll(MouseScrollEvent event) {
        if (mc.screen != null) return;
        if (!mc.options.keyShift.isDown()) return;

        Module flight = Modules.get().get(Flight.class);
        if (flight == null || !flight.isActive()) return;
        if (Modules.get().isActive(Freecam.class)) return;

        BoolSetting scrollSpeedEnabled = findBoolSetting(flight, "scroll-speed");
        if (scrollSpeedEnabled == null || !scrollSpeedEnabled.get()) return;

        DoubleSetting sensitivity = findDoubleSetting(flight, "scroll-sensitivity");
        if (sensitivity == null) return;

        DoubleSetting speed = findDoubleSetting(flight, "speed");
        if (speed == null) return;

        double current = speed.get();
        double newVal = Math.max(0.0, current + event.value * sensitivity.get());
        speed.set(newVal);

        event.cancel();
    }

    private BoolSetting findBoolSetting(Module module, String name) {
        for (SettingGroup group : module.settings) {
            for (Setting<?> setting : group) {
                if (name.equals(setting.name) && setting instanceof BoolSetting bs) {
                    return bs;
                }
            }
        }
        return null;
    }

    private DoubleSetting findDoubleSetting(Module module, String name) {
        for (SettingGroup group : module.settings) {
            for (Setting<?> setting : group) {
                if (name.equals(setting.name) && setting instanceof DoubleSetting ds) {
                    return ds;
                }
            }
        }
        return null;
    }
}
