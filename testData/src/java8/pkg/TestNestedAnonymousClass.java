package pkg;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestNestedAnonymousClass {
  public Runnable r = new B() {
    public void run() {
      ExecutorService ex = Executors.newFixedThreadPool(1);
      ex.submit(new Runnable() {
        public void run() {
          System.out.println("Hello");
          b();
          TestNestedAnonymousClass.this.a();
        }
      });
    }
  };

  public abstract class B implements Runnable {
    protected void b() {

    }
  }

  public void a() {

  }
}
