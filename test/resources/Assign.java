import benchmark.objects.A;
import benchmark.objects.B;

public class Assign {
    public static void main(String[] args) {
        A a1 = new A();

        A a2 = a1;

        B b1 = new B();
        a1.f = b1;

        B b2 = a1.f;

        a2.f = a1.f;

        B b3 = a1.getF();

        A[] aArray = new A[3];

        aArray[0] = a1;
        a2 = aArray[0];
    }
}