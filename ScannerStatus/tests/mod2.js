"use strict";

var SomeMod1Class = require('./mod1');

function SomeMod2Class(name) {
    this.name = name;
    this.getInfo = function() {
        return "SomeMod2Class("+name+")";
    }
    this.doSomething = function(arg) {
        var sm1c = new SomeMod1Class(arg);
        var info = sm1c.getInfo();
        return "SomeMod2Class("+name+"), info="+info;
    }
}
module.exports = SomeMod2Class;
