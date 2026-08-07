package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import org.jspecify.annotations.Nullable;

class FD<K, V> extends Node<K, V> implements NodeFactory<K, V> {
   protected static final VarHandle VALUE;
   volatile References.SoftValueReference<V> value;

   FD() {
   }

   FD(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      this(new References.WeakKeyReference(var1, var2), var3, var4, var5, var6);
   }

   FD(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      VALUE.set(this, new References.SoftValueReference(var1, var2, var3));
   }

   public final Object getKeyReference() {
      References.SoftValueReference var1 = VALUE.getAcquire(this);
      return var1.getKeyReference();
   }

   public final @Nullable Object getKeyReferenceOrNull() {
      References.SoftValueReference var1 = VALUE.getAcquire(this);
      Reference var2 = (Reference)var1.getKeyReference();
      return var2.get() == null ? null : var2;
   }

   public final K getKey() {
      References.SoftValueReference var1 = VALUE.getAcquire(this);
      Reference var2 = (Reference)var1.getKeyReference();
      return (K)var2.get();
   }

   public final V getValue() {
      Reference var1 = VALUE.getAcquire(this);

      while(true) {
         Object var2 = var1.get();
         if (var2 != null) {
            return (V)var2;
         }

         VarHandle.loadLoadFence();
         Reference var3 = VALUE.getAcquire(this);
         if (var1 == var3) {
            return null;
         }

         var1 = var3;
      }
   }

   public final void setValue(V var1, ReferenceQueue<V> var2) {
      Reference var3 = VALUE.getAcquire(this);
      VALUE.setRelease(this, new References.SoftValueReference(this.getKeyReference(), var1, var2));
      VarHandle.storeStoreFence();
      var3.clear();
   }

   public final Object getValueReference() {
      return VALUE.getAcquire(this);
   }

   public final boolean containsValue(Object var1) {
      return this.getValue() == var1;
   }

   public Node<K, V> newNode(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      return new FD<K, V>(var1, var2, var3, var4, var5, var6);
   }

   public Node<K, V> newNode(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      return new FD<K, V>(var1, var2, var3, var4, var5);
   }

   public Object newLookupKey(Object var1) {
      return new References.LookupKeyReference(var1);
   }

   public Object newReferenceKey(K var1, ReferenceQueue<K> var2) {
      return new References.WeakKeyReference(var1, var2);
   }

   public boolean softValues() {
      return true;
   }

   public final boolean isAlive() {
      Object var1 = this.getKeyReference();
      return var1 != RETIRED_WEAK_KEY && var1 != DEAD_WEAK_KEY;
   }

   public final boolean isRetired() {
      return this.getKeyReference() == RETIRED_WEAK_KEY;
   }

   public final void retire() {
      References.SoftValueReference var1 = VALUE.getOpaque(this);
      Reference var2 = (Reference)var1.getKeyReference();
      var2.clear();
      var1.setKeyReference(RETIRED_WEAK_KEY);
      var1.clear();
   }

   public final boolean isDead() {
      return this.getKeyReference() == DEAD_WEAK_KEY;
   }

   public final void die() {
      References.SoftValueReference var1 = VALUE.getOpaque(this);
      Reference var2 = (Reference)var1.getKeyReference();
      var2.clear();
      var1.setKeyReference(DEAD_WEAK_KEY);
      var1.clear();
   }

   static {
      MethodHandles.Lookup var0 = MethodHandles.lookup();

      try {
         VALUE = var0.findVarHandle(FD.class, "value", References.SoftValueReference.class);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
