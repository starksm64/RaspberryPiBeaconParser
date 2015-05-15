var net = require('net');
Stomp = require('./stomp');

wrapTCP = function(port, host) {
  var socket, ws;
  socket = null;
  ws = {
    url: 'tcp:// ' + host + ':' + port,
    send: function(d) {
      return socket.write(d);
    },
    close: function() {
      return socket.end();
    }
  };
  socket = net.connect(port, host, function(e) {
    return ws.onopen();
  });
  socket.on('error', function(e) {
    return typeof ws.onclose === "function" ? ws.onclose(e) : void 0;
  });
  socket.on('close', function(e) {
    return typeof ws.onclose === "function" ? ws.onclose(e) : void 0;
  });
  socket.on('data', function(data) {
    var event;
    event = {
      'data': data.toString()
    };
    return ws.onmessage(event);
  });
  return ws;
};
