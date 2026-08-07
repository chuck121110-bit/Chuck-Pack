package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

class FS<K, V> extends Node<K, V> implements NodeFactory<K, V> {
   protected static final VarHandle KEY;
   protected static final VarHandle VALUE;
   volatile References.WeakKeyReference<K> key;
   volatile V value;

   FS() {
   }

   FS(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      this(new References.WeakKeyReference(var1, var2), var3, var4, var5, var6);
   }

   FS(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      KEY.set(this, var1);
      VALUE.set(this, var2);
   }

   public final K getKey() {
      return (K)KEY.get(this).get();
   }

   public final Object getKeyReference() {
      return KEY.getOpaque(this);
   }

   public final @Nullable Object getKeyReferenceOrNull() {
      Reference var1 = KEY.getOpaque(this);
      return var1.get() == null ? null : var1;
   }

   public final V getValue() {
      return (V)VALUE.getAcquire(this);
   }

   public final void setValue(V var1, ReferenceQueue<V> var2) {
      VALUE.setRelease(this, var1);
   }

   public final Object getValueReference() {
      return VALUE.getAcquire(this);
   }

   public final boolean containsValue(Object var1) {
      return Objects.equals(var1, this.getValue());
   }

   public Node<K, V> newNode(K var1, ReferenceQueue<K> var2, V var3, ReferenceQueue<V> var4, int var5, long var6) {
      return new FS<K, V>(var1, var2, var3, var4, var5, var6);
   }

   public Node<K, V> newNode(Object var1, V var2, ReferenceQueue<V> var3, int var4, long var5) {
      return new FS<K, V>(var1, var2, var3, var4, var5);
   }

   public Object newLookupKey(Object var1) {
      return new References.LookupKeyReference(var1);
   }

   public Object newReferenceKey(K var1, ReferenceQueue<K> var2) {
      return new References.WeakKeyReference(var1, var2);
   }

   public final boolean isAlive() {
      Object var1 = this.getKeyReference();
      return var1 != RETIRED_WEAK_KEY && var1 != DEAD_WEAK_KEY;
   }

   public final boolean isRetired() {
      return this.getKeyReference() == RETIRED_WEAK_KEY;
   }

   public final void retire() {
      this.key.clear();
      KEY.set(this, RETIRED_WEAK_KEY);
   }

   public final boolean isDead() {
      return this.getKeyReference() == DEAD_WEAK_KEY;
   }

   public final void die() {
      this.key.clear();
      VALUE.set(this, (Void)null);
      KEY.set(this, DEAD_WEAK_KEY);
   }

   static {
      MethodHandles.Lookup var0 = MethodHandles.lookup();

      try {
         KEY = var0.findVarHandle(FS.class, "key", References.WeakKeyReference.class);
         VALUE = var0.findVarHandle(FS.class, "value", Object.class);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
