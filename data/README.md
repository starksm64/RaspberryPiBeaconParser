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

## FourScannersRun#1-2015-03-07.json.gz
This file contains 38942 beacon events of the course of 22 minutes. The data summary in terms of uuid, major, minor, code and manufacturer is:

	[data 684]$ cat FourScannersRun#1-2015-03-07.json | grep uuid | sort | uniq -c | sort -n
	6086   "uuid": "DAF246CEF20211E4B116123B93F75CBA",
	6492   "uuid": "DAF246CEF20311E4B116123B93F75CBA",
	6501   "uuid": "DAF246CEF20411E4B116123B93F75CBA",
	6510   "uuid": "DAF246CEF20111E4B116123B93F75CBA",
	13353   "uuid": "DAF246CE836311E4B116123B93F75CBA",
	[data 685]$ cat FourScannersRun#1-2015-03-07.json | grep major | sort | uniq -c | sort -n
	3212   "major": 14,
	4225   "major": 18,
	5916   "major": 1,
	6086   "major": 202,
	6492   "major": 203,
	6501   "major": 204,
	6510   "major": 201,
	[data 686]$ cat FourScannersRun#1-2015-03-07.json | grep minor | sort | uniq -c | sort -n
	 408   "minor": 8,
	 675   "minor": 5,
	1474   "minor": 7,
	3212   "minor": 1,
	3359   "minor": 6,
	4225   "minor": 2,
	25589   "minor": 20,
	[data 688]$ cat FourScannersRun#1-2015-03-07.json | grep code | sort | uniq -c | sort -n
	38942   "code": 533,
	[data 689]$ cat FourScannersRun#1-2015-03-07.json | grep manufacturer | sort | uniq -c | sort -n
	38942   "manufacturer": 19456,
	
This data set now also includes a messageType field that indicates whether a message is a SCANNER_READ(0) or SCANNER_HEARTBEAT(1) event:

	[data 696]$ cat FourScannersRun#1-2015-03-07.json | grep messageType | sort | uniq -c | sort -n
    13353   "messageType": 1,
    25589   "messageType": 0,

The represents the following scenario. There are four scanners with scannerID settings of Room201, Room202, Room203 and Room204. Each scanner has a beacon next to it that has a uuid of the form DAF246CEF**RRR**11E4B116123B93F75CBA where RRR is the room number. These beacons have a major value equal to the room number, and a minor value equal to their configured transmit power times -1. The remaining six beacons have a fixed uuid of DAF246CE836311E4B116123B93F75CBA, a major value equal to the beacon number (1,2,5,6,7,8) and a minor value that is currently not meaningful.

The six beacons were moved between the rooms in the following manner. The first time is roughly when the event begins, and the time after the description is roughly when it ends. It is only rough as the times are when I left my computer either with the beacon or to get the beacon, and when I returned to my computer either after leaving the beacon or returning with the beacon.

* Sat Mar  7 11:46:11 PST 2015
	* All beacons start in 201
* Sat Mar  7 11:46:26 PST 2015
	* 1 and 2 go to 204
	* Sat Mar  7 11:47:24 PST 2015
* Sat Mar  7 11:48:23 PST 2015
	* 5 and 6 go to 202
	* Sat Mar  7 11:49:40 PST 2015
* Sat Mar  7 11:50:05 PST 2015
	* 7 and 8 go to 203
	* Sat Mar  7 11:51:32 PST 2015
* Sat Mar  7 11:52:16 PST 2015
	* 1 goes from 204 to 202 to meet 5, then both return to 201
	* Sat Mar  7 11:53:39 PST 2015
* Sat Mar  7 11:54:48 PST 2015
	* 2 returns to 201
	* Sat Mar  7 11:55:45 PST 2015
* Sat Mar  7 11:55:53 PST 2015
	* 7 returns to 201
	* Sat Mar  7 11:56:54 PST 2015
* Sat Mar  7 11:57:06 PST 2015
	* 6 goes to 203 to meet 8, then both return to 201
	* Sat Mar  7 11:58:20 PST 2015
* Sat Mar  7 11:58:43 PST 2015
	* all scanners stopped

### FourScannersRun1-2015-03-10.json.gz
This file contains 103446 beacon events of the course of 20 minutes. The only real difference between this dataset and FourScannersRun#1-2015-03-07.json.gz is that the beacons {1,2,5,6,7,8} are transmitting at maximum power vs, a much lower power level in the previous run.

The data summary in terms of uuid, major, minor, code, manufacturer and messageType is:

	[data 534]$ cat FourScannersRun#1-2015-03-10.json | grep uuid | sort | uniq -c | sort -n
	7765   "uuid": "DAF246CEF20211E4B116123B93F75CBA",
	7960   "uuid": "DAF246CEF20311E4B116123B93F75CBA",
	8946   "uuid": "DAF246CEF20411E4B116123B93F75CBA",
	9006   "uuid": "DAF246CEF20111E4B116123B93F75CBA",
	69769   "uuid": "DAF246CE836311E4B116123B93F75CBA",
	[data 535]$ cat FourScannersRun#1-2015-03-10.json | grep major | sort | uniq -c | sort -n
	7765   "major": 202,
	7960   "major": 203,
	8946   "major": 204,
	9006   "major": 201,
	11741   "major": 1,
	58028   "major": 0,
	[data 536]$ cat FourScannersRun#1-2015-03-10.json | grep minor | sort | uniq -c | sort -n
	8852   "minor": 7,
	10692   "minor": 8,
	10867   "minor": 1,
	11741   "minor": 6,
	12513   "minor": 2,
	15104   "minor": 5,
	33677   "minor": 20,
	[data 537]$ cat FourScannersRun#1-2015-03-10.json | grep code | sort | uniq -c | sort -n
	103446   "code": 533,
	[data 538]$ cat FourScannersRun#1-2015-03-10.json | grep manufacturer | sort | uniq -c | sort -n
	103446   "manufacturer": 19456,
	[data 539]$ cat FourScannersRun#1-2015-03-10.json | grep messageType | sort | uniq -c | sort -n
	33677   "messageType": 1,
	69769   "messageType": 0, 

The represents the following scenario. There are four scanners with scannerID settings of Room201, Room202, Room203 and Room204. Each scanner has a beacon next to it that has a uuid of the form DAF246CEF**RRR**11E4B116123B93F75CBA where RRR is the room number. These beacons have a major value equal to the room number, and a minor value equal to their configured transmit power times -1. The remaining six beacons have a fixed uuid of DAF246CE836311E4B116123B93F75CBA, a minor value equal to the beacon number (1,2,5,6,7,8) and a major value of 0.

The six beacons were moved between the rooms in the following manner. The first time is roughly when the event begins, and the time after the description is roughly when it ends. It is only rough as the times are when I left my computer either with the beacon or to get the beacon, and when I returned to my computer either after leaving the beacon or returning with the beacon.

	•	Tue Mar 10 12:41:59 PDT 2015
		◦	All beacons start in 201
	•	Tue Mar 10 12:42:10 PDT 2015
		◦	1 and 2 go to 204
		◦	Tue Mar 10 12:43:14 PDT 2015
	•	Tue Mar 10 12:44:02 PDT 2015
		◦	5 and 6 go to 202
		◦	Tue Mar 10 12:45:04 PDT 2015
	•	Tue Mar 10 12:45:44 PDT 2015
		◦	7 and 8 go to 203
		◦	Tue Mar 10 12:46:59 PDT 2015
	•	Tue Mar 10 12:50:56 PDT 2015
		◦	1 goes from 204 to 202 to meet 5, then both return to 201
		◦	Tue Mar 10 12:52:18 PDT 2015
	•	Tue Mar 10 12:52:30 PDT 2015
		◦	2 returns to 201
		◦	Tue Mar 10 12:53:28 PDT 2015
	•	Tue Mar 10 12:53:39 PDT 2015
		◦	7 returns to 201
		◦	Tue Mar 10 12:54:50 PDT 2015
	•	Tue Mar 10 12:55:08 PDT 2015
		◦	6 goes to 203 to meet 8, then both return to 201
		◦	Tue Mar 10 12:56:45 PDT 2015
	•	Tue Mar 10 12:59:00 PDT 2015
		◦	all scanners stopped

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


