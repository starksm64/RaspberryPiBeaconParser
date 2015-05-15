angular.module('ui.bootstrap.demo', ['ui.bootstrap']);
angular.module('ui.bootstrap.demo').controller('CarouselDemoCtrl', function ($scope) {
  $scope.myInterval = 5000;
  var slides = $scope.slides = [
      {
          image: 'images/IntelNUC.png',
            text: 'IntelNUC'
      }, {
          image: 'images/Pi2B.png',
              text: 'Raspberry Pi 2 B'
      }, {
          image: 'images/PiB+.png',
              text: 'Raspberry Pi B+'
      }, {
          image: 'images/default.png',
            text: 'Raspberry Pi Generic'
      }];

});