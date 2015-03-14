
//var client = Stomp.client('ws://52.10.252.216:61614');
var client = Stomp.client('ws://localhost:61614');
client.connect('admin', 'admin', function(frame) {
  console.log("connected to Stomp");
  client.subscribe('/queue/beaconEvents', function(message) {
    console.log('Message: ', message);
  });
});
