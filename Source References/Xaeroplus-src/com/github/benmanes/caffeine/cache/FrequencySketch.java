package com.github.benmanes.caffeine.cache;

import com.google.errorprone.annotations.Var;

final class FrequencySketch {
   static final long RESET_MASK = 8608480567731124087L;
   static final long ONE_MASK = 1229782938247303441L;
   int sampleSize;
   int blockMask;
   long[] table;
   int size;

   public FrequencySketch() {
   }

   public void ensureCapacity(long maximumSize) {
      Caffeine.requireArgument(maximumSize >= 0L);
      int maximum = (int)Math.min(maximumSize, 1073741823L);
      if (this.table == null || this.table.length < maximum) {
         this.sampleSize = maximumSize == 0L ? 10 : (int)Math.min(10L * (long)maximum, 2147483647L);
         this.table = new long[Math.max(Caffeine.ceilingPowerOfTwo(maximum), 8)];
         this.blockMask = (this.table.length >>> 3) - 1;
         this.size = 0;
      }
   }

   public boolean isNotInitialized() {
      return this.table == null;
   }

   public int frequency(Object e) {
      if (this.isNotInitialized()) {
         return 0;
      } else {
         int frequency = Integer.MAX_VALUE;
         int blockHash = spread(e.hashCode());
         int counterHash = rehash(blockHash);
         int block = (blockHash & this.blockMask) << 3;

         for(int i = 0; i < 4; ++i) {
            int h = counterHash >>> (i << 3);
            int index = h >>> 1 & 15;
            int offset = h & 1;
            int slot = block + offset + (i << 1);
            int count = (int)(this.table[slot] >>> (index << 2) & 15L);
            frequency = Math.min(frequency, count);
         }

         return frequency;
      }
   }

   public void increment(Object e) {
      if (!this.isNotInitialized()) {
         int blockHash = spread(e.hashCode());
         int counterHash = rehash(blockHash);
         int block = (blockHash & this.blockMask) << 3;
         int h1 = counterHash >>> 8;
         int h2 = counterHash >>> 16;
         int h3 = counterHash >>> 24;
         int index0 = counterHash >>> 1 & 15;
         int index1 = h1 >>> 1 & 15;
         int index2 = h2 >>> 1 & 15;
         int index3 = h3 >>> 1 & 15;
         int slot0 = block + (counterHash & 1);
         int slot1 = block + (h1 & 1) + 2;
         int slot2 = block + (h2 & 1) + 4;
         int slot3 = block + (h3 & 1) + 6;
         boolean added = this.incrementAt(slot0, index0) | this.incrementAt(slot1, index1) | this.incrementAt(slot2, index2) | this.incrementAt(slot3, index3);
         if (added && ++this.size == this.sampleSize) {
            this.reset();
         }

      }
   }

   static int spread(@Var int x) {
      x ^= x >>> 17;
      x *= -312814405;
      x ^= x >>> 11;
      x *= -1404298415;
      x ^= x >>> 15;
      return x;
   }

   static int rehash(@Var int x) {
      x *= 830770091;
      x ^= x >>> 14;
      return x;
   }

   boolean incrementAt(int i, int j) {
      int offset = j << 2;
      long mask = 15L << offset;
      if ((this.table[i] & mask) != mask) {
         long[] var10000 = this.table;
         var10000[i] += 1L << offset;
         return true;
      } else {
         return false;
      }
   }

   void reset() {
      long count = 0L;

      for(int i = 0; i < this.table.length; ++i) {
         count += (long)Long.bitCount(this.table[i] & 1229782938247303441L);
         this.table[i] = this.table[i] >>> 1 & 8608480567731124087L;
      }

      this.size = (int)((long)this.size - (count >>> 2) >>> 1);
   }
}
