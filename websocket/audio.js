(function () {
    var app = angular.module('audio', []);

    app.controller('AudioController', function ($scope, $interval) {
        var status = this;
        this.alertActive = false;
        this.alertSound = new Audio("alert.wav");

        $interval(toggleSound, 15000);

        /**
         * Iterate over the scanner status and update the sinceLastStatus
         */
        function toggleSound() {
            console.log("toggleSound...");
            if(status.alertActive) {
                status.alertActive = false;
                status.alertSound.pause();
            } else {
                status.alertActive = true;
                status.alertSound.play();
            }
        }
    });

})();
