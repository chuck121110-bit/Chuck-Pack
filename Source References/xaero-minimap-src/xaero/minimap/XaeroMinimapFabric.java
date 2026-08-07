package xaero.minimap;

import java.io.IOException;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import xaero.common.PlatformContext;
import xaero.common.PlatformContextFabric;

public class XaeroMinimapFabric extends XaeroMinimap implements ClientModInitializer, DedicatedServerModInitializer {
   public static String fileLayoutID = "minimap_fabric";
   private final PlatformContextFabric xaeroHudFabric;

   public XaeroMinimapFabric() {
      this.xaeroHudFabric = (PlatformContextFabric)this.platformContext;
   }

   public void onInitializeClient() {
      try {
         this.loadCommon();
         this.loadClient();
      } catch (Throwable e) {
         this.xaeroHudFabric.firstStageError = e;
      }

   }

   public void onInitializeServer() {
      try {
         this.loadCommon();
         this.loadServer();
      } catch (Throwable e) {
         this.xaeroHudFabric.firstStageError = e;
      }

   }

   protected void loadClient() throws IOException {
      super.loadClient();
      this.xaeroHudFabric.postLoadClient();
   }

   protected void loadCommon() {
      super.loadCommon();
      this.xaeroHudFabric.postLoadCommon();
   }

   public void loadServer() {
      super.loadServer();
      this.xaeroHudFabric.postLoadServer();
   }

   public void tryLoadLater() {
      if (!this.xaeroHudFabric.preTryLoadLater()) {
         this.loadLater();
      }
   }

   public void tryLoadLaterServer() {
      if (!this.xaeroHudFabric.preTryLoadLaterServer()) {
         this.loadLaterServer();
      }
   }

   protected PlatformContext createPlatformContext() {
      return new PlatformContextFabric(this);
   }

   public String getFileLayoutID() {
      return fileLayoutID;
   }
}
