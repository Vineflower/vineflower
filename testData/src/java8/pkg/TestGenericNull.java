package pkg;

import java.util.Map;
import java.util.Optional;

public abstract class TestGenericNull<T> {
    public abstract T get();


    public class Int extends TestGenericNull<Integer> {
        @Override
        public Integer get() {
            return null;
        }
    }

    public Object doThing(Map<Integer, Optional<T>> map) {
      return map.get(0).orElse(null);
    }
}
