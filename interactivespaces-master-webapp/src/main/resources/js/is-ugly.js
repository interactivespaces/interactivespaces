/**
 * Copyright (C) 2014 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

function InteractiveSpacesUgly() {
}

InteractiveSpacesUgly.prototype = {
  'initialize' : function() {
    var args = document.location.search.substring(1).split('&');

    var argsParsed = {};

    for (i = 0; i < args.length; i++) {
      arg = unescape(args[i]);

      if (arg.indexOf('=') == -1) {
        argsParsed[arg.trim()] = true;
      } else {
        kvp = arg.split('=');
        argsParsed[kvp[0].trim()] = kvp[1].trim();
      }
    }

    var uglyHost = document.location.hostname;
    var uglyPort = 8090;
    if (argsParsed.isport) {
      uglyPort = argsParsed.isport;
    }

    this.ws = new WebSocket("ws://" + uglyHost + ":" + uglyPort + "/websocket");

    this.ws.onopen = (function(event) {
      this.onWebSocketOpen();
    }).bind(this);

    this.ws.onmessage = (function(event) {
      this.onWebSocketMessage(event);
    }).bind(this);

    this.ws.onclose = (function(event) {
      this.onWebSocketClose();
    }).bind(this);
    
    this.requestId = 0;
  },

  'onWebSocketOpen' : function() {
    console.log("Connected to Interactive Spaces master");
  },

  'onWebSocketClose' : function() {
    console.log("Lost connection to Interactive Spaces master");
  },

  'onWebSocketMessage' : function(event) {
    var message = JSON.parse(event.data);
    console.log(message);
  },

  'onIFrameLoad' : function() {
    var iframeWindow = window.frames["mainContent"].window;
    iframeWindow.ugly = this;

    window.location.hash = iframeWindow.location.pathname;
  },

  'changePage' : function(page) {
    var url = window.location.origin + page;

    $('#mainContent').attr('src', url);
  },

  'executeApi' : function(type, data) {
    var message = {
      'type' : type,
      'data' : data,
      'requestId' : (this.requestId++).toString(16)
    };

    this.ws.send(JSON.stringify(message));
  },

  'setInitialPage' : function() {
    var currentHash = window.location.hash;

    if (currentHash) {
      this.changePage(currentHash.substr(1));
    }
  },
};
