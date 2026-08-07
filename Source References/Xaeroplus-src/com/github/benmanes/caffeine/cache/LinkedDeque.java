package com.github.benmanes.caffeine.cache;

import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

interface LinkedDeque<E> extends Deque<E> {
   boolean isFirst(@Nullable E e);

   boolean isLast(@Nullable E e);

   void moveToFront(E e);

   void moveToBack(E e);

   @Nullable E getPrevious(E e);

   void setPrevious(E e, @Nullable E prev);

   @Nullable E getNext(E e);

   void setNext(E e, @Nullable E next);

   PeekingIterator<E> iterator();

   PeekingIterator<E> descendingIterator();

   public interface PeekingIterator<E> extends Iterator<E> {
      @Nullable E peek();

      static <E> PeekingIterator<E> concat(PeekingIterator<E> first, PeekingIterator<E> second) {
         return new PeekingIterator<E>() {
            // $FF: synthetic field
            final LinkedDeque.PeekingIterator val$first;
            // $FF: synthetic field
            final LinkedDeque.PeekingIterator val$second;

            {
               this.val$first = val$first;
               this.val$second = val$second;
            }

            public boolean hasNext() {
               return this.val$first.hasNext() || this.val$second.hasNext();
            }

            public E next() {
               if (this.val$first.hasNext()) {
                  return (E)this.val$first.next();
               } else if (this.val$second.hasNext()) {
                  return (E)this.val$second.next();
               } else {
                  throw new NoSuchElementException();
               }
            }

            public @Nullable E peek() {
               return (E)(this.val$first.hasNext() ? this.val$first.peek() : this.val$second.peek());
            }
         };
      }

      static <E> PeekingIterator<E> comparing(PeekingIterator<E> first, PeekingIterator<E> second, Comparator<E> comparator) {
         return new PeekingIterator<E>() {
            // $FF: synthetic field
            final LinkedDeque.PeekingIterator val$first;
            // $FF: synthetic field
            final LinkedDeque.PeekingIterator val$second;
            // $FF: synthetic field
            final Comparator val$comparator;

            {
               this.val$first = val$first;
               this.val$second = val$second;
               this.val$comparator = val$comparator;
            }

            public boolean hasNext() {
               return this.val$first.hasNext() || this.val$second.hasNext();
            }

            public E next() {
               if (!this.val$first.hasNext()) {
                  return (E)this.val$second.next();
               } else if (!this.val$second.hasNext()) {
                  return (E)this.val$first.next();
               } else {
                  E o1 = (E)Objects.requireNonNull(this.val$first.peek());
                  E o2 = (E)Objects.requireNonNull(this.val$second.peek());
                  boolean greaterOrEqual = this.val$comparator.compare(o1, o2) >= 0;
                  return (E)(greaterOrEqual ? this.val$first.next() : this.val$second.next());
               }
            }

            public @Nullable E peek() {
               if (!this.val$first.hasNext()) {
                  return (E)this.val$second.peek();
               } else if (!this.val$second.hasNext()) {
                  return (E)this.val$first.peek();
               } else {
                  E o1 = (E)Objects.requireNonNull(this.val$first.peek());
                  E o2 = (E)Objects.requireNonNull(this.val$second.peek());
                  boolean greaterOrEqual = this.val$comparator.compare(o1, o2) >= 0;
                  return (E)(greaterOrEqual ? o1 : o2);
               }
            }
         };
      }
   }
}
