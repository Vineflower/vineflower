/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

public class TestClassSwitch {

  public void testCaseOrder(int a) {

    switch (a) {
      case 13:
        System.out.println(13);
        return;
      case 5:
        System.out.println(5);
    }
  }

  public void testFallThrough(int a, int b) {

    int x = 17;

    switch (a) {
      case 13:
        System.out.println(13);
      case 5:
        System.out.println(5);
        x += 17;
        break;
      case 17:
        System.out.println(17);
        if (b > 0) {
          x = 5000;
        }
      case 18:
      case 19:
      case 20:
        System.out.println("hi");
        x += 170000;
        break;
    }

    System.out.println(x);
  }
}
