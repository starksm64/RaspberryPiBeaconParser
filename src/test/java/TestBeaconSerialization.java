import org.jboss.summit2015.beacon.Beacon;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.FileInputStream;
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
}
