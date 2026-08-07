package xaero.hud.minimap.element.render;

public abstract class MinimapElementRenderProvider<E, RC> {
   public abstract void begin(MinimapElementRenderLocation var1, RC var2);

   public abstract boolean hasNext(MinimapElementRenderLocation var1, RC var2);

   public abstract E getNext(MinimapElementRenderLocation var1, RC var2);

   public E setupContextAndGetNext(MinimapElementRenderLocation location, RC context) {
      return (E)this.getNext(location, context);
   }

   public abstract void end(MinimapElementRenderLocation var1, RC var2);
}
