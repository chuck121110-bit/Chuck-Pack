package com.github.benmanes.caffeine.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

final class References {
   private References() {
   }

   interface InternalReference<E> {
      @Nullable E get();

      Object getKeyReference();

      default boolean referenceEquals(@Nullable Object object) {
         if (object == this) {
            return true;
         } else if (object instanceof InternalReference) {
            InternalReference<?> referent = (InternalReference)object;
            return this.get() == referent.get();
         } else {
            return false;
         }
      }

      default boolean objectEquals(@Nullable Object object) {
         if (object == this) {
            return true;
         } else if (object instanceof InternalReference) {
            InternalReference<?> referent = (InternalReference)object;
            return Objects.equals(this.get(), referent.get());
         } else {
            return false;
         }
      }
   }

   static final class LookupKeyReference<K> implements InternalReference<K> {
      private final int hashCode;
      private final K key;

      public LookupKeyReference(K key) {
         this.hashCode = System.identityHashCode(key);
         this.key = (K)Objects.requireNonNull(key);
      }

      public K get() {
         return this.key;
      }

      public Object getKeyReference() {
         return this;
      }

      public boolean equals(@Nullable Object object) {
         return this.referenceEquals(object);
      }

      public int hashCode() {
         return this.hashCode;
      }

      public String toString() {
         return String.format(Locale.US, "%s{key=%s, hashCode=%d}", this.getClass().getSimpleName(), this.get(), this.hashCode);
      }
   }

   static final class LookupKeyEqualsReference<K> implements InternalReference<K> {
      private final int hashCode;
      private final K key;

      public LookupKeyEqualsReference(K key) {
         this.hashCode = key.hashCode();
         this.key = (K)Objects.requireNonNull(key);
      }

      public K get() {
         return this.key;
      }

      public Object getKeyReference() {
         return this;
      }

      public boolean equals(@Nullable Object object) {
         return this.objectEquals(object);
      }

      public int hashCode() {
         return this.hashCode;
      }

      public String toString() {
         return String.format(Locale.US, "%s{key=%s, hashCode=%d}", this.getClass().getSimpleName(), this.get(), this.hashCode);
      }
   }

   static class WeakKeyReference<K> extends WeakReference<@Nullable K> implements InternalReference<K> {
      private final int hashCode;

      public WeakKeyReference(@Nullable K key, @Nullable ReferenceQueue<K> queue) {
         super(key, queue);
         this.hashCode = System.identityHashCode(key);
      }

      public final Object getKeyReference() {
         return this;
      }

      public final boolean equals(@Nullable Object object) {
         return this.referenceEquals(object);
      }

      public final int hashCode() {
         return this.hashCode;
      }

      public final String toString() {
         return String.format(Locale.US, "%s{key=%s, hashCode=%d}", this.getClass().getSimpleName(), this.get(), this.hashCode);
      }
   }

   static final class WeakKeyEqualsReference<K> extends WeakReference<K> implements InternalReference<K> {
      private final int hashCode;

      public WeakKeyEqualsReference(K key, @Nullable ReferenceQueue<K> queue) {
         super(key, queue);
         this.hashCode = key.hashCode();
      }

      public Object getKeyReference() {
         return this;
      }

      public boolean equals(@Nullable Object object) {
         return this.objectEquals(object);
      }

      public int hashCode() {
         return this.hashCode;
      }

      public String toString() {
         return String.format(Locale.US, "%s{key=%s, hashCode=%d}", this.getClass().getSimpleName(), this.get(), this.hashCode);
      }
   }

   static final class WeakValueReference<V> extends WeakReference<@Nullable V> implements InternalReference<V> {
      private Object keyReference;

      public WeakValueReference(Object keyReference, @Nullable V value, @Nullable ReferenceQueue<V> queue) {
         super(value, queue);
         this.keyReference = keyReference;
      }

      public Object getKeyReference() {
         return this.keyReference;
      }

      public void setKeyReference(Object keyReference) {
         this.keyReference = keyReference;
      }

      public boolean equals(@Nullable Object object) {
         return this.referenceEquals(object);
      }

      public int hashCode() {
         V value = (V)this.get();
         return value == null ? 0 : value.hashCode();
      }

      public String toString() {
         return String.format(Locale.US, "%s{value=%s}", this.getClass().getSimpleName(), this.get());
      }
   }

   static final class SoftValueReference<V> extends SoftReference<@Nullable V> implements InternalReference<V> {
      private Object keyReference;

      public SoftValueReference(Object keyReference, @Nullable V value, @Nullable ReferenceQueue<V> queue) {
         super(value, queue);
         this.keyReference = keyReference;
      }

      public Object getKeyReference() {
         return this.keyReference;
      }

      public void setKeyReference(Object keyReference) {
         this.keyReference = keyReference;
      }

      public boolean equals(@Nullable Object object) {
         return this.referenceEquals(object);
      }

      public int hashCode() {
         V value = (V)this.get();
         return value == null ? 0 : value.hashCode();
      }

      public String toString() {
         return String.format(Locale.US, "%s{value=%s}", this.getClass().getSimpleName(), this.get());
      }
   }
}
