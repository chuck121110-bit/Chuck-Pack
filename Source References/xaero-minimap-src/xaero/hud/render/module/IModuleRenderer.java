package xaero.hud.render.module;

import net.minecraft.class_332;
import xaero.hud.module.ModuleSession;

public interface IModuleRenderer<MS extends ModuleSession<MS>> {
   void render(MS var1, ModuleRenderContext var2, class_332 var3, float var4);
}
