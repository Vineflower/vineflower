package pkg;

import java.util.List;
import java.util.Random;

public class TestIfLoop {
    public int testCompoundCondition(int i, int j) {
        while (i > 0 && j < 3) {
            if (j < 0) {
                j -= 1;

                if (i > 3) {
                    if (j == -2) {
                        j = 1;
                    }
                }
            }

            if (i > 10) {
                i--;
                return i;
            }
        }

        return 3;
    }

    public Object testCollection(List<Object> list) {
        while (!list.isEmpty()) {
            if (list.size() > 3) {
                list.remove(3);
            } else {
                return list.remove(0);
            }
        }

        return null;
    }

    public void testCompound2(int a, int b, Random random) {
        a = random.nextInt(8) - random.nextInt(8) + a;
        b = random.nextInt(8) - random.nextInt(8) + b;

        while (a >= 0 && a <= 20 && b >= 0 && b <= 20) {
            a -= random.nextInt(4) - random.nextInt(4);
            b -= random.nextInt(4) - random.nextInt(4);
        }
    }

    public int testElseIf(int i) {
        while (i > 0) {
            if (i == 10) {
                i++;
            } else if (i == 11) {
                i += 2;
            } else if (i == 12) {
                i--;
            } else if (i == 13) {
                i /= 2;
            } else if (i == 14) {
                i -= 4;
            } else {
                throw new RuntimeException();
            }
        }

        return 2;
    }
}
