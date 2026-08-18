package net.chuck.chuckpack.modules.render;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;

public class CoordinateLogout extends Module {
    private final SettingGroup sgTarget = settings.createGroup("Target");
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Double> targetX = sgTarget.add(new DoubleSetting.Builder()
        .name("X")
        .description("Target X coordinate.")
        .defaultValue(0)
        .noSlider()
        .build()
    );

    private final Setting<Double> targetY = sgTarget.add(new DoubleSetting.Builder()
        .name("Y")
        .description("Target Y coordinate.")
        .defaultValue(0)
        .noSlider()
        .build()
    );

    private final Setting<Double> targetZ = sgTarget.add(new DoubleSetting.Builder()
        .name("Z")
        .description("Target Z coordinate.")
        .defaultValue(0)
        .noSlider()
        .build()
    );

    private final Setting<Double> radius = sgTarget.add(new DoubleSetting.Builder()
        .name("radius")
        .description("Logout radius in blocks.")
        .defaultValue(1000)
        .min(1)
        .sliderRange(1, 10000)
        .build()
    );

    private final Setting<Boolean> renderSphere = sgRender.add(new BoolSetting.Builder()
        .name("render-sphere")
        .description("Render a wireframe sphere at the target.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> segments = sgRender.add(new IntSetting.Builder()
        .name("segments")
        .description("Circle smoothness.")
        .defaultValue(64)
        .min(8)
        .sliderRange(8, 128)
        .visible(renderSphere::get)
        .build()
    );

    private final Setting<meteordevelopment.meteorclient.utils.render.color.SettingColor> sphereColor = sgRender.add(new meteordevelopment.meteorclient.settings.ColorSetting.Builder()
        .name("color")
        .description("Sphere color.")
        .defaultValue(new meteordevelopment.meteorclient.utils.render.color.SettingColor(255, 0, 0, 100))
        .visible(renderSphere::get)
        .build()
    );

    public CoordinateLogout() {
        super(Categories.Render, "coordinate-logout",
            "Logs you out when you get within a radius of the target coordinates.");
    }

    private boolean disconnected = false;

    @Override
    public void onActivate() {
        disconnected = false;
    }

    @Override
    public void onDeactivate() {
        disconnected = false;
    }

    @EventHandler
    private void onTick(meteordevelopment.meteorclient.events.world.TickEvent.Pre event) {
        if (disconnected) return;
        if (mc.player == null || mc.getConnection() == null) return;

        double dx = mc.player.getX() - targetX.get();
        double dy = mc.player.getY() - targetY.get();
        double dz = mc.player.getZ() - targetZ.get();
        double distSq = dx * dx + dy * dy + dz * dz;
        double radiusSq = radius.get() * radius.get();

        if (distSq <= radiusSq) {
            disconnected = true;
            hardDisconnect("Coordinate-Logout: Reached target");
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (disconnected) return;
        if (!renderSphere.get()) return;

        float cx = targetX.get().floatValue();
        float cy = targetY.get().floatValue();
        float cz = targetZ.get().floatValue();
        float r = radius.get().floatValue();
        int seg = segments.get();
        var color = sphereColor.get();

        drawCircle(event, cx, cy, cz, r, seg, color, true, false);
        drawCircle(event, cx, cy, cz, r, seg, color, false, true);
        drawCircle(event, cx, cy, cz, r, seg, color, false, false);
    }

    private void drawCircle(Render3DEvent event, float cx, float cy, float cz, float r, int seg,
                            meteordevelopment.meteorclient.utils.render.color.SettingColor color,
                            boolean xyPlane, boolean xzPlane) {
        var renderer = event.renderer;
        double step = Math.PI * 2.0 / seg;

        for (int i = 0; i < seg; i++) {
            double a1 = step * i;
            double a2 = step * (i + 1);

            float x1, y1, z1, x2, y2, z2;

            if (xyPlane) {
                x1 = cx + (float)(Math.cos(a1) * r);
                y1 = cy + (float)(Math.sin(a1) * r);
                z1 = cz;
                x2 = cx + (float)(Math.cos(a2) * r);
                y2 = cy + (float)(Math.sin(a2) * r);
                z2 = cz;
            } else if (xzPlane) {
                x1 = cx + (float)(Math.cos(a1) * r);
                y1 = cy;
                z1 = cz + (float)(Math.sin(a1) * r);
                x2 = cx + (float)(Math.cos(a2) * r);
                y2 = cy;
                z2 = cz + (float)(Math.sin(a2) * r);
            } else {
                x1 = cx;
                y1 = cy + (float)(Math.cos(a1) * r);
                z1 = cz + (float)(Math.sin(a1) * r);
                x2 = cx;
                y2 = cy + (float)(Math.cos(a2) * r);
                z2 = cz + (float)(Math.sin(a2) * r);
            }

            renderer.line(x1, y1, z1, x2, y2, z2, color);
        }
    }

    @Override
    public String getInfoString() {
        return String.format("%.0f, %.0f, %.0f", targetX.get(), targetY.get(), targetZ.get());
    }

    private static boolean isDisconnecting = false;

    public static void hardDisconnect(String reason) {
        if (isDisconnecting) return;
        isDisconnecting = true;

        try {
            Module autoReconnect = Modules.get().get("auto-reconnect");
            if (autoReconnect != null) {
                try {
                    Field field = autoReconnect.getClass().getDeclaredField("lastServerConnection");
                    field.setAccessible(true);
                    field.set(autoReconnect, null);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player != null && mc.player.connection != null) {
                mc.player.connection.handleDisconnect(new ClientboundDisconnectPacket(Component.literal(reason)));
            }
        } catch (Exception ignored) {}

        isDisconnecting = false;
    }
}
