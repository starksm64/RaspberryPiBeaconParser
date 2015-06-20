package org.jboss.summit2015.scanner.status;
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

/**
 * @author Scott Stark (sstark@redhat.com) (C) 2014 Red Hat Inc.
 */
public enum StatusProperties {
   ScannerID,          // the name of the scanner passed in via the --scannerID argument
   HostIPAddress,      // the ip address of the scanner host
   MACAddress,         // the MAC address of the scanner host
   SystemType,         // Type of the system; PiB, PiB+, P2B, IntelNUC, BeagleBoneBlack, ...
   SystemOS,           // Type of the operating system; Pidora, Raspbian, ...
   SystemTime,         // strftime(timestr, 128, "%F %T", tm) = YYYY-MM-DD HH:MM:SS
   SystemTimeMS,       // system time in milliseconds since epoch
   Uptime,             // uptime of scanner process in seconds as string formatted as "uptime: %ld, days:%d, hrs: %d, min: %d"
   SystemUptime,       // uptime of system in seconds as string formatted as "uptime: %ld, days:%d, hrs: %d, min: %d"
   Procs,              // number of procs active on the scanner
   LoadAverage,        // load averages for the past 1, 5, and 15 minutes "load average: 0.00, 0.01, 0.05"
   RawEventCount,      // Raw number of BLE iBeacon type of events from the bluetooth stack
   PublishEventCount,  // The number of time windowed events pushed to the message broker
   HeartbeatCount,     // The number of events from the scanner's associated --heartbeatUUID beacon
   HeartbeatRSSI,      // The average RSSI for the scanner's associated --heartbeatUUID beacon
   EventsWindow,       // The counts of beacon events as a sequence of +{minorID}: {count}; values
   ActiveBeacons,      // The count of beacons active in the last events window
   MemTotal,           // Total memory on scanner in MB
   MemFree,            // Free memory on scanner in MB
   MemActive,          // Total - Free memory on scanner in MB
   SwapTotal,          // Total swap memory on scanner in MB
   SwapFree,           // Free swap memory on scanner in MB
}
