/*
   Stomp Over WebSocket http://www.jmesnil.net/stomp-websocket/doc/ | Apache License V2.0
   Copyright (C) 2013 [Jeff Mesnil](http://jmesnil.net/)
 */

var StompModule, net, overTCP, wrapTCP;

  StompModule = require('./stomp');
  net = require('./tcp-socket.js');
  console.log("Properties of net:");
  for(var np in net)
  console.log(np);

  StompModule.Stomp.setInterval = function(interval, f) {
    return setInterval(f, interval);
  };

  StompModule.Stomp.clearInterval = function(id) {
    return clearInterval(id);
  };

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

  overTCP = function(host, port) {
    var socket;
    socket = wrapTCP(port, host);
    return StompModule.Stomp.over(socket);
  };

if (typeof window !== "undefined" && window !== null) {
  console.log("Setting window.overTCP, window="+typeof window+", overTCP="+typeof overTCP);
  window.overTCP = overTCP;
} else if (!exports) {
  console.log("Setting self.overTCP, self="+typeof self+", overTCP="+typeof overTCP);
  self.overTCP = overTCP;
}

