import gimbal.BeaconConfiguration;

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
   String activateBeacon(String factoryID, String beaconName, int configID);

   @DELETE
   @Path("/api/beacons/{factory_id}")
   @Produces("application/json")
   String deactivateBeacon(@PathParam("factory_id") String factoryID);

   @GET
   @Path("/api/beacons/{factory_id}")
   @Produces("application/json")
   String getBeacon(@PathParam("factory_id") String factoryID);

   @GET
   @Path("/api/beacons")
   @Produces("application/json")
   String getBeacons();

   @PUT
   @Path("/api/beacons/{factory_id}")
   @Produces("application/json")
   String updateBeacon(@PathParam("factory_id") String factoryID,
                         @FormParam("name")String name,
                         @FormParam("config_id") String configID);

   @PUT
   @Path("/api/beacons/{factory_id}")
   @Produces("application/json")
   String updateBeaconLocation(@PathParam("factory_id") String factoryID,
                         @FormParam("latitude") String latitude,
                         @FormParam("longitude") String longitude);

   @GET
   @Path("/fake")
   @Produces("application/json")
   public Integer getConfigIDForName(@PathParam("name") String name);
}
