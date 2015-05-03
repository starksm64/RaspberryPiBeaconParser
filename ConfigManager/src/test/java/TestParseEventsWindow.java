import org.junit.Test;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TestParseEventsWindow {
   @Test
   public void parseEventsWindow() {
      String window = "+15=8; +23=6; +27=2; +39=7; +43=7; +51=9; +57=5; +60=9; +67=7; +68=7; +70=5; +75=5; +81=6; +89=7; +105=7; +116=8; +117=10; +118=8; +123=5; +127=3; +128=5; +136=7; +141=9; +147=4; +151=7; +156=7; +158=4; +999=4;";
      String[] beacons = window.split("; ");
      for(String beacon : beacons) {
         String[] pair = beacon.split("=");
         System.out.printf("%s has count: %s\n", pair[0], pair[1]);
      }
   }
}
