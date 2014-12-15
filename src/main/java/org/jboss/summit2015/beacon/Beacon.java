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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class Beacon {
   /** The current byte[] version */
   private static final int VERSION = 1;
   private String uuid;
   private int code;
   private int manufacturer;
   private int major;
   private int minor;
   private int power;
   private int rssi;

   public Beacon(String uuid, int code, int manufacturer, int major, int minor, int power, int rssi) {
      this.uuid = uuid;
      this.code = code;
      this.manufacturer = manufacturer;
      this.major = major;
      this.minor = minor;
      this.power = power;
      this.rssi = rssi;
   }

   public String getUUID() {
      return uuid;
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

   public int getRssi() {
      return rssi;
   }

   public void setRssi(int rssi) {
      this.rssi = rssi;
   }


   public static Beacon fromByteMsg(byte[] msg) throws IOException {
      ByteArrayInputStream bais = new ByteArrayInputStream(msg);
      DataInputStream dis = new DataInputStream(bais);
      int version = dis.readInt();
      if(version != VERSION)
         throw new IOException(String.format("Msg version: %d does not match current version: %d", version, VERSION));
      String uuid = dis.readUTF();
      int code = dis.readInt();
      int manufacturer = dis.readInt();
      int major = dis.readInt();
      int minor = dis.readInt();
      int power = dis.readInt();
      int rssi = dis.readInt();
      dis.close();
      Beacon beacon = new Beacon(uuid, code, manufacturer, major, minor, power, rssi);
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
      dos.writeUTF(uuid);
      dos.writeInt(code);
      dos.writeInt(manufacturer);
      dos.writeInt(major);
      dos.writeInt(minor);
      dos.writeInt(power);
      dos.writeInt(rssi);
      dos.close();
      return baos.toByteArray();
   }
}
