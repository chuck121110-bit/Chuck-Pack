package xaero.hud.minimap.module;

import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_408;
import net.minecraft.class_418;
import xaero.common.HudMod;
import xaero.common.core.IGuiGraphics;
import xaero.common.effect.Effects;
import xaero.common.misc.Misc;
import xaero.hud.gui.util.GuiUtils;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.render.MinimapPipRenderState;
import xaero.hud.render.module.IModuleRenderer;
import xaero.hud.render.module.ModuleRenderContext;
import xaero.lib.XaeroLib;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.client.gui.IScreenBase;

public class MinimapRenderer implements IModuleRenderer<MinimapSession> {
   public void render(MinimapSession session, ModuleRenderContext c, class_332 guiGraphics, float partialTicks) {
      class_310 mc = class_310.method_1551();
      if (!Misc.hasEffect(mc.field_1724, Effects.NO_MINIMAP) && !Misc.hasEffect(mc.field_1724, Effects.NO_MINIMAP_HARMFUL) && !session.getProcessor().getNoMinimapMessageReceived()) {
         if ((!session.getHideMinimapUnderScreen() || mc.field_1755 == null || mc.field_1755 instanceof IScreenBase || mc.field_1755 instanceof class_408 || mc.field_1755 instanceof class_418) && (!session.getHideMinimapUnderF3() || !mc.field_61504.method_72776())) {
            int renderX = c.x;
            int renderY = c.y;
            MinimapPipRenderState renderState = session.getProcessor().getRenderState();
            if (session.getProcessor().isEnlargedMap()) {
               renderState = session.getProcessor().getEnlargedRenderState();
               if ((Boolean)HudMod.INSTANCE.getHudConfigs().getClientConfigManager().getEffective(MinimapProfiledConfigOptions.CENTERED_ENLARGED)) {
                  renderX = (c.screenWidth - c.w) / 2;
                  renderY = (c.screenHeight - c.w) / 2;
               }
            }

            ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
            float minimapScale = GuiUtils.getMinimapScale(configManager);
            renderState.update(renderX, renderY, c.screenWidth, c.screenHeight, c.screenScale, minimapScale, session.getConfiguredWidth(), c.w, partialTicks, XaeroLib.INSTANCE.getClient().getBufferProvider());
            ((IGuiGraphics)guiGraphics).xaero_mm_getGuiRenderState().method_70922(renderState);
            session.getProcessor().getRenderer().renderOutsidePip(session, renderX, renderY, c.screenWidth, c.screenHeight, c.screenScale, minimapScale, session.getConfiguredWidth(), partialTicks, guiGraphics);
         }
      }
   }
}
