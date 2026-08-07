package xaeroplus.fabric.mixin.client.fabric;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.class_2561;
import org.spongepowered.asm.mixin.Mixin;
import xaeroplus.commands.XPClientCommandSource;

@Mixin({FabricClientCommandSource.class})
public interface MixinFabricClientCommandSource extends XPClientCommandSource {
   default void xaeroplus$sendSuccess(class_2561 message) {
      ((FabricClientCommandSource)this).sendFeedback(message);
   }

   default void xaeroplus$sendFailure(class_2561 message) {
      ((FabricClientCommandSource)this).sendError(message);
   }
}
