/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pkg;


public class TestAsserts {
  public static int foo() {
    int i=1;
    assert i > 1;
    assert i> 1 && i < 5;
    return 1;
  }

  public void test1(int x) {
    System.out.println("a");
    assert x > 5;
    if (x == 7) {
      System.out.println("b");
    }

    System.out.println("c");
  }

  public void test2(int x) {
    System.out.println("a");
    if (x == 7) {
      System.out.println("b");
    }
    assert x > 5;


    System.out.println("c");
  }

  public static class InnerStatic {
    public void test3(int x) {
      System.out.println("a");
      assert x > 10;
    }
  }

  public class Inner {
    public void test4(int x) {
      System.out.println("a");
      assert x > 10;
    }
  }

  public void test5(int x) {
    System.out.println("a");
    if (x == 7) {
      System.out.println("b");
    }
    assert false;


    System.out.println("c");
  }

  public void test6(int x) {
    System.out.println("a");
    if (x > 7) {
      assert x > 10;
      System.out.println("c");
    }

    System.out.println("c");
  }

  public void test7(int x) {
    System.out.println("a");
    if (x > 7) {
      assert x > 10;
    }

    System.out.println("c");
  }
}
