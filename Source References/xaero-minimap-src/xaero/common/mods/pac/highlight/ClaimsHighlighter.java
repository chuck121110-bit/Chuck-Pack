package xaero.common.mods.pac.highlight;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.class_124;
import net.minecraft.class_1937;
import net.minecraft.class_2561;
import net.minecraft.class_5250;
import net.minecraft.class_5321;
import xaero.common.HudMod;
import xaero.common.IXaeroMinimap;
import xaero.common.minimap.highlight.ChunkHighlighter;
import xaero.hud.minimap.common.config.option.MinimapProfiledConfigOptions;
import xaero.hud.minimap.info.render.compile.InfoDisplayCompiler;
import xaero.lib.client.config.ClientConfigManager;
import xaero.lib.common.util.TextSplitter;
import xaero.pac.client.claims.api.IClientClaimsManagerAPI;
import xaero.pac.client.claims.api.IClientDimensionClaimsManagerAPI;
import xaero.pac.client.claims.player.api.IClientPlayerClaimInfoAPI;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;

public class ClaimsHighlighter extends ChunkHighlighter {
   private final IClientClaimsManagerAPI claimsManager;
   private final IXaeroMinimap modMain;
   private final ClientConfigManager configManager;
   private List<class_2561> cachedTooltip;
   private IPlayerChunkClaimAPI cachedTooltipFor;
   private int cachedForWidth;
   private String cachedForCustomName;
   private int cachedForClaimsColor;
   private boolean cachedPartyOwned;
   private class_2561 cachedForPartyName;

   public ClaimsHighlighter(IXaeroMinimap modMain, IClientClaimsManagerAPI claimsManager) {
      super(true);
      this.modMain = modMain;
      this.configManager = HudMod.INSTANCE.getHudConfigs().getClientConfigManager();
      this.claimsManager = claimsManager;
   }

   public boolean regionHasHighlights(class_5321<class_1937> dimension, int regionX, int regionZ) {
      IClientDimensionClaimsManagerAPI claimsDimension = this.claimsManager.getDimension(dimension.method_29177());
      if (claimsDimension == null) {
         return false;
      } else {
         return claimsDimension.getRegion(regionX, regionZ) != null;
      }
   }

   protected int[] getColors(class_5321<class_1937> dimension, int chunkX, int chunkZ) {
      if (!(Boolean)this.configManager.getEffective(MinimapProfiledConfigOptions.OPAC_CLAIMS)) {
         return null;
      } else {
         IPlayerChunkClaimAPI currentClaim = this.claimsManager.get(dimension.method_29177(), chunkX, chunkZ);
         if (currentClaim == null) {
            return null;
         } else {
            IPlayerChunkClaimAPI topClaim = this.claimsManager.get(dimension.method_29177(), chunkX, chunkZ - 1);
            IPlayerChunkClaimAPI rightClaim = this.claimsManager.get(dimension.method_29177(), chunkX + 1, chunkZ);
            IPlayerChunkClaimAPI bottomClaim = this.claimsManager.get(dimension.method_29177(), chunkX, chunkZ + 1);
            IPlayerChunkClaimAPI leftClaim = this.claimsManager.get(dimension.method_29177(), chunkX - 1, chunkZ);
            IClientPlayerClaimInfoAPI claimInfo = this.claimsManager.getPlayerInfo(currentClaim.getPlayerId());
            int claimColor = this.getClaimsColor(currentClaim, claimInfo);
            int claimColorFormatted = (claimColor & 255) << 24 | (claimColor >> 8 & 255) << 16 | (claimColor >> 16 & 255) << 8;
            int borderOpacity = (Integer)this.configManager.getEffective(MinimapProfiledConfigOptions.OPAC_CLAIMS_BORDER_OPACITY);
            int fillOpacity = (Integer)this.configManager.getEffective(MinimapProfiledConfigOptions.OPAC_CLAIMS_FILL_OPACITY);
            int centerColor = claimColorFormatted | 255 * fillOpacity / 100;
            int sideColor = claimColorFormatted | 255 * borderOpacity / 100;
            this.resultStore[0] = centerColor;
            this.resultStore[1] = topClaim != currentClaim ? sideColor : centerColor;
            this.resultStore[2] = rightClaim != currentClaim ? sideColor : centerColor;
            this.resultStore[3] = bottomClaim != currentClaim ? sideColor : centerColor;
            this.resultStore[4] = leftClaim != currentClaim ? sideColor : centerColor;
            return this.resultStore;
         }
      }
   }

   public boolean chunkIsHighlit(class_5321<class_1937> dimension, int chunkX, int chunkZ) {
      return this.claimsManager.get(dimension.method_29177(), chunkX, chunkZ) != null;
   }

   public void addChunkHighlightTooltips(InfoDisplayCompiler compiler, class_5321<class_1937> dimension, int chunkX, int chunkZ, int width) {
      if ((Boolean)this.configManager.getEffective(MinimapProfiledConfigOptions.OPAC_CURRENT_CLAIM)) {
         IPlayerChunkClaimAPI currentClaim = this.claimsManager.get(dimension.method_29177(), chunkX, chunkZ);
         if (currentClaim != null) {
            UUID currentClaimId = currentClaim.getPlayerId();
            IClientPlayerClaimInfoAPI claimInfo = this.claimsManager.getPlayerInfo(currentClaimId);
            String customName = this.getClaimsName(currentClaim, claimInfo);
            int actualClaimsColor = this.getClaimsColor(currentClaim, claimInfo);
            int claimsColor = actualClaimsColor | -16777216;
            boolean partyOwned = claimInfo.isPartyOwned();
            class_2561 partyName = claimInfo.getPartyName();
            if (!Objects.equals(currentClaim, this.cachedTooltipFor) || this.cachedForWidth != width || this.cachedForClaimsColor != claimsColor || !Objects.equals(customName, this.cachedForCustomName) || partyOwned != this.cachedPartyOwned || !Objects.equals(partyName, this.cachedForPartyName)) {
               class_5250 tooltip = class_2561.method_43470("□ ").method_27694((s) -> s.method_36139(claimsColor));
               tooltip.method_10855().add(this.claimsManager.getFullName(currentClaim).method_27661().method_27692(class_124.field_1068));
               this.cachedTooltip = new ArrayList();
               TextSplitter.splitTextIntoLines(this.cachedTooltip, width, width, tooltip, (StringBuilder)null);
               this.cachedTooltipFor = currentClaim;
               this.cachedForWidth = width;
               this.cachedForCustomName = customName;
               this.cachedForClaimsColor = claimsColor;
               this.cachedPartyOwned = partyOwned;
               this.cachedForPartyName = partyName;
            }

            for(int i = 0; i < this.cachedTooltip.size(); ++i) {
               compiler.addLine((class_2561)this.cachedTooltip.get(i));
            }

         }
      }
   }

   private String getClaimsName(IPlayerChunkClaimAPI currentClaim, IClientPlayerClaimInfoAPI claimInfo) {
      int subConfigIndex = currentClaim.getSubConfigIndex();
      String customName = claimInfo.getClaimsName(subConfigIndex);
      if (subConfigIndex != -1 && customName == null) {
         customName = claimInfo.getClaimsName();
      }

      return customName;
   }

   private int getClaimsColor(IPlayerChunkClaimAPI currentClaim, IClientPlayerClaimInfoAPI claimInfo) {
      int subConfigIndex = currentClaim.getSubConfigIndex();
      Integer actualClaimsColor = claimInfo.getClaimsColor(subConfigIndex);
      if (subConfigIndex != -1 && actualClaimsColor == null) {
         actualClaimsColor = claimInfo.getClaimsColor();
      }

      return actualClaimsColor;
   }
}
