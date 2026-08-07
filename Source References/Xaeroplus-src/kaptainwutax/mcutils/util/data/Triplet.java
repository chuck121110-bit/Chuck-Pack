package kaptainwutax.mcutils.util.data;

import java.util.Objects;

public final class Triplet<A, B, C> {
   private final A a;
   private final B b;
   private final C c;

   public Triplet(A a, B b, C c) {
      this.a = a;
      this.b = b;
      this.c = c;
   }

   public Triplet(Triplet<? extends A, ? extends B, ? extends C> other) {
      this(other.a, other.b, other.c);
   }

   public A getFirst() {
      return this.a;
   }

   public B getSecond() {
      return this.b;
   }

   public C getThird() {
      return this.c;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Triplet)) {
         return false;
      } else {
         Triplet<?, ?, ?> triplet = (Triplet)o;
         return Objects.equals(this.a, triplet.a) && Objects.equals(this.b, triplet.b) && Objects.equals(this.c, triplet.c);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.a, this.b, this.c});
   }

   public String toString() {
      String var10000 = String.valueOf(this.a);
      return "(" + var10000 + ", " + String.valueOf(this.b) + ", " + String.valueOf(this.c) + ")";
   }
}
