import gimbal.Beacon;
import gimbal.BeaconConfiguration;
import gimbal.BeaconConfigurationInfo;
import org.jboss.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Add -Djava.util.logging.manager=org.jboss.logmanager.LogManager to enable more verbose logging
 *
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class TestGimalManager {
   private static Logger log = Logger.getLogger(TestGimalManager.class);
   private static IGimbalManager manager;

   @BeforeClass
   public static void classInit() throws IOException {
      String token = ResteasyGimbalManager.loadManagerTokenFromLocalProperties();
      manager = ResteasyGimbalManager.getInstance(token);
   }

   public void testDecodeWireOutput() {
      String wire = "[0x1f][0x8b][0x8][0x0][0x0][0x0][0x0][0x0][0x0][0x3][0x8b][0xcc]/U[0xc8]--.Q(N-Q[0xa8][0xcc]/-RPw[0xce][0xcf]+I[0xcd]+[0xd1][\\r][0xa9],HUW[0xc8]HMLI-R([0xc9]WPO,([0xc8][0xc9]LN,[0xc9][0xcc][0xcf][0xd3][0xcf]*[0xce][0xcf]S[0x7][0x0]n[0xc1]Y[0xdb]=[0x0][0x0][0x0]";
   }

   @Test
   public void testGetBeacon() {
      Beacon beacon = manager.getBeacon("VMU8-C5J5W");
      System.out.println(beacon);
   }
   @Test
   public void testGetBeaconJSON() {
      String beacon = manager.getBeaconJSON("VMU8-C5J5W");
      System.out.println(beacon);
   }
   @Test
   public void testGetBeaconConfig() {
      BeaconConfigurationInfo config = manager.getBeaconConfigByFactoryID("VMU8-C5J5W");
      System.out.println(config);
   }

   @Test
   public void testGetBeacons() {
      List<Beacon> beacons = manager.getBeacons();
      System.out.printf("Received %d beacons\n", beacons.size());
      for (Beacon beacon : beacons) {
         System.out.println(beacon);
      }
   }

   @Test
   public void testGetEmptyBeacons() {
      List<Beacon> beacons = manager.getBeacons();
      System.out.printf("Received %d beacons\n", beacons.size());
      ArrayList<Integer> empty = new ArrayList<>();
      for (Beacon beacon : beacons) {
         String level = beacon.getBatteryLevel();
         if(level.startsWith("Empty") || level.startsWith("Low")) {
            System.out.printf("%s\n", beacon);
            empty.add(beacon.getIdFromName());
         }
      }
      System.out.printf("Low or empty beacons ids(%d): %s\n", empty.size(), empty);
   }

   @Test
   public void testGetBeaconConfigByFactoryID() {
      String factoryID = "68TM-Q4QCR";
      BeaconConfigurationInfo configInfo = manager.getBeaconConfigByFactoryID(factoryID);
      System.out.printf("configInfo(%s): %s\n", factoryID, configInfo);
   }

   @Test
   public void testUpdateBeaconConfig() {
      String factoryID = "68TM-Q4QCR";
      Integer configID = manager.getConfigIDForName("RedHatSummitBaseBeaconConfig-68TM-Q4QCR");
      manager.updateBeacon(factoryID, "Trial20", configID);
   }

   @Test
   public void testListConfigurationsJSON() {
      List<String> configurations = manager.getBeaconConfigurationsJSON();
      for (String config : configurations) {
         System.out.println(config);
      }
   }

   @Test
   public void testListConfigurations() {
      List<BeaconConfiguration> configurations = manager.getBeaconConfigurations();
      for (BeaconConfiguration config : configurations) {
         System.out.println("### Configuration:");
         System.out.println(config);
      }
   }

   @Test
   public void testListDuplicateMinorIDs() {
      HashMap<Integer, BeaconConfiguration> configsByMinor = new HashMap<>();
      List<BeaconConfiguration> configurations = manager.getBeaconConfigurations();
      System.out.printf("Checking %d configurations...\n", configurations.size());
      int maxMinorID = -1;
      for (BeaconConfiguration config : configurations) {
         int minor = config.getMinor();
         maxMinorID = Math.max(maxMinorID, minor);
         if(configsByMinor.containsKey(minor)) {
            System.out.printf("Duplicate(%d), %s, %s\n", minor, config.getName(), configsByMinor.get(minor).getName());
         } else {
            configsByMinor.put(minor, config);
         }
      }
      System.out.printf("Done\n");
      for(int minor = 0; minor < 200; minor ++) {
         if(!configsByMinor.containsKey(minor))
            System.out.printf("%d is available\n", minor);
      }
   }

   @Test
   public void testGetConfigurationJSON() {
      String config = manager.getBeaconConfigurationJSON("18334");
      System.out.printf("config(id=18334): %s\n", config);
   }

   @Test
   public void testGetConfiguration() {
      BeaconConfiguration config = manager.getBeaconConfiguration("18334");
      System.out.printf("config(id=18334): %s\n", config);
   }

   @Test
   public void testGetConfigurationByName() {
      Integer id = manager.getConfigIDForName("RedHatSummitBaseBeaconConfig-943E-Z88T6");
      BeaconConfiguration config = manager.getBeaconConfiguration(id.toString());
      System.out.printf("%s\n", config);

   }

   @Test
   public void testDeleteConfigurationByName() {
      Integer id = manager.getConfigIDForName("RedHatSummitBaseBeaconConfig-32J8-E29SP");
      BeaconConfiguration config = manager.getBeaconConfiguration(id.toString());
      System.out.printf("config(id=18334): %s\n", config);
      manager.deleteConfiguration(config.getId());
   }


   @Test
   public void testCreateIBeaconConfig() {

   }

   @Test
   public void testActivateBeaconWithConfig() {
      log.trace("testActivateBeaconWithConfig");
      String factoryID = "C7ZE-29EN6";
      String beaconName = "Trial4";
      manager.activateBeacon(factoryID, beaconName, 21770);
   }

   @Test
   public void testActivateBeacon() {
      String baseConfigName = "RedHatSummitBaseBeaconConfig";
      String factoryID = "KYPX-NCZNK";
      int major = 0;
      int minor = 63;
      String beaconName = "Trial"+minor;
      BeaconConfiguration config = ResteasyGimbalManager.configureAndActivateIBeacon(manager, baseConfigName,
         factoryID, beaconName, major, minor);
      System.out.printf("testActivateBeacon: %s\n", config);
   }

   @Test
   public void testActivateBeacons() {
      String[] ids = {
         "QGX7-HJE9B",
         "RGWJ-N7QFW",
         "H8CQ-4FZ6Y",
         "T8Q6-9148V",
         "MW7X-PPW81",
         "TX6Y-EJ232",
         "SB8N-5WP2B",
         "QPZP-FCYQ9",
         "P8NP-A2F69",
         "P28Y-WHVZA",
         "6KM8-1X8Z9"
      };
      int minor = 33;
      String baseConfigName = "RedHatSummitBaseBeaconConfig";
      for (String factoryID : ids) {
         String beaconName = "Trial"+minor;
         String configName = baseConfigName + "-" + factoryID;
         int configID = manager.getConfigIDForName(configName);
         System.out.printf("Activating: %s\n", factoryID);
         Beacon beacon = manager.activateBeacon(factoryID, beaconName, configID);
         System.out.printf("testActivateBeacon: %s\n", beacon);
         minor ++;
      }
   }

   @Test
   public void testDeactiveBeacon() {
      String[] ids = {"QGX7-HJE9B",
         "RGWJ-N7QFW",
         "H8CQ-4FZ6Y",
         "T8Q6-9148V", "MW7X-PPW81",
         "TX6Y-EJ232",
         "SB8N-5WP2B",
         "QPZP-FCYQ9",
         "P8NP-A2F69",
         "P28Y-WHVZA",
         "6KM8-1X8Z9"};
      for (String factoryID : ids) {
         manager.deactivateBeacon(factoryID);
      }
   }

   @Test
   public void testGetBeaconTags() {
      // Trial3 beacon
      String factoryID = "R4MQ-7RM41";
      String tags = manager.getBeaconTags(factoryID);
      System.out.printf("testGetBeaconTags(%s): %s\n", factoryID, tags);
   }

   @Test
   public void testWriteTagsToJson() {
      JsonArrayBuilder tagsBuilder = Json.createArrayBuilder();
      tagsBuilder.add("tag1");
      tagsBuilder.add("tag2");
      JsonObjectBuilder tags = Json.createObjectBuilder();
      tags.add("tags", tagsBuilder.build());

      StringWriter sw = new StringWriter();
      JsonWriter json = Json.createWriter(sw);
      json.writeObject(tags.build());
      json.close();
      System.out.printf("testWriteTagsToJson: %s\n", sw.toString());
   }

   /*
   curl -X POST -H "Content-Type: application/json" -H "Authorization: Token token=*" -d '{"tags":["Room201", "Trial3"]}' --trace-ascii - https://manager.gimbal.com/api/beacons/R4MQ-7RM41/tags
    */
   @Test
   public void testSetBeaconTags() {
      // Trial3 beacon
      String factoryID = "R4MQ-7RM41";
      String[] tags = {"Room201", "Trial3"};
      String result = manager.setBeaconTags(factoryID, tags);
      System.out.printf("testAddBeaconTags(%s): %s\n", factoryID, result);
   }

   @Test
   public void testDeleteBeaconTags() {
      // Trial3 beacon
      String factoryID = "R4MQ-7RM41";
      String result = manager.deleteBeaconTags(factoryID);
      System.out.printf("testDeleteBeaconTags(%s): %s\n", factoryID, result);
   }
}
