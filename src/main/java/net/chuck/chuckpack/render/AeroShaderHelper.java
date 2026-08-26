package net.chuck.chuckpack.render;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShaders;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

public class AeroShaderHelper {
    private static boolean dirty;
    private static Runnable pendingTracers;

    public static void markDirty() {
        dirty = true;
    }

    public static void setPendingTracers(Runnable tracers) {
        pendingTracers = tracers;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private static void onRender3D(Render3DEvent event) {
        if (!dirty) return;
        dirty = false;
        PostProcessShaders.STORAGE_OUTLINE.render();

        if (pendingTracers != null) {
            pendingTracers.run();
            pendingTracers = null;
        }
    }
}
