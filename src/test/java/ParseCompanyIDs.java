import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class ParseCompanyIDs {
   /**
    * https://www.bluetooth.org/en-us/specification/assigned-numbers/company-identifiers
    * @throws Exception
    */
   @Test
   public void parseIDs() throws Exception {
      FileInputStream fis = new FileInputStream("/private/tmp/ids.txt");
      BufferedReader br = new BufferedReader(new InputStreamReader(fis));
      String line = br.readLine();
      while (line != null) {
         String[] fields = line.split("\t");
         if(fields.length != 3)
            break;
         String name = fields[2].replace(' ', '_');
         name = name.replace(".", "");
         System.out.printf("\t%s(%s),\n", name, fields[1]);
         line = br.readLine();
      }
   }
}
