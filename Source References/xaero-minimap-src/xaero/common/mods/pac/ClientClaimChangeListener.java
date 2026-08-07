package xaero.common.mods.pac;

import net.minecraft.class_2960;
import xaero.common.XaeroMinimapSession;
import xaero.common.minimap.highlight.DimensionHighlighterHandler;
import xaero.common.minimap.write.MinimapWriter;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.claims.tracker.api.IClaimsManagerListenerAPI;

public class ClientClaimChangeListener implements IClaimsManagerListenerAPI {
   public void onWholeRegionChange(class_2960 dimension, int regionX, int regionZ) {
      XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
      MinimapWriter write = minimapSession.getMinimapProcessor().getMinimapWriter();
      DimensionHighlighterHandler dimHighlightHandler = write.getDimensionHighlightHandler();
      if (dimHighlightHandler != null) {
         for(int i = -1; i < 2; ++i) {
            for(int j = -1; j < 2; ++j) {
               if (i == 0 && j == 0 || i * i != j * j) {
                  dimHighlightHandler.requestRefresh(regionX + i, regionZ + j);
               }
            }
         }
      }

   }

   public void onChunkChange(class_2960 dimension, int chunkX, int chunkZ, IPlayerChunkClaimAPI claim) {
      XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
      MinimapWriter write = minimapSession.getMinimapProcessor().getMinimapWriter();
      DimensionHighlighterHandler dimHighlightHandler = write.getDimensionHighlightHandler();
      if (dimHighlightHandler != null) {
         for(int i = -1; i < 2; ++i) {
            for(int j = -1; j < 2; ++j) {
               if (i == 0 && j == 0 || i * i != j * j) {
                  dimHighlightHandler.requestRefresh(chunkX + i >> 5, chunkZ + j >> 5);
               }
            }
         }
      }

   }

   public void onDimensionChange(class_2960 dimension) {
      XaeroMinimapSession minimapSession = XaeroMinimapSession.getCurrentSession();
      MinimapWriter write = minimapSession.getMinimapProcessor().getMinimapWriter();
      DimensionHighlighterHandler dimHighlightHandler = write.getDimensionHighlightHandler();
      if (dimHighlightHandler != null) {
         dimHighlightHandler.requestRefresh();
      }

   }
}
