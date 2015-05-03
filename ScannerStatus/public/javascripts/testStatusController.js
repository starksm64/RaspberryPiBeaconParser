var statusCtrl = require('./StatusController');
function newStatusScope() {
    var aScope = {};
    aScope.scannerStatus = new Map();
    aScope.scanner = 0;
    aScope.connected = false;
    aScope.client = null;
    return aScope;
}
var $scope;
$scope = newStatusScope();
var MockPromise = (function () {
    function MockPromise() {
    }
    MockPromise.prototype.then = function (successCallback) {
        return new MockPromise();
    };
    MockPromise.prototype.catch = function (onRejected) {
        return new MockPromise();
    };
    MockPromise.prototype.finally = function (finallyCallback) {
        return new MockPromise();
    };
    return MockPromise;
})();
function getIntervalService(func, delay) {
    console.log("interval called");
    return new MockPromise();
}
var getIntervalService;
(function (getIntervalService) {
    function cancel(promise) {
        return false;
    }
    getIntervalService.cancel = cancel;
    ;
})(getIntervalService || (getIntervalService = {}));
var $interval = getIntervalService;
var sc = new statusCtrl.StatusController($scope, $interval);
while ($scope.connected) {
    console.log("Updating statusView...");
    sc.updateStatusView();
}
//# sourceMappingURL=testStatusController.js.map