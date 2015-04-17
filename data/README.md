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

The represents the following scenario. There are four scanners with scannerID settings of Room201, Room202, Room203 and Room204. Each scanner has a beacon next to it that has a uuid of the form DAF246CEF**RRR**11E4B116123B93F75CBA where RRR is the room number. These beacons have a major value equal to the room number, and a minor value equal to their configured transmit power times -1. The remaining six beacons have a fixed uuid of DAF246CE836311E4B116123B93F75CBA, a minor value equal to the beacon number {1,2,5,6,7,8} and a major value of 0.

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

### FourScannersRun1-2015-03-13.json.gz
A dataset that repeats the senario as detailed in FourScannersRun1-2015-03-10.json.gz dataset. In this run, the scanners included some additional shielding to reduce the signals between the room scanners. The scanners also were run with --skipHeartbeat to reduce the event data to only that from the six beacons with minor ids: {1,2,5,6,7,8}.

The data summary in terms of uuid, major, minor, code, manufacturer and messageType is:

	[data 658]$ gzcat FourScannersRun1-2015-03-13.json.gz | grep uuid | sort | uniq -c | sort -n
	26317   "uuid": "DAF246CE836311E4B116123B93F75CBA",
	[data 659]$ gzcat FourScannersRun1-2015-03-13.json.gz | grep major | sort | uniq -c | sort -n
	7373   "major": 1,
	18944   "major": 0,
	[data 660]$ gzcat FourScannersRun1-2015-03-13.json.gz | grep minor | sort | uniq -c | sort -n
	3831   "minor": 7,
	4706   "minor": 8,
	5196   "minor": 1,
	5211   "minor": 5,
	7373   "minor": 6,
	[data 661]$ gzcat FourScannersRun1-2015-03-13.json.gz | grep code | sort | uniq -c | sort -n
	26317   "code": 533,
	[data 662]$ gzcat FourScannersRun1-2015-03-13.json.gz | grep manufacturer | sort | uniq -c | sort -n
	26317   "manufacturer": 19456,
	[data 663]$ gzcat FourScannersRun1-2015-03-13.json.gz | grep messageType | sort | uniq -c | sort -n
	26317   "messageType": 0,
	[data 664]$ 



### FourScannersBeacon5Run2-2015-03-13.json.gz
A test run of beacon with minor id 5 that walks between the room 1-4 scanners. In this run the scanners had additional screening between the scanners to attempt to reduce the cross reception between the scanners.

### FourScannersBeacon5Run1-2015-03-13.json.gz
A test run of beacon with minor id 5 that walks between the room 1-4 scanners.

### SixScannersRun-2015-03-31.json.gz
A test run of ~150 beacons moving between 6 difference scanners. The becons were broken up into groups of 10 with
another 7 beacons used individually. A summary of the data is:

	[data 863]$ gzcat SixScannersRun-2015-03-31.json.gz | grep uuid | sort | uniq -c | sort -n
	   3   "uuid": "DAF246CEF20111E4B116123B93F75CBA",
	82821   "uuid": "DAF246CE836311E4B116123B93F75CBA",
	[data 864]$ gzcat SixScannersRun-2015-03-31.json.gz | grep major | sort | uniq -c | sort -n
	   3   "major": 201,
	 909   "major": 1,
	81912   "major": 0,
	[data 865]$ gzcat SixScannersRun-2015-03-31.json.gz | grep minor | sort | uniq -c | sort -n
	   2   "minor": 113,
	   2   "minor": 146,
	   2   "minor": 78,
	   2   "minor": 94,
	   3   "minor": 108,
	   3   "minor": 150,
	   3   "minor": 201,
	   4   "minor": 22,
	   4   "minor": 45,
	   5   "minor": 13,
	  22   "minor": 107,
	  22   "minor": 144,
	  23   "minor": 106,
	  23   "minor": 34,
	  23   "minor": 42,
	  23   "minor": 48,
	  23   "minor": 56,
	  24   "minor": 14,
	  28   "minor": 25,
	  29   "minor": 7,
	  82   "minor": 103,
	  94   "minor": 122,
	 121   "minor": 92,
	 125   "minor": 12,
	 166   "minor": 36,
	 166   "minor": 37,
	 167   "minor": 53,
	 168   "minor": 40,
	 171   "minor": 2,
	 195   "minor": 125,
	 198   "minor": 21,
	 200   "minor": 29,
	 233   "minor": 17,
	 257   "minor": 23,
	 257   "minor": 28,
	 258   "minor": 27,
	 258   "minor": 30,
	 281   "minor": 20,
	 281   "minor": 49,
	 281   "minor": 58,
	 281   "minor": 6,
	 282   "minor": 46,
	 282   "minor": 5,
	 289   "minor": 43,
	 345   "minor": 138,
	 345   "minor": 149,
	 345   "minor": 91,
	 346   "minor": 88,
	 352   "minor": 71,
	 361   "minor": 38,
	 387   "minor": 97,
	 388   "minor": 127,
	 388   "minor": 128,
	 388   "minor": 70,
	 388   "minor": 75,
	 389   "minor": 105,
	 389   "minor": 82,
	 390   "minor": 117,
	 393   "minor": 123,
	 401   "minor": 142,
	 403   "minor": 147,
	 406   "minor": 39,
	 472   "minor": 77,
	 474   "minor": 109,
	 474   "minor": 152,
	 475   "minor": 114,
	 475   "minor": 139,
	 475   "minor": 96,
	 491   "minor": 120,
	 496   "minor": 24,
	 497   "minor": 32,
	 498   "minor": 9,
	 500   "minor": 19,
	 502   "minor": 134,
	 502   "minor": 59,
	 503   "minor": 33,
	 504   "minor": 55,
	 505   "minor": 18,
	 505   "minor": 52,
	 528   "minor": 156,
	 531   "minor": 26,
	 533   "minor": 47,
	 533   "minor": 8,
	 534   "minor": 35,
	 534   "minor": 54,
	 535   "minor": 50,
	 538   "minor": 1,
	 547   "minor": 64,
	 550   "minor": 112,
	 585   "minor": 154,
	 591   "minor": 65,
	 623   "minor": 69,
	 641   "minor": 140,
	 651   "minor": 145,
	 651   "minor": 157,
	 652   "minor": 111,
	 653   "minor": 102,
	 659   "minor": 132,
	 659   "minor": 76,
	 667   "minor": 115,
	 668   "minor": 86,
	 669   "minor": 110,
	 669   "minor": 135,
	 669   "minor": 79,
	 671   "minor": 148,
	 680   "minor": 133,
	 681   "minor": 84,
	 685   "minor": 74,
	 692   "minor": 124,
	 705   "minor": 80,
	 706   "minor": 83,
	 711   "minor": 99,
	 729   "minor": 57,
	 730   "minor": 44,
	 731   "minor": 15,
	 733   "minor": 51,
	 745   "minor": 100,
	 750   "minor": 137,
	 753   "minor": 98,
	 786   "minor": 130,
	 797   "minor": 87,
	 821   "minor": 72,
	 822   "minor": 126,
	 832   "minor": 121,
	 838   "minor": 90,
	 845   "minor": 95,
	 846   "minor": 129,
	 854   "minor": 73,
	 897   "minor": 101,
	 900   "minor": 131,
	 910   "minor": 118,
	 923   "minor": 89,
	 945   "minor": 104,
	 948   "minor": 153,
	 948   "minor": 85,
	 949   "minor": 143,
	 978   "minor": 151,
	 984   "minor": 63,
	1028   "minor": 81,
	1073   "minor": 68,
	1078   "minor": 116,
	1114   "minor": 61,
	1200   "minor": 62,
	1204   "minor": 60,
	1250   "minor": 119,
	1253   "minor": 66,
	1271   "minor": 93,
	1290   "minor": 155,
	1476   "minor": 136,
	1494   "minor": 67,
	1523   "minor": 159,
	1728   "minor": 158,
	1732   "minor": 141,
	[data 866]$ gzcat SixScannersRun-2015-03-31.json.gz | grep code | sort | uniq -c | sort -n
	82824   "code": 533,
	[data 867]$ gzcat SixScannersRun-2015-03-31.json.gz | grep manufacturer | sort | uniq -c | sort -n
	82824   "manufacturer": 19456,
	[data 868]$ gzcat SixScannersRun-2015-03-31.json.gz | grep messageType | sort | uniq -c | sort -n
	   3   "messageType": 1,
	82821   "messageType": 0,
	[data 869]$ gzcat SixScannersRun-2015-03-31.json.gz | grep minor | sort | uniq -c | sort -n | wc
		 153     459    3114
	
The event sequence of movements is given below. A G13 reference means group number 13 of 10 beacons. A reference to a number 13 without a G prefix means the beacon with the minor value equal to 13.

* Starting locations, Tue Mar 31 15:01:12 PDT 2015:
	* 201; G2, G9, G11, G13, 69, 98
	* 202; G5, 159
	* 203; G8, G15
	* 204; G1, G10, G12, 81 
	* 205; G3, G6, G7, 66
	* 206; G4, G14, 61, 130

* Tue Mar 31 15:02:07 PDT 2015
	* G1 from 204 to 205
	* 81 from 204 to 205
	* 98 from 201 to 204
	* 66 from 205 to 201
 
* Tue Mar 31 15:08:04 PDT 2015
	* G2 from 201 to 203
	* 69 from 201 to 203
	* G8 from 203 to 201
 
* Tue Mar 31 15:08:04 PDT 2015
	* 154 from 201 to 205 and put on dog
 
* Tue Mar 31 15:11:12 PDT 2015
	* G3 from 205 to 206
	* G4 from 206 to 201
	* 130 from 206 to 201
 
* Tue Mar 31 15:14:19 PDT 2015
	* G5 from 202 to 204
	* 66 from 201 to 202
	* 98 from 204 to 201
 
* Tue Mar 31 15:16:12 PDT 2015
	* G6 from 205 to 202
	* 130 from 201 to 205
	* 81 from 205 to 202
	* 66 from 202 to 201
 
* Tue Mar 31 15:19:13 PDT 2015
	* G7 from 205 to 204
	* 66 from 201 to 205
	* 159 from 204 to 201
 
* Tue Mar 31 15:22:04 PDT 2015
	* G8 from 201 to 202
	* 98 from 201 to 202
	* 81 from 202 to 201
 
* Tue Mar 31 15:30:34 PDT 2015
	* G9 from 201 to 206
	* 159 from 201 to 206
	* 61 from 206 to 201
 
* G10 from 204 to 203
	* 81 from 201 to 204
 
* Tue Mar 31 15:33:46 PDT 2015
	* G11 from 201 to 204
	* 61 from 201 to 204
	* G10 from 204 to 201
 
* Tue Mar 31 15:35:59 PDT 2015
	* G12 from 204 to 203
	* 81 from 201 to 203 via 204
	* 69 from 203 to 201
 
* Tue Mar 31 15:40:55 PDT 2015
	* G13 from 201 to 205
	* 66 from 205 to 201
 
* Tue Mar 31 15:42:14 PDT 2015
	* G14 from 206 to 201
	* 159 from 206 to 201
	* 69 from 201 to 206
 
* Tue Mar 31 15:44:30 PDT 2015
	* G15 from 203 to 202
	* 66 from 201 to 203
	* 81 from 203 to 201
 
* Tue Mar 31 15:47:41 PDT 2015
	* G1 from 205 to 201
	* 159 from 201 to 205
 
* Tue Mar 31 15:48:09 PDT 2015
	* G2 from 203 to 201
	* 81 from 201 to 203 and back to 201
 
* Tue Mar 31 15:49:56 PDT 2015
	* G3 from 206 to 201
	* 81 from 201 to 206 and back to 201
 
* Tue Mar 31 15:53:23 PDT 2015
	* G5 from 204 to 201
	* 130 from 204 to 201 
	* 81 from 201 to 204 and back to 201
 
* Tue Mar 31 15:55:59 PDT 2015
	* G6 from 202 to 201
	* 81 from 201 to 202 and back to 201
 
* Tue Mar 31 15:56:26 PDT 2015
	* G7 from 204 to 201
	* 81 from 201 to 204 and back to 201
 
* Tue Mar 31 15:58:03 PDT 2015
	* G8 from 202 to 201
	* 98 from 202 to 201
	* 81 from 201 to 202 and back to 201
 
* Tue Mar 31 15:59:33 PDT 2015
	* G9 from 206 to 201
	* 81 from 201 to 206 and back to 201
	* 69 from 206 to 201

* Tue Mar 31 16:01:37 PDT 2015
	* G11 from 204 to 201
	* 81 from 201 to 204 and back to 201
	* 61 from 204 to 201
 
* Tue Mar 31 16:04:02 PDT 2015
	* G12 from 203 to 201
	* 81 from 201 to 203 and back to 201
	* 66 from 203 to 201
 
* Tue Mar 31 16:05:51 PDT 2015
	* G13 from 205 to 201
	* 81 from 201 to 205 and back to 201
	* 159 from 205 to 201
 
* Tue Mar 31 16:07:27 PDT 2015
	* 81 searched for any outstanding beacons:
	* 201 to 202 to 204 to 203 to 206 back to 201
 
* End Tue Mar 31 16:11:07 PDT 2015

# SevenScannersRun-2015-04-17.json.gz

[tmp 1219]$ cat SevenScannersRun-2015-04-17.json | grep uuid | sort | uniq -c | sort -n
1716   "uuid": "DAF246CEF99911E4B116123B93F75CBA",
3990   "uuid": "DAF246CEF20111E4B116123B93F75CBA",
4179   "uuid": "DAF246CEF20311E4B116123B93F75CBA",
5214   "uuid": "DAF246CEF20411E4B116123B93F75CBA",
7629   "uuid": "DAF246CEF20211E4B116123B93F75CBA",
1201401   "uuid": "DAF246CE836311E4B116123B93F75CBA",
[tmp 1220]$ cat SevenScannersRun-2015-04-17.json | grep major | sort | uniq -c | sort -n
1716   "major": 999,
3990   "major": 201,
4179   "major": 203,
5214   "major": 204,
7629   "major": 202,
24362   "major": 1,
1177039   "major": 0,
[tmp 1221]$ cat SevenScannersRun-2015-04-17.json | grep minor | sort | uniq -c | sort -n
1716   "minor": 999,
2771   "minor": 28,
3708   "minor": 26,
3775   "minor": 88,
3784   "minor": 159,
3803   "minor": 81,
3814   "minor": 23,
3828   "minor": 66,
3851   "minor": 149,
3936   "minor": 138,
3990   "minor": 201,
4108   "minor": 16,
4179   "minor": 203,
4376   "minor": 12,
4599   "minor": 27,
4660   "minor": 9,
4822   "minor": 30,
4847   "minor": 13,
5113   "minor": 148,
5168   "minor": 29,
5214   "minor": 204,
5469   "minor": 31,
5496   "minor": 32,
5499   "minor": 24,
5565   "minor": 25,
5610   "minor": 22,
5624   "minor": 19,
5996   "minor": 130,
6063   "minor": 126,
6156   "minor": 132,
6172   "minor": 87,
6262   "minor": 155,
6289   "minor": 95,
6300   "minor": 72,
6322   "minor": 119,
6331   "minor": 76,
6473   "minor": 73,
6498   "minor": 157,
6524   "minor": 86,
6587   "minor": 33,
6593   "minor": 144,
6622   "minor": 115,
6758   "minor": 55,
6791   "minor": 59,
6910   "minor": 52,
6968   "minor": 68,
6997   "minor": 120,
7034   "minor": 75,
7100   "minor": 77,
7175   "minor": 103,
7193   "minor": 121,
7263   "minor": 150,
7286   "minor": 123,
7303   "minor": 48,
7336   "minor": 110,
7339   "minor": 93,
7368   "minor": 64,
7390   "minor": 7,
7396   "minor": 56,
7427   "minor": 114,
7453   "minor": 127,
7461   "minor": 139,
7507   "minor": 42,
7515   "minor": 134,
7531   "minor": 107,
7609   "minor": 18,
7614   "minor": 152,
7629   "minor": 202,
7679   "minor": 99,
7686   "minor": 135,
7698   "minor": 128,
7702   "minor": 53,
7737   "minor": 89,
7753   "minor": 50,
7841   "minor": 136,
7923   "minor": 117,
8010   "minor": 113,
8026   "minor": 65,
8028   "minor": 92,
8067   "minor": 43,
8104   "minor": 122,
8106   "minor": 151,
8195   "minor": 125,
8199   "minor": 34,
8213   "minor": 78,
8216   "minor": 142,
8216   "minor": 82,
8224   "minor": 118,
8257   "minor": 94,
8266   "minor": 91,
8275   "minor": 79,
8279   "minor": 102,
8285   "minor": 40,
8328   "minor": 108,
8328   "minor": 69,
8332   "minor": 71,
8381   "minor": 141,
8393   "minor": 54,
8405   "minor": 47,
8420   "minor": 61,
8437   "minor": 105,
8452   "minor": 97,
8470   "minor": 38,
8477   "minor": 146,
8537   "minor": 154,
8537   "minor": 98,
8614   "minor": 35,
8694   "minor": 106,
8695   "minor": 37,
8710   "minor": 80,
8713   "minor": 67,
8725   "minor": 111,
8737   "minor": 70,
8755   "minor": 39,
8793   "minor": 14,
8963   "minor": 145,
9039   "minor": 84,
9254   "minor": 36,
9268   "minor": 83,
9284   "minor": 116,
9330   "minor": 51,
9347   "minor": 112,
9419   "minor": 158,
9619   "minor": 8,
9663   "minor": 44,
9674   "minor": 156,
9696   "minor": 45,
9817   "minor": 109,
9854   "minor": 147,
9881   "minor": 15,
10004   "minor": 100,
10056   "minor": 101,
10088   "minor": 20,
10159   "minor": 137,
10185   "minor": 2,
10221   "minor": 133,
10296   "minor": 21,
10301   "minor": 90,
10338   "minor": 46,
10352   "minor": 57,
10390   "minor": 49,
10457   "minor": 85,
10479   "minor": 6,
10510   "minor": 1,
10514   "minor": 129,
10693   "minor": 104,
10819   "minor": 153,
10839   "minor": 124,
10857   "minor": 143,
10880   "minor": 140,
11010   "minor": 58,
11402   "minor": 74,
11562   "minor": 62,
12995   "minor": 5,
15928   "minor": 63,
17902   "minor": 60,
[tmp 1222]$ cat SevenScannersRun-2015-04-17.json | grep messageType | sort | uniq -c | sort -n
13383   "messageType": 1,
1210746   "messageType": 0,
[tmp 1223]$ cat SevenScannersRun-2015-04-17.json | grep minor | sort | uniq -c | sort -n | wc
     156     468    3203
[tmp 1224]$ 

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


