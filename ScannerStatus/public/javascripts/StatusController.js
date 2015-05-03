/// <reference path='paths.ts' />
'use strict';
/**
 * The main controller for the app. The controller:
 * - retrieves and persists the model via the todoStorage service
 * - exposes the model to the template and provides event handlers
 */
var StatusController = (function () {
    function StatusController($scope, $interval) {
        var _this = this;
        this.$scope = $scope;
        this.$interval = $interval;
        $scope.scanner = 0;
        this.scannerStatus = $scope.scannerStatus = new Map();
        // Use lambda to capture correct 'this'
        var callback = function () {
            _this.updateStatusView();
        };
        $interval(callback, 15000);
        $scope.client = new WebSocket("ws://52.10.252.216:61614/stomp");
        $scope.client.addEventListener("open", function (evt) {
            console.log("open: " + evt);
            var command = ["CONNECT"];
            command.push("accept-version:1.2");
            command.push(StatusController.LF);
            var msg = command.join(StatusController.LF);
            msg = msg + StatusController.NULL;
            console.log(msg);
            $scope.client.send(msg);
        });
        $scope.client.addEventListener("error", function (evt) {
            console.log("error: " + evt);
        });
        $scope.client.addEventListener("close", function (evt) {
            console.log("close: " + evt);
        });
        var messageCallback = function (message) {
            _this.messageCallback(message);
        };
        $scope.client.addEventListener("message", messageCallback, true);
        $scope.connected = true;
    }
    /**
     * Iterate over the scanner status and update the sinceLastStatus
     */
    StatusController.prototype.updateStatusView = function () {
        console.log("updateStatusView...");
        var nowMS = +new Date();
        this.scannerStatus.forEach(function (scanner, k) {
            scanner.updateSinceLastStatus(nowMS);
        });
    };
    StatusController.prototype.messageCallback = function (message) {
        //console.log("%O", message);
        if (message.data) {
            var headers = message.data.split(StatusController.LF);
            //console.log("%O", headers);
            if (headers[0] === "MESSAGE") {
                this.message(headers);
            }
            else if (headers[0] === "CONNECTED") {
                this.subscribe();
            }
            else if (headers[0] === "ERROR") {
                this.error(headers);
                this.disconnect();
            }
        }
    };
    StatusController.prototype.message = function (msgHeaders) {
        var lastStatus = new Map();
        //console.log("%O", msgHeaders);
        for (var n in msgHeaders) {
            var hdr = msgHeaders[n];
            var colon = hdr.indexOf(":");
            var key = hdr.substr(0, colon);
            var value = hdr.substr(colon + 1);
            //console.log(key+": "+value);
            lastStatus.set(key, value);
        }
        var scannerID = lastStatus.get('ScannerID');
        if (!scannerID) {
            console.log("Skipping emtpy msg, %0", msgHeaders);
            return;
        }
        if (typeof lastStatus.get("SystemType") === 'undefined')
            lastStatus.set("SystemType", "default");
        // Get the ScannerHealth object for the scannerID
        var scannerHealth = this.scannerStatus.get(scannerID);
        if (typeof scannerHealth === 'undefined') {
            scannerHealth = new ScannerStatus.ScannerHealth(scannerID);
            this.scannerStatus.set(scannerID, scannerHealth);
            console.log("Added scanner: %s, %O", scannerID, this.scannerStatus);
        }
        scannerHealth.setLastStatus(lastStatus);
        scannerHealth.setTime(+new Date());
    };
    StatusController.prototype.subscribe = function () {
        var command = ["SUBSCRIBE"];
        command.push("id:0");
        command.push("destination:/topic/scannerHealth");
        command.push(StatusController.LF);
        var msg = command.join(StatusController.LF);
        msg = msg + StatusController.NULL;
        console.log(msg);
        this.$scope.client.send(msg);
    };
    StatusController.prototype.error = function (headers) {
    };
    StatusController.prototype.setScanner = function (scanner) {
        this.$scope.scanner = scanner;
    };
    StatusController.prototype.getScanner = function () {
        return this.$scope.scanner;
    };
    StatusController.prototype.getScannerStatus = function () {
        var scanners = new Array();
        this.scannerStatus.forEach(function (scanner, k) {
            scanners.push(scanner);
        });
        return scanners;
    };
    StatusController.prototype.disconnect = function () {
        //client.disconnect();
        this.$scope.connected = false;
    };
    StatusController.LF = '\x0A';
    StatusController.NULL = '\x00';
    StatusController.$inject = [
        '$scope',
        '$interval',
    ];
    return StatusController;
})();
//# sourceMappingURL=StatusController.js.map