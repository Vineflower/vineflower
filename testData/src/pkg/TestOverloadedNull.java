package pkg;

public class TestOverloadedNull {
    public void test() {
        Object o = null;
        accept((Integer) o);
    }

    private void accept(Object o) {

    }

    private void accept(Number n) {

    }

    private void accept(Integer i) {

    }
}
