package xaero.hud.minimap.info.render.compile;

import net.minecraft.class_2338;
import xaero.hud.minimap.info.InfoDisplay;
import xaero.hud.minimap.module.MinimapSession;

@FunctionalInterface
public interface InfoDisplayOnCompile<T> {
   void onCompile(InfoDisplay<T> var1, InfoDisplayCompiler var2, MinimapSession var3, int var4, class_2338 var5);
}
