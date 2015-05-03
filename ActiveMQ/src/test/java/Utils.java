import org.jboss.summit2015.beacon.Beacon;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class Utils {

   static int getIntProperty(Message msg, String name) throws JMSException {
      Number number = (Number) msg.getObjectProperty(name);
      return number.intValue();
   }
   public static Beacon extractBeaconLax(Message msg) throws Exception {
      Beacon beacon = new Beacon();
      beacon.setScannerID(msg.getStringProperty("scannerID"));
      beacon.setUUID(msg.getStringProperty("uuid"));
      beacon.setCode(getIntProperty(msg, "code"));
      beacon.setManufacturer(getIntProperty(msg, "manufacturer"));
      beacon.setMajor(getIntProperty(msg, "major"));
      beacon.setMinor(getIntProperty(msg, "minor"));
      beacon.setPower(getIntProperty(msg, "power"));
      beacon.setCalibratedPower(getIntProperty(msg, "calibratedPower"));
      beacon.setRssi(getIntProperty(msg, "rssi"));
      beacon.setTime(msg.getLongProperty("time"));
      beacon.setMessageType(getIntProperty(msg, "messageType"));
      return beacon;
   }
   public static Beacon extractBeacon(Message msg) throws Exception {
      Beacon beacon = new Beacon();
      beacon.setScannerID(msg.getStringProperty("scannerID"));
      beacon.setUUID(msg.getStringProperty("uuid"));
      beacon.setCode(msg.getIntProperty("code"));
      beacon.setManufacturer(msg.getIntProperty("manufacturer"));
      beacon.setMajor(msg.getIntProperty("major"));
      beacon.setMinor(msg.getIntProperty("minor"));
      beacon.setPower(msg.getIntProperty("power"));
      beacon.setCalibratedPower(msg.getIntProperty("calibratedPower"));
      beacon.setRssi(msg.getIntProperty("rssi"));
      beacon.setTime(msg.getLongProperty("time"));
      beacon.setMessageType(msg.getIntProperty("messageType"));
      return beacon;
   }
   public static void populateMessage(Message message, Beacon beacon) throws JMSException {
      message.setStringProperty("uuid", beacon.getUUID());
      message.setStringProperty("scannerID", beacon.getScannerID());
      message.setIntProperty("major", beacon.getMajor());
      message.setIntProperty("minor", beacon.getMinor());
      message.setIntProperty("manufacturer", beacon.getManufacturer());
      message.setIntProperty("code", beacon.getCode());
      message.setIntProperty("power", beacon.getCalibratedPower());
      message.setIntProperty("calibratedPower", beacon.getCalibratedPower());
      message.setIntProperty("rssi", beacon.getRssi());
      message.setLongProperty("time", beacon.getTime());
      message.setIntProperty("messageType", beacon.getMessageType());
   }
   public static double estimateDistance(int calibratedPower, double rssi) {
     if (rssi == 0) {
       return -1.0; // if we cannot determine accuracy, return -1.
     }

     double ratio = rssi*1.0/calibratedPower;
     if (ratio < 1.0) {
       return Math.pow(ratio,10);
     }
     else {
       double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
       return accuracy;
     }
   }
}
