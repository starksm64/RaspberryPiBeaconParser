(function(){
    var app = angular.module('activemq-directives', []);
    var stomp = require('./stompmod.js');
    var client = stomp.overTCP('localhost', 61613);

    app.directive("statusHeartbeats", function() {
      return {
        restrict: "E",
        templateUrl: "heartbeats.html",
        controller: function() {
            var status = this;
            status.heartbeats = [];
            client.connect('guest', 'guest', function(frame) {
              console.log('connected to Stomp');
              this.status = 'Connected';
              client.subscribe('/topic/scannerHealth', function(message) {
                console.log("+++ received message");
                  status.heartbeats.push(message.headers);
                //dumpObject(message.headers);
              });
            });
            this.disconnect = function() {
                client.disconnect();
                this.status = 'Disconnected';
            };
        },
        controllerAs: "status"
      };
    });
})();