(function () {
    var app = angular.module('statusHealth', ['AngularStompDK']);

    angular.module('statusHealth')
        .config(function (ngstompProvider) {
            ngstompProvider
                //.url('ws://localhost:61614/stomp')
                .url('ws://52.10.252.216:61614/stomp')
                .credential('guest', 'guest')
        });

    app.controller('StatusController', function ($scope, $interval, ngstomp) {
        var status = this;
        this.scannerStatus = {};
        this.scanner = 0;
        console.log('connected to Stomp');
        this.connectedState = 'Connected';
        ngstomp.subscribe('/topic/scannerHealth', messageCallback);
        //dumpObject(message.headers);

        $interval(updateStatusView, 15000);

        /**
         * Iterate over the scanner status and update the sinceLastStatus
         */
        function updateStatusView() {
            console.log("updateStatusView...");
            var nowMS = +new Date();
            for(var scannerID in status.scannerStatus) {
                var scanner = status.scannerStatus[scannerID];
                scanner.updateSinceLastStatus(nowMS);
            }
        }

        function messageCallback(message) {
            //console.log("%O", message);
            var msgHeaders = {};

            var key;
            for (key in message.headers) {
                //console.log("Copying: " + key);
                msgHeaders[key] = message.headers[key];
            }
            var bodyParts = message.body.split("\n");
            //console.log("bodyParts: %O", bodyParts);
            for(var n = 0; n < bodyParts.length; n ++) {
                var line = bodyParts[n];
                var colon = line.indexOf(":");
                var pair = [line.substring(0, colon), line.substring(colon+1)];
                //console.log("Testing: " + line);
                if(line.length > 0) {
                    //console.log("Copying: " + line);
                    msgHeaders[pair[0]] = pair[1];
                }
            }
            var scannerID = msgHeaders['ScannerID'];
            if(!scannerID) {
                console.log("Skipping emtpy msg");
                return;
            }
            //console.log(scannerID+", msgHeaders: %O", msgHeaders);
            /*
            console.log(scannerID);
            console.log(msgHeaders['SystemTime']);
            console.log(msgHeaders['Uptime']);
            console.log(msgHeaders['HostIPAddress']);
            console.log(msgHeaders['HeartbeatCount']);
            console.log(msgHeaders['HeartbeatRSSI']);
            */
            if(typeof msgHeaders["SystemType"] === 'undefined')
                msgHeaders["SystemType"] = "default";
            // Get the ScannerHealth object for the scannerID
            var scannerHealth = status.scannerStatus[scannerID];
            if(typeof scannerHealth === 'undefined') {
                scannerHealth = new ScannerHealth(scannerID);
                status.scannerStatus[scannerID] = scannerHealth;
            }
            scannerHealth.setLastStatus(msgHeaders);
            scannerHealth.setTime(+new Date());
            //status.scannerStatus[scannerID] = msgHeaders;
        }
        this.setScanner = function(scanner) {
            this.scanner = scanner;
            this.active = scanner;
        };
        this.getScanner = function(){
            return this.scanner;
        };

        this.disconnect = function () {
            client.disconnect();
            this.connectedState = 'Disconnected';
        };

    });

})();
