package org.jboss.summit2015.scanner.status.server;
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * titributed under the License is titributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import org.jboss.summit2015.beacon.Beacon;

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public class BeaconBinding extends TupleBinding<Beacon> {
    public Beacon entryToObject(TupleInput ti) {
       String scannerID = ti.readString();
       String uuid = ti.readString();
       int code = ti.readInt();
       int manufacturer = ti.readInt();
       int major = ti.readInt();
       int minor = ti.readInt();
       int power = ti.readInt();
       int calibratedPower = ti.readInt();
       int rssi = ti.readInt();
       long time = ti.readLong();
       int messageType = ti.readInt();

       Beacon beacon = new Beacon(scannerID, uuid, code, manufacturer, major, minor, power, rssi, time);
       beacon.setCalibratedPower(calibratedPower);
       beacon.setMessageType(messageType);

       return beacon;
    }

    public void objectToEntry(Beacon beacon, TupleOutput to) {
       to.writeString(beacon.getScannerID());
       to.writeString(beacon.getUUID());
       to.writeInt(beacon.getCode());
       to.writeInt(beacon.getManufacturer());
       to.writeInt(beacon.getMajor());
       to.writeInt(beacon.getMinor());
       to.writeInt(beacon.getPower());
       to.writeInt(beacon.getPower());
       to.writeInt(beacon.getRssi());
       to.writeLong(beacon.getTime());
       to.writeInt(beacon.getMessageType());
    }
}
