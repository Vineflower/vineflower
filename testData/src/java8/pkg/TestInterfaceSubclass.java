package pkg;

public interface TestInterfaceSubclass {
    void doThing();

    int doOtherThing();

    class Subclass implements TestInterfaceSubclass {

        @Override
        public void doThing() {
            System.out.println("Hi");
        }

        @Override
        public int doOtherThing() {
            return 0;
        }
    }

    abstract class AbstractClass implements TestInterfaceSubclass {
        public abstract double doDoubleThing();
    }

    class Multiple extends AbstractClass implements TestInterfaceSubclass {

        @Override
        public void doThing() {
            System.out.println("Hello");
        }

        @Override
        public int doOtherThing() {
            return 1;
        }

        @Override
        public double doDoubleThing() {
            return 2.452;
        }
    }
}
