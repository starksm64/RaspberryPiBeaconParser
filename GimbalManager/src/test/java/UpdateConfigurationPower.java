import gimbal.BeaconConfiguration;

import java.util.List;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class UpdateConfigurationPower {
   public static void main(String[] args) throws Exception {
      String token = ResteasyGimbalManager.loadManagerTokenFromLocalProperties();
      IGimbalManager manager = ResteasyGimbalManager.getInstance(token);
      List<BeaconConfiguration> configs = manager.getBeaconConfigurations();

      String factoryID = "68TM-Q4QCR";
      BeaconConfiguration bc20 = null;
      Properties properties = new Properties();
      properties.setProperty("transmission_power", "-2");
      BeaconConfiguration minPower = null;
      for(BeaconConfiguration bc : configs) {
         if(bc.getName().startsWith("RedHatSummitBaseBeaconConfig-")) {
            System.out.printf("%s[%d](%d,%d), power=%d\n", bc.getName(), bc.getId(), bc.getMajor(), bc.getMinor(), bc.getTransmissionPowerAsInt());
         }
         if(bc.getName().endsWith("RedHatSummit Beacon Min Power"))
            minPower = bc;
         if(bc.getName().endsWith(factoryID)) {
            bc20 = bc;
         }
         if(bc.getTransmissionPowerAsInt() < -2) {
            try {
               manager.updateConfiguration(bc, properties);
               System.out.printf("...Updated power for: %s[%d]", bc.getName(), bc.getId());
            } catch (Exception e) {
               System.out.printf("...Failed to update power for: %s[%d]", bc.getName(), bc.getId());
            }
         }
      }

   }
}
