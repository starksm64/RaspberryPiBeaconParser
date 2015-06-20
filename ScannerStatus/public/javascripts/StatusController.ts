/// <reference path='paths.ts' />
'use strict';

/**
 * The main controller for the app. The controller:
 * - retrieves and persists the model via the todoStorage service
 * - exposes the model to the template and provides event handlers
 */
class StatusController {
    static LF: any = '\x0A';
    static NULL: any = '\x00';
    public static $inject = [
        '$scope',
        '$interval',
    ];
    private scannerStatus: Map<string, ScannerStatus.ScannerHealth>;

    constructor(private $scope:ScannerStatus.IStatusScope,
                private $interval:ng.IIntervalService) {
        $scope.scanner = 0;
        this.scannerStatus = $scope.scannerStatus = new Map<string, ScannerStatus.ScannerHealth>();
        // Use lambda to capture correct 'this'
        var callback = () => {
            this.updateStatusView();
        };
        $interval(callback, 15000);

        $scope.client = new WebSocket("ws://184.72.167.147:61614/stomp");
        $scope.client.addEventListener("open", (evt: Event) => {
            console.log("open: %O", evt);
            var command = ["CONNECT"];
            command.push("accept-version:1.2");
            command.push(StatusController.LF);
            var msg = command.join(StatusController.LF);
            msg = msg + StatusController.NULL;
            console.log(msg);
            $scope.client.send(msg);
        });
        $scope.client.addEventListener("error", (evt: ErrorEvent) => {
            console.log("error: %O", evt);
        });
        $scope.client.addEventListener("close", (evt: CloseEvent) => {
            console.log("close: %O", evt);
        });
        var messageCallback = (message: any) => {
            this.messageCallback(message);
        };
        $scope.client.addEventListener("message", messageCallback, true);
        $scope.connected = true;
    }

    /**
     * Iterate over the scanner status and update the sinceLastStatus
     */
    updateStatusView() : void {
        console.log("updateStatusView...");
        var nowMS = +new Date();
        this.scannerStatus.forEach((scanner:ScannerStatus.ScannerHealth,k:string) => {
            scanner.updateSinceLastStatus(nowMS);
        });
    }

    messageCallback(message: any) : void {
        //console.log("%O", message);
        if(message.data) {
            var headers: string[] = message.data.split(StatusController.LF);
            //console.log("%O", headers);
            if(headers[0] === "MESSAGE") {
                this.message(headers);
            } else if(headers[0] === "CONNECTED") {
                this.subscribe();
            } else if(headers[0] === "ERROR") {
                this.error(headers);
                this.disconnect();
            }
        }
    }

    message(msgHeaders: string[]): void {
        var lastStatus = new Map<string,string>();
        //console.log("%O", msgHeaders);
        for(var n in msgHeaders) {
            var hdr: string = msgHeaders[n];
            var colon: number = hdr.indexOf(":");
            var key: string = hdr.substr(0, colon);
            var value: string = hdr.substr(colon+1);
            //console.log(key+": "+value);
            lastStatus.set(key, value);
        }
        var scannerID: string = lastStatus.get('ScannerID');
        if(!scannerID) {
            console.log("Skipping emtpy msg, %0", msgHeaders);
            return;
        }
        if(typeof lastStatus.get("SystemType") === 'undefined')
            lastStatus.set("SystemType", "default");
        // Get the ScannerHealth object for the scannerID
        var scannerHealth = this.scannerStatus.get(scannerID);
        if(typeof scannerHealth === 'undefined') {
            scannerHealth = new ScannerStatus.ScannerHealth(scannerID);
            this.scannerStatus.set(scannerID, scannerHealth);
            console.log("Added scanner: %s, %O", scannerID, this.scannerStatus);
        }
        scannerHealth.setLastStatus(lastStatus);
        scannerHealth.setTime(+new Date());
    }

    subscribe(): void {
        var command = ["SUBSCRIBE"];
        command.push("id:0");
        command.push("destination:/topic/scannerHealth");
        command.push(StatusController.LF);
        var msg = command.join(StatusController.LF);
        msg = msg + StatusController.NULL;
        console.log(msg);
        this.$scope.client.send(msg);
    }
    error(headers: string[]): void {
    }

    setScanner(scanner: number) : void {
        this.$scope.scanner = scanner;
    }
    getScanner() : number {
        return this.$scope.scanner;
    }

    getScannerStatus() : Array<ScannerStatus.ScannerHealth> {
        var scanners: Array<ScannerStatus.ScannerHealth> = new Array<ScannerStatus.ScannerHealth>();
        this.scannerStatus.forEach((scanner:ScannerStatus.ScannerHealth,k:string) => {
            scanners.push(scanner);
        });
        return scanners;
    }
    disconnect() : void {
        //client.disconnect();
        this.$scope.connected = false;
    }
}
