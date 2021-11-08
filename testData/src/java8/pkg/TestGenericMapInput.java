package pkg;

import java.util.List;
import java.util.Map;

public class TestGenericMapInput {
    public static<N extends Number> void addToMap(Map<String, N> map, List<N> source)
    {
        // Should not create (Foo item : source)
        for (N item : source)
        {
            map.put(item.toString(), item);
        }
    }
}
