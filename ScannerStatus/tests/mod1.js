"use strict";

function SomeMod1Class(name) {
    this.name = name;
    this.getInfo = function() {
        return "SomeMod1Class("+name+")";
    }
}
module.exports = SomeMod1Class;
