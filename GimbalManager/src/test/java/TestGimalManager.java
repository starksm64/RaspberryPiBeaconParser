import gimbal.BeaconConfiguration;
import org.jboss.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
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

   @Test
   public void testListConfigurationsJSON() {
      List<String> configurations = manager.getBeaconConfigurationsJSON();
      for(String config : configurations) {
         System.out.println(config);
      }
   }
   @Test
   public void testListConfigurations() {
      List<BeaconConfiguration> configurations = manager.getBeaconConfigurations();
      for(BeaconConfiguration config : configurations) {
         System.out.println("### Configuration:");
         System.out.println(config);
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


   /*
   curl -H "Content-Type: application/json" \
   -H "Authorization: Token token=b11140b2b25805f2e34b6698679d6f2e" \
   -d '{"name":"RedHatSummitBaseBeaconConfig-32J8-E29SP","beacon_type":"iBeacon","transmission_power":-16,"antenna_type":"Recommended","proximity_uuid":"DAF246CE-8363-11E4-B116-123B93F75CBA","major":1,"minor":4}' \
   https://manager.gimbal.com/api/beacon_configurations
    */
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
      String factoryID = "C7ZE-29EN6";
      String beaconName = "Trial4";
      int major = 1;
      int minor = 1;
      BeaconConfiguration config = ResteasyGimbalManager.configureAndActivateIBeacon(manager, baseConfigName,
         factoryID, beaconName, major, minor);
      System.out.printf("testActivateBeacon: %s\n", config);
   }
}
