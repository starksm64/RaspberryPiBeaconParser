require('../scannerHealth.js');

var scanner201 = new ScannerHealth("Room201");
console.log(scanner201);

scanner201.setTime(Date.now());
scanner201.setProperty('SystemType', 'Pi2B');
console.log(scanner201);
