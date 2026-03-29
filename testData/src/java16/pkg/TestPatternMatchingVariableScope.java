package pkg;

public class TestPatternMatchingVariableScope {

    public void test(Object o) {
        {
            int i = o instanceof Integer integer ? integer : 0;
            int integer = i;
            System.out.println(integer);
        }
        {
            if (o instanceof Integer i) {
                int ix = i;
            }
        }
        {
            while (o instanceof Integer i) {
                int ix = i;
            }
        }
        {
            do {
                int ix = 0;
            } while (o instanceof Integer i && i > 0);
        }
        {
            for (; o instanceof Integer i; o = i.hashCode()) {
                int ix = i;
            }
        }
        if (o instanceof Integer i) {
            System.out.println(i);
        }
    }

    public void testNegated(Object o) {
        {
            if (!(o instanceof Integer i)) {
                System.out.println();
            } else {
                System.out.println(i);
            }
        }
        {
            if (!(o instanceof Integer i)) {
                throw new IllegalArgumentException();
            } else {
                System.out.println(i);
            }
        }
        {
            while (!(o instanceof Integer i)) {
                System.out.println();
            }
            System.out.println(i);
        }
        {
            do {
                System.out.println();
            } while (!(o instanceof Integer i));
            System.out.println(i);
        }
        {
            for (; !(o instanceof Integer i); o = o.hashCode()) {
                System.out.println();
            }
            System.out.println(i);
        }
        if (o instanceof Integer i) {
            System.out.println(i);
        }
    }
}
