package pkg;

import java.util.Random;

public class TestArrayInitializations {
  public int[] test1(Random r){
    return new int[]{r.nextInt(10), r.nextInt(20)};
  }

  public int[] test2(Random r){
    final int[] ints = new int[2];
    ints[0] = r.nextInt(10);
    ints[1] = r.nextInt(20);
    return ints;
  }

  public int[] test3(Random r){
    final int[] ints = new int[2];
    ints[1] = r.nextInt(20);
    ints[0] = r.nextInt(10);
    return ints;
  }

  public Object[] test4(Random r){
    final Object[] objects = new Object[3];
    objects[0] = r.nextInt(10);
    objects[1] = r;
    objects[2] = objects;
    return objects;
  }

  public Object[] test5(Random r){
    final Object[] objects = new Object[3];
    objects[0] = r.nextInt(10);
    objects[1] = r;
    objects[2] = objects[0];
    return objects;
  }

  public Object[] test6(Random r){
    Object[] o;
    Object[] objects;
    objects = o = new Object[3];
    objects[0] = r.nextInt(10);
    objects[1] = r;
    objects[2] = o;
    return objects;
  }

  public Object[] test7(Random r){
    Object[] o;
    Object[] objects;
    o = objects = new Object[3];
    objects[0] = r.nextInt(10);
    objects[1] = r;
    objects[2] = o;
    return objects;
  }

  public int[] test8(Random r){
    return new int[]{10, 20};
  }

  public int[] test9(Random r){
    final int[] ints = new int[2];
    ints[0] = 10;
    ints[1] = 20;
    return ints;
  }

  public int[] test10(Random r){
    final int[] ints = new int[2];
    ints[0] = 10;
    ints[1] = r.nextInt(20);
    return ints;
  }

  public int[] test11(Random r){
    final int[] ints = new int[2];
    ints[0] = r.nextInt(10);
    ints[1] = 20;
    return ints;
  }

  public int[] test12(Random r){
    final int[] ints = new int[3];
    ints[0] = r.nextInt(10);
    ints[2] = 30;
    ints[1] = r.nextInt(20);
    return ints;
  }

  public int[] test13(Random r){
    final int[] ints = new int[2];
    ints[1] = 20;
    ints[0] = 10;
    return ints;
  }

  public int[] test14(Random r){
    final int[] ints = new int[2];
    ints[1] = r.nextInt(20);
    ints[0] = 10;
    return ints;
  }

  public int[] test15(Random r){
    final int[] ints = new int[2];
    ints[1] = 20;
    ints[0] = r.nextInt(10);
    return ints;
  }

  public int[] test16(Random r){
    final int[] ints = new int[3];
    ints[1] = r.nextInt(20);
    ints[2] = 30;
    ints[0] = r.nextInt(10);
    return ints;
  }
}
