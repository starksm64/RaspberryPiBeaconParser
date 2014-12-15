import org.jboss.summit2015.beacon.CommandArgs;
import org.jboss.summit2015.beacon.HCIDumpParser;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TstHCIDumpParser {

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
   public void testHexRE() {
      String line = "> 04 3E 2A 02 01 03 01 17 8F A5 38 12 0F 1E 02 01 06 1A FF 4C ";
      Pattern p = Pattern.compile(">\\s04((?:\\s\\p{XDigit}{2}){3})(.*)");
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
   @Test
   public void testRE() {
      String line = "> 04 3E 2A 02 01 03 01 92 EF 0D 06 40 30 1E 02 01 06 1A FF 4C ";
      Pattern p = Pattern.compile(">\\s04.*((?:\\s\\p{XDigit}{2}){6})((?:\\s\\p{XDigit}{2}){4}) 1A FF 4C");
      Matcher m = p.matcher(line.trim());
      assert m.matches();
      for(int n = 0; n <= m.groupCount(); n ++)
         System.out.printf("group(%d): %s\n", n, m.group(n));
      System.out.printf("BDADDR: %s\n", m.group(1));
   }
   @Test
   public void testHcidump() throws Exception {
      CommandArgs cmdArgs = new CommandArgs();
      cmdArgs.rawDumpFile = "/tmp/hcidump.raw";
      HCIDumpParser parser = new HCIDumpParser(cmdArgs);
      parser.scanHCI();
   }
}
