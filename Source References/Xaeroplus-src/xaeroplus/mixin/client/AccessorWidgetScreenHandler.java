package xaeroplus.mixin.client;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import xaero.lib.client.gui.widget.online.Widget;
import xaero.lib.client.gui.widget.online.WidgetScreenHandler;

@Mixin(
   value = {WidgetScreenHandler.class},
   remap = false
)
public interface AccessorWidgetScreenHandler {
   @Invoker("addWidget")
   void invokeAddWidget(Widget widget);

   @Accessor("widgets")
   List<Widget> getWidgets();
}
