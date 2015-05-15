/// <reference path="../scannerHealth.ts" />
import SH = require('../scannerHealth');

var scanner201 = new SH.ScannerHealth("Room201");
console.log(scanner201);
for(var p in scanner201) {
    console.log(p);
}

scanner201.setTime(Date.now());
scanner201.setProperty('ScannerID', 'Room201');
scanner201.setProperty('SystemType', 'Pi2B');
scanner201.setProperty('Uptime', 'days: 2 hours:7');
console.log(scanner201);
console.log(scanner201.getLastStatus());
for(var p in scanner201.getLastStatus()) {
    console.log(p);
}
console.log(scanner201.getProperty('SystemType'));
