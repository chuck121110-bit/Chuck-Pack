package xaeroplus.commands;

import net.minecraft.class_2172;
import net.minecraft.class_2561;

public interface XPClientCommandSource extends class_2172 {
   void xaeroplus$sendSuccess(class_2561 message);

   void xaeroplus$sendFailure(class_2561 message);
}
