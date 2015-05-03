import org.jboss.summit2015.beacon.Beacon;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class AvgBeacon {
   Beacon beacon;
   int count;
   int rssi;
   long time;

   AvgBeacon(Beacon beacon) {
      this.beacon = beacon;
      this.rssi = beacon.getRssi();
      this.time = beacon.getTime();
      this.count = 1;
   }

   void update(Beacon beacon) {
      this.rssi += beacon.getRssi();
      this.time += beacon.getTime();
      count++;
   }

   Beacon getAvgBeacon() {
      beacon.setRssi(getAvgRssi());
      beacon.setTime(getAvgTime());
      return beacon;
   }

   int getAvgRssi() {
      return rssi / count;
   }

   long getAvgTime() {
      return time / count;
   }
}
