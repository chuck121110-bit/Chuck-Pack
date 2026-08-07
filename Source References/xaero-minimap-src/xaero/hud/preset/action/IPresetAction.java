package xaero.hud.preset.action;

public interface IPresetAction<M> {
   void apply(M var1);

   void confirm(M var1);

   void cancel(M var1);
}
