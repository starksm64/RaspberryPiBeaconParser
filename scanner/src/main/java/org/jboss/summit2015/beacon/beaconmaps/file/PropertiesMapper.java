package org.jboss.summit2015.beacon.beaconmaps.file;

import org.jboss.summit2015.beacon.common.IBeaconMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

/**
 * Beacon mapping implementation based on a backing Properties file.
 */
public class PropertiesMapper implements IBeaconMapper {
    private Properties mapping;

    public PropertiesMapper(Properties mapping) {
        this.mapping = new Properties();
        // Copy the properties into the mapping
        for(String key : mapping.stringPropertyNames()) {
            this.mapping.setProperty(key, mapping.getProperty(key));
        }
    }

    /**
     * Load the id=user properties from a standard properties file as expected by {@link Properties#load(Reader)}
     * @param store - properties file
     * @throws IOException
     */
    public PropertiesMapper(File store) throws IOException {
        this.mapping = new Properties();
        FileReader reader = new FileReader(store);
        this.mapping.load(reader);
    }

    /**
     * Create the id=user mappings from a string of the format id1=user1,id2=user2,...
     * @param mappings
     */
    public PropertiesMapper(String mappings) {
        String[] pairs = mappings.split(",");
        for(String spair : pairs) {
            String[] pair = spair.split("=");
            this.mapping.setProperty(pair[0], pair[1]);
        }
    }

    @Override
    public String lookupUser(int minorID) {
        String user = this.mapping.getProperty(""+minorID);
        if(user == null)
            user = "Unknown";
        return user;
    }
}
