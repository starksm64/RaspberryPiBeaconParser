import org.jboss.summit2015.scanner.status.javafx.JSchUtils;
import org.jboss.summit2015.scanner.status.javafx.TextStream;
import org.junit.Test;

import java.io.File;

/**
 * Created by starksm on 6/18/15.
 */
public class TestJSchUtils {
    @Test
    public void testUtilsScpToRemote() throws Exception {
        File local = new File("/Users/starksm/Dev/IoT/BLE/RaspberryPiBeaconParser/ConfigManager/build/libs/ConfigManager-service.jar");
        TextStream tout = System.out::print;
        JSchUtils.scpToRemote("192.168.1.47", local, "/usr/local/bin/ConfigManager-service.jar", tout);
    }
}
