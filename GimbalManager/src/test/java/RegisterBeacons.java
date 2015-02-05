import gimbal.BeaconConfiguration;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class RegisterBeacons {
   static String[] beaconIDs = {
      "32J8-E29SP",
      "EUTK-4ZN23",
      "8TET-Y3Q31",
      "FEJX-MATF9",
      "GJZF-12KV6",
      "943E-ZBBT6",
      "32CR-6BZBH",
      "J9FB-1QRG6",
      "SJV4-SKC9G",
      "C7ZE-29EN6"
   };


   /**
    * Add -Djava.util.logging.manager=org.jboss.logmanager.LogManager to enable logging
    * @param args
    * @throws Exception
    */
   public static void main(String[] args) throws Exception {
      String token = ResteasyGimbalManager.loadManagerTokenFromLocalProperties();
      IGimbalManager manager = ResteasyGimbalManager.getInstance(token);
      String baseConfigName = "RedHatSummitBaseBeaconConfig";
      int major = 1;
      int minor = 4;
      for(String factoryID : beaconIDs) {
         String beaconName = "Trial" + minor;
         System.out.printf("Activating(%s,%s)\n", factoryID, beaconName);
         BeaconConfiguration config = ResteasyGimbalManager.configureAndActivateIBeacon(manager, baseConfigName, factoryID, beaconName, major, minor);
         System.out.printf("Activated: %s, config=%s\n", beaconName, config);
         minor ++;
      }
   }
}
