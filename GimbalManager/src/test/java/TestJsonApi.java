import org.junit.Test;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.io.StringWriter;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TestJsonApi {
   @Test
   public void testJsonGenerator() {
      StringWriter sw = new StringWriter();
      JsonGenerator json = Json.createGenerator(sw);
      json.writeStartObject();
      json.write("factory_id", "12345");
      json.write("name", "Trial4");
      json.write("config_id", 12345);
      json.writeEnd();
      json.flush();
      System.out.printf("json=%s\n", sw.toString());
   }
}
