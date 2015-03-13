import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonStreamParser;
import org.jboss.summit2015.beacon.Beacon;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TestBeaconSerialization {
   @Test
   public void testReadBeacon() throws IOException {
      FileInputStream fis = new FileInputStream("/tmp/testSerializeBeacon.ser");
      DataInputStream dis = new DataInputStream(fis);
      byte[] data = new byte[fis.available()];
      dis.readFully(data);
      dis.close();

      Beacon beacon = Beacon.fromByteMsg(data);
      System.out.printf("Read beacon: %s\n", beacon);
   }


   @Test
   public void testWriteJson() throws Exception {
      // (String scannerID, String uuid, int code, int manufacturer, int major, int minor, int power, int rssi)
      String scannerID = "testWriteJson";
      String uuid = "DAF246CE836311E4";
      int code = 533;
      int manufacturer = 19456;
      int major = 45334;
      int minor = 4667;
      int power = 0;
      int rssi = -79;
      long time = System.currentTimeMillis();
      Beacon beacon = new Beacon(scannerID, uuid, code, manufacturer, major, minor, power, rssi, time);
      String jsonOutput = beacon.toJSON();
      FileWriter fw = new FileWriter("/tmp/testWriteJson.json");
      fw.write(jsonOutput);
      beacon.setMajor(beacon.getMajor()+1);
      jsonOutput = beacon.toJSON();
      fw.write(jsonOutput);
      fw.close();
   }
   @Test
   public void testReadJson() throws Exception {
      FileReader fr = new FileReader("/tmp/testWriteJson.json");
      Gson gson = new Gson();
      JsonStreamParser parser = new JsonStreamParser(fr);
      while(parser.hasNext()) {
         JsonElement jse = parser.next();
         Beacon beacon = gson.fromJson(jse, Beacon.class);
         System.out.printf("Beacon from json:%s\n", beacon);
      }
   }

   @Test
   public void testGetMessageTypeTime() {
      Gson gson = new Gson();
      String json = "{\n" +
         "  \"scannerID\": \"Room203\",\n" +
         "  \"uuid\": \"DAF246CEF20311E4B116123B93F75CBA\",\n" +
         "  \"code\": 533,\n" +
         "  \"manufacturer\": 19456,\n" +
         "  \"major\": 203,\n" +
         "  \"minor\": 20,\n" +
         "  \"power\": -62,\n" +
         "  \"calibratedPower\": -62,\n" +
         "  \"rssi\": -67,\n" +
         "  \"messageType\": 1,\n" +
         "  \"time\": 1426016481227\n" +
         "}";
      JsonStreamParser parser = new JsonStreamParser(json);
      JsonElement jse = parser.next();
      JsonPrimitive messageType = jse.getAsJsonObject().getAsJsonPrimitive("messageType");
      System.out.printf("jse.messageType = %d\n", messageType.getAsInt());
      JsonPrimitive time = jse.getAsJsonObject().getAsJsonPrimitive("time");
      System.out.printf("jse.time = %d\n", time.getAsLong());
   }
}
