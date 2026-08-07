package xaero.common;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import xaero.common.events.ModClientEventsFabric;
import xaero.hud.minimap.render.MinimapPipRenderState;
import xaero.hud.minimap.render.MinimapPipRenderer;

public class PlatformContextLoaderClientOnlyFabric extends PlatformContextLoaderClientOnly {
   public void preInit(String modId, IXaeroMinimap modMain) {
      ((ModClientEventsFabric)modMain.getModClientEvents()).register();
      modMain.getSupportMods().registerClientEvents();
      modMain.ensureControlsRegister();
      modMain.getControlsRegister().registerKeybindings(KeyBindingHelper::registerKeyBinding, (c) -> {
      });
      SpecialGuiElementRegistry.register((ctx) -> new MinimapPipRenderer(MinimapPipRenderState.class, ctx.vertexConsumers()));
      SpecialGuiElementRegistry.register((ctx) -> new MinimapPipRenderer(MinimapPipRenderState.Enlarged.class, ctx.vertexConsumers()));
   }
}
