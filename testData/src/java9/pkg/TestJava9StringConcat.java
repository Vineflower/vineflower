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

public class TestJava9StringConcat {
  public String test1(String prefix, int a) {
    return prefix + a;
  }

  public String test2(String var, int b, Object c) {
    return "(" + var + "-" + b + "---" + c + ")";
  }

  public String testEmptyPrefix(int value){
    return "" + value;
  }

  public String testEmptyInfix(int value, int value2){
    return value + "" + value2;
  }

  public String test3() {
    return "" + get();
  }

  public String get() {
    return "";
  }
}