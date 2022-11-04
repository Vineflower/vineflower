8  Root  F:16
16 Do    P:8  F:19 D:Do
19 Seq   P:16 F:13
13 If    P:19 F:1  D:If D:Neg
18 Seq   P:13 F:11
1  Block P:13
11 If    P:18 F:2  D:If D:Matched
4  Block P:19
2  Block P:11
5  Block P:18
6  Exit
13 -> 4  Regular        L
1  -> 18 Regular        L E If:13
11 -> 5  Regular        L
2  -> 4  Break     C:13 L E If:11
4  -> 16 Continue  C:16 L
5  -> 6  BreakExit C:16 E
13 < [If [Func GE [Invoke hashCode java/lang/Object ()I VIRTUAL [Var U 1]] [Const I 0]]]
11 < [If [Func NE [Func INSTANCEOF [Var U 1] [Const java/lang/String null] [Var java/lang/String 2]] [Const Z 0]]]
4  < [Invoke println java/io/PrintStream (Ljava/lang/Object;)V VIRTUAL [Field out java/lang/System Ljava/io/PrintStream;] [Var U 1]]
5  < [Return RETURN V]
=======================================
while(true) {
   label13:
   if (var1.hashCode() >= 0) {
      if (var1 instanceof "null" var2 != false) {
         System.out.println(var1);
      }

      return;
   }

   System.out.println(var1);
}