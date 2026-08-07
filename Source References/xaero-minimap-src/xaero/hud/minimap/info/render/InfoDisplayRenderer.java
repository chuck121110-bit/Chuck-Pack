package xaero.hud.minimap.info.render;

import java.util.Iterator;
import java.util.List;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_332;
import xaero.common.HudMod;
import xaero.hud.minimap.Minimap;
import xaero.hud.minimap.common.config.MinimapConfigConstants;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.info.InfoDisplay;
import xaero.hud.minimap.info.render.compile.InfoDisplayCompiler;
import xaero.hud.minimap.module.MinimapSession;
import xaero.lib.client.config.ClientConfigManager;

public final class InfoDisplayRenderer {
   public static final int DEPTH_OFFSET = 2;
   private final InfoDisplayCompiler compiler;

   private InfoDisplayRenderer(InfoDisplayCompiler compiler) {
      this.compiler = compiler;
   }

   public void render(MinimapSession session, Minimap minimap, int height, int size, class_2338 playerPos, int scaledX, int scaledY, float mapScale, class_332 guiGraphics) {
      ClientConfigManager configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      Iterator<InfoDisplay<?>> iterator = minimap.getInfoDisplays().getManager().getOrderedStream().iterator();
      int interfaceSize = size;
      int scaledHeight = (int)((float)height * mapScale);
      int align = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.INFO_DISPLAY_ALIGNMENT);
      boolean under = scaledY + size / 2 < scaledHeight / 2;
      int stringY = scaledY + (under ? size : -9);
      int bgOpacityConfig = (Integer)configManager.getEffective(MinimapProfiledConfigOptions.INFO_DISPLAY_BG_OPACITY);
      int bgOpacityMask = bgOpacityConfig * 255 / 100 << 24;

      while(iterator.hasNext()) {
         InfoDisplay<?> infoDisplay = (InfoDisplay)iterator.next();
         List<class_2561> compiledLines = this.compiler.compile(infoDisplay, session, size, playerPos);
         int textColorIndex = infoDisplay.getTextColor();
         int backgroundColorIndex = infoDisplay.getBackgroundColor();
         int textColor = MinimapConfigConstants.COLORS[textColorIndex < 0 ? 15 : textColorIndex % MinimapConfigConstants.COLORS.length];
         int backgroundColor = backgroundColorIndex < 0 ? 0 : bgOpacityMask | MinimapConfigConstants.COLORS[backgroundColorIndex % MinimapConfigConstants.COLORS.length] & 16777215;
         int startIndex = 0;
         int endIndex = compiledLines.size();
         int step = 1;
         if (!under) {
            startIndex = endIndex - 1;
            endIndex = -1;
            step = -1;
         }

         for(int i = startIndex; i != endIndex; i += step) {
            class_2561 s = (class_2561)compiledLines.get(i);
            int stringWidth = class_310.method_1551().field_1772.method_27525(s);
            int stringX = scaledX + (align == 0 ? interfaceSize / 2 - stringWidth / 2 : (align == 1 ? 6 : interfaceSize - 6 - stringWidth));
            if (backgroundColor != 0) {
               guiGraphics.method_25294(stringX - 1, stringY - 1, stringX + stringWidth + 1, stringY + 9, backgroundColor);
            }

            guiGraphics.method_51439(class_310.method_1551().field_1772, s, stringX, stringY, textColor, true);
            stringY += 10 * step;
         }

         compiledLines.clear();
      }

   }

   public static final class Builder {
      private Builder() {
      }

      private Builder setDefault() {
         return this;
      }

      public InfoDisplayRenderer build() {
         return new InfoDisplayRenderer(InfoDisplayCompiler.Builder.begin().build());
      }

      public static Builder begin() {
         return (new Builder()).setDefault();
      }
   }
}
