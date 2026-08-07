package com.github.benmanes.caffeine.cache;

import org.jspecify.annotations.NullMarked;

@NullMarked
public enum RemovalCause {
   EXPLICIT {
      public boolean wasEvicted() {
         return false;
      }
   },
   REPLACED {
      public boolean wasEvicted() {
         return false;
      }
   },
   COLLECTED {
      public boolean wasEvicted() {
         return true;
      }
   },
   EXPIRED {
      public boolean wasEvicted() {
         return true;
      }
   },
   SIZE {
      public boolean wasEvicted() {
         return true;
      }
   };

   public abstract boolean wasEvicted();

   // $FF: synthetic method
   private static RemovalCause[] $values() {
      return new RemovalCause[]{EXPLICIT, REPLACED, COLLECTED, EXPIRED, SIZE};
   }
}
