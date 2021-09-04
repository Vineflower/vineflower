package pkg;

public class TestLocalScopeClash {
    void acceptInt(int i) {

    }

    void acceptBoolean(boolean b) {

    }

    void test(boolean a, boolean b) {
        {
            int i = 32767;
            i = i + 1;
            acceptInt(i);
            i = i + 1;
            i = i | 7;
            i = 1 ^ i;
            i = i * 2;
            i = i + 2;
            acceptInt(i);
        }
        {
            boolean i = a;
            i &= (i & b);
            i ^= i || b;
            acceptBoolean(i);
        }
    }
}
