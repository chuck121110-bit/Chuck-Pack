package xaero.map.radar.tracker;

import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import net.minecraft.class_1044;
import net.minecraft.class_10799;
import net.minecraft.class_12247;
import net.minecraft.class_1657;
import net.minecraft.class_1664;
import net.minecraft.class_2561;
import net.minecraft.class_287;
import net.minecraft.class_289;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_4587;
import xaero.lib.client.graphics.XaeroRenderType;
import xaero.lib.client.graphics.util.ImmediateRenderUtil;
import xaero.map.element.MapElementGraphics;
import xaero.map.graphics.MapRenderHelper;

public class PlayerTrackerIconRenderer {
   public boolean isPlayerUpsideDown(class_1657 player) {
      class_2561 playerCustomName = player.method_5797();
      return playerCustomName != null && isUpsideDownName(playerCustomName.getString());
   }

   protected static boolean isUpsideDownName(String playerName) {
      return "Dinnerbone".equals(playerName) || "Grumm".equals(playerName);
   }

   public void renderIcon(MapElementGraphics guiGraphics, class_1657 player, class_2960 skinTextureLocation) {
      class_4587 matrixStack = guiGraphics.pose();
      boolean upsideDown = player != null && this.isPlayerUpsideDown(player);
      int textureY = 8 + (upsideDown ? 8 : 0);
      int textureH = 8 * (upsideDown ? -1 : 1);
      class_1044 abstractTexture = class_310.method_1551().method_1531().method_4619(skinTextureLocation);
      if (abstractTexture != null) {
         class_287 bufferbuilder = class_289.method_1348().method_60827(class_5596.field_27382, XaeroRenderType.POSITION_COLOR_TEX);
         MapRenderHelper.blitIntoExistingBuffer(matrixStack.method_23760().method_23761(), bufferbuilder, -4.0F, -4.0F, 8, textureY, 8, 8, 8, textureH, 1.0F, 1.0F, 1.0F, 1.0F, 64, 64);
         if (player != null && player.method_74091(class_1664.field_7563)) {
            textureY = 8 + (upsideDown ? 8 : 0);
            textureH = 8 * (upsideDown ? -1 : 1);
            MapRenderHelper.blitIntoExistingBuffer(matrixStack.method_23760().method_23761(), bufferbuilder, -4.0F, -4.0F, 40, textureY, 8, 8, 8, textureH, 1.0F, 1.0F, 1.0F, 1.0F, 64, 64);
         }

         ImmediateRenderUtil.drawImmediateMeshData(bufferbuilder.method_60794(), XaeroRenderType.RP_POSITION_COLOR_TEX_TRANSLUCENT_CULL, new class_12247.class_12337(abstractTexture.method_71659(), abstractTexture.method_75484()));
      }
   }

   public void renderIconGUI(class_332 guiGraphics, class_1657 player, class_2960 skinTextureLocation) {
      boolean upsideDown = player != null && this.isPlayerUpsideDown(player);
      int textureY = 8 + (upsideDown ? 8 : 0);
      int textureH = 8 * (upsideDown ? -1 : 1);
      guiGraphics.method_25293(class_10799.field_56883, skinTextureLocation, -4, -4, 8.0F, (float)textureY, 8, 8, 8, textureH, 64, 64, -1);
      if (player != null && player.method_74091(class_1664.field_7563)) {
         textureY = 8 + (upsideDown ? 8 : 0);
         textureH = 8 * (upsideDown ? -1 : 1);
         guiGraphics.method_25293(class_10799.field_56883, skinTextureLocation, -4, -4, 40.0F, (float)textureY, 8, 8, 8, textureH, 64, 64, -1);
      }

   }
}
