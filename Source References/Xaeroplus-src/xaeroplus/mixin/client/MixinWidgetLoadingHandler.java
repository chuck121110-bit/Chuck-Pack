package xaeroplus.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xaero.lib.client.gui.GuiSettings;
import xaero.lib.client.gui.widget.online.Alignment;
import xaero.lib.client.gui.widget.online.ClickAction;
import xaero.lib.client.gui.widget.online.HoverAction;
import xaero.lib.client.gui.widget.online.TextWidgetBuilder;
import xaero.lib.client.gui.widget.online.Widget;
import xaero.lib.client.gui.widget.online.WidgetLoadingHandler;
import xaero.lib.client.gui.widget.online.WidgetScreenHandler;

@Mixin(
   value = {WidgetLoadingHandler.class},
   remap = false
)
public class MixinWidgetLoadingHandler {
   @Shadow
   private WidgetScreenHandler handler;

   @Overwrite
   public void loadWidget(String serialized) {
      ((AccessorWidgetScreenHandler)this.handler).getWidgets().clear();
      TextWidgetBuilder builder = new TextWidgetBuilder();
      builder.setText("§7§kaa§r §l§bXaero§aPlus§r §7§kaa§r");
      builder.setAlignment(Alignment.CENTER);
      builder.setOnClick(ClickAction.URL);
      builder.setUrl("https://github.com/rfresh2/XaeroPlus");
      builder.setTooltip("Click to open the link");
      builder.setOnHover(HoverAction.TOOLTIP);
      builder.setX(0);
      builder.setY(20);
      builder.setHorizontalAnchor(0.5F);
      builder.setVerticalAnchor(0.0F);
      builder.setNoGuiScale(false);
      builder.setLocation(GuiSettings.class);
      Widget widget = builder.build();
      ((AccessorWidgetScreenHandler)this.handler).invokeAddWidget(widget);
   }
}
