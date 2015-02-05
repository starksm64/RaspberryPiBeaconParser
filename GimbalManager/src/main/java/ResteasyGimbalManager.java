import gimbal.BeaconConfiguration;
import org.jboss.resteasy.client.jaxrs.ProxyBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A factory for the IGimbalManager interface that uses the resteasy JAX-RS apis
 *
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class ResteasyGimbalManager {
   static IGimbalManager instance;

   /**
    * Create an IGimbalManager instance using the JAX-RS ClientBuilder.
    * @param token - the gimbal account API Key to pass in the AUTHORIZATION HTTP header.
    * @return the singleton IGimbalManager instance
    */
   synchronized public static IGimbalManager getInstance(String token) {
      if(instance != null)
         return instance;

      Client client = ClientBuilder.newClient();
      client.register(TrafficLogger.class);
      client.register(new AuthHeadersRequestFilter(token));
      WebTarget target = client.target("https://manager.gimbal.com");
      ResteasyWebTarget rtarget = (ResteasyWebTarget)target;

      ProxyBuilder<IGimbalManager> proxyBuilder = rtarget.proxyBuilder(IGimbalManager.class);
      IGimbalManager proxy = proxyBuilder.defaultConsumes(MediaType.APPLICATION_JSON_TYPE).build();
      instance = new GimbalManagerProxy(proxy, token);
      return instance;
   }

   /**
    * Gimbal token for the API key in the AUTHORIZATION HTTP header. This first checks the GIMBAL_MANAGER_TOKEN
    * system property, and if null, loads ${user.home}/.local/system.properties and looks for the
    * GIMBAL_MANAGER_TOKEN property there.
    *
    * @return the GIMBAL_MANAGER_TOKEN system property or property from ~/.local/system.properties
    * @throws IOException
    */
   public static String loadManagerTokenFromLocalProperties() throws IOException {
      String token = System.getProperty("GIMBAL_MANAGER_TOKEN");
      if(token == null) {
         // Read from ~/.local/system.properties
         String home = System.getProperty("user.home");
         String myProps = home + File.separator + ".local/system.properties";
         InputStream is = new FileInputStream(myProps);
         Properties props = new Properties();
         props.load(is);
         token = props.getProperty("GIMBAL_MANAGER_TOKEN");
         if(token == null)
            throw new IllegalStateException("Need GIMBAL_MANAGER_TOKEN system property set");
      }
      return token;
   }

   /**
    * Utility method that clones the iBeacon configuration given by the baseConfigName and sets the
    * major and minor values the arguments passed in. The beacon is then activated with the new
    * configuration using the given factoryID.
    *
    * @param baseConfigName - the configuration name of the base iBeacon configuration to clone
    * @param factoryID
    * @param beaconName -
    * @param major
    * @param minor
    */
   public static BeaconConfiguration configureAndActivateIBeacon(IGimbalManager manager, String baseConfigName,
         String factoryID, String beaconName, int major, int minor) {
      Integer id = manager.getConfigIDForName(baseConfigName);
      BeaconConfiguration config = manager.getBeaconConfiguration(id.toString());
      String name = baseConfigName + "-" + factoryID;
      String type = config.getType();
      String power = config.getTransmissionPower();
      String antenna = config.getAntennaType();
      String proximityUUID = config.getProximityUUID();
      String measuredPower = config.getMeasuredPower();
      BeaconConfiguration newConfig = manager.createIBeaconConfiguration(name, type, power, antenna, proximityUUID, major, minor, measuredPower);
      Integer newID = newConfig.getId();
      manager.activateBeacon(factoryID, beaconName, newID);
      return newConfig;
   }
}
