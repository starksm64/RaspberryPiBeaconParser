var ScannerStatus;
(function (ScannerStatus) {
    var ScannerHealth = (function () {
        function ScannerHealth(scannerID) {
            this.scannerID = scannerID;
            this.lastStatus = new Map();
            this.sinceLastStatus = 0;
            this.alertSound = new Audio('alert.wav');
            this.alertClearSound = new Audio('alertclear.wav');
            this.alertClearSounded = true;
        }
        ScannerHealth.prototype.getScannerID = function () {
            return this.scannerID;
        };
        ScannerHealth.prototype.setScannerID = function (value) {
            this.scannerID = value;
        };
        ScannerHealth.prototype.getTime = function () {
            return this.time;
        };
        ScannerHealth.prototype.setTime = function (value) {
            this.time = value;
        };
        ScannerHealth.prototype.getSinceLastStatus = function () {
            return this.sinceLastStatus;
        };
        ScannerHealth.prototype.setSinceLastStatus = function (seconds) {
            this.sinceLastStatus = seconds;
        };
        ScannerHealth.prototype.updateSinceLastStatus = function (now) {
            this.sinceLastStatus = (now - this.time) / 1000;
            if (this.sinceLastStatus > 60) {
                this.alertClearSounded = false;
                this.alertSound.play();
            }
            else {
                this.alertSound.pause();
                if (!this.alertClearSounded) {
                    this.alertClearSounded = true;
                    this.alertClearSound.play();
                }
            }
        };
        ScannerHealth.prototype.getAlert = function () {
            return this.sinceLastStatus > 60;
        };
        ScannerHealth.prototype.getLastStatus = function () {
            return this.lastStatus;
        };
        ScannerHealth.prototype.setLastStatus = function (value) {
            this.lastStatus = value;
        };
        ScannerHealth.prototype.getProperty = function (key) {
            return this.lastStatus.get(key);
        };
        ScannerHealth.prototype.setProperty = function (key, value) {
            this.lastStatus.set(key, value);
        };
        return ScannerHealth;
    })();
    ScannerStatus.ScannerHealth = ScannerHealth;
})(ScannerStatus || (ScannerStatus = {}));
//# sourceMappingURL=scannerHealth.js.map