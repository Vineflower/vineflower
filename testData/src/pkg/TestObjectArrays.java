package pkg;

import java.util.Random;

public class TestObjectArrays {
    public void test() {
        Object[][] things = new Object[3][];

        for (Object[] thing : things) {
            for (Object o : thing) {
                System.out.println(o);
            }
        }

        things[0] = new Object[]{1, new int[]{1, 2, 3}, "hi"};
        things[1] = new Object[]{new Object()
        {
            int x = 3;
            {
                x *= 3 * x;
                System.out.println("Hi!");
            }

            {
                x += 224;
            }

            int hashcode = new Random().nextInt() + x;

            @Override
            public int hashCode() {
                return hashcode;
            }
        }
        };
    }
}
