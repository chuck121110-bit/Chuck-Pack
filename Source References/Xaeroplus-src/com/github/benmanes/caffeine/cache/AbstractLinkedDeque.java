package com.github.benmanes.caffeine.cache;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

abstract class AbstractLinkedDeque<E> extends AbstractCollection<E> implements LinkedDeque<E> {
   @Nullable E first;
   @Nullable E last;
   int modCount;

   void linkFirst(E e) {
      E f = this.first;
      this.first = e;
      if (f == null) {
         this.last = e;
      } else {
         this.setPrevious(f, e);
         this.setNext(e, f);
      }

      ++this.modCount;
   }

   void linkLast(E e) {
      E l = this.last;
      this.last = e;
      if (l == null) {
         this.first = e;
      } else {
         this.setNext(l, e);
         this.setPrevious(e, l);
      }

      ++this.modCount;
   }

   E unlinkFirst() {
      E f = (E)Objects.requireNonNull(this.first);
      E next = (E)this.getNext(f);
      this.setNext(f, (Object)null);
      this.first = next;
      if (next == null) {
         this.last = null;
      } else {
         this.setPrevious(next, (Object)null);
      }

      ++this.modCount;
      return f;
   }

   E unlinkLast() {
      E l = (E)Objects.requireNonNull(this.last);
      E prev = (E)this.getPrevious(l);
      this.setPrevious(l, (Object)null);
      this.last = prev;
      if (prev == null) {
         this.first = null;
      } else {
         this.setNext(prev, (Object)null);
      }

      ++this.modCount;
      return l;
   }

   void unlink(E e) {
      E prev = (E)this.getPrevious(e);
      E next = (E)this.getNext(e);
      if (prev == null) {
         this.first = next;
      } else {
         this.setNext(prev, next);
         this.setPrevious(e, (Object)null);
      }

      if (next == null) {
         this.last = prev;
      } else {
         this.setPrevious(next, prev);
         this.setNext(e, (Object)null);
      }

      ++this.modCount;
   }

   public boolean isEmpty() {
      return this.first == null;
   }

   void checkNotEmpty() {
      if (this.isEmpty()) {
         throw new NoSuchElementException();
      }
   }

   public int size() {
      int size = 0;

      for(E e = this.first; e != null; e = (E)this.getNext(e)) {
         ++size;
      }

      return size;
   }

   public void clear() {
      E next;
      for(E e = this.first; e != null; e = next) {
         next = (E)this.getNext(e);
         this.setPrevious(e, (Object)null);
         this.setNext(e, (Object)null);
      }

      this.first = this.last = null;
      ++this.modCount;
   }

   public abstract boolean contains(Object o);

   public boolean isFirst(@Nullable E e) {
      return e != null && e == this.first;
   }

   public boolean isLast(@Nullable E e) {
      return e != null && e == this.last;
   }

   public void moveToFront(E e) {
      if (e != this.first) {
         this.unlink(e);
         this.linkFirst(e);
      }

   }

   public void moveToBack(E e) {
      if (e != this.last) {
         this.unlink(e);
         this.linkLast(e);
      }

   }

   public @Nullable E peek() {
      return (E)this.peekFirst();
   }

   public @Nullable E peekFirst() {
      return this.first;
   }

   public @Nullable E peekLast() {
      return this.last;
   }

   public E getFirst() {
      this.checkNotEmpty();
      return (E)Objects.requireNonNull(this.peekFirst());
   }

   public E getLast() {
      this.checkNotEmpty();
      return (E)Objects.requireNonNull(this.peekLast());
   }

   public E element() {
      return (E)this.getFirst();
   }

   public boolean offer(E e) {
      return this.offerLast(e);
   }

   public boolean offerFirst(E e) {
      Objects.requireNonNull(e);
      if (this.contains(e)) {
         return false;
      } else {
         this.linkFirst(e);
         return true;
      }
   }

   public boolean offerLast(E e) {
      Objects.requireNonNull(e);
      if (this.contains(e)) {
         return false;
      } else {
         this.linkLast(e);
         return true;
      }
   }

   public boolean add(E e) {
      return this.offerLast(e);
   }

   public void addFirst(E e) {
      if (!this.offerFirst(e)) {
         throw new IllegalArgumentException();
      }
   }

   public void addLast(E e) {
      if (!this.offerLast(e)) {
         throw new IllegalArgumentException();
      }
   }

   public @Nullable E poll() {
      return (E)this.pollFirst();
   }

   public @Nullable E pollFirst() {
      return (E)(this.isEmpty() ? null : this.unlinkFirst());
   }

   public @Nullable E pollLast() {
      return (E)(this.isEmpty() ? null : this.unlinkLast());
   }

   public E remove() {
      return (E)this.removeFirst();
   }

   public E removeFirst() {
      this.checkNotEmpty();
      return (E)Objects.requireNonNull(this.pollFirst());
   }

   public abstract boolean remove(Object o);

   public boolean removeFirstOccurrence(Object o) {
      return this.remove(o);
   }

   public E removeLast() {
      this.checkNotEmpty();
      return (E)Objects.requireNonNull(this.pollLast());
   }

   public boolean removeLastOccurrence(Object o) {
      return this.remove(o);
   }

   public boolean removeAll(Collection<?> c) {
      boolean modified = false;

      for(Object o : c) {
         modified |= this.remove(o);
      }

      return modified;
   }

   public void push(E e) {
      this.addFirst(e);
   }

   public E pop() {
      return (E)this.removeFirst();
   }

   public LinkedDeque.PeekingIterator<E> iterator() {
      return new AbstractLinkedDeque<E>.AbstractLinkedIterator(this.first) {
         @Nullable E computeNext() {
            return (E)AbstractLinkedDeque.this.getNext(Objects.requireNonNull(this.cursor));
         }
      };
   }

   public LinkedDeque.PeekingIterator<E> descendingIterator() {
      return new AbstractLinkedDeque<E>.AbstractLinkedIterator(this.last) {
         @Nullable E computeNext() {
            return (E)AbstractLinkedDeque.this.getPrevious(Objects.requireNonNull(this.cursor));
         }
      };
   }

   abstract class AbstractLinkedIterator implements LinkedDeque.PeekingIterator<E> {
      @Nullable E previous;
      @Nullable E cursor;
      int expectedModCount;

      AbstractLinkedIterator(E start) {
         this.expectedModCount = AbstractLinkedDeque.this.modCount;
         this.cursor = start;
      }

      public boolean hasNext() {
         this.checkForConcurrentModification();
         return this.cursor != null;
      }

      public @Nullable E peek() {
         return this.cursor;
      }

      public E next() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         } else {
            this.previous = this.cursor;
            this.cursor = (E)this.computeNext();
            return (E)Objects.requireNonNull(this.previous);
         }
      }

      abstract @Nullable E computeNext();

      public void remove() {
         if (this.previous == null) {
            throw new IllegalStateException();
         } else {
            this.checkForConcurrentModification();
            AbstractLinkedDeque.this.removeFirstOccurrence(this.previous);
            this.expectedModCount = AbstractLinkedDeque.this.modCount;
            this.previous = (E)null;
         }
      }

      void checkForConcurrentModification() {
         if (AbstractLinkedDeque.this.modCount != this.expectedModCount) {
            throw new ConcurrentModificationException();
         }
      }
   }
}
