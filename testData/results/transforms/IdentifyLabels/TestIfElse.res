11 Root  F:12
12 If    P:11 F:1  D:IfElse D:Neg
8  Block P:12
7  Block P:12
1  Block P:12
9  Exit
1  -> 8  Regular        L E If:12
1  -> 7  Regular        L E IfE:12
8  -> 9  BreakExit C:12
7  -> 9  BreakExit C:12
12 < [If [Func BOOL_NOT [Func GT [Var U 2] [Const X 0]]]]
8  < [Return RETURN I [Const I 0]]
7  < [Return RETURN I [Const I 1]]
=======================================
if (!(var2 > 0)) {
   return 0;
} else {
   return 1;
}