package xaeroplus.event;

public abstract class PhasedEvent {
   private Phase phase;

   public PhasedEvent() {
      this.phase = Phase.PRE;
   }

   public Phase phase() {
      return this.phase;
   }

   public void setPhase(Phase phase) {
      this.phase = phase;
   }
}
