import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;
import org.jboss.summit2015.beacon.Beacon;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class ShowBeaconPath {
   public static void main(String[] args) throws Exception {
      System.out.printf("Parsing args: %s\n", Arrays.asList(args));
      String dataSet = "FourScannersRun1-2015-03-10.json.gz";
      // Get the dataSet input stream directly from git
      URL gitURL = new URL("file:/Users/starksm/Dev/IoT/BLE/RaspberryPiBeaconParser/data/" + dataSet);
      InputStream is = gitURL.openStream();
      GZIPInputStream gzip = new GZIPInputStream(is);
      InputStreamReader reader = new InputStreamReader(gzip);

      HashMap<Integer, LinkedList<Beacon>> eventByBeacon = new HashMap<>();
      long start = System.currentTimeMillis();
      Gson gson = new Gson();
      JsonStreamParser jsp = new JsonStreamParser(reader);
      int count = 0;
      while (jsp.hasNext()) {
         JsonElement jse = jsp.next();
         Beacon beacon = gson.fromJson(jse, Beacon.class);
         int tag = beacon.getMinor();
         if(tag <= 8) {
            LinkedList<Beacon> events = eventByBeacon.get(tag);
            if(events == null) {
               events = new LinkedList<>();
               eventByBeacon.put(tag, events);
               events.add(beacon);
               continue;
            }
            // See if the last event has the same scannerID
            Beacon lastEvent = events.peekLast();
            if(!lastEvent.getScannerID().equals(beacon.getScannerID()))
               events.add(beacon);
         }
         count++;
      }
      reader.close();

      /*
      for(int n = 1; n <= 8; n ++) {
         List<Beacon> events = eventByBeacon.get(n);
         if(events != null) {
            System.out.printf("Events for beacon(%d):\n", n);
            for (Beacon event : events) {
               System.out.printf("%s\n", event);
            }
         }
      }
      */
      List<Beacon> events = eventByBeacon.get(6);
      if(events != null) {
         System.out.printf("Events for beacon(%d):\n", 6);
         for (Beacon event : events) {
            System.out.printf("%s\n", event);
         }
      }
   }
}
