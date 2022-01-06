package pkg;

public class TestLongMethodInvocation {
  private TestLongMethodInvocation longField;

  public TestLongMethodInvocation longMethodInvocation() {
    this.longMethodInvocation()
      .longMethodInvocation()
      .longMethodInvocation()
      .longMethodInvocation()
      .longMethodInvocation()
      .longMethodInvocation()
      .longMethodInvocation()
      .longMethodInvocation()
      .longMethodInvocation()
      .longMethodInvocation()
      .longMethodInvocation()
      .longMethodInvocation()
      .longMethodInvocation()
      .longMethodInvocation()
      .longMethodInvocation()
      .longMethodInvocation();
    return this;
  }

  public void longFieldInvocation() {
    this.longField
      .longField
      .longField
      .longField
      .longField
      .longField
      .longField
      .longField
      .longField
      .longField
      .longField
      .longField
      .longField
      .longField
      .longField
      .longField
      .longField
      .longField
      .longField
      .longField
      .longField
      .longField
      .longField
      .longField
      .longMethodInvocation();
  }

  public void longMixture() {
    this.longField
      .longMethodInvocation()
      .longField
      .longMethodInvocation()
      .longField
      .longMethodInvocation()
      .longField
      .longMethodInvocation()
      .longField
      .longMethodInvocation()
      .longField
      .longMethodInvocation()
      .longField
      .longMethodInvocation()
      .longField
      .longMethodInvocation();
  }
}
