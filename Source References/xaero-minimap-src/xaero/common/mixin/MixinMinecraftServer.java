package xaero.common.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import xaero.common.server.IMinecraftServer;
import xaero.common.server.MinecraftServerData;

@Mixin({MinecraftServer.class})
public class MixinMinecraftServer implements IMinecraftServer {
   private MinecraftServerData xaeroMinimapServerData;

   public MinecraftServerData getXaeroMinimapServerData() {
      return this.xaeroMinimapServerData;
   }

   public void setXaeroMinimapServerData(MinecraftServerData data) {
      this.xaeroMinimapServerData = data;
   }
}
