package xaero.common.mixin;

import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import xaero.common.server.player.IServerPlayer;
import xaero.common.server.player.ServerPlayerData;

@Mixin({class_3222.class})
public class MixinServerPlayer implements IServerPlayer {
   private ServerPlayerData xaeroMinimapPlayerData;

   public ServerPlayerData getXaeroMinimapPlayerData() {
      return this.xaeroMinimapPlayerData;
   }

   public void setXaeroMinimapPlayerData(ServerPlayerData data) {
      this.xaeroMinimapPlayerData = data;
   }
}
