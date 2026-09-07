package net.chuck.chuckpack.modules.world.printer;

/**
 * Minimal mutable pair replacing {@code net.minecraft.util.Tuple},
 * which was removed in 26.2. Same getA/getB/setA/setB surface the
 * 26.1.2 code used, printer logic untouched.
 */
public class Tuple<A, B> {
    private A a;
    private B b;

    public Tuple(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    public void setA(A a) {
        this.a = a;
    }

    public void setB(B b) {
        this.b = b;
    }
}
