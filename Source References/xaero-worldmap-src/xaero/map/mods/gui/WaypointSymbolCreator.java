package xaero.map.mods.gui;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_10366;
import net.minecraft.class_10799;
import net.minecraft.class_11278;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import org.joml.Matrix4fStack;
import xaero.lib.client.graphics.util.ImmediateRenderUtil;
import xaero.lib.client.graphics.util.TextureUtils;
import xaero.map.element.MapElementGraphics;
import xaero.map.exception.OpenGLException;
import xaero.map.graphics.ImprovedFramebuffer;
import xaero.map.icon.XaeroIcon;
import xaero.map.icon.XaeroIconAtlas;
import xaero.map.icon.XaeroIconAtlasManager;
import xaero.map.misc.Misc;

public class WaypointSymbolCreator {
   private static final int PREFERRED_ATLAS_WIDTH = 1024;
   private static final int ICON_WIDTH = 64;
   public static final class_2960 minimapTextures = class_2960.method_60655("xaerobetterpvp", "gui/guis.png");
   public static final int white = -1;
   private class_310 mc = class_310.method_1551();
   private XaeroIcon deathSymbolTexture;
   private final Map<String, XaeroIcon> charSymbols = new HashMap();
   private XaeroIconAtlasManager iconManager;
   private ImprovedFramebuffer atlasRenderFramebuffer;
   private XaeroIconAtlas lastAtlas;
   private class_11278 orthoProjectionCache;

   public XaeroIcon getDeathSymbolTexture(MapElementGraphics guiGraphics) {
      if (this.deathSymbolTexture == null) {
         this.createDeathSymbolTexture(guiGraphics);
      }

      return this.deathSymbolTexture;
   }

   private void createDeathSymbolTexture(MapElementGraphics guiGraphics) {
      this.deathSymbolTexture = this.createCharSymbol(guiGraphics, true, (String)null);
   }

   public XaeroIcon getSymbolTexture(MapElementGraphics guiGraphics, String c) {
      XaeroIcon icon;
      synchronized(this.charSymbols) {
         icon = (XaeroIcon)this.charSymbols.get(c);
      }

      if (icon == null) {
         icon = this.createCharSymbol(guiGraphics, false, c);
      }

      return icon;
   }

   private XaeroIcon createCharSymbol(MapElementGraphics guiGraphics, boolean death, String c) {
      if (this.iconManager == null) {
         int maxTextureSize = RenderSystem.getDevice().getMaxTextureSize();
         int atlasTextureSize = Math.min(maxTextureSize, 1024) / 64 * 64;
         this.atlasRenderFramebuffer = new ImprovedFramebuffer(atlasTextureSize, atlasTextureSize, true);
         this.atlasRenderFramebuffer.closeColorTexture();
         OpenGLException.checkGLError();
         this.atlasRenderFramebuffer.setColorTexture((GpuTexture)null, (GpuTextureView)null);
         this.iconManager = new XaeroIconAtlasManager(64, atlasTextureSize, new ArrayList());
         this.orthoProjectionCache = new class_11278("waypoint symbol creator", -1.0F, 1000.0F, true);
      }

      XaeroIconAtlas atlas = this.iconManager.getCurrentAtlas();
      XaeroIcon icon = atlas.createIcon();
      guiGraphics.flush();
      this.atlasRenderFramebuffer.bindAsMainTarget(false);
      this.atlasRenderFramebuffer.setColorTexture(atlas);
      if (this.lastAtlas != atlas) {
         TextureUtils.clearRenderTarget(this.atlasRenderFramebuffer, 0, 1.0F);
         this.lastAtlas = atlas;
      }

      GpuBufferSlice ortho = this.orthoProjectionCache.method_71092((float)this.atlasRenderFramebuffer.field_1482, (float)this.atlasRenderFramebuffer.field_1481);
      RenderSystem.setProjectionMatrix(ortho, class_10366.field_54954);
      Matrix4fStack shaderMatrixStack = RenderSystem.getModelViewStack();
      shaderMatrixStack.pushMatrix();
      shaderMatrixStack.identity();
      class_4587 matrixStack = guiGraphics.pose();
      matrixStack.method_22903();
      matrixStack.method_34426();
      matrixStack.method_46416((float)icon.getOffsetX(), (float)(this.atlasRenderFramebuffer.field_1481 - 64 - icon.getOffsetY()), 0.0F);
      matrixStack.method_46416(2.0F, 2.0F, 0.0F);
      if (!death) {
         matrixStack.method_22905(3.0F, 3.0F, 1.0F);
         guiGraphics.drawString(this.mc.field_1772, (String)c, 0, 0, -1);
      } else {
         matrixStack.method_22905(3.0F, 3.0F, 1.0F);
         ImmediateRenderUtil.setShaderColor(0.243F, 0.243F, 0.243F, 1.0F);
         guiGraphics.blit((class_2960)minimapTextures, 1, 1, 0, 87, 9, 9, 9, 78, 256, class_10799.field_56883);
         ImmediateRenderUtil.setShaderColor(0.988F, 0.988F, 0.988F, 1.0F);
         guiGraphics.blit((class_2960)minimapTextures, 0, 0, 0, 87, 9, 9, 9, 78, 256, class_10799.field_56883);
         ImmediateRenderUtil.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      }

      matrixStack.method_22909();
      guiGraphics.flush();
      Misc.minecraftOrtho(this.mc, false);
      shaderMatrixStack.popMatrix();
      this.atlasRenderFramebuffer.bindDefaultFramebuffer(this.mc);
      if (death) {
         this.deathSymbolTexture = icon;
      } else {
         synchronized(this.charSymbols) {
            this.charSymbols.put(c, icon);
         }
      }

      return icon;
   }

   public void resetChars() {
      synchronized(this.charSymbols) {
         this.charSymbols.clear();
      }

      this.lastAtlas = null;
      this.deathSymbolTexture = null;
      if (this.iconManager != null) {
         this.iconManager.clearAtlases();
         this.atlasRenderFramebuffer.setColorTexture((GpuTexture)null, (GpuTextureView)null);
      }

   }
}
