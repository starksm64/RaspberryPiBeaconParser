import gimbal.Beacon;
import gimbal.BeaconConfiguration;
import gimbal.BeaconConfigurationInfo;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class GimbalManagerProxy implements IGimbalManager {
   private IGimbalManager instance;
   private String token;
   /** A mapping from the configuration name to its internal id */
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

   @PUT
   @Path("/api/beacon_configurations/{configuration_id}")
   @Produces("application/json")
   @Override
   public BeaconConfiguration updateConfiguration(int configID, @FormParam("name") String name, @DefaultValue("iBeacon") @FormParam("beacon_type") String type, @DefaultValue("DAF246CE-8363-11E4-B116-123B93F75CBA") @FormParam("proximity_uuid") String proximityUUID,
                                   @FormParam("major") int major, @FormParam("minor") int minor,
                                   Properties additional) {
      Client client = ClientBuilder.newClient();
      client.register(TrafficLogger.class);
      client.register(new AuthHeadersRequestFilter(token));
      String url = String.format("https://manager.gimbal.com/api/beacon_configurations/%d", configID);
      WebTarget target = client.target(url);
      ResteasyWebTarget rtarget = (ResteasyWebTarget)target;

      StringWriter sw = new StringWriter();
      JsonGenerator json = Json.createGenerator(sw);
      json.writeStartObject();
      json.write("name", name);
      json.write("beacon_type", type);
      json.write("proximity_uuid", proximityUUID);
      json.write("major", major);
      json.write("minor", minor);
      for(String key : additional.stringPropertyNames()) {
         json.write(key, additional.getProperty(key));
      }
      json.writeEnd();
      json.flush();
      Entity<String> jsonEntity = Entity.json(sw.toString());
      BeaconConfiguration response = rtarget.request(MediaType.APPLICATION_JSON_TYPE).post(jsonEntity, BeaconConfiguration.class);
      return response;
   }

   @PUT
   @Path("/api/beacon_configurations/{configuration_id}")
   @Produces("application/json")
   public BeaconConfiguration updateConfiguration(BeaconConfiguration existingConfig, Properties additional) {
      Client client = ClientBuilder.newClient();
      client.register(TrafficLogger.class);
      client.register(new AuthHeadersRequestFilter(token));
      String url = String.format("https://manager.gimbal.com/api/beacon_configurations/%d", existingConfig.getId());
      WebTarget target = client.target(url);
      ResteasyWebTarget rtarget = (ResteasyWebTarget)target;

      StringWriter sw = new StringWriter();
      JsonGenerator json = Json.createGenerator(sw);
      json.writeStartObject();
      json.write("name", existingConfig.getName());
      json.write("beacon_type", existingConfig.getType());
      json.write("proximity_uuid", existingConfig.getProximityUUID());
      json.write("major", existingConfig.getMajor());
      json.write("minor", existingConfig.getMinor());
      if(additional.containsKey("measured_power"))
         json.write("measured_power", Integer.valueOf(additional.getProperty("measured_power")));
      if(additional.containsKey("transmission_power"))
         json.write("transmission_power", Integer.valueOf(additional.getProperty("transmission_power")));
      if(additional.containsKey("antenna_type"))
         json.write("antenna_type", additional.getProperty("antenna_type"));
      json.writeEnd();
      json.flush();
      Entity<String> jsonEntity = Entity.json(sw.toString());
      System.out.printf("updateConfiguration(%s)\n", sw.toString());
      BeaconConfiguration response = rtarget.request(MediaType.APPLICATION_JSON_TYPE).put(jsonEntity, BeaconConfiguration.class);
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
   public Beacon activateBeacon(String factoryID, @FormParam("name") String name, @FormParam("config_id") int configID) {
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
      Beacon response = rtarget.request(MediaType.APPLICATION_JSON_TYPE).post(jsonEntity, Beacon.class);
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
   public Beacon getBeacon(String factoryID) {
      return instance.getBeacon(factoryID);
   }
   @Override
   @GET
   @Path("/api/beacons/{factory_id}")
   @Produces("application/json")
   public String getBeaconJSON(String factoryID) {
      return instance.getBeaconJSON(factoryID);
   }

   @Override
   @GET
   @Path("/api/beacons")
   @Produces("application/json")
   public List<Beacon> getBeacons() {
      return instance.getBeacons();
   }

   @Override
   @PUT
   @Path("/api/beacons/{factory_id}")
   @Produces("application/json")
   public Beacon updateBeacon(String factoryID, @FormParam("name") String name, @FormParam("config_id") int configID) {
      Client client = ClientBuilder.newClient();
      client.register(TrafficLogger.class);
      client.register(new AuthHeadersRequestFilter(token));
      String url = String.format("https://manager.gimbal.com/api/beacons/%s", factoryID);
      WebTarget target = client.target(url);
      ResteasyWebTarget rtarget = (ResteasyWebTarget)target;

      StringWriter sw = new StringWriter();
      JsonGenerator json = Json.createGenerator(sw);
      json.writeStartObject();
      json.write("name", name);
      json.write("config_id", configID);
      json.writeEnd();
      json.flush();
      Entity<String> jsonEntity = Entity.json(sw.toString());
      System.out.printf("updateConfiguration(%s)\n", sw.toString());
      Beacon response = rtarget.request(MediaType.APPLICATION_JSON_TYPE).put(jsonEntity, Beacon.class);
      return response;
   }

   @GET
   @Path("/api/beacons/{factory_id}/tags")
   @Override
   public String getBeaconTags(String factoryID) {
      return instance.getBeaconTags(factoryID);
   }

   @POST
   @Path("/api/beacons/{factory_id}/tags")
   @Override
   public String setBeaconTags(@PathParam("factory_id") String factoryID, @FormParam("tags") String[] tags) {
      Client client = ClientBuilder.newClient();
      client.register(TrafficLogger.class);
      client.register(new AuthHeadersRequestFilter(token));
      String url = String.format("https://manager.gimbal.com/api/beacons/%s/tags", factoryID);
      WebTarget target = client.target(url);
      ResteasyWebTarget rtarget = (ResteasyWebTarget)target;

      JsonArrayBuilder tagsBuilder = Json.createArrayBuilder();
      for(String tag : tags)
         tagsBuilder.add(tag);
      JsonObjectBuilder tagsObj = Json.createObjectBuilder();
      tagsObj.add("tags", tagsBuilder.build());

      StringWriter sw = new StringWriter();
      JsonWriter json = Json.createWriter(sw);
      json.writeObject(tagsObj.build());
      json.close();
      Entity<String> jsonEntity = Entity.json(sw.toString());
      String response = rtarget.request(MediaType.APPLICATION_JSON_TYPE).post(jsonEntity, String.class);
      return response;
   }

   @DELETE
   @Path("/api/beacons/{factory_id}/tags")
   @Produces("application/json")
   public String deleteBeaconTags(@PathParam("factory_id") String factoryID) {
      return instance.deleteBeaconTags(factoryID);
   }

   @Override
   @PUT
   @Path("/api/beacons/{factory_id}")
   @Produces("application/json")
   public String updateBeaconLocation(String factoryID, @FormParam("latitude") String latitude, @FormParam("longitude") String longitude) {
      return instance.updateBeaconLocation(factoryID, latitude, longitude);
   }

   @GET
   @Path("/api/beacons/{factory_id}/configuration")
   @Produces("application/json")
   public BeaconConfigurationInfo getBeaconConfigByFactoryID(@PathParam("factory_id") String factoryID) {
      return instance.getBeaconConfigByFactoryID(factoryID);
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

   @Override
   public int findAvailableMinorIDInRange(int begin, int end) {
      HashMap<Integer, BeaconConfiguration> configsByMinor = new HashMap<>();
      List<BeaconConfiguration> configurations = getBeaconConfigurations();
      for (BeaconConfiguration config : configurations) {
         int minor = config.getMinor();
         configsByMinor.put(minor, config);
      }
      int available = -1;
      for(int minor = begin; minor < end; minor ++) {
         if(!configsByMinor.containsKey(minor)) {
            available = minor;
            break;
         }

      }
      return available;
   }
}
