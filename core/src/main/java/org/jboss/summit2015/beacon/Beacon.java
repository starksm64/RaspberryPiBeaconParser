package org.jboss.summit2015.beacon;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class Beacon implements Serializable {
   /** version prior to toJSON addition */
   private static final long serialVersionUID = 1112483274306465419L;
   private static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSS");

   /** The current byte[] version */
   private static final int VERSION = 4;
   private String scannerID;
   private String uuid;
   private int code;
   private int manufacturer;
   private int major;
   private int minor;
   private int power;
   private int calibratedPower;
   private int rssi;
   private int messageType;
   private int count;
   private long time;
    private int scannerSequenceNo;

    public Beacon() {
   }
   /**
    * Create a beacon
    * @param uuid - proximity uuid
    * @param code - beacon type code
    * @param manufacturer - manufacturer code
    * @param major - beacon major code
    * @param minor - beacon minor code
    * @param power - transmit power
    * @param rssi - received signal strength indicator
    */
   public Beacon(String scannerID, String uuid, int code, int manufacturer, int major, int minor, int power, int rssi) {
      this(scannerID, uuid,code,manufacturer,major,minor,power,rssi,System.currentTimeMillis());
   }
   /**
    * Create a beacon
    * @param scannerID - the id of the scanner which detected the beacon event
    * @param uuid - proximity uuid
    * @param code - beacon type code
    * @param manufacturer - manufacturer code
    * @param major - beacon major code
    * @param minor - beacon minor code
    * @param power - transmit power
    * @param rssi - received signal strength indicator
    * @param time - timestamp of receipt of beacon information
    */
   public Beacon(String scannerID, String uuid, int code, int manufacturer, int major, int minor, int power, int rssi, long time) {
      this.scannerID = scannerID;
      this.uuid = uuid;
      this.code = code;
      this.manufacturer = manufacturer;
      this.major = major;
      this.minor = minor;
      this.power = power;
      this.rssi = rssi;
      this.time = time;
   }
   public Beacon(Beacon orig) {
      this.scannerID = orig.scannerID;
      this.uuid = orig.uuid;
      this.code = orig.code;
      this.manufacturer = orig.manufacturer;
      this.major = orig.major;
      this.minor = orig.minor;
      this.power = orig.power;
      this.calibratedPower = orig.calibratedPower;
      this.rssi = orig.rssi;
      this.time = orig.time;
      this.messageType = orig.messageType;
   }

   public String getScannerID() {
      return scannerID;
   }

   public void setScannerID(String scannerID) {
      this.scannerID = scannerID;
   }

   public String getUUID() {
      return uuid;
   }
   public void setUUID(String uuid) {
      this.uuid = uuid;
   }

   public int getCode() {
      return code;
   }

   public void setCode(int code) {
      this.code = code;
   }

   public int getManufacturer() {
      return manufacturer;
   }

   public void setManufacturer(int manufacturer) {
      this.manufacturer = manufacturer;
   }

   public int getMajor() {
      return major;
   }

   public void setMajor(int major) {
      this.major = major;
   }

   public int getMinor() {
      return minor;
   }

   public void setMinor(int minor) {
      this.minor = minor;
   }

   public int getPower() {
      return power;
   }

   public void setPower(int power) {
      this.power = power;
   }

   public int getCalibratedPower() {
      return calibratedPower;
   }

   public void setCalibratedPower(int calibratedPower) {
      this.calibratedPower = calibratedPower;
   }

   public int getRssi() {
      return rssi;
   }

   public void setRssi(int rssi) {
      this.rssi = rssi;
   }

   public long getTime() {
      return time;
   }

   public void setTime(long time) {
      this.time = time;
   }

   public int getMessageType() {
      return messageType;
   }

   public void setMessageType(int messageType) {
      this.messageType = messageType;
   }

   public int getCount() {
      return count;
   }

   public void setCount(int count) {
      this.count = count;
   }
   public void incCount() {
      this.count ++;
   }

   public boolean isHeartbeat() {
      return messageType == MsgType.SCANNER_HEARTBEAT.ordinal();
   }
   public void setHeartbeat(boolean flag) {
      if(flag)
         messageType = MsgType.SCANNER_HEARTBEAT.ordinal();
      else
         messageType = MsgType.SCANNER_READ.ordinal();
   }

   public static Beacon fromByteMsg(byte[] msg) throws IOException {
      ByteArrayInputStream bais = new ByteArrayInputStream(msg);
      DataInputStream dis = new DataInputStream(bais);
      Beacon beacon = null;
      int version = dis.readInt();
      switch (version) {
         case VERSION:
            beacon = readVersion4(dis);
            break;
         case 3:
            // TODO?
         default:
            throw new IOException(String.format("Msg version: %d does not match current version: %d", version, VERSION));
      }

      return beacon;
   }

   public static Beacon fromProperties(Map<String, Object> beaconProps) {
      Beacon beacon = new Beacon();
      beacon.setScannerID(beaconProps.get("scannerID").toString());
      beacon.setUUID(beaconProps.get("uuid").toString());
      beacon.setCode((Integer) beaconProps.get("code"));
      beacon.setManufacturer((Integer) beaconProps.get("manufacturer"));
      beacon.setMajor((Integer) beaconProps.get("major"));
      beacon.setMinor((Integer) beaconProps.get("minor"));
      beacon.setPower((Integer) beaconProps.get("power"));
      beacon.setCalibratedPower(beacon.getPower());
      beacon.setRssi((Integer) beaconProps.get("rssi"));
      beacon.setTime((Long) beaconProps.get("time"));
      beacon.setMessageType((Integer) beaconProps.get("messageType"));
      return beacon;
   }

   /**
    * Write the current beacon to a serialized binary form using a DataOutputStream for use as the form to send
    * to a mqtt broker. To unserialize a msg use #fromByteMsg()
    *
    * @return byte array serialized form
    * @throws IOException
    */
   public byte[] toByteMsg() throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream dos = new DataOutputStream(baos);
      dos.writeInt(VERSION);
      dos.writeInt(scannerID.length());
      dos.writeBytes(scannerID);
      dos.writeInt(uuid.length());
      dos.writeBytes(uuid);
      dos.writeInt(code);
      dos.writeInt(manufacturer);
      dos.writeInt(major);
      dos.writeInt(minor);
      dos.writeInt(power);
      dos.writeInt(calibratedPower);
      dos.writeInt(rssi);
      dos.writeLong(time);
      dos.writeInt(messageType);
      dos.close();
      return baos.toByteArray();
   }

   public String toJSON() {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      String jsonOutput = gson.toJson(this);
      return jsonOutput;
   }
   public String toJSONSimple() {
      Gson gson = new GsonBuilder().create();
      String jsonOutput = gson.toJson(this);
      return jsonOutput;
   }

   public Map<String, Object> toProperties() {
      HashMap<String, Object> beaconProps = new HashMap<>();
      beaconProps.put("scannerID", getScannerID());
      beaconProps.put("uuid", getUUID());
      beaconProps.put("code", getCode());
      beaconProps.put("manufacturer", getManufacturer());
      beaconProps.put("major", getMajor());
      beaconProps.put("minor", getMinor());
      beaconProps.put("power", getPower());
      beaconProps.put("power", getPower());
      beaconProps.put("rssi", getRssi());
      beaconProps.put("time", getTime());
      beaconProps.put("messageType", getMessageType());
      return beaconProps;
   }

   public String toString() {
      Date date = new Date(time);
      return String.format("{[%s,%d,%d]code=%d,manufacturer=%d,cpower=%d,rssi=%d,time=%s @ %s}", uuid, major, minor, code, manufacturer, calibratedPower, rssi, TIME_FORMAT.format(date), scannerID);
   }

   private static Beacon readVersion4(DataInputStream dis) throws IOException {
      int length = dis.readInt();
      byte[] scannerBytes = new byte[length];
      dis.readFully(scannerBytes);
      String scannerID = new String(scannerBytes);
      length = dis.readInt();
      byte[] uuidBytes = new byte[length];
      dis.readFully(uuidBytes);
      String uuid = new String(uuidBytes);
      int code = dis.readInt();
      int manufacturer = dis.readInt();
      int major = dis.readInt();
      int minor = dis.readInt();
      int power = dis.readInt();
      int calibratedPower = dis.readInt();
      int rssi = dis.readInt();
      long time = dis.readLong();
      int messageType = dis.readInt();
      dis.close();
      Beacon beacon = new Beacon(scannerID, uuid, code, manufacturer, major, minor, power, rssi, time);
      beacon.setCalibratedPower(calibratedPower);
      beacon.setMessageType(messageType);
      return beacon;
   }

    public void setScannerSequenceNo(int scannerSequenceNo) {
        this.scannerSequenceNo = scannerSequenceNo;
    }

    public int getScannerSequenceNo() {
        return scannerSequenceNo;
    }
}
