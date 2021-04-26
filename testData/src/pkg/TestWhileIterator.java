package pkg;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TestWhileIterator {
    public int testNested(List<Object> list, Set<Object> set) {
        Iterator<Object> it1 = list.iterator();
        Iterator<Object> it2 = set.iterator();
        int i = 0;

        while (it1.hasNext() && it2.hasNext()) {
            if (it1.next().equals(it2.next())) {
                i++;
            }
        }

        return i;
    }
}
