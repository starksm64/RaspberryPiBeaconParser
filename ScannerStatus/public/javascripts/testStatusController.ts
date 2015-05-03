/// <reference path='paths.ts' />
import statusScope = require('./IStatusScope');
import ScannerHealth = require('./scannerHealth');
import statusCtrl = require('./StatusController');

function newStatusScope(): statusScope.IStatusScope {
    var aScope: any = {};
    aScope.scannerStatus = new Map<string, ScannerHealth.ScannerHealth>();
    aScope.scanner = 0;
    aScope.connected = false;
    aScope.client = null;
    return aScope;
}

var $scope:statusScope.IStatusScope;
$scope = newStatusScope();

class MockPromise implements ng.IPromise<any> {

    constructor(){}

    then<TResult>(successCallback:(p1:any) => ng.IHttpPromise<TResult>) : ng.IPromise<any> {
        return new MockPromise();
    }

    catch<TResult>(onRejected:(p1:any) => ng.IHttpPromise<TResult>) : ng.IPromise<any> {
        return new MockPromise();
    }

    finally<TResult>(finallyCallback:()=>any) : ng.IPromise<any> {
        return new MockPromise();
    }
}

function getIntervalService(func: Function, delay: number) : ng.IPromise<any> {
    console.log("interval called");
    return new MockPromise();
}
module getIntervalService {
    export function cancel(promise: ng.IPromise<any>): boolean {
        return false;
    };
}

var $interval: ng.IIntervalService = getIntervalService;

var sc: statusCtrl.StatusController = new statusCtrl.StatusController($scope, $interval);
while($scope.connected) {
    console.log("Updating statusView...");
    sc.updateStatusView();
}