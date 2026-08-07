package kaptainwutax.mcutils.util.data;

import java.util.Objects;

public final class Pair<A, B> {
   private final A a;
   private final B b;

   public Pair(A a, B b) {
      this.a = a;
      this.b = b;
   }

   public Pair(Pair<? extends A, ? extends B> other) {
      this(other.a, other.b);
   }

   public A getFirst() {
      return this.a;
   }

   public B getSecond() {
      return this.b;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Pair)) {
         return false;
      } else {
         Pair<?, ?> pair = (Pair)o;
         return Objects.equals(this.a, pair.a) && Objects.equals(this.b, pair.b);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.a, this.b});
   }

   public String toString() {
      String var10000 = String.valueOf(this.a);
      return "(" + var10000 + ", " + String.valueOf(this.b) + ")";
   }
}
