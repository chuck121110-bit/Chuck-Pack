package xaero.hud.minimap.player.tracker;

import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.class_1044;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_287;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_640;
import xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRenderer;
import xaero.hud.render.util.RenderBufferUtil;

public class PlayerTrackerIconRenderer {
   public boolean isPlayerUpsideDown(class_1657 player) {
      class_2561 playerCustomName = player.method_5797();
      return playerCustomName != null && isUpsideDownName(playerCustomName.getString());
   }

   protected static boolean isUpsideDownName(String playerName) {
      return "Dinnerbone".equals(playerName) || "Grumm".equals(playerName);
   }

   public void renderIcon(class_310 mc, MultiTextureRenderTypeRenderer renderer, class_4587 matrixStack, class_1657 player, class_2960 skinTextureLocation, class_640 playerInfo, float alpha) {
      boolean upsideDown = player != null && this.isPlayerUpsideDown(player);
      int textureY = 8 + (!upsideDown ? 8 : 0);
      int textureH = 8 * (!upsideDown ? -1 : 1);
      class_1044 texture = mc.method_1531().method_4619(skinTextureLocation);
      if (texture != null) {
         GpuTexture textureId = texture.method_68004();
         class_287 bufferbuilder = renderer.begin(textureId);
         RenderBufferUtil.addTexturedColoredRect(matrixStack.method_23760().method_23761(), bufferbuilder, -4.0F, -4.0F, 8, textureY, 8, 8, 8, textureH, 1.0F, 1.0F, 1.0F, alpha, 64.0F);
         if (playerInfo != null) {
            if (playerInfo.method_65195()) {
               textureY = 8 + (!upsideDown ? 8 : 0);
               textureH = 8 * (!upsideDown ? -1 : 1);
               RenderBufferUtil.addTexturedColoredRect(matrixStack.method_23760().method_23761(), bufferbuilder, -4.0F, -4.0F, 40, textureY, 8, 8, 8, textureH, 1.0F, 1.0F, 1.0F, alpha, 64.0F);
            }
         }
      }
   }
}
