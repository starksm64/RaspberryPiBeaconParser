import java.io.IOException;
import java.util.Scanner;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class HexToAscii {
   public static void main(String[] args) throws IOException {
      byte[] buffer = new byte[128];
      StringBuilder ascii = new StringBuilder();
      Scanner s = new Scanner(System.in);
      int read = 0;
      do {
         ascii.setLength(0);
         System.out.print("Enter hex string: ");
     		String inputHex = s.nextLine();
         read = inputHex.length();
         // Break into octets
         String[] octets = inputHex.split(" ");
         for(String octet : octets) {
            int dec = Integer.parseInt(octet, 16);
            char c = (char) Character.toUpperCase(dec);
            ascii.append(c);
         }
         System.out.printf("Ascii string: %s\n", ascii);
      } while(read > 0);
   }
}
