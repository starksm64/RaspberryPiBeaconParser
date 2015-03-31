import gimbal.Beacon;
import gimbal.BeaconConfiguration;
import gimbal.BeaconConfigurationInfo;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.List;
import java.util.Properties;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public interface IGimbalManager {

// Beacon configurations
   /**
    * https://manager.gimbal.com/api/beacon_configurations
    */
   @GET
   @Path("/api/beacon_configurations")
   @Produces("application/json")
   List<String> getBeaconConfigurationsJSON();

   @GET
   @Path("/api/beacon_configurations")
   @Produces("application/json")
   List<BeaconConfiguration> getBeaconConfigurations();

   /**
    * https://manager.gimbal.com/api/beacon_configurations
    */
   @GET
   @Path("/api/beacon_configurations/{configuration_id}")
   @Produces("application/json")
   String getBeaconConfigurationJSON(@PathParam("configuration_id") String configID);

   @GET
   @Path("/api/beacon_configurations/{configuration_id}")
   @Produces("application/json")
   BeaconConfiguration getBeaconConfiguration(@PathParam("configuration_id") String configID);

   @POST
   @Path("/api/beacon_configurations")
   String createIBeaconConfigurationJSON(@FormParam("name") String name,
                                     @DefaultValue("iBeacon") @FormParam("beacon_type") String type,
                                     @DefaultValue("") @FormParam("transmission_power") String power,
                                     @DefaultValue("Recommended") @FormParam("antenna_type") String antenna,
                                     @DefaultValue("DAF246CE-8363-11E4-B116-123B93F75CBA") @FormParam("proximity_uuid") String proximityUUID,
                                     @FormParam("major") int major,
                                     @FormParam("minor") int minor,
                                     @DefaultValue("Recommended") @FormParam("measured_power") String measuredPower
                                     );
   @POST
   @Path("/api/beacon_configurations")
   BeaconConfiguration createIBeaconConfiguration(@FormParam("name") String name,
                                     @DefaultValue("iBeacon") @FormParam("beacon_type") String type,
                                     @DefaultValue("") @FormParam("transmission_power") String power,
                                     @DefaultValue("Recommended") @FormParam("antenna_type") String antenna,
                                     @DefaultValue("DAF246CE-8363-11E4-B116-123B93F75CBA") @FormParam("proximity_uuid") String proximityUUID,
                                     @FormParam("major") int major,
                                     @FormParam("minor") int minor,
                                     @DefaultValue("Recommended") @FormParam("measured_power") String measuredPower
                                     );


   /**
    * Update a configuration.
    * @param configID
    * @param name
    * @param type
    * @param proximityUUID
    * @param major
    * @param minor
    * @param additional ; measured_power(int), antenna_type(string), transmission_power(int)
    * @return the updated configuration object
    */
   @PUT
   @Path("/api/beacon_configurations/{configuration_id}")
   @Produces("application/json")
   BeaconConfiguration updateConfiguration(@PathParam("configuration_id") int configID,
                @FormParam("name") String name,
                @DefaultValue("iBeacon") @FormParam("beacon_type") String type,
                @DefaultValue("DAF246CE-8363-11E4-B116-123B93F75CBA") @FormParam("proximity_uuid") String proximityUUID,
                @FormParam("major") int major,
                @FormParam("minor") int minor,
                Properties additional
   );

   /**
    * Update an existing configuration, using the existing name, beacon_type, uuid, major and minor.
    * @param existingConfig
    * @param additional ; measured_power(int), antenna_type(string), transmission_power(int)
    * @return the updated configuration object
    */
   @PUT
   @Path("/api/beacon_configurations/{configuration_id}")
   @Produces("application/json")
   BeaconConfiguration updateConfiguration(BeaconConfiguration existingConfig, Properties additional);

   @DELETE
   @Path("/api/beacon_configurations/{configuration_id}")
   @Produces("application/json")
   void deleteConfiguration(@PathParam("configuration_id") int configID);

// Beacons
   @POST
   @Path("/api/beacons")
   @Produces("application/json")
   @Consumes("application/json")
   /**
    * @param factory_id - factory id of the beacon to activate
    * @param beaconName - name to assign beacon
    * @param config_id - the id of the configuration to use for the beacon
    *
   String activateBeacon(@FormParam("factory_id") String factoryID,
                         @FormParam("name")String beaconName,
    @FormParam("config_id") String configID);
    */
   Beacon activateBeacon(String factoryID, String beaconName, int configID);

   @DELETE
   @Path("/api/beacons/{factory_id}")
   @Produces("application/json")
   String deactivateBeacon(@PathParam("factory_id") String factoryID);

   /**
    * Get the tags for a beacon
    * @param factoryID
    * @return
    */
   @GET
   @Path("/api/beacons/{factory_id}/tags")
   String getBeaconTags(@PathParam("factory_id") String factoryID);

   /**
    * Add tags to a beacon
    * @param factoryID
    * @param tags - the tag names to add
    * @return the tags associated with the beacon
    */
   @POST
   @Path("/api/beacons/{factory_id}/tags")
   @Produces("application/json")
   String setBeaconTags(@PathParam("factory_id") String factoryID, @FormParam("tags") String[] tags);

   /**
    * Delete all tags for a beacon
    * @param factoryID
    * @return
    */
   @DELETE
   @Path("/api/beacons/{factory_id}/tags")
   @Produces("application/json")
   String deleteBeaconTags(@PathParam("factory_id") String factoryID);

   @GET
   @Path("/api/beacons/{factory_id}")
   @Produces("application/json")
   Beacon getBeacon(@PathParam("factory_id") String factoryID);

   @GET
   @Path("/api/beacons")
   @Produces("application/json")
   List<Beacon> getBeacons();

   @PUT
   @Path("/api/beacons/{factory_id}")
   @Produces("application/json")
   Beacon updateBeacon(@PathParam("factory_id") String factoryID,
                         @FormParam("name")String name,
                         @FormParam("config_id") int configID);

   @PUT
   @Path("/api/beacons/{factory_id}")
   @Produces("application/json")
   String updateBeaconLocation(@PathParam("factory_id") String factoryID,
                         @FormParam("latitude") String latitude,
                         @FormParam("longitude") String longitude);


   @GET
   @Path("/api/beacons/{factory_id}/configuration")
   @Produces("application/json")
   public BeaconConfigurationInfo getBeaconConfigByFactoryID(@PathParam("factory_id") String factory_id);

   @GET
   @Path("/fake")
   @Produces("application/json")
   public Integer getConfigIDForName(@PathParam("name") String name);
}
