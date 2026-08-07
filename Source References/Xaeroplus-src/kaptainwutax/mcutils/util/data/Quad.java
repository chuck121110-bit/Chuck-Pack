package kaptainwutax.mcutils.util.data;

import java.util.Objects;

public final class Quad<A, B, C, D> {
   private final A a;
   private final B b;
   private final C c;
   private final D d;

   public Quad(A a, B b, C c, D d) {
      this.a = a;
      this.b = b;
      this.c = c;
      this.d = d;
   }

   public Quad(Quad<? extends A, ? extends B, ? extends C, ? extends D> other) {
      this(other.a, other.b, other.c, other.d);
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

   public D getFourth() {
      return this.d;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Quad)) {
         return false;
      } else {
         Quad<?, ?, ?, ?> quad = (Quad)o;
         return Objects.equals(this.a, quad.a) && Objects.equals(this.b, quad.b) && Objects.equals(this.c, quad.c) && Objects.equals(this.d, quad.d);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.a, this.b, this.c, this.d});
   }

   public String toString() {
      String var10000 = String.valueOf(this.a);
      return "(" + var10000 + ", " + String.valueOf(this.b) + ", " + String.valueOf(this.c) + ", " + String.valueOf(this.d) + ")";
   }
}
