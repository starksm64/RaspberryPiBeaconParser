/* A stub nodejs stomp socket setup that will be browserify'd for use in browser
 browserify stomp-in.js >stompbundle.js

 Then include in app html:
 <script type="text/javascript" src="stompbundle.js"></script>

 Use the Stomp global to connect to stomp server:
 var client = Stomp.overTCP('52.10.252.216', 61613);
 client.connect('guest', 'guest', function (frame) {
     console.log('connected to Stomp');

     client.subscribe('/topic/mytopic', function (message) {
         console.log("+++ received message: ", typeof message);
         var msgHeaders = {};

         var key;
         for (key in message.headers) {
             msgHeaders[key] = message.headers[key];
         }

     });

 });
*/
net = require('net');
var StompAPI = require('./stompmod.js');
