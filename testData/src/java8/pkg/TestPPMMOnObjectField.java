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

public class TestPPMMOnObjectField {
   int a;
   // Bytecode wise ipp and ppi are identical when not using the intermediate value.
   // We keep these seperate tests just to see the bytecode.
   public void ipp() {
      a++;
      a++;
      a++;
      a++;
   }
   public void ppi() {
      ++a;
      ++a;
      ++a;
      ++a;
   }
   public void imm() {
      a--;
      a--;
      a--;
      a--;
   }
   public void mmi() {
      --a;
      --a;
      --a;
      --a;
   }
   
   // These versions actually use the intermediate value
   public void ippf() {
      t(a++);
      t(a++);
      t(a++);
      t(a++);
   }
   public void ppif() {
      t(++a);
      t(++a);
      t(++a);
      t(++a);
   }
   public void immf() {
      t(a--);
      t(a--);
      t(a--);
      t(a--);
   }
   public void mmif() {
      t(--a);
      t(--a);
      t(--a);
      t(--a);
   }
   private static void t(int x){
   }
}