package pkg;

public class TestRenameEntities {
  public class a {
    public Object a;

    public void a() {
      this.a = new Object();

      new b().a();
    }
  }

  public class b {
    public Object a;

    public void a() {
      this.a = new Object();

      new a().a();
    }
  }
}
