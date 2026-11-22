package pkg

/*
 * This is a multifile facade. The actual implementations of the functions are in the following files:
 * pkg/TestMultifile__TestMultifileAKt
 * pkg/TestMultifile__TestMultifileBKt
 */
internal class TestMultifile {
   @JvmStatic
   fun inA() {
      TestMultifile__TestMultifileAKt.inA()
   }

   @JvmStatic
   fun inB() {
      TestMultifile__TestMultifileBKt.inB()// 1
   }
}

class 'pkg/TestMultifile' {
   method 'inA ()V' {
      0      10
      1      10
      2      10
      3      11
   }

   method 'inB ()V' {
      0      15
      1      15
      2      15
      3      16
   }
}

Lines mapping:
1 <-> 16
