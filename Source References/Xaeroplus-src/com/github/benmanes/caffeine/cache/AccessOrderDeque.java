package com.github.benmanes.caffeine.cache;

import org.jspecify.annotations.Nullable;

final class AccessOrderDeque<E extends AccessOrderDeque.AccessOrder<E>> extends AbstractLinkedDeque<E> {
   public boolean contains(Object o) {
      return o instanceof AccessOrder && this.contains((AccessOrder)o);
   }

   boolean contains(AccessOrder<?> e) {
      return e.getPreviousInAccessOrder() != null || e.getNextInAccessOrder() != null || e == this.first;
   }

   public boolean remove(Object o) {
      return o instanceof AccessOrder && this.remove((AccessOrder)o);
   }

   boolean remove(E e) {
      if (this.contains(e)) {
         this.unlink(e);
         return true;
      } else {
         return false;
      }
   }

   public @Nullable E getPrevious(E e) {
      return (E)e.getPreviousInAccessOrder();
   }

   public void setPrevious(E e, @Nullable E prev) {
      e.setPreviousInAccessOrder(prev);
   }

   public @Nullable E getNext(E e) {
      return (E)e.getNextInAccessOrder();
   }

   public void setNext(E e, @Nullable E next) {
      e.setNextInAccessOrder(next);
   }

   interface AccessOrder<T extends AccessOrder<T>> {
      @Nullable T getPreviousInAccessOrder();

      void setPreviousInAccessOrder(@Nullable T prev);

      @Nullable T getNextInAccessOrder();

      void setNextInAccessOrder(@Nullable T next);
   }
}
