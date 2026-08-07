package xaero.common.minimap.mcworld;

import java.util.function.LongSupplier;
import net.minecraft.class_12205;
import net.minecraft.class_2874;
import net.minecraft.class_638;
import net.minecraft.class_7134;
import xaero.hud.packet.basic.ClientboundRulesPacket;

public class MinimapClientWorldData {
   private int serverModNetworkVersion;
   public Integer serverLevelId;
   public float shadowR = 1.0F;
   public float shadowG = 1.0F;
   public float shadowB = 1.0F;
   private ClientboundRulesPacket syncedRules;
   private class_12205 attributeSystem;

   public MinimapClientWorldData(class_638 world) {
      if (world.method_27983() != class_638.field_25179 && !world.method_40134().method_40226(class_7134.field_37666.method_29177())) {
         if (world.method_27983() == class_638.field_25180 || world.method_40134().method_40226(class_7134.field_37667.method_29177())) {
            this.shadowR = 1.0F;
            this.shadowG = 0.0F;
            this.shadowB = 0.0F;
         }
      } else {
         this.shadowR = 0.518F;
         this.shadowG = 0.678F;
         this.shadowB = 1.0F;
      }

      class_12205.class_12314 builder = class_12205.method_76394();
      LongSupplier dayTimeSupplier = () -> world.method_8532();
      class_2874 dimType = world.method_8597();
      builder.method_76417(dimType.comp_5146());
      dimType.comp_5222().forEach((holder) -> builder.method_76420(holder, dayTimeSupplier));
      this.attributeSystem = builder.method_76410();
   }

   public void setServerModNetworkVersion(int serverModNetworkVersion) {
      this.serverModNetworkVersion = serverModNetworkVersion;
   }

   public int getServerModNetworkVersion() {
      return this.serverModNetworkVersion;
   }

   public void setSyncedRules(ClientboundRulesPacket syncedRules) {
      this.syncedRules = syncedRules;
   }

   public ClientboundRulesPacket getSyncedRules() {
      if (this.syncedRules == null) {
         this.syncedRules = new ClientboundRulesPacket(true, true, true);
      }

      return this.syncedRules;
   }

   public class_12205 getAttributeSystem() {
      return this.attributeSystem;
   }
}
