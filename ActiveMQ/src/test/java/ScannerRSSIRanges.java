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
 * Parse the beacon event json data into scanner:beacon readings of rssi values
 *
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class ScannerRSSIRanges {
   public static void main(String[] args) throws Exception {
      System.out.printf("Parsing args: %s\n", Arrays.asList(args));
      String dataSet = "FourScannersRun1-2015-03-10.json.gz";
      // Get the dataSet input stream directly from git
      URL gitURL = new URL("file:/Users/starksm/Dev/IoT/BLE/RaspberryPiBeaconParser/data/" + dataSet);
      InputStream is = gitURL.openStream();
      GZIPInputStream gzip = new GZIPInputStream(is);
      InputStreamReader reader = new InputStreamReader(gzip);

      HashMap<String, LinkedList<Integer>> eventByBeacon = new HashMap<>();
      long start = System.currentTimeMillis();
      Gson gson = new Gson();
      JsonStreamParser jsp = new JsonStreamParser(reader);
      int count = 0;
      while (jsp.hasNext()) {
         JsonElement jse = jsp.next();
         Beacon beacon = gson.fromJson(jse, Beacon.class);
         int tag = beacon.getMinor();
         String key = beacon.getScannerID() + ':' + tag;
         if(tag <= 8) {
            LinkedList<Integer> readings = eventByBeacon.get(key);
            if(readings == null) {
               readings = new LinkedList<>();
               eventByBeacon.put(key, readings);
            }
            readings.add(beacon.getRssi());
         }
         count++;
      }
      reader.close();

      for(int r = 201; r <= 204; r ++) {
         String room = "Room"+r;
         for(int b : new int[]{1,2,5,6,7,8}) {
            String key = room + ':' + b;
            List<Integer> readings = eventByBeacon.get(key);
            analyze(key, readings);
         }
      }
   }

   static void analyze(String key, List<Integer> readings) {
      int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, avg = 0;
      for(Integer reading : readings) {
         min = Math.min(min, reading);
         max = Math.max(max, reading);
         avg += reading;
      }
      avg /= readings.size();
      System.out.printf("%s: min=%d, max=%d, avg=%d\n", key, min, max, avg);
   }
}
