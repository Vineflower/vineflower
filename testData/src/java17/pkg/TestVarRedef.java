package pkg;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class TestVarRedef {
  public static Transformer newTransformer() {
    return null;
  }

  public String myMethod() {
    Source source = null;
    try {
      if (source == null) {
        String string = null;
        return string;
      }
      StringWriter stringWriter = new StringWriter();
      StreamResult streamResult = new StreamResult(stringWriter);
      newTransformer().transform(source, streamResult);
      String string = stringWriter.getBuffer().toString();
      return string;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (source != null) {
        System.out.println(".");
      }
    }
  }
}
