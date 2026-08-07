package xaero.common.gui;

import java.util.Objects;
import net.minecraft.class_2561;
import net.minecraft.class_410;
import net.minecraft.class_437;

public class GuiReset extends class_410 {
   public GuiReset(IXaeroConfirmScreenCallback callback, class_437 parent, class_437 escScreen) {
      Objects.requireNonNull(callback);
      super(callback::accept, class_2561.method_43471("gui.xaero_reset_config_profile_default_message"), class_2561.method_43471("gui.xaero_reset_config_profile_default_message2"));
   }
}
