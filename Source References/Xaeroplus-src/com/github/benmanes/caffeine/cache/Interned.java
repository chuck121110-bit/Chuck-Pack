package com.github.benmanes.caffeine.cache;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

final class Interned<K, V> extends Node<K, V> implements NodeFactory<K, V> {
   static final NodeFactory<Object, Object> FACTORY = new Interned<Object, Object>();
   volatile Reference<?> keyReference;

   Interned() {
      this.keyReference = NodeFactory.DEAD_WEAK_KEY;
   }

   Interned(Reference<K> keyReference) {
      this.keyReference = keyReference;
   }

   public @Nullable K getKey() {
      return (K)this.keyReference.get();
   }

   public Object getKeyReference() {
      return this.keyReference;
   }

   public Object getKeyReferenceOrNull() {
      return this.keyReference;
   }

   public V getValue() {
      return (V)Boolean.TRUE;
   }

   public V getValueReference() {
      return (V)Boolean.TRUE;
   }

   public void setValue(V value, @Nullable ReferenceQueue<V> referenceQueue) {
   }

   public boolean containsValue(Object value) {
      return Objects.equals(value, this.getValue());
   }

   public Node<K, V> newNode(K key, @Nullable ReferenceQueue<K> keyReferenceQueue, V value, @Nullable ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new Interned<K, V>(new References.WeakKeyEqualsReference(key, keyReferenceQueue));
   }

   public Node<K, V> newNode(Object keyReference, V value, @Nullable ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new Interned<K, V>((Reference)keyReference);
   }

   public Object newLookupKey(Object key) {
      return new References.LookupKeyEqualsReference(key);
   }

   public Object newReferenceKey(K key, ReferenceQueue<K> referenceQueue) {
      return new References.WeakKeyEqualsReference(key, referenceQueue);
   }

   public boolean isAlive() {
      Object keyRef = this.keyReference;
      return keyRef != RETIRED_WEAK_KEY && keyRef != DEAD_WEAK_KEY;
   }

   public boolean isRetired() {
      return this.keyReference == RETIRED_WEAK_KEY;
   }

   public void retire() {
      Reference<?> keyRef = this.keyReference;
      this.keyReference = RETIRED_WEAK_KEY;
      keyRef.clear();
   }

   public boolean isDead() {
      return this.keyReference == DEAD_WEAK_KEY;
   }

   public void die() {
      Reference<?> keyRef = this.keyReference;
      this.keyReference = DEAD_WEAK_KEY;
      keyRef.clear();
   }
}
