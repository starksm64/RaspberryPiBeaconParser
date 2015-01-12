import org.jboss.summit2015.beacon.HCIDumpParser;
import org.junit.Test;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TstSSHStreamParser {
   @Test
   public void testHcidumpStream() throws Exception {
      String[] args = {"-host", "/tmp/hcidump.raw", "stream"};
      HCIDumpParser parser = new HCIDumpParser();
      parser.run(args);
   }
}
