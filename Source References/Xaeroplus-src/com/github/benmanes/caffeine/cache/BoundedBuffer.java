package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

final class BoundedBuffer<E> extends StripedBuffer<E> {
   static final int BUFFER_SIZE = 16;
   static final int MASK = 15;

   protected Buffer<E> create(E e) {
      return new RingBuffer<E>(e);
   }

   static final class RingBuffer<E> extends BBHeader.ReadAndWriteCounterRef implements Buffer<E> {
      static final VarHandle BUFFER = MethodHandles.arrayElementVarHandle(Object[].class);
      final @Nullable Object[] buffer = new Object[16];

      public RingBuffer(E e) {
         BUFFER.set(this.buffer, 0, e);
         WRITE.set(this, 1);
      }

      public int offer(E e) {
         long head = this.readCounter;
         long tail = this.writeCounterOpaque();
         long size = tail - head;
         if (size >= 16L) {
            return 1;
         } else if (this.casWriteCounter(tail, tail + 1L)) {
            int index = (int)(tail & 15L);
            BUFFER.setRelease(this.buffer, index, e);
            return 0;
         } else {
            return -1;
         }
      }

      public void drainTo(Consumer<E> consumer) {
         long head = this.readCounter;
         long tail = this.writeCounterOpaque();
         long size = tail - head;
         if (size != 0L) {
            do {
               int index = (int)(head & 15L);
               E e = (E)BUFFER.getAcquire(this.buffer, index);
               if (e == null) {
                  break;
               }

               BUFFER.setRelease(this.buffer, index, (Void)null);
               consumer.accept(e);
               ++head;
            } while(head != tail);

            this.setReadCounterOpaque(head);
         }
      }

      public long reads() {
         return this.readCounter;
      }

      public long writes() {
         return this.writeCounter;
      }
   }
}
