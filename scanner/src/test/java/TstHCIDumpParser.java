import org.jboss.summit2015.beacon.Beacon;
import org.jboss.summit2015.beacon.scanner.HCIDumpParser;
import org.jboss.summit2015.beacon.scanner.CommandArgs;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TstHCIDumpParser {
   private static final String PI_HOST = "";

   @Test
   public void testQuantifier() {
      String line = "> 04 dog dog dog dog dog dog";
      Pattern p = Pattern.compile(">\\s04((\\s[dog]{3}){3})(.*)");
      Matcher m = p.matcher(line);
      assert m.matches();

      for(int n = 0; n <= m.groupCount(); n ++)
         System.out.printf("group(%d): %s\n", n, m.group(n));
   }
   @Test
   public void testHexREGroups() {
      String line = "> 04 3E 2A 02 01 03 01 17 8F A5 38 12 0F 1E 02 01 06 1A FF 4C ";
      Pattern p = Pattern.compile(">\\s04((?:\\s\\p{XDigit}{2}){6})(.*)");
      Matcher m = p.matcher(line);
      assert m.matches();
      for(int n = 0; n <= m.groupCount(); n ++)
         System.out.printf("group(%d): %s\n", n, m.group(n));
   }

   /**
    * Test matching the first line of a beacon in the hcidump output
    */
   @Test
   public void testRE() {
      String line = "> 04 3E 2A 02 01 03 01 92 EF 0D 06 40 30 1E 02 01 06 1A FF 4C ";
      Pattern p = Pattern.compile(">\\s04.*((?:\\s\\p{XDigit}{2}){6})((?:\\s\\p{XDigit}{2}){4}) 1A FF 4C");
      Matcher m = p.matcher(line.trim());
      Assert.assertTrue("There are matches", m.matches());
      for(int n = 0; n <= m.groupCount(); n ++)
         System.out.printf("group(%d): %s\n", n, m.group(n));
      System.out.printf("BDADDR: %s\n", m.group(1));
   }

   /**
    * Test running the HCIDumpParser in parser mode with a test file stream resource located at /hcidump.raw
    * @throws Exception
    */
   @Test
   public void testHcidumpParser() throws Exception {
      String[] args = {"-rawDumpFile", "/hcidump.raw", "parse", "-skipPublish"};
      HCIDumpParser parser = new HCIDumpParser();
      LinkedBlockingDeque<Beacon> beacons = new LinkedBlockingDeque<>();
      parser.setBeacons(beacons);
      parser.run(args);
      Assert.assertTrue("Beacon count > 0", beacons.size() > 0);
      System.out.printf("beacon count=%d\n", beacons.size());
   }

   /**
    * Test running the HCIDumpParser in streaming mode with a test file stream resource located at /hcidump.raw. This
    * exposes the stream via a local socket at port 12345 by default.
    * @see #testHcidumpStreamRead
    * @see #testHcidumpStreamRead5000
    * @throws Exception
    */
   @Test
   public void testHcidumpStream() throws Exception {
      String[] args = {"-rawDumpFile", "/hcidump.raw", "stream"};
      HCIDumpParser parser = new HCIDumpParser();
      parser.run(args);
   }

   /**
    *
    * @throws Exception
    */
   @Test
   public void testHcidumpSSHStream() throws Exception {
      String[] args = {"stream-ssh", "-host", "192.168.1.95", "-skipPublish"};
      HCIDumpParser parser = new HCIDumpParser();
      parser.setBeacons(new LinkedBlockingDeque<>());
      Thread t = new Thread(() -> {
         try {
            parser.run(args);
         } catch (Exception e) {
            e.printStackTrace();
         }
      }, "ParserThread");
      t.start();
      LinkedBlockingDeque<Beacon> beacons = parser.getBeacons();
      while(beacons.size() < 100) {
         System.out.printf("Waiting for 100 beacons..., size=%d\n", beacons.size());
         Thread.sleep(1000);
      }
      System.out.printf("%s\n", beacons);
      parser.stop();
   }

   /**
    * Test client to read the hcidump stream from the HCIDumpParser running in stream mode. Since this loops
    * until no data is returned, the server should be running with a rawDumpFile specified.
    * @see CommandArgs#rawDumpFile
    * @throws Exception
    */
   @Test
   public void testHcidumpStreamRead() throws Exception {
      Socket client = new Socket(InetAddress.getLocalHost(), 12345);
      //Socket client = new Socket(InetAddress.getByName("192.168.1.95"), 12345);
      InputStream is = client.getInputStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      String line = br.readLine();
      while(line != null) {
         System.out.println(line);
         line = br.readLine();
      }
      client.close();
   }

   /**
    * Test client to read 5000 lines from the hcidump stream from the HCIDumpParser running in stream mode.
    * @throws Exception
    */
   @Test
   public void testHcidumpStreamRead5000() throws Exception {
      Socket client = new Socket(InetAddress.getLocalHost(), 12345);
      //Socket client = new Socket(InetAddress.getByName("192.168.1.95"), 12345);
      InputStream is = client.getInputStream();
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      for(int n = 0; n < 5000; n ++) {
         String line = br.readLine();
         System.out.printf("%d, %s\n", n, line);
      }
      client.close();
   }
}
