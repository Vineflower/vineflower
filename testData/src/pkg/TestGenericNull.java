package pkg;

public abstract class TestGenericNull<T> {
    public abstract T get();


    public class Int extends TestGenericNull<Integer> {
        @Override
        public Integer get() {
            return null;
        }
    }
}
