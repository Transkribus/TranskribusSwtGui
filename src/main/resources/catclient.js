
	    sayHello = function(what) {	    	
	    	println("hello world: "+what);
	    };
	    
//(function(module, global){
//	println("module = "+module);
//	println("global = "+global);

	
//	  include('socket.io');
//	load(['socket.io']);
//	engine.eval(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream("socket.io.js")));
	
	  var CatClient = function() {
	    var self = this;
	
	    self.debug  = false;
	    self.server = null;
	    
//	    self.hello = function() {
//	    	
//	    	println("hello!!!!");
//	    }
	    
	    /**
	    * Initialization method
	    * @param url {String} Server URL to connect to
	    */
	    self.connect = function(url) {
	      
	      var ioOptions = {}
	      var match = /^(.*)@(\d+)(.*)$/.exec(url);
	      if (match) {
	        url = match[1] + match[3];
	        var port = match[2];
	        println("port = "+port);
	        println("url = "+url);
	        println("Use reverse proxy for "+ url+ " at port "+ port);
	        ioOptions.resource = 'socket.io/p' + port;
	        // avoid socket.io socket cache. Since we are redirecting 
	        // sockets with url and not machine:port, socket.io cache
	        // uses the same connection for htr@port and itp@port
	        ioOptions['force new connection'] = true;
	      }
	       
	      self.server = new io.connect(url, ioOptions);
	
	      if (self.debug) {
	        var emit = self.server.emit;
	        self.server.emit = function() {
	          if (arguments.length === 2) {
	            println("emit", [arguments[0]], arguments[1]);
	          } else {
	            println("emit", arguments);
	          }
	          emit.apply(this, arguments);
	        }
	      }
	    };
	    
	    /**
	    * Event handler
	    * @param ev {Mixed} String or Array of strings name of trigger
	    * @param fn {Function} Callback
	    */
	    self.on = function(ev, fn) {
	      if (typeof ev === 'string') {
	        ev = [ev];
	      }
	      for (e in ev) {
	        self.server.on(ev[e], function(obj){
	          if (obj) {
	            try {
	              if (self.debug && obj.errors && obj.errors.length > 0) {
	                console.error(obj.errors);
	              }
	            } catch (err) {
	              // Probably obj.errors is undefined
	            } finally {
	              if (self.debug) {
	                var msg = "received";
	                if (obj && obj.data && obj.data.elapsedTime) {
	                  msg = msg + " (" + obj.data.elapsedTime.toFixed(1) + "ms)";
	                }
	                if (arguments.length === 1) {
	                  println(msg, [ev[e]], arguments[0]);
	                } else {
	                  println(msg, [ev[e]], arguments);
	                }
	              }
	              fn(obj.data, obj.errors);
	            }
	          } else {
	            fn();
	          }
	        });
	      }
	    };
	
	    /**
	    * Trigger event 
	    */
	    self.trigger = function() {
	      //println("trigger", arguments);
	      self.server.$emit.apply(self.server, arguments);
	    };
	
	    /**
	    * removeAllListeners 
	    */
	    self.removeAllListeners = function() {
	      self.server.$events = {};
	    };
	
	    
	    /**
	    * Check connection status 
	    */
	    self.isConnected = function() {
	      return self.server.socket.open;
	    };
	
	    
	    /**
	    * Tries to reconnect if connection drops.
	    */
	    self.checkConnection = function() {
	      if (!self.server.socket.open) {
	        self.server.socket.reconnect();
	      }
	    };
	
	    /** 
	    * Retrieves decoding results for the current segment.
	    * @param {Object}
	    * @setup obj
	    *   ms {Number}
	    * @trigger pingResult
	    * @return {Object} 
	    *   errors {Array} List of error messages
	    *   data {Object}
	    *   @setup data
	    *     ms {Number} Original ms
	    *     elapsedTime {Number} ms 0 by definition
	    */
	    self.ping = function(obj) {
	      self.checkConnection();
	      self.server.emit('ping', {data: obj});
	    };
	        
	    /** 
	    * Configures server as specified by the client.
	    * @param {Object} Server-specific configuration
	    * @trigger configureResult
	    * @return {Object}
	    *   errors {Array} List of error messages
	    *   data {Object} Configuration after setting the server
	    *   @setup data
	    *     config {Object} Server-specific configuration
	    *     elapsedTime {Number} ms    
	    */
	    self.configure = function(obj) {
	      self.checkConnection();
	      self.server.emit('configure', {data: obj});
	    };
	    
	    /** 
	    * Validates source-target pair.
	    * @param {Object} 
	    * @setup obj
	    *   source {String}
	    *   target {String}
	    * @trigger validateResult
	    * @return {Object} 
	    *   errors {Array} List of error messages
	    *   data {Object}
	    *   @setup data
	    *     elapsedTime {Number} ms
	    */
	    self.validate = function(obj) {
	      self.checkConnection();
	      self.server.emit('validate', {data: obj});
	    };
	
	    /** 
	    * Resets server.
	    * @trigger resetResult
	    * @return {Object} 
	    *   errors {Array} List of error messages
	    *   data {Object} Response data
	    *   @setup data
	    *     elapsedTime {Number} ms
	    */
	    self.reset = function() {
	      self.checkConnection();
	      self.server.emit('reset');
	    };
	
	    /** 
	    * Retrieves server configuration.
	    * @trigger getServerConfigResult
	    * @return {Object} 
	    *   errors {Array} List of error messages
	    *   data {Object} Response data
	    *   @setup data
	    *     config {Object} Server-specific configuration
	    *     elapsedTime {Number} ms
	    */
	    self.getServerConfig = function() {
	      self.checkConnection();
	      self.server.emit('getServerConfig');
	    };
	
	    /** 
	    * Retrieves decoding results for the current segment.
	    * @param {Object}
	    * @setup obj
	    *   source {String}
	    *   numResults {Number} How many results should be retrieved
	    * @trigger decodeResult
	    * @return {Object} 
	    *   errors {Array} List of error messages
	    *   data {Object}
	    *   @setup data
	    *     source {String}
	    *     sourceSegmentation {Array} Verified source segmentation
	    *     elapsedTime {Number} ms
	    *     nbest {Array} List of objects
	    *     @setup nbest
	    *       target {String} Result
	    *       targetSegmentation {Array} Segmentation of result
	    *       elapsedTime {Number} ms
	    *       [author] {String} Technique or person that generated the target result
	    *       [alignments] {Array} Dimensions: source * target
	    *       [confidences] {Array} List of floats for each token
	    *       [quality] {Number} Quality measure of overall hypothesis    
	    */
	    self.decode = function(obj) {
	      self.checkConnection();
	      self.server.emit('decode', {data: obj});
	    };
	    
	    /** 
	    * Retrieves tokenization results for the current segment.
	    * @param {Object}
	    * @setup obj
	    *   source {String}
	    *   target {String}
	    * @trigger getTokensResult
	    * @return {Object} 
	    *   errors {Array} List of error messages
	    *   data {Object}
	    *   @setup data
	    *     source {String}
	    *     sourceSegmentation {Array} Verified source segmentation
	    *     target {String} Result
	    *     targetSegmentation {Array} Segmentation of result 
	    *     elapsedTime {Number} ms
	    */
	    self.getTokens = function(obj) {
	      self.checkConnection();
	      self.server.emit('getTokens', {data: obj});
	    };
	
	    /** 
	    * Retrieves alignment results  for the current segment.
	    * @param {Object}    
	    * @setup obj
	    *   source {String}
	    *   target {String}
	    * @trigger getAlignmentsResult
	    * @return {Object} 
	    *   errors {Array} List of error messages
	    *   data {Object}
	    *   @setup data
	    *     alignments {Array} Dimensions: source * target
	    *     source {String}
	    *     sourceSegmentation {Array} Verified source segmentation
	    *     target {String} Result
	    *     targetSegmentation {Array}    
	    *     elapsedTime {Number} ms
	    */
	    self.getAlignments = function(obj) {
	      self.checkConnection();
	      self.server.emit('getAlignments', {data: obj});
	    };
	
	    /** 
	    * Retrieves confidence results for the current segment.
	    * @param {Object} 
	    * @setup obj
	    *   source {String}
	    *   target {String}
	    *   validatedTokens {Array} List of Booleans, where 1 indicates that the token is validated
	    * @trigger getConfidencesResult
	    * @return {Object}
	    *   errors {Array} List of error messages
	    *   data {Object}
	    *   @setup data
	    *     quality {Number} Quality measure of overall hypothesis
	    *     confidences {Array} List of floats for each token
	    *     source {String} Verified source
	    *     sourceSegmentation {Array} Verified source segmentation
	    *     target {String} Result
	    *     targetSegmentation {Array}
	    *     elapsedTime {Number} ms
	    */
	    self.getConfidences = function(obj) {
	      self.checkConnection();
	      self.server.emit('getConfidences', {data: obj});
	    };
	
	    /** 
	    * Retrieves contributions that users completed after full supervision.
	    * @trigger getValidatedContributionsResult
	    * @return {Object}
	    *   errors {Array} List of error messages
	    *   data {Object}
	    *   @setup data
	    *     contributions {Array} List of validated contributions
	    *     @setup contributions
	    *       source {String} Validated source
	    *       target {String} Validated target
	    *     elapsedTime {Number} ms
	    */
	    self.getValidatedContributions = function() {
	      self.checkConnection();
	      self.server.emit('getValidatedContributions');
	    };
	
	    /** 
	    * Adds a replacement rule.
	    * @param {Object} 
	    * @setup obj
	    *   [ruleId] {Number}
	    *   sourceRule {String}
	    *   targetRule {String}
	    *   targetReplacement {String}
	    *   matchCase {Boolean}
	    *   isRegExp {Boolean}
	    *   persistent {Boolean} TODO
	    * @trigger setReplacementRuleResult
	    * @return {Object}
	    *   errors {Array} List of error messages
	    *   data {Object}
	    *   @setup data
	    *     elapsedTime {Number} ms
	    *     ruleId {Number} ruleId of the rule 
	    */    
	    self.setReplacementRule = function(obj) {
	      self.checkConnection();
	      self.server.emit('setReplacementRule', {data: obj});
	    };
	
	    /** 
	    * Returns the list of rules.
	    * @trigger getReplacementRulesResult
	    * @return {Object}
	    *   errors {Array} List of error messages
	    *   data {Object}
	    *   @setup data
	    *     elapsedTime {Number} ms
	    *     rules {Array} List of rules
	    *     @setup rules
	    *       ruleId {Number}
	    *       sourceRule {String}
	    *       targetRule {String}
	    *       targetReplacement {String}
	    *       isRegExp {Boolean}
	    *       matchCase {Boolean}
	    *       nFails {Number} Number of times the regex provoked an exception
	    *       persistent {Boolean} TODO
	    */    
	    self.getReplacementRules = function(obj) {
	      self.checkConnection();
	      self.server.emit('getReplacementRules', {data: obj});
	    };
	
	    /** 
	    * Deletes replacement rule.
	    * @param {Object} 
	    * @setup obj
	    *   ruleId {Number}
	    * @trigger delReplacementRuleResult
	    * @return {Object}
	    *   errors {Array} List of error messages
	    *   data {Object}
	    *   @setup data
	    *     elapsedTime {Number} ms
	    */    
	    self.delReplacementRule = function(obj) {
	      self.checkConnection();
	      self.server.emit('delReplacementRule', {data: obj});
	    };
	
	    /** 
	    * Applies *all* replacement rules, so that the user does not need to type for an entered rule to become visible.
	    * @param {Object}
	    * @setup obj
	    *   source {String}
	    *   target {String}
	    * @trigger applyReplacementRulesResult
	    * @return {Object}
	    *   errors {Array} List of error messages
	    *   data {Object}
	    *   @setup data
	    *     elapsedTime {Number} ms
	    */    
	    self.applyReplacementRules = function(obj) {
	      self.checkConnection();
	      self.server.emit('applyReplacementRules', {data: obj});
	    };    
	
	  };
	
//	  module.exports = CatClient;
	
//	})('object' === typeof module ? module : {}, this);