package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

class WISMW<K, V> extends WIS<K, V> {
   static final LocalCacheFactory FACTORY = WISMW::new;
   protected static final VarHandle MAXIMUM;
   protected static final VarHandle WEIGHTED_SIZE;
   volatile long maximum;
   volatile long weightedSize;
   long windowMaximum;
   long windowWeightedSize;
   long mainProtectedMaximum;
   long mainProtectedWeightedSize;
   double stepSize;
   long adjustment;
   long hitsInSample;
   long missesInSample;
   double previousSampleHitRate;
   final FrequencySketch sketch = new FrequencySketch();
   final AccessOrderDeque<Node<K, V>> accessOrderWindowDeque;
   final AccessOrderDeque<Node<K, V>> accessOrderProbationDeque;
   final AccessOrderDeque<Node<K, V>> accessOrderProtectedDeque;

   WISMW(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      if (var1.hasInitialCapacity()) {
         long var4 = Math.min(var1.getMaximum(), (long)var1.getInitialCapacity());
         this.sketch.ensureCapacity(var4);
      }

      this.accessOrderWindowDeque = !var1.evicts() && !var1.expiresAfterAccess() ? null : new AccessOrderDeque();
      this.accessOrderProbationDeque = new AccessOrderDeque<Node<K, V>>();
      this.accessOrderProtectedDeque = new AccessOrderDeque<Node<K, V>>();
   }

   protected final boolean evicts() {
      return true;
   }

   protected final long maximum() {
      return MAXIMUM.get(this);
   }

   protected final long maximumAcquire() {
      return MAXIMUM.getAcquire(this);
   }

   protected final void setMaximum(long var1) {
      MAXIMUM.setRelease(this, var1);
   }

   protected final long weightedSize() {
      return WEIGHTED_SIZE.get(this);
   }

   protected final long weightedSizeAcquire() {
      return WEIGHTED_SIZE.getAcquire(this);
   }

   protected final void setWeightedSize(long var1) {
      WEIGHTED_SIZE.setRelease(this, var1);
   }

   protected final long windowMaximum() {
      return this.windowMaximum;
   }

   protected final void setWindowMaximum(long var1) {
      this.windowMaximum = var1;
   }

   protected final long windowWeightedSize() {
      return this.windowWeightedSize;
   }

   protected final void setWindowWeightedSize(long var1) {
      this.windowWeightedSize = var1;
   }

   protected final long mainProtectedMaximum() {
      return this.mainProtectedMaximum;
   }

   protected final void setMainProtectedMaximum(long var1) {
      this.mainProtectedMaximum = var1;
   }

   protected final long mainProtectedWeightedSize() {
      return this.mainProtectedWeightedSize;
   }

   protected final void setMainProtectedWeightedSize(long var1) {
      this.mainProtectedWeightedSize = var1;
   }

   protected final double stepSize() {
      return this.stepSize;
   }

   protected final void setStepSize(double var1) {
      this.stepSize = var1;
   }

   protected final long adjustment() {
      return this.adjustment;
   }

   protected final void setAdjustment(long var1) {
      this.adjustment = var1;
   }

   protected final long hitsInSample() {
      return this.hitsInSample;
   }

   protected final void setHitsInSample(long var1) {
      this.hitsInSample = var1;
   }

   protected final long missesInSample() {
      return this.missesInSample;
   }

   protected final void setMissesInSample(long var1) {
      this.missesInSample = var1;
   }

   protected final double previousSampleHitRate() {
      return this.previousSampleHitRate;
   }

   protected final void setPreviousSampleHitRate(double var1) {
      this.previousSampleHitRate = var1;
   }

   protected final FrequencySketch frequencySketch() {
      return this.sketch;
   }

   protected final AccessOrderDeque<Node<K, V>> accessOrderWindowDeque() {
      return this.accessOrderWindowDeque;
   }

   protected final AccessOrderDeque<Node<K, V>> accessOrderProbationDeque() {
      return this.accessOrderProbationDeque;
   }

   protected final AccessOrderDeque<Node<K, V>> accessOrderProtectedDeque() {
      return this.accessOrderProtectedDeque;
   }

   static {
      MethodHandles.Lookup var0 = MethodHandles.lookup();

      try {
         MAXIMUM = var0.findVarHandle(WISMW.class, "maximum", Long.TYPE);
         WEIGHTED_SIZE = var0.findVarHandle(WISMW.class, "weightedSize", Long.TYPE);
      } catch (ReflectiveOperationException var2) {
         throw new ExceptionInInitializerError(var2);
      }
   }
}
