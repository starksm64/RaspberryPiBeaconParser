import gimbal.BeaconConfiguration;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class GimbalManagerProxy implements IGimbalManager {
   private IGimbalManager instance;
   private String token;
   private HashMap<String, Integer> nameToIDMap = new HashMap<>();

   public GimbalManagerProxy(IGimbalManager proxy, String token) {
      this.instance = proxy;
      this.token = token;
   }

   @Override
   @GET
   @Path("/api/beacon_configurations")
   @Produces("application/json")
   public List<BeaconConfiguration> getBeaconConfigurations() {
      return instance.getBeaconConfigurations();
   }

   @Override
   public List<String> getBeaconConfigurationsJSON() {
      return instance.getBeaconConfigurationsJSON();
   }

   @Override
   @GET
   @Path("/api/beacon_configurations/{configuration_id}")
   @Produces("application/json")
   public String getBeaconConfigurationJSON(String configID) {
      return instance.getBeaconConfigurationJSON(configID);
   }

   @Override
   @GET
   @Path("/api/beacon_configurations/{configuration_id}")
   @Produces("application/json")
   public BeaconConfiguration getBeaconConfiguration(String configID) {
      return instance.getBeaconConfiguration(configID);
   }

   @Override
   @POST
   @Path("/api/beacon_configurations")
   public String createIBeaconConfigurationJSON(@FormParam("name") String name, @DefaultValue("iBeacon") @FormParam("beacon_type") String type, @DefaultValue("") @FormParam("transmission_power") String power, @DefaultValue("Recommended") @FormParam("antenna_type") String antenna, @DefaultValue("DAF246CE-8363-11E4-B116-123B93F75CBA") @FormParam("proximity_uuid") String proximityUUID, @FormParam("major") int major, @FormParam("minor") int minor, @DefaultValue("Recommended") @FormParam("measured_power") String measuredPower) {
      return instance.createIBeaconConfigurationJSON(name, type, power, antenna, proximityUUID, major, minor, measuredPower);
   }

   @Override
   @POST
   @Path("/api/beacon_configurations")
   public BeaconConfiguration createIBeaconConfiguration(@FormParam("name") String name, @DefaultValue("iBeacon") @FormParam("beacon_type") String type,
      @DefaultValue("") @FormParam("transmission_power") String power,
      @DefaultValue("Recommended") @FormParam("antenna_type") String antenna,
      @DefaultValue("DAF246CE-8363-11E4-B116-123B93F75CBA") @FormParam("proximity_uuid") String proximityUUID,
      @FormParam("major") int major, @FormParam("minor") int minor,
      @DefaultValue("Recommended") @FormParam("measured_power") String measuredPower) {
      Client client = ClientBuilder.newClient();
      client.register(TrafficLogger.class);
      client.register(new AuthHeadersRequestFilter(token));
      WebTarget target = client.target("https://manager.gimbal.com/api/beacon_configurations");
      ResteasyWebTarget rtarget = (ResteasyWebTarget)target;

      StringWriter sw = new StringWriter();
      JsonGenerator json = Json.createGenerator(sw);
      json.writeStartObject();
      json.write("name", name);
      json.write("beacon_type", type);
      int ipower = Integer.valueOf(power);
      json.write("transmission_power", ipower);
      json.write("antenna_type", antenna);
      json.write("proximity_uuid", proximityUUID);
      json.write("major", major);
      json.write("minor", minor);
      //json.write("measured_power", measuredPower);
      json.writeEnd();
      json.flush();
      Entity<String> jsonEntity = Entity.json(sw.toString());
      BeaconConfiguration response = rtarget.request(MediaType.APPLICATION_JSON_TYPE).post(jsonEntity, BeaconConfiguration.class);
      return response;
   }

   @Override
   public void deleteConfiguration(int configID) {
      instance.deleteConfiguration(configID);
   }

   @Override
   @POST
   @Path("/api/beacons")
   @Produces("application/json")
   public String activateBeacon(String factoryID, @FormParam("name") String name, @FormParam("config_id") int configID) {
      Client client = ClientBuilder.newClient();
      client.register(TrafficLogger.class);
      client.register(new AuthHeadersRequestFilter(token));
      WebTarget target = client.target("https://manager.gimbal.com/api/beacons");
      ResteasyWebTarget rtarget = (ResteasyWebTarget)target;

      StringWriter sw = new StringWriter();
      JsonGenerator json = Json.createGenerator(sw);
      json.writeStartObject();
      json.write("factory_id", factoryID);
      json.write("name", name);
      json.write("config_id", configID);
      json.writeEnd();
      json.flush();
      Entity<String> jsonEntity = Entity.json(sw.toString());
      String response = rtarget.request(MediaType.APPLICATION_JSON_TYPE).post(jsonEntity, String.class);
      return response;
   }

   @Override
   @DELETE
   @Path("/api/beacons/{factory_id}")
   @Produces("application/json")
   public String deactivateBeacon(String factoryID) {
      return instance.deactivateBeacon(factoryID);
   }

   @Override
   @GET
   @Path("/api/beacons/{factory_id}")
   @Produces("application/json")
   public String getBeacon(String factoryID) {
      return instance.getBeacon(factoryID);
   }

   @Override
   @GET
   @Path("/api/beacons")
   @Produces("application/json")
   public String getBeacons() {
      return instance.getBeacons();
   }

   @Override
   @PUT
   @Path("/api/beacons/{factory_id}")
   @Produces("application/json")
   public String updateBeacon(String factoryID, @FormParam("name") String name, @FormParam("config_id") String configID) {
      return instance.updateBeacon(factoryID, name, configID);
   }

   @Override
   @PUT
   @Path("/api/beacons/{factory_id}")
   @Produces("application/json")
   public String updateBeaconLocation(String factoryID, @FormParam("latitude") String latitude, @FormParam("longitude") String longitude) {
      return instance.updateBeaconLocation(factoryID, latitude, longitude);
   }

   @Override
   @GET
   @Path("/fake")
   @Produces("application/json")
   public Integer getConfigIDForName(String name) {
      if(nameToIDMap.isEmpty()) {
         List<BeaconConfiguration> configurations = instance.getBeaconConfigurations();
         for(BeaconConfiguration config : configurations) {
            nameToIDMap.put(config.getName(), config.getId());
         }
      }
      Integer id = nameToIDMap.get(name);
      return id;
   }
}
