package pkg;

import ext.ExampleAnnotation;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public record TestTextTokens2<T>(String name, @ExampleAnnotation T value, int index, Object... args) {
   public void foo() {
      System.out.println(this.name + ": " + this.value);// 12
   }// 13

   public void bar(Supplier<Optional<? extends BiConsumer<T, String>>> r) {
      String s = "Hello world";// 16
      ((Optional)r.get()).ifPresent(c -> c.accept(this.value, s));// 17
   }// 18

   public Function<String, String> baz() {
      return s -> s + " " + this.name;// 21
   }
}

class 'pkg/TestTextTokens2' {
   method 'foo ()V' {
      0      10
      1      10
      2      10
      3      10
      4      10
      5      10
      6      10
      7      10
      8      10
      9      10
      a      10
      b      10
      c      10
      d      10
      e      10
      f      10
      10      10
      11      10
      12      10
      13      11
   }

   method 'bar (Ljava/util/function/Supplier;)V' {
      0      14
      1      14
      2      14
      3      15
      4      15
      5      15
      6      15
      7      15
      8      15
      9      15
      a      15
      b      15
      13      15
      14      15
      15      15
      16      16
   }

   method 'lambda$bar$0 (Ljava/lang/String;Ljava/util/function/BiConsumer;)V' {
      0      15
      1      15
      2      15
      3      15
      4      15
      5      15
      6      15
      7      15
      8      15
      9      15
      a      15
      b      15
   }

   method 'baz ()Ljava/util/function/Function;' {
      6      19
   }

   method 'lambda$baz$1 (Ljava/lang/String;)Ljava/lang/String;' {
      0      19
      1      19
      2      19
      3      19
      4      19
      5      19
      6      19
      7      19
      8      19
      9      19
      a      19
   }
}

Lines mapping:
12 <-> 11
13 <-> 12
16 <-> 15
17 <-> 16
18 <-> 17
21 <-> 20

/*
Tokens:
(9:15, 9:30) class [declaration] pkg/TestTextTokens2
(9:34, 9:40) class [reference] java/lang/String
(9:41, 9:45) field [declaration] pkg/TestTextTokens2#name:Ljava/lang/String;
(9:48, 9:65) class [reference] ext/ExampleAnnotation
(9:68, 9:73) field [declaration] pkg/TestTextTokens2#value:Ljava/lang/Object;
(9:79, 9:84) field [declaration] pkg/TestTextTokens2#index:I
(9:86, 9:92) class [reference] java/lang/Object
(9:96, 9:100) field [declaration] pkg/TestTextTokens2#args:[Ljava/lang/Object;
(10:16, 10:19) method [declaration] pkg/TestTextTokens2#foo()V
(11:7, 11:13) class [reference] java/lang/System
(11:14, 11:17) field [reference] java/lang/System#out:Ljava/io/PrintStream;
(11:18, 11:25) method [reference] java/io/PrintStream#println(Ljava/lang/String;)V
(11:31, 11:35) field [reference] pkg/TestTextTokens2#name:Ljava/lang/String;
(11:50, 11:55) field [reference] pkg/TestTextTokens2#value:Ljava/lang/Object;
(14:16, 14:19) method [declaration] pkg/TestTextTokens2#bar(Ljava/util/function/Supplier;)V
(14:20, 14:28) class [reference] java/util/function/Supplier
(14:29, 14:37) class [reference] java/util/Optional
(14:48, 14:58) class [reference] java/util/function/BiConsumer
(14:62, 14:68) class [reference] java/lang/String
(14:72, 14:73) parameter [declaration] pkg/TestTextTokens2#bar(Ljava/util/function/Supplier;)V(1:r)
(15:7, 15:13) class [reference] java/lang/String
(15:14, 15:15) local [declaration] pkg/TestTextTokens2#bar(Ljava/util/function/Supplier;)V(2:s)
(16:9, 16:17) class [reference] java/util/Optional
(16:18, 16:19) parameter [reference] pkg/TestTextTokens2#bar(Ljava/util/function/Supplier;)V(1:r)
(16:20, 16:23) method [reference] java/util/function/Supplier#get()Ljava/lang/Object;
(16:27, 16:36) method [reference] java/util/Optional#ifPresent(Ljava/util/function/Consumer;)V
(16:37, 16:38) parameter [declaration] pkg/TestTextTokens2#lambda$bar$0(Ljava/lang/String;Ljava/util/function/BiConsumer;)V(2:c)
(16:42, 16:43) parameter [reference] pkg/TestTextTokens2#lambda$bar$0(Ljava/lang/String;Ljava/util/function/BiConsumer;)V(2:c)
(16:44, 16:50) method [reference] java/util/function/BiConsumer#accept(Ljava/lang/Object;Ljava/lang/Object;)V
(16:56, 16:61) field [reference] pkg/TestTextTokens2#value:Ljava/lang/Object;
(16:63, 16:64) local [reference] pkg/TestTextTokens2#bar(Ljava/util/function/Supplier;)V(2:s)
(19:11, 19:19) class [reference] java/util/function/Function
(19:20, 19:26) class [reference] java/lang/String
(19:28, 19:34) class [reference] java/lang/String
(19:36, 19:39) method [declaration] pkg/TestTextTokens2#baz()Ljava/util/function/Function;
(20:14, 20:15) parameter [declaration] pkg/TestTextTokens2#lambda$baz$1(Ljava/lang/String;)Ljava/lang/String;(1:s)
(20:19, 20:20) parameter [reference] pkg/TestTextTokens2#lambda$baz$1(Ljava/lang/String;)Ljava/lang/String;(1:s)
(20:34, 20:38) field [reference] pkg/TestTextTokens2#name:Ljava/lang/String;
*/
