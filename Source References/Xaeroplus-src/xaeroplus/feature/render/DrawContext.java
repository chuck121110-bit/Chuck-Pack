package xaeroplus.feature.render;

import net.minecraft.class_4587;
import org.joml.Matrix4f;
import xaero.lib.client.graphics.XaeroBufferProvider;

public record DrawContext(class_4587 matrixStack, XaeroBufferProvider renderTypeBuffers, double fboScale, boolean worldmap, Matrix4f untranslatedMapViewMatrix, int cameraBlockX, int cameraBlockZ) {
}
