import gimbal.BeaconConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class RegisterBeacons {
   static String[][] beaconIDs = {
      /*
      "4", "32J8-E29SP",
      "5", "EUTK-4ZN23",
      "6", "8TET-Y3Q31",
      "7", "FEJX-MATF9",
      "8", "GJZF-12KV6",
      "9", "943E-ZBBT6",
      "10", "32CR-6BZBH",
      "11", "J9FB-1QRG6",
      "12", "SJV4-SKC9G",
      "13", "C7ZE-29EN6"
      */

   };

   static String[][] loadBeaconIDs() throws IOException {
      // Look for a beaconids.txt file
      File beacons = new File("beaconids.txt");
      ArrayList<String[]> ids = new ArrayList<>();
      Reader reader = null;
      if(beacons.exists()) {
         reader = new FileReader(beacons);
      } else {
         // Try to find the ids as a resource
         URL resURL = RegisterBeacons.class.getResource("beaconids.txt");
         if(resURL == null)
            throw new FileNotFoundException(beacons.getAbsolutePath());
         reader = new InputStreamReader(resURL.openStream());
      }

      BufferedReader br = new BufferedReader(reader);
      String line = br.readLine();
      while(line != null) {
         if(line.startsWith("#")) {
            line = br.readLine();
            continue;
         }
         String[] id = line.split(", ");
         ids.add(id);
         line = br.readLine();
      }
      System.out.printf("loaded ids to register: %s\n", ids);
      for(String[] info: ids) {
         System.out.printf("{%s,%s}\n", info[0], info[1]);
      }
      String[][] loadedIDs = new String[ids.size()][2];
      ids.toArray(loadedIDs);
      return loadedIDs;
   }

   /**
    * Add -Djava.util.logging.manager=org.jboss.logmanager.LogManager to enable logging
    * @param args
    * @throws Exception
    */
   public static void main(String[] args) throws Exception {
      String token = ResteasyGimbalManager.loadManagerTokenFromLocalProperties();
      IGimbalManager manager = ResteasyGimbalManager.getInstance(token);
      String baseConfigName = "RedHatSummitBaseBeaconConfig";
      int major = 0;
      String[][] beaconIDs = loadBeaconIDs();
      for(String[] info : beaconIDs) {
         int minor = Integer.decode(info[0]);
         String beaconName = "Beacon" + minor;
         String factoryID = info[1];
         System.out.printf("Activating(%s,%s)\n", factoryID, beaconName);
         BeaconConfiguration config = ResteasyGimbalManager.configureAndActivateIBeacon(manager, baseConfigName, factoryID, beaconName, major, minor);
         System.out.printf("Activated: %s, config=%s\n", beaconName, config);
         minor ++;
      }
   }
}
