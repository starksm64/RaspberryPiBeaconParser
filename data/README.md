## Data Test Files
This directory contains example data files representing beacon events.

* beanEvents.ser - binary file of the serialized forms of org.jboss.summit2015.beacon.Beacon objects written using the java.io.ObjectOutputStream. The file contains 3600 events from two beacons.
* testWriteJson.json - A json represention of the beanEvents.ser objects.

### TwoScannersRun#1-2015-03-02.json.gz
This file contains 18064 beacons events over the course of about 9 minutes. The events were gathered by two scanners identified by the scannerIDs of "Room201" and "Room202". There are four active beacons. Beacon 1 with uuid,major,minor of {DAF246CE836311E4B116123B93F75CBA,14,1} was the beacon that moved around. It first started nearest to Room201, then travelled to near Room202 where it sat for a couple of minutes, and then left Room202 to outside any scanner range for a couple of minutes, then travelled back to Room202 and ended back in Room201.

Beacon 2 {DAF246CE836311E4B116123B93F75CBA,18,2} remained in Room201 and thus closest to that scanner the entire time.

Beacon 3 {DAF246CE836311E4B116123B93F75CBA,0,3} was situated on top of the Room201 scanner the entire time.

Beacon 4 {DAF246CE836311E4B116123B93F75CBA,1,4} was situated on top of the Room202 scanner the entire time.




