## Data Test Files
This directory contains example data files representing beacon events.

* beanEvents.ser - binary file of the serialized forms of org.jboss.summit2015.beacon.Beacon objects written using the java.io.ObjectOutputStream. The file contains 3600 events from two beacons.
* testWriteJson.json - A json represention of the beanEvents.ser objects.

### TwoScannersRun#1-2015-03-02.json.gz
This file contains 18064 beacon events over the course of about 9 minutes. The events were gathered by two scanners identified by the scannerIDs of "Room201" and "Room202". There are four active beacons. Beacon 1 with uuid,major,minor of {DAF246CE836311E4B116123B93F75CBA,14,1} was the beacon that moved around. It first started nearest to Room201, then travelled to near Room202 where it sat for a couple of minutes, and then left Room202 to outside any scanner range for a couple of minutes, then travelled back to Room202 and ended back in Room201.

Beacon 2 {DAF246CE836311E4B116123B93F75CBA,18,2} remained in Room201 and thus closest to that scanner the entire time.

Beacon 3 {DAF246CE836311E4B116123B93F75CBA,0,3} was situated on top of the Room201 scanner the entire time.

Beacon 4 {DAF246CE836311E4B116123B93F75CBA,1,4} was situated on top of the Room202 scanner the entire time.

**Note: This data has events from older incarnations of the scanner that has issues with decoding the uuid correctly. Its probably not useful data. **

### TwoScannersRun#1-2015-03-03.json.gz
This file contains 39802 beacon events over the course of 26 minutes, beginning at around Tue Mar  3 13:16:10 PST 2015 and ending around Tue Mar  3 13:42:30 PST 2015. The time data is in the Java System.currentTimeMillis() value, the difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC.

	[data 552]$ less TwoScannersRun#1-2015-03-03.json | grep minor | sort | uniq -c | sort -n
	1306   "minor": 1,
	3338   "minor": 2,
	13337   "minor": 4,
	21821   "minor": 3,
	[data 553]$ less TwoScannersRun#1-2015-03-03.json | grep uuid | sort | uniq -c | sort -n
	39802   "uuid": "DAF246CE836311E4B116123B93F75CBA",
	[data 554]$ less TwoScannersRun#1-2015-03-03.json | grep code | sort | uniq -c | sort -n
	39802   "code": 533,

There are four active beacons. Beacon 1 with uuid,major,minor of {DAF246CE836311E4B116123B93F75CBA,14,1} was the beacon that moved around. It first started nearest to Room201, then travels between Room201 and Room202, then into Room202 where it sat for a couple of minutes. It travels around the two rooms and away from them until it returns walking first past Room202 and ending up in Room201 until the data ends.

Beacon 2 {DAF246CE836311E4B116123B93F75CBA,18,2} remained in Room201 and thus closest to that scanner the entire time.

Beacon 3 {DAF246CE836311E4B116123B93F75CBA,0,3} was situated on top of the Room201 scanner the entire time.

Beacon 4 {DAF246CE836311E4B116123B93F75CBA,1,4} was situated on top of the Room202 scanner the entire time.

## Estimating beacon distance
http://stackoverflow.com/questions/20416218/understanding-ibeacon-distancing

The following function provides a simple estimate of the distance from the scanner given the beacon event RSSI and CalibratedPower readings.

	static double estimateDistance(int calibratedPower, double rssi) {
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


