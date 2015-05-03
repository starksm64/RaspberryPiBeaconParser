/// <reference path='paths.ts' />

module ScannerStatus {
    export interface IStatusScope extends ng.IScope {
        scannerStatus: Map<string, ScannerStatus.ScannerHealth>;
        scanner: number;
        connected: boolean;
        client: WebSocket;
    }
}
