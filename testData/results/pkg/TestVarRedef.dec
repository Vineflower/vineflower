package pkg;

import java.io.StringWriter;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;

public class TestVarRedef {
   public static Transformer newTransformer() {
      return null;// 10
   }

   public String myMethod() {
      Source source = null;// 14

      try {
         if (source == null) {// 16
            return null;// 17 18
         }

         StringWriter stringWriter = new StringWriter();// 20
         StreamResult streamResult = new StreamResult(stringWriter);// 21
         newTransformer().transform(source, streamResult);// 22
         return stringWriter.getBuffer().toString();// 23 24
      } catch (Exception e) {// 25
         throw new RuntimeException(e);// 26
      } finally {
         if (source != null) {// 28
            System.out.println(".");// 29
         }
      }
   }
}

class 'pkg/TestVarRedef' {
   method 'newTransformer ()Ljavax/xml/transform/Transformer;' {
      0      9
      1      9
   }

   method 'myMethod ()Ljava/lang/String;' {
      0      13
      1      13
      2      16
      3      16
      4      16
      5      16
      6      17
      9      17
      1f      20
      24      21
      28      21
      29      22
      2a      22
      2b      22
      2c      22
      2d      22
      2e      22
      2f      22
      30      22
      31      23
      32      23
      33      23
      34      23
      35      23
      36      23
      37      23
      3c      23
      4d      24
      52      25
      56      25
      59      27
      5a      27
      5b      27
      5c      27
      5d      28
      5e      28
      5f      28
      60      28
      61      28
      62      28
      63      28
      64      28
   }
}

Lines mapping:
10 <-> 10
14 <-> 14
16 <-> 17
17 <-> 18
18 <-> 18
20 <-> 21
21 <-> 22
22 <-> 23
23 <-> 24
24 <-> 24
25 <-> 25
26 <-> 26
28 <-> 28
29 <-> 29
Not mapped:
31
