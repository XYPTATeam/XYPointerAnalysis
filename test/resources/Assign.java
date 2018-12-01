import benchmark.objects.*;

public class Assign {
    public static void main(String[] args) {
        A a1 = new A();

        A a2 = a1;

        B b1 = new B();
        a1.f = b1;

        B b2 = a1.f;

        B b3 = a1.getF();
    }
}