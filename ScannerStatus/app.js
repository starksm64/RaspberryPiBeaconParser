/// <reference path="typings/node/node.d.ts"/>
var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');

var app = express();
var expressWs = require('express-ws')(app);

// uncomment after placing your favicon in /public
//app.use(favicon(__dirname + '/public/favicon.ico'));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));
app.use(express.static('/node_modules', path.join(__dirname, 'node_modules')));

// Handle
app.get('/node_modules/bootstrap/dist/css/bootstrap.min.css',function(req,res) {
    res.sendFile(path.join(__dirname,'node_modules','bootstrap/dist/css','bootstrap.min.css'));
});
app.get('/node_modules/bootstrap/dist/js/bootstrap.min.js',function(req,res) {
    res.sendFile(path.join(__dirname,'node_modules','bootstrap/dist/js','bootstrap.min.js'));
});
app.get('/node_modules/bootstrap/dist/fonts/glyphicons-halflings-regular.woff',function(req,res) {
    res.sendFile(path.join(__dirname,'node_modules','bootstrap/dist/fonts','glyphicons-halflings-regular.woff'));
});
app.get('/node_modules/bootstrap/dist/fonts/glyphicons-halflings-regular.woff2',function(req,res) {
    res.sendFile(path.join(__dirname,'node_modules','bootstrap/dist/fonts','glyphicons-halflings-regular.woff2'));
});

app.get('/node_modules/angular/angular.js',function(req,res) {
    res.sendFile(path.join(__dirname,'node_modules','angular','angular.js'));
});
app.get('/node_modules/angular/angular.min.js',function(req,res) {
    res.sendFile(path.join(__dirname,'node_modules','angular','angular.min.js'));
});
app.get('/node_modules/angular/angular.min.js.map',function(req,res) {
    res.sendFile(path.join(__dirname,'node_modules','angular','angular.min.js.map'));
});

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  var err = new Error('Not Found');
  err.status = 404;
  next(err);
});

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
  app.use(function(err, req, res, next) {
    res.status(err.status || 500);
    res.render('error', {
      message: err.message,
      error: err
    });
  });
}

module.exports = app;
