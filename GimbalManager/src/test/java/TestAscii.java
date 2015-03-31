import org.junit.Test;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TestAscii {

   @Test
   public void testDigits() {
      // 0-9
      int digitsHex[] = {0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39};
      for(int d : digitsHex) {
         System.out.printf("%x is '%c', ", d, Character.toUpperCase(d));
      }
   }
}
