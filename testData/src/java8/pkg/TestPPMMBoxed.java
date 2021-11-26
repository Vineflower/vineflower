/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

public class TestPPMMBoxed {
   // Bytecode wise ipp and ppi are identical when not using the intermediate value. 
   // We keep these seperate tests just to see the bytecode.
   public void ipp() {
      Integer a = 0;
      a++;
      a++;
      a++;
      a++;
   }
   public void ppi() {
      Integer a = 0;
      ++a;
      ++a;
      ++a;
      ++a;
   }
   public void imm() {
      Integer a = 0;
      a--;
      a--;
      a--;
      a--;
   }
   public void mmi() {
      Integer a = 0;
      --a;
      --a;
      --a;
      --a;
   }
   
   // These versions actually use the intermediate value
   public void ippf() {
      Integer a = 0;
      t(a++);
      t(a++);
      t(a++);
      t(a++);
   }
   public void ppif() {
      Integer a = 0;
      t(++a);
      t(++a);
      t(++a);
      t(++a);
   }
   public void immf() {
      Integer a = 0;
      t(a--);
      t(a--);
      t(a--);
      t(a--);
   }
   public void mmif() {
      Integer a = 0;
      t(--a);
      t(--a);
      t(--a);
      t(--a);
   }
   private static void t(int x){
   }
}