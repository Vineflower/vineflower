8  Root  F:13
13 Seq   P:8  F:12
12 Do    P:13 F:9  D:While
9  If    P:12 F:2  D:If D:Neg D:PPMM
2  Block P:9
5  Block P:13
6  Exit
12 -> 5  Regular        L
9  -> 5  Break     C:12 E
2  -> 12 Continue  C:12 E IfN:9
5  -> 6  BreakExit C:12
12 < [Func GT [Var U 1] [Const I 10]]
9  < [If [Func BOOL_NOT [Func EQ [Func PPI I [Var U 1]] [Const I 15]]]]
5  < [Return RETURN V]
=======================================
while (var1 > 10) {
   if (!(++var1 == 15)) {
      continue;
   }
   break;
}

return;