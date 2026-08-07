package com.github.benmanes.caffeine.cache;

import com.google.errorprone.annotations.Var;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

abstract class StripedBuffer<E> implements Buffer<E> {
   static final VarHandle TABLE_BUSY;
   static final int NCPU;
   static final int MAXIMUM_TABLE_SIZE;
   static final int ATTEMPTS = 3;
   volatile Buffer<E> @Nullable [] table;
   volatile int tableBusy;

   final boolean casTableBusy() {
      return TABLE_BUSY.compareAndSet(this, 0, 1);
   }

   protected abstract Buffer<E> create(E e);

   public int offer(E e) {
      long z = mix64(Thread.currentThread().getId());
      int increment = (int)(z >>> 32) | 1;
      int h = (int)z;
      boolean uncontended = true;
      Buffer<E>[] buffers = this.table;
      int mask;
      int result;
      Buffer<E> buffer;
      return buffers != null && (mask = buffers.length - 1) >= 0 && (buffer = buffers[h & mask]) != null && (uncontended = (result = buffer.offer(e)) != -1) ? result : this.expandOrRetry(e, h, increment, uncontended);
   }

   final int expandOrRetry(E e, @Var int h, int increment, @Var boolean wasUncontended) {
      int result = -1;
      boolean collide = false;

      for(int attempt = 0; attempt < 3; ++attempt) {
         Buffer<E>[] buffers;
         int n;
         if ((buffers = this.table) != null && (n = buffers.length) > 0) {
            Buffer<E> buffer;
            if ((buffer = buffers[n - 1 & h]) == null) {
               if (this.tableBusy == 0 && this.casTableBusy()) {
                  boolean created = false;

                  try {
                     int mask;
                     int j;
                     Buffer<E>[] rs;
                     if ((rs = this.table) != null && (mask = rs.length) > 0 && rs[j = mask - 1 & h] == null) {
                        rs[j] = this.create(e);
                        created = true;
                     }
                  } finally {
                     this.tableBusy = 0;
                  }

                  if (created) {
                     result = 0;
                     break;
                  }
                  continue;
               }

               collide = false;
            } else if (!wasUncontended) {
               wasUncontended = true;
            } else {
               if ((result = buffer.offer(e)) != -1) {
                  break;
               }

               if (n < MAXIMUM_TABLE_SIZE && this.table == buffers) {
                  if (!collide) {
                     collide = true;
                  } else if (this.tableBusy == 0 && this.casTableBusy()) {
                     try {
                        if (this.table == buffers) {
                           this.table = (Buffer[])Arrays.copyOf(buffers, n << 1);
                        }
                     } finally {
                        this.tableBusy = 0;
                     }

                     collide = false;
                     continue;
                  }
               } else {
                  collide = false;
               }
            }

            h += increment;
         } else if (this.tableBusy == 0 && this.table == buffers && this.casTableBusy()) {
            boolean init = false;

            try {
               if (this.table == buffers) {
                  Buffer<E>[] rs = new Buffer[]{this.create(e)};
                  this.table = rs;
                  init = true;
               }
            } finally {
               this.tableBusy = 0;
            }

            if (init) {
               result = 0;
               break;
            }
         }
      }

      return result;
   }

   public void drainTo(Consumer<E> consumer) {
      Buffer<E>[] buffers = this.table;
      if (buffers != null) {
         for(Buffer<E> buffer : buffers) {
            if (buffer != null) {
               buffer.drainTo(consumer);
            }
         }

      }
   }

   public long reads() {
      Buffer<E>[] buffers = this.table;
      if (buffers == null) {
         return 0L;
      } else {
         long reads = 0L;

         for(Buffer<E> buffer : buffers) {
            if (buffer != null) {
               reads += buffer.reads();
            }
         }

         return reads;
      }
   }

   public long writes() {
      Buffer<E>[] buffers = this.table;
      if (buffers == null) {
         return 0L;
      } else {
         long writes = 0L;

         for(Buffer<E> buffer : buffers) {
            if (buffer != null) {
               writes += buffer.writes();
            }
         }

         return writes;
      }
   }

   static long mix64(@Var long z) {
      z = (z ^ z >>> 30) * -4658895280553007687L;
      z = (z ^ z >>> 27) * -7723592293110705685L;
      return z ^ z >>> 31;
   }

   static VarHandle findVarHandle(Class<?> recv, String name, Class<?> type) {
      try {
         return MethodHandles.lookup().findVarHandle(recv, name, type);
      } catch (ReflectiveOperationException e) {
         throw new ExceptionInInitializerError(e);
      }
   }

   static {
      TABLE_BUSY = findVarHandle(StripedBuffer.class, "tableBusy", Integer.TYPE);
      NCPU = Runtime.getRuntime().availableProcessors();
      MAXIMUM_TABLE_SIZE = 4 * Caffeine.ceilingPowerOfTwo(NCPU);
   }
}
