package xaero.map.mods.pac.gui.claim.element;

import java.util.Iterator;
import xaero.map.element.render.ElementRenderLocation;
import xaero.map.element.render.ElementRenderProvider;
import xaero.map.mods.pac.gui.claim.ClaimResultElement;
import xaero.map.mods.pac.gui.claim.ClaimResultElementManager;

public class ClaimResultElementRenderProvider extends ElementRenderProvider<ClaimResultElement, ClaimResultElementRenderContext> {
   private final ClaimResultElementManager manager;
   private Iterator<ClaimResultElement> iterator;

   public ClaimResultElementRenderProvider(ClaimResultElementManager manager) {
      this.manager = manager;
   }

   public void begin(ElementRenderLocation location, ClaimResultElementRenderContext context) {
      this.iterator = this.manager.getIterator();
   }

   public boolean hasNext(ElementRenderLocation location, ClaimResultElementRenderContext context) {
      return this.iterator != null && this.iterator.hasNext();
   }

   public ClaimResultElement getNext(ElementRenderLocation location, ClaimResultElementRenderContext context) {
      return (ClaimResultElement)this.iterator.next();
   }

   public void end(ElementRenderLocation location, ClaimResultElementRenderContext context) {
      this.iterator = null;
   }
}
