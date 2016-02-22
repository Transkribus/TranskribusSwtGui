/**
 * 
 */

//include('socket.io');

sayHello("hellooo");

//var m = require('CatClient');

var cc = new CatClient();

url = "http://casmacat.iti.upv.es@3001/casmacat";
cc.connect(url);