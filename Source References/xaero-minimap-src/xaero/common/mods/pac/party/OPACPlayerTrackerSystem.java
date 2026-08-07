package xaero.common.mods.pac.party;

import java.util.Iterator;
import xaero.common.mods.pac.SupportOpenPartiesAndClaims;
import xaero.hud.minimap.player.tracker.system.IRenderedPlayerTracker;
import xaero.hud.minimap.player.tracker.system.ITrackedPlayerReader;
import xaero.pac.common.parties.party.api.IPartyMemberDynamicInfoSyncableAPI;

public class OPACPlayerTrackerSystem implements IRenderedPlayerTracker<IPartyMemberDynamicInfoSyncableAPI> {
   private final SupportOpenPartiesAndClaims opac;
   private final OPACTrackedPlayerReader reader;

   public OPACPlayerTrackerSystem(SupportOpenPartiesAndClaims opac) {
      this.opac = opac;
      this.reader = new OPACTrackedPlayerReader();
   }

   public ITrackedPlayerReader<IPartyMemberDynamicInfoSyncableAPI> getReader() {
      return this.reader;
   }

   public Iterator<IPartyMemberDynamicInfoSyncableAPI> getTrackedPlayerIterator() {
      return this.opac.getAllyIterator();
   }
}
